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
  for (const [modern, legacy] of PATH_FAMILIES) {
    const active = node.id === 'forge' ? legacy : modern;
    const inactive = node.id === 'forge' ? modern : legacy;
    if (normalized.includes(`/${inactive}/`)) return null;
    normalized = normalized.replace(`/${active}/`, `/${modern}/`);
  }
  return normalized;
}

function stable(value) {
  if (Array.isArray(value)) return value.map(stable);
  if (!value || typeof value !== 'object') return value;
  return Object.fromEntries(Object.keys(value).sort().map(key => [key, stable(value[key])]));
}

function contentHash(name, bytes) {
  let normalized = bytes;
  if (name.endsWith('.json') || name === 'pack.mcmeta') {
    const json = JSON.parse(bytes.toString('utf8').replace(/^\uFEFF/, ''));
    if (/\/recipe\//.test(name) && json.result && typeof json.result === 'object') {
      if (json.result.item && !json.result.id) json.result.id = json.result.item;
      delete json.result.item;
    }
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