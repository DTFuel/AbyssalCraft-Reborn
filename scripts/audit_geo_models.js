#!/usr/bin/env node
'use strict';

const crypto = require('crypto');
const fs = require('fs');
const path = require('path');
const { build, pngSize } = require('./convert_modelbase_to_geo');

const ROOT = path.resolve(__dirname, '..');
const MANIFEST = path.join(ROOT, 'docs/validation/RR-GEO-MODEL-FIDELITY.json');
const mode = process.argv[2] || '--check';
if (!['--check', '--write'].includes(mode) || process.argv.length > 3) {
    console.error('Usage: node scripts/audit_geo_models.js [--check|--write]');
    process.exit(2);
}

const failures = [];
const stableJson = value => `${JSON.stringify(value, null, 2)}\n`;
const resolve = relative => path.join(ROOT, relative);
const sha256 = file => crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');

function fail(id, message) {
    failures.push(`${id}: ${message}`);
}

function validateFinite(id, value, location) {
    if (value === null) {
        fail(id, `${location} is null`);
        return;
    }
    if (typeof value === 'number' && !Number.isFinite(value)) {
        fail(id, `${location} is not finite`);
        return;
    }
    if (Array.isArray(value)) {
        value.forEach((entry, index) => validateFinite(id, entry, `${location}[${index}]`));
    } else if (typeof value === 'object') {
        Object.entries(value).forEach(([key, entry]) => validateFinite(id, entry, `${location}.${key}`));
    }
}

function validateModelBipedGeometry(entry, bones) {
    const expected = {
        armorHead: { pivot: [0, 24, 0], uv: [0, 0], cubes: 2 },
        armorBody: { pivot: [0, 24, 0], uv: [16, 16], cubes: 1 },
        armorRightArm: { pivot: [-5, 22, 0], uv: [40, 16], cubes: 1 },
        armorLeftArm: { pivot: [5, 22, 0], uv: [40, 16], cubes: 1 },
        armorRightLeg: { pivot: [-2, 12, 0], uv: [0, 16], cubes: 1 },
        armorLeftLeg: { pivot: [2, 12, 0], uv: [0, 16], cubes: 1 },
        armorRightBoot: { pivot: [-2, 12, 0], cubes: 0 },
        armorLeftBoot: { pivot: [2, 12, 0], cubes: 0 }
    };
    const inflate = entry.parameters.modelBipedInflate;
    for (const [name, expectation] of Object.entries(expected)) {
        const bone = bones.find(candidate => candidate.name === name);
        if (!bone) {
            fail(entry.id, `missing ModelBiped bone ${name}`);
            continue;
        }
        if (JSON.stringify(bone.pivot) !== JSON.stringify(expectation.pivot)) {
            fail(entry.id, `${name} pivot=${JSON.stringify(bone.pivot)}, expected ${JSON.stringify(expectation.pivot)}`);
        }
        const cubes = bone.cubes || [];
        if (cubes.length !== expectation.cubes) {
            fail(entry.id, `${name} cubes=${cubes.length}, expected ${expectation.cubes}`);
        }
        if (expectation.uv && JSON.stringify(cubes[0]?.uv) !== JSON.stringify(expectation.uv)) {
            fail(entry.id, `${name} UV=${JSON.stringify(cubes[0]?.uv)}, expected ${JSON.stringify(expectation.uv)}`);
        }
        for (const cube of cubes) {
            const expectedInflate = name === 'armorHead' && cube === cubes[1] ? inflate + 0.5 : inflate;
            if (cube.inflate !== expectedInflate) {
                fail(entry.id, `${name} inflate=${cube.inflate}, expected ${expectedInflate}`);
            }
        }
    }
}

function validateGeometry(entry, geo) {
    validateFinite(entry.id, geo, 'geo');
    if (geo.format_version !== '1.12.0') fail(entry.id, `format_version=${geo.format_version}`);
    const geometries = geo['minecraft:geometry'];
    if (!Array.isArray(geometries) || geometries.length !== 1) {
        fail(entry.id, `expected one geometry, found ${geometries?.length ?? 0}`);
        return;
    }

    const geometry = geometries[0];
    const description = geometry.description || {};
    if (description.identifier !== `geometry.${entry.identifier}`) {
        fail(entry.id, `identifier=${description.identifier}`);
    }
    const dimensions = pngSize(resolve(entry.texture));
    if (!dimensions) {
        fail(entry.id, `missing or invalid texture ${entry.texture}`);
    } else if (description.texture_width !== dimensions[0]
        || description.texture_height !== dimensions[1]) {
        fail(entry.id, `texture dimensions ${description.texture_width}x${description.texture_height}`
            + ` != PNG ${dimensions[0]}x${dimensions[1]}`);
    }

    const bones = Array.isArray(geometry.bones) ? geometry.bones : [];
    const names = new Set();
    for (const bone of bones) {
        if (!bone.name) fail(entry.id, 'bone without a name');
        else if (names.has(bone.name)) fail(entry.id, `duplicate bone ${bone.name}`);
        else names.add(bone.name);
    }
    for (const bone of bones) {
        if (bone.parent && !names.has(bone.parent)) {
            fail(entry.id, `bone ${bone.name} has missing parent ${bone.parent}`);
        }
        const ancestors = new Set([bone.name]);
        let parent = bone.parent;
        while (parent) {
            if (ancestors.has(parent)) {
                fail(entry.id, `parent cycle at ${bone.name}`);
                break;
            }
            ancestors.add(parent);
            parent = bones.find(candidate => candidate.name === parent)?.parent;
        }
        for (const cube of bone.cubes || []) {
            if (!Array.isArray(cube.size) || cube.size.length !== 3
                || cube.size.some(size => size < 0)) {
                fail(entry.id, `bone ${bone.name} has invalid cube size`);
            }
            if (!Array.isArray(cube.uv) || cube.uv.length !== 2
                || cube.uv.some(coordinate => coordinate < 0)) {
                fail(entry.id, `bone ${bone.name} has invalid cube UV`);
            }
        }
    }

    const cubes = bones.reduce((count, bone) => count + (bone.cubes?.length || 0), 0);
    if (bones.length !== entry.bones) fail(entry.id, `bones=${bones.length}, expected ${entry.bones}`);
    if (cubes !== entry.cubes) fail(entry.id, `cubes=${cubes}, expected ${entry.cubes}`);
    if (entry.parameters?.modelBipedInflate !== undefined) {
        validateModelBipedGeometry(entry, bones);
    }

    if (entry.animation_source) {
        const animationSource = resolve(entry.animation_source);
        if (!fs.existsSync(animationSource)) {
            fail(entry.id, `missing animation source ${entry.animation_source}`);
        } else {
            const source = fs.readFileSync(animationSource, 'utf8');
            const references = [...source.matchAll(/pose\.apply\("([^"]+)"\)/g)]
                .map(match => match[1]);
            const uniqueReferences = new Set(references);
            if (uniqueReferences.size !== entry.animation_bones) {
                fail(entry.id, `animation bones=${uniqueReferences.size}, expected ${entry.animation_bones}`);
            }
            if (references.length !== uniqueReferences.size) {
                fail(entry.id, `animation source contains duplicate pose bindings`);
            }
            for (const name of uniqueReferences) {
                if (!names.has(name)) fail(entry.id, `animation references missing bone ${name}`);
            }
        }
    }
}

if (!fs.existsSync(MANIFEST)) {
    throw new Error(`Missing manifest ${path.relative(ROOT, MANIFEST)}`);
}
const manifest = JSON.parse(fs.readFileSync(MANIFEST, 'utf8'));
for (const entry of manifest.models) {
    const source = resolve(entry.source);
    const texture = resolve(entry.texture);
    const target = resolve(entry.target);
    if (!fs.existsSync(source)) {
        fail(entry.id, `missing source ${entry.source}`);
        continue;
    }
    if (sha256(source) !== entry.source_sha256) {
        fail(entry.id, `source SHA-256 changed for ${entry.source}`);
        continue;
    }
    if (!fs.existsSync(texture)) {
        fail(entry.id, `missing texture ${entry.texture}`);
        continue;
    }

    let expected;
    try {
        expected = build(source, entry.identifier, texture, entry.parameters);
    } catch (error) {
        fail(entry.id, `conversion failed: ${error.message}`);
        continue;
    }
    validateGeometry(entry, expected);

    if (mode === '--write') {
        fs.mkdirSync(path.dirname(target), { recursive: true });
        fs.writeFileSync(target, stableJson(expected));
    }
    if (!fs.existsSync(target)) {
        fail(entry.id, `missing generated asset ${entry.target}`);
        continue;
    }

    let actual;
    try {
        actual = JSON.parse(fs.readFileSync(target, 'utf8'));
    } catch (error) {
        fail(entry.id, `invalid target JSON: ${error.message}`);
        continue;
    }
    validateGeometry(entry, actual);
    if (stableJson(actual) !== stableJson(expected)) {
        fail(entry.id, `${entry.target} is stale; run audit_geo_models.js --write`);
    }
}

if (failures.length) {
    failures.forEach(message => console.error(` - ${message}`));
    throw new Error(`RR_GEO_MODEL_AUDIT_FAILED blocked=${failures.length}`);
}

console.log(`RR_GEO_MODEL_AUDIT_OK models=${manifest.models.length}`
    + ` bones=${manifest.models.reduce((sum, entry) => sum + entry.bones, 0)}`
    + ` cubes=${manifest.models.reduce((sum, entry) => sum + entry.cubes, 0)}`);
