const fs = require('fs');
const zlib = require('zlib');

function openZip(file) {
  const bytes = fs.readFileSync(file);
  const minimum = Math.max(0, bytes.length - 0xFFFF - 22);
  let end = -1;
  for (let offset = bytes.length - 22; offset >= minimum; offset--) {
    if (bytes.readUInt32LE(offset) === 0x06054B50) {
      end = offset;
      break;
    }
  }
  if (end < 0) throw new Error(`ZIP end record missing: ${file}`);
  const count = bytes.readUInt16LE(end + 10);
  let offset = bytes.readUInt32LE(end + 16);
  const entries = new Map();
  for (let index = 0; index < count; index++) {
    if (bytes.readUInt32LE(offset) !== 0x02014B50) throw new Error(`ZIP central directory corrupt: ${file}`);
    const method = bytes.readUInt16LE(offset + 10);
    const compressedSize = bytes.readUInt32LE(offset + 20);
    const size = bytes.readUInt32LE(offset + 24);
    const nameLength = bytes.readUInt16LE(offset + 28);
    const extraLength = bytes.readUInt16LE(offset + 30);
    const commentLength = bytes.readUInt16LE(offset + 32);
    const localOffset = bytes.readUInt32LE(offset + 42);
    const name = bytes.toString('utf8', offset + 46, offset + 46 + nameLength).replaceAll('\\', '/');
    entries.set(name, { method, compressedSize, size, localOffset });
    offset += 46 + nameLength + extraLength + commentLength;
  }
  function read(name) {
    const entry = entries.get(name);
    if (!entry) return null;
    const local = entry.localOffset;
    if (bytes.readUInt32LE(local) !== 0x04034B50) throw new Error(`ZIP local header corrupt: ${name}`);
    const start = local + 30 + bytes.readUInt16LE(local + 26) + bytes.readUInt16LE(local + 28);
    const compressed = bytes.subarray(start, start + entry.compressedSize);
    if (entry.method === 0) return Buffer.from(compressed);
    if (entry.method === 8) return zlib.inflateRawSync(compressed);
    throw new Error(`Unsupported ZIP method ${entry.method}: ${name}`);
  }
  return { entries, names: [...entries.keys()], read };
}

module.exports = { openZip };