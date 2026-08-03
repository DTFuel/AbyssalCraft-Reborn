// Converts a 1.12 Betweenlands ModelBase Java class into a GeckoLib (Bedrock 1.12.0) .geo.json.
//
// Calibrated MC ModelRenderer -> GeckoLib block-geo mapping (validated on betweenstone_pebble_pile):
//   X/Z centered at pixel 0 (no +8), Y = 24 - mc_y (Y=0 at block bottom).
//   bone pivot   = (absRotX, 24 - absRotY, absRotZ)   where absRot = sum of rotationPoints up the parent chain
//   bone rotation = [-degX, -degY, -degZ]              (own setRotateAngle, radians->deg, Y-flip negation)
//   cube origin  = (absRotX+ox, 24-(absRotY+oy+h), absRotZ+oz), size (w,h,d), uv [texU,texV], inflate=scale
// Parent/child bones are preserved (GeckoLib composes parent->child transforms, matching 1.12 addChild render).
//
// Usage: node convert_modelbase_to_geo.js <ModelBase.java> <out.geo.json> <identifier> [texture.png]

const fs = require('fs');
const path = require('path');

function num(s) { return parseFloat(String(s).replace(/[fFdD]/g, '').trim()); }

// Reads a PNG's width/height from its IHDR. The geo.json MUST use the ACTUAL texture dimensions, not the model's
// declared textureWidth/Height: in 1.12 ModelFromModelBase was given the real texture size (e.g. moss_bed's model
// declares 128x70 but the texture is 128x128), and the UVs are normalised by the real size. If the destination
// texture textures/block/<identifier>.png exists, use it; otherwise fall back to the model's declared dims.
function pngSize(p) {
    if (!fs.existsSync(p)) return null;
    const b = fs.readFileSync(p);
    if (b.length < 24) return null;
    return [b.readUInt32BE(16), b.readUInt32BE(20)];
}

function parse(javaPath, parameters = {}) {
    const text = fs.readFileSync(javaPath, 'utf8');
    let texW = 64, texH = 64;
    let mw = text.match(/textureWidth\s*=\s*(\d+)/); if (mw) texW = parseInt(mw[1]);
    let mh = text.match(/textureHeight\s*=\s*(\d+)/); if (mh) texH = parseInt(mh[1]);

    const bipedParents = {
        bipedHead: 'armorHead',
        bipedBody: 'armorBody',
        bipedRightArm: 'armorRightArm',
        bipedLeftArm: 'armorLeftArm',
        bipedRightLeg: 'armorRightLeg',
        bipedLeftLeg: 'armorLeftLeg'
    };
    const renderers = {}; // name -> { texU, texV, rot:[rx,ry,rz], boxes:[{ox,oy,oz,w,h,d,inflate}], parent, relRot:[x,y,z] }
    const ensure = (n) => (renderers[n] = renderers[n] || { texU: 0, texV: 0, rot: [0, 0, 0], boxes: [], parent: null, relRot: [0, 0, 0] });
    const clean = (n) => {
        const name = n.replace(/^this\./, '');
        return Object.hasOwn(parameters, 'modelBipedInflate') ? bipedParents[name] || name : name;
    };

    // EnderDragon-style support (ModelDragon*): local float vars for coordinate expressions (`float f1 = -16.0F;`),
    // a `setTextureOffset("bone.sub", u, v)` map, and a small arithmetic evaluator for args like `-8.0F + f1`.
    const vars = { ...parameters };
    for (const [name, value] of Object.entries(vars)) {
        if (!/^[A-Za-z_]\w*$/.test(name) || !Number.isFinite(value)) {
            throw new Error(`Invalid conversion parameter ${name}=${value}`);
        }
    }
    if (Object.hasOwn(parameters, 'modelBipedInflate')) {
        const inflate = parameters.modelBipedInflate;
        const addBipedPart = (name, texU, texV, relRot, box, mirror = false) => {
            const renderer = ensure(name);
            renderer.texU = texU;
            renderer.texV = texV;
            renderer.relRot = relRot;
            renderer.boxes.push({ ...box, inflate });
            renderer.mirror = mirror;
        };
        addBipedPart('armorHead', 0, 0, [0, 0, 0], { ox: -4, oy: -8, oz: -4, w: 8, h: 8, d: 8 });
        ensure('armorHead').boxes.push({ ox: -4, oy: -8, oz: -4, w: 8, h: 8, d: 8,
            inflate: inflate + 0.5, texU: 32, texV: 0 });
        addBipedPart('armorBody', 16, 16, [0, 0, 0], { ox: -4, oy: 0, oz: -2, w: 8, h: 12, d: 4 });
        addBipedPart('armorRightArm', 40, 16, [-5, 2, 0], { ox: -3, oy: -2, oz: -2, w: 4, h: 12, d: 4 });
        addBipedPart('armorLeftArm', 40, 16, [5, 2, 0], { ox: -1, oy: -2, oz: -2, w: 4, h: 12, d: 4 }, true);
        addBipedPart('armorRightLeg', 0, 16, [-2, 12, 0], { ox: -2, oy: 0, oz: -2, w: 4, h: 12, d: 4 });
        addBipedPart('armorLeftLeg', 0, 16, [2, 12, 0], { ox: -2, oy: 0, oz: -2, w: 4, h: 12, d: 4 }, true);
        ensure('armorRightBoot').relRot = [-2, 12, 0];
        ensure('armorLeftBoot').relRot = [2, 12, 0];
    }
    for (const m of text.matchAll(/\bfloat\s+(\w+)\s*=\s*(-?[\d.]+)[fFdD]?\s*;/g)) vars[m[1]] = parseFloat(m[2]);
    const texOffs = {};
    for (const m of text.matchAll(/setTextureOffset\(\s*"([^"]+)"\s*,\s*(\d+)\s*,\s*(\d+)\s*\)/g)) texOffs[m[1]] = [parseInt(m[2]), parseInt(m[3])];
    const evalNum = (s) => {
        let e = String(s).replace(/([\d.])[fFdD]\b/g, '$1').trim();
        if (e === '') return NaN;
        e = e.replace(/[A-Za-z_]\w*/g, (id) => (id in vars ? '(' + vars[id] + ')' : id));
        if (!/^[-+*/(). \d]+$/.test(e)) return NaN;
        try { const v = Function('"use strict";return (' + e + ')')(); return typeof v === 'number' ? v : NaN; } catch (_) { return NaN; }
    };

    // new ModelRenderer(this, U, V)
    for (const m of text.matchAll(/(?:this\.)?(\w+)\s*=\s*new\s+ModelRenderer\(\s*this\s*,\s*([\d.\-fF]+)\s*,\s*([\d.\-fF]+)\s*\)/g)) {
        const r = ensure(clean(m[1])); r.texU = num(m[2]); r.texV = num(m[3]);
    }
    // also bare "new ModelRenderer(this)" (defaults 0,0)
    for (const m of text.matchAll(/(?:this\.)?(\w+)\s*=\s*new\s+ModelRenderer\(\s*this\s*\)/g)) ensure(clean(m[1]));
    // new ModelRenderer(this, "name")  (EnderDragon-style; per-box texOffs used instead of a bone texOffs)
    for (const m of text.matchAll(/(?:this\.)?(\w+)\s*=\s*new\s+ModelRenderer\(\s*this\s*,\s*"[^"]*"\s*\)/g)) ensure(clean(m[1]));

    // setRotationPoint(x,y,z)  (args may be expressions, e.g. `8.0F + f1`)
    for (const m of text.matchAll(/(?:this\.)?(\w+)\.setRotationPoint\(([^)]*)\)/g)) {
        if (Object.hasOwn(parameters, 'modelBipedInflate') && bipedParents[m[1]]) continue;
        const a = m[2].split(',').map(evalNum); const r = ensure(clean(m[1])); r.relRot = [a[0], a[1], a[2]];
    }
    // addBox(ox,oy,oz,w,h,d[,scale])  OR  addBox("sub", x,y,z,w,h,d)  (EnderDragon-style named box;
    // its UV comes from setTextureOffset("<bone>.<sub>", u, v) rather than a per-bone texOffs).
    for (const m of text.matchAll(/(?:this\.)?(\w+)\.addBox\(([^)]*)\)/g)) {
        const nm = clean(m[1]); const r = ensure(nm);
        const args = m[2].split(',').map((s) => s.trim());
        if (args[0].startsWith('"')) {
            const sub = args[0].replace(/"/g, '');
            const a = args.slice(1).map(evalNum);
            const to = texOffs[nm + '.' + sub];
            r.boxes.push({ ox: a[0], oy: a[1], oz: a[2], w: a[3], h: a[4], d: a[5], inflate: a.length > 6 ? a[6] : 0,
                texU: to ? to[0] : undefined, texV: to ? to[1] : undefined });
        } else {
            const a = args.map(evalNum);
            r.boxes.push({ ox: a[0], oy: a[1], oz: a[2], w: a[3], h: a[4], d: a[5], inflate: a.length > 6 ? a[6] : 0 });
        }
    }
    // setRotateAngle(name, x, y, z)
    for (const m of text.matchAll(/setRotateAngle\(\s*(?:this\.)?(\w+)\s*,\s*([^)]*)\)/g)) {
        const a = m[2].split(',').map(num); const r = ensure(clean(m[1])); r.rot = [a[0], a[1], a[2]];
    }
    // setRotation(name, x, y, z) -- helper some models (e.g. AbyssalCraft's ModelChagaroth) use instead of setRotateAngle
    for (const m of text.matchAll(/setRotation\(\s*(?:this\.)?(\w+)\s*,\s*([^)]*)\)/g)) {
        const a = m[2].split(',').map(num); const nm = clean(m[1]);
        if (renderers[nm] && a.length >= 3 && !Number.isNaN(a[0])) renderers[nm].rot = [a[0], a[1], a[2]];
    }
    // NAME.mirror = true  (per-renderer; Bedrock geo carries mirror per cube)
    for (const m of text.matchAll(/(?:this\.)?(\w+)\.mirror\s*=\s*true/g)) {
        const nm = clean(m[1]); if (renderers[nm]) renderers[nm].mirror = true;
    }
    // parent.addChild(child)
    for (const m of text.matchAll(/(?:this\.)?(\w+)\.addChild\(\s*(?:this\.)?(\w+)\s*\)/g)) {
        const parent = clean(m[1]);
        const child = ensure(clean(m[2])); child.parent = parent; ensure(parent);
    }
    return { texW, texH, renderers };
}

function absRot(name, renderers, memo) {
    if (memo[name]) return memo[name];
    const r = renderers[name];
    const base = r.parent ? absRot(r.parent, renderers, memo) : [0, 0, 0];
    const v = [base[0] + r.relRot[0], base[1] + r.relRot[1], base[2] + r.relRot[2]];
    memo[name] = v; return v;
}

// Rotation conversion (validated on offering_table): geometry is Y-flipped (24 - mc_y), so to keep the visual
// up/down and in/out correct under GeckoLib's rotation convention, X and Z keep their sign while Y is negated.
// (All-negate looked fine on the symmetric pebble but flipped offering_table's legs inward / edges down.)
const degX = (rad) => +(rad * 180 / Math.PI).toFixed(4);
const degY = (rad) => +(-rad * 180 / Math.PI).toFixed(4);
const degZ = (rad) => +(rad * 180 / Math.PI).toFixed(4);
const r4 = (x) => +x.toFixed(4);

function build(javaPath, identifier, texturePath, parameters) {
    const { texW, texH, renderers } = parse(javaPath, parameters);
    // Prefer the ACTUAL destination texture size over the model's declared textureWidth/Height (see pngSize note).
    // The explicit path keeps this converter namespace-agnostic. The legacy Betweenlands fallback preserves the
    // original CLI behavior for callers that do not pass one.
    const actual = (texturePath ? pngSize(texturePath) : null)
        || pngSize(path.join('src/main/resources/assets/thebetweenlands/textures/block', identifier + '.png'))
        || pngSize(path.join('src/main/resources/assets/thebetweenlands/textures/item', identifier + '.png'));
    const finalW = actual ? actual[0] : texW;
    const finalH = actual ? actual[1] : texH;
    const memo = {};
    const bones = [];
    for (const name of Object.keys(renderers)) {
        const r = renderers[name];
        const abs = absRot(name, renderers, memo);
        const bone = { name };
        if (r.parent) bone.parent = r.parent;
        bone.pivot = [r4(abs[0]), r4(24 - abs[1]), r4(abs[2])];
        if (r.rot[0] || r.rot[1] || r.rot[2]) bone.rotation = [degX(r.rot[0]), degY(r.rot[1]), degZ(r.rot[2])];
        if (r.boxes.length) {
            bone.cubes = r.boxes.map(b => {
                const cube = {
                    origin: [r4(abs[0] + b.ox), r4(24 - (abs[1] + b.oy + b.h)), r4(abs[2] + b.oz)],
                    size: [b.w, b.h, b.d],
                    uv: [b.texU !== undefined ? b.texU : r.texU, b.texV !== undefined ? b.texV : r.texV]
                };
                if (r.mirror) cube.mirror = true;
                if (b.inflate) cube.inflate = b.inflate;
                return cube;
            });
        }
        const numbers = JSON.stringify(bone).match(/-?\d+(?:\.\d+)?(?:e[+-]?\d+)?/gi) || [];
        if (numbers.some(value => !Number.isFinite(Number(value)))) {
            throw new Error(`Non-finite value in bone ${name} from ${javaPath}`);
        }
        bones.push(bone);
    }
    return {
        format_version: '1.12.0',
        'minecraft:geometry': [{
            description: { identifier: 'geometry.' + identifier, texture_width: finalW, texture_height: finalH },
            bones
        }]
    };
}

if (require.main === module) {
    const [, , javaPath, outPath, identifier, texturePath] = process.argv;
    if (!javaPath || !outPath) {
        console.error('Usage: node convert_modelbase_to_geo.js <ModelBase.java> <out.geo.json> <identifier> [texture.png]');
        process.exit(2);
    }
    const geo = build(javaPath, identifier || 'model', texturePath);
    fs.mkdirSync(path.dirname(outPath), { recursive: true });
    fs.writeFileSync(outPath, `${JSON.stringify(geo, null, 2)}\n`);
    console.log('wrote ' + outPath + ' (' + geo['minecraft:geometry'][0].bones.length + ' bones)');
}

module.exports = { build, parse, pngSize };
