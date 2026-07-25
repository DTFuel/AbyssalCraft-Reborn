#!/usr/bin/env node
/*
 * Converts a 1.12 Tabula `ModelBase` entity model (.java) into a 1.20.1
 * `createBodyLayer()` LayerDefinition method body (geometry only).
 *
 * Handles: `new ModelRenderer(this, u, v)` (+ setTextureOffset), setRotationPoint,
 * addBox (one or many, with optional inflate/scale), setRotateAngle, mirror, and the
 * addChild hierarchy. The per-entity setupAnim() must still be hand-ported from the
 * original setRotationAngles().
 *
 * Usage: node scripts/convert_modelbase_to_entitymodel.js <Model.java>
 */
'use strict';
const fs = require('fs');

function num(s) {
    s = s.trim().replace(/[fF]$/, '');
    if (s === '') return 0;
    const v = parseFloat(s);
    return Number.isFinite(v) ? v : 0;
}
function fmt(v) {
    // Print floats with an F suffix; integers stay integers for box dims.
    if (Number.isInteger(v)) return String(v) + '.0F';
    return String(v) + 'F';
}
function fmtInt(v) { return String(Math.round(v)); }

function parse(javaPath) {
    const text = fs.readFileSync(javaPath, 'utf8');
    let texW = 64, texH = 32;
    let m;
    if ((m = text.match(/textureWidth\s*=\s*(\d+)/))) texW = parseInt(m[1]);
    if ((m = text.match(/textureHeight\s*=\s*(\d+)/))) texH = parseInt(m[1]);

    // Only parse the constructor body (between the first `public ModelXxx(` and its render/setRotationAngles).
    const ctorStart = text.indexOf('){', text.indexOf('public Model'));
    let body = text;
    const renderIdx = text.indexOf('public void render');
    const setAnglesIdx = text.indexOf('public void setRotationAngles');
    let end = text.length;
    if (renderIdx > 0) end = Math.min(end, renderIdx);
    if (setAnglesIdx > 0) end = Math.min(end, setAnglesIdx);
    body = text.substring(0, end);

    const parts = {}; // name -> { tex:[u,v], offset:[x,y,z], rot:[x,y,z], boxes:[], mirror:false, children:[] }
    const order = [];
    function ensure(name) {
        if (!parts[name]) { parts[name] = { tex: [0, 0], offset: [0, 0, 0], rot: [0, 0, 0], boxes: [], mirror: false, children: [] }; order.push(name); }
        return parts[name];
    }

    const lines = body.split('\n');
    const curTex = {}; // running texOffs per part (for setTextureOffset before addBox)
    for (const line of lines) {
        let mm;
        if ((mm = line.match(/(?:this\.)?(\w+)\s*=\s*new \w*ModelRenderer\(this(?:\s*,\s*([\d.]+)\s*,\s*([\d.]+))?\)/))) {
            const p = ensure(mm[1]);
            if (mm[2] !== undefined) { p.tex = [num(mm[2]), num(mm[3])]; curTex[mm[1]] = [num(mm[2]), num(mm[3])]; }
        } else if ((mm = line.match(/(?:this\.)?(\w+)\.setTextureOffset\(([\d.]+)\s*,\s*([\d.]+)\)/))) {
            curTex[mm[1]] = [num(mm[2]), num(mm[3])];
            if (parts[mm[1]] && parts[mm[1]].boxes.length === 0) parts[mm[1]].tex = curTex[mm[1]];
        } else if ((mm = line.match(/(?:this\.)?(\w+)\.setRotationPoint\(([^)]+)\)/))) {
            const a = mm[2].split(',');
            ensure(mm[1]).offset = [num(a[0]), num(a[1]), num(a[2])];
        } else if ((mm = line.match(/(?:this\.)?(\w+)\.addBox\(([^)]+)\)/))) {
            const a = mm[2].split(',');
            const p = ensure(mm[1]);
            const tex = curTex[mm[1]] || p.tex;
            p.boxes.push({ tex: tex.slice(), o: [num(a[0]), num(a[1]), num(a[2])], s: [num(a[3]), num(a[4]), num(a[5])], inflate: a[6] !== undefined ? num(a[6]) : 0 });
        } else if ((mm = line.match(/(?:this\.)?set(?:RotateAngle|Rotation)\(\s*(\w+)\s*,\s*([^)]+)\)/))) {
            const a = mm[2].split(',');
            ensure(mm[1]).rot = [num(a[0]), num(a[1]), num(a[2])];
        } else if ((mm = line.match(/(?:this\.)?(\w+)\.mirror\s*=\s*true/))) {
            ensure(mm[1]).mirror = true;
        } else if ((mm = line.match(/(?:this\.)?(\w+)\.addChild\((?:this\.)?(\w+)\)/))) {
            ensure(mm[1]).children.push(mm[2]);
        }
    }
    // roots = parts that are never a child
    const isChild = new Set();
    for (const n of order) for (const c of parts[n].children) isChild.add(c);
    const roots = order.filter(n => !isChild.has(n));
    return { texW, texH, parts, roots };
}

function emitPart(name, parts, indent, parentVar) {
    const p = parts[name];
    const pad = '        ';
    let cube = 'CubeListBuilder.create()';
    if (p.mirror) cube += '.mirror()';
    if (p.boxes.length === 0) {
        cube += '.texOffs(' + fmtInt(p.tex[0]) + ', ' + fmtInt(p.tex[1]) + ')';
    } else {
        for (const b of p.boxes) {
            cube += '.texOffs(' + fmtInt(b.tex[0]) + ', ' + fmtInt(b.tex[1]) + ')';
            cube += '.addBox(' + fmt(b.o[0]) + ', ' + fmt(b.o[1]) + ', ' + fmt(b.o[2]) + ', '
                + fmtInt(b.s[0]) + ', ' + fmtInt(b.s[1]) + ', ' + fmtInt(b.s[2]);
            if (b.inflate && b.inflate !== 0) cube += ', new CubeDeformation(' + fmt(b.inflate) + ')';
            cube += ')';
        }
    }
    const hasRot = p.rot[0] !== 0 || p.rot[1] !== 0 || p.rot[2] !== 0;
    let pose;
    if (hasRot) {
        pose = 'PartPose.offsetAndRotation(' + fmt(p.offset[0]) + ', ' + fmt(p.offset[1]) + ', ' + fmt(p.offset[2])
            + ', ' + fmt(p.rot[0]) + ', ' + fmt(p.rot[1]) + ', ' + fmt(p.rot[2]) + ')';
    } else {
        pose = 'PartPose.offset(' + fmt(p.offset[0]) + ', ' + fmt(p.offset[1]) + ', ' + fmt(p.offset[2]) + ')';
    }
    let out = pad + 'PartDefinition ' + name + ' = ' + parentVar + '.addOrReplaceChild("' + name + '",\n'
        + pad + '        ' + cube + ',\n'
        + pad + '        ' + pose + ');\n';
    for (const c of p.children) out += emitPart(c, parts, indent + 1, name);
    return out;
}

function build(javaPath) {
    const { texW, texH, parts, roots } = parse(javaPath);
    let out = '    public static LayerDefinition createBodyLayer() {\n';
    out += '        MeshDefinition mesh = new MeshDefinition();\n';
    out += '        PartDefinition root = mesh.getRoot();\n\n';
    for (const r of roots) out += emitPart(r, parts, 0, 'root');
    out += '\n        return LayerDefinition.create(mesh, ' + texW + ', ' + texH + ');\n';
    out += '    }';
    return { out, roots, partNames: Object.keys(parts) };
}

// --- full-class emission (fields + ctor-resolve-all + createBodyLayer + setupAnim) ---

function bfsOrder(parts, roots) {
    const out = [];
    const q = roots.slice();
    while (q.length) {
        const n = q.shift();
        out.push(n);
        for (const c of parts[n].children) q.push(c);
    }
    return out;
}

function parentMap(parts) {
    const parentOf = {};
    for (const n in parts) for (const c of parts[n].children) parentOf[c] = n;
    return parentOf;
}

// Translate a 1.12.2 setRotationAngles(...) body into a modern setupAnim body (text transform).
function transformSetRotationAngles(text) {
    const idx = text.indexOf('setRotationAngles');
    if (idx < 0) return null;
    const braceStart = text.indexOf('{', idx);
    if (braceStart < 0) return null;
    let depth = 0, end = -1;
    for (let i = braceStart; i < text.length; i++) {
        if (text[i] === '{') depth++;
        else if (text[i] === '}') { depth--; if (depth === 0) { end = i; break; } }
    }
    if (end < 0) return null;
    let body = text.substring(braceStart + 1, end);
    body = body.replace(/MathHelper/g, 'Mth');
    body = body.replace(/entity\.ticksExisted/g, 'entity.tickCount');
    body = body.replace(/\.rotateAngleX\b/g, '.xRot');
    body = body.replace(/\.rotateAngleY\b/g, '.yRot');
    body = body.replace(/\.rotateAngleZ\b/g, '.zRot');
    // 1.12.2 setRotationAngles(f, f1, f2, f3, f4, f5, entity) -> modern params (multi-digit first).
    body = body.replace(/\bf1\b/g, 'limbSwingAmount');
    body = body.replace(/\bf2\b/g, 'ageInTicks');
    body = body.replace(/\bf3\b/g, 'netHeadYaw');
    body = body.replace(/\bf4\b/g, 'headPitch');
    body = body.replace(/\bf5\b/g, 'partialTick');
    body = body.replace(/\bf\b/g, 'limbSwing');
    // 1.12.2 ModelBase/derived fields that setRotationAngles reads but modern setupAnim lacks: declare
    // them from the entity up front (swingProgress in [0,1] makes the attack block a no-op at rest).
    let prefix = '';
    if (/\bswingProgress\b/.test(body)) prefix += '\n        float swingProgress = entity.getAttackAnim(ageInTicks - entity.tickCount);';
    if (/\bisRiding\b/.test(body)) prefix += '\n        boolean isRiding = entity.isPassenger();';
    if (/\bisChild\b/.test(body)) prefix += '\n        boolean isChild = entity.isBaby();';
    return prefix + body.replace(/\s+$/, '\n');
}

function emitFullClass(javaPath, className, entityFqn) {
    const { texW, texH, parts, roots } = parse(javaPath);
    const text = fs.readFileSync(javaPath, 'utf8');
    const entitySimple = entityFqn.split('.').pop();
    const origName = require('path').basename(javaPath, '.java');
    const bfs = bfsOrder(parts, roots);
    const parentOf = parentMap(parts);
    const rootSet = new Set(roots);
    const usesInflate = Object.keys(parts).some(n => parts[n].boxes.some(b => b.inflate && b.inflate !== 0));

    let s = 'package com.shinoow.abyssalcraft.client.model.entity;\n\n';
    s += 'import net.minecraft.client.model.HierarchicalModel;\n';
    s += 'import net.minecraft.client.model.geom.ModelPart;\n';
    s += 'import net.minecraft.client.model.geom.PartPose;\n';
    s += 'import net.minecraft.client.model.geom.builders.CubeListBuilder;\n';
    if (usesInflate) s += 'import net.minecraft.client.model.geom.builders.CubeDeformation;\n';
    s += 'import net.minecraft.client.model.geom.builders.LayerDefinition;\n';
    s += 'import net.minecraft.client.model.geom.builders.MeshDefinition;\n';
    s += 'import net.minecraft.client.model.geom.builders.PartDefinition;\n';
    s += 'import net.minecraft.util.Mth;\n\n';
    s += 'import ' + entityFqn + ';\n\n';
    s += '/**\n * Full faithful port of 1.12.2 ' + origName + ' (' + texW + 'x' + texH + '). Geometry, part\n';
    s += ' * hierarchy and the setRotationAngles animation are transcribed verbatim by\n';
    s += ' * scripts/convert_modelbase_to_entitymodel.js --full (do not hand-edit; regenerate instead).\n */\n';
    s += 'public class ' + className + ' extends HierarchicalModel<' + entitySimple + '> {\n\n';
    s += '    private final ModelPart root;\n';
    for (const n of bfs) s += '    private final ModelPart ' + n + ';\n';
    s += '\n    public ' + className + '(ModelPart root) {\n';
    s += '        this.root = root;\n';
    for (const n of bfs) {
        if (rootSet.has(n)) s += '        this.' + n + ' = root.getChild("' + n + '");\n';
        else s += '        this.' + n + ' = this.' + parentOf[n] + '.getChild("' + n + '");\n';
    }
    s += '    }\n\n';
    s += '    public static LayerDefinition createBodyLayer() {\n';
    s += '        MeshDefinition mesh = new MeshDefinition();\n';
    s += '        PartDefinition root = mesh.getRoot();\n\n';
    for (const r of roots) s += emitPart(r, parts, 0, 'root');
    s += '\n        return LayerDefinition.create(mesh, ' + texW + ', ' + texH + ');\n';
    s += '    }\n\n';
    const anim = transformSetRotationAngles(text);
    s += '    @Override\n';
    s += '    public void setupAnim(' + entitySimple + ' entity, float limbSwing, float limbSwingAmount,\n';
    s += '                          float ageInTicks, float netHeadYaw, float headPitch) {';
    s += anim ? anim : '\n';
    s += '    }\n\n';
    s += '    @Override\n    public ModelPart root() {\n        return this.root;\n    }\n';
    s += '}\n';
    return s;
}

const args = process.argv.slice(2);
if (args[0] === '--full') {
    // node convert_modelbase_to_entitymodel.js --full <Model.java> <ClassName> <EntityFqn> <outPath>
    const [, javaPath, className, entityFqn, outPath] = args;
    if (!javaPath || !className || !entityFqn || !outPath) {
        console.error('usage: --full <Model.java> <ClassName> <EntityFqn> <outPath>');
        process.exit(1);
    }
    const src = emitFullClass(javaPath, className, entityFqn);
    fs.writeFileSync(outPath, src, 'utf8');
    console.error('wrote ' + outPath + ' (' + src.split('\n').length + ' lines)');
} else {
    const path = args[0];
    if (!path) { console.error('usage: node convert_modelbase_to_entitymodel.js <Model.java> | --full ...'); process.exit(1); }
    const r = build(path);
    console.log('// roots: ' + r.roots.join(', '));
    console.log('// parts: ' + r.partNames.join(', '));
    console.log(r.out);
}

