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
    const bytes = fs.readFileSync(file);
    const signature = bytes.subarray(0, 8).toString('hex');
    const width = bytes.length >= 24 ? bytes.readUInt32BE(16) : 0;
    const height = bytes.length >= 24 ? bytes.readUInt32BE(20) : 0;
    const compressed = [];
    for (let offset = 8; offset + 12 <= bytes.length;) {
        const length = bytes.readUInt32BE(offset);
        const type = bytes.toString('ascii', offset + 4, offset + 8);
        if (type === 'IDAT') compressed.push(bytes.subarray(offset + 8, offset + 8 + length));
        offset += 12 + length;
    }
    let decoded = false;
    try {
        decoded = zlib.inflateSync(Buffer.concat(compressed)).length > 0;
    } catch (_) {
        decoded = false;
    }
    if (signature !== '89504e470d0a1a0a' || width < 1 || height < 1 || !decoded) {
        missing.push(`undecodable PNG ${relative(file, ROOT)}`);
    }
}

const languageFiles = walk(path.join(ASSET_ROOTS[0], 'lang')).filter(file => file.endsWith('.json'));
const languageKeys = new Map(languageFiles.map(file => [path.basename(file, '.json'), Object.keys(readJson(file))]));
const englishKeys = new Set(languageKeys.get('en_us') || []);
const languageMissing = Object.fromEntries([...languageKeys].map(([language, keys]) =>
    [language, [...englishKeys].filter(key => !new Set(keys).has(key)).length]));

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
console.log(`RR_ASSET_LANG_KEYSET ${JSON.stringify(Object.fromEntries([...languageKeys].map(([key, value]) => [key, value.length])))}`);
console.log(`RR_ASSET_LANG_MISSING_VS_EN_US ${JSON.stringify(languageMissing)}`);