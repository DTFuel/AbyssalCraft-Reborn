#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..');
const CATALOG_PATH = path.join(ROOT, 'src', 'main', 'resources', 'data', 'abyssalcraft', 'catalog',
  'legacy_machine_catalog.json');
const GENERATED_ROOT = path.join(ROOT, 'src', 'main', 'generated', 'data', 'abyssalcraft');
const LAYOUTS = [
  { directory: 'recipes', resultKey: 'item', forgeTags: true },
  { directory: 'recipe', resultKey: 'id', forgeTags: false },
];
const MACHINE_TYPES = new Set([
  'abyssalcraft:crystallization',
  'abyssalcraft:transmutation',
  'abyssalcraft:materialization',
]);
const EXPECTED = { source: 223, migrated: 142, replaced: 77, retired: 4, blocked: 0 };

const TAGS = new Map([
  ['oreAbyssalnite', 'c:ores/abyssalnite'],
  ['oreCoralium', 'c:ores/coralium'],
  ['oreIron', 'c:ores/iron'],
  ['oreGold', 'c:ores/gold'],
  ['oreCoal', 'c:ores/coal'],
  ['oreRedstone', 'c:ores/redstone'],
  ['oreDiamond', 'c:ores/diamond'],
  ['oreLapis', 'c:ores/lapis'],
  ['logWood', 'minecraft:logs'],
  ['plankWood', 'minecraft:planks'],
  ['treeSapling', 'minecraft:saplings'],
  ['treeLeaves', 'minecraft:leaves'],
  ['vine', 'minecraft:vines'],
  ['listAllmeatraw', 'c:foods/raw_meat'],
  ['dustCoal', 'c:dusts/coal'],
  ['dustSulfur', 'c:dusts/sulfur'],
  ['dustSaltpeter', 'c:dusts/saltpeter'],
  ['oreSaltpeter', 'c:ores/saltpeter'],
]);
for (const material of ['Tin', 'Copper', 'Aluminum', 'Aluminium', 'Zinc', 'Magnesium', 'Calcium',
  'Bronze', 'Brass', 'Iron', 'Gold']) {
  const name = material.toLowerCase().replace('aluminum', 'aluminium');
  TAGS.set(`ingot${material}`, `c:ingots/${name}`);
  TAGS.set(`ore${material}`, `c:ores/${name}`);
  TAGS.set(`nugget${material}`, `c:nuggets/${name}`);
  TAGS.set(`dust${material}`, `c:dusts/${name}`);
  TAGS.set(`block${material}`, `c:storage_blocks/${name}`);
}

function fail(message) {
  throw new Error(message);
}

function splitArguments(value) {
  const result = [];
  let depth = 0;
  let quoted = false;
  let start = 0;
  for (let index = 0; index < value.length; index++) {
    const character = value[index];
    if (character === '"') quoted = !quoted;
    if (quoted) continue;
    if (character === '(') depth++;
    else if (character === ')') depth--;
    else if (character === ',' && depth === 0) {
      result.push(value.slice(start, index).trim());
      start = index + 1;
    }
  }
  result.push(value.slice(start).trim());
  return result;
}

function snake(value) {
  return value.replace(/([a-z0-9])([A-Z])/g, '$1_$2').toLowerCase().replace('aluminum', 'aluminium');
}

function legacyTag(value, count) {
  if (value.startsWith('crystalCluster')) {
    return { id: `abyssalcraft:${snake(value.slice('crystalCluster'.length))}_crystal_cluster`, count, tag: false };
  }
  if (value.startsWith('crystalShard')) {
    return { id: `abyssalcraft:crystal_shard_${snake(value.slice('crystalShard'.length))}`, count, tag: false };
  }
  if (value.startsWith('crystalFragment')) {
    return { id: `abyssalcraft:crystal_fragment_${snake(value.slice('crystalFragment'.length))}`, count, tag: false };
  }
  if (value.startsWith('crystal')) {
    return { id: `abyssalcraft:crystal_${snake(value.slice('crystal'.length))}`, count, tag: false };
  }
  return { id: TAGS.get(value) || `legacy:${value.toLowerCase()}`, count, tag: true };
}

function registryId(expression, metadata) {
  const value = expression.trim();
  if (value.startsWith('ACItems.')) {
    const name = value.slice(8);
    if (name === 'dread_plagued_gateway_key') return 'abyssalcraft:dreadkey';
    if (name === 'omothol_forged_gateway_key') return 'abyssalcraft:gatewaykeyjzh';
    return `abyssalcraft:${name}`;
  }
  if (value.startsWith('ACBlocks.')) return `abyssalcraft:${value.slice(9)}`;
  if (value.startsWith('Items.')) {
    const name = value.slice(6);
    if (name === 'DYE') return metadata === '4' ? 'minecraft:lapis_lazuli' : 'minecraft:bone_meal';
    if (name === 'COAL' && metadata === '1') return 'minecraft:charcoal';
    if (name === 'POTIONITEM') return 'minecraft:potion';
    return `minecraft:${name.toLowerCase()}`;
  }
  if (value.startsWith('Blocks.')) {
    const name = value.slice(7);
    if (name === 'STONEBRICK') return 'minecraft:stone_bricks';
    if (name === 'DEADBUSH') return 'minecraft:dead_bush';
    if (name === 'PRISMARINE') {
      return metadata === '1' ? 'minecraft:prismarine_bricks'
        : metadata === '2' ? 'minecraft:dark_prismarine' : 'minecraft:prismarine';
    }
    if (name === 'STONE' && metadata && metadata !== '0') {
      return ({
        1: 'minecraft:granite',
        2: 'minecraft:polished_granite',
        3: 'minecraft:diorite',
        4: 'minecraft:polished_diorite',
        5: 'minecraft:andesite',
        6: 'minecraft:polished_andesite',
      })[metadata] || 'minecraft:stone';
    }
    return `minecraft:${name.toLowerCase()}`;
  }
  fail(`unsupported legacy machine expression: ${expression}`);
}

function stack(expression) {
  if (expression.startsWith('"')) return legacyTag(expression.slice(1, -1), 1);
  if (expression.startsWith('PotionUtils.')) return { id: 'minecraft:potion', count: 1, tag: false };
  if (!expression.startsWith('new ItemStack(')) {
    return { id: registryId(expression, null), count: 1, tag: false };
  }
  const parts = splitArguments(expression.slice('new ItemStack('.length, -1));
  return {
    id: registryId(parts[0], parts.length > 2 ? parts[2] : null),
    count: parts.length > 1 && /^\d+$/.test(parts[1]) ? Number(parts[1]) : 1,
    tag: false,
  };
}

function stacks(argumentsList) {
  const result = [];
  for (let index = 0; index < argumentsList.length;) {
    const value = argumentsList[index];
    if (value.startsWith('"') && index + 1 < argumentsList.length && /^\d+$/.test(argumentsList[index + 1])) {
      result.push(legacyTag(value.slice(1, -1), Number(argumentsList[index + 1])));
      index += 2;
    } else {
      result.push(stack(value));
      index++;
    }
  }
  return result;
}

function parseEntry(entry, resolutions) {
  const match = /^AbyssalCraftAPI\.(addSingleCrystallization|addCrystallization|addTransmutation|addMaterialization)\((.*)\);$/.exec(entry.legacy_call);
  if (!match) fail(`invalid legacy call at ordinal ${entry.ordinal}`);
  const kind = match[1].includes('Crystallization') ? 'crystallization'
    : match[1] === 'addTransmutation' ? 'transmutation' : 'materialization';
  const raw = splitArguments(match[2]);
  const experience = kind === 'materialization' ? 0 : Number(raw.pop().replace(/F$/, ''));
  const parsedStacks = stacks(raw);
  const inputs = kind === 'materialization' ? parsedStacks.slice(1) : [parsedStacks[0]];
  const rawOutputs = kind === 'materialization' ? [parsedStacks[0]] : parsedStacks.slice(1);
  const outputs = rawOutputs.map(output => output.tag
    ? { id: resolutions.get(output.id), count: output.count, tag: false }
    : output);
  if (outputs.some(output => !output.id)) fail(`missing output resolution at ordinal ${entry.ordinal}`);
  const expectedKind = entry.kind.toLowerCase();
  if (kind !== expectedKind) fail(`kind mismatch at ordinal ${entry.ordinal}: ${kind} != ${expectedKind}`);
  const logicalId = `${entry.classification_key}_${outputs[0].id.split(':')[1].replaceAll('/', '_')}`;
  return { ...entry, kind, inputs, outputs, experience, logicalId };
}

function ingredient(input, forgeTags) {
  let id = input.id;
  if (input.tag && forgeTags && id.startsWith('c:')) id = `forge:${id.slice(2)}`;
  return { [input.tag ? 'tag' : 'item']: id };
}

function result(output, resultKey) {
  return { [resultKey]: output.id, count: output.count };
}

function recipe(entry, layout) {
  const json = { type: `abyssalcraft:${entry.kind}` };
  if (entry.kind === 'materialization') {
    json.inputs = entry.inputs.map(input => ({
      ingredient: ingredient(input, layout.forgeTags),
      count: input.count,
    }));
  } else {
    json.input = ingredient(entry.inputs[0], layout.forgeTags);
  }
  json.result = result(entry.outputs[0], layout.resultKey);
  if (entry.kind === 'crystallization' && entry.outputs.length === 2) {
    json.secondary_result = result(entry.outputs[1], layout.resultKey);
  }
  if (entry.kind !== 'materialization') {
    json.experience = entry.experience;
    json.time = 200;
  }
  return `${JSON.stringify(json, null, 2)}\n`;
}

function readCatalog() {
  const catalog = JSON.parse(fs.readFileSync(CATALOG_PATH, 'utf8'));
  for (const [key, value] of Object.entries(EXPECTED)) {
    if (catalog[key] !== value) fail(`catalog ${key}=${catalog[key]} expected=${value}`);
  }
  if (!Array.isArray(catalog.entries) || catalog.entries.length !== EXPECTED.source) {
    fail(`catalog entries=${catalog.entries && catalog.entries.length} expected=${EXPECTED.source}`);
  }
  const resolutions = new Map(catalog.output_resolutions.map(entry => [entry.tag, entry.item]));
  const executable = catalog.entries.filter(entry => entry.status === 'MIGRATED' || entry.status === 'REPLACED')
    .map(entry => parseEntry(entry, resolutions));
  if (executable.length !== EXPECTED.migrated + EXPECTED.replaced) fail(`executable entries=${executable.length}`);
  const ids = new Set(executable.map(entry => entry.logicalId));
  if (ids.size !== executable.length) fail(`duplicate logical IDs: ${executable.length - ids.size}`);
  return executable;
}

function files(directory) {
  if (!fs.existsSync(directory)) return [];
  return fs.readdirSync(directory, { withFileTypes: true })
    .filter(entry => entry.isFile() && entry.name.endsWith('.json'))
    .map(entry => path.join(directory, entry.name));
}

function ownedMachineFile(file) {
  try {
    return MACHINE_TYPES.has(JSON.parse(fs.readFileSync(file, 'utf8')).type);
  } catch (error) {
    fail(`cannot classify recipe ${path.relative(ROOT, file)}: ${error.message}`);
  }
}

function expectedFiles(entries) {
  const expected = new Map();
  for (const layout of LAYOUTS) {
    for (const entry of entries) {
      expected.set(path.join(GENERATED_ROOT, layout.directory, `${entry.logicalId}.json`), recipe(entry, layout));
    }
  }
  return expected;
}

function check(entries, expected) {
  const failures = [];
  const actualOwned = LAYOUTS.flatMap(layout => files(path.join(GENERATED_ROOT, layout.directory)))
    .filter(ownedMachineFile);
  for (const file of actualOwned) {
    if (!expected.has(file)) failures.push(`stale owned recipe ${path.relative(ROOT, file)}`);
  }
  for (const [file, content] of expected) {
    if (!fs.existsSync(file)) failures.push(`missing recipe ${path.relative(ROOT, file)}`);
    else if (fs.readFileSync(file, 'utf8').replaceAll('\r\n', '\n') !== content) {
      failures.push(`stale content ${path.relative(ROOT, file)}`);
    }
  }
  if (actualOwned.length !== entries.length * LAYOUTS.length) {
    failures.push(`physical owned recipes=${actualOwned.length} expected=${entries.length * LAYOUTS.length}`);
  }
  if (failures.length) fail(`machine recipe check failed (${failures.length}):\n${failures.slice(0, 30).join('\n')}`);
  console.log(`MACHINE_RECIPE_CHECK_OK catalog=223 migrated=142 replaced=77 retired=4 blocked=0 logical=${entries.length} physical=${expected.size}`);
}

function write(entries, expected) {
  const expectedPaths = new Set(expected.keys());
  let removed = 0;
  let written = 0;
  for (const layout of LAYOUTS) {
    const directory = path.join(GENERATED_ROOT, layout.directory);
    fs.mkdirSync(directory, { recursive: true });
    for (const file of files(directory)) {
      if (ownedMachineFile(file) && !expectedPaths.has(file)) {
        fs.unlinkSync(file);
        removed++;
      }
    }
  }
  for (const [file, content] of expected) {
    if (!fs.existsSync(file) || fs.readFileSync(file, 'utf8').replaceAll('\r\n', '\n') !== content) {
      fs.writeFileSync(file, content, 'utf8');
      written++;
    }
  }
  console.log(`MACHINE_RECIPE_WRITE_OK logical=${entries.length} physical=${expected.size} written=${written} removedStale=${removed}`);
}

const mode = process.argv[2];
if ((mode !== '--check' && mode !== '--write') || process.argv.length !== 3) {
  console.error('Usage: node scripts/generate_machine_recipes.js --check|--write');
  process.exit(2);
}

try {
  const entries = readCatalog();
  const expected = expectedFiles(entries);
  if (mode === '--write') write(entries, expected);
  else check(entries, expected);
} catch (error) {
  console.error(error.message);
  process.exit(1);
}