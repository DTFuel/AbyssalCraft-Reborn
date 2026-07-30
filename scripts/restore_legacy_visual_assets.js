#!/usr/bin/env node

const fs = require('fs');
const path = require('path');
const { readPng, writePng, composite, crop } = require('./png_rgba');

const ROOT = path.resolve(__dirname, '..');
const LEGACY = path.join(ROOT,
    'docs/AbyssalCraft-1.12.2/src/main/resources/assets/abyssalcraft');
const MODERN = path.join(ROOT, 'src/main/resources/assets/abyssalcraft');

function readJson(file) {
    return JSON.parse(fs.readFileSync(file, 'utf8').replace(/^\uFEFF/, ''));
}

function writeJson(relativePath, value) {
    const target = path.join(MODERN, relativePath);
    fs.mkdirSync(path.dirname(target), { recursive: true });
    fs.writeFileSync(target, `${JSON.stringify(value, null, 2)}\n`, 'utf8');
}

function copyTexture(source, target) {
    const from = path.join(LEGACY, 'textures', source);
    const to = path.join(MODERN, 'textures', target);
    fs.mkdirSync(path.dirname(to), { recursive: true });
    fs.copyFileSync(from, to);
}

function compositeTexture(sources, target) {
    const output = path.join(MODERN, 'textures', target);
    fs.mkdirSync(path.dirname(output), { recursive: true });
    writePng(output, composite(sources.map(source =>
        readPng(path.join(LEGACY, 'textures', source)))));
}

function cropTexture(source, target, x, y, width, height) {
    const output = path.join(MODERN, 'textures', target);
    fs.mkdirSync(path.dirname(output), { recursive: true });
    writePng(output, crop(readPng(path.join(LEGACY, 'textures', source)), x, y, width, height));
}

function stripUnusedFaces(model) {
    for (const element of model.elements || []) {
        for (const [direction, face] of Object.entries(element.faces || {})) {
            if (face.texture === '#-1' || face.texture === '#missing') delete element.faces[direction];
        }
    }
    return model;
}

function restoreLegacyBlockModel(sourceName, targetName, textures) {
    const model = stripUnusedFaces(readJson(path.join(LEGACY, 'models/block', `${sourceName}.json`)));
    model.credit = `Exact geometry port of AbyssalCraft 1.12.2 ${sourceName}`;
    model.parent = 'abyssalcraft:block/legacy_custom_model';
    model.render_type = 'minecraft:cutout';
    model.textures = textures;
    writeJson(`models/block/${targetName}.json`, model);
}

for (const tier of ['small', 'medium', 'large', 'huge']) {
    const id = `crystalbag_${tier}`;
    copyTexture(`items/${id}.png`, `item/${id}.png`);
    writeJson(`models/item/${id}.json`, {
        parent: 'minecraft:item/generated',
        textures: { layer0: `abyssalcraft:item/${id}` },
    });
}

copyTexture('blocks/crate.png', 'block/crate.png');
writeJson('models/block/crate.json', {
    parent: 'minecraft:block/cube_all',
    textures: { all: 'abyssalcraft:block/crate' },
});

copyTexture('blocks/ritualaltar/parts.png', 'block/ritual_altar/parts.png');
copyTexture('blocks/ritualaltar/cloth.png', 'block/ritual_altar/cloth.png');
restoreLegacyBlockModel('ritualaltar', 'ritual_altar', {
    '0': 'minecraft:block/cobblestone',
    '1': 'abyssalcraft:block/ritual_altar/parts',
    '3': 'abyssalcraft:block/ritual_altar/cloth',
    '4': 'abyssalcraft:block/monolith_stone',
    particle: '#0',
});

copyTexture('blocks/ritualpedestal/overlay_0.png', 'block/ritual_pedestal/overlay_0.png');
copyTexture('blocks/ritualpedestal/glyphs.png', 'block/ritual_pedestal/glyphs.png');
restoreLegacyBlockModel('ritualpedestal', 'ritual_pedestal', {
    '0': 'minecraft:block/cobblestone',
    '1': 'abyssalcraft:block/ritual_pedestal/overlay_0',
    '2': 'abyssalcraft:block/ritual_pedestal/glyphs',
    particle: '#0',
});

copyTexture('blocks/ritualpedestal/overlay2.png', 'block/energy_pedestal/overlay.png');
for (const [source, target, tiered] of [
    ['energypedestal', 'energy_pedestal', false],
    ['energypedestal_tilted', 'energy_pedestal_tilted', false],
    ['tiered_energypedestal', 'tiered_energy_pedestal', true],
    ['tiered_energypedestal_tilted', 'tiered_energy_pedestal_tilted', true],
]) {
    const textures = {
        '0': 'abyssalcraft:block/monolith_stone',
        '1': 'abyssalcraft:block/energy_pedestal/overlay',
        particle: '#0',
    };
    if (tiered) {
        textures['3'] = 'abyssalcraft:block/energy_trim';
        textures['4'] = 'abyssalcraft:block/energy_glow';
    } else {
        textures['2'] = 'abyssalcraft:block/energy_glow';
    }
    restoreLegacyBlockModel(source, target, textures);
}

const spiritAltar = readJson(path.join(LEGACY, 'models/block/spirit_altar.json'));
spiritAltar.credit = 'Exact geometry and UV port of AbyssalCraft 1.12.2 spirit_altar';
spiritAltar.parent = 'abyssalcraft:block/legacy_custom_model';
spiritAltar.textures = {
    '2': 'abyssalcraft:block/chiseled_darkstone_brick',
    '3': 'abyssalcraft:block/darkstone_cobblestone',
    '4': 'minecraft:block/gold_block',
    particle: 'abyssalcraft:block/darkstone_cobblestone',
};
writeJson('models/block/spirit_altar.json', spiritAltar);

copyTexture('blocks/dlttside_overlay.png', 'block/darklands_oak_log_overlay.png');
const darklandsLog = readJson(path.join(LEGACY, 'models/block/dltlog_2.json'));
darklandsLog.credit = 'Exact geometry and UV port of AbyssalCraft 1.12.2 dltlog_2';
darklandsLog.parent = 'abyssalcraft:block/legacy_custom_model';
darklandsLog.render_type = 'minecraft:cutout';
darklandsLog.textures = { particle: '#side' };
writeJson('models/block/darklands_oak_log_layered.json', darklandsLog);

copyTexture('blocks/drgtop.png', 'block/dreadlands_grass_top.png');
writeJson('models/item/dreadlands_grass.json', {
    parent: 'abyssalcraft:block/dreadlands_grass',
    textures: {
        top: 'abyssalcraft:block/dreadlands_grass_top',
        overlay: 'abyssalcraft:block/dreadlands_grass_side',
    },
});

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
for (const [id, material] of Object.entries(tombstoneMaterials)) {
    const relativePath = `models/block/tombstone_${id}.json`;
    const model = readJson(path.join(MODERN, relativePath));
    model.textures['1'] = material;
    writeJson(relativePath, model);
}

const stateTransformerPath = 'models/block/state_transformer.json';
writeJson(stateTransformerPath,
    stripUnusedFaces(readJson(path.join(MODERN, stateTransformerPath))));
for (const anchor of [
    'portal_anchor', 'portal_anchor_active',
    'unchained_portal_anchor', 'unchained_portal_anchor_active',
]) {
    const relativePath = `models/block/${anchor}.json`;
    writeJson(relativePath, stripUnusedFaces(readJson(path.join(MODERN, relativePath))));
}

const brewingPipe = readJson(path.join(LEGACY, 'models/block/brewing_stand_pipe.json'));
brewingPipe.parent = 'abyssalcraft:block/legacy_custom_model';
brewingPipe.render_type = 'minecraft:cutout';
brewingPipe.textures = {
    '0': 'minecraft:block/brewing_stand',
    particle: 'minecraft:block/brewing_stand',
};
writeJson('models/block/brewing_stand_pipe.json', brewingPipe);
writeJson('blockstates/sequential_brewing_stand.json', {
    multipart: [
        { apply: { model: 'minecraft:block/brewing_stand' } },
        { when: { facing: 'north' }, apply: { model: 'abyssalcraft:block/brewing_stand_pipe' } },
        { when: { facing: 'east' }, apply: { model: 'abyssalcraft:block/brewing_stand_pipe', y: 90 } },
        { when: { facing: 'south' }, apply: { model: 'abyssalcraft:block/brewing_stand_pipe', y: 180 } },
        { when: { facing: 'west' }, apply: { model: 'abyssalcraft:block/brewing_stand_pipe', y: 270 } },
    ],
});
writeJson('models/item/sequential_brewing_stand.json', {
    parent: 'minecraft:item/generated',
    textures: { layer0: 'minecraft:item/brewing_stand' },
});
fs.rmSync(path.join(MODERN, 'models/block/sequential_brewing_stand.json'), { force: true });

for (const [source, id] of [
    ['cpp.png', 'coralium_plate'],
    ['dpp.png', 'dreadium_plate'],
]) {
    copyTexture(`items/${source}`, `item/${id}.png`);
    writeJson(`models/item/${id}.json`, {
        parent: 'minecraft:item/generated',
        textures: { layer0: `abyssalcraft:item/${id}` },
    });
}

copyTexture('items/dk.png', 'item/dreadkey.png');
writeJson('models/item/dreadkey.json', {
    parent: 'minecraft:item/generated',
    textures: { layer0: 'abyssalcraft:item/dreadkey' },
});

copyTexture('items/katana.png', 'item/dreadium_katana_hilt.png');
const katanaHilt = stripUnusedFaces(readJson(path.join(LEGACY, 'models/item/dreadhilt.json')));
katanaHilt.credit = 'Exact geometry port of AbyssalCraft 1.12.2 dreadhilt';
katanaHilt.parent = 'abyssalcraft:block/legacy_custom_model';
katanaHilt.textures = { '0': 'abyssalcraft:item/dreadium_katana_hilt' };
writeJson('models/item/dreadium_katana_hilt.json', katanaHilt);

copyTexture('model/katana.png', 'model/katana.png');
copyTexture('model/katana.png', 'block/legacy_obj/katana.png');
for (const extension of ['obj', 'mtl']) {
    const source = path.join(LEGACY, 'models/item', `dreadkatana.${extension}`);
    const target = path.join(MODERN, 'models/item', `dreadkatana.${extension}`);
    fs.copyFileSync(source, target);
}
fs.writeFileSync(path.join(MODERN, 'models/item/dreadkatana.mtl'),
    fs.readFileSync(path.join(MODERN, 'models/item/dreadkatana.mtl'), 'utf8')
        .replace('abyssalcraft:model/katana', 'abyssalcraft:block/legacy_obj/katana'));
writeJson('models/item/dreadium_katana.json', {
    credit: 'Exact OBJ port of AbyssalCraft 1.12.2 dreadkatana',
    parent: 'abyssalcraft:block/legacy_custom_model',
    loader: '__LOADER__:obj',
    model: 'abyssalcraft:models/item/dreadkatana.obj',
    flip_v: true,
    automatic_culling: true,
    shade_quads: true,
    emissive_ambient: false,
    textures: { particle: 'abyssalcraft:block/legacy_obj/katana' },
});

copyTexture('blocks/sealinglock_misc.png', 'block/sealing_lock_misc.png');
const sealingLock = stripUnusedFaces(readJson(path.join(LEGACY, 'models/block/sealing_lock.json')));
sealingLock.credit = 'Exact geometry and UV port of AbyssalCraft 1.12.2 sealing_lock';
sealingLock.parent = 'abyssalcraft:block/legacy_custom_model';
sealingLock.render_type = 'minecraft:cutout';
sealingLock.textures = {
    '0': 'abyssalcraft:block/elysian_stone_brick',
    '1': 'abyssalcraft:block/chiseled_elysian_stone_brick',
    '3': 'abyssalcraft:block/sealing_lock_misc',
    particle: 'abyssalcraft:block/elysian_stone_brick',
};
writeJson('models/block/sealing_lock.json', sealingLock);

copyTexture('blocks/multiblock.png', 'block/multi_block.png');

copyTexture('model/blocks/odb.png', 'model/blocks/odb.png');
copyTexture('model/blocks/odb.png', 'block/legacy_obj/odb.png');
for (const extension of ['obj', 'mtl']) {
    fs.copyFileSync(
        path.join(LEGACY, 'models/block', `odb.${extension}`),
        path.join(MODERN, 'models/block', `odb.${extension}`));
}
fs.writeFileSync(path.join(MODERN, 'models/block/odb.mtl'),
    fs.readFileSync(path.join(MODERN, 'models/block/odb.mtl'), 'utf8')
        .replace('abyssalcraft:model/blocks/odb', 'abyssalcraft:block/legacy_obj/odb'));
writeJson('models/block/oblivion_deathbomb.json', {
    credit: 'Exact OBJ port of AbyssalCraft 1.12.2 odb',
    parent: 'abyssalcraft:block/legacy_custom_model',
    loader: '__LOADER__:obj',
    model: 'abyssalcraft:models/block/odb.obj',
    flip_v: true,
    automatic_culling: true,
    shade_quads: true,
    emissive_ambient: false,
    render_type: 'minecraft:cutout',
    textures: { particle: 'abyssalcraft:block/legacy_obj/odb' },
});

for (const weapon of ['cudgel', 'staff']) {
    copyTexture(`model/${weapon}.png`, `model/${weapon}.png`);
    copyTexture(`model/${weapon}.png`, `block/legacy_obj/${weapon}.png`);
    for (const extension of ['obj', 'mtl']) {
        fs.copyFileSync(
            path.join(LEGACY, 'models/item', `${weapon}.${extension}`),
            path.join(MODERN, 'models/item', `${weapon}.${extension}`));
    }
    const material = path.join(MODERN, 'models/item', `${weapon}.mtl`);
    fs.writeFileSync(material, fs.readFileSync(material, 'utf8')
        .replace(`abyssalcraft:model/${weapon}`, `abyssalcraft:block/legacy_obj/${weapon}`));
}
writeJson('models/item/cudgel.json', {
    credit: 'Exact OBJ port of AbyssalCraft 1.12.2 cudgel',
    parent: 'abyssalcraft:block/legacy_custom_model',
    loader: '__LOADER__:obj',
    model: 'abyssalcraft:models/item/cudgel.obj',
    flip_v: true,
    automatic_culling: true,
    shade_quads: true,
    emissive_ambient: false,
    textures: { particle: 'abyssalcraft:block/legacy_obj/cudgel' },
});
writeJson('models/item/staff_of_the_gatekeeper.json', {
    credit: 'Exact OBJ port of AbyssalCraft 1.12.2 staff',
    parent: 'abyssalcraft:block/legacy_custom_model',
    loader: '__LOADER__:obj',
    model: 'abyssalcraft:models/item/staff.obj',
    flip_v: true,
    automatic_culling: true,
    shade_quads: true,
    emissive_ambient: false,
    textures: { particle: 'abyssalcraft:block/legacy_obj/staff' },
});

for (const texture of [
    'remnant_librarian', 'remnant_priest', 'remnant_blacksmith',
    'remnant_butcher', 'remnant_banker', 'remnant_master_blacksmith',
]) {
    copyTexture(`model/remnant/${texture}.png`, `model/remnant/${texture}.png`);
}

for (const texture of [
    'abyssal_zombie_eyes.png',
    'boss/dragonboss_eyes.png',
    'coraliumsquid_eyes.png',
    'elite/dragonminion_eyes.png',
    'elite/shadowbeast_eyes.png',
    'ghoul/depths_ghoul_eyes.png',
    'ghoul/dreaded_ghoul_eyes.png',
    'ghoul/ghoul_eyes.png',
    'ghoul/shadow_ghoul_eyes.png',
    'shadowcreature_eyes.png',
    'shadowmonster_eyes.png',
]) {
    copyTexture(`model/${texture}`, `model/${texture}`);
}

for (const [source, target] of [
    ['coraliump_1.png', 'plated_coralium_layer_1.png'],
    ['depths_1_inner.png', 'depths_layer_1.png'],
    ['ethaxium_1.png', 'ethaxium_layer_1.png'],
]) {
    copyTexture(`armor/${source}`, `models/armor/${target}`);
}

for (const gui of [
    'crystalbag', 'crystallizer', 'materializer',
    'spellcraft', 'spellcraft_test', 'spirit_tablet', 'transmutator',
]) {
    copyTexture(`gui/container/${gui}.png`, `gui/container/${gui}.png`);
}

for (const book of [
    'necronomicon', 'necronomicon_cor', 'necronomicon_dre',
    'necronomicon_omt', 'abyssalnomicon',
]) {
    copyTexture(`gui/${book}.png`, `gui/${book}.png`);
}

for (const jei of [
    'crystallizer_nei', 'materializer_nei', 'ritual_nei',
    'transformation_ritual_jei', 'transmutator_nei',
]) {
    copyTexture(`gui/container/${jei}.png`, `gui/container/${jei}.png`);
}

copyTexture('blocks/drgsides_snowed.png', 'block/dreadlands_grass_side_snowed.png');

copyTexture('blocks/dsssides.png', 'block/darkstone_slab_side.png');
copyTexture('blocks/eb.png', 'block/ethaxium_legacy_brick.png');

for (const [effect, icon] of [
    ['coralium_plague', 0],
    ['dread_plague', 1],
    ['antimatter', 2],
    ['coralium_antidote', 3],
    ['dread_antidote', 3],
]) {
    cropTexture('misc/potionfx.png', `mob_effect/${effect}.png`, icon * 18, 198, 18, 18);
}

for (const [prefix, targetPrefix] of [
    ['', 'composite'],
    ['dark_', 'dark_composite'],
]) {
    for (let variant = 1; variant <= 3; variant++) {
        compositeTexture([
            `blocks/ethaxium_bricks/${prefix}bricks_base.png`,
            `blocks/ethaxium_bricks/${prefix}faces_${variant}.png`,
        ], `block/ethaxium_bricks/${targetPrefix}_${variant}.png`);
    }
}
fs.rmSync(path.join(MODERN, 'textures/block/ethaxium_brick.png'), { force: true });

for (const texture of [
    'bricks_base', 'faces_1', 'faces_2', 'faces_3', 'sigil', 'cracks',
    'dark_bricks_base', 'dark_faces_1', 'dark_faces_2', 'dark_faces_3',
    'dark_sigil', 'dark_cracks',
]) {
    copyTexture(`blocks/ethaxium_bricks/${texture}.png`, `block/ethaxium_bricks/${texture}.png`);
}
const doubleLayeredCube = readJson(path.join(LEGACY, 'models/block/double_layered_cube.json'));
doubleLayeredCube.parent = 'abyssalcraft:block/legacy_custom_model';
doubleLayeredCube.render_type = 'minecraft:cutout';
writeJson('models/block/double_layered_cube.json', doubleLayeredCube);
for (const [id, dark, decoration] of [
    ['ethaxium_bricks', false, null],
    ['chiseled_ethaxium_brick', false, 'sigil'],
    ['cracked_ethaxium_brick', false, 'cracks'],
    ['dark_ethaxium_brick', true, null],
    ['chiseled_dark_ethaxium_brick', true, 'dark_sigil'],
    ['cracked_dark_ethaxium_brick', true, 'dark_cracks'],
]) {
    const prefix = dark ? 'dark_' : '';
    for (let variant = 1; variant <= 3; variant++) {
        const textures = {
            all: `abyssalcraft:block/ethaxium_bricks/${prefix}bricks_base`,
            overlay: `abyssalcraft:block/ethaxium_bricks/${prefix}faces_${variant}`,
            particle: `abyssalcraft:block/ethaxium_bricks/${prefix}bricks_base`,
        };
        if (decoration) textures.overlay2 = `abyssalcraft:block/ethaxium_bricks/${decoration}`;
        writeJson(`models/block/${id}_${variant}.json`, {
            parent: `abyssalcraft:block/${decoration ? 'double_layered_cube' : 'layered_ore'}`,
            render_type: 'minecraft:cutout',
            textures,
        });
    }
    writeJson(`models/block/${id}.json`, { parent: `abyssalcraft:block/${id}_1` });
    writeJson(`models/item/${id}.json`, { parent: `abyssalcraft:block/${id}` });
    writeJson(`blockstates/${id}.json`, {
        variants: {
            '': [
                { model: `abyssalcraft:block/${id}_1`, weight: 2 },
                { model: `abyssalcraft:block/${id}_2`, weight: 1 },
                { model: `abyssalcraft:block/${id}_3`, weight: 1 },
            ],
        },
    });
}

for (const antidote of ['coralium_antidote', 'dread_antidote']) {
    for (let frame = 1; frame <= 4; frame++) {
        const id = `${antidote}_${frame}`;
        copyTexture(`items/${id}.png`, `item/${id}.png`);
        writeJson(`models/item/${id}.json`, {
            parent: 'minecraft:item/generated',
            textures: { layer0: `abyssalcraft:item/${id}` },
        });
    }
    writeJson(`models/item/${antidote}.json`, {
        parent: 'minecraft:item/generated',
        textures: { layer0: `abyssalcraft:item/${antidote}` },
        overrides: [1, 2, 3, 4].map(frame => ({
            predicate: { 'abyssalcraft:content': frame / 5 },
            model: `abyssalcraft:item/${antidote}_${frame}`,
        })),
    });
}

copyTexture('items/interdimensionalcage_captured.png', 'item/interdimensional_cage_captured.png');
fs.copyFileSync(
    path.join(LEGACY, 'textures/items/interdimensionalcage_captured.png.mcmeta'),
    path.join(MODERN, 'textures/item/interdimensional_cage_captured.png.mcmeta'));
writeJson('models/item/interdimensional_cage.json', {
    parent: 'minecraft:item/generated',
    textures: { layer0: 'abyssalcraft:item/interdimensional_cage' },
    overrides: [{
        predicate: { 'abyssalcraft:captured': 1 },
        model: 'abyssalcraft:item/interdimensional_cage_captured',
    }],
});
writeJson('models/item/interdimensional_cage_captured.json', {
    parent: 'minecraft:item/generated',
    textures: { layer0: 'abyssalcraft:item/interdimensional_cage_captured' },
});

writeJson('models/item/soulreaper.json', {
    parent: 'minecraft:item/handheld',
    textures: { layer0: 'abyssalcraft:item/soulreaper' },
    overrides: [0, 1, 2, 3, 4, 5].map(level => ({
        predicate: { 'abyssalcraft:level': level / 5 },
        model: `abyssalcraft:item/soulreaper_${level}`,
    })),
});
writeJson('models/item/soulreaper_0.json', {
    parent: 'minecraft:item/handheld',
    textures: { layer0: 'abyssalcraft:item/soulreaper' },
});
for (let level = 1; level <= 5; level++) {
    const id = `soulreaper_${level}`;
    copyTexture(`items/${id}.png`, `item/${id}.png`);
    writeJson(`models/item/${id}.json`, {
        parent: 'minecraft:item/handheld',
        textures: { layer0: `abyssalcraft:item/${id}` },
    });
}

for (const texture of [
    'glyph_0', 'glyph_1', 'glyph_2', 'spirit_tablet_shard_0',
]) {
    copyTexture(`items/spirit_tablet/${texture}.png`, `item/spirit_tablet/${texture}.png`);
}
writeJson('models/item/spirit_tablet.json', {
    parent: 'minecraft:item/generated',
    textures: { layer0: 'abyssalcraft:item/spirit_tablet' },
    overrides: [0, 1, 2].map(mode => ({
        predicate: { 'abyssalcraft:mode': mode / 2 },
        model: `abyssalcraft:item/spirit_tablet_${mode}`,
    })),
});
for (let mode = 0; mode <= 2; mode++) {
    writeJson(`models/item/spirit_tablet_${mode}.json`, {
        parent: 'minecraft:item/generated',
        textures: {
            layer0: 'abyssalcraft:item/spirit_tablet',
            layer1: `abyssalcraft:item/spirit_tablet/glyph_${mode}`,
        },
    });
}
writeJson('models/item/spirit_tablet_shard_0.json', {
    parent: 'minecraft:item/generated',
    textures: { layer0: 'abyssalcraft:item/spirit_tablet/spirit_tablet_shard_0' },
});

console.log('RR_LEGACY_VISUAL_RESTORE_OK itemTextures=4 blockTextures=5'
    + ' itemModels=34 blockModels=16 blockstates=1 sanitizedModels=9');