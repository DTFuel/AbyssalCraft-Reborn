const crypto = require('crypto');
const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..');
const LEGACY = path.join(ROOT, 'docs/AbyssalCraft-1.12.2/src/main/resources/assets/abyssalcraft');
const ASSETS = path.join(ROOT, 'src/main/resources/assets/abyssalcraft');
const CATALOG = path.join(ASSETS, 'complex_block_model_fidelity.json');
const mode = process.argv[2] || '--check';
if (!['--check', '--write'].includes(mode) || process.argv.length > 3) {
    console.error('Usage: node scripts/audit_complex_block_models.js [--check|--write]');
    process.exit(2);
}

const failures = [];
const slash = value => value.replaceAll('\\', '/');
const relative = file => slash(path.relative(ROOT, file));
const sha256 = file => crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');
const readJson = file => JSON.parse(fs.readFileSync(file, 'utf8').replace(/^\uFEFF/, ''));
const stableJson = value => `${JSON.stringify(value, null, 2)}\n`;
const asset = name => path.join(ASSETS, name);
const legacy = name => path.join(LEGACY, name);

function write(file, content) {
    fs.mkdirSync(path.dirname(file), { recursive: true });
    const normalized = Buffer.isBuffer(content) ? content : content.replace(/\r\n/g, '\n');
    if (!fs.existsSync(file) || !fs.readFileSync(file).equals(Buffer.from(normalized))) {
        fs.writeFileSync(file, normalized);
    }
}

function requireFile(file, owner) {
    if (!fs.existsSync(file)) failures.push(`${owner}: missing ${relative(file)}`);
    return fs.existsSync(file);
}

function allFaces(texture, uv) {
    return Object.fromEntries(['down', 'up', 'north', 'south', 'west', 'east']
        .map(face => [face, uv ? { uv, texture } : { texture }]));
}

function parseObj(file) {
    const objects = [];
    let current = null;
    const vertices = [];
    const textureCoordinates = [];
    for (const line of fs.readFileSync(file, 'utf8').split(/\r?\n/)) {
        const fields = line.trim().split(/\s+/);
        if (fields[0] === 'o') {
            current = { name: fields.slice(1).join('_'), indices: new Set(), faces: [] };
            objects.push(current);
        } else if (fields[0] === 'v') {
            vertices.push(fields.slice(1, 4).map(Number));
        } else if (fields[0] === 'vt') {
            textureCoordinates.push(fields.slice(1, 3).map(Number));
        } else if (fields[0] === 'f' && current) {
            const face = fields.slice(1).map(token => {
                const [vertex, texture] = token.split('/').map(Number);
                current.indices.add(vertex - 1);
                return { vertex: vertex - 1, texture: texture - 1 };
            });
            current.faces.push(face);
        }
    }
    return objects.filter(object => object.indices.size >= 4).map(object => {
        const points = [...object.indices].map(index => vertices[index]);
        const mins = [0, 1, 2].map(axis => Math.min(...points.map(point => point[axis])) * 16);
        const maxs = [0, 1, 2].map(axis => Math.max(...points.map(point => point[axis])) * 16);
        const clamp = value => Number(Math.max(-16, Math.min(32, value)).toFixed(4));
        const faces = {};
        const directions = ['north', 'south', 'up', 'down', 'east', 'west'];
        if (object.faces.length !== directions.length) {
            throw new Error(`${relative(file)} ${object.name} has ${object.faces.length} faces, expected six`);
        }
        for (let faceIndex = 0; faceIndex < object.faces.length; faceIndex++) {
            const face = object.faces[faceIndex];
            const direction = directions[faceIndex];
            const coordinates = face.map(reference => textureCoordinates[reference.texture]);
            if (coordinates.some(value => !value)) {
                throw new Error(`${relative(file)} ${object.name} has a face without texture coordinates`);
            }
            const u = coordinates.map(value => value[0] * 16);
            const v = coordinates.map(value => (1 - value[1]) * 16);
            faces[direction] = {
                texture: '#all',
                uv: [Math.min(...u), Math.min(...v), Math.max(...u), Math.max(...v)]
                    .map(value => Number(value.toFixed(4))),
            };
        }
        if (Object.keys(faces).length !== 6) {
            throw new Error(`${relative(file)} ${object.name} produced ${Object.keys(faces).length} model faces`);
        }
        return {
            name: object.name,
            from: mins.map(clamp),
            to: maxs.map(clamp),
            faces,
        };
    }).filter(element => element.from.some((value, axis) => value !== element.to[axis]));
}

const statueSources = {
    cthulhu: 'cthulhustatue', hastur: 'hasturstatue', jzahar: 'jzaharstatue',
    azathoth: 'azathothstatue', nyarlathotep: 'nyarlathotepstatue',
    yog_sothoth: 'yogsothothstatue', shub_niggurath: 'shubniggurathstatue',
};
const statueBlocks = [
    ['decorative_cthulhu_statue', 'cthulhu'], ['decorative_hastur_statue', 'hastur'],
    ['decorative_jzahar_statue', 'jzahar'], ['decorative_azathoth_statue', 'azathoth'],
    ['decorative_nyarlathotep_statue', 'nyarlathotep'],
    ['decorative_yog_sothoth_statue', 'yog_sothoth'],
    ['decorative_shub_niggurath_statue', 'shub_niggurath'],
    ['deity_statue', 'cthulhu'], ['cthulhu_statue', 'cthulhu'], ['hastur_statue', 'hastur'],
    ['jzahar_statue', 'jzahar'], ['azathoth_statue', 'azathoth'],
    ['nyarlathotep_statue', 'nyarlathotep'], ['yog_sothoth_statue', 'yog_sothoth'],
    ['shub_niggurath_statue', 'shub_niggurath'],
];
const tombstones = [
    ['tombstone_stone', 'stone'], ['tombstone_abyssal_stone', 'abyssal_stone'],
    ['tombstone_coralium_stone', 'coralium_stone'], ['tombstone_darkstone', 'darkstone'],
    ['tombstone_dreadstone', 'dreadstone'], ['tombstone_elysian_stone', 'elysian_stone'],
    ['tombstone_ethaxium', 'ethaxium'], ['tombstone_monolith_stone', 'monolith_stone'],
    ['tombstone_omothol_stone', 'omothol_stone'],
];
const legacyCustomDisplay = {
    gui: { rotation: [30, 45, 0], translation: [0, 0, 0], scale: [0.625, 0.625, 0.625] },
    ground: { rotation: [0, 0, 0], translation: [0, 3, 0], scale: [0.25, 0.25, 0.25] },
    head: { rotation: [0, 180, 0], translation: [0, 0, 0], scale: [1, 1, 1] },
    fixed: { rotation: [0, 180, 0], translation: [0, 0, 0], scale: [0.5, 0.5, 0.5] },
    thirdperson_righthand: {
        rotation: [75, 315, 0], translation: [0, 2.5, 0], scale: [0.375, 0.375, 0.375],
    },
    firstperson_righthand: { rotation: [0, 315, 0], translation: [0, 0, 0], scale: [0.4, 0.4, 0.4] },
};
const ores = [
    ['coralium_ore', 'stone', 'coralium', 'coraliumore'],
    ['abyssalnite_ore', 'stone', 'abyssalnite', 'abyore'],
    ['abyssal_abyssalnite_ore', 'abyssal_stone', 'abyssalnite', 'abyssal_abyssalnite_ore'],
    ['dreadlands_abyssalnite_ore', 'dreadstone', 'abyssalnite', 'abydreadore'],
    ['dreaded_abyssalnite_ore', 'dreadstone', 'dreaded_abyssalnite', 'dreadore'],
    ['nitre_ore', 'stone', 'nitre', 'nitreore'],
    ['abyssal_coralium_ore', 'abyssal_stone', 'abyssal_coralium', 'abycorore'],
    ['abyssal_iron_ore', 'abyssal_stone', 'iron', 'abyiroore'],
    ['abyssal_gold_ore', 'abyssal_stone', 'gold', 'abygolore'],
    ['abyssal_diamond_ore', 'abyssal_stone', 'diamond', 'abydiaore'],
    ['abyssal_nitre_ore', 'abyssal_stone', 'nitre', 'abynitore'],
    ['pearlescent_coralium_ore', 'stone', 'pearlescent_coralium', 'abypcorore'],
    ['liquified_coralium_ore', 'stone', 'liquified_coralium', 'abylcorore'],
];
const machines = [
    ['crystallizer', true], ['transmutator', true], ['materializer', false],
];
const rotations = { north: 0, east: 90, south: 180, west: 270 };

function horizontalState(model, litModel) {
    const variants = {};
    for (const [facing, y] of Object.entries(rotations)) {
        const rotation = y ? { y, uvlock: true } : {};
        if (litModel) {
            variants[`facing=${facing},lit=false`] = { model: `abyssalcraft:block/${model}`, ...rotation };
            variants[`facing=${facing},lit=true`] = { model: `abyssalcraft:block/${litModel}`, ...rotation };
        } else variants[`facing=${facing}`] = { model: `abyssalcraft:block/${model}`, ...rotation };
    }
    return { variants };
}

function modernTexture(name) {
    return `abyssalcraft:block/${name}`;
}

function hostTexture(name) {
    return name === 'stone' ? 'minecraft:block/stone' : modernTexture(name);
}

function visibleElements(elements) {
    return structuredClone(elements).map(element => {
        for (const [face, definition] of Object.entries(element.faces || {})) {
            if (definition.texture === '#-1') delete element.faces[face];
        }
        return element;
    });
}

function generate() {
    write(asset('models/block/legacy_custom_model.json'), stableJson({
        credit: 'Shared 1.12.2 custom-block item transforms',
        display: legacyCustomDisplay,
    }));
    const ownedBlocks = [...statueBlocks.map(value => value[0]), ...tombstones.map(value => value[0]),
        ...ores.map(value => value[0]), ...machines.map(value => value[0]),
        'dreadlands_grass', 'fused_abyssal_sand'];
    for (const block of ownedBlocks) {
        for (const type of ['blockstates', 'models/block', 'models/item']) {
            const stale = path.join(ROOT, `src/main/generated/assets/abyssalcraft/${type}/${block}.json`);
            if (fs.existsSync(stale)) fs.rmSync(stale);
        }
    }
    for (const [deity, source] of Object.entries(statueSources)) {
        const obj = legacy(`models/block/${source}.obj`);
        const textureSource = legacy(`textures/model/blocks/${source}.png`);
        const textureTarget = asset(`textures/block/statue/${deity}.png`);
        const objects = parseObj(obj);
        if (objects.length < 5) throw new Error(`${source}.obj produced only ${objects.length} objects`);
        write(textureTarget, fs.readFileSync(textureSource));
        write(asset(`models/block/statue/${source}.obj`), fs.readFileSync(obj));
        write(asset(`models/block/statue/${source}.mtl`), [
            `# Deterministic material remap of ${relative(legacy(`models/block/${source}.mtl`))}`,
            '',
            'newmtl None',
            'Ka 1.000000 1.000000 1.000000',
            `map_Kd ${modernTexture(`statue/${deity}`)}`,
            '',
        ].join('\n'));
        write(asset(`models/block/statue/${deity}.json`), stableJson({
            credit: `Exact OBJ port of ${relative(obj)}`,
            loader: '__LOADER__:obj',
            model: `abyssalcraft:models/block/statue/${source}.obj`,
            flip_v: true,
            automatic_culling: true,
            shade_quads: true,
            emissive_ambient: false,
            render_type: 'minecraft:translucent',
            textures: { particle: modernTexture(`statue/${deity}`) },
        }));
    }
    for (const [block, deity] of statueBlocks) {
        write(asset(`blockstates/${block}.json`), stableJson(horizontalState(`statue/${deity}`)));
        write(asset(`models/item/${block}.json`), stableJson({
            parent: `abyssalcraft:block/statue/${deity}`,
            display: legacyCustomDisplay,
        }));
    }

    for (const [source, target] of [
        ['energycontainer', 'energy_container'],
        ['tiered_energycontainer', 'tiered_energy_container'],
    ]) {
        const legacyModel = readJson(legacy(`models/block/${source}.json`));
        write(asset(`models/block/${target}.json`), stableJson({
            credit: `Exact element geometry port of ${relative(legacy(`models/block/${source}.json`))}`,
            parent: 'abyssalcraft:block/legacy_custom_model',
            elements: legacyModel.elements,
        }));
    }
    for (const [source, target] of [
        ['energycollector', 'energy_collector'],
        ['tiered_energycollector', 'tiered_energy_collector'],
        ['energydepositioner', 'energy_depositioner'],
    ]) {
        const legacyModel = readJson(legacy(`models/block/${source}.json`));
        write(asset(`models/block/${target}.json`), stableJson({
            credit: `Exact element geometry port of ${relative(legacy(`models/block/${source}.json`))}`,
            parent: 'abyssalcraft:block/legacy_custom_model',
            render_type: 'minecraft:cutout',
            elements: visibleElements(legacyModel.elements),
        }));
    }
    const relayDisplay = readJson(legacy('models/item/energyrelay.json')).display;
    for (const [source, target] of [
        ['energyrelay', 'energy_relay'],
        ['tiered_energyrelay', 'tiered_energy_relay'],
    ]) {
        const legacyModel = readJson(legacy(`models/block/${source}.json`));
        write(asset(`models/block/${target}.json`), stableJson({
            credit: `Exact element geometry port of ${relative(legacy(`models/block/${source}.json`))}`,
            render_type: 'minecraft:cutout',
            elements: visibleElements(legacyModel.elements),
            display: relayDisplay,
        }));
    }

    const tombstoneParent = readJson(legacy('models/block/tombstone.json'));
    for (const [block, texture] of tombstones) {
        const source = legacy(`models/block/${block}.json`);
        const sourceModel = readJson(source);
        const secondary = sourceModel.textures['1'];
        const modernSecondary = secondary && secondary.includes('cobblestone')
            ? 'minecraft:block/cobblestone' : modernTexture(texture);
        const primary = hostTexture(texture);
        write(asset(`models/block/${block}.json`), stableJson({
            credit: `Port of ${relative(source)} using legacy tombstone geometry`,
            textures: { '0': primary, '1': modernSecondary, particle: primary },
            elements: tombstoneParent.elements,
            display: tombstoneParent.display,
        }));
        write(asset(`blockstates/${block}.json`), stableJson(horizontalState(block)));
        write(asset(`models/item/${block}.json`), stableJson({ parent: `abyssalcraft:block/${block}` }));
    }

    const layered = readJson(legacy('models/block/layered_cube.json'));
    write(asset('models/block/layered_ore.json'), stableJson({
        credit: `Port of ${relative(legacy('models/block/layered_cube.json'))}`,
        parent: 'minecraft:block/block',
        render_type: 'minecraft:cutout',
        ambientocclusion: layered.ambientocclusion,
        textures: layered.textures,
        elements: layered.elements,
    }));
    for (const [block, base, overlay] of ores) {
        write(asset(`models/block/${block}.json`), stableJson({
            parent: 'abyssalcraft:block/layered_ore',
            textures: { all: hostTexture(base), overlay: modernTexture(`ore_overlay/${overlay}`) },
        }));
        const overlaySource = legacy(`textures/blocks/ores/${overlay}.png`);
        const overlayTarget = asset(`textures/block/ore_overlay/${overlay}.png`);
        write(overlayTarget, fs.readFileSync(overlaySource));
        write(asset(`blockstates/${block}.json`), stableJson({ variants: { '': { model: `abyssalcraft:block/${block}` } } }));
        write(asset(`models/item/${block}.json`), stableJson({ parent: `abyssalcraft:block/${block}` }));
    }
    write(asset('models/block/dreadlands_grass.json'), stableJson({
        parent: 'minecraft:block/grass_block',
        textures: {
            particle: modernTexture('dreadlands_dirt'), bottom: modernTexture('dreadlands_dirt'),
            side: modernTexture('dreadlands_grass_side'), top: 'minecraft:block/grass_block_top',
            overlay: 'minecraft:block/grass_block_side_overlay',
        },
    }));
    write(asset('blockstates/dreadlands_grass.json'), stableJson({
        variants: { '': { model: 'abyssalcraft:block/dreadlands_grass' } },
    }));
    write(asset('models/item/dreadlands_grass.json'), stableJson({ parent: 'abyssalcraft:block/dreadlands_grass' }));
    write(asset('models/block/fused_abyssal_sand.json'), stableJson({
        parent: 'minecraft:block/cube_bottom_top',
        textures: {
            particle: modernTexture('abyssal_sand'), bottom: modernTexture('abyssal_sand'),
            side: modernTexture('fused_abyssal_sand_side'), top: modernTexture('fused_abyssal_sand_top'),
        },
    }));
    write(asset('blockstates/fused_abyssal_sand.json'), stableJson({
        variants: { '': { model: 'abyssalcraft:block/fused_abyssal_sand' } },
    }));
    write(asset('models/item/fused_abyssal_sand.json'), stableJson({ parent: 'abyssalcraft:block/fused_abyssal_sand' }));

    const entries = [];
    for (const [block, deity] of statueBlocks) {
        const source = statueSources[deity];
        entries.push(entry(block, 'statue', `models/block/${source}.obj`,
            [`models/block/statue/${deity}.json`], [`textures/block/statue/${deity}.png`],
            'facing=north/east/south/west -> y=0/90/180/270, uvlock on rotated states',
            'Original OBJ vertices, faces, and UVs are preserved byte-for-byte and baked by the active loader OBJ implementation.',
            [`models/block/${source}.mtl`, `textures/model/blocks/${source}.png`]));
    }
    for (const [block] of tombstones) entries.push(entry(block, 'tombstone', `models/block/${block}.json`,
        [`models/block/${block}.json`], tombstones.find(value => value[0] === block)[1] === 'stone'
            ? [] : [`textures/block/${tombstones.find(value => value[0] === block)[1]}.png`],
        'facing=north/east/south/west -> y=0/90/180/270, uvlock on rotated states',
        'Exact eight-element legacy Blockbench tombstone geometry with material-specific modern textures.',
        ['models/block/tombstone.json']));
    for (const [block, base, overlay, sourceState] of ores) entries.push(entry(block, 'layered_ore', `blockstates/${sourceState}.json`,
        ['models/block/layered_ore.json', `models/block/${block}.json`],
        [...(base === 'stone' ? [] : [`textures/block/${base}.png`]), `textures/block/ore_overlay/${overlay}.png`],
        'single state -> base cube plus 0.001-expanded overlay cube',
        'Exact legacy two-layer cube geometry; modern child supplies independent host stone and ore overlay.',
        ['models/block/layered_cube.json', `textures/blocks/ores/${overlay}.png`]));
    for (const [block, lit] of machines) entries.push(entry(block, 'machine', `models/block/${block}.json`,
        [`models/block/${block}.json`, ...(lit ? [`models/block/${block}_active.json`] : [])],
        [`textures/block/${block}_front.png`, `textures/block/${block}_side.png`, `textures/block/${block}_top.png`,
            ...(lit ? [`textures/block/${block}_front_active.png`] : [])],
        lit ? 'four horizontal facings x lit=false/true; active model and front texture differ'
            : 'four horizontal facings; no synthetic lit property',
        'Vanilla orientable geometry preserves distinct front/side/top faces and rotates through blockstate.'));
    entries.push(entry('dreadlands_grass', 'multiface_ground', 'models/block/dreadgrass.json',
        ['models/block/dreadlands_grass.json'], ['textures/block/dreadlands_grass_side.png', 'textures/block/dreadlands_dirt.png'],
        'single state -> distinct bottom/side/top plus tinted overlay', 'Vanilla grass parent retains legacy grass tint and separate dirt/side faces.'));
    entries.push(entry('fused_abyssal_sand', 'multiface_ground', 'blockstates/fusedabyssalsand.json',
        ['models/block/fused_abyssal_sand.json'], ['textures/block/fused_abyssal_sand_side.png',
            'textures/block/abyssal_sand.png', 'textures/block/fused_abyssal_sand_top.png'],
        'single state -> distinct bottom/side/top', 'cube_bottom_top replacement preserves the legacy three-surface material intent.'));
    entries.sort((left, right) => left.block.localeCompare(right.block));
    write(CATALOG, stableJson({ schema: 1, task: 'T9.2b / PK-2b', algorithm: 'SHA-256', entries }));
}

function entry(block, kind, source, models, textures, stateMapping, geometryRationale, relatedSources = []) {
    const sourceFile = legacy(source);
    const sourceArtifacts = [source, ...relatedSources].map(name => {
        const file = legacy(name);
        return { path: relative(file), sha256: sha256(file) };
    });
    return {
        block: `abyssalcraft:${block}`, status: 'ACTIVE', kind,
        legacySource: relative(sourceFile), legacySha256: sha256(sourceFile),
        sourceArtifacts,
        modernBlockstate: `src/main/resources/assets/abyssalcraft/blockstates/${block}.json`,
        modernModels: models.map(model => `src/main/resources/assets/abyssalcraft/${model}`),
        textures: textures.map(texture => `src/main/resources/assets/abyssalcraft/${texture}`),
        stateMapping, geometryRationale,
    };
}

function auditModel(file, owner) {
    if (!requireFile(file, owner)) return null;
    let json;
    try { json = readJson(file); } catch (error) {
        failures.push(`${owner}: invalid JSON ${relative(file)}: ${error.message}`);
        return null;
    }
    return json;
}

function audit() {
    if (!requireFile(CATALOG, 'catalog')) return;
    const catalog = auditModel(CATALOG, 'catalog');
    if (!catalog) return;
    const expected = 42;
    if (catalog.entries.length !== expected) failures.push(`catalog entries=${catalog.entries.length}, expected=${expected}`);
    const ids = new Set();
    for (const item of catalog.entries) {
        const owner = item.block;
        if (ids.has(owner)) failures.push(`${owner}: duplicate catalog entry`);
        ids.add(owner);
        if (item.status !== 'ACTIVE') failures.push(`${owner}: status=${item.status}`);
        const source = path.join(ROOT, item.legacySource);
        if (requireFile(source, owner) && sha256(source) !== item.legacySha256) failures.push(`${owner}: stale legacy SHA-256`);
        for (const artifact of item.sourceArtifacts || []) {
            const artifactFile = path.join(ROOT, artifact.path);
            if (requireFile(artifactFile, owner) && sha256(artifactFile) !== artifact.sha256) {
                failures.push(`${owner}: stale source artifact SHA-256 ${artifact.path}`);
            }
        }
        const state = auditModel(path.join(ROOT, item.modernBlockstate), owner);
        const models = item.modernModels.map(model => auditModel(path.join(ROOT, model), owner)).filter(Boolean);
        item.textures.forEach(texture => requireFile(path.join(ROOT, texture), owner));
        for (const model of models) {
            for (const texture of Object.values(model.textures || {})) {
                if (typeof texture !== 'string' || texture.startsWith('#') || !texture.startsWith('abyssalcraft:')) continue;
                const texturePath = texture.slice('abyssalcraft:'.length);
                requireFile(asset(`textures/${texturePath}.png`), `${owner} model texture`);
            }
        }
        if (!state) continue;
        const serializedState = JSON.stringify(state);
        item.modernModels.forEach(model => {
            const marker = 'assets/abyssalcraft/models/';
            const reference = `abyssalcraft:${model.slice(model.indexOf(marker) + marker.length, -5)}`;
            if (!serializedState.includes(reference) && item.kind !== 'layered_ore' && !model.includes('_active')) {
                failures.push(`${owner}: blockstate does not reference ${reference}`);
            }
        });
        if (item.kind === 'statue') {
            const model = models[0] || {};
            const itemModel = auditModel(asset(`models/item/${owner.slice('abyssalcraft:'.length)}.json`), owner) || {};
            if (JSON.stringify(itemModel.display?.gui?.rotation) !== JSON.stringify(legacyCustomDisplay.gui.rotation)
                || JSON.stringify(itemModel.display?.gui?.scale) !== JSON.stringify(legacyCustomDisplay.gui.scale)) {
                failures.push(`${owner}: statue item is missing the legacy GUI transform`);
            }
            if (model.loader !== '__LOADER__:obj') failures.push(`${owner}: statue loader=${model.loader}`);
            if (model.flip_v !== true) failures.push(`${owner}: statue OBJ must enable flip_v`);
            if (model.render_type !== 'minecraft:translucent') {
                failures.push(`${owner}: statue render_type=${model.render_type}`);
            }
            const modelReference = typeof model.model === 'string' ? model.model : '';
            const expectedSource = path.basename(item.legacySource, '.obj');
            const expectedReference = `abyssalcraft:models/block/statue/${expectedSource}.obj`;
            if (modelReference !== expectedReference) {
                failures.push(`${owner}: statue model=${modelReference}, expected=${expectedReference}`);
            } else {
                const targetObj = asset(modelReference.slice('abyssalcraft:'.length));
                const targetMtl = targetObj.replace(/\.obj$/, '.mtl');
                if (requireFile(targetObj, owner) && sha256(targetObj) !== sha256(source)) {
                    failures.push(`${owner}: modern OBJ does not exactly preserve legacy geometry`);
                }
                if (requireFile(targetMtl, owner)) {
                    const material = fs.readFileSync(targetMtl, 'utf8');
                    const expectedTexture = Object.values(model.textures || {}).find(value => value?.includes('statue/'));
                    if (!expectedTexture || !material.includes(`map_Kd ${expectedTexture}`)) {
                        failures.push(`${owner}: modern MTL does not bind ${expectedTexture}`);
                    }
                }
            }
            const objects = parseObj(source);
            if (objects.length < 5) failures.push(`${owner}: statue OBJ has only ${objects.length} objects`);
            auditHorizontal(owner, state, false);
        } else if (item.kind === 'tombstone') {
            if ((models[0]?.elements || []).length !== 8) failures.push(`${owner}: tombstone geometry is not the legacy eight-element form`);
            auditHorizontal(owner, state, false);
        } else if (item.kind === 'layered_ore') {
            const parent = models.find(model => model.elements)?.elements || [];
            if (parent.length !== 2) failures.push(`${owner}: layered ore must have exactly base and overlay elements`);
            if (!models.some(model => model.textures?.all && model.textures?.overlay)) failures.push(`${owner}: layered ore textures incomplete`);
            if (!models.some(model => model.render_type === 'minecraft:cutout')) {
                failures.push(`${owner}: layered ore parent must use minecraft:cutout`);
            }
        } else if (item.kind === 'machine') auditHorizontal(owner, state, item.modernModels.length === 2);
        else if (item.kind === 'multiface_ground') {
            const textures = models[0]?.textures || {};
            const distinct = new Set(Object.values(textures).filter(value => typeof value === 'string'));
            if (distinct.size < 3) failures.push(`${owner}: multiface ground has fewer than three texture bindings`);
        }
    }
    for (const [model, expectedElements] of Object.entries({
        energy_collector: 9,
        tiered_energy_collector: 12,
        energy_relay: 23,
        tiered_energy_relay: 26,
        energy_depositioner: 9,
    })) {
        const generated = auditModel(asset(`models/block/${model}.json`), `abyssalcraft:${model}`);
        if ((generated?.elements || []).length !== expectedElements) {
            failures.push(`abyssalcraft:${model}: elements=${generated?.elements?.length}, expected=${expectedElements}`);
        }
        if (generated?.render_type !== 'minecraft:cutout') {
            failures.push(`abyssalcraft:${model}: render_type=${generated?.render_type}`);
        }
    }
    const counts = Object.fromEntries([...new Set(catalog.entries.map(item => item.kind))].sort()
        .map(kind => [kind, catalog.entries.filter(item => item.kind === kind).length]));
    if (!failures.length) console.log(`RR_COMPLEX_BLOCK_MODEL_AUDIT_OK active=${catalog.entries.length}`
        + ` blocked=0 sources=${new Set(catalog.entries.flatMap(item => item.sourceArtifacts.map(source => source.sha256))).size}`
        + ` models=${new Set(catalog.entries.flatMap(item => item.modernModels)).size}`
        + ` textures=${new Set(catalog.entries.flatMap(item => item.textures)).size}`
        + ` coverage=${JSON.stringify(counts)}`);
}

function auditHorizontal(owner, state, lit) {
    const variants = state.variants || {};
    const expected = lit ? 8 : 4;
    if (Object.keys(variants).length !== expected) failures.push(`${owner}: horizontal states=${Object.keys(variants).length}, expected=${expected}`);
    for (const [facing, y] of Object.entries(rotations)) {
        const keys = lit ? [`facing=${facing},lit=false`, `facing=${facing},lit=true`] : [`facing=${facing}`];
        for (const key of keys) {
            const variant = variants[key];
            if (!variant) failures.push(`${owner}: missing ${key}`);
            else if ((variant.y || 0) !== y || (y !== 0 && variant.uvlock !== true)) failures.push(`${owner}: invalid rotation ${key}`);
        }
        if (lit && variants[keys[0]]?.model === variants[keys[1]]?.model) failures.push(`${owner}: idle/active models are identical`);
    }
}

if (mode === '--write') generate();
audit();
if (failures.length) {
    console.error(failures.join('\n'));
    throw new Error(`RR_COMPLEX_BLOCK_MODEL_AUDIT_FAILED blocked=${failures.length}`);
}