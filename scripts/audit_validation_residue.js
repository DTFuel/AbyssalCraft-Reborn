#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..');
const SCAN_ROOTS = ['src', 'integration'];
const PERMANENT_FIXTURES = new Map([
  ['src/main/java/com/shinoow/abyssalcraft/platform/ResurrectionLiveFixtureCompat.java', 'server restart fixture boundary'],
  ['src/main/java/com/shinoow/abyssalcraft/system/ritual/ResurrectionLiveFixture.java', 'persistent resurrection restart contract'],
  ['src/main/java/com/shinoow/abyssalcraft/validation/server/NaturalSpawnServerFixture.java', 'dedicated-server natural spawn contract'],
  ['src/main/java/com/shinoow/abyssalcraft/validation/server/ServerRuntimeDataFixture.java', 'dedicated-server runtime data contract'],
]);
const PERMANENT_PATTERNS = [
  /(?:^|\/)validation\//,
  /(?:SelfTest|Audit|Invariant|ValidationData|Validator|Orchestrator|Sampler|Matrix)\.java$/,
];
const TEMPORARY_NAME = /(?:^|[._-])(?:probe|temp|temporary|debug|diagnostic)(?:[._-]|$)/i;
const FIXTURE_NAME = /Fixture\.(?:java|json|nbt)$/i;
const TEMPORARY_MARKER = /(?:TODO[-_ ]?REMOVE|TEMPORARY[-_ ]?PROBE|RR[-_ ]?TEMP[-_ ]?|DEBUG[-_ ]?ONLY)/i;
const failures = [];

function walk(directory) {
  if (!fs.existsSync(directory)) return [];
  return fs.readdirSync(directory, { withFileTypes: true }).flatMap(entry => {
    const target = path.join(directory, entry.name);
    return entry.isDirectory() ? walk(target) : [target];
  });
}

function relative(file) {
  return path.relative(ROOT, file).replaceAll('\\', '/');
}

for (const [file] of PERMANENT_FIXTURES) {
  if (!fs.existsSync(path.join(ROOT, file))) failures.push(`stale permanent fixture allowlist entry ${file}`);
}
let permanent = 0;
let scanned = 0;
for (const file of SCAN_ROOTS.flatMap(root => walk(path.join(ROOT, root)))) {
  const name = relative(file);
  scanned++;
  const allowed = PERMANENT_FIXTURES.has(name) || PERMANENT_PATTERNS.some(pattern => pattern.test(name));
  if (allowed) permanent++;
  if (FIXTURE_NAME.test(name) && !allowed) failures.push(`fixture is not allowlisted ${name}`);
  if (TEMPORARY_NAME.test(name) && !allowed) failures.push(`temporary probe-like file ${name}`);
  if (/\.(?:java|js|json|md|txt)$/i.test(name)) {
    const source = fs.readFileSync(file, 'utf8');
    if (TEMPORARY_MARKER.test(source) && !allowed) failures.push(`temporary marker in ${name}`);
  }
}

if (failures.length) {
  console.error(`R8_VALIDATION_RESIDUE_AUDIT_FAILED failures=${failures.length}`);
  failures.forEach(failure => console.error(`- ${failure}`));
  process.exitCode = 1;
} else {
  console.log(`R8_VALIDATION_RESIDUE_AUDIT_OK scanned=${scanned} permanent=${permanent} fixtures=${PERMANENT_FIXTURES.size} temporary=0`);
}