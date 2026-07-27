#!/usr/bin/env node

const crypto = require('crypto');
const fs = require('fs');
const path = require('path');
const { openZip } = require('./lib/zip');

const ROOT = path.resolve(__dirname, '..');
const NODES = [
  { id: 'forge', directory: '1.20.1-forge' },
  { id: 'neo', directory: '1.21.1-neoforge' },
];
const PATH_FAMILIES = [
  ['recipe', 'recipes'], ['loot_table', 'loot_tables'], ['advancement', 'advancements'],
  ['structure', 'structures'], ['predicate', 'predicates'], ['item_modifier', 'item_modifiers'],
  ['tags/block', 'tags/blocks'], ['tags/item', 'tags/items'],
  ['tags/entity_type', 'tags/entity_types'], ['tags/fluid', 'tags/fluids'],
];
const ALLOWED_ONE_SIDED = new Map([
  ['data/abyssalcraft/tags/item/enchantable/staff_of_rending.json', 'neo: 1.21 enchantment target'],
  ['data/abyssalcraft/tags/block/incorrect_for_abyssalnite_tool.json', 'neo: 1.21 tool tier'],
  ['data/abyssalcraft/tags/block/incorrect_for_dreadium_tool.json', 'neo: 1.21 tool tier'],
  ['data/abyssalcraft/tags/block/incorrect_for_ethaxium_tool.json', 'neo: 1.21 tool tier'],
  ['data/abyssalcraft/tags/block/incorrect_for_refined_coralium_tool.json', 'neo: 1.21 tool tier'],
  ['data/minecraft/tags/block/incorrect_for_wooden_tool.json', 'neo: 1.21 vanilla tier'],
  ['data/minecraft/tags/block/incorrect_for_stone_tool.json', 'neo: 1.21 vanilla tier'],
  ['data/minecraft/tags/block/incorrect_for_iron_tool.json', 'neo: 1.21 vanilla tier'],
  ['data/minecraft/tags/block/incorrect_for_diamond_tool.json', 'neo: 1.21 vanilla tier'],
  ['data/minecraft/tags/block/incorrect_for_netherite_tool.json', 'neo: 1.21 vanilla tier'],
  ['data/forge/tags/block/needs_netherite_tool.json', 'forge: Forge tier contract'],
  ['data/neoforge/tags/block/needs_netherite_tool.json', 'neo: NeoForge tier contract'],
  ['data/minecraft/tags/entity_type/arthropod.json', 'neo: 1.21 classification'],
  ['data/minecraft/tags/entity_type/can_breathe_under_water.json', 'neo: 1.21 classification'],
  ['data/minecraft/tags/entity_type/undead.json', 'neo: 1.21 classification'],
]);
const failures = [];

function findJar(node) {
  const directory = path.join(ROOT, 'versions', node.directory, 'build', 'libs');
  if (!fs.existsSync(directory)) return null;
  const jars = fs.readdirSync(directory).filter(name => name.endsWith('.jar') && !name.endsWith('-sources.jar'));
  return jars.length === 1 ? path.join(directory, jars[0]) : null;
}

function logicalPath(name, node) {
  if (!/^(?:assets|data)\//.test(name) && name !== 'pack.mcmeta') return null;
  let normalized = name;
  if (normalized.startsWith('data/')) {
    const parts = normalized.split('/');
    const prefix = `${parts[0]}/${parts[1]}/`;
    let resourcePath = parts.slice(2).join('/');
    for (const [modern, legacy] of PATH_FAMILIES) {
      const active = node.id === 'forge' ? legacy : modern;
      const inactive = node.id === 'forge' ? modern : legacy;
      if (resourcePath === inactive || resourcePath.startsWith(`${inactive}/`)) return null;
      if (resourcePath === active || resourcePath.startsWith(`${active}/`)) {
        resourcePath = modern + resourcePath.substring(active.length);
        break;
      }
    }
    normalized = prefix + resourcePath;
  }
  return normalized;
}

function stable(value) {
  if (Array.isArray(value)) return value.map(stable);
  if (!value || typeof value !== 'object') return value;
  return Object.fromEntries(Object.keys(value).sort().map(key => [key, stable(value[key])]));
}

function visit(value, consumer) {
  if (Array.isArray(value)) {
    value.forEach(child => visit(child, consumer));
  } else if (value && typeof value === 'object') {
    consumer(value);
    Object.values(value).forEach(child => visit(child, consumer));
  }
}

function normalizeRecipe(json) {
  for (const key of ['result', 'secondary_result']) {
    const result = json[key];
    if (result && typeof result === 'object' && !Array.isArray(result)) {
      if (result.item && !result.id) result.id = result.item;
      delete result.item;
    }
  }
  visit(json, object => {
    if (typeof object.tag === 'string' && object.tag.startsWith('forge:')) {
      object.tag = `c:${object.tag.substring('forge:'.length)}`;
    }
  });
}

function normalizeAdvancement(json) {
  if (json.display?.icon?.item && !json.display.icon.id) json.display.icon.id = json.display.icon.item;
  if (json.display?.icon) delete json.display.icon.item;
  visit(json.criteria, object => {
    if (!Array.isArray(object.items) || !object.items.length) return;
    if (object.items.every(entry => entry && typeof entry === 'object' && !Array.isArray(entry)
        && Object.keys(entry).length === 1 && Array.isArray(entry.items)
        && entry.items.every(item => typeof item === 'string'))) {
      object.items = object.items.flatMap(entry => entry.items);
    }
  });
}

function normalizeLoot(json) {
  visit(json, object => {
    if (object.function === 'minecraft:set_count' && object.add === false) delete object.add;
    if (object.function === 'minecraft:enchanted_count_increase'
        && object.enchantment === 'minecraft:looting') {
      object.function = 'minecraft:looting_enchant';
      delete object.enchantment;
    }
    if (object.condition === 'minecraft:random_chance_with_enchanted_bonus'
        && object.enchantment === 'minecraft:looting'
        && object.enchanted_chance?.type === 'minecraft:linear') {
      const chance = object.unenchanted_chance;
      const multiplier = object.enchanted_chance.per_level_above_first;
      const expectedBase = chance + multiplier;
      if (typeof chance === 'number' && typeof multiplier === 'number'
          && Math.abs(object.enchanted_chance.base - expectedBase) < 1.0e-12) {
        object.condition = 'minecraft:random_chance_with_looting';
        object.chance = chance;
        object.looting_multiplier = multiplier;
        delete object.unenchanted_chance;
        delete object.enchanted_chance;
        delete object.enchantment;
      }
    }
    const enchantments = object.predicate?.predicates?.['minecraft:enchantments'];
    if (Array.isArray(enchantments)) {
      object.predicate = {
        enchantments: enchantments.map(entry => ({
          enchantment: entry.enchantments,
          ...(entry.levels ? { levels: entry.levels } : {}),
        })),
      };
    }
  });
}

function contentHash(name, bytes) {
  let normalized = bytes;
  if (name.endsWith('.json') || name === 'pack.mcmeta') {
    const json = JSON.parse(bytes.toString('utf8').replace(/^\uFEFF/, ''));
    if (/\/recipe\//.test(name)) normalizeRecipe(json);
    if (/\/advancement\//.test(name)) normalizeAdvancement(json);
    if (/\/loot_table\//.test(name)) normalizeLoot(json);
    normalized = Buffer.from(JSON.stringify(stable(json)));
  }
  return crypto.createHash('sha256').update(normalized).digest('hex');
}

function index(node) {
  const jar = findJar(node);
  if (!jar) {
    failures.push(`${node.id}: expected exactly one production JAR`);
    return new Map();
  }
  const zip = openZip(jar);
  const result = new Map();
  for (const name of zip.names) {
    const logical = logicalPath(name, node);
    if (!logical || name.endsWith('/')) continue;
    if (result.has(logical)) failures.push(`${node.id}: duplicate normalized resource ${logical}`);
    try {
      result.set(logical, contentHash(logical, zip.read(name)));
    } catch (error) {
      failures.push(`${node.id}: unreadable resource ${name}: ${error.message}`);
    }
  }
  return result;
}

const indexes = Object.fromEntries(NODES.map(node => [node.id, index(node)]));
const all = new Set([...indexes.forge.keys(), ...indexes.neo.keys()]);
let matched = 0;
let allowed = 0;
for (const name of [...all].sort()) {
  const forge = indexes.forge.get(name);
  const neo = indexes.neo.get(name);
  if (forge && neo) {
    if (forge === neo) matched++;
    else failures.push(`content mismatch ${name}`);
    if (ALLOWED_ONE_SIDED.has(name)) failures.push(`stale allowed difference is now paired ${name}`);
  } else {
    const declaration = ALLOWED_ONE_SIDED.get(name);
    const side = forge ? 'forge' : 'neo';
    if (!declaration || !declaration.startsWith(`${side}:`)) failures.push(`undeclared ${side}-only resource ${name}`);
    else allowed++;
  }
}
for (const [name, declaration] of ALLOWED_ONE_SIDED) {
  if (!all.has(name)) failures.push(`declared difference missing ${name} (${declaration})`);
}

if (failures.length) {
  console.error(`R8_RESOURCE_INDEX_AUDIT_FAILED failures=${failures.length} matched=${matched} allowed=${allowed}`);
  failures.slice(0, 20).forEach(failure => console.error(`- ${failure}`));
  process.exitCode = 1;
} else {
  console.log(`R8_RESOURCE_INDEX_AUDIT_OK forge=${indexes.forge.size} neo=${indexes.neo.size} matched=${matched} allowed=${allowed}`);
}