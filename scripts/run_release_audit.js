#!/usr/bin/env node

const { spawnSync } = require('child_process');
const path = require('path');

const ROOT = path.resolve(__dirname, '..');
const AUDITS = [
  ['scripts/audit_validation_residue.js'],
  ['scripts/generate_docs_index.js', '--check'],
  ['scripts/audit_assets.js', '--check'],
  ['scripts/audit_compat.js'],
  ['scripts/audit_renames.js'],
  ['scripts/audit_datapack_plural.js'],
  ['scripts/audit_release_jars.js'],
  ['scripts/audit_resource_index.js'],
];

const failures = [];
for (const [script, ...args] of AUDITS) {
  console.log(`R8_RELEASE_AUDIT_RUN script=${script} args=${args.join(',') || '<none>'}`);
  const result = spawnSync(process.execPath, [script, ...args], { cwd: ROOT, stdio: 'inherit' });
  if (result.error) {
    console.error(`R8_RELEASE_AUDIT_FAILED script=${script} reason=${result.error.message}`);
    failures.push(`${script}:spawn`);
  } else if (result.status !== 0) {
    console.error(`R8_RELEASE_AUDIT_FAILED script=${script} exit=${result.status}`);
    failures.push(`${script}:${result.status}`);
  }
}
if (failures.length) {
  console.error(`R8_RELEASE_AUDIT_SUMMARY_FAILED audits=${AUDITS.length} failures=${failures.length} detail=${failures.join(',')}`);
  process.exit(1);
}
console.log(`R8_RELEASE_AUDIT_OK audits=${AUDITS.length} failures=0`);