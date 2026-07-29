const LANGUAGES = [
    'en_us', 'es_es', 'fr_fr', 'ja_jp', 'ko_kr', 'ru_ru', 'zh_cn', 'zh_tw',
];

// Values are ordered by LANGUAGES. These keys must not be inferred from an equal legacy English value:
// several legacy entity names collide with unrelated blocks such as Shoggoth Ooze.
const ENTITY_NAME_ROWS = [
    ['pilot_mob', 'Pilot Mob', 'Criatura piloto', 'Créature pilote', 'パイロットモブ', '파일럿 몹', 'Пилотный моб', '先导生物', '先導生物'],
    ['antizombie', 'Anti-Zombie', 'Zombi de antimateria', 'Zombie d’antimatière', '反物質ゾンビ', '반물질 좀비', 'Зомби из антиматерии', '反物质僵尸', '反物質僵屍'],
    ['antiabyssalzombie', 'Anti-Abyssal Zombie', 'Zombi abisal de antimateria', 'Zombie abyssal d’antimatière', '反物質アビサルゾンビ', '반물질 심연 좀비', 'Абиссальный зомби из антиматерии', '反物质深渊僵尸', '反物質深淵僵屍'],
    ['anticreeper', 'Anti-Creeper', 'Creeper de antimateria', 'Creeper d’antimatière', '反物質クリーパー', '반물질 크리퍼', 'Крипер из антиматерии', '反物质苦力怕', '反物質苦力怕'],
    ['antiskeleton', 'Anti-Skeleton', 'Esqueleto de antimateria', 'Squelette d’antimatière', '反物質スケルトン', '반물질 스켈레톤', 'Скелет из антиматерии', '反物质骷髅', '反物質骷髏'],
    ['antispider', 'Anti-Spider', 'Araña de antimateria', 'Araignée d’antimatière', '反物質クモ', '반물질 거미', 'Паук из антиматерии', '反物质蜘蛛', '反物質蜘蛛'],
    ['antighoul', 'Anti-Ghoul', 'Necrófago de antimateria', 'Goule d’antimatière', '反物質グール', '반물질 구울', 'Гуль из антиматерии', '反物质食尸鬼', '反物質食屍鬼'],
    ['antiplayer', 'Anti-Player', 'Jugador de antimateria', 'Joueur d’antimatière', '反物質プレイヤー', '반물질 플레이어', 'Игрок из антиматерии', '反物质玩家', '反物質玩家'],
    ['anticow', 'Anti-Cow', 'Vaca de antimateria', 'Vache d’antimatière', '反物質ウシ', '반물질 소', 'Корова из антиматерии', '反物质牛', '反物質牛'],
    ['antipig', 'Anti-Pig', 'Cerdo de antimateria', 'Cochon d’antimatière', '反物質ブタ', '반물질 돼지', 'Свинья из антиматерии', '反物质猪', '反物質豬'],
    ['antichicken', 'Anti-Chicken', 'Gallina de antimateria', 'Poule d’antimatière', '反物質ニワトリ', '반물질 닭', 'Курица из антиматерии', '反物质鸡', '反物質雞'],
    ['antibat', 'Anti-Bat', 'Murciélago de antimateria', 'Chauve-souris d’antimatière', '反物質コウモリ', '반물질 박쥐', 'Летучая мышь из антиматерии', '反物质蝙蝠', '反物質蝙蝠'],
    ['abyssalzombie', 'Abyssal Zombie', 'Zombi abisal', 'Zombie abyssal', 'アビサルゾンビ', '심연 좀비', 'Абиссальный зомби', '深渊僵尸', '深淵僵屍'],
    ['coraliumsquid', 'Coralium-Infested Squid', 'Calamar infestado de coralium', 'Calmar infesté de coralium', 'コラリウムに侵されたイカ', '코랄륨에 감염된 오징어', 'Кальмар, заражённый коралиумом', '珊瑚侵染鱿鱼', '珊瑚侵染魷魚'],
    ['dreadling', 'Dreadling', 'Diablillo del terror', 'Diablotin d’effroi', 'ドレッドリング', '드레들링', 'Детёныш ужаса', '惧魔', '懼魔'],
    ['dreadspawn', 'Dread Spawn', 'Engendro del terror', 'Progéniture d’effroi', 'ドレッドスポーン', '드레드 스폰', 'Порождение ужаса', '恐惧孳生体', '恐懼孳生體'],
    ['greaterdreadspawn', 'Greater Dread Spawn', 'Engendro mayor del terror', 'Grande progéniture d’effroi', 'グレータードレッドスポーン', '상급 드레드 스폰', 'Великое порождение ужаса', '大型恐惧孳生体', '大型恐懼孳生體'],
    ['lesserdreadbeast', 'Lesser Dreadbeast', 'Bestia menor del terror', 'Bête d’effroi mineure', 'レッサードレッドビースト', '하급 드레드 비스트', 'Малый зверь ужаса', '小型恐惧野兽', '小型恐懼野獸'],
    ['shadowcreature', 'Shadow Creature', 'Criatura de las sombras', 'Créature de l’ombre', 'シャドウクリーチャー', '그림자 생물', 'Теневое существо', '暗影生物', '暗影生物'],
    ['shadowmonster', 'Shadow Monster', 'Monstruo de las sombras', 'Monstre de l’ombre', 'シャドウモンスター', '그림자 괴물', 'Теневой монстр', '暗影怪物', '暗影怪物'],
    ['shadowbeast', 'Shadow Beast', 'Bestia de las sombras', 'Bête de l’ombre', 'シャドウビースト', '그림자 야수', 'Теневой зверь', '暗影野兽', '暗影野獸'],
    ['ghoul', 'Ghoul', 'Necrófago', 'Goule', 'グール', '구울', 'Гуль', '食尸鬼', '食屍鬼'],
    ['depths_ghoul', 'Depths Ghoul', 'Necrófago de las profundidades', 'Goule des profondeurs', 'デプスグール', '심연 구울', 'Гуль глубин', '深渊食尸鬼', '深淵食屍鬼'],
    ['dreaded_ghoul', 'Dreaded Ghoul', 'Necrófago del terror', 'Goule d’effroi', 'ドレッドグール', '공포 구울', 'Гуль ужаса', '恐惧食尸鬼', '恐懼食屍鬼'],
    ['omothol_ghoul', 'Omothol Ghoul', 'Necrófago de Omothol', 'Goule d’Omothol', 'オモソールグール', '오모솔 구울', 'Гуль Омотола', '奥穆索食尸鬼', '奧穆索食屍鬼'],
    ['shadow_ghoul', 'Shadow Ghoul', 'Necrófago de las sombras', 'Goule de l’ombre', 'シャドウグール', '그림자 구울', 'Теневой гуль', '暗影食尸鬼', '暗影食屍鬼'],
    ['lesser_shoggoth', 'Lesser Shoggoth', 'Shoggoth menor', 'Shoggoth inférieur', 'レッサーショゴス', '하급 쇼고스', 'Малый шоггот', '小型修格斯', '小型修格斯'],
    ['shoggoth', 'Shoggoth', 'Shoggoth', 'Shoggoth', 'ショゴス', '쇼고스', 'Шоггот', '修格斯', '修格斯'],
    ['greater_shoggoth', 'Greater Shoggoth', 'Shoggoth mayor', 'Shoggoth supérieur', 'グレーターショゴス', '상급 쇼고스', 'Большой шоггот', '大型修格斯', '大型修格斯'],
    ['acidprojectile', 'Acid Projectile', 'Proyectil ácido', 'Projectile acide', '酸液弾', '산성 투사체', 'Кислотный снаряд', '酸液弹', '酸液彈'],
    ['dreadslug', 'Dread Slug', 'Babosa del terror', 'Limace d’effroi', 'ドレッドスラッグ', '공포 민달팽이', 'Слизень ужаса', '恐惧蛞蝓', '恐懼蛞蝓'],
    ['inkprojectile', 'Ink Projectile', 'Proyectil de tinta', 'Projectile d’encre', '墨汁弾', '먹물 투사체', 'Чернильный снаряд', '墨汁弹', '墨汁彈'],
    ['coraliumarrow', 'Coralium Arrow', 'Flecha de coralium', 'Flèche de coralium', 'コラリウムの矢', '코랄륨 화살', 'Коралиумовая стрела', '珊瑚箭', '珊瑚箭'],
    ['dreadedcharge', 'Dreaded Charge', 'Carga del terror', 'Charge d’effroi', 'ドレッドチャージ', '공포 에너지탄', 'Заряд ужаса', '恐惧能量弹', '恐懼能量彈'],
    ['blackhole', 'Black Hole', 'Agujero negro', 'Trou noir', 'ブラックホール', '블랙홀', 'Чёрная дыра', '黑洞', '黑洞'],
    ['implosion', 'Implosion', 'Implosión', 'Implosion', '爆縮', '내파', 'Имплозия', '内爆', '內爆'],
    ['primedodb', 'Primed Oblivion Deathbomb', 'Bomba mortal del olvido activada', 'Bombe mortelle de l’oubli amorcée', '起爆済みオブリビオン・デスボム', '점화된 망각의 죽음 폭탄', 'Взведённая бомба смерти забвения', '已激活的湮灭弹', '已啟動的湮滅彈'],
    ['compasstentacle', 'Compass Tentacle', 'Tentáculo de brújula', 'Tentacule-boussole', 'コンパスの触手', '나침반 촉수', 'Щупальце-компас', '指南针触手', '指南針觸手'],
    ['portal', 'Portal', 'Portal', 'Portail', 'ポータル', '차원문', 'Портал', '传送门', '傳送門'],
    ['singleportal', 'Single-Use Portal', 'Portal de un solo uso', 'Portail à usage unique', '使い切りポータル', '일회용 차원문', 'Одноразовый портал', '一次性传送门', '一次性傳送門'],
    ['spirititem', 'Spirit Item', 'Objeto espiritual', 'Objet spirituel', '魂のアイテム', '영혼 아이템', 'Одухотворённый предмет', '灵魂物品', '靈魂物品'],
    ['gatekeeperessence', 'Gatekeeper Essence', 'Esencia del Guardián', 'Essence du Gardien', '門番のエッセンス', '문지기의 정수', 'Эссенция Привратника', '守门人精华', '守門人精華'],
    ['dreadguard', 'Dreadguard', 'Guardián del terror', 'Gardien d’effroi', 'ドレッドガード', '공포 수호자', 'Страж ужаса', '恐惧守卫', '恐懼守衛'],
    ['remnant', 'Remnant', 'Remanente', 'Vestige', 'レムナント', '잔존자', 'Остаток', '残存者', '殘存者'],
    ['chagaroth', "Cha'garoth, the Dreadbeast", "Cha'garoth, la Bestia del Terror", "Cha'garoth, la Bête d’effroi", '恐怖の獣チャガロス', '차가로스, 공포의 야수', "Ча'гарот, зверь ужаса", '恐惧野兽查伽洛斯', '恐懼野獸查加羅特'],
    ['shuboffspring', 'Dark Offspring', 'Descendencia oscura', 'Rejeton sombre', 'ダークオフスプリング', '어둠의 자손', 'Тёмный отпрыск', '黑暗幼崽', '黑暗幼崽'],
];

const DISPLAY_NAME_OVERRIDES = {
    en_us: {
        'item.abyssalcraft.eye_of_the_abyss': 'Eye of the Abyss',
        'item.abyssalcraft.essence_of_the_gatekeeper': 'Essence of the Gatekeeper',
        'item.abyssalcraft.staff_of_the_gatekeeper': 'Staff of the Gatekeeper',
        'item.abyssalcraft.chunk_of_coralium': 'Chunk of Coralium',
        'item.abyssalcraft.dreaded_shard_of_abyssalnite': 'Dreaded Shard of Abyssalnite',
        'item.abyssalcraft.shard_of_oblivion': 'Shard of Oblivion',
    },
    zh_cn: {
        'entity.abyssalcraft.antizombie': '反物质僵尸',
        'entity.abyssalcraft.antiabyssalzombie': '反物质深渊僵尸',
        'entity.abyssalcraft.anticreeper': '反物质苦力怕',
        'entity.abyssalcraft.antiskeleton': '反物质骷髅',
        'entity.abyssalcraft.antispider': '反物质蜘蛛',
        'entity.abyssalcraft.antighoul': '反物质食尸鬼',
        'entity.abyssalcraft.antiplayer': '反物质玩家',
        'entity.abyssalcraft.anticow': '反物质牛',
        'entity.abyssalcraft.antipig': '反物质猪',
        'entity.abyssalcraft.antichicken': '反物质鸡',
        'entity.abyssalcraft.antibat': '反物质蝙蝠',
        'item.abyssalcraft.anti_chicken': '反物质鸡肉',
    },
    zh_tw: {
        'entity.abyssalcraft.antizombie': '反物質僵屍',
        'entity.abyssalcraft.antiabyssalzombie': '反物質深淵僵屍',
        'entity.abyssalcraft.anticreeper': '反物質苦力怕',
        'entity.abyssalcraft.antiskeleton': '反物質骷髏',
        'entity.abyssalcraft.antispider': '反物質蜘蛛',
        'entity.abyssalcraft.antighoul': '反物質食屍鬼',
        'entity.abyssalcraft.antiplayer': '反物質玩家',
        'entity.abyssalcraft.anticow': '反物質牛',
        'entity.abyssalcraft.antipig': '反物質豬',
        'entity.abyssalcraft.antichicken': '反物質雞',
        'entity.abyssalcraft.antibat': '反物質蝙蝠',
        'item.abyssalcraft.anti_chicken': '反物質雞肉',
        'block.abyssalcraft.darklands_oak_door': '黑暗之地橡木門',
    },
};

const TERM_REPLACEMENTS = {
    zh_cn: [
        ['阴影', '暗影'], ['惧质', '恐惧素'], ['黑暗石', '暗黑石'], ['守门者', '守门人'],
        ['势能', 'PE'], ['位能', 'PE'], ['Potential Energy', 'PE'], ['潜能操纵器', 'PE操纵器'], ['能量收集器', 'PE收集器'], ['能量容器', 'PE容器'],
        ['能量基座', 'PE基座'], ['能量中继器', 'PE中继器'], ['能量沉积器', 'PE沉积器'],
        ['PE（PE）', 'PE'], ['活暗影', '活体暗影'],
    ],
    zh_tw: [
        ['陰影', '暗影'], ['懼質', '恐懼素'], ['黑暗石', '暗黑石'], ['守門者', '守門人'],
        ['勢能', 'PE'], ['位能', 'PE'], ['Potential Energy', 'PE'], ['潛能操縱器', 'PE操縱器'], ['能量收集器', 'PE收集器'], ['能量容器', 'PE容器'],
        ['能量基座', 'PE基座'], ['能量中繼器', 'PE中繼器'], ['能量沉積器', 'PE沉積器'],
        ['PE（PE）', 'PE'], ['活暗影', '活體暗影'],
    ],
};

const UI_TEXT_ROWS = [
    ['gui.abyssalcraft.necronomicon.back', 'Back', 'Atrás', 'Retour', '戻る', '뒤로', 'Назад', '返回', '返回'],
];

const IDENTICAL_TO_ENGLISH = {
    es_es: new Set([
        'item.abyssalcraft.necronomicon', 'item.abyssalcraft.abyssalnomicon',
        'block.abyssalcraft.mural', 'block.abyssalcraft.ethaxium',
        'entity.abyssalcraft.shoggoth', 'entity.abyssalcraft.portal',
    ]),
    fr_fr: new Set([
        'item.abyssalcraft.necronomicon', 'item.abyssalcraft.abyssalnomicon',
        'block.abyssalcraft.ethaxium', 'entity.abyssalcraft.shoggoth',
        'entity.abyssalcraft.implosion',
    ]),
};

const MIRRORED_NAME_PAIRS = [
    ['block.abyssalcraft.materializer', 'container.abyssalcraft.materializer'],
    ['block.abyssalcraft.crystallizer', 'container.abyssalcraft.crystallizer'],
    ['block.abyssalcraft.transmutator', 'container.abyssalcraft.transmutator'],
    ['block.abyssalcraft.research_table', 'container.abyssalcraft.research_table'],
    ['block.abyssalcraft.sequential_brewing_stand', 'container.abyssalcraft.sequential_brewing_stand'],
    ['block.abyssalcraft.crate', 'container.abyssalcraft.crate'],
    ['block.abyssalcraft.energycontainer', 'container.abyssalcraft.energy_container'],
    ['block.abyssalcraft.energydepositioner', 'container.abyssalcraft.energy_depositioner'],
    ['item.abyssalcraft.book_of_many_faces', 'container.abyssalcraft.book_of_many_faces'],
];

const ID_FAMILY_TERMS = {
    zh_cn: {
        dreadium: '恐惧素', darkstone: '暗黑石', darklands: '黑暗之地',
        gatekeeper: '守门人', shadow: '暗影',
    },
    zh_tw: {
        dreadium: '恐懼素', darkstone: '暗黑石', darklands: '黑暗之地',
        gatekeeper: '守門人', shadow: '暗影',
    },
};

const ID_FAMILY_EXCEPTIONS = new Set([
    'entity.abyssalcraft.shadowboss',
    'item.abyssalcraft.shadowboss_spawn_egg',
]);

module.exports = {
    LANGUAGES,
    ENTITY_NAME_ROWS,
    DISPLAY_NAME_OVERRIDES,
    TERM_REPLACEMENTS,
    IDENTICAL_TO_ENGLISH,
    MIRRORED_NAME_PAIRS,
    ID_FAMILY_TERMS,
    ID_FAMILY_EXCEPTIONS,
    UI_TEXT_ROWS,
};