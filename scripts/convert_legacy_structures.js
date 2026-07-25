const fs = require('fs');
const path = require('path');
const zlib = require('zlib');
const crypto = require('crypto');

const TAG = {
  END: 0, BYTE: 1, SHORT: 2, INT: 3, LONG: 4, FLOAT: 5, DOUBLE: 6,
  BYTE_ARRAY: 7, STRING: 8, LIST: 9, COMPOUND: 10, INT_ARRAY: 11, LONG_ARRAY: 12
};

class Reader {
  constructor(buffer) { this.buffer = buffer; this.offset = 0; }
  byte() { return this.buffer.readInt8(this.offset++); }
  ubyte() { return this.buffer.readUInt8(this.offset++); }
  short() { const value = this.buffer.readInt16BE(this.offset); this.offset += 2; return value; }
  ushort() { const value = this.buffer.readUInt16BE(this.offset); this.offset += 2; return value; }
  int() { const value = this.buffer.readInt32BE(this.offset); this.offset += 4; return value; }
  long() { const value = this.buffer.readBigInt64BE(this.offset); this.offset += 8; return value; }
  float() { const value = this.buffer.readFloatBE(this.offset); this.offset += 4; return value; }
  double() { const value = this.buffer.readDoubleBE(this.offset); this.offset += 8; return value; }
  string() { const length = this.ushort(); const value = this.buffer.toString('utf8', this.offset, this.offset + length); this.offset += length; return value; }
  bytes(length) { const value = this.buffer.subarray(this.offset, this.offset + length); this.offset += length; return Buffer.from(value); }
}

class Writer {
  constructor() { this.parts = []; }
  push(buffer) { this.parts.push(buffer); }
  byte(value) { const b = Buffer.alloc(1); b.writeInt8(value); this.push(b); }
  ubyte(value) { const b = Buffer.alloc(1); b.writeUInt8(value); this.push(b); }
  short(value) { const b = Buffer.alloc(2); b.writeInt16BE(value); this.push(b); }
  ushort(value) { const b = Buffer.alloc(2); b.writeUInt16BE(value); this.push(b); }
  int(value) { const b = Buffer.alloc(4); b.writeInt32BE(value); this.push(b); }
  long(value) { const b = Buffer.alloc(8); b.writeBigInt64BE(BigInt(value)); this.push(b); }
  float(value) { const b = Buffer.alloc(4); b.writeFloatBE(value); this.push(b); }
  double(value) { const b = Buffer.alloc(8); b.writeDoubleBE(value); this.push(b); }
  string(value) { const b = Buffer.from(value, 'utf8'); this.ushort(b.length); this.push(b); }
  finish() { return Buffer.concat(this.parts); }
}

function readPayload(reader, type) {
  switch (type) {
    case TAG.BYTE: return reader.byte();
    case TAG.SHORT: return reader.short();
    case TAG.INT: return reader.int();
    case TAG.LONG: return reader.long();
    case TAG.FLOAT: return reader.float();
    case TAG.DOUBLE: return reader.double();
    case TAG.BYTE_ARRAY: return reader.bytes(reader.int());
    case TAG.STRING: return reader.string();
    case TAG.LIST: {
      const elementType = reader.ubyte();
      const length = reader.int();
      const value = [];
      for (let i = 0; i < length; i++) value.push(readPayload(reader, elementType));
      return { elementType, value };
    }
    case TAG.COMPOUND: {
      const value = new Map();
      while (true) {
        const childType = reader.ubyte();
        if (childType === TAG.END) break;
        const name = reader.string();
        value.set(name, { type: childType, value: readPayload(reader, childType) });
      }
      return value;
    }
    case TAG.INT_ARRAY: {
      const length = reader.int();
      const value = new Int32Array(length);
      for (let i = 0; i < length; i++) value[i] = reader.int();
      return value;
    }
    case TAG.LONG_ARRAY: {
      const length = reader.int();
      const value = new Array(length);
      for (let i = 0; i < length; i++) value[i] = reader.long();
      return value;
    }
    default: throw new Error(`Unsupported NBT tag type ${type}`);
  }
}

function writePayload(writer, type, value) {
  switch (type) {
    case TAG.BYTE: writer.byte(value); break;
    case TAG.SHORT: writer.short(value); break;
    case TAG.INT: writer.int(value); break;
    case TAG.LONG: writer.long(value); break;
    case TAG.FLOAT: writer.float(value); break;
    case TAG.DOUBLE: writer.double(value); break;
    case TAG.BYTE_ARRAY: writer.int(value.length); writer.push(value); break;
    case TAG.STRING: writer.string(value); break;
    case TAG.LIST:
      writer.ubyte(value.elementType); writer.int(value.value.length);
      value.value.forEach(entry => writePayload(writer, value.elementType, entry));
      break;
    case TAG.COMPOUND:
      for (const [name, child] of value.entries()) {
        writer.ubyte(child.type); writer.string(name); writePayload(writer, child.type, child.value);
      }
      writer.ubyte(TAG.END);
      break;
    case TAG.INT_ARRAY:
      writer.int(value.length); for (const entry of value) writer.int(entry);
      break;
    case TAG.LONG_ARRAY:
      writer.int(value.length); for (const entry of value) writer.long(entry);
      break;
    default: throw new Error(`Unsupported NBT tag type ${type}`);
  }
}

function readNbt(file) {
  const raw = zlib.gunzipSync(fs.readFileSync(file));
  const reader = new Reader(raw);
  const type = reader.ubyte();
  if (type !== TAG.COMPOUND) throw new Error(`${file}: root is not a compound`);
  const name = reader.string();
  const value = readPayload(reader, type);
  if (reader.offset !== raw.length) throw new Error(`${file}: trailing bytes ${raw.length - reader.offset}`);
  return { type, name, value };
}

function writeNbt(file, root) {
  const writer = new Writer();
  writer.ubyte(root.type); writer.string(root.name); writePayload(writer, root.type, root.value);
  fs.mkdirSync(path.dirname(file), { recursive: true });
  fs.writeFileSync(file, zlib.gzipSync(writer.finish(), { level: 9, mtime: 0 }));
}

function child(compound, name, expectedType) {
  const tag = compound.get(name);
  if (!tag || tag.type !== expectedType) throw new Error(`Missing/wrong ${name}, expected tag ${expectedType}`);
  return tag.value;
}

function compoundString(compound, name) {
  return child(compound, name, TAG.STRING);
}

function optionalString(compound, name) {
  const tag = compound.get(name);
  return tag && tag.type === TAG.STRING ? tag.value : null;
}

function stateSummary(state) {
  const propertiesTag = state.get('Properties');
  const properties = {};
  if (propertiesTag && propertiesTag.type === TAG.COMPOUND) {
    for (const [name, value] of propertiesTag.value.entries()) properties[name] = String(value.value);
  }
  return { name: compoundString(state, 'Name'), properties };
}

function listFiles(root) {
  const result = [];
  for (const entry of fs.readdirSync(root, { withFileTypes: true })) {
    const target = path.join(root, entry.name);
    if (entry.isDirectory()) result.push(...listFiles(target));
    else if (entry.name.endsWith('.nbt')) result.push(target);
  }
  return result.sort();
}

function sha(buffer) { return crypto.createHash('sha256').update(buffer).digest('hex'); }

function stringTag(value) { return { type: TAG.STRING, value }; }

function compoundTag(entries) { return { type: TAG.COMPOUND, value: new Map(entries) }; }

function propertiesOf(state) {
  const tag = state.get('Properties');
  if (!tag) return {};
  if (tag.type !== TAG.COMPOUND) throw new Error('Blockstate Properties is not a compound');
  return Object.fromEntries([...tag.value.entries()].map(([name, value]) => [name, String(value.value)]));
}

function mappedProperties(kind, properties) {
  switch (kind) {
    case undefined: return {};
    case 'level': return properties.level === undefined ? {} : { level: properties.level };
    case 'axis': return properties.axis === undefined ? {} : { axis: properties.axis };
    case 'facing': return properties.facing === undefined ? {} : { facing: properties.facing };
    case 'fence': return Object.fromEntries(['north', 'east', 'south', 'west']
      .filter(name => properties[name] !== undefined).map(name => [name, properties[name]]));
    case 'slab': return { type: properties.half === 'top' ? 'top' : 'bottom' };
    case 'stairs': return Object.fromEntries(['facing', 'half', 'shape']
      .filter(name => properties[name] !== undefined).map(name => [name, properties[name]]));
    default: throw new Error(`Unknown property mapping ${kind}`);
  }
}

function setState(state, name, properties = {}) {
  state.set('Name', stringTag(name));
  if (Object.keys(properties).length === 0) {
    state.delete('Properties');
  } else {
    state.set('Properties', compoundTag(Object.entries(properties).map(([key, value]) => [key, stringTag(value)])));
  }
}

function replacementMetadata(rule, oldName, properties) {
  const suffix = Object.keys(properties).sort().map(key => `${key}=${properties[key]}`).join(',');
  return `replacement:${rule}|${oldName}${suffix ? `|${suffix}` : ''}`;
}

function markerNbt(metadata) {
  return compoundTag([
    ['id', stringTag('minecraft:structure_block')],
    ['mode', stringTag('DATA')],
    ['metadata', stringTag(metadata)],
    ['name', stringTag('')],
    ['posX', { type: TAG.INT, value: 0 }],
    ['posY', { type: TAG.INT, value: 0 }],
    ['posZ', { type: TAG.INT, value: 0 }],
    ['powered', { type: TAG.BYTE, value: 0 }],
    ['showair', { type: TAG.BYTE, value: 0 }],
    ['showboundingbox', { type: TAG.BYTE, value: 0 }]
  ]);
}

function listTag(type, value) { return { type: TAG.LIST, value: { elementType: type, value } }; }

function intTag(value) { return { type: TAG.INT, value }; }

function blockState(name, properties = {}) {
  const entries = [['Name', stringTag(name)]];
  if (Object.keys(properties).length) {
    entries.push(['Properties', compoundTag(Object.entries(properties).map(([key, value]) => [key, stringTag(value)]))]);
  }
  return new Map(entries);
}

function parseOffset(expression, variable) {
  const compact = expression.replace(/\s+/g, '');
  if (compact === variable) return 0;
  const match = compact.match(new RegExp(`^${variable}([+-])(\\d+)$`));
  if (!match) throw new Error(`Unsupported coordinate expression ${expression}`);
  return (match[1] === '-' ? -1 : 1) * Number(match[2]);
}

function houseState(expression) {
  const normalized = expression.trim().replace(/,\s*2$/, '');
  const direct = new Map([
    ['ACBlocks.ethaxium_brick.getDefaultState()', blockState('abyssalcraft:ethaxium_bricks')],
    ['ACBlocks.darkstone_brick.getDefaultState()', blockState('abyssalcraft:darkstone_brick')],
    ['ACBlocks.darkstone_cobblestone.getDefaultState()', blockState('abyssalcraft:darkstone_cobblestone')],
    ['ACBlocks.ethaxium_pillar.getDefaultState()', blockState('abyssalcraft:ethaxium_pillar', { axis: 'y' })],
    ['ACBlocks.ethaxium_brick_stairs.getDefaultState()', blockState('abyssalcraft:ethaxium_brick_stairs',
      { facing: 'north', half: 'bottom', shape: 'straight' })],
    ['ACBlocks.ethaxium_brick_fence.getDefaultState()', blockState('abyssalcraft:ethaxium_brick_fence')],
    ['ACBlocks.darkstone.getDefaultState()', blockState('abyssalcraft:darkstone')],
    ['ACBlocks.glowing_darkstone_bricks.getDefaultState()', blockState('abyssalcraft:glowing_darkstone_bricks')],
    ['Blocks.AIR.getDefaultState()', blockState('minecraft:air')],
    ['Blocks.LAVA.getDefaultState()', blockState('minecraft:lava', { level: '0' })],
    ['Blocks.WATER.getDefaultState()', blockState('minecraft:water', { level: '0' })],
    ['ACBlocks.ethaxium_brick.getStateFromMeta(1)', blockState('abyssalcraft:chiseled_ethaxium_brick')]
  ]);
  if (direct.has(normalized)) return direct.get(normalized);
  const stair = normalized.match(/^ACBlocks\.ethaxium_brick_stairs\.getStateFromMeta\(([123])\)$/);
  if (stair) {
    const facing = { '1': 'west', '2': 'south', '3': 'north' }[stair[1]];
    return blockState('abyssalcraft:ethaxium_brick_stairs', { facing, half: 'bottom', shape: 'straight' });
  }
  throw new Error(`Unsupported StructureHouse blockstate: ${normalized}`);
}

function compileEthaxiumHouse(root) {
  const source = fs.readFileSync(path.join(root, 'docs', 'AbyssalCraft-1.12.2', 'src', 'main', 'java',
    'com', 'shinoow', 'abyssalcraft', 'common', 'structures', 'StructureHouse.java'), 'utf8');
  const pattern = /world\.setBlockState\(new BlockPos\(([^)]*)\),\s*([^;]+)\);/g;
  const placements = [];
  let match;
  while ((match = pattern.exec(source)) !== null) {
    const coordinates = match[1].split(',').map(value => value.trim());
    if (coordinates.length !== 3) throw new Error(`Bad StructureHouse coordinate tuple: ${match[1]}`);
    placements.push({
      x: parseOffset(coordinates[0], 'i'), y: parseOffset(coordinates[1], 'j'),
      z: parseOffset(coordinates[2], 'k'), state: houseState(match[2])
    });
  }
  if (placements.length !== 3060) throw new Error(`Expected 3060 StructureHouse placements, got ${placements.length}`);
  const minX = Math.min(...placements.map(entry => entry.x));
  const minY = Math.min(...placements.map(entry => entry.y));
  const minZ = Math.min(...placements.map(entry => entry.z));
  const maxX = Math.max(...placements.map(entry => entry.x));
  const maxY = Math.max(...placements.map(entry => entry.y));
  const maxZ = Math.max(...placements.map(entry => entry.z));
  const palette = [];
  const paletteIndex = new Map();
  const blocks = placements.map(entry => {
    const summary = stateSummary(entry.state);
    const key = `${summary.name}|${JSON.stringify(summary.properties)}`;
    if (!paletteIndex.has(key)) {
      paletteIndex.set(key, palette.length);
      palette.push(entry.state);
    }
    return new Map([
      ['pos', listTag(TAG.INT, [entry.x - minX, entry.y - minY, entry.z - minZ])],
      ['state', intTag(paletteIndex.get(key))]
    ]);
  });
  return {
    type: TAG.COMPOUND,
    name: '',
    value: new Map([
      ['DataVersion', intTag(3465)],
      ['size', listTag(TAG.INT, [maxX - minX + 1, maxY - minY + 1, maxZ - minZ + 1])],
      ['palette', listTag(TAG.COMPOUND, palette)],
      ['blocks', listTag(TAG.COMPOUND, blocks)],
      ['entities', listTag(TAG.COMPOUND, [])]
    ])
  };
}

function transformStructure(root, mappings, relativePath) {
  const palette = child(root.value, 'palette', TAG.LIST).value;
  const blocks = child(root.value, 'blocks', TAG.LIST).value;
  const paletteDecisions = [];
  for (let index = 0; index < palette.length; index++) {
    const state = palette[index];
    const oldName = compoundString(state, 'Name');
    const oldProperties = propertiesOf(state);
    if (!oldName.startsWith('abyssalcraft:')) {
      paletteDecisions.push({ kind: 'vanilla', oldName, oldProperties });
      continue;
    }
    const fixed = mappings.static[oldName];
    const dynamic = mappings.dynamic[oldName];
    if (fixed && dynamic) throw new Error(`${relativePath}: duplicate mapping decision for ${oldName}`);
    if (!fixed && !dynamic) throw new Error(`${relativePath}: unmapped AbyssalCraft block ${oldName}`);
    if (fixed) {
      const properties = mappedProperties(fixed.properties, oldProperties);
      setState(state, fixed.target, properties);
      paletteDecisions.push({ kind: 'static', oldName, oldProperties, target: fixed.target, properties });
    } else {
      setState(state, 'minecraft:structure_block', { mode: 'data' });
      paletteDecisions.push({ kind: 'dynamic', oldName, oldProperties, rule: dynamic });
    }
  }

  let originalMarkers = 0;
  let replacementMarkers = 0;
  let preservedBlockEntities = 0;
  let markerizedBlockEntities = 0;
  for (const block of blocks) {
    const stateIndex = child(block, 'state', TAG.INT);
    const decision = paletteDecisions[stateIndex];
    const nbt = block.get('nbt');
    const nbtId = nbt && nbt.type === TAG.COMPOUND ? optionalString(nbt.value, 'id') : null;
    if (nbtId === 'minecraft:structure_block') originalMarkers++;

    if (decision.kind === 'dynamic') {
      block.set('nbt', markerNbt(replacementMetadata(decision.rule, decision.oldName, decision.oldProperties)));
      replacementMarkers++;
      if (nbtId && nbtId.startsWith('abyssalcraft:')) markerizedBlockEntities++;
      continue;
    }
    if (nbtId && nbtId.startsWith('abyssalcraft:')) {
      throw new Error(`${relativePath}: AC block entity ${nbtId} remained on static block ${decision.oldName}`);
    }
    if (nbtId && nbtId !== 'minecraft:structure_block') preservedBlockEntities++;
  }
  return { originalMarkers, replacementMarkers, preservedBlockEntities, markerizedBlockEntities };
}

function auditStructure(file, sourceRoot) {
  const root = readNbt(file);
  const palette = child(root.value, 'palette', TAG.LIST).value;
  const blocks = child(root.value, 'blocks', TAG.LIST).value;
  const entities = child(root.value, 'entities', TAG.LIST).value;
  const size = Array.from(child(root.value, 'size', TAG.LIST).value);
  const names = palette.map(state => compoundString(state, 'Name'));
  const states = palette.map(stateSummary);
  let markers = 0;
  let blockEntities = 0;
  const markerMetadata = [];
  const blockEntityIds = [];
  for (const block of blocks) {
    const nbt = block.get('nbt');
    if (!nbt) continue;
    const id = nbt.value.get('id');
    if (id && id.value === 'minecraft:structure_block') {
      markers++;
      markerMetadata.push(optionalString(nbt.value, 'metadata') || optionalString(nbt.value, 'name') || '<missing>');
    } else {
      blockEntities++;
      blockEntityIds.push(id ? String(id.value) : '<missing>');
    }
  }
  return {
    path: path.relative(sourceRoot, file).replaceAll('\\', '/'),
    sourceSha256: sha(fs.readFileSync(file)),
    dataVersion: child(root.value, 'DataVersion', TAG.INT),
    size,
    blocks: blocks.length,
    palette: palette.length,
    markers,
    blockEntities,
    entities: entities.length,
    names,
    states,
    markerMetadata,
    blockEntityIds,
    root
  };
}

function main() {
  const root = path.resolve(__dirname, '..');
  const sourceRoot = path.join(root, 'docs', 'AbyssalCraft-1.12.2', 'src', 'main', 'resources',
    'assets', 'abyssalcraft', 'structures');
  const files = listFiles(sourceRoot);
  const audits = files.map(file => auditStructure(file, sourceRoot));
  const mappings = JSON.parse(fs.readFileSync(path.join(root, 'scripts', 'legacy-structure-mappings.json'), 'utf8'));
  const names = [...new Set(audits.flatMap(entry => entry.names))].sort();
  const totals = audits.reduce((sum, entry) => ({
    blocks: sum.blocks + entry.blocks,
    markers: sum.markers + entry.markers,
    blockEntities: sum.blockEntities + entry.blockEntities,
    entities: sum.entities + entry.entities
  }), { blocks: 0, markers: 0, blockEntities: 0, entities: 0 });
  const manifest = {
    format: 1,
    source: 'AbyssalCraft-1.12.2/DataVersion-1343',
    files: audits.map(({ root: ignored, names: ignoredNames, ...entry }) => entry),
    totals,
    paletteNames: names,
    paletteStates: [...new Map(audits.flatMap(entry => entry.states)
      .map(state => [`${state.name}|${JSON.stringify(state.properties)}`, state])).values()]
      .sort((a, b) => a.name.localeCompare(b.name) || JSON.stringify(a.properties).localeCompare(JSON.stringify(b.properties))),
    markerMetadata: [...new Set(audits.flatMap(entry => entry.markerMetadata))].sort(),
    blockEntityIds: [...new Set(audits.flatMap(entry => entry.blockEntityIds))].sort()
  };
  const manifestPath = path.join(root, 'scripts', 'legacy-structures-manifest.json');
  fs.writeFileSync(manifestPath, `${JSON.stringify(manifest, null, 2)}\n`);
  console.log(`RR_WORLD_STRUCTURE_AUDIT_OK files=${files.length} blocks=${totals.blocks} markers=${totals.markers} blockEntities=${totals.blockEntities} paletteNames=${names.length}`);
  console.log(`manifest=${path.relative(root, manifestPath)}`);

  const outputs = [
    path.join(root, 'src', 'main', 'resources', 'data', 'abyssalcraft', 'structures', 'legacy'),
    path.join(root, 'src', 'main', 'resources', 'data', 'abyssalcraft', 'structure', 'legacy')
  ];
  outputs.forEach(output => fs.rmSync(output, { recursive: true, force: true }));
  const conversion = { originalMarkers: 0, replacementMarkers: 0, preservedBlockEntities: 0, markerizedBlockEntities: 0 };
  for (const audit of audits) {
    const result = transformStructure(audit.root, mappings, audit.path);
    Object.keys(conversion).forEach(key => conversion[key] += result[key]);
    for (const output of outputs) writeNbt(path.join(output, audit.path), audit.root);
  }
  const house = compileEthaxiumHouse(root);
  for (const output of outputs) writeNbt(path.join(output, 'omothol', 'ethaxium_house.nbt'), house);
  const pluralFiles = listFiles(outputs[0]);
  const singularFiles = listFiles(outputs[1]);
  if (pluralFiles.length !== 37 || singularFiles.length !== 37) {
    throw new Error(`Expected 37 files per output, got ${pluralFiles.length}/${singularFiles.length}`);
  }
  for (let i = 0; i < pluralFiles.length; i++) {
    if (path.relative(outputs[0], pluralFiles[i]) !== path.relative(outputs[1], singularFiles[i])) {
      throw new Error(`Output path mismatch at index ${i}`);
    }
    if (sha(fs.readFileSync(pluralFiles[i])) !== sha(fs.readFileSync(singularFiles[i]))) {
      throw new Error(`Output hash mismatch for ${path.relative(outputs[0], pluralFiles[i])}`);
    }
    auditStructure(pluralFiles[i], outputs[0]);
    auditStructure(singularFiles[i], outputs[1]);
  }
  if (conversion.originalMarkers !== totals.markers) {
    throw new Error(`Original marker count changed: ${conversion.originalMarkers}/${totals.markers}`);
  }
  if (conversion.preservedBlockEntities + conversion.markerizedBlockEntities !== totals.blockEntities) {
    throw new Error(`Block entity conservation failed: ${JSON.stringify(conversion)} source=${totals.blockEntities}`);
  }
  const houseAudit = auditStructure(path.join(outputs[0], 'omothol', 'ethaxium_house.nbt'), outputs[0]);
  if (houseAudit.blocks !== 3060 || houseAudit.size.join(',') !== '19,7,24') {
    throw new Error(`Ethaxium house invariant failed: blocks=${houseAudit.blocks} size=${houseAudit.size}`);
  }
  console.log(`RR_WORLD_STRUCTURE_CONVERT_OK files=${pluralFiles.length}x2 originalMarkers=${conversion.originalMarkers} replacementMarkers=${conversion.replacementMarkers} preservedBE=${conversion.preservedBlockEntities} markerizedBE=${conversion.markerizedBlockEntities} house=${houseAudit.blocks}@${houseAudit.size.join('x')}`);
}

main();