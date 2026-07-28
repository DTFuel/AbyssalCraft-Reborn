const fs = require('fs');
const path = require('path');
const {
    LANGUAGES,
    ENTITY_NAME_ROWS,
    DISPLAY_NAME_OVERRIDES,
    TERM_REPLACEMENTS,
    MIRRORED_NAME_PAIRS,
    ID_FAMILY_TERMS,
} = require('./localization_name_contract');

const ROOT = path.resolve(__dirname, '..');
const MODERN_LANG = path.join(ROOT, 'src/main/resources/assets/abyssalcraft/lang');
const LEGACY_LANG = path.join(ROOT,
    'docs/AbyssalCraft-1.12.2/src/main/resources/assets/abyssalcraft/lang');
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
    ['dark', '暗黑'], ['darklands', '黑暗之地'], ['darkstone', '暗黑石'], ['depths', '深渊'],
    ['dreadium', '恐惧素'], ['dreadstone', '恐惧石'], ['dreadwood', '恐惧木'],
    ['elysian', '乐土'], ['ethaxium', '魂素'], ['fence', '栅栏'], ['fire', '火焰'],
    ['glowing', '发光'], ['leaves', '树叶'], ['log', '原木'], ['mimic', '拟态'],
    ['monolith', '巨石'], ['oak', '橡木'], ['omothol', '奥穆索'], ['pillar', '柱'],
    ['planks', '木板'], ['plated', '镀层'], ['refined', '精炼'], ['samurai', '武士'],
    ['slab', '台阶'], ['stairs', '楼梯'], ['stone', '石'], ['wall', '墙'],
]);
const ZH_DEITIES = new Map([
    ['azathoth', '阿撒托斯'], ['cthulhu', '克苏鲁'], ['hastur', '哈斯塔'],
    ['jzahar', '扎哈尔'], ['nyarlathotep', '奈亚拉托提普'],
    ['shub_niggurath', '莎布·尼古拉丝'], ['yog_sothoth', '犹格·索托斯'],
]);
const ZH_ENTITIES = new Map([
    ['acidprojectile', '酸液弹'], ['antiabyssalzombie', '反深渊僵尸'], ['antibat', '反蝙蝠'],
    ['antichicken', '反鸡'], ['anticow', '反牛'], ['anticreeper', '反苦力怕'],
    ['antighoul', '反食尸鬼'], ['antipig', '反猪'], ['antiplayer', '反玩家'],
    ['antiskeleton', '反骷髅'], ['antispider', '反蜘蛛'], ['antizombie', '反僵尸'],
    ['blackhole', '黑洞'], ['chagaroth', '查伽洛斯'], ['chagarothfist', '查伽洛斯之拳'],
    ['chagarothspawn', '查伽洛斯幼体'], ['compasstentacle', '指南针触手'],
    ['coraliumarrow', '珊瑚箭'], ['depths_ghoul', '深渊食尸鬼'], ['dragonboss', '龙族首领'],
    ['dragonminion', '龙族仆从'], ['dreaded_ghoul', '恐惧食尸鬼'],
    ['dreadedcharge', '恐惧能量弹'], ['dreadguard', '恐惧守卫'], ['dreadslug', '恐惧蛞蝓'],
    ['evil_chicken', '邪恶鸡'], ['evil_cow', '邪恶牛'], ['evil_pig', '邪恶猪'],
    ['evil_sheep', '邪恶羊'], ['gatekeeperessence', '守门人精华'],
    ['ghoul', '食尸鬼'], ['greater_shoggoth', '大型修格斯'], ['gskeleton', '骷髅巨人'],
    ['implosion', '内爆'], ['inkprojectile', '墨汁弹'], ['jzahar', '扎哈尔'],
    ['jzaharminion', '守门人仆从'], ['lesser_shoggoth', '小型修格斯'],
    ['omothol_ghoul', '奥穆索食尸鬼'], ['pilot_mob', '先导生物'], ['portal', '传送门'],
    ['remnant', '残魂'], ['shadow_ghoul', '暗影食尸鬼'], ['shadowboss', '萨克托斯'],
    ['shoggoth', '修格斯'], ['shuboffspring', '莎布之嗣'], ['singleportal', '单向传送门'],
    ['spirititem', '灵魂物品'],
]);
const ZH_EXPLICIT = new Map([
    ['container.abyssalcraft.spirit_tablet', '灵魂石板过滤器'],
    ['gui.abyssalcraft.necronomicon.abyssal_wasteland.text', '第一个深渊维度：永恒暮色下由深渊石构成的荒芜大地。珊瑚侵染了沼泽与湖泊，暗影生物四处游荡。通过珊瑚浸染石构成的传送门可抵达此地。'],
    ['gui.abyssalcraft.necronomicon.ac.text', 'AbyssalCraft 将你带入旧日支配者的神话：穿越腐化维度，施行禁忌仪式，并追寻势能。本书将指引你踏上深渊之路。'],
    ['gui.abyssalcraft.necronomicon.content.active', '启用 %1$s | 负责人=%2$s | 引用=%3$s'],
    ['gui.abyssalcraft.necronomicon.content.blocked', '阻塞 %1$s | 负责人=%2$s | 引用=%3$s | %4$s'],
    ['gui.abyssalcraft.necronomicon.content.missing', '缺失 %1$s | 负责人=%2$s | 引用=%3$s | %4$s'],
    ['gui.abyssalcraft.necronomicon.dark_realm.text', '最深处的深渊：由暗黑石构成的无光虚空，末日先驱萨克托斯在此等候。鲜有人能从这里归来，归来者也无一如初。这里是深渊之路的终点。'],
    ['gui.abyssalcraft.necronomicon.dreadlands.text', '由恐惧瘟疫催生的腐化红色荒原，恐惧素矿埋藏其中，恐惧守卫四处巡逻。就连脚下的恐惧石也浸透死亡。只有准备充分的人才能在这里生存。'],
    ['gui.abyssalcraft.necronomicon.goo.text', '旧日支配者是沉睡在现实帷幕之外的远古神祇。它们的影响通过深渊维度渗入世界。你将首先面对克苏鲁，而在其身后还有外神等待着。'],
    ['gui.abyssalcraft.necronomicon.intro.text', '这本古老典籍汇集了关于深渊维度、其中居民以及束缚它们的黑暗仪式之知识。翻阅书页即可学习，但务必谨记，知识总有代价。'],
    ['gui.abyssalcraft.necronomicon.knowledge.text', '亲历深渊即可获得知识：击杀其中的生物、踏足各个维度，并在结晶器中研究遗物。随着理解加深，本书将揭示更多内容。'],
    ['gui.abyssalcraft.necronomicon.omothol.text', '守门人的领域：苍白虚空中漂浮着破碎石岛。深渊守门人扎哈尔居住于此，守卫通往前方的道路。这是审判与可怖力量之地。'],
    ['gui.abyssalcraft.spirit_tablet.components', '匹配数据'],
    ['gui.abyssalcraft.spirit_tablet.subtypes', '忽略损伤值'],
    ['itemGroup.abyssalcraft.blocks', 'AbyssalCraft：方块'],
    ['itemGroup.abyssalcraft.combat', 'AbyssalCraft：战斗用品'],
    ['itemGroup.abyssalcraft.crystals', 'AbyssalCraft：晶体'],
    ['itemGroup.abyssalcraft.decorations', 'AbyssalCraft：装饰方块'],
    ['itemGroup.abyssalcraft.food', 'AbyssalCraft：食物'],
    ['itemGroup.abyssalcraft.items', 'AbyssalCraft：物品'],
    ['itemGroup.abyssalcraft.materials', 'AbyssalCraft：材料'],
    ['itemGroup.abyssalcraft.ores', 'AbyssalCraft：矿石'],
    ['itemGroup.abyssalcraft.tools', 'AbyssalCraft：工具'],
    ['jei.abyssalcraft.anvil_forging', '铁砧锻造'],
    ['jei.abyssalcraft.anvil_price', '花费：%s'],
    ['message.abyssalcraft.ritual.no_energy', '死灵之书没有足够的势能来完成此仪式。'],
    ['message.abyssalcraft.ritual.no_ritual', '没有与这些祭品匹配的仪式。'],
    ['message.abyssalcraft.ritual.no_structure', '仪式需要在祭坛周围布置八座基座。'],
    ['message.abyssalcraft.ritual.success', '仪式已完成。'],
    ['message.abyssalcraft.spell.fizzle', '法术消散了：范围内没有目标，或势能不足。'],
    ['message.abyssalcraft.spirit_altar.cleared', '已清除 %s 个已配置容器'],
    ['message.abyssalcraft.spirit_altar.disabled', '已禁用 %s 个已配置容器'],
    ['message.abyssalcraft.spirit_altar.enabled', '已启用 %s 个已配置容器'],
    ['message.abyssalcraft.spirit_altar.routes', '已连接路线：%s'],
    ['message.abyssalcraft.spirit_altar.scanned', '找到 %s 个已配置容器'],
    ['message.abyssalcraft.spirit_tablet.applied', '已应用传输配置'],
    ['message.abyssalcraft.spirit_tablet.bad_route', '应用前请至少记录一个目的地'],
    ['message.abyssalcraft.spirit_tablet.cleared', '已清除传输配置'],
    ['message.abyssalcraft.spirit_tablet.mode', '灵魂石板模式：%s'],
    ['message.abyssalcraft.spirit_tablet.no_destination', '记录的目的地不可用'],
    ['message.abyssalcraft.spirit_tablet.no_host', '该方块无法保存传输配置'],
    ['message.abyssalcraft.spirit_tablet.no_inventory', '该方块的这一面没有容器'],
    ['message.abyssalcraft.spirit_tablet.path_added', '已将容器加入传输路径'],
    ['message.abyssalcraft.spirit_tablet.waypoint_added', '已将路径点加入传输路径'],
    ['necronomicon.information.ritualcharms', '仪式护符'],
    ['necronomicon.information.upgradingpe', '升级势能方块'],
    ['necronomicon.text.crafting.portalanchor.1', '拥有银之钥后，你无需再举行启门仪式来开启传送门。改为制作传送门锚，并将其放置在需要的位置。'],
    ['necronomicon.text.crafting.portalanchor.2', '你现在也能举行一种仪式来制造不受限制的传送门锚，打破迫使它们只能在相连维度间开启传送门的以太锁链。'],
    ['necronomicon.text.machines.3', '结晶器用于使元素结晶，从物质中分离出构成它们的基础元素或分子。任何恐惧材料、恐惧碎片以及任意结晶元素（完整晶体或晶体碎片）均可作为燃料。'],
    ['necronomicon.text.peupgrading.1', '如果势能产出仍然太慢，可以在铁砧上使用维度之戒升级各种势能处理方块，以提升它们的属性。'],
    ['necronomicon.text.peupgrading.2', '基础戒指由金锭和烈焰粉制成，之后可通过仪式逐级升级。'],
    ['necronomicon.text.peupgrading.3', '每个升级后的方块都会显示代表其等级的戒指。'],
    ['necronomicon.text.ritualcharms.1', '仪式护符能暂时增强势能操纵器的特定属性。基础护符可以清除已强化操纵器上的现有增幅效果，也可以直接破坏方块。添加任意类型的增幅器都会提高操纵器的基础势能产出，其中力量增幅器还会进一步提升产出。'],
    ['necronomicon.text.ritualcharms.2', '雕像被增幅后，基座上会显示代表该增幅器的符号，与护符上的符号相同。'],
    ['tooltip.abyssalcraft.book_tier', '等级 %s'],
    ['tooltip.abyssalcraft.spell', '法术：%s'],
    ['tooltip.abyssalcraft.spirit_tablet.mode', '模式：%s'],
    ['tooltip.abyssalcraft.spirit_tablet.route', '路径点：%s'],
    ['item.abyssalcraft.abyssal_ghoul_flesh', '深渊食尸鬼肉'],
    ['item.abyssalcraft.anti_chicken', '反鸡'],
    ['item.abyssalcraft.anti_ghoul_flesh', '食尸鬼反物质肉'],
    ['item.abyssalcraft.charcoal', '木炭'],
    ['item.abyssalcraft.chunk_of_coralium', '珊瑚块'],
    ['item.abyssalcraft.dreaded_ghoul_flesh', '恐惧食尸鬼肉'],
    ['item.abyssalcraft.dreaded_shard_of_abyssalnite', '恐惧渊素碎片'],
    ['item.abyssalcraft.ghoul_flesh', '食尸鬼肉'],
    ['item.abyssalcraft.pilot_crystal', '先导水晶'],
    ['item.abyssalcraft.pilot_fuel', '先导燃料'],
    ['item.abyssalcraft.shadow_ghoul_flesh', '暗影食尸鬼肉'],
    ['item.abyssalcraft.shadow_shard', '暗影碎片'],
    ['item.abyssalcraft.shard_of_oblivion', '湮灭碎片'],
    ['item.abyssalcraft.spell_staff', '法术权杖'],
    ['item.minecraft.potion.effect.antimatter', '反物质药水'],
]);
const CONTENT_LANGUAGES = ['es_es', 'fr_fr', 'ja_jp', 'ko_kr', 'ru_ru', 'zh_tw'];
const CONTENT_TERM_ROWS = [
    ['Abyssal Wasteland', 'Páramo Abisal', 'Terres désolées abyssales', '深淵の荒野', '심연의 황무지', 'Пустошь Бездны', '深淵荒原'],
    ['Abyssal Cobblestone', 'Adoquín abisal', 'Pierres abyssales', '深淵の丸石', '심연의 조약돌', 'Булыжник Бездны', '深淵鵝卵石'],
    ['Abyssal Stone', 'Piedra abisal', 'Pierre abyssale', '深淵石', '심연석', 'Камень Бездны', '深淵石'],
    ['Coralium Cobblestone', 'Adoquín de coralium', 'Pierres de coralium', 'コラリウムの丸石', '코랄륨 조약돌', 'Коралиумовый булыжник', '珊瑚鵝卵石'],
    ['Coralium Stone', 'Piedra de coralium', 'Pierre de coralium', 'コラリウム石', '코랄륨석', 'Коралиумовый камень', '珊瑚石'],
    ['Dark Ethaxium', 'Ethaxium oscuro', 'Ethaxium sombre', 'ダークエサキシウム', '어두운 에사시움', 'Тёмный этаксиум', '暗黑魂素'],
    ['Darklands Oak', 'Roble de las Tierras Oscuras', 'Chêne des Terres sombres', 'ダークランドのオーク', '어둠의 땅 참나무', 'Дуб Тёмных земель', '恐懼之地橡木'],
    ['Dreadstone Cobblestone', 'Adoquín del terror', 'Pierres d’effroi', 'ドレッドストーンの丸石', '드레드스톤 조약돌', 'Булыжник ужаса', '恐懼石鵝卵石'],
    ['Elysian Cobblestone', 'Adoquín elíseo', 'Pierres élyséennes', 'エリシアンの丸石', '엘리시안 조약돌', 'Элизийский булыжник', '樂土鵝卵石'],
    ['Elysian Stone', 'Piedra elísea', 'Pierre élyséenne', 'エリシアン石', '엘리시안석', 'Элизийский камень', '樂土石'],
    ['Monolith Stone', 'Piedra monolítica', 'Pierre monolithique', 'モノリスストーン', '모놀리스 돌', 'Монолитный камень', '巨石'],
    ['Omothol Stone', 'Piedra de Omothol', 'Pierre d’Omothol', 'オモソール石', '오모솔석', 'Камень Омотола', '奧穆索石'],
    ['Energy Container', 'Contenedor de energía', 'Conteneur d’énergie', 'エネルギーコンテナ', '에너지 보관함', 'Контейнер энергии', '勢能容器'],
    ['Energy Depositioner', 'Depositador de energía', 'Dépositaire d’énergie', 'エネルギー蓄積器', '에너지 축적기', 'Накопитель энергии', '勢能沉積器'],
    ['Idol of Fading', 'Ídolo del desvanecimiento', 'Idole de l’effacement', '消失の偶像', '소멸의 우상', 'Идол угасания', '消逝神像'],
    ['Mimic Fire', 'Fuego mímico', 'Feu mimétique', '擬態の炎', '모방 불꽃', 'Мимикрирующий огонь', '擬態火焰'],
    ['Portal Anchor', 'Ancla de portal', 'Ancre de portail', 'ポータルアンカー', '차원문 닻', 'Якорь портала', '傳送門錨'],
    ['Unchained Portal Anchor', 'Ancla de portal liberada', 'Ancre de portail libérée', '解放されたポータルアンカー', '해방된 차원문 닻', 'Освобождённый якорь портала', '無拘束傳送門錨'],
    ['Research Table', 'Mesa de investigación', 'Table de recherche', '研究台', '연구대', 'Стол исследований', '研究台'],
    ['Sequential Brewing Stand', 'Soporte de pociones secuencial', 'Alambic séquentiel', '連続醸造台', '순차 양조기', 'Последовательная варочная стойка', '序列釀造台'],
    ['Spirit Altar', 'Altar espiritual', 'Autel spirituel', '魂の祭壇', '영혼 제단', 'Алтарь духов', '靈魂祭壇'],
    ['Place of Power Core', 'Núcleo del lugar de poder', 'Cœur du lieu de puissance', '力の場の中核', '힘의 장소 핵', 'Ядро места силы', '力量之地核心'],
    ['Staff of Rending', 'Bastón del desgarro', 'Bâton de déchirement', '裂断の杖', '분열의 지팡이', 'Посох разрыва', '撕裂法杖'],
    ['Book of Many Faces', 'Libro de las muchas caras', 'Livre aux multiples visages', '多面の書', '다중 얼굴의 책', 'Книга множества ликов', '千面之書'],
    ['Chunk of Coralium', 'Trozo de coralium', 'Morceau de coralium', 'コラリウムの塊', '코랄륨 덩어리', 'Кусок коралиума', '珊瑚塊'],
    ['Chunk Of Coralium', 'Trozo de coralium', 'Morceau de coralium', 'コラリウムの塊', '코랄륨 덩어리', 'Кусок коралиума', '珊瑚塊'],
    ['Cooked Generic Meat', 'Carne genérica cocida', 'Viande générique cuite', '焼いた汎用肉', '익힌 일반 고기', 'Приготовленное обычное мясо', '熟通用肉'],
    ['Generic Meat', 'Carne genérica', 'Viande générique', '汎用肉', '일반 고기', 'Обычное мясо', '通用肉'],
    ['Interdimensional Cage', 'Jaula interdimensional', 'Cage interdimensionnelle', '次元間ケージ', '차원간 우리', 'Межпространственная клетка', '跨維度牢籠'],
    ['Shard of Oblivion', 'Fragmento del olvido', 'Éclat d’oubli', '忘却の欠片', '망각의 파편', 'Осколок забвения', '湮滅碎片'],
    ['Spell Staff', 'Bastón de hechizos', 'Bâton de sort', '呪文の杖', '주문 지팡이', 'Посох заклинаний', '法術權杖'],
    ['Spirit Tablet', 'Tableta espiritual', 'Tablette spirituelle', '魂の石板', '영혼 석판', 'Табличка духов', '靈魂石板'],
    ['Stone Tablet', 'Tableta de piedra', 'Tablette de pierre', '石の石板', '돌 석판', 'Каменная табличка', '石板'],
    ['Abyssalnomicon', 'Abyssalnomicon', 'Abyssalnomicon', 'アビサルノミコン', '아비살노미콘', 'Абиссалономикон', '深淵之書'],
    ['Materializer', 'Materializador', 'Matérialisateur', '物質化装置', '물질화 장치', 'Материализатор', '物質化器'],
    ['Transmutator', 'Transmutador', 'Transmutateur', '変成装置', '변환 장치', 'Трансмутатор', '嬗變器'],
    ['Multi-Block', 'Multibloque', 'Multibloc', 'マルチブロック', '멀티블록', 'Мультиблок', '多方塊結構'],
    ['Multi Block', 'Multibloque', 'Multibloc', 'マルチブロック', '멀티블록', 'Мультиблок', '多方塊結構'],
    ['Mural', 'Mural', 'Fresque', '壁画', '벽화', 'Фреска', '壁畫'],
    ['Charcoal', 'Carbón vegetal', 'Charbon de bois', '木炭', '숯', 'Древесный уголь', '木炭'],
    ['Nitre', 'Salitre', 'Salpêtre', '硝石', '초석', 'Селитра', '硝石'],
    ['Pilot Crystal', 'Cristal piloto', 'Cristal pilote', '先導クリスタル', '선도 수정', 'Пилотный кристалл', '先導水晶'],
    ['Pilot Fuel', 'Combustible piloto', 'Carburant pilote', '先導燃料', '선도 연료', 'Пилотное топливо', '先導燃料'],
    ['Shadow Shard', 'Fragmento de sombra', 'Éclat d’ombre', '影の欠片', '그림자 파편', 'Осколок тени', '暗影碎片'],
    ['Anti-Chicken', 'Antipollo', 'Anti-poule', '反物質のニワトリ', '반물질 닭', 'Анти-курица', '反雞'],
    ['Ghoul Anti-Flesh', 'Anticarne de necrófago', 'Anti-chair de goule', 'グールの反物質肉', '구울 반물질 살점', 'Антиплоть гуля', '食屍鬼反物質肉'],
    ['Beryl', 'Berilo', 'Béryl', 'ベリル', '녹주석', 'Берилл', '綠柱石'],
    ['Beryllium', 'Berilio', 'Béryllium', 'ベリリウム', '베릴륨', 'Бериллий', '鈹'],
    ['Calcium', 'Calcio', 'Calcium', 'カルシウム', '칼슘', 'Кальций', '鈣'],
    ['Copper', 'Cobre', 'Cuivre', '銅', '구리', 'Медь', '銅'],
    ['Tin', 'Estaño', 'Étain', 'スズ', '주석', 'Олово', '錫'],
    ['Abyssalnite', 'Abisalita', 'Abyssalite', 'アビサルナイト', '아비살나이트', 'Абиссалнит', '淵素'],
    ['Coralium', 'Coralium', 'Coralium', 'コラリウム', '코랄륨', 'Коралиум', '珊瑚'],
    ['Darklands', 'Tierras Oscuras', 'Terres sombres', 'ダークランド', '어둠의 땅', 'Тёмные земли', '黑暗之地'],
    ['Darkstone', 'Piedra oscura', 'Pierre sombre', 'ダークストーン', '다크스톤', 'Тёмный камень', '暗黑石'],
    ['Dreadlands', 'Tierras del Terror', 'Terres de l’effroi', 'ドレッドランド', '공포의 땅', 'Земли ужаса', '恐懼之地'],
    ['Dreadstone', 'Piedra del terror', 'Pierre d’effroi', 'ドレッドストーン', '드레드스톤', 'Камень ужаса', '恐懼石'],
    ['Dreadwood', 'Madera del terror', 'Bois d’effroi', 'ドレッドウッド', '드레드우드', 'Древесина ужаса', '恐懼木'],
    ['Dreadium', 'Dreadium', 'Dreadium', 'ドレディウム', '드레디움', 'Дредиум', '恐懼素'],
    ['Ethaxium', 'Ethaxium', 'Ethaxium', 'エサキシウム', '에사시움', 'Этаксиум', '魂素'],
    ['Omothol', 'Omothol', 'Omothol', 'オモソール', '오모솔', 'Омотол', '奧穆索'],
    ['Monolith', 'Monolito', 'Monolithe', 'モノリス', '모놀리스', 'Монолит', '巨石'],
    ['Abyssal', 'Abisal', 'Abyssal', '深淵', '심연', 'Бездонный', '深淵'],
    ['Chiseled', 'Cincelado', 'Ciselé', '模様入り', '조각된', 'Резной', '鏨製'],
    ['Cracked', 'Agrietado', 'Craquelé', 'ひび割れた', '금이 간', 'Потрескавшийся', '裂紋'],
    ['Decorative', 'Decorativa', 'Décorative', '装飾用', '장식용', 'Декоративная', '裝飾性'],
    ['Refined', 'Refinado', 'Raffiné', '精製', '정제된', 'Очищенный', '精煉'],
    ['Antimatter', 'Antimateria', 'Antimatière', '反物質', '반물질', 'Антиматерия', '反物質'],
    ['Oblivion', 'Olvido', 'Oubli', '忘却', '망각', 'Забвение', '湮滅'],
    ['Eldritch', 'Arcano', 'Occulte', '異界', '섬뜩한', 'Запредельный', '異界'],
    ['Dreaded', 'Aterrador', 'Redoutable', '恐怖の', '공포의', 'Ужасный', '恐懼'],
    ['Depths', 'Profundidades', 'Profondeurs', '深層', '심층', 'Глубины', '深淵'],
    ['Shadow', 'Sombra', 'Ombre', '影', '그림자', 'Тень', '暗影'],
    ['Spirit', 'Espiritual', 'Spirituel', '魂', '영혼', 'Духовный', '靈魂'],
    ['Nyarlathotep', 'Nyarlathotep', 'Nyarlathotep', 'ニャルラトホテプ', '니알라토텝', 'Ньярлатхотеп', '奈亞拉托提普'],
    ['Shub-Niggurath', 'Shub-Niggurath', 'Shub-Niggurath', 'シュブ＝ニグラス', '슈브 니구라스', 'Шуб-Ниггурат', '莎布·尼古拉絲'],
    ['Yog-Sothoth', 'Yog-Sothoth', 'Yog-Sothoth', 'ヨグ＝ソトース', '요그 소토스', 'Йог-Сотот', '猶格·索托斯'],
    ["J'zahar", "J'zahar", "J'zahar", 'ジャ・ザール', '자하르', "Дж'захар", '扎哈爾'],
    ['Azathoth', 'Azathoth', 'Azathoth', 'アザトース', '아자토스', 'Азатот', '阿撒托斯'],
    ['Cthulhu', 'Cthulhu', 'Cthulhu', 'クトゥルフ', '크툴루', 'Ктулху', '克蘇魯'],
    ['Hastur', 'Hastur', 'Hastur', 'ハスター', '하스터', 'Хастур', '哈斯塔'],
    ['Overworld', 'Mundo superior', 'Monde normal', 'オーバーワールド', '오버월드', 'Обычный мир', '主世界'],
    ['Samurai', 'Samurái', 'Samouraï', '侍', '사무라이', 'Самурайский', '武士'],
    ['Greater', 'Superior', 'Supérieur', '上位', '상급', 'Высший', '大型'],
    ['Lesser', 'Menor', 'Inférieur', '下位', '하급', 'Малый', '小型'],
    ['Demon', 'Demoníaco', 'Démoniaque', 'デーモン', '악마', 'Демонический', '惡魔'],
    ['Evil', 'Maligno', 'Maléfique', '邪悪な', '사악한', 'Злой', '邪惡'],
    ['Deity', 'Deidad', 'Divinité', '神格', '신격', 'Божество', '神祇'],
    ['Dread', 'Terror', 'Effroi', '恐怖', '공포', 'Ужас', '恐懼'],
    ['Muck', 'Fango', 'Boue', '泥', '진흙', 'Грязь', '淤泥'],
    ['Silver', 'Plateada', 'Argentée', '銀', '은', 'Серебряный', '銀'],
    ['Sealing', 'Sellado', 'Scellement', '封印', '봉인', 'Запечатывающий', '封印'],
    ['Energy', 'Energía', 'Énergie', 'エネルギー', '에너지', 'Энергия', '勢能'],
    ['Ghoul', 'Necrófago', 'Goule', 'グール', '구울', 'Гуль', '食屍鬼'],
    ['Flesh', 'Carne', 'Chair', '肉', '살점', 'Плоть', '肉'],
    ['Chicken', 'Pollo', 'Poule', 'ニワトリ', '닭', 'Курица', '雞'],
    ['Fuel', 'Combustible', 'Carburant', '燃料', '연료', 'Топливо', '燃料'],
];
const CONTENT_TYPE_ROWS = [
    ['Pressure Plate', 'Placa de presión de {base}', 'Plaque de pression en {base}', '{base}の感圧板', '{base} 감압판', 'Нажимная плита: {base}', '{base}壓力板'],
    ['Energy Pedestal', 'Pedestal de energía de {base}', 'Piédestal d’énergie de {base}', '{base}のエネルギー台座', '{base} 에너지 받침대', 'Энергетический пьедестал: {base}', '{base}勢能基座'],
    ['Crystal Cluster', 'Cúmulo de cristal de {base}', 'Amas cristallin de {base}', '{base}の結晶クラスター', '{base} 수정 군집', 'Скопление кристаллов: {base}', '{base}晶簇'],
    ['Gem Cluster', 'Cúmulo de gemas de {base}', 'Amas de gemmes de {base}', '{base}の宝石クラスター', '{base} 보석 군집', 'Скопление самоцветов: {base}', '{base}寶石簇'],
    ['Spawn Egg', 'Huevo generador de {base}', 'Œuf d’apparition de {base}', '{base}のスポーンエッグ', '{base} 생성 알', 'Яйцо призыва: {base}', '{base}生成蛋'],
    ['Fence Gate', 'Puerta de valla de {base}', 'Portillon en {base}', '{base}のフェンスゲート', '{base} 울타리 문', 'Калитка: {base}', '{base}柵欄門'],
    ['Cobblestone', 'Adoquín de {base}', 'Pierres de {base}', '{base}の丸石', '{base} 조약돌', 'Булыжник: {base}', '{base}鵝卵石'],
    ['Tombstone', 'Lápida de {base}', 'Tombe en {base}', '{base}の墓石', '{base} 묘비', 'Надгробие: {base}', '{base}墓碑'],
    ['Chestplate', 'Peto de {base}', 'Plastron en {base}', '{base}のチェストプレート', '{base} 흉갑', 'Нагрудник: {base}', '{base}胸甲'],
    ['Leggings', 'Grebas de {base}', 'Jambières en {base}', '{base}のレギンス', '{base} 각반', 'Поножи: {base}', '{base}護腿'],
    ['Button', 'Botón de {base}', 'Bouton en {base}', '{base}のボタン', '{base} 버튼', 'Кнопка: {base}', '{base}按鈕'],
    ['Stairs', 'Escaleras de {base}', 'Escalier en {base}', '{base}の階段', '{base} 계단', 'Ступени: {base}', '{base}樓梯'],
    ['Fence', 'Valla de {base}', 'Barrière en {base}', '{base}のフェンス', '{base} 울타리', 'Ограда: {base}', '{base}柵欄'],
    ['Slab', 'Losa de {base}', 'Dalle en {base}', '{base}のハーフブロック', '{base} 반 블록', 'Плита: {base}', '{base}半磚'],
    ['Wall', 'Muro de {base}', 'Muret en {base}', '{base}の塀', '{base} 담장', 'Стена: {base}', '{base}牆'],
    ['Door', 'Puerta de {base}', 'Porte en {base}', '{base}のドア', '{base} 문', 'Дверь: {base}', '{base}門'],
    ['Pillar', 'Pilar de {base}', 'Pilier en {base}', '{base}の柱', '{base} 기둥', 'Колонна: {base}', '{base}柱'],
    ['Log', 'Tronco de {base}', 'Bûche de {base}', '{base}の原木', '{base} 원목', 'Бревно: {base}', '{base}原木'],
    ['Planks', 'Tablones de {base}', 'Planches de {base}', '{base}の板材', '{base} 판자', 'Доски: {base}', '{base}木板'],
    ['Statue', 'Estatua de {base}', 'Statue de {base}', '{base}の像', '{base} 조각상', 'Статуя: {base}', '{base}雕像'],
    ['Bricks', 'Ladrillos de {base}', 'Briques de {base}', '{base}レンガ', '{base} 벽돌', 'Кирпичи: {base}', '{base}磚塊'],
    ['Brick', 'Ladrillos de {base}', 'Briques de {base}', '{base}レンガ', '{base} 벽돌', 'Кирпичи: {base}', '{base}磚'],
    ['Stone', 'Piedra de {base}', 'Pierre de {base}', '{base}石', '{base} 돌', 'Камень: {base}', '{base}石'],
    ['Ore', 'Mineral de {base}', 'Minerai de {base}', '{base}鉱石', '{base} 광석', 'Руда: {base}', '{base}礦石'],
    ['Block', 'Bloque de {base}', 'Bloc de {base}', '{base}ブロック', '{base} 블록', 'Блок: {base}', '{base}塊'],
    ['Boots', 'Botas de {base}', 'Bottes en {base}', '{base}のブーツ', '{base} 부츠', 'Ботинки: {base}', '{base}靴子'],
    ['Helmet', 'Casco de {base}', 'Casque en {base}', '{base}のヘルメット', '{base} 투구', 'Шлем: {base}', '{base}頭盔'],
    ['Flesh', 'Carne de {base}', 'Chair de {base}', '{base}の肉', '{base} 살점', 'Плоть: {base}', '{base}肉'],
    ['Antidote', 'Antídoto de {base}', 'Antidote de {base}', '{base}の解毒剤', '{base} 해독제', 'Противоядие: {base}', '{base}解毒劑'],
    ['Scroll', 'Pergamino de {base}', 'Parchemin de {base}', '{base}の巻物', '{base} 두루마리', 'Свиток: {base}', '{base}卷軸'],
    ['Charm', 'Amuleto de {base}', 'Charme de {base}', '{base}の護符', '{base} 부적', 'Оберег: {base}', '{base}護符'],
    ['Scale', 'Escama de {base}', 'Écaille de {base}', '{base}の鱗', '{base} 비늘', 'Чешуя: {base}', '{base}鱗片'],
    ['Key', 'Llave de {base}', 'Clé de {base}', '{base}の鍵', '{base} 열쇠', 'Ключ: {base}', '{base}鑰匙'],
    ['Tablet', 'Tableta de {base}', 'Tablette de {base}', '{base}の石板', '{base} 석판', 'Табличка: {base}', '{base}石板'],
    ['Shard', 'Fragmento de {base}', 'Éclat de {base}', '{base}の欠片', '{base} 파편', 'Осколок: {base}', '{base}碎片'],
    ['Staff', 'Bastón de {base}', 'Bâton de {base}', '{base}の杖', '{base} 지팡이', 'Посох: {base}', '{base}法杖'],
    ['Crystal', 'Cristal de {base}', 'Cristal de {base}', '{base}の結晶', '{base} 수정', 'Кристалл: {base}', '{base}水晶'],
    ['Meat', 'Carne de {base}', 'Viande de {base}', '{base}の肉', '{base} 고기', 'Мясо: {base}', '{base}肉'],
];
const CONTENT_PREFIX_ROWS = [
    ['Block of ', 'Bloque de {base}', 'Bloc de {base}', '{base}ブロック', '{base} 블록', 'Блок: {base}', '{base}塊'],
    ['Shard of ', 'Fragmento de {base}', 'Éclat de {base}', '{base}の欠片', '{base} 파편', 'Осколок: {base}', '{base}碎片'],
    ['Skin of the ', 'Piel de {base}', 'Peau de {base}', '{base}の皮', '{base} 가죽', 'Шкура: {base}', '{base}獸皮'],
];
const CONTENT_PROPER_NAMES = {
    es_es: new Set(['Necronomicon', 'Abyssalnomicon', 'Ethaxium', 'Mural']),
    fr_fr: new Set(['Necronomicon', 'Abyssalnomicon', 'Ethaxium']),
};
const DECORATIVE_STATUE_TEMPLATES = [
    'Estatua decorativa de {base}', 'Statue décorative de {base}', '装飾用{base}の像',
    '장식용 {base} 조각상', 'Декоративная статуя: {base}', '裝飾性{base}雕像',
];

function displayName(key) {
    const match = /^(?:block|item)\.abyssalcraft\.([a-z0-9_]+)$/.exec(key);
    if (!match) throw new Error(`Unsupported translation key: ${key}`);
    return match[1].split('_').map((word, index) => {
        if (index > 0 && (word === 'of' || word === 'the')) return word;
        const special = SPECIAL_WORDS.get(word);
        if (special) return special;
        if (word === 'odb' || word === 'pe') return word.toUpperCase();
        return word[0].toUpperCase() + word.slice(1);
    }).join(' ')
        .replace('Shub Niggurath', 'Shub-Niggurath')
        .replace('Yog Sothoth', 'Yog-Sothoth');
}

function chineseName(key) {
    const explicit = ZH_EXPLICIT.get(key);
    if (explicit) return explicit;
    const match = /^(?:block|item)\.abyssalcraft\.([A-Za-z0-9_]+)$/.exec(key);
    if (!match) throw new Error(`Unsupported translation key: ${key}`);
    const id = match[1].toLowerCase();
    if (id === 'monolith_stone') return '巨石';
    if (id === 'mimic_fire') return '拟态火焰';
    const decorativeStatue = id.match(/^decorative_(.+)_statue$/);
    if (decorativeStatue && ZH_DEITIES.has(decorativeStatue[1])) {
        return `装饰性${ZH_DEITIES.get(decorativeStatue[1])}雕像`;
    }
    const buttonBases = new Map([
        ['abyssal_stone', '深渊石'], ['coralium_stone', '珊瑚石'],
        ['darklands_oak', '恐惧之地橡木'], ['darkstone', '暗黑石'], ['dreadwood', '恐惧木'],
    ]);
    if (id.endsWith('_button') && buttonBases.has(id.slice(0, -'_button'.length))) {
        return `${buttonBases.get(id.slice(0, -'_button'.length))}按钮`;
    }
    const spawnEgg = id.match(/^(.+)_spawn_egg$/);
    if (spawnEgg && ZH_ENTITIES.has(spawnEgg[1])) return `${ZH_ENTITIES.get(spawnEgg[1])}刷怪蛋`;
    const gemCluster = id.match(/^coralium_gem_cluster_(\d+)$/);
    if (gemCluster) return `珊瑚宝石簇 ${gemCluster[1]}`;
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

function readJson(file) {
    return JSON.parse(fs.readFileSync(file, 'utf8').replace(/^\uFEFF/, ''));
}

function readLegacyLang(language) {
    const entries = {};
    const file = path.join(LEGACY_LANG, `${language}.lang`);
    for (const line of fs.readFileSync(file, 'utf8').replace(/^\uFEFF/, '').split(/\r?\n/)) {
        if (!line || line.startsWith('#')) continue;
        const separator = line.indexOf('=');
        if (separator < 0) continue;
        entries[line.slice(0, separator)] = line.slice(separator + 1);
    }
    return entries;
}

function restoreLegacyTranslations() {
    const english = readJson(path.join(MODERN_LANG, 'en_us.json'));
    const legacyEnglish = readLegacyLang('en_us');
    const legacyKeysByValue = new Map();
    for (const [key, value] of Object.entries(legacyEnglish)) {
        const keys = legacyKeysByValue.get(value) || [];
        keys.push(key);
        legacyKeysByValue.set(value, keys);
    }
    const normalizeId = value => value.toLowerCase().replace(/[^a-z0-9]/g, '');
    const legacyEntityKeyById = new Map(Object.keys(legacyEnglish)
        .filter(key => key.startsWith('entity.abyssalcraft.') && key.endsWith('.name'))
        .map(key => [normalizeId(key.slice('entity.abyssalcraft.'.length, -'.name'.length)), key]));
    const legacyCrystalKeyByMaterial = new Map(Object.keys(legacyEnglish)
        .filter(key => /^item\.crystal\.[A-Za-z]+\.name$/.test(key))
        .map(key => [key.slice('item.crystal.'.length, -'.name'.length).toLowerCase(), key]));

    for (const language of LANGUAGES.slice(1)) {
        const file = path.join(MODERN_LANG, `${language}.json`);
        const translations = readJson(file);
        const legacyTranslations = readLegacyLang(language);
        let direct = 0;
        let valueMatch = 0;
        let entityMatch = 0;
        let crystalMatch = 0;
        for (const [key, englishValue] of Object.entries(english)) {
            if (translations[key] !== englishValue) continue;
            const directValue = legacyTranslations[key];
            if (directValue && directValue !== englishValue) {
                translations[key] = directValue;
                direct++;
                continue;
            }
            const candidates = new Set((legacyKeysByValue.get(englishValue) || [])
                .map(legacyKey => legacyTranslations[legacyKey])
                .filter(value => value && value !== englishValue));
            if (candidates.size === 1) {
                translations[key] = [...candidates][0];
                valueMatch++;
            }
        }
        for (const [key, englishValue] of Object.entries(english)) {
            if (translations[key] !== englishValue || !key.startsWith('entity.abyssalcraft.')) continue;
            const legacyKey = legacyEntityKeyById.get(normalizeId(key.slice('entity.abyssalcraft.'.length)));
            const value = legacyKey && legacyTranslations[legacyKey];
            if (value && value !== englishValue) {
                translations[key] = value;
                entityMatch++;
            }
        }
        for (const [key, englishValue] of Object.entries(english)) {
            if (translations[key] !== englishValue) continue;
            const id = key.slice(key.lastIndexOf('.') + 1);
            const match = id.match(/^crystal_(?:fragment_|shard_)?([A-Za-z]+)$/)
                || id.match(/^([a-z]+)_crystal_cluster$/);
            if (!match) continue;
            const legacyKey = legacyCrystalKeyByMaterial.get(match[1].toLowerCase());
            const base = legacyKey && legacyTranslations[legacyKey];
            if (!base || base === legacyEnglish[legacyKey]) continue;
            let value = base;
            if (/^crystal_fragment_/i.test(id)) {
                value = (legacyTranslations['crystalfragment.suffix'] || '%s Fragment').replace('%s', base);
            } else if (/^crystal_shard_/i.test(id)) {
                value = (legacyTranslations['crystalshard.suffix'] || '%s Shard').replace('%s', base);
            } else if (/_crystal_cluster$/i.test(id)) {
                value = (legacyTranslations['crystalcluster.suffix'] || '%s Cluster').replace('%s', base);
            }
            if (value !== englishValue) {
                translations[key] = value;
                crystalMatch++;
            }
        }
        fs.writeFileSync(file, `${JSON.stringify(translations, null, 2)}\n`, 'utf8');
        console.log(`RR_LANG_LEGACY_RESTORE language=${language} direct=${direct}`
            + ` valueMatch=${valueMatch} entityMatch=${entityMatch} crystalMatch=${crystalMatch}`);
    }
}

function localizeChineseFallbacks() {
    const english = readJson(path.join(MODERN_LANG, 'en_us.json'));
    const file = path.join(MODERN_LANG, 'zh_cn.json');
    const translations = readJson(file);
    let localized = 0;
    const unresolved = [];
    for (const [key, englishValue] of Object.entries(english)) {
        if (translations[key] !== englishValue || !/[A-Za-z]{3}/.test(englishValue)) continue;
        let translated = ZH_EXPLICIT.get(key);
        if (!translated && key.startsWith('entity.abyssalcraft.')) {
            translated = ZH_ENTITIES.get(key.slice('entity.abyssalcraft.'.length));
        }
        if (!translated && /^(?:block|item)\.abyssalcraft\./.test(key)) translated = chineseName(key);
        if (!translated || translated === englishValue || !/[\u3400-\u9fff]/.test(translated)) {
            unresolved.push(key);
            continue;
        }
        translations[key] = translated;
        localized++;
    }
    if (unresolved.length) throw new Error(`Unresolved zh_cn English fallbacks: ${unresolved.join(', ')}`);
    fs.writeFileSync(file, `${JSON.stringify(translations, null, 2)}\n`, 'utf8');
    console.log(`RR_LANG_ZH_CN_LOCALIZE localized=${localized} unresolved=0`);
}

function contentValue(language, key, englishValue, translations) {
    const languageIndex = CONTENT_LANGUAGES.indexOf(language) + 1;
    const normalize = value => ['ja_jp', 'zh_tw'].includes(language)
        ? value.replaceAll(' ', '') : value.replace(/\s+/g, ' ').trim();
    const translateBase = base => {
        let value = base;
        for (const row of [...CONTENT_TERM_ROWS].sort((left, right) => right[0].length - left[0].length)) {
            value = value.split(row[0]).join(row[languageIndex]);
        }
        return normalize(value);
    };
    const format = (template, base) => normalize(template.replace('{base}', translateBase(base)));

    const id = key.slice(key.lastIndexOf('.') + 1);
    const decorativeStatue = englishValue.match(/^Decorative (.+) Statue$/);
    if (decorativeStatue) {
        return format(DECORATIVE_STATUE_TEMPLATES[languageIndex - 1], decorativeStatue[1]);
    }
    if (id.endsWith('_spawn_egg') && englishValue.endsWith(' Spawn Egg')) {
        const entityId = id.slice(0, -'_spawn_egg'.length);
        const entityValue = translations[`entity.abyssalcraft.${entityId}`]
            || englishValue.slice(0, -' Spawn Egg'.length);
        const row = CONTENT_TYPE_ROWS.find(type => type[0] === 'Spawn Egg');
        return normalize(row[languageIndex].replace('{base}', normalize(entityValue)));
    }

    const numberedType = englishValue.match(/^(.+) (Gem Cluster|Shard) (\d+)$/);
    if (numberedType) {
        const row = CONTENT_TYPE_ROWS.find(type => type[0] === numberedType[2]);
        return `${format(row[languageIndex], numberedType[1])} ${numberedType[3]}`;
    }

    for (const row of CONTENT_PREFIX_ROWS) {
        if (englishValue.startsWith(row[0])) {
            return format(row[languageIndex], englishValue.slice(row[0].length));
        }
    }
    for (const row of CONTENT_TYPE_ROWS) {
        const suffix = ` ${row[0]}`;
        if (englishValue.endsWith(suffix)) {
            return format(row[languageIndex], englishValue.slice(0, -suffix.length));
        }
    }
    return translateBase(englishValue);
}

function localizeContentFallbacks() {
    const english = readJson(path.join(MODERN_LANG, 'en_us.json'));
    const outputs = new Map();
    const failures = [];
    const scriptPatterns = {
        ja_jp: /[\u3040-\u30ff\u3400-\u9fff]/,
        ko_kr: /[\uac00-\ud7af]/,
        ru_ru: /[\u0400-\u04ff]/,
        zh_tw: /[\u3400-\u9fff]/,
    };
    for (const language of CONTENT_LANGUAGES) {
        const file = path.join(MODERN_LANG, `${language}.json`);
        const translations = readJson(file);
        let localized = 0;
        let properNames = 0;
        for (const [key, englishValue] of Object.entries(english)) {
            const ownedDecorativeStatue = /^block\.abyssalcraft\.decorative_.+_statue$/.test(key);
            if ((translations[key] !== englishValue && !ownedDecorativeStatue)
                    || !/^(?:block|item)\.abyssalcraft\./.test(key)) continue;
            if (CONTENT_PROPER_NAMES[language]?.has(englishValue)) {
                properNames++;
                continue;
            }
            const value = contentValue(language, key, englishValue, translations);
                if (!value || value === englishValue || (scriptPatterns[language]
                    && !scriptPatterns[language].test(value))) failures.push(`${language}:${key}`);
            else {
                translations[key] = value;
                localized++;
            }
        }
        outputs.set(file, translations);
        console.log(`RR_LANG_CONTENT_LOCALIZE language=${language} localized=${localized}`
            + ` properNames=${properNames}`);
    }
    if (failures.length) throw new Error(`Unresolved localized content: ${failures.join(', ')}`);
    for (const [file, translations] of outputs) {
        fs.writeFileSync(file, `${JSON.stringify(translations, null, 2)}\n`, 'utf8');
    }
    console.log('RR_LANG_CONTENT_LOCALIZE_OK unresolved=0');
}

function normalizeChineseNameFamily(language, key, original) {
    const terms = ID_FAMILY_TERMS[language];
    if (!terms || typeof original !== 'string') return original;
    const lowerKey = key.toLowerCase();
    let value = original;
    if (/^(?:block|item)\.abyssalcraft\./.test(lowerKey) && lowerKey.includes('dreadium')) {
        if (lowerKey.includes('crystal_fragment_dreadium')) return `${terms.dreadium}水晶碎屑`;
        if (lowerKey.includes('crystal_shard_dreadium')) return `${terms.dreadium}水晶碎片`;
        if (lowerKey.endsWith('.crystal_dreadium')) return `${terms.dreadium}水晶`;
        if (lowerKey.endsWith('.dreadium_crystal_cluster')) return `${terms.dreadium}晶簇`;
        if (lowerKey.endsWith('.dreadium_plate')) return `${terms.dreadium}板`;
        const dreadPrefix = language === 'zh_cn' ? '恐惧' : '恐懼';
        if (value.startsWith(dreadPrefix) && !value.startsWith(terms.dreadium)) {
            value = terms.dreadium + value.slice(dreadPrefix.length);
        }
    }
    if (/^block\.abyssalcraft\./.test(lowerKey) && lowerKey.includes('darkstone')) {
        const darkPrefix = language === 'zh_cn' ? '黑暗' : '黑暗';
        if (value.startsWith(darkPrefix)) value = terms.darkstone + value.slice(darkPrefix.length);
    }
    return value;
}

function normalizeLocalizedNames() {
    const outputs = new Map(LANGUAGES.map(language => {
        const file = path.join(MODERN_LANG, `${language}.json`);
        return [language, { file, translations: readJson(file) }];
    }));

    for (const row of ENTITY_NAME_ROWS) {
        const id = row[0];
        for (let index = 0; index < LANGUAGES.length; index++) {
            outputs.get(LANGUAGES[index]).translations[`entity.abyssalcraft.${id}`] = row[index + 1];
        }
    }

    for (const [language, overrides] of Object.entries(DISPLAY_NAME_OVERRIDES)) {
        Object.assign(outputs.get(language).translations, overrides);
    }

    for (const [language, replacements] of Object.entries(TERM_REPLACEMENTS)) {
        const translations = outputs.get(language).translations;
        for (const [key, original] of Object.entries(translations)) {
            if (typeof original !== 'string') continue;
            let normalized = original;
            for (const [from, to] of replacements) normalized = normalized.split(from).join(to);
            if (key.includes('.darklands')) {
                normalized = normalized
                    .split(language === 'zh_cn' ? '恐惧之地' : '恐懼之地')
                    .join('黑暗之地');
            }
            translations[key] = normalizeChineseNameFamily(language, key, normalized);
        }
    }

    for (const { translations } of outputs.values()) {
        for (const [sourceKey, targetKey] of MIRRORED_NAME_PAIRS) {
            translations[targetKey] = translations[sourceKey];
        }
    }

    const english = outputs.get('en_us').translations;
    const spawnEggKeys = Object.keys(english)
        .filter(key => /^item\.abyssalcraft\..+_spawn_egg$/.test(key));
    for (const language of LANGUAGES) {
        const translations = outputs.get(language).translations;
        for (const key of spawnEggKeys) {
            const id = key.slice('item.abyssalcraft.'.length, -'_spawn_egg'.length);
            const entityName = translations[`entity.abyssalcraft.${id}`];
            if (!entityName) continue;
            if (language === 'en_us') translations[key] = `${entityName} Spawn Egg`;
            else if (language === 'zh_cn') translations[key] = `${entityName}刷怪蛋`;
            else translations[key] = contentValue(language, key, english[key], translations);
        }
    }

    for (const [language, { file, translations }] of outputs) {
        fs.writeFileSync(file, `${JSON.stringify(translations, null, 2)}\n`, 'utf8');
        console.log(`RR_LANG_NAME_NORMALIZE language=${language}`
            + ` entities=${ENTITY_NAME_ROWS.length} spawnEggs=${spawnEggKeys.length}`);
    }
    console.log('RR_LANG_NAME_NORMALIZE_OK');
}

if (process.argv[2] === '--restore-legacy') {
    restoreLegacyTranslations();
    process.exit(0);
}

if (process.argv[2] === '--localize-zh-cn') {
    localizeChineseFallbacks();
    process.exit(0);
}

if (process.argv[2] === '--localize-content') {
    localizeContentFallbacks();
    process.exit(0);
}

if (process.argv[2] === '--normalize-names') {
    normalizeLocalizedNames();
    process.exit(0);
}

const keys = [...new Set(fs.readFileSync(0, 'utf8').split(/\r?\n/)
    .map(line => line.trim()).filter(Boolean))].sort();
if (keys.length === 0) throw new Error('No translation keys were provided on stdin');

if (process.argv[2] === '--zh-cn') {
    const file = path.join(MODERN_LANG, 'zh_cn.json');
    const translations = readJson(file);
    for (const key of keys) translations[key] = chineseName(key);
    fs.writeFileSync(file, `${JSON.stringify(translations, null, 2)}\n`, 'utf8');
    console.log(`RR_LANG_ZH_CN_SYNC keys=${keys.length}`);
    process.exit(0);
}

for (const language of LANGUAGES) {
    const file = path.join(MODERN_LANG, `${language}.json`);
    const translations = readJson(file);
    let added = 0;
    for (const key of keys) {
        if (Object.hasOwn(translations, key)) continue;
        translations[key] = displayName(key);
        added++;
    }
    fs.writeFileSync(file, `${JSON.stringify(translations, null, 2)}\n`, 'utf8');
    console.log(`RR_LANG_FALLBACK_SYNC language=${language} added=${added}`);
}
