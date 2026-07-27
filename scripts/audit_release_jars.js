#!/usr/bin/env node

const crypto = require('crypto');
const fs = require('fs');
const path = require('path');
const { openZip } = require('./lib/zip');

const ROOT = path.resolve(__dirname, '..');
const PROPERTIES = parseProperties(path.join(ROOT, 'gradle.properties'));
const NODES = [
  {
    id: '1.20.1-forge', loader: 'forge', minecraft: '1.20.1', metadata: 'META-INF/mods.toml',
    otherMetadata: 'META-INF/neoforge.mods.toml', structure: 'data/abyssalcraft/structures/',
    otherStructure: 'data/abyssalcraft/structure/', dependencies: ['forge', 'minecraft', 'terrablender'],
  },
  {
    id: '1.21.1-neoforge', loader: 'neoforge', minecraft: '1.21.1', metadata: 'META-INF/neoforge.mods.toml',
    otherMetadata: 'META-INF/mods.toml', structure: 'data/abyssalcraft/structure/',
    otherStructure: 'data/abyssalcraft/structures/', dependencies: ['neoforge', 'minecraft', 'terrablender'],
  },
];
const RELEASE_INPUTS = ['src/main/java', 'src/main/resources', 'src/main/generated', 'build.gradle.kts', 'gradle.properties'];
const DEV_ONLY = [
  /^\.git(?:hub)?\//, /^\.idea\//, /^\.vscode\//, /^run\//, /^build\//, /^scripts\//, /^docs\//,
  /(?:^|\/)\.cache\//, /(?:^|\/)Thumbs\.db$/i, /(?:^|\/)desktop\.ini$/i, /(?:^|\/)\.DS_Store$/,
  /\.(?:java|kt|kts|gradle|ps1)$/i,
];
const failures = [];

function parseProperties(file) {
  return Object.fromEntries(fs.readFileSync(file, 'utf8').split(/\r?\n/)
    .map(line => line.trim()).filter(line => line && !line.startsWith('#') && line.includes('='))
    .map(line => [line.slice(0, line.indexOf('=')), line.slice(line.indexOf('=') + 1)]));
}

function walk(target) {
  if (!fs.existsSync(target)) return [];
  const stat = fs.statSync(target);
  if (stat.isFile()) return [target];
  return fs.readdirSync(target, { withFileTypes: true }).flatMap(entry => walk(path.join(target, entry.name)));
}

function productionJar(node) {
  const directory = path.join(ROOT, 'versions', node.id, 'build', 'libs');
  if (!fs.existsSync(directory)) return null;
  const jars = fs.readdirSync(directory)
    .filter(name => name.endsWith('.jar') && !/(?:-sources|-dev|-shadow|-all)\.jar$/.test(name))
    .map(name => path.join(directory, name));
  if (jars.length !== 1) {
    failures.push(`${node.id}: expected exactly one production JAR, found ${jars.length}`);
    return null;
  }
  return jars[0];
}

function tomlValue(text, key) {
  const match = text.match(new RegExp(`^\\s*${key}\\s*=\\s*["']([^"']*)["']`, 'm'));
  return match && match[1];
}

function expectedMetadata(node) {
  const source = fs.readFileSync(path.join(ROOT, 'src', 'main', 'resources', node.metadata), 'utf8');
  const values = {
    id: PROPERTIES['mod.id'], name: PROPERTIES['mod.name'], version: PROPERTIES['mod.version'],
    group: PROPERTIES['mod.group'], authors: PROPERTIES['mod.authors'],
    description: PROPERTIES['mod.description'], license: PROPERTIES['mod.license'],
  };
  return source.replace(/\$\{([^}]+)\}/g, (placeholder, key) => key in values ? values[key] : placeholder)
    .replace(/\r\n/g, '\n');
}

const licenseDeclaration = PROPERTIES['mod.license'];
if (!licenseDeclaration) failures.push('license: gradle.properties has no mod.license declaration');
const licenseFiles = fs.readdirSync(ROOT, { withFileTypes: true })
  .filter(entry => entry.isFile() && /^(?:LICENSE|LICENCE|COPYING)(?:\.|$)/i.test(entry.name));
if (licenseFiles.length === 0) {
  failures.push(`license: declared '${licenseDeclaration || '<missing>'}' but repository has no LICENSE/LICENCE/COPYING file`);
}

const inputFiles = RELEASE_INPUTS.flatMap(input => walk(path.join(ROOT, input)));
const newestInput = inputFiles.reduce((newest, file) => fs.statSync(file).mtimeMs > fs.statSync(newest).mtimeMs ? file : newest);
const results = [];
for (const node of NODES) {
  const jar = productionJar(node);
  if (!jar) continue;
  const jarStat = fs.statSync(jar);
  if (jarStat.mtimeMs < fs.statSync(newestInput).mtimeMs) {
    failures.push(`${node.id}: stale JAR ${path.relative(ROOT, jar)} older than ${path.relative(ROOT, newestInput)}`);
  }
  if (!path.basename(jar).includes(`-${PROPERTIES['mod.version']}+${node.minecraft}.jar`)) {
    failures.push(`${node.id}: production JAR filename does not contain mod and Minecraft versions`);
  }
  let zip;
  try {
    zip = openZip(jar);
  } catch (error) {
    failures.push(`${node.id}: unreadable JAR: ${error.message}`);
    continue;
  }
  if (!zip.entries.has(node.metadata)) failures.push(`${node.id}: missing ${node.metadata}`);
  if (zip.entries.has(node.otherMetadata)) failures.push(`${node.id}: mutually exclusive metadata present ${node.otherMetadata}`);
  const metadataBytes = zip.read(node.metadata);
  if (metadataBytes) {
    const metadata = metadataBytes.toString('utf8').replace(/\r\n/g, '\n');
    if (/\$\{[^}]+\}/.test(metadata)) failures.push(`${node.id}: unexpanded metadata placeholder`);
    if (metadata !== expectedMetadata(node)) failures.push(`${node.id}: metadata differs from expanded source template`);
    const expectedVersion = PROPERTIES['mod.version'];
    if (tomlValue(metadata, 'modId') !== PROPERTIES['mod.id']) failures.push(`${node.id}: metadata modId mismatch`);
    if (tomlValue(metadata, 'version') !== expectedVersion) failures.push(`${node.id}: metadata version expected=${expectedVersion} actual=${tomlValue(metadata, 'version')}`);
    if (tomlValue(metadata, 'license') !== licenseDeclaration) failures.push(`${node.id}: metadata license mismatch`);
    for (const dependency of node.dependencies) {
      if (!new RegExp(`modId\\s*=\\s*["']${dependency}["']`).test(metadata)) failures.push(`${node.id}: missing dependency ${dependency}`);
    }
  }
  const structures = zip.names.filter(name => name.startsWith(node.structure) && name.endsWith('.nbt')).length;
  const wrongStructures = zip.names.filter(name => name.startsWith(node.otherStructure) && name.endsWith('.nbt')).length;
  if (structures === 0) failures.push(`${node.id}: no structures in ${node.structure}`);
  if (wrongStructures !== 0) failures.push(`${node.id}: wrong structure directory contains ${wrongStructures} NBT files`);
  const devOnly = zip.names.filter(name => DEV_ONLY.some(pattern => pattern.test(name)));
  if (devOnly.length) failures.push(`${node.id}: dev-only JAR entries ${devOnly.slice(0, 8).join(', ')}`);
  const sha256 = crypto.createHash('sha256').update(fs.readFileSync(jar)).digest('hex');
  results.push({ node: node.id, jar: path.relative(ROOT, jar).replaceAll('\\', '/'), sha256, entries: zip.names.length, structures });
}

if (failures.length) {
  console.error(`R8_RELEASE_JAR_AUDIT_FAILED failures=${failures.length}`);
  failures.forEach(failure => console.error(`- ${failure}`));
  results.forEach(result => console.error(`- SHA256 ${result.node} ${result.sha256} ${result.jar}`));
  process.exitCode = 1;
} else {
  results.forEach(result => console.log(`R8_RELEASE_JAR_SHA256 node=${result.node} sha256=${result.sha256} jar=${result.jar}`));
  console.log(`R8_RELEASE_JAR_AUDIT_OK nodes=${results.length} entries=${results.reduce((sum, result) => sum + result.entries, 0)} structures=${results.reduce((sum, result) => sum + result.structures, 0)}`);
}