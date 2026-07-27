#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..');
const SOURCE_ROOTS = [
  path.join(ROOT, 'src', 'main', 'resources', 'data'),
  path.join(ROOT, 'src', 'main', 'generated', 'data'),
];
const FAMILIES = [
  { name: 'recipe', forge: 'recipes', neo: 'recipe', recipe: true },
  { name: 'loot_table', forge: 'loot_tables', neo: 'loot_table' },
  { name: 'advancement', forge: 'advancements', neo: 'advancement' },
  { name: 'structure', forge: 'structures', neo: 'structure' },
  { name: 'tag_block', forge: 'tags/blocks', neo: 'tags/block' },
  { name: 'tag_item', forge: 'tags/items', neo: 'tags/item' },
  { name: 'tag_entity_type', forge: 'tags/entity_types', neo: 'tags/entity_type' },
  { name: 'tag_fluid', forge: 'tags/fluids', neo: 'tags/fluid' },
];

// Exact logical IDs only. Adding a one-sided file requires an owner and a deliberate edit here.
const EXCEPTIONS = new Map([
  ['tag_item:abyssalcraft:enchantable/staff_of_rending.json',
    { side: 'neo', owner: 'EnchantmentCompat / EnchantmentMatrixSelfTest' }],
  ['tag_block:abyssalcraft:incorrect_for_abyssalnite_tool.json',
    { side: 'neo', owner: 'ACTagData Layout.NEO / ToolCompat' }],
  ['tag_block:abyssalcraft:incorrect_for_dreadium_tool.json',
    { side: 'neo', owner: 'ACTagData Layout.NEO / ToolCompat' }],
  ['tag_block:abyssalcraft:incorrect_for_ethaxium_tool.json',
    { side: 'neo', owner: 'ACTagData Layout.NEO / ToolCompat' }],
  ['tag_block:abyssalcraft:incorrect_for_refined_coralium_tool.json',
    { side: 'neo', owner: 'ACTagData Layout.NEO / ToolCompat' }],
  ['tag_block:minecraft:incorrect_for_wooden_tool.json',
    { side: 'neo', owner: 'ACTagData Layout.NEO / vanilla 1.21 tier contract' }],
  ['tag_block:minecraft:incorrect_for_stone_tool.json',
    { side: 'neo', owner: 'ACTagData Layout.NEO / vanilla 1.21 tier contract' }],
  ['tag_block:minecraft:incorrect_for_iron_tool.json',
    { side: 'neo', owner: 'ACTagData Layout.NEO / vanilla 1.21 tier contract' }],
  ['tag_block:minecraft:incorrect_for_diamond_tool.json',
    { side: 'neo', owner: 'ACTagData Layout.NEO / vanilla 1.21 tier contract' }],
  ['tag_block:minecraft:incorrect_for_netherite_tool.json',
    { side: 'neo', owner: 'ACTagData Layout.NEO / vanilla 1.21 tier contract' }],
  ['tag_block:forge:needs_netherite_tool.json',
    { side: 'forge', owner: 'ACTagData Layout.FORGE / TierSortingRegistry' }],
  ['tag_block:neoforge:needs_netherite_tool.json',
    { side: 'neo', owner: 'ACTagData Layout.NEO / NeoForge tier tag' }],
  ['tag_entity_type:minecraft:arthropod.json',
    { side: 'neo', owner: 'Minecraft 1.21 entity type classification' }],
  ['tag_entity_type:minecraft:can_breathe_under_water.json',
    { side: 'neo', owner: 'Minecraft 1.21 entity type classification' }],
  ['tag_entity_type:minecraft:undead.json',
    { side: 'neo', owner: 'Minecraft 1.21 entity type classification' }],
]);

const failures = [];
const entries = new Map();
const physical = new Map();
const stats = Object.fromEntries(FAMILIES.map(family => [family.name, {
  logical: 0, paired: 0, exceptions: 0, schema: 0,
}]));
let stale = 0;

function walk(directory) {
  if (!fs.existsSync(directory)) return [];
  const files = [];
  for (const entry of fs.readdirSync(directory, { withFileTypes: true })) {
    const absolute = path.join(directory, entry.name);
    if (entry.isDirectory()) files.push(...walk(absolute));
    else files.push(absolute);
  }
  return files;
}

function relative(file) {
  return path.relative(ROOT, file).replaceAll('\\', '/');
}

function familyFor(relativeToData) {
  const parts = relativeToData.split('/');
  if (parts.length < 3) return null;
  const namespace = parts.shift();
  for (const family of FAMILIES) {
    for (const [side, directory] of [['forge', family.forge], ['neo', family.neo]]) {
      const directoryParts = directory.split('/');
      if (directoryParts.every((part, index) => parts[index] === part)) {
        const resourcePath = parts.slice(directoryParts.length).join('/');
        if (resourcePath) return { family, side, namespace, resourcePath };
      }
    }
  }
  return null;
}

function validate(file, family, side) {
  if (!file.endsWith('.json')) return;
  let json;
  try {
    json = JSON.parse(fs.readFileSync(file, 'utf8'));
  } catch (error) {
    failures.push(`invalid JSON ${relative(file)}: ${error.message}`);
    return;
  }
  if (family.recipe) {
    const result = json.result;
    if (result && typeof result === 'object' && !Array.isArray(result)) {
      const expected = side === 'forge' ? 'item' : 'id';
      const forbidden = side === 'forge' ? 'id' : 'item';
      if (typeof result[expected] !== 'string' || forbidden in result) {
        failures.push(`recipe result schema ${relative(file)} expected=result.${expected}`);
      } else {
        stats[family.name].schema++;
      }
    } else if (typeof result !== 'string') {
      failures.push(`recipe result schema ${relative(file)} expected object or string`);
    } else {
      stats[family.name].schema++;
    }
  }
}

for (const root of SOURCE_ROOTS) {
  for (const file of walk(root)) {
    const dataPath = path.relative(root, file).replaceAll('\\', '/');
    const parsed = familyFor(dataPath);
    if (!parsed) continue;
    const physicalKey = `${parsed.namespace}/${parsed.side === 'forge' ? parsed.family.forge : parsed.family.neo}/${parsed.resourcePath}`;
    if (physical.has(physicalKey)) {
      stale++;
      failures.push(`stale duplicate ${physicalKey}: ${relative(physical.get(physicalKey))} and ${relative(file)}`);
      continue;
    }
    physical.set(physicalKey, file);
    const logicalKey = `${parsed.family.name}:${parsed.namespace}:${parsed.resourcePath}`;
    const logical = entries.get(logicalKey) || { family: parsed.family, sides: {} };
    validate(file, parsed.family, parsed.side);
    logical.sides[parsed.side] = { file };
    entries.set(logicalKey, logical);
  }
}

for (const [logicalKey, logical] of entries) {
  const familyStats = stats[logical.family.name];
  familyStats.logical++;
  const forge = logical.sides.forge;
  const neo = logical.sides.neo;
  const exception = EXCEPTIONS.get(logicalKey);
  if (forge && neo) {
    familyStats.paired++;
    if (exception) failures.push(`stale exception is now paired ${logicalKey}`);
  } else if (!exception) {
    failures.push(`missing ${forge ? 'neo' : 'forge'} pair ${logicalKey}`);
  } else {
    const actualSide = forge ? 'forge' : 'neo';
    if (exception.side !== actualSide) {
      failures.push(`exception side mismatch ${logicalKey} expected=${exception.side} actual=${actualSide}`);
    } else {
      familyStats.exceptions++;
    }
  }
}

for (const [logicalKey, exception] of EXCEPTIONS) {
  if (!entries.has(logicalKey)) failures.push(`declared exception missing ${logicalKey} owner=${exception.owner}`);
}

const totals = Object.values(stats).reduce((sum, value) => ({
  logical: sum.logical + value.logical,
  paired: sum.paired + value.paired,
  exceptions: sum.exceptions + value.exceptions,
  schema: sum.schema + value.schema,
}), { logical: 0, paired: 0, exceptions: 0, schema: 0 });
const detail = FAMILIES.map(({ name }) => {
  const value = stats[name];
  return `${name}=${value.logical}/${value.paired}/${value.exceptions}`;
}).join(' ');

if (failures.length > 0) {
  console.error(`RR_DATAPACK_PLURAL_AUDIT_FAILED failures=${failures.length} stale=${stale}`);
  failures.slice(0, 25).forEach(failure => console.error(`- ${failure}`));
  process.exitCode = 1;
} else {
  console.log(`RR_DATAPACK_PLURAL_AUDIT_OK logical=${totals.logical} paired=${totals.paired} exceptions=${totals.exceptions} stale=${stale} recipeSchema=${totals.schema} ${detail}`);
}