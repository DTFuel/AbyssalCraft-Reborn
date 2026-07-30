const fs = require('fs');
const zlib = require('zlib');

const SIGNATURE = Buffer.from('89504e470d0a1a0a', 'hex');

function paeth(left, up, upLeft) {
    const estimate = left + up - upLeft;
    const leftDistance = Math.abs(estimate - left);
    const upDistance = Math.abs(estimate - up);
    const diagonalDistance = Math.abs(estimate - upLeft);
    if (leftDistance <= upDistance && leftDistance <= diagonalDistance) return left;
    return upDistance <= diagonalDistance ? up : upLeft;
}

function readPng(file) {
    const bytes = fs.readFileSync(file);
    if (!bytes.subarray(0, 8).equals(SIGNATURE)) throw new Error(`Invalid PNG ${file}`);
    let width;
    let height;
    const compressed = [];
    for (let offset = 8; offset < bytes.length;) {
        const length = bytes.readUInt32BE(offset);
        const type = bytes.toString('ascii', offset + 4, offset + 8);
        const data = bytes.subarray(offset + 8, offset + 8 + length);
        if (type === 'IHDR') {
            width = data.readUInt32BE(0);
            height = data.readUInt32BE(4);
            if (data[8] !== 8 || data[9] !== 6 || data[12] !== 0) {
                throw new Error(`Unsupported PNG format ${file}: depth=${data[8]} type=${data[9]} interlace=${data[12]}`);
            }
        } else if (type === 'IDAT') compressed.push(data);
        offset += length + 12;
    }
    if (!width || !height || compressed.length === 0) throw new Error(`Incomplete PNG ${file}`);
    const filtered = zlib.inflateSync(Buffer.concat(compressed));
    const stride = width * 4;
    const pixels = Buffer.alloc(stride * height);
    let source = 0;
    for (let y = 0; y < height; y++) {
        const filter = filtered[source++];
        for (let x = 0; x < stride; x++) {
            const raw = filtered[source++];
            const left = x >= 4 ? pixels[y * stride + x - 4] : 0;
            const up = y > 0 ? pixels[(y - 1) * stride + x] : 0;
            const upLeft = x >= 4 && y > 0 ? pixels[(y - 1) * stride + x - 4] : 0;
            const predictor = switchFilter(filter, left, up, upLeft);
            pixels[y * stride + x] = (raw + predictor) & 0xFF;
        }
    }
    return { width, height, pixels };
}

function switchFilter(filter, left, up, upLeft) {
    switch (filter) {
        case 0: return 0;
        case 1: return left;
        case 2: return up;
        case 3: return Math.floor((left + up) / 2);
        case 4: return paeth(left, up, upLeft);
        default: throw new Error(`Unsupported PNG filter ${filter}`);
    }
}

const CRC_TABLE = Array.from({ length: 256 }, (_, value) => {
    let crc = value;
    for (let bit = 0; bit < 8; bit++) crc = crc & 1 ? 0xEDB88320 ^ (crc >>> 1) : crc >>> 1;
    return crc >>> 0;
});

function crc32(bytes) {
    let crc = 0xFFFFFFFF;
    for (const byte of bytes) crc = CRC_TABLE[(crc ^ byte) & 0xFF] ^ (crc >>> 8);
    return (crc ^ 0xFFFFFFFF) >>> 0;
}

function chunk(type, data) {
    const name = Buffer.from(type, 'ascii');
    const result = Buffer.alloc(data.length + 12);
    result.writeUInt32BE(data.length, 0);
    name.copy(result, 4);
    data.copy(result, 8);
    result.writeUInt32BE(crc32(Buffer.concat([name, data])), data.length + 8);
    return result;
}

function writePng(file, image) {
    const header = Buffer.alloc(13);
    header.writeUInt32BE(image.width, 0);
    header.writeUInt32BE(image.height, 4);
    header[8] = 8;
    header[9] = 6;
    const stride = image.width * 4;
    const raw = Buffer.alloc((stride + 1) * image.height);
    for (let y = 0; y < image.height; y++) {
        const target = y * (stride + 1);
        raw[target] = 0;
        image.pixels.copy(raw, target + 1, y * stride, (y + 1) * stride);
    }
    fs.writeFileSync(file, Buffer.concat([
        SIGNATURE,
        chunk('IHDR', header),
        chunk('IDAT', zlib.deflateSync(raw, { level: 9 })),
        chunk('IEND', Buffer.alloc(0)),
    ]));
}

function composite(images) {
    if (images.length === 0) throw new Error('No PNG layers supplied');
    const { width, height } = images[0];
    if (images.some(image => image.width !== width || image.height !== height)) {
        throw new Error('PNG layer dimensions differ');
    }
    const pixels = Buffer.from(images[0].pixels);
    for (const image of images.slice(1)) {
        for (let index = 0; index < pixels.length; index += 4) {
            const sourceAlpha = image.pixels[index + 3] / 255;
            if (sourceAlpha === 0) continue;
            const targetAlpha = pixels[index + 3] / 255;
            const outputAlpha = sourceAlpha + targetAlpha * (1 - sourceAlpha);
            for (let channel = 0; channel < 3; channel++) {
                pixels[index + channel] = outputAlpha === 0 ? 0 : Math.round(
                    (image.pixels[index + channel] * sourceAlpha
                        + pixels[index + channel] * targetAlpha * (1 - sourceAlpha)) / outputAlpha);
            }
            pixels[index + 3] = Math.round(outputAlpha * 255);
        }
    }
    return { width, height, pixels };
}

function crop(image, x, y, width, height) {
    if (x < 0 || y < 0 || width < 1 || height < 1
        || x + width > image.width || y + height > image.height) {
        throw new Error('PNG crop is outside the source image');
    }
    const pixels = Buffer.alloc(width * height * 4);
    for (let row = 0; row < height; row++) {
        image.pixels.copy(pixels, row * width * 4,
            ((y + row) * image.width + x) * 4,
            ((y + row) * image.width + x + width) * 4);
    }
    return { width, height, pixels };
}

module.exports = { readPng, writePng, composite, crop };