#!/usr/bin/env node

const crypto = require('crypto');
const fs = require('fs');
const path = require('path');

const root = path.resolve(__dirname, '..');
const legacyRelative = 'docs/AbyssalCraft-1.12.2/src/main/java/com/shinoow/abyssalcraft/common/world/ChunkGeneratorDarkRealm.java';
const legacySource = path.join(root, ...legacyRelative.split('/'));
const outputPath = process.argv[2] ? path.resolve(process.argv[2])
    : path.join(root, 'src', 'main', 'resources', 'data', 'abyssalcraft', 'validation', 'dark_realm_noise_1_12_2.json');
const fixedSeed = 1251393890;
const samples = [
    [0, 64, 0], [16, 64, 16], [32, 64, 32], [-16, 64, -16], [-32, 64, -32],
    [8, 96, 8], [24, 96, 24], [-8, 96, -8], [-24, 96, -24],
    [0, 30, 0], [16, 30, 16], [0, 157, 0], [16, 157, 16],
    [0, 80, 0], [0, 120, 0], [64, 90, 64], [-64, 110, -64],
    [48, 70, 48], [-48, 100, -48], [80, 85, 80]
];

class JavaRandom {
    constructor(seed) {
        this.seed = (BigInt(seed) ^ 0x5DEECE66Dn) & ((1n << 48n) - 1n);
    }
    next(bits) {
        this.seed = (this.seed * 0x5DEECE66Dn + 0xBn) & ((1n << 48n) - 1n);
        return Number(this.seed >> BigInt(48 - bits));
    }
    nextInt(bound) {
        if ((bound & -bound) === bound) return Math.floor((bound * this.next(31)) / 0x80000000);
        let bits;
        let value;
        do {
            bits = this.next(31);
            value = bits % bound;
        } while (bits - value + bound - 1 >= 0x80000000);
        return value;
    }
    nextDouble() {
        return (this.next(26) * 134217728 + this.next(27)) / 9007199254740992;
    }
}

function fade(value) {
    return value * value * value * (value * (value * 6 - 15) + 10);
}
function lerp(amount, first, second) {
    return first + amount * (second - first);
}
function grad(hash, x, y, z) {
    const h = hash & 15;
    const u = h < 8 ? x : y;
    const v = h < 4 ? y : (h === 12 || h === 14 ? x : z);
    return ((h & 1) === 0 ? u : -u) + ((h & 2) === 0 ? v : -v);
}

class NoiseGeneratorImproved {
    constructor(random) {
        this.xCoord = random.nextDouble() * 256;
        this.yCoord = random.nextDouble() * 256;
        this.zCoord = random.nextDouble() * 256;
        this.permutations = new Array(512);
        for (let index = 0; index < 256; index++) this.permutations[index] = index;
        for (let index = 0; index < 256; index++) {
            const swap = random.nextInt(256 - index) + index;
            const value = this.permutations[index];
            this.permutations[index] = this.permutations[swap];
            this.permutations[swap] = value;
            this.permutations[index + 256] = this.permutations[index];
        }
    }
    sample(x, y, z) {
        const floorX = Math.floor(x);
        const floorY = Math.floor(y);
        const floorZ = Math.floor(z);
        const localX = x - floorX;
        const localY = y - floorY;
        const localZ = z - floorZ;
        const px = floorX & 255;
        const py = floorY & 255;
        const pz = floorZ & 255;
        const a = this.permutations[px] + py;
        const aa = this.permutations[a] + pz;
        const ab = this.permutations[a + 1] + pz;
        const b = this.permutations[px + 1] + py;
        const ba = this.permutations[b] + pz;
        const bb = this.permutations[b + 1] + pz;
        const xFade = fade(localX);
        const yFade = fade(localY);
        const zFade = fade(localZ);
        return lerp(zFade,
            lerp(yFade,
                lerp(xFade, grad(this.permutations[aa], localX, localY, localZ),
                    grad(this.permutations[ba], localX - 1, localY, localZ)),
                lerp(xFade, grad(this.permutations[ab], localX, localY - 1, localZ),
                    grad(this.permutations[bb], localX - 1, localY - 1, localZ))),
            lerp(yFade,
                lerp(xFade, grad(this.permutations[aa + 1], localX, localY, localZ - 1),
                    grad(this.permutations[ba + 1], localX - 1, localY, localZ - 1)),
                lerp(xFade, grad(this.permutations[ab + 1], localX, localY - 1, localZ - 1),
                    grad(this.permutations[bb + 1], localX - 1, localY - 1, localZ - 1))));
    }
    populate(output, x, y, z, xSize, ySize, zSize, xScale, yScale, zScale, amplitude) {
        if (ySize === 1) {
            let index = 0;
            for (let xIndex = 0; xIndex < xSize; xIndex++) {
                const sampleX = x + xIndex * xScale + this.xCoord;
                const floorX = Math.floor(sampleX);
                const localX = sampleX - floorX;
                const px = floorX & 255;
                const xFade = fade(localX);
                for (let zIndex = 0; zIndex < zSize; zIndex++) {
                    const sampleZ = z + zIndex * zScale + this.zCoord;
                    const floorZ = Math.floor(sampleZ);
                    const localZ = sampleZ - floorZ;
                    const pz = floorZ & 255;
                    const a = this.permutations[px];
                    const b = this.permutations[px + 1];
                    const lower = lerp(xFade,
                        grad(this.permutations[a + pz], localX, 0, localZ),
                        grad(this.permutations[b + pz], localX - 1, 0, localZ));
                    const upper = lerp(xFade,
                        grad(this.permutations[a + pz + 1], localX, 0, localZ - 1),
                        grad(this.permutations[b + pz + 1], localX - 1, 0, localZ - 1));
                    output[index++] += lerp(fade(localZ), lower, upper) / amplitude;
                }
            }
            return;
        }
        let index = 0;
        for (let xIndex = 0; xIndex < xSize; xIndex++) {
            const sampleX = x + xIndex * xScale + this.xCoord;
            for (let zIndex = 0; zIndex < zSize; zIndex++) {
                const sampleZ = z + zIndex * zScale + this.zCoord;
                for (let yIndex = 0; yIndex < ySize; yIndex++) {
                    const sampleY = y + yIndex * yScale + this.yCoord;
                    output[index++] += this.sample(sampleX, sampleY, sampleZ) / amplitude;
                }
            }
        }
    }
}

class NoiseGeneratorOctaves {
    constructor(random, octaves) {
        this.generators = Array.from({ length: octaves }, () => new NoiseGeneratorImproved(random));
    }
    generate3d(x, y, z, xSize, ySize, zSize, xScale, yScale, zScale) {
        const output = new Array(xSize * ySize * zSize).fill(0);
        let frequency = 1;
        for (const generator of this.generators) {
            generator.populate(output, x * frequency, y * frequency, z * frequency,
                xSize, ySize, zSize, xScale * frequency, yScale * frequency,
                zScale * frequency, frequency);
            frequency /= 2;
        }
        return output;
    }
    generate2d(x, z, xSize, zSize, xScale, zScale) {
        return this.generate3d(x, 10, z, xSize, 1, zSize, xScale, 1, zScale);
    }
}

class LegacyDarkRealmNoise {
    constructor() {
        const random = new JavaRandom(fixedSeed);
        this.noise1 = new NoiseGeneratorOctaves(random, 16);
        this.noise2 = new NoiseGeneratorOctaves(random, 16);
        this.noise3 = new NoiseGeneratorOctaves(random, 8);
        this.noise4 = new NoiseGeneratorOctaves(random, 10);
        this.noise5 = new NoiseGeneratorOctaves(random, 16);
        this.chunkCache = new Map();
    }
    densities(chunkX, chunkZ) {
        const cacheKey = `${chunkX},${chunkZ}`;
        if (this.chunkCache.has(cacheKey)) return this.chunkCache.get(cacheKey);
        const x = chunkX * 2;
        const z = chunkZ * 2;
        const noiseData4 = this.noise4.generate2d(x, z, 3, 3, 1.121, 1.121);
        this.noise5.generate2d(x, z, 3, 3, 200, 200);
        const noiseData1 = this.noise3.generate3d(x, 0, z, 3, 33, 3, 1368.824 / 80, 684.412 / 160, 1368.824 / 80);
        const noiseData2 = this.noise1.generate3d(x, 0, z, 3, 33, 3, 1368.824, 684.412, 1368.824);
        const noiseData3 = this.noise2.generate3d(x, 0, z, 3, 33, 3, 1368.824, 684.412, 1368.824);
        const result = new Array(297);
        let index = 0;
        let column = 0;
        for (let xIndex = 0; xIndex < 3; xIndex++) {
            for (let zIndex = 0; zIndex < 3; zIndex++) {
                let scale = (noiseData4[column++] + 256) / 512;
                scale = Math.max(0, Math.min(1, scale)) + 0.5;
                for (let yIndex = 0; yIndex < 33; yIndex++) {
                    const blend = (noiseData1[index] / 10 + 1) / 2;
                    const low = noiseData2[index] / 512;
                    const high = noiseData3[index] / 512;
                    let density = blend < 0 ? low : blend > 1 ? high : low + (high - low) * blend;
                    density -= 8;
                    if (yIndex > 1) {
                        const amount = (yIndex - 1) / 31;
                        density = density * (1 - amount) - 30 * amount;
                    }
                    if (yIndex < 8) {
                        const amount = (8 - yIndex) / 7;
                        density = density * (1 - amount) - 30 * amount;
                    }
                    result[index++] = density;
                }
            }
        }
        this.chunkCache.set(cacheKey, result);
        return result;
    }
    sample(worldX, worldY, worldZ) {
        if (worldY < 30 || worldY > 157) {
            return { density: null, air: false, chunk: [Math.floor(worldX / 16), Math.floor(worldZ / 16)],
                local: [Math.floorMod ? Math.floorMod(worldX, 16) : ((worldX % 16) + 16) % 16,
                    ((worldZ % 16) + 16) % 16], cell: null };
        }
        const chunkX = Math.floor(worldX / 16);
        const chunkZ = Math.floor(worldZ / 16);
        const localX = worldX - chunkX * 16;
        const localZ = worldZ - chunkZ * 16;
        const cellX = Math.floor(localX / 8);
        const cellZ = Math.floor(localZ / 8);
        const cellY = Math.min(31, Math.floor((worldY - 30) / 4));
        const fractionX = (localX % 8) / 8;
        const fractionZ = (localZ % 8) / 8;
        const fractionY = ((worldY - 30) % 4) / 4;
        const values = this.densities(chunkX, chunkZ);
        const at = (x, z, y) => values[(x * 3 + z) * 33 + y];
        const lowerA = lerp(fractionZ, at(cellX, cellZ, cellY), at(cellX, cellZ + 1, cellY));
        const lowerB = lerp(fractionZ, at(cellX + 1, cellZ, cellY), at(cellX + 1, cellZ + 1, cellY));
        const upperA = lerp(fractionZ, at(cellX, cellZ, cellY + 1), at(cellX, cellZ + 1, cellY + 1));
        const upperB = lerp(fractionZ, at(cellX + 1, cellZ, cellY + 1), at(cellX + 1, cellZ + 1, cellY + 1));
        const density = lerp(fractionY, lerp(fractionX, lowerA, lowerB), lerp(fractionX, upperA, upperB));
        return { density, air: density > 0, chunk: [chunkX, chunkZ], local: [localX, localZ],
            cell: [cellX, cellY, cellZ] };
    }
    carves(worldX, worldY, worldZ) {
        return this.sample(worldX, worldY, worldZ).air;
    }
}

const source = fs.readFileSync(legacySource, 'utf8');
for (const anchor of ['omtRNG = new Random(1251393890L);', 'initializeNoiseField', 'if (d15 > 0.0D)']) {
    if (!source.includes(anchor)) throw new Error(`legacy source anchor missing: ${anchor}`);
}
const sourceSha256 = crypto.createHash('sha256').update(source).digest('hex');
const legacy = new LegacyDarkRealmNoise();
const discovered = [];
for (let x = -256; x <= 256 && discovered.length < 8; x += 8) {
    for (let z = -256; z <= 256 && discovered.length < 8; z += 8) {
        for (let y = 34; y <= 150 && discovered.length < 8; y += 4) {
            if (legacy.carves(x, y, z)) discovered.push([x, y, z]);
        }
    }
}
if (discovered.length < 8) throw new Error(`legacy carve scan found only ${discovered.length} positive samples`);
const oracleSamples = samples.concat(discovered);
const baseline = {
    schema: 1,
    provenance: {
        source: legacyRelative,
        sourceSha256,
        fixedSeed,
        algorithm: 'minecraft-1.12.2-java-random-improved-octaves+ChunkGeneratorDarkRealm.initializeNoiseField',
        extractor: 'scripts/extract_dark_realm_oracle.js',
        output: 'density>0 means the legacy second pass writes minecraft:air at pos',
        coordinateDerivation: 'chunk=floor(xz/16), local=xz-chunk*16, cell=[floor(localX/8),floor((y-30)/4),floor(localZ/8)]'
    },
    samples: oracleSamples.map(pos => ({ pos, ...legacy.sample(...pos) }))
};
fs.mkdirSync(path.dirname(outputPath), { recursive: true });
fs.writeFileSync(outputPath, `${JSON.stringify(baseline, null, 2)}\n`);
console.log(`RR_WORLD_ORACLE_EXTRACT_OK samples=${baseline.samples.length} sourceSha256=${sourceSha256} output=${outputPath}`);