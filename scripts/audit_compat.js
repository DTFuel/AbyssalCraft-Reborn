const fs = require('fs');
const path = require('path');

const root = path.resolve(__dirname, '..');
const javaRoot = path.join(root, 'src', 'main', 'java');
const platformRoot = path.join(javaRoot, 'com', 'shinoow', 'abyssalcraft', 'platform');
const allowedConditions = new Set(['forge', '<1.21', '>=1.21']);
const loaderReference = /\bnet\.(?:minecraftforge|neoforged)\./;
const directive = /^\s*(?:\*\/)?\/\/\?\s*(if\s+([^\s{]+)\s*\{|\}\s*else\s*\{|\})/;
const errors = [];

function walk(directory) {
    return fs.readdirSync(directory, { withFileTypes: true }).flatMap(entry => {
        const file = path.join(directory, entry.name);
        return entry.isDirectory() ? walk(file) : [file];
    });
}

const javaFiles = walk(javaRoot).filter(file => file.endsWith('.java'));
const businessFiles = javaFiles.filter(file => !file.startsWith(platformRoot + path.sep));
for (const file of businessFiles) {
    const source = fs.readFileSync(file, 'utf8');
    if (loaderReference.test(source)) {
        errors.push(`${path.relative(root, file)} references a loader API outside platform/**`);
    }
}

const platformForks = [];
let businessVersionForks = 0;
for (const file of javaFiles) {
    const source = fs.readFileSync(file, 'utf8');
    const stack = [];
    const conditions = new Set();
    source.split(/\r?\n/).forEach((line, index) => {
        const match = line.match(directive);
        if (!match) return;
        const token = match[1];
        if (token.startsWith('if ')) {
            const condition = match[2];
            if (!allowedConditions.has(condition)) {
                errors.push(`${path.relative(root, file)}:${index + 1} uses unknown condition '${condition}'`);
            }
            stack.push({ condition, hasElse: false, line: index + 1 });
            conditions.add(condition);
            if (!file.startsWith(platformRoot + path.sep)) {
                if (condition === 'forge') {
                    errors.push(`${path.relative(root, file)}:${index + 1} uses a loader fork outside platform/**`);
                } else {
                    businessVersionForks++;
                }
            }
        } else if (token.startsWith('} else')) {
            const current = stack[stack.length - 1];
            if (!current || current.hasElse) {
                errors.push(`${path.relative(root, file)}:${index + 1} has an unmatched else`);
            } else {
                current.hasElse = true;
            }
        } else if (!stack.pop()) {
            errors.push(`${path.relative(root, file)}:${index + 1} has an unmatched close`);
        }
    });
    for (const open of stack) {
        errors.push(`${path.relative(root, file)}:${open.line} has an unclosed '${open.condition}' block`);
    }
    if (conditions.size && file.startsWith(platformRoot + path.sep)) {
        platformForks.push({ symbol: path.basename(file, '.java'), conditions: [...conditions].sort() });
    }
}

const businessSources = businessFiles.map(file => ({ file, source: fs.readFileSync(file, 'utf8') }));
const platformSources = new Map(javaFiles
    .filter(file => file.startsWith(platformRoot + path.sep))
    .map(file => [path.basename(file, '.java'), fs.readFileSync(file, 'utf8')]));
const forkSymbols = new Set(platformForks.map(fork => fork.symbol));
const reachable = new Set(['ModBootstrapCompat']);
const mixinConfig = JSON.parse(fs.readFileSync(path.join(root, 'src', 'main', 'resources', 'abyssalcraft.mixins.json'), 'utf8'));
for (const entry of [...(mixinConfig.mixins || []), ...(mixinConfig.client || []), ...(mixinConfig.server || [])]) {
    if (entry.startsWith('platform.')) reachable.add(entry.substring('platform.'.length));
}
for (const fork of platformForks) {
    if (businessSources.some(entry => new RegExp(`\\b${fork.symbol}\\b`).test(entry.source))) {
        reachable.add(fork.symbol);
    }
}
let changed;
do {
    changed = false;
    for (const owner of [...reachable]) {
        const source = platformSources.get(owner);
        if (!source) continue;
        for (const candidate of forkSymbols) {
            if (!reachable.has(candidate) && new RegExp(`\\b${candidate}\\b`).test(source)) {
                reachable.add(candidate);
                changed = true;
            }
        }
    }
} while (changed);
for (const fork of platformForks) {
    if (!reachable.has(fork.symbol)) {
        errors.push(`platform fork ${fork.symbol} is outside the reachable consumer closure`);
    }
}

const compatSpec = fs.readFileSync(path.join(root, 'docs', 'spec', 'compat-audit.md'), 'utf8');
const documentedForks = new Set([...compatSpec.matchAll(/^\| `([^`]+)` \|/gm)].map(match => match[1]));
for (const fork of platformForks) {
    if (!documentedForks.has(fork.symbol)) errors.push(`platform fork ${fork.symbol} is missing from compat-audit.md`);
}
for (const symbol of documentedForks) {
    if (!forkSymbols.has(symbol)) errors.push(`compat-audit.md documents stale platform fork ${symbol}`);
}

if (errors.length) {
    console.error('RR-COMPAT audit failed:');
    errors.forEach(error => console.error(`- ${error}`));
    process.exit(1);
}

const loaderForks = platformForks.filter(fork => fork.conditions.includes('forge')).length;
const versionForks = platformForks.filter(fork => fork.conditions.some(condition => condition.includes('1.21'))).length;
console.log(`RR_COMPAT_AUDIT_OK symbols=${platformForks.length} loader=${loaderForks} version=${versionForks} businessLoaderReferences=0 businessVersionForks=${businessVersionForks} documented=${documentedForks.size}`);
