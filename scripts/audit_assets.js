const crypto = require('crypto');
const fs = require('fs');
const path = require('path');
const zlib = require('zlib');
const { readPng, composite, crop } = require('./png_rgba');
const {
    LANGUAGES: LOCALIZATION_LANGUAGES,
    ENTITY_NAME_ROWS,
    DISPLAY_NAME_OVERRIDES,
    TERM_REPLACEMENTS,
    IDENTICAL_TO_ENGLISH,
    MIRRORED_NAME_PAIRS,
    ID_FAMILY_TERMS,
    ID_FAMILY_EXCEPTIONS,
    UI_TEXT_ROWS,
} = require('./localization_name_contract');

const ROOT = path.resolve(__dirname, '..');
const ASSET_ROOTS = [
    path.join(ROOT, 'src/main/resources/assets/abyssalcraft'),
    path.join(ROOT, 'src/main/generated/assets/abyssalcraft'),
];
const LEGACY_ROOT = path.join(ROOT,
    'docs/AbyssalCraft-1.12.2/src/main/resources/assets/abyssalcraft');
const LEDGER = path.join(ROOT, 'docs/validation/RR-ASSET-SHA256.json');
const LEGACY_TEXTURE_LEDGER = path.join(ROOT, 'docs/validation/RR-LEGACY-TEXTURES.json');
const RETIRED_LEGACY_TEXTURES = new Set([
    'logo.png',
    'armor/default.png',
    'blocks/altar.png',
    'blocks/altar/basebot.png',
    'blocks/altar/basetop.png',
    'blocks/altar/parts.png',
    'blocks/calcifiedstone.png',
    'blocks/dsbf.png',
    'blocks/ritualaltar/cloth2.png',
    'blocks/ritualaltar/cloth3.png',
    'blocks/ritualaltar/cloth4.png',
    'blocks/ritualaltar/parts2.png',
    'blocks/ritualpedestal/overlay_1.png',
    'blocks/ritualpedestal/overlay_2.png',
    'blocks/ritualpedestal/overlay_3.png',
    'blocks/ritualpedestal/overlay_4.png',
    'blocks/ritualpedestal/overlay_5.png',
    'blocks/ritualpedestal/overlay_6.png',
    'blocks/ritualpedestal/overlay_7.png',
    'blocks/summoning_statue/masonry.png',
    'blocks/summoning_statue/misc.png',
    'blocks/summoning_statue/robe_front.png',
    'blocks/summoning_statue/robe_sides.png',
    'blocks/summoning_statue/robe_top.png',
    'gui/necronomicon/crafting.png',
    'gui/necronomicon/crystallization.png',
    'gui/necronomicon/item.png',
    'gui/necronomicon/materialization.png',
    'gui/necronomicon/missing.png',
    'gui/necronomicon/missing_item.png',
    'gui/necronomicon/missing_recipe.png',
    'gui/necronomicon/placeofpower.png',
    'gui/necronomicon/ritual.png',
    'gui/necronomicon/ritual_creation.png',
    'gui/necronomicon/ritual_infusion.png',
    'gui/necronomicon/spell.png',
    'gui/necronomicon/template.png',
    'gui/necronomicon/template1024.png',
    'gui/necronomicon/template512.png',
    'gui/necronomicon/transmutation.png',
    'items/deprecated.png',
    'items/devsword.png',
    'items/hilt.png',
    'items/necronahicon.png',
    'items/scrolls/scroll_alt.png',
    'items/scriptures_omniscience.png',
    'model/abyssal_zombie_old.png',
    'model/abyssal_zombie_old_eyes.png',
    'model/remnant/trader/villager.png',
    'model/staff2.png',
]);
const AUTHORED_TEXTURES = new Set([
    'block/chiseled_coralium_stone_brick.png',
    'block/cracked_coralium_stone_brick.png',
    'block/ethaxium_bricks/composite_1.png',
    'block/ethaxium_bricks/composite_2.png',
    'block/ethaxium_bricks/composite_3.png',
    'block/ethaxium_bricks/dark_composite_1.png',
    'block/ethaxium_bricks/dark_composite_2.png',
    'block/ethaxium_bricks/dark_composite_3.png',
    'block/ghoul_head/depths_ghoul.png',
    'block/ghoul_head/depths_ghoul_orange.png',
    'block/ghoul_head/depths_ghoul_pete.png',
    'block/ghoul_head/depths_ghoul_wilson.png',
    'mob_effect/antimatter.png',
    'mob_effect/coralium_antidote.png',
    'mob_effect/coralium_plague.png',
    'mob_effect/dread_antidote.png',
    'mob_effect/dread_plague.png',
    'particle/abyssal_fx.png',
]);
const missing = [];
const mode = process.argv[2] || '--write';
if (!['--check', '--write'].includes(mode) || process.argv.length > 3) {
    console.error('Usage: node scripts/audit_assets.js [--check|--write]');
    process.exit(2);
}

function walk(directory) {
    if (!fs.existsSync(directory)) return [];
    return fs.readdirSync(directory, { withFileTypes: true }).flatMap(entry => {
        const target = path.join(directory, entry.name);
        return entry.isDirectory() ? walk(target) : [target];
    });
}

function relative(file, root) {
    return path.relative(root, file).replaceAll('\\', '/');
}

function find(relativePath) {
    return ASSET_ROOTS.map(root => path.join(root, relativePath)).find(fs.existsSync);
}

function requireAsset(relativePath, owner) {
    const file = find(relativePath);
    if (!file) missing.push(`${relativePath} <- ${owner}`);
    return file;
}

function readJson(file) {
    return JSON.parse(fs.readFileSync(file, 'utf8').replace(/^\uFEFF/, ''));
}

function sha256(file) {
    return crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');
}

function decodablePng(file) {
    const bytes = fs.readFileSync(file);
    if (bytes.subarray(0, 8).toString('hex') !== '89504e470d0a1a0a'
        || bytes.length < 24 || bytes.readUInt32BE(16) < 1 || bytes.readUInt32BE(20) < 1) return false;
    const compressed = [];
    for (let offset = 8; offset + 12 <= bytes.length;) {
        const length = bytes.readUInt32BE(offset);
        if (bytes.toString('ascii', offset + 4, offset + 8) === 'IDAT') {
            compressed.push(bytes.subarray(offset + 8, offset + 8 + length));
        }
        offset += 12 + length;
    }
    try {
        return zlib.inflateSync(Buffer.concat(compressed)).length > 0;
    } catch (_) {
        return false;
    }
}

function pngDimensions(file) {
    const bytes = fs.readFileSync(file);
    if (bytes.length < 24 || bytes.subarray(0, 8).toString('hex') !== '89504e470d0a1a0a') return null;
    return { width: bytes.readUInt32BE(16), height: bytes.readUInt32BE(20) };
}

function legacyTextureDisposition(source, modernByHash, sourceHash) {
    if (RETIRED_LEGACY_TEXTURES.has(source)) {
        return {
            status: 'RETIRED',
            targets: ['docs/spec/legacy-texture-audit.md'],
            reason: 'The legacy-only, test, deprecated, old-variant, or removed content owner is absent from the frozen modern production contract.',
            owner: 'RR-ASSET/PK-1b',
        };
    }

    const exactTargets = modernByHash.get(sourceHash);
    if (exactTargets) {
        return {
            status: 'MIGRATED',
            targets: exactTargets,
            reason: 'Legacy pixels are preserved byte-for-byte in the listed modern texture target(s).',
            owner: 'RR-ASSET/PK-1b',
        };
    }

    if (source === 'blocks/coralium_bricks/cracks.png') {
        return {
            status: 'REPLACED',
            targets: ['src/main/resources/assets/abyssalcraft/textures/block/cracked_coralium_stone_brick.png'],
            reason: 'The legacy cracks overlay is deterministically source-over composited with bricks_base into the registered modern cracked Coralium brick texture.',
            owner: 'RR-ASSET/PK-2b',
        };
    }
    if (source === 'blocks/coralium_bricks/sigil.png') {
        return {
            status: 'REPLACED',
            targets: ['src/main/resources/assets/abyssalcraft/textures/block/chiseled_coralium_stone_brick.png'],
            reason: 'The legacy sigil overlay is deterministically source-over composited with bricks_base into the registered modern chiseled Coralium brick texture.',
            owner: 'RR-ASSET/PK-2b',
        };
    }
    if (source === 'misc/potionfx.png') {
        return {
            status: 'REPLACED',
            targets: [
                'src/main/resources/assets/abyssalcraft/textures/mob_effect/coralium_plague.png',
                'src/main/resources/assets/abyssalcraft/textures/mob_effect/dread_plague.png',
                'src/main/resources/assets/abyssalcraft/textures/mob_effect/antimatter.png',
                'src/main/resources/assets/abyssalcraft/textures/mob_effect/coralium_antidote.png',
                'src/main/resources/assets/abyssalcraft/textures/mob_effect/dread_antidote.png',
            ],
            reason: 'The four legacy 18x18 status-icon cells are deterministically cropped into the five registered modern MobEffect texture paths; both antidotes share legacy cell 3.',
            owner: 'RR-ASSET/mob-effects',
        };
    }
    throw new Error(`No legacy texture disposition rule for ${source}`);
}

function split(reference, defaultNamespace) {
    const separator = reference.indexOf(':');
    return separator < 0
        ? [defaultNamespace, reference]
        : [reference.slice(0, separator), reference.slice(separator + 1)];
}

const models = new Set();
const textures = new Set();

function auditTexture(reference, owner) {
    const [namespace, name] = split(reference, 'minecraft');
    if (namespace !== 'abyssalcraft' || textures.has(name)) return;
    textures.add(name);
    requireAsset(`textures/${name}.png`, owner);
}

function auditModel(reference, owner, defaultNamespace = 'abyssalcraft') {
    const [namespace, name] = split(reference, defaultNamespace);
    if (namespace !== 'abyssalcraft' || models.has(name)) return;
    models.add(name);
    const relativePath = `models/${name}.json`;
    const file = requireAsset(relativePath, owner);
    if (!file) return;
    const model = readJson(file);
    if (model.parent) auditModel(model.parent, relativePath, 'minecraft');
    if (model.loader === '__LOADER__:obj') {
        const [objNamespace, objPath] = split(model.model || '', 'abyssalcraft');
        if (objNamespace !== 'abyssalcraft' || !objPath.endsWith('.obj')) {
            missing.push(`${relativePath} invalid OBJ reference ${model.model}`);
        } else {
            requireAsset(objPath, relativePath);
            const materialPath = objPath.replace(/\.obj$/, '.mtl');
            const materialFile = requireAsset(materialPath, relativePath);
            if (materialFile) {
                const material = fs.readFileSync(materialFile, 'utf8');
                const texture = material.match(/^map_Kd\s+(\S+)$/m)?.[1];
                if (texture) auditTexture(texture, materialPath);
                else missing.push(`${materialPath} missing map_Kd texture`);
            }
        }
    }
    for (const texture of Object.values(model.textures || {})) {
        if (typeof texture === 'string' && !texture.startsWith('#')) {
            auditTexture(texture, relativePath);
        }
    }
}

const modelMetadata = new Map();

function inspectModel(reference, defaultNamespace = 'abyssalcraft', visiting = new Set()) {
    const [namespace, name] = split(reference, defaultNamespace);
    if (namespace === 'minecraft') {
        return { customGeometry: false, guiDisplay: name.startsWith('block/') };
    }
    if (namespace !== 'abyssalcraft' || visiting.has(name)) {
        return { customGeometry: false, guiDisplay: false };
    }
    if (modelMetadata.has(name)) return modelMetadata.get(name);
    const file = find(`models/${name}.json`);
    if (!file) return { customGeometry: false, guiDisplay: false };
    const model = readJson(file);
    const nextVisiting = new Set(visiting).add(name);
    const inherited = model.parent
        ? inspectModel(model.parent, 'minecraft', nextVisiting)
        : { customGeometry: false, guiDisplay: false };
    const metadata = {
        customGeometry: model.loader === '__LOADER__:obj' || Array.isArray(model.elements)
            || inherited.customGeometry,
        guiDisplay: model.display?.gui != null || inherited.guiDisplay,
    };
    modelMetadata.set(name, metadata);
    return metadata;
}

function visit(value, owner) {
    if (Array.isArray(value)) {
        value.forEach(child => visit(child, owner));
    } else if (value && typeof value === 'object') {
        for (const [key, child] of Object.entries(value)) {
            if (key === 'model' && typeof child === 'string') auditModel(child, owner);
            else if ((key === 'texture' || key === 'file') && typeof child === 'string') {
                auditTexture(child, owner);
            } else visit(child, owner);
        }
    }
}

const blockstates = ASSET_ROOTS.flatMap(root => walk(path.join(root, 'blockstates')))
    .filter(file => file.endsWith('.json'));
for (const file of blockstates) visit(readJson(file), relative(file, ROOT));

const itemModels = ASSET_ROOTS.flatMap(root => walk(path.join(root, 'models/item')))
    .filter(file => file.endsWith('.json'));
let customItemModels = 0;
for (const file of itemModels) {
    const reference = `abyssalcraft:item/${path.basename(file, '.json')}`;
    auditModel(reference, relative(file, ROOT));
    const itemModel = readJson(file);
    if (typeof itemModel.parent !== 'string' || !itemModel.parent.startsWith('abyssalcraft:block/')) continue;
    const metadata = inspectModel(reference);
    if (metadata.customGeometry) {
        customItemModels++;
        if (!metadata.guiDisplay) missing.push(`${reference} custom geometry has no GUI display transform`);
    }
}

for (const root of ASSET_ROOTS) {
    for (const file of walk(path.join(root, 'models')).filter(candidate => candidate.endsWith('.json'))) {
        const source = fs.readFileSync(file, 'utf8');
        if (source.includes('#missing') || source.includes('#-1')) {
            missing.push(`model contains placeholder texture face ${relative(file, ROOT)}`);
        }
        if (/minecraft:item\/(?:bundle|amethyst_shard|blaze_powder)/.test(source)) {
            missing.push(`model borrows forbidden placeholder item texture ${relative(file, ROOT)}`);
        }
    }
}

function requireModelContract(name, parent, textures, renderType) {
    const file = requireAsset(`models/block/${name}.json`, `model contract ${name}`);
    if (!file) return;
    const model = readJson(file);
    if (model.parent !== parent) missing.push(`model contract ${name} parent=${model.parent}, expected=${parent}`);
    if (renderType && model.render_type !== renderType) {
        missing.push(`model contract ${name} render_type=${model.render_type}, expected=${renderType}`);
    }
    for (const [slot, texture] of Object.entries(textures)) {
        if (model.textures?.[slot] !== texture) {
            missing.push(`model contract ${name} texture ${slot}=${model.textures?.[slot]}, expected=${texture}`);
        }
    }
}

function requireElementModelContract(name, expectedElements, requiresRotation = true) {
    const file = requireAsset(`models/block/${name}.json`, `element model contract ${name}`);
    if (!file) return;
    const model = readJson(file);
    if (!Array.isArray(model.elements) || model.elements.length !== expectedElements) {
        missing.push(`element model contract ${name} elements=${model.elements?.length}, expected=${expectedElements}`);
    }
    if (requiresRotation && !model.elements?.some(element => element.rotation)) {
        missing.push(`element model contract ${name} has no rotated elements`);
    }
}

const energyTiers = ['', 'overworld_', 'abyssal_wasteland_', 'dreadlands_', 'omothol_'];
const energyModelName = (prefix, kind) => prefix ? `${prefix}energy_${kind}` : `energy${kind}`;
const energyContainerHosts = {
    '': null,
    'overworld_': 'minecraft:block/stone',
    'abyssal_wasteland_': 'abyssalcraft:block/abyssal_stone',
    'dreadlands_': 'abyssalcraft:block/dreadstone',
    'omothol_': 'abyssalcraft:block/omothol_stone',
};
for (const prefix of energyTiers) {
    const tiered = energyContainerHosts[prefix] != null;
    const collectorTextures = {
        '2': 'abyssalcraft:block/energycollector',
        '3': 'abyssalcraft:block/energy_glow',
        particle: 'abyssalcraft:block/monolith_stone',
        side: 'abyssalcraft:block/monolith_stone',
    };
    if (tiered) {
        collectorTextures['4'] = energyContainerHosts[prefix];
        collectorTextures['5'] = 'abyssalcraft:block/energy_trim';
    }
    requireModelContract(energyModelName(prefix, 'collector'),
        `abyssalcraft:block/${tiered ? 'tiered_energy_collector' : 'energy_collector'}`,
        collectorTextures, 'minecraft:cutout');
    const containerTextures = {
        '0': 'abyssalcraft:block/monolith_stone',
        '2': 'abyssalcraft:block/energycontainer',
        '3': 'abyssalcraft:block/energy_glow',
    };
    if (energyContainerHosts[prefix]) {
        containerTextures['4'] = 'abyssalcraft:block/energy_trim';
        containerTextures['5'] = energyContainerHosts[prefix];
    }
    requireModelContract(energyModelName(prefix, 'container'),
        `abyssalcraft:block/${prefix ? 'tiered_energy_container' : 'energy_container'}`,
        containerTextures, 'minecraft:cutout');
    if (tiered) {
        const pedestal = energyModelName(prefix, 'pedestal');
        requireModelContract(pedestal, 'abyssalcraft:block/tiered_energy_pedestal', {
            '2': energyContainerHosts[prefix],
        });
        requireModelContract(`${pedestal}_tilted`,
            'abyssalcraft:block/tiered_energy_pedestal_tilted', {
                '2': energyContainerHosts[prefix],
            });
    }
    const relayTextures = {
        '0': 'abyssalcraft:block/monolith_stone',
        '2': 'abyssalcraft:block/energy_glow',
        particle: 'abyssalcraft:block/monolith_stone',
    };
    if (tiered) {
        relayTextures['3'] = 'abyssalcraft:block/energy_trim';
        relayTextures['4'] = energyContainerHosts[prefix];
    }
    requireModelContract(energyModelName(prefix, 'relay'),
        `abyssalcraft:block/${tiered ? 'tiered_energy_relay' : 'energy_relay'}`,
        relayTextures, 'minecraft:cutout');
}
requireElementModelContract('energy_collector', 9, false);
requireElementModelContract('tiered_energy_collector', 12, false);
requireElementModelContract('energy_container', 14);
requireElementModelContract('tiered_energy_container', 26);
requireElementModelContract('energy_relay', 23);
requireElementModelContract('tiered_energy_relay', 26);
requireElementModelContract('energy_depositioner', 9, false);
requireModelContract('energydepositioner', 'abyssalcraft:block/energy_depositioner', {
    '0': 'abyssalcraft:block/monolith_stone', '1': 'abyssalcraft:block/shoggoth_ooze',
    '2': 'abyssalcraft:block/energydepositioner', particle: 'abyssalcraft:block/monolith_stone',
}, 'minecraft:cutout');

const ghoulHead = readJson(requireAsset('models/block/ghoul_head.json', 'Ghoul head geometry'));
if (ghoulHead.parent === 'minecraft:block/cube_all' || ghoulHead.elements?.length !== 7) {
    missing.push(`Ghoul head must use the seven-element legacy head geometry`);
}
if (ghoulHead.render_type !== 'minecraft:cutout'
    || ghoulHead.elements?.some(element => element.shade !== false)) {
    missing.push('Ghoul head must use cutout rendering with unshaded legacy elements');
}
for (const [id, skin] of Object.entries({
    dghead: 'depths_ghoul', phead: 'depths_ghoul_pete',
    whead: 'depths_ghoul_wilson', ohead: 'depths_ghoul_orange',
})) {
    requireModelContract(id, 'abyssalcraft:block/ghoul_head', {
        all: `abyssalcraft:block/ghoul_head/${skin}`,
    });
    const texture = requireAsset(`textures/block/ghoul_head/${skin}.png`, `${id} square head texture`);
    const dimensions = texture && pngDimensions(texture);
    if (!dimensions || dimensions.width !== 128 || dimensions.height !== 128) {
        missing.push(`${id} head texture must use a 128x128 square block atlas`);
    }
    const item = readJson(requireAsset(`models/item/${id}.json`, `${id} item model`));
    if (item.parent !== 'minecraft:item/generated' || item.textures?.layer0 !== `abyssalcraft:block/${id}`) {
        missing.push(`${id} must retain its legacy two-dimensional inventory model`);
    }
}
if (JSON.stringify(ghoulHead.elements?.[0]?.faces?.north?.uv) !== JSON.stringify([1.125, 1.125, 2.25, 2.25])) {
    missing.push('Ghoul head UVs do not match the square legacy-skin atlas');
}

for (const [texture, expectedFrames] of [
    ['item/essence_of_the_gatekeeper.png', null],
    ['item/transmutation_gem/gem.png', [0, 1, 2, 3, 4, 5, 6, 7, 6, 5, 4, 3, 2, 1]],
    ['item/transmutation_gem/container.png',
        [0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 7, 7, 7, 7, 7, 7, 7, 6, 5, 4, 3, 2, 1]],
]) {
    const png = requireAsset(`textures/${texture}`, `${texture} animation`);
    const meta = requireAsset(`textures/${texture}.mcmeta`, `${texture} animation metadata`);
    if (!png || !meta) continue;
    const dimensions = pngDimensions(png);
    const animation = readJson(meta).animation;
    if (!dimensions || dimensions.height !== dimensions.width * 8 || !animation) {
        missing.push(`${texture} must be an eight-frame vertical animation`);
    }
    if (expectedFrames && JSON.stringify(animation.frames) !== JSON.stringify(expectedFrames)) {
        missing.push(`${texture} legacy animation frame order changed`);
    }
}

const bladeModel = readJson(requireAsset('models/item/dreadium_katana_blade.json', 'Dreadium Katana blade'));
if (bladeModel.textures?.layer0 !== 'abyssalcraft:item/dreadium_katana_blade') {
    missing.push('Dreadium Katana blade does not use the legacy dreadblade texture');
}
const dreadKeyModel = readJson(requireAsset('models/item/dreadkey.json', 'Dread-plagued Gateway Key'));
if (dreadKeyModel.parent !== 'minecraft:item/generated'
    || dreadKeyModel.textures?.layer0 !== 'abyssalcraft:item/dreadkey') {
    missing.push('Dread-plagued Gateway Key does not use its legacy dk texture');
}
for (const tier of ['small', 'medium', 'large', 'huge']) {
    const id = `crystalbag_${tier}`;
    const model = readJson(requireAsset(`models/item/${id}.json`, `${id} visual contract`));
    if (model.parent !== 'minecraft:item/generated'
        || model.textures?.layer0 !== `abyssalcraft:item/${id}`) {
        missing.push(`${id} does not use its dedicated legacy texture`);
    }
}
const crateModel = readJson(requireAsset('models/block/crate.json', 'Crate visual contract'));
if (crateModel.parent !== 'minecraft:block/cube_all'
    || crateModel.textures?.all !== 'abyssalcraft:block/crate') {
    missing.push('Crate must use the dedicated legacy crate texture');
}
for (const [id, elements, textures] of [
    ['ritual_altar', 18, {
        '0': 'minecraft:block/cobblestone', '1': 'abyssalcraft:block/ritual_altar/parts',
        '3': 'abyssalcraft:block/ritual_altar/cloth', '4': 'abyssalcraft:block/monolith_stone',
    }],
    ['ritual_pedestal', 19, {
        '0': 'minecraft:block/cobblestone', '1': 'abyssalcraft:block/ritual_pedestal/overlay_0',
        '2': 'abyssalcraft:block/ritual_pedestal/glyphs',
    }],
]) {
    const model = readJson(requireAsset(`models/block/${id}.json`, `${id} legacy geometry`));
    if (model.parent !== 'abyssalcraft:block/legacy_custom_model'
        || model.render_type !== 'minecraft:cutout' || model.elements?.length !== elements) {
        missing.push(`${id} legacy geometry contract changed`);
    }
    for (const [slot, texture] of Object.entries(textures)) {
        if (model.textures?.[slot] !== texture) missing.push(`${id} texture ${slot} changed`);
    }
}
const tombstoneMaterials = {
    stone: 'minecraft:block/cobblestone',
    abyssal_stone: 'abyssalcraft:block/abyssal_cobblestone',
    coralium_stone: 'abyssalcraft:block/coralium_cobblestone',
    darkstone: 'abyssalcraft:block/darkstone_cobblestone',
    dreadstone: 'abyssalcraft:block/dreadstone_cobblestone',
    elysian_stone: 'abyssalcraft:block/elysian_cobblestone',
    ethaxium: 'abyssalcraft:block/ethaxium_legacy_brick',
    monolith_stone: 'abyssalcraft:block/shoggoth_ooze',
    omothol_stone: 'abyssalcraft:block/dark_ethaxium_brick',
};
for (const [id, texture] of Object.entries(tombstoneMaterials)) {
    const model = readJson(requireAsset(`models/block/tombstone_${id}.json`, `${id} tombstone material`));
    if (model.textures?.['1'] !== texture) missing.push(`tombstone_${id} secondary material changed`);
}
const spiritAltarModel = readJson(requireAsset('models/block/spirit_altar.json', 'Spirit Altar fidelity'));
if (spiritAltarModel.elements?.length !== 7
    || spiritAltarModel.textures?.['2'] !== 'abyssalcraft:block/chiseled_darkstone_brick'
    || spiritAltarModel.textures?.['3'] !== 'abyssalcraft:block/darkstone_cobblestone'
    || spiritAltarModel.textures?.['4'] !== 'minecraft:block/gold_block') {
    missing.push('Spirit Altar legacy material/geometry contract changed');
}
for (const [id, elements, tiered] of [
    ['energy_pedestal', 10, false],
    ['energy_pedestal_tilted', 10, false],
    ['tiered_energy_pedestal', 16, true],
    ['tiered_energy_pedestal_tilted', 16, true],
]) {
    const model = readJson(requireAsset(`models/block/${id}.json`, `${id} legacy geometry`));
    if (model.parent !== 'abyssalcraft:block/legacy_custom_model'
        || model.elements?.length !== elements
        || model.textures?.['0'] !== 'abyssalcraft:block/monolith_stone'
        || model.textures?.['1'] !== 'abyssalcraft:block/energy_pedestal/overlay'
        || (tiered
            ? model.textures?.['3'] !== 'abyssalcraft:block/energy_trim'
                || model.textures?.['4'] !== 'abyssalcraft:block/energy_glow'
            : model.textures?.['2'] !== 'abyssalcraft:block/energy_glow')) {
        missing.push(`${id} legacy geometry/material contract changed`);
    }
}
for (const id of [
    'energypedestal', 'overworld_energy_pedestal', 'abyssal_wasteland_energy_pedestal',
    'dreadlands_energy_pedestal', 'omothol_energy_pedestal',
]) {
    const state = readJson(requireAsset(`blockstates/${id}.json`, `${id} tilt states`));
    if (!state.variants?.['tilted=false'] || !state.variants?.['tilted=true']) {
        missing.push(`${id} lost its legacy tilted visual state`);
    }
}
const dreadlandsGrassItem = readJson(requireAsset(
    'models/item/dreadlands_grass.json', 'Dreadlands Grass inventory texture'));
if (dreadlandsGrassItem.textures?.top !== 'abyssalcraft:block/dreadlands_grass_top'
    || dreadlandsGrassItem.textures?.overlay !== 'abyssalcraft:block/dreadlands_grass_side') {
    missing.push('Dreadlands Grass item does not use the legacy inventory top/side textures');
}
const dreadlandsGrassState = readJson(requireAsset(
    'blockstates/dreadlands_grass.json', 'Dreadlands Grass snowy variants'));
const dreadlandsGrassNormal = dreadlandsGrassState.variants?.['snowy=false'];
const dreadlandsGrassSnowy = dreadlandsGrassState.variants?.['snowy=true'];
if (!Array.isArray(dreadlandsGrassNormal) || dreadlandsGrassNormal.length !== 4
    || JSON.stringify(dreadlandsGrassNormal.map(variant => variant.y || 0))
        !== JSON.stringify([0, 90, 180, 270])
    || dreadlandsGrassSnowy?.model !== 'abyssalcraft:block/dreadlands_grass_snowed') {
    missing.push('Dreadlands Grass legacy normal/snowy state variants are missing');
}
const dreadlandsGrassSnowedModel = readJson(requireAsset(
    'models/block/dreadlands_grass_snowed.json', 'snow-covered Dreadlands Grass'));
if (dreadlandsGrassSnowedModel.parent !== 'minecraft:block/cube_bottom_top'
    || dreadlandsGrassSnowedModel.textures?.side
        !== 'abyssalcraft:block/dreadlands_grass_side_snowed'
    || dreadlandsGrassSnowedModel.textures?.top !== 'abyssalcraft:block/dreadlands_grass_top') {
    missing.push('Dreadlands Grass snow-covered model does not use the legacy textures');
}
const darklandsLogModel = readJson(requireAsset(
    'models/block/darklands_oak_log.json', 'Darklands Oak Log overlay'));
if (darklandsLogModel.parent !== 'abyssalcraft:block/darklands_oak_log_layered'
    || darklandsLogModel.textures?.overlay !== 'abyssalcraft:block/darklands_oak_log_overlay') {
    missing.push('Darklands Oak Log lost its legacy bark overlay');
}
for (const id of [
    'ethaxium_bricks', 'chiseled_ethaxium_brick', 'cracked_ethaxium_brick',
    'dark_ethaxium_brick', 'chiseled_dark_ethaxium_brick', 'cracked_dark_ethaxium_brick',
]) {
    const state = readJson(requireAsset(`blockstates/${id}.json`, `${id} weighted variants`));
    const variants = state.variants?.[''];
    if (!Array.isArray(variants) || variants.length !== 3
        || JSON.stringify(variants.map(variant => variant.weight)) !== JSON.stringify([2, 1, 1])) {
        missing.push(`${id} legacy 2:1:1 face variants are missing`);
    }
    for (let variant = 1; variant <= 3; variant++) {
        const model = readJson(requireAsset(`models/block/${id}_${variant}.json`, `${id} face ${variant}`));
        if (!model.textures?.overlay?.endsWith(`faces_${variant}`)) {
            missing.push(`${id} face variant ${variant} uses the wrong overlay`);
        }
    }
}
const normalizedVariantTransforms = variants => variants.map(variant => ({
    x: variant.x || 0,
    y: variant.y || 0,
    uvlock: variant.uvlock || false,
    weight: variant.weight || 1,
}));
for (const [modernId, legacyId] of [
    ['ethaxium_brick', 'ethaxiumbrick'],
    ['dark_ethaxium_brick', 'darkethaxiumbrick'],
]) {
    const slab = readJson(requireAsset(
        `blockstates/${modernId}_slab.json`, `${modernId} slab weighted variants`));
    if (Object.keys(slab.variants || {}).length !== 3
        || Object.values(slab.variants || {}).some(variants =>
            !Array.isArray(variants) || JSON.stringify(variants.map(variant => variant.weight || 1))
                !== JSON.stringify([2, 1, 1]))) {
        missing.push(`${modernId} slab lost its legacy 2:1:1 face variants`);
    }
    const stairs = readJson(requireAsset(
        `blockstates/${modernId}_stairs.json`, `${modernId} stairs weighted variants`));
    const legacyStairs = readJson(path.join(
        LEGACY_ROOT, 'blockstates', `${legacyId}stairs.json`));
    const modernKeys = Object.keys(stairs.variants || {}).sort();
    const legacyKeys = Object.keys(legacyStairs.variants || {}).sort();
    if (modernKeys.length !== 40 || JSON.stringify(modernKeys) !== JSON.stringify(legacyKeys)) {
        missing.push(`${modernId} stairs state set differs from the legacy 40-state set`);
    } else {
        for (const key of modernKeys) {
            if (JSON.stringify(normalizedVariantTransforms(stairs.variants[key]))
                !== JSON.stringify(normalizedVariantTransforms(legacyStairs.variants[key]))) {
                missing.push(`${modernId} stairs legacy transform/weight changed for ${key}`);
            }
        }
    }
}
const darkstoneSlab = readJson(requireAsset(
    'models/block/darkstone_slab.json', 'Darkstone Slab dedicated side texture'));
if (darkstoneSlab.textures?.side !== 'abyssalcraft:block/darkstone_slab_side') {
    missing.push('Darkstone Slab no longer uses its legacy side texture');
}
const ethaxiumFence = readJson(requireAsset(
    'models/block/ethaxium_brick_fence_post.json', 'Ethaxium Fence dedicated texture'));
if (ethaxiumFence.textures?.texture !== 'abyssalcraft:block/ethaxium_legacy_brick') {
    missing.push('Ethaxium Fence no longer uses the legacy eb texture');
}
const brewingState = readJson(requireAsset('blockstates/sequential_brewing_stand.json', 'brewing multipart'));
const brewingPipe = readJson(requireAsset('models/block/brewing_stand_pipe.json', 'brewing pipe'));
if (brewingState.multipart?.length !== 5 || brewingPipe.elements?.length !== 2
    || brewingPipe.textures?.['0'] !== 'minecraft:block/brewing_stand'
    || find('models/block/sequential_brewing_stand.json')) {
    missing.push('Sequential Brewing Stand reverted to a placeholder cube');
}
for (const antidote of ['coralium_antidote', 'dread_antidote']) {
    const model = readJson(requireAsset(`models/item/${antidote}.json`, `${antidote} fill states`));
    const predicates = model.overrides?.map(override => override.predicate?.['abyssalcraft:content']);
    if (JSON.stringify(predicates) !== JSON.stringify([0.2, 0.4, 0.6, 0.8])) {
        missing.push(`${antidote} legacy fill-state models changed`);
    }
}
const cageModel = readJson(requireAsset('models/item/interdimensional_cage.json', 'captured cage state'));
if (cageModel.overrides?.[0]?.predicate?.['abyssalcraft:captured'] !== 1) {
    missing.push('Interdimensional Cage captured-state model is missing');
}
const soulReaperModel = readJson(requireAsset('models/item/soulreaper.json', 'Soul Reaper levels'));
if (soulReaperModel.overrides?.length !== 6) missing.push('Soul Reaper six visual levels are missing');
const spiritTabletModel = readJson(requireAsset('models/item/spirit_tablet.json', 'Spirit Tablet modes'));
const spiritTabletModes = spiritTabletModel.overrides?.map(override => override.predicate?.['abyssalcraft:mode']);
if (JSON.stringify(spiritTabletModes) !== JSON.stringify([0, 0.5, 1])) {
    missing.push('Spirit Tablet three mode glyphs are missing');
}
for (let mode = 0; mode <= 2; mode++) {
    const model = readJson(requireAsset(`models/item/spirit_tablet_${mode}.json`, `Spirit Tablet mode ${mode}`));
    if (model.textures?.layer1 !== `abyssalcraft:item/spirit_tablet/glyph_${mode}`) {
        missing.push(`Spirit Tablet mode ${mode} uses the wrong glyph`);
    }
}
const spiritShardZero = readJson(requireAsset(
    'models/item/spirit_tablet_shard_0.json', 'Spirit Tablet shard zero'));
if (spiritShardZero.textures?.layer0
    !== 'abyssalcraft:item/spirit_tablet/spirit_tablet_shard_0') {
    missing.push('Spirit Tablet shard zero reuses the complete tablet texture');
}
const katanaModel = readJson(requireAsset('models/item/dreadium_katana.json', 'Dreadium Katana OBJ'));
const katanaHilt = readJson(requireAsset('models/item/dreadium_katana_hilt.json', 'Dreadium Katana hilt'));
if (katanaModel.loader !== '__LOADER__:obj'
    || katanaModel.model !== 'abyssalcraft:models/item/dreadkatana.obj'
    || katanaHilt.elements?.length !== 2) {
    missing.push('Dreadium Katana legacy OBJ/hilt geometry is missing');
}
const sealingLockModel = readJson(requireAsset('models/block/sealing_lock.json', 'Sealing Lock geometry'));
if (sealingLockModel.elements?.length !== 3
    || sealingLockModel.textures?.['0'] !== 'abyssalcraft:block/elysian_stone_brick'
    || sealingLockModel.textures?.['1'] !== 'abyssalcraft:block/chiseled_elysian_stone_brick'
    || sealingLockModel.textures?.['3'] !== 'abyssalcraft:block/sealing_lock_misc') {
    missing.push('Sealing Lock reverted from its legacy three-element model');
}
const multiBlockModel = readJson(requireAsset('models/block/multi_block.json', 'Place of Power Core texture'));
if (multiBlockModel.textures?.all !== 'abyssalcraft:block/multi_block') {
    missing.push('Place of Power Core does not use the legacy multiblock texture');
}
const oblivionDeathbomb = readJson(requireAsset(
    'models/block/oblivion_deathbomb.json', 'Oblivion Deathbomb OBJ'));
if (oblivionDeathbomb.loader !== '__LOADER__:obj'
    || oblivionDeathbomb.model !== 'abyssalcraft:models/block/odb.obj') {
    missing.push('Oblivion Deathbomb legacy OBJ model is missing');
}
const primedOdbRenderer = fs.readFileSync(path.join(ROOT,
    'src/main/java/com/shinoow/abyssalcraft/client/render/entity/effect/PrimedODBRenderer.java'), 'utf8');
const miscRenderers = fs.readFileSync(path.join(ROOT,
    'src/main/java/com/shinoow/abyssalcraft/client/render/entity/MiscRenderers.java'), 'utf8');
if (!primedOdbRenderer.includes('TntMinecartRenderer.renderWhiteSolidBlock')
    || !miscRenderers.includes('RitualBlocks.OBLIVION_DEATHBOMB.get().defaultBlockState()')
    || !miscRenderers.includes('RitualBlocks.ODB_CORE.get().defaultBlockState()')) {
    missing.push('Primed ODB entities do not render their faithful block models');
}
for (const [id, modelPath] of [
    ['cudgel', 'abyssalcraft:models/item/cudgel.obj'],
    ['staff_of_the_gatekeeper', 'abyssalcraft:models/item/staff.obj'],
]) {
    const model = readJson(requireAsset(`models/item/${id}.json`, `${id} OBJ`));
    if (model.loader !== '__LOADER__:obj' || model.model !== modelPath) {
        missing.push(`${id} legacy OBJ model is missing`);
    }
}
const eliteGeoModel = fs.readFileSync(path.join(ROOT,
    'src/main/java/com/shinoow/abyssalcraft/client/render/entity/boss/EliteGeoModel.java'), 'utf8');
for (const profession of [
    'remnant_librarian', 'remnant_priest', 'remnant_blacksmith',
    'remnant_butcher', 'remnant_banker', 'remnant_master_blacksmith',
]) {
    if (!eliteGeoModel.includes(`textures/model/remnant/${profession}.png`)) {
        missing.push(`Remnant profession texture is not routed ${profession}`);
    }
}
if (!eliteGeoModel.includes('REMNANT_TEXTURES[remnant.getProfession()]')) {
    missing.push('Remnant renderer ignores its synchronized profession');
}
const eyeRoutes = new Map([
    ['client/render/entity/legacy/AbyssalZombieRenderer.java', ['abyssal_zombie_eyes.png']],
    ['client/render/entity/legacy/CoraliumSquidRenderer.java', ['coraliumsquid_eyes.png']],
    ['client/render/entity/BossRenderers.java', ['boss/dragonboss_eyes.png', 'elite/dragonminion_eyes.png']],
    ['client/render/entity/GhoulShoggothRenderers.java', [
        'depths_ghoul_eyes', 'dreaded_ghoul_eyes', 'ghoul_eyes', 'shadow_ghoul_eyes',
    ]],
    ['client/render/entity/legacy/LegacyRenderers.java', [
        'elite/shadowbeast_eyes.png', 'shadowcreature_eyes.png', 'shadowmonster_eyes.png',
    ]],
]);
for (const [source, routes] of eyeRoutes) {
    const renderer = fs.readFileSync(path.join(
        ROOT, 'src/main/java/com/shinoow/abyssalcraft', source), 'utf8');
    for (const route of routes) {
        if (!renderer.includes(route)) missing.push(`legacy eye texture is not routed ${route}`);
    }
}
const necronomiconItemSource = fs.readFileSync(path.join(ROOT,
    'src/main/java/com/shinoow/abyssalcraft/content/item/book/NecronomiconItem.java'), 'utf8');
const patchouliBookSource = fs.readFileSync(path.join(ROOT,
    'src/main/java/com/shinoow/abyssalcraft/data/gen/PatchouliBookData.java'), 'utf8');
const patchouliActionSource = fs.readFileSync(path.join(ROOT,
    'src/main/java/com/shinoow/abyssalcraft/client/necronomicon/PatchouliActionComponent.java'), 'utf8');
if (!necronomiconItemSource.includes('PatchouliNecronomicon.open(bookType)')) {
    missing.push('Necronomicon items do not route normal use through Patchouli');
}
for (const book of [
    '"necronomicon"', '"abyssal_wasteland_necronomicon"', '"dreadlands_necronomicon"',
    '"omothol_necronomicon"', '"abyssalnomicon"',
]) {
    if (!patchouliBookSource.includes(book)) {
        missing.push(`Patchouli Necronomicon edition is not generated ${book}`);
    }
}
if (!patchouliBookSource.includes('dont_generate_book')
    || !patchouliBookSource.includes('custom_book_item')
    || !patchouliBookSource.includes('NecronomiconPageManifest.isAvailableForBook(page, tier)')) {
    missing.push('Patchouli Necronomicons lost their custom-item or cumulative tier contract');
}
if (!patchouliActionSource.includes('lookup.apply(IVariable.wrap(action')
    || !patchouliActionSource.includes('new NecronomiconPageActionMessage')
    || !patchouliActionSource.includes('new OpenSpellbookMessage')) {
    missing.push('Patchouli Necronomicon action component is incomplete');
}
const legacyJeiBackgrounds = fs.readFileSync(path.join(ROOT,
    'src/main/java/com/shinoow/abyssalcraft/integration/jei/LegacyJeiBackgrounds.java'), 'utf8');
for (const texture of [
    'crystallizer_nei.png', 'materializer_nei.png', 'ritual_nei.png',
    'transformation_ritual_jei.png', 'transmutator_nei.png',
]) {
    if (!legacyJeiBackgrounds.includes(`textures/gui/container/${texture}`)) {
        missing.push(`legacy JEI background is not routed ${texture}`);
    }
}
const necronomiconModel = readJson(requireAsset('models/item/necronomicon_book.json', 'Necronomicon geometry'));
if (necronomiconModel.ambientocclusion !== false) {
    missing.push('Necronomicon thin geometry must disable ambient occlusion');
}
const bowModel = readJson(requireAsset('models/item/coralium_longbow.json', 'Coralium Longbow model'));
if (bowModel.parent !== 'minecraft:item/bow' || bowModel.overrides?.length !== 3
    || bowModel.overrides[0]?.predicate?.pulling !== 1
    || bowModel.overrides[1]?.predicate?.pull !== 0.65
    || bowModel.overrides[2]?.predicate?.pull !== 0.9) {
    missing.push('Coralium Longbow must expose the three legacy pulling models');
}
const itemProperties = fs.readFileSync(path.join(ROOT,
    'src/main/java/com/shinoow/abyssalcraft/client/ClientItemProperties.java'), 'utf8');
if (!itemProperties.includes('ACRef.vanilla("pull")')
    || !itemProperties.includes('ACRef.vanilla("pulling")')) {
    missing.push('Coralium Longbow vanilla pull/pulling item properties are not registered');
}

const javaFiles = walk(path.join(ROOT, 'src/main/java/com/shinoow/abyssalcraft/client'))
    .filter(file => file.endsWith('.java'));
const directPattern = /ACRef\.id\("(textures\/[^"]+|font\/[^"]+)"\)/g;
let directReferences = 0;
for (const file of javaFiles) {
    const source = fs.readFileSync(file, 'utf8');
    for (const match of source.matchAll(directPattern)) {
        directReferences++;
        const reference = match[1];
        if (reference.startsWith('textures/')) requireAsset(reference, relative(file, ROOT));
        else requireAsset(`${reference}.json`, relative(file, ROOT));
    }
}

const clientSetup = fs.readFileSync(path.join(ROOT,
    'src/main/java/com/shinoow/abyssalcraft/client/ACClientSetup.java'), 'utf8');
const screens = [...clientSetup.matchAll(/ClientScreenCompat\.queue\(/g)].length;
if (screens !== 13 || !clientSetup.includes('ClientScreenCompat.queuedCount() != 13')) {
    missing.push(`screen registration coverage expected=13 actual=${screens}`);
}

const entityCatalog = fs.readFileSync(path.join(ROOT,
    'src/main/java/com/shinoow/abyssalcraft/content/entity/legacy/EntityCatalogInvariant.java'), 'utf8');
const entityMatch = entityCatalog.match(/CONTENT_ENTITIES = Set\.of\(([\s\S]*?)\n    \);/);
const entities = entityMatch ? [...entityMatch[1].matchAll(/"[a-z0-9_]+"/g)].length : 0;
const rendererRelay = fs.readFileSync(path.join(ROOT,
    'src/main/java/com/shinoow/abyssalcraft/client/render/ACEntityRenderers.java'), 'utf8');
if (entities !== 63 || rendererRelay.includes('ACPlaceholderRenderer')
    || !rendererRelay.includes('Missing faithful entity renderer')
    || !rendererRelay.includes('registeredEntities.size() != expectedEntities')) {
    missing.push(`faithful entity renderer coverage expected=63 catalog=${entities}`);
}
if (fs.existsSync(path.join(ROOT,
    'src/main/java/com/shinoow/abyssalcraft/client/render/entity/ACPlaceholderRenderer.java'))) {
    missing.push('placeholder entity renderer remains in production');
}
const modelLayers = [...fs.readFileSync(path.join(ROOT,
    'src/main/java/com/shinoow/abyssalcraft/registry/ModModelLayers.java'), 'utf8')
    .matchAll(/public static final ModelLayerLocation /g)].length;
if (modelLayers < 20 || !rendererRelay.includes('Duplicate model layer')) {
    missing.push(`model layer coverage declarations=${modelLayers}`);
}

const particleFiles = walk(path.join(ASSET_ROOTS[0], 'particles')).filter(file => file.endsWith('.json'));
for (const file of particleFiles) {
    for (const texture of readJson(file).textures || []) {
        const [namespace, name] = split(texture, 'minecraft');
        if (namespace === 'abyssalcraft') {
            requireAsset(`textures/particle/${name}.png`, relative(file, ROOT));
        }
    }
}

const soundsFile = requireAsset('sounds.json', 'registered sound catalog');
const sounds = soundsFile ? readJson(soundsFile) : {};
const soundFiles = new Set();
for (const [event, definition] of Object.entries(sounds)) {
    for (const entry of definition.sounds || []) {
        const reference = typeof entry === 'string' ? entry : entry.name;
        const [namespace, name] = split(reference, 'minecraft');
        if (namespace === 'abyssalcraft') {
            soundFiles.add(name);
            requireAsset(`sounds/${name}.ogg`, `sounds.json:${event}`);
        }
    }
}

const pngFiles = ASSET_ROOTS.flatMap(root => walk(path.join(root, 'textures')))
    .filter(file => file.endsWith('.png'));
for (const file of pngFiles) {
    if (!decodablePng(file)) {
        missing.push(`undecodable PNG ${relative(file, ROOT)}`);
    }
}

const clusterTexture = requireAsset('textures/block/crystal_cluster.png', 'animated crystal cluster');
const clusterMetadata = requireAsset('textures/block/crystal_cluster.png.mcmeta', 'animated crystal cluster');
if (clusterTexture) {
    const dimensions = pngDimensions(clusterTexture);
    if (!dimensions || dimensions.width !== 16 || dimensions.height !== 256) {
        missing.push(`crystal cluster texture dimensions expected=16x256 actual=${dimensions && `${dimensions.width}x${dimensions.height}`}`);
    }
}
if (clusterMetadata) {
    const animation = readJson(clusterMetadata).animation;
    if (!animation || !Number.isInteger(animation.frametime) || animation.frametime < 1) {
        missing.push('crystal cluster animation metadata has no positive integer frametime');
    }
}
const clusterModel = readJson(requireAsset('models/block/crystal_cluster.json', 'crystal cluster cutout model'));
if (clusterModel.render_type !== 'minecraft:cutout') {
    missing.push('crystal cluster model must use the cross-version cutout render type');
}
for (const [bucket, fluid] of [
    ['liquid_coralium_bucket', 'liquid_coralium_still'],
    ['liquid_antimatter_bucket', 'liquid_antimatter_still'],
]) {
    const model = readJson(requireAsset(`models/item/${bucket}.json`, `${bucket} layered model`));
    if (model.parent !== 'minecraft:item/generated'
        || model.textures?.layer0 !== `abyssalcraft:block/${fluid}`
        || model.textures?.layer1 !== 'minecraft:item/bucket') {
        missing.push(`${bucket} must render the fluid behind the vanilla empty-bucket shell`);
    }
}

const legacyPotionAtlas = readPng(path.join(LEGACY_ROOT, 'textures/misc/potionfx.png'));
for (const [effect, icon] of [
    ['coralium_plague', 0],
    ['dread_plague', 1],
    ['antimatter', 2],
    ['coralium_antidote', 3],
    ['dread_antidote', 3],
]) {
    const expected = crop(legacyPotionAtlas, icon * 18, 198, 18, 18);
    const actualFile = requireAsset(`textures/mob_effect/${effect}.png`, `${effect} legacy effect icon`);
    if (!actualFile) continue;
    const actual = readPng(actualFile);
    if (actual.width !== 18 || actual.height !== 18 || !actual.pixels.equals(expected.pixels)) {
        missing.push(`${effect} does not match legacy potionfx status icon ${icon}`);
    }
}

const legacyTextureRoot = path.join(LEGACY_ROOT, 'textures');
const legacyTextureFiles = walk(legacyTextureRoot).filter(file => file.endsWith('.png')).sort();
const legacyTextureHashes = new Set(legacyTextureFiles.map(sha256));
let authoredTextureCount = 0;
for (const file of pngFiles) {
    if (legacyTextureHashes.has(sha256(file))) continue;
    const root = ASSET_ROOTS.find(candidate => file.startsWith(path.join(candidate, 'textures')));
    const name = root ? relative(file, path.join(root, 'textures')) : relative(file, ROOT);
    if (!AUTHORED_TEXTURES.has(name)) missing.push(`unapproved non-legacy texture ${name}`);
    else authoredTextureCount++;
}
for (const name of AUTHORED_TEXTURES) {
    if (!find(`textures/${name}`)) missing.push(`stale authored texture allowlist ${name}`);
}
if (authoredTextureCount !== AUTHORED_TEXTURES.size) {
    missing.push(`authored texture coverage expected=${AUTHORED_TEXTURES.size} actual=${authoredTextureCount}`);
}
for (const [target, sources] of [
    ['chiseled_coralium_stone_brick.png', [
        'coralium_bricks/bricks_base.png', 'coralium_bricks/sigil.png']],
    ['cracked_coralium_stone_brick.png', [
        'coralium_bricks/bricks_base.png', 'coralium_bricks/cracks.png']],
    ['dark_ethaxium_brick.png', [
        'ethaxium_bricks/dark_bricks_base.png', 'ethaxium_bricks/dark_faces_1.png']],
    ...[1, 2, 3].flatMap(variant => [
        [`ethaxium_bricks/composite_${variant}.png`, [
            'ethaxium_bricks/bricks_base.png', `ethaxium_bricks/faces_${variant}.png`]],
        [`ethaxium_bricks/dark_composite_${variant}.png`, [
            'ethaxium_bricks/dark_bricks_base.png', `ethaxium_bricks/dark_faces_${variant}.png`]],
    ]),
]) {
    const expected = composite(sources.map(source =>
        readPng(path.join(legacyTextureRoot, 'blocks', source))));
    const actual = readPng(path.join(ASSET_ROOTS[0], 'textures/block', target));
    if (expected.width !== actual.width || expected.height !== actual.height
        || !expected.pixels.equals(actual.pixels)) {
        missing.push(`legacy layered-brick composition mismatch ${target}`);
    }
}
const modernTextureByHash = new Map();
for (const file of pngFiles) {
    const hash = sha256(file);
    const targets = modernTextureByHash.get(hash) || [];
    targets.push(relative(file, ROOT));
    modernTextureByHash.set(hash, targets.sort());
}
if (mode === '--write') {
    const entries = legacyTextureFiles.map(file => {
        const source = relative(file, legacyTextureRoot);
        const hash = sha256(file);
        return { source, sha256: hash, ...legacyTextureDisposition(source, modernTextureByHash, hash) };
    });
    fs.mkdirSync(path.dirname(LEGACY_TEXTURE_LEDGER), { recursive: true });
    fs.writeFileSync(LEGACY_TEXTURE_LEDGER, `${JSON.stringify({
        schema: 1,
        task: 'T9.1b / PK-1b',
        algorithm: 'SHA-256',
        sourceRoot: relative(legacyTextureRoot, ROOT),
        entries,
    }, null, 2)}\n`);
}
const legacyTextureEntries = fs.existsSync(LEGACY_TEXTURE_LEDGER)
    ? readJson(LEGACY_TEXTURE_LEDGER).entries || [] : [];
const legacyTextureBySource = new Map();
const legacyTextureCounts = { MIGRATED: 0, REPLACED: 0, RETIRED: 0, BLOCKED: 0 };
for (const entry of legacyTextureEntries) {
    if (!entry || typeof entry.source !== 'string' || legacyTextureBySource.has(entry.source)) {
        missing.push(`legacy texture duplicate/invalid source ${entry && entry.source}`);
        continue;
    }
    legacyTextureBySource.set(entry.source, entry);
}
for (const file of legacyTextureFiles) {
    const source = relative(file, legacyTextureRoot);
    const entry = legacyTextureBySource.get(source);
    if (!decodablePng(file)) missing.push(`undecodable legacy PNG ${source}`);
    if (!entry) {
        missing.push(`legacy texture unclassified ${source}`);
        continue;
    }
    if (entry.sha256 !== sha256(file)) missing.push(`legacy texture hash mismatch ${source}`);
    if (!Object.hasOwn(legacyTextureCounts, entry.status)) {
        missing.push(`legacy texture invalid status ${source}:${entry.status}`);
        continue;
    }
    legacyTextureCounts[entry.status]++;
    if (!entry.owner || !entry.reason) missing.push(`legacy texture missing owner/reason ${source}`);
    if (!Array.isArray(entry.targets)) missing.push(`legacy texture targets must be an array ${source}`);
    let matchingTargetHash = false;
    for (const target of entry.targets || []) {
        const targetFile = path.join(ROOT, target);
        if (!fs.existsSync(targetFile)) missing.push(`legacy texture target missing ${source} -> ${target}`);
        else if (target.endsWith('.png')) {
            if (!decodablePng(targetFile)) missing.push(`legacy texture target undecodable ${source} -> ${target}`);
            if (sha256(targetFile) === entry.sha256) matchingTargetHash = true;
        }
    }
    if (entry.status === 'MIGRATED' && !matchingTargetHash) {
        missing.push(`legacy texture migrated without matching target hash ${source}`);
    }
    if ((entry.status === 'RETIRED') !== RETIRED_LEGACY_TEXTURES.has(source)) {
        missing.push(`legacy texture retirement allowlist mismatch ${source}:${entry.status}`);
    }
    if (entry.status !== 'RETIRED' && (!entry.targets || entry.targets.length === 0)) {
        missing.push(`legacy texture active status without target ${source}`);
    }
}
for (const source of legacyTextureBySource.keys()) {
    if (!fs.existsSync(path.join(legacyTextureRoot, source))) missing.push(`legacy texture stale ledger entry ${source}`);
}
if (legacyTextureFiles.length !== 644 || legacyTextureEntries.length !== 644) {
    missing.push(`legacy texture cardinality source=${legacyTextureFiles.length} ledger=${legacyTextureEntries.length}`);
}
if (legacyTextureCounts.BLOCKED !== 0) missing.push(`legacy texture blocked=${legacyTextureCounts.BLOCKED}`);

const languageFiles = walk(path.join(ASSET_ROOTS[0], 'lang')).filter(file => file.endsWith('.json'));
const languageData = new Map(languageFiles.map(file => [path.basename(file, '.json'), readJson(file)]));
const languageKeys = new Map([...languageData].map(([language, entries]) => [language, Object.keys(entries)]));
const englishKeys = new Set(languageKeys.get('en_us') || []);
const languageMissing = Object.fromEntries([...languageKeys].map(([language, keys]) =>
    [language, [...englishKeys].filter(key => !new Set(keys).has(key)).length]));
const englishEntries = languageData.get('en_us') || {};
const contentNamePattern = /^(?:block|item|entity|fluid)\.abyssalcraft\./;
const placeholderSignature = value => (String(value).match(/%(?:\d+\$)?[sdif]/g) || []).sort().join('|');
const expectedLanguageSet = new Set(LOCALIZATION_LANGUAGES);
for (const language of LOCALIZATION_LANGUAGES) {
    if (!languageData.has(language)) missing.push(`localization language missing ${language}`);
}
for (const language of languageData.keys()) {
    if (!expectedLanguageSet.has(language)) missing.push(`unexpected localization language ${language}`);
}
for (const [language, entries] of languageData) {
    const keys = new Set(Object.keys(entries));
    for (const key of englishKeys) {
        if (!keys.has(key)) missing.push(`translation key missing ${language}:${key}`);
    }
    for (const key of keys) {
        if (!englishKeys.has(key)) missing.push(`translation key not present in en_us ${language}:${key}`);
    }
    for (const [key, englishValue] of Object.entries(englishEntries)) {
        const value = entries[key];
        if (placeholderSignature(value) !== placeholderSignature(englishValue)) {
            missing.push(`translation placeholder mismatch ${language}:${key}`);
        }
        if (language !== 'en_us' && contentNamePattern.test(key) && value === englishValue
            && /[A-Za-z]{3}/.test(value) && !IDENTICAL_TO_ENGLISH[language]?.has(key)) {
            missing.push(`unexpected English content name ${language}:${key}`);
        }
    }
}
for (const row of ENTITY_NAME_ROWS) {
    const key = `entity.abyssalcraft.${row[0]}`;
    for (let index = 0; index < LOCALIZATION_LANGUAGES.length; index++) {
        const language = LOCALIZATION_LANGUAGES[index];
        if (languageData.get(language)?.[key] !== row[index + 1]) {
            missing.push(`canonical entity name mismatch ${language}:${key}`);
        }
    }
}
for (const row of UI_TEXT_ROWS) {
    const key = row[0];
    for (let index = 0; index < LOCALIZATION_LANGUAGES.length; index++) {
        const language = LOCALIZATION_LANGUAGES[index];
        if (languageData.get(language)?.[key] !== row[index + 1]) {
            missing.push(`canonical UI text mismatch ${language}:${key}`);
        }
    }
}
for (const [language, overrides] of Object.entries(DISPLAY_NAME_OVERRIDES)) {
    const entries = languageData.get(language) || {};
    for (const [key, expected] of Object.entries(overrides)) {
        if (entries[key] !== expected) missing.push(`canonical display name mismatch ${language}:${key}`);
    }
}
for (const [language, replacements] of Object.entries(TERM_REPLACEMENTS)) {
    const entries = languageData.get(language) || {};
    for (const [forbidden] of replacements) {
        for (const [key, value] of Object.entries(entries)) {
            if (typeof value === 'string' && value.includes(forbidden)) {
                missing.push(`non-canonical term ${language}:${key}:${forbidden}`);
            }
        }
    }
    const dreadlandsTerm = language === 'zh_cn' ? '恐惧之地' : '恐懼之地';
    for (const [key, value] of Object.entries(entries)) {
        if (contentNamePattern.test(key) && key.includes('.darklands') && value.includes(dreadlandsTerm)) {
            missing.push(`Darklands/Dreadlands name collision ${language}:${key}`);
        }
    }
}
for (const [language, families] of Object.entries(ID_FAMILY_TERMS)) {
    const entries = languageData.get(language) || {};
    for (const [family, expectedTerm] of Object.entries(families)) {
        for (const [key, value] of Object.entries(entries)) {
            if (!contentNamePattern.test(key) || !key.toLowerCase().includes(family)
                || ID_FAMILY_EXCEPTIONS.has(key)) continue;
            if (!value.includes(expectedTerm)) {
                missing.push(`localized name family mismatch ${language}:${family}:${key}`);
            }
        }
    }
}
const requiredEntityScripts = {
    ja_jp: /[\u3040-\u30ff\u3400-\u9fff]/,
    ko_kr: /[\uac00-\ud7af]/,
    ru_ru: /[\u0400-\u04ff]/,
    zh_cn: /[\u3400-\u9fff]/,
    zh_tw: /[\u3400-\u9fff]/,
};
for (const [language, scriptPattern] of Object.entries(requiredEntityScripts)) {
    for (const [key, value] of Object.entries(languageData.get(language) || {})) {
        if (key.startsWith('entity.abyssalcraft.') && !scriptPattern.test(value)) {
            missing.push(`entity name uses wrong script ${language}:${key}`);
        }
    }
}
const spawnEggTemplates = {
    en_us: base => `${base} Spawn Egg`,
    es_es: base => `Huevo generador de ${base}`,
    fr_fr: base => `Œuf d’apparition de ${base}`,
    ja_jp: base => `${base}のスポーンエッグ`,
    ko_kr: base => `${base} 생성 알`,
    ru_ru: base => `Яйцо призыва: ${base}`,
    zh_cn: base => `${base}刷怪蛋`,
    zh_tw: base => `${base}生成蛋`,
};
const localizedSpawnEggKeys = Object.keys(englishEntries)
    .filter(key => /^item\.abyssalcraft\..+_spawn_egg$/.test(key));
for (const [language, entries] of languageData) {
    for (const key of localizedSpawnEggKeys) {
        const id = key.slice('item.abyssalcraft.'.length, -'_spawn_egg'.length);
        const entityName = entries[`entity.abyssalcraft.${id}`];
        if (entityName && entries[key] !== spawnEggTemplates[language](entityName)) {
            missing.push(`spawn egg name mismatch ${language}:${key}`);
        }
    }
    for (const [sourceKey, targetKey] of MIRRORED_NAME_PAIRS) {
        if (entries[sourceKey] !== entries[targetKey]) {
            missing.push(`mirrored display name mismatch ${language}:${sourceKey}:${targetKey}`);
        }
    }
}
for (const [key, value] of Object.entries(englishEntries)) {
    if (contentNamePattern.test(key) && /\s(?:Of|The)\s/.test(value)) {
        missing.push(`English display-name article capitalization ${key}`);
    }
}
const requiredJeiKeys = new Set([
    'jei.abyssalcraft.anvil_forging', 'jei.abyssalcraft.anvil_price',
    'jei.abyssalcraft.crystallizer_fuel', 'jei.abyssalcraft.transmutator_fuel',
    'jei.abyssalcraft.rending', 'jei.abyssalcraft.infusion_ritual', 'jei.abyssalcraft.ritual',
    'jei.abyssalcraft.creation_ritual', 'jei.abyssalcraft.transformation_ritual', 'jei.abyssalcraft.spell',
    'jei.abyssalcraft.fuel_time', 'jei.abyssalcraft.ritual_energy',
    'jei.abyssalcraft.ritual_book_type', 'jei.abyssalcraft.ritual_dimension',
    'jei.abyssalcraft.rending_energy', 'jei.abyssalcraft.essence_type',
    'jei.abyssalcraft.spell_energy', 'jei.abyssalcraft.scroll_type',
    ...[
        'infusion', 'creation', 'transformation', 'portal', 'summon', 'respawn_jzahar',
        'breeding', 'dread_spawn', 'potion_aoe', 'resurrection', 'cleansing', 'corruption',
        'infesting', 'curing', 'purging', 'mass_enchanting', 'weather', 'house',
    ].map(kind => `jei.abyssalcraft.ritual_kind.${kind}`),
    ...['entity', 'entity_or_self', 'block', 'self']
        .map(target => `jei.abyssalcraft.spell_target.${target}`),
]);
for (const [language, keys] of languageKeys) {
    const keySet = new Set(keys);
    for (const key of requiredJeiKeys) {
        if (!keySet.has(key)) missing.push(`JEI translation missing ${language}:${key}`);
    }
}
const necronomiconNameKeys = [
    'item.abyssalcraft.abyssal_wasteland_necronomicon',
    'item.abyssalcraft.dreadlands_necronomicon',
    'item.abyssalcraft.omothol_necronomicon',
];
const dimensionTitlePairs = [
    ['dimension.abyssalcraft.abyssal_wasteland', 'gui.abyssalcraft.necronomicon.abyssal_wasteland.title'],
    ['dimension.abyssalcraft.dreadlands', 'gui.abyssalcraft.necronomicon.dreadlands.title'],
    ['dimension.abyssalcraft.omothol', 'gui.abyssalcraft.necronomicon.omothol.title'],
    ['dimension.abyssalcraft.dark_realm', 'gui.abyssalcraft.necronomicon.dark_realm.title'],
];
for (const [language, entries] of languageData) {
    const names = necronomiconNameKeys.map(key => entries[key]);
    if (names.some(name => typeof name !== 'string') || new Set(names).size !== names.length) {
        missing.push(`advanced Necronomicon names are not distinct in ${language}`);
    }
    for (const [dimensionKey, titleKey] of dimensionTitlePairs) {
        if (typeof entries[dimensionKey] !== 'string' || entries[dimensionKey] === dimensionKey
            || entries[dimensionKey] !== entries[titleKey]) {
            missing.push(`dimension translation mismatch ${language}:${dimensionKey}`);
        }
    }
}

const legacyByHash = new Map(walk(LEGACY_ROOT).filter(file => fs.statSync(file).isFile()).map(file =>
    [crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex'), relative(file, LEGACY_ROOT)]));
const ledgerEntries = {};
for (const root of ASSET_ROOTS) {
    for (const file of walk(root).sort()) {
        const name = relative(file, root);
        const sha256 = crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');
        const legacy = legacyByHash.get(sha256);
        ledgerEntries[name] = {
            sha256,
            source: root.includes('generated') ? 'datagen' : legacy ? `legacy:${legacy}` : 'ported-or-authored',
        };
    }
}
const ledger = `${JSON.stringify({
    algorithm: 'SHA-256',
    legacyRoot: relative(LEGACY_ROOT, ROOT),
    entries: ledgerEntries,
}, null, 2)}\n`;
if (mode === '--check') {
    if (!fs.existsSync(LEDGER)) missing.push(`asset ledger missing ${relative(LEDGER, ROOT)}`);
    else if (fs.readFileSync(LEDGER, 'utf8').replace(/\r\n/g, '\n') !== ledger) {
        missing.push(`asset ledger stale ${relative(LEDGER, ROOT)}; run without --check to update`);
    }
} else {
    fs.mkdirSync(path.dirname(LEDGER), { recursive: true });
    fs.writeFileSync(LEDGER, ledger);
}

if (missing.length) {
    console.error(missing.join('\n'));
    throw new Error(`RR-ASSET missing=${missing.length}`);
}

console.log(`RR_ASSET_AUDIT_OK missing=0 blockstates=${blockstates.length} itemModels=${itemModels.length}`
    + ` models=${models.size} textures=${pngFiles.length} referencedTextures=${textures.size}`
    + ` customItemDisplays=${customItemModels}`
    + ` directRefs=${directReferences} particles=${particleFiles.length} sounds=${Object.keys(sounds).length}`
    + ` ogg=${soundFiles.size} screens=${screens} entities=${entities} modelLayers=${modelLayers}`
    + ` fonts=1 languages=${languageFiles.length} ledger=${Object.keys(ledgerEntries).length}`);
console.log(`RR_ASSET_LEDGER_MODE mode=${mode === '--check' ? 'check' : 'write'}`);
console.log(`RR_LEGACY_TEXTURE_AUDIT_OK source=${legacyTextureFiles.length}`
    + ` migrated=${legacyTextureCounts.MIGRATED} replaced=${legacyTextureCounts.REPLACED}`
    + ` retired=${legacyTextureCounts.RETIRED} blocked=${legacyTextureCounts.BLOCKED}`);
console.log(`RR_ASSET_LANG_KEYSET ${JSON.stringify(Object.fromEntries([...languageKeys].map(([key, value]) => [key, value.length])))}`);
console.log(`RR_ASSET_LANG_MISSING_VS_EN_US ${JSON.stringify(languageMissing)}`);
console.log(`RR_ASSET_JEI_LANG_OK languages=${languageFiles.length} keys=${requiredJeiKeys.size}`);
console.log(`RR_ASSET_LANG_NAMES_OK languages=${languageFiles.length}`
    + ` entities=${ENTITY_NAME_ROWS.length} spawnEggs=${localizedSpawnEggKeys.length}`
    + ` mirrored=${MIRRORED_NAME_PAIRS.length} ui=${UI_TEXT_ROWS.length}`);
console.log(`RR_ASSET_VISUAL_FIDELITY_OK legacyExact=${pngFiles.length - authoredTextureCount}`
    + ` authored=${authoredTextureCount} placeholderFaces=0 placeholderRenderers=0`);