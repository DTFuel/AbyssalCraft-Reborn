#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..');
const DATA_ROOTS = [
  path.join(ROOT, 'src', 'main', 'resources', 'data'),
  path.join(ROOT, 'src', 'main', 'generated', 'data'),
];
const JAVA_ROOT = path.join(ROOT, 'src', 'main', 'java');
const PAIRS = [
  ['recipe', 'recipes'],
  ['loot_table', 'loot_tables'],
  ['advancement', 'advancements'],
  ['structure', 'structures'],
  ['predicate', 'predicates'],
  ['item_modifier', 'item_modifiers'],
  ['tags/block', 'tags/blocks'],
  ['tags/item', 'tags/items'],
  ['tags/entity_type', 'tags/entity_types'],
  ['tags/fluid', 'tags/fluids'],
];
const MAPS = [
  ['src/main/java/com/shinoow/abyssalcraft/data/gen/LegacyCraftingRecipeData.java', 'FIELD_OVERRIDES'],
  ['src/main/java/com/shinoow/abyssalcraft/data/gen/LegacyCraftingRecipeData.java', 'LEGACY_ID_OVERRIDES'],
  ['src/main/java/com/shinoow/abyssalcraft/data/gen/EntityLootData.java', 'ITEM_IDS'],
  ['src/main/java/com/shinoow/abyssalcraft/system/knowledge/NecronomiconItemVisuals.java', 'RENAMES'],
];
const NODE_ONLY_PATHS = new Map([
  ['src/main/resources/data/abyssalcraft/tags/item/enchantable/staff_of_rending.json',
    'EnchantmentCompat and EnchantmentMatrixSelfTest: 1.21 datapack enchantment target'],
  ['src/main/generated/data/abyssalcraft/tags/block/incorrect_for_abyssalnite_tool.json',
    'ACTagData Layout.NEO and ToolCompat'],
  ['src/main/generated/data/abyssalcraft/tags/block/incorrect_for_dreadium_tool.json',
    'ACTagData Layout.NEO and ToolCompat'],
  ['src/main/generated/data/abyssalcraft/tags/block/incorrect_for_ethaxium_tool.json',
    'ACTagData Layout.NEO and ToolCompat'],
  ['src/main/generated/data/abyssalcraft/tags/block/incorrect_for_refined_coralium_tool.json',
    'ACTagData Layout.NEO and ToolCompat'],
]);
const CROSS_REGISTRY_COLLISIONS = new Set([
  'abyssalcraft:antichicken', // Legacy item id; still the canonical entity type id.
  'abyssalcraft:coralium', // Legacy item id; still the canonical damage type id.
]);

const failures = [];
const jsonFiles = [];
let pairedFiles = 0;
let recipes = 0;

function walk(directory, predicate = () => true) {
  if (!fs.existsSync(directory)) return [];
  const result = [];
  for (const entry of fs.readdirSync(directory, { withFileTypes: true })) {
    const absolute = path.join(directory, entry.name);
    if (entry.isDirectory()) result.push(...walk(absolute, predicate));
    else if (predicate(absolute)) result.push(absolute);
  }
  return result;
}

function relative(file) {
  return path.relative(ROOT, file).replaceAll('\\', '/');
}

function parseJson(file) {
  try {
    return JSON.parse(fs.readFileSync(file, 'utf8'));
  } catch (error) {
    failures.push(`invalid JSON ${relative(file)}: ${error.message}`);
    return null;
  }
}

function visitStrings(value, consumer) {
  if (typeof value === 'string') consumer(value);
  else if (Array.isArray(value)) value.forEach(entry => visitStrings(entry, consumer));
  else if (value && typeof value === 'object') Object.values(value).forEach(entry => visitStrings(entry, consumer));
}

function auditPairs() {
  for (const root of DATA_ROOTS) {
    for (const [modern, legacy] of PAIRS) {
      const modernRoot = path.join(root, 'abyssalcraft', ...modern.split('/'));
      const legacyRoot = path.join(root, 'abyssalcraft', ...legacy.split('/'));
      const modernFiles = walk(modernRoot);
      const legacyFiles = walk(legacyRoot);
      if (modernFiles.length === 0 && legacyFiles.length === 0) continue;
      for (const [sourceRoot, targetRoot, files] of [
        [modernRoot, legacyRoot, modernFiles],
        [legacyRoot, modernRoot, legacyFiles],
      ]) {
        for (const file of files) {
          const counterpart = path.join(targetRoot, path.relative(sourceRoot, file));
          if (!fs.existsSync(counterpart)) {
            if (!NODE_ONLY_PATHS.has(relative(file))) {
              failures.push(`missing path pair ${relative(file)} -> ${relative(counterpart)}`);
            }
          } else {
            pairedFiles++;
          }
        }
      }
    }
  }
}

function auditRecipes(file, json) {
  const segments = relative(file).split('/');
  const directory = segments[segments.indexOf('abyssalcraft') + 1];
  if (directory !== 'recipe' && directory !== 'recipes') return;
  recipes++;
  const result = json && json.result;
  if (!result || typeof result !== 'object' || Array.isArray(result)) {
    failures.push(`recipe result is not an object ${relative(file)}`);
    return;
  }
  const expected = directory === 'recipe' ? 'id' : 'item';
  const forbidden = directory === 'recipe' ? 'item' : 'id';
  if (typeof result[expected] !== 'string' || forbidden in result) {
    failures.push(`recipe result key ${relative(file)} expected=${expected} actual=${Object.keys(result).join(',')}`);
  }
}

function extractMap(source, name) {
  const start = source.indexOf(` ${name} =`);
  if (start < 0) throw new Error(`mapping ${name} not found`);
  const end = source.indexOf('\n    );', start);
  if (end < 0) throw new Error(`mapping ${name} terminator not found`);
  const entries = [];
  const body = source.slice(start, end);
  const pattern = /(?:Map\.entry\()?"([a-z0-9_:]+)"\s*,\s*"([a-z0-9_:/]+)"\)?/g;
  for (const match of body.matchAll(pattern)) entries.push([match[1], match[2]]);
  if (entries.length === 0) throw new Error(`mapping ${name} has no entries`);
  return entries;
}

function auditRenameMaps(resourceValues) {
  const javaFiles = walk(JAVA_ROOT, file => file.endsWith('.java'));
  const javaSources = new Map(javaFiles.map(file => [relative(file), fs.readFileSync(file, 'utf8')]));
  let mappings = 0;
  let staleReferences = 0;
  for (const [mapFile, name] of MAPS) {
    const source = javaSources.get(mapFile);
    if (!source) {
      failures.push(`rename map owner missing ${mapFile}`);
      continue;
    }
    let entries;
    try {
      entries = extractMap(source, name);
    } catch (error) {
      failures.push(error.message);
      continue;
    }
    for (const [oldValue, newValue] of entries) {
      mappings++;
      if (oldValue === newValue) continue;
      const namespace = newValue.includes(':') ? '' : 'abyssalcraft:';
      const target = namespace + newValue;
      const consumed = [...javaSources.entries()].some(([file, text]) => file !== mapFile && text.includes(`"${newValue}"`))
        || resourceValues.has(target);
      if (!consumed) failures.push(`rename target is not consumed ${name}: ${oldValue} -> ${target}`);
      if (oldValue.includes(':') && resourceValues.has(oldValue) && !CROSS_REGISTRY_COLLISIONS.has(oldValue)) {
        staleReferences++;
        failures.push(`legacy registry id remains in current JSON values ${name}: ${oldValue}`);
      }
    }
  }
  return { mappings, staleReferences };
}

auditPairs();
const resourceValues = new Set();
for (const root of DATA_ROOTS) {
  for (const file of walk(root, candidate => candidate.endsWith('.json'))) {
    jsonFiles.push(file);
    const json = parseJson(file);
    if (json === null) continue;
    visitStrings(json, value => resourceValues.add(value));
    auditRecipes(file, json);
  }
}
const renameStats = auditRenameMaps(resourceValues);

if (failures.length > 0) {
  console.error(`RR_RENAME_AUDIT_FAILED failures=${failures.length}`);
  failures.slice(0, 80).forEach(failure => console.error(`- ${failure}`));
  process.exitCode = 1;
} else {
  console.log(`RR_RENAME_AUDIT_OK json=${jsonFiles.length} paired=${pairedFiles / 2} recipes=${recipes} mappings=${renameStats.mappings} stale=${renameStats.staleReferences}`);
}