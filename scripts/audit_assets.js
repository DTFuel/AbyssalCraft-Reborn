const crypto = require('crypto');
const fs = require('fs');
const path = require('path');
const zlib = require('zlib');

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
    'gui/container/spellcraft_test.png',
    'items/deprecated.png',
    'items/devsword.png',
    'items/necronahicon.png',
    'items/scriptures_omniscience.png',
    'model/abyssal_zombie_old.png',
    'model/abyssal_zombie_old_eyes.png',
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
    const exactTargets = modernByHash.get(sourceHash);
    if (exactTargets) {
        return {
            status: 'MIGRATED',
            targets: exactTargets,
            reason: 'Legacy pixels are preserved byte-for-byte in the listed modern texture target(s).',
            owner: 'RR-ASSET/PK-1b',
        };
    }

    if (RETIRED_LEGACY_TEXTURES.has(source)) {
        return {
            status: 'RETIRED',
            targets: ['docs/spec/legacy-texture-audit.md'],
            reason: 'The legacy-only, test, deprecated, old-variant, or removed content owner is absent from the frozen modern production contract.',
            owner: 'RR-ASSET/PK-1b',
        };
    }

    if (source.startsWith('blocks/')) {
        return {
            status: 'REPLACED',
            targets: ['src/main/resources/assets/abyssalcraft/complex_block_model_fidelity.json'],
            reason: 'The registered block is represented by the audited modern complex-model catalog; legacy layers are merged, renamed, or superseded by its listed model textures.',
            owner: 'RR-ASSET/PK-2b',
        };
    }
    if (source.startsWith('armor/')) {
        return {
            status: 'REPLACED',
            targets: [
                'src/main/java/com/shinoow/abyssalcraft/content/item/armor/ArmorItems.java',
                'src/main/java/com/shinoow/abyssalcraft/client/render/armor/ACArmorVisuals.java',
            ],
            reason: 'Legacy armor layers are superseded by the registered modern armor set and its explicit client visual contract.',
            owner: 'RR-ASSET/armor',
        };
    }
    if (source.startsWith('gui/necronomicon/') || /^gui\/(?:abyssalnomicon|necronomicon)/.test(source)) {
        return {
            status: 'REPLACED',
            targets: ['src/main/java/com/shinoow/abyssalcraft/client/necronomicon/NecronomiconScreen.java'],
            reason: 'The fixed legacy book sheet/page chrome is replaced by the modern navigable Necronomicon screen and its live page renderers.',
            owner: 'RR-ASSET/necronomicon',
        };
    }
    if (source.startsWith('gui/container/') && /(?:_nei|_jei)\.png$/.test(source)) {
        return {
            status: 'REPLACED',
            targets: ['src/main/java/com/shinoow/abyssalcraft/integration/jei/ACJEIPlugin.java'],
            reason: 'The legacy NEI/JEI bitmap is replaced by registered modern JEI categories and drawable layouts.',
            owner: 'RR-ASSET/JEI',
        };
    }
    if (source.startsWith('gui/container/')) {
        return {
            status: 'REPLACED',
            targets: ['src/main/java/com/shinoow/abyssalcraft/client/ACClientSetup.java'],
            reason: 'The legacy fixed container bitmap is replaced by a registered modern screen implementation with live slots and widgets.',
            owner: 'RR-ASSET/GUI',
        };
    }
    if (source.startsWith('items/')) {
        const targets = source.startsWith('items/spirit_tablet/')
            ? [
                'src/main/resources/assets/abyssalcraft/models/item/spirit_tablet.json',
                'src/main/java/com/shinoow/abyssalcraft/client/screen/item/SpiritTabletScreen.java',
            ]
            : source.startsWith('items/crystalbag_')
                ? ['src/main/java/com/shinoow/abyssalcraft/client/screen/item/CrystalBagScreen.java']
                : ['docs/spec/item-content.md'];
        return {
            status: 'REPLACED',
            targets,
            reason: 'The legacy metadata frame or abbreviated icon is superseded by the registered modern item, semantic model, unified texture, or component-driven screen behavior.',
            owner: 'RR-ASSET/item-content',
        };
    }
    if (source.startsWith('model/')) {
        return {
            status: 'REPLACED',
            targets: ['src/main/java/com/shinoow/abyssalcraft/client/render/entity/ACTexturedRenderer.java'],
            reason: 'The legacy entity/tool layer is superseded by the modern entity renderer, eyes layer, Geo layer, or held-item model contract.',
            owner: 'RR-ASSET/entity-rendering',
        };
    }
    if (source === 'misc/potionfx.png') {
        return {
            status: 'REPLACED',
            targets: ['src/main/resources/assets/abyssalcraft/textures/particle/abyssal_fx.png'],
            reason: 'The legacy potion atlas is replaced by the decodable modern AbyssalCraft particle texture and registered particle behavior.',
            owner: 'RR-ASSET/client-fx',
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
    for (const texture of Object.values(model.textures || {})) {
        if (typeof texture === 'string' && !texture.startsWith('#')) {
            auditTexture(texture, relativePath);
        }
    }
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
for (const file of itemModels) {
    auditModel(`abyssalcraft:item/${path.basename(file, '.json')}`, relative(file, ROOT));
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

const energyTiers = ['', 'overworld_', 'abyssal_wasteland_', 'dreadlands_', 'omothol_'];
const energyModelName = (prefix, kind) => prefix ? `${prefix}energy_${kind}` : `energy${kind}`;
for (const prefix of energyTiers) {
    requireModelContract(energyModelName(prefix, 'collector'),
        'abyssalcraft:block/layered_ore', {
            all: 'abyssalcraft:block/energy_glow', overlay: 'abyssalcraft:block/energycollector',
        }, 'minecraft:cutout');
    requireModelContract(energyModelName(prefix, 'container'),
        'abyssalcraft:block/layered_ore', {
            all: 'abyssalcraft:block/energy_glow', overlay: 'abyssalcraft:block/energycontainer',
        }, 'minecraft:cutout');
    requireModelContract(energyModelName(prefix, 'pedestal'),
        'abyssalcraft:block/rending_pedestal', {
            '0': 'abyssalcraft:block/energy_glow', '1': 'abyssalcraft:block/energy_trim',
        }, 'minecraft:cutout');
}
requireModelContract('energydepositioner', 'abyssalcraft:block/layered_ore', {
    all: 'abyssalcraft:block/energy_glow', overlay: 'abyssalcraft:block/energydepositioner',
}, 'minecraft:cutout');

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
const entities = entityMatch ? [...entityMatch[1].matchAll(/"[a-z0-9_]+"/g)].length + 1 : 0;
const rendererRelay = fs.readFileSync(path.join(ROOT,
    'src/main/java/com/shinoow/abyssalcraft/client/render/ACEntityRenderers.java'), 'utf8');
if (entities !== 64 || !rendererRelay.includes('ACPlaceholderRenderer::new')
    || !rendererRelay.includes('registeredEntities.size() != expectedEntities')) {
    missing.push(`entity renderer coverage expected=64 catalog=${entities}`);
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

const legacyTextureRoot = path.join(LEGACY_ROOT, 'textures');
const legacyTextureFiles = walk(legacyTextureRoot).filter(file => file.endsWith('.png')).sort();
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
const languageKeys = new Map(languageFiles.map(file => [path.basename(file, '.json'), Object.keys(readJson(file))]));
const englishKeys = new Set(languageKeys.get('en_us') || []);
const languageMissing = Object.fromEntries([...languageKeys].map(([language, keys]) =>
    [language, [...englishKeys].filter(key => !new Set(keys).has(key)).length]));
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