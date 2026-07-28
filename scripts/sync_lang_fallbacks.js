const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..');
const LANGUAGES = [
    'en_us', 'es_es', 'fr_fr', 'ja_jp', 'ko_kr', 'ru_ru', 'zh_cn', 'zh_tw',
];
const SPECIAL_WORDS = new Map([
    ['abyssalcraft', 'AbyssalCraft'],
    ['abyssalnite', 'Abyssalnite'],
    ['coralium', 'Coralium'],
    ['cthulhu', 'Cthulhu'],
    ['dreadium', 'Dreadium'],
    ['dreadlands', 'Dreadlands'],
    ['ethaxium', 'Ethaxium'],
    ['jzahar', "J'zahar"],
    ['nyarlathotep', 'Nyarlathotep'],
    ['omothol', 'Omothol'],
    ['shoggoth', 'Shoggoth'],
]);
const ZH_MATERIALS = new Map([
    ['abyssalnite', '渊素'], ['alumina', '氧化铝'], ['aluminium', '铝'],
    ['beryl', '绿柱石'], ['beryllium', '铍'], ['blaze', '烈焰'], ['calcium', '钙'],
    ['carbon', '碳'], ['copper', '铜'], ['coralium', '珊瑚'], ['dreadium', '恐惧素'],
    ['gold', '金'], ['hydrogen', '氢'], ['iron', '铁'], ['magnesia', '氧化镁'],
    ['magnesium', '镁'], ['methane', '甲烷'], ['nitrate', '硝酸盐'], ['nitrogen', '氮'],
    ['oxygen', '氧'], ['phosphorus', '磷'], ['potassium', '钾'], ['redstone', '红石'],
    ['silica', '二氧化硅'], ['silicon', '硅'], ['sulfur', '硫'], ['tin', '锡'], ['zinc', '锌'],
]);
const ZH_WORDS = new Map([
    ['abyssal', '深渊'], ['abyssalnite', '渊素'], ['brick', '砖'], ['bricks', '砖块'],
    ['chiseled', '錾制'], ['cobblestone', '圆石'], ['coralium', '珊瑚'], ['cracked', '裂纹'],
    ['dark', '暗黑'], ['darklands', '恐惧之地'], ['darkstone', '暗黑石'], ['depths', '深渊'],
    ['dreadium', '恐惧素'], ['dreadstone', '恐惧石'], ['dreadwood', '恐惧木'],
    ['elysian', '乐土'], ['ethaxium', '魂素'], ['fence', '栅栏'], ['fire', '火焰'],
    ['glowing', '发光'], ['leaves', '树叶'], ['log', '原木'], ['mimic', '拟态'],
    ['monolith', '巨石'], ['oak', '橡木'], ['omothol', '奥穆索'], ['pillar', '柱'],
    ['planks', '木板'], ['plated', '镀层'], ['refined', '精炼'], ['samurai', '武士'],
    ['slab', '台阶'], ['stairs', '楼梯'], ['stone', '石'], ['wall', '墙'],
]);

function displayName(key) {
    const match = /^(?:block|item)\.abyssalcraft\.([a-z0-9_]+)$/.exec(key);
    if (!match) throw new Error(`Unsupported translation key: ${key}`);
    return match[1].split('_').map(word => {
        const special = SPECIAL_WORDS.get(word);
        if (special) return special;
        if (word === 'odb' || word === 'pe') return word.toUpperCase();
        return word[0].toUpperCase() + word.slice(1);
    }).join(' ')
        .replace('Shub Niggurath', 'Shub-Niggurath')
        .replace('Yog Sothoth', 'Yog-Sothoth');
}

function chineseName(key) {
    const match = /^(?:block|item)\.abyssalcraft\.([a-z0-9_]+)$/.exec(key);
    if (!match) throw new Error(`Unsupported translation key: ${key}`);
    const id = match[1];
    if (id === 'monolith_stone') return '巨石';
    if (id === 'mimic_fire') return '拟态火焰';
    if (id.endsWith('_crystal_cluster')) {
        const material = id.slice(0, -'_crystal_cluster'.length);
        return `${ZH_MATERIALS.get(material) || material}晶簇`;
    }
    for (const [prefix, suffix] of [
        ['crystal_fragment_', '水晶碎屑'], ['crystal_shard_', '水晶碎片'], ['crystal_', '水晶'],
    ]) {
        if (id.startsWith(prefix)) {
            const material = id.slice(prefix.length);
            return `${ZH_MATERIALS.get(material) || material}${suffix}`;
        }
    }
    const armor = id.match(/^(.*)_(helmet|chestplate|leggings|boots)$/);
    if (armor) {
        const armorParts = armor[1].split('_').map(word => ZH_WORDS.get(word) || word).join('');
        const armorType = { helmet: '头盔', chestplate: '胸甲', leggings: '护腿', boots: '靴子' }[armor[2]];
        return `${armorParts}${armorType}`;
    }
    return id.split('_').map(word => ZH_WORDS.get(word) || ZH_MATERIALS.get(word) || word).join('');
}

const keys = [...new Set(fs.readFileSync(0, 'utf8').split(/\r?\n/)
    .map(line => line.trim()).filter(Boolean))].sort();
if (keys.length === 0) throw new Error('No translation keys were provided on stdin');

if (process.argv[2] === '--zh-cn') {
    const file = path.join(ROOT, 'src/main/resources/assets/abyssalcraft/lang/zh_cn.json');
    const translations = JSON.parse(fs.readFileSync(file, 'utf8').replace(/^\uFEFF/, ''));
    for (const key of keys) translations[key] = chineseName(key);
    fs.writeFileSync(file, `${JSON.stringify(translations, null, 2)}\n`, 'utf8');
    console.log(`RR_LANG_ZH_CN_SYNC keys=${keys.length}`);
    process.exit(0);
}

for (const language of LANGUAGES) {
    const file = path.join(ROOT, 'src/main/resources/assets/abyssalcraft/lang', `${language}.json`);
    const translations = JSON.parse(fs.readFileSync(file, 'utf8').replace(/^\uFEFF/, ''));
    let added = 0;
    for (const key of keys) {
        if (Object.hasOwn(translations, key)) continue;
        translations[key] = displayName(key);
        added++;
    }
    fs.writeFileSync(file, `${JSON.stringify(translations, null, 2)}\n`, 'utf8');
    console.log(`RR_LANG_FALLBACK_SYNC language=${language} added=${added}`);
}
