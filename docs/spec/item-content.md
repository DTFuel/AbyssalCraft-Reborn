# 物品内容移植 (Item Content) — 子系统规格

> 覆盖 Stage B1/B2 的内容移植约定（PB-1 材料 / **PB-2 食物·杂项** / **PB-5 装饰方块** / PB-3 建材 / PB-6 工具 / PB-7 护甲 / **PB-8 分叉方块变体**…；物品与素方块共用本规格）。
> 主文档（01/02）与 `DEVELOPMENT.md` 只留任务勾选 + 一行进度 + 指向本文件的链接。

## 1. 范围与现状

| 任务 | 内容 | 状态 |
|---|---|---|
| PB-2 | 食物 15 + 杂项物品 9（shell） | ✅ 双端 `compileJava`+`runServer` 验证（注册冻结零错） |
| PB-5 | 装饰/素功能方块 29（17 FACING + 10 素 + 2 植物） | ✅ 双端 `compileJava`+`runServer` 验证（BLOCK+ITEM 冻结零错） |
| PB-6 | 工具 20（4 tier × {pickaxe,axe,shovel,hoe,sword}） | ✅ 双端 `compileJava`+`runServer` 验证（ITEM 冻结零工具错） |
| PB-1 / PB-3 | 材料 117 / 建材 86 | ✅ 由并发 agent 交付（PB-1 详见 `materials-subsystem.md`；PB-3 见平行表 CR-12） |
| PB-7 | 护甲 28（7 材料 × {helmet,chestplate,leggings,boots}） | ✅ 由并发 agent 交付（见平行表 CR-16） |
| PB-8 | 分叉方块变体 16（button×5 + plate×5 + sapling×2 + door×2 + gate×2） | ✅ 双端 `compileJava`+`runData`(贴图严格校验)+`runServer` 验证（BLOCK+ITEM 冻结零错） |
| PB-4 / RR-DATA | 矿石 13（注册 + 忠实掉落 + 完整采集级别） | ✅ 13 矿 Silk Touch、6 材料矿 Fortune、9 镐级别矩阵及双端数据加载由 RR-DATA 收口 |

## 2. 共享约定（供全部 PB-* 物品任务复用）

- **注册 id = 干净 snake_case**（对齐旧 `ACItems` 字段名，如 `coralium_plagued_flesh`/`oblivion_catalyst`），**不用**旧缩写注册名（`corflesh`/`oc`）。依据：设计案 §5 命名例（`dreadium_ingot` 非旧 `dreadiumingot`）。i18n key `item.abyssalcraft.<id>`。
- **每模块自带 `ModRegistrar`**（自己的 `DeferredRegister` 包装），在 **Gate 集成时**接一行进 `registry/ModRegistries.ALL`（业务/registrar 零 `//?`）。
- **数值取一手**：食物 hunger/saturation、堆叠上限等从旧 `init/ItemHandler.java` 逐条读，不臆测。
- **创造栏归属**：物品入创造页 = **Gate B 集成动作**（✅ 完成，见 §5f）——relay `ModCreativeTabs` 的 7 分类页用 vanilla `CreativeModeTab.Builder.displayItems`（fork-free）拉各内容 list/`registrar.entries()`；并行内容任务**不碰** `ModCreativeTabs`（后续接入 = 在对应页 `displayItems` 加一行）。
- **自定义方块避 1.21 抽象 `codec()`**（PB-5 实测）：1.21 起每个**非抽象** Block 子类须实现 `MapCodec<?> codec()`。具体 `Block` 已给实现（`new Block(props)` 双端 OK），但 `HorizontalDirectionalBlock`/`BushBlock` 等**抽象基类把 codec() 重声明 abstract** → 直接子类**仅 neoforge 编译报**「未覆盖抽象方法 codec()」（forge 绿，易漏）。规避：**extends 具体 `Block`** 并复用属性静态常量（如 `HorizontalDirectionalBlock.FACING`）；或 `//? if >=1.21 codec(){return simpleCodec(X::new);}` 落 platform 基类。机器块能编译＝`InteractiveBlockCompat extends Block` 继承具体 codec。**双端都要 compileJava**，勿只跑 forge。

## 3. 食物兼容层 `platform/FoodCompat`

- 跨版本分叉（javap 双 jar 核）：`FoodProperties.Builder` 的 `saturationMod(float)`(1.20.1) ↔ `saturationModifier(float)`(1.21.1)；另 1.20.1 `meat()`/`alwaysEat()` ↔ 1.21.1 无 `meat()`（狼粮改 `wolf_food` 标签）/`alwaysEdible()`。
- 业务只调 `FoodCompat.food(int nutrition, float saturation)`（唯一 `//?` 落此，`>=1.21` 分支注释）。`Item.Properties.food(FoodProperties)` 两端同签名。
- **狼粮 `meat()` 两端都先不做**（延标签阶段，保持双端一致）。

## 3b. 工具兼容层 `platform/ToolCompat`（PB-6）

- 1.21 工具大改（javap 双 jar 核）：`Tier.getLevel():int`(1.20.1) ↔ `getIncorrectBlocksForDrops():TagKey<Block>`(1.21；AC 全用 `BlockTags.INCORRECT_FOR_NETHERITE_TOOL`)；工具 item ctor `new XxxItem(Tier,dmg,speed,Props)`(1.20.1) ↔ `new XxxItem(Tier,Props.attributes(XxxItem.createAttributes(Tier,dmg,speed)))`(1.21)。dmg 类型 sword/pickaxe/hoe=int、axe/shovel=float（两版一致，`createAttributes` 有对应重载）。
- 业务只调 `ToolCompat.{tier,sword,pickaxe,axe,shovel,hoe}(...)`（`//?` 全落此）。耐久由 `tier.getUses()` 自动设（vanilla 不显式 `.durability()`）；props=`new Item.Properties()`。
- **修理料只读 PB-1**：`Ingredient.of(BuiltInRegistries.ITEM.get(ACRef.id("<ingot>")))` lazy 解析（`registry.get(RL)→Item` 双端同签名），不耦合 `MaterialItems` 内部结构。
- PB-7 护甲的 `ArmorMaterial`（1.21 改 Holder/组件）预计同法落一个 `platform/ArmorCompat`。

## 3c. 方块工厂 `platform/BlockFactory`（PB-8）

- **建材分叉构造变体**仍用具体 vanilla 类（承 PB-3 避 1.21 抽象 `codec()` 的策略），但这些类的 ctor 在 1.20.1↔1.21 分叉（javap 双 jar 逐一核）：
  - `ButtonBlock`：forge `(Props, BlockSetType, int ticks, boolean arrowsCanPress)` ↔ neo `(BlockSetType, int ticks, Props)`（1.21 无 arrowsCanPress 参，业务传入的值在 neo 分支忽略）。
  - `PressurePlateBlock`：forge `(Sensitivity, Props, BlockSetType)` ↔ neo `(BlockSetType, Props)`（`Sensitivity` 1.21 删，forge 全用 `EVERYTHING`）。
  - `DoorBlock` / `FenceGateBlock`：forge `(Props, BlockSetType/WoodType)` ↔ neo `(BlockSetType/WoodType, Props)`（仅参序反转）。
  - `SaplingBlock`：forge 匿名 `AbstractTreeGrower` ↔ neo `TreeGrower` record；两端均绑定 AC configured feature（`darklands_tree` / `dreadlands_tree`）。
- 业务只调 `BlockFactory.{button,pressurePlate,door,fenceGate,sapling}(...)`（`//?` 全落此文件）。石家族用 `BlockSetType.STONE`、木家族用 `BlockSetType.OAK`；`FenceGateBlock` 用 `WoodType.OAK`。
- 两树苗已实测长成对应 AC log，而非 vanilla oak；configured feature 由 worldgen 数据持有。
- 模型 datagen 复用 PB-3 的 `platform/BlockModelGen`：`button`/`pressurePlate`/`cross`(sapling) 已有；PB-8 补 `door()`(`doorBlockWithRenderType` cutout + flat item 模型) / `fenceGate()`(`fenceGateBlock` + block item) 两 helper。

## 4. 明确延后（不在 B1 物品任务范围）

- **食物进食效果**（旧 `onFoodEaten` 的原版 HUNGER/NAUSEA/… + 自定义 `coralium_plague`/`dread_plague`）→ **效果系统 T7.10**（自定义 plague MobEffect 尚不存在；含 `isPlayerCoralium` 条件逻辑，声明式 FoodProperties 无法表达）。
- **物品模型 / 贴图** → 资产阶段 **PK**（旧贴图命名乱：`corflesh→items/cf`、`oc` 多层合成）。PB-2 只注册 + 命名，`runServer` 验证（服务端不烘焙模型）。

## 5. PB-2 交付明细

**食物 15**（`FoodCompat.food(nutri,sat)`，值取自 1.12.2 ItemHandler）：coralium_plagued_flesh(2,.1) · anti_beef/anti_chicken/anti_pork/rotten_anti_flesh/anti_spider_eye/anti_plagued_flesh/anti_ghoul_flesh(0,0) · generic_meat(4,.4) · cooked_generic_meat(9,.9) · ghoul_flesh/abyssal_ghoul_flesh/dreaded_ghoul_flesh/shadow_ghoul_flesh(2,.1) · omothol_ghoul_flesh(3,.3)。

**杂项 9**（plain `Item`；系统行为延后）：oblivion_catalyst · anti_bone · eye_of_the_abyss(`stacksTo(1)`) · essence_of_the_gatekeeper · token_of_jzahar · spirit_tablet_shard_0..3。

**未纳入 PB-2（延各系统）**：antidote(效果/血浆) · lost_page/scriptures_of_omniscience(知识) · rings(仪式) · coin(→PB-1 材料) · doors(→PB-3 方块) · stone_tablet/scrolls(法术) · spirit_tablet 全/crystal bags(菜单容器)。

文件：`content/item/misc/MiscItems.java`（registrar，24 item）+ `platform/FoodCompat.java`（新，CR-11）+ `lang/en_us.json`（24 键）+ `registry/ModRegistries.ALL` 接 `MiscItems.ITEMS`（Gate B 集成）。

## 5b. PB-5 交付明细（装饰/素功能方块）

**29 块**（clean id；硬度取自 1.12.2 BlockHandler）：
- **17 FACING**（`DecoFacingBlock extends Block` + `HorizontalDirectionalBlock.FACING`，放置背对玩家）：7 装饰雕像 `decorative_{cthulhu,hastur,jzahar,azathoth,nyarlathotep,yog_sothoth,shub_niggurath}_statue`(6/12) + `mural`(5/10) + 9 墓碑 `tombstone_{stone,abyssal_stone,coralium_stone,darkstone,dreadstone,elysian_stone,ethaxium,monolith_stone,omothol_stone}`(2.5/20)。
- **10 素块**：4 ingot 块 `block_of_{abyssalnite,refined_coralium,dreadium,ethaxium}`(METAL 5/6) · `dreadlands_dirt/grass/muck` · `abyssal_sand` · `fused_abyssal_sand` · `abyssal_sand_glass`(noOcclusion)。Dreadlands grass 恢复低光/遮光阈值退化与传播；muck 恢复 14/16 高度和水平速度 ×0.8。
- **2 植物**（noCollission+instabreak）：`luminous_thistle` · `wastelands_thorn`；恢复 Abyssal/Fused Sand 与 vanilla 草土存活，thorn 仅对腿/脚均无护甲的玩家造成 1 点 cactus damage。

**未纳入 PB-5（延各系统）**：`monolith_pillar`(PE `IEnergyAmplifier`) · 功能/召唤 statue（仪式） · 26 crystal cluster（晶体系统） · 建材族/矿（PB-3/PB-4） · 各 spawner/energy/ritual/portal 块。

文件：`content/block/deco/**`（registrar 29 块 + 29 blockitem + 专属行为/形状）+ `data/gen/DecoBlockData`（基础 blockstate/model/item model）+ lang/贴图。多面 grass/fused sand、红色 tint 与基础朝向几何已可加载；最终雕像/墓碑高保真模型仍归 `T9.2b`。

## 5c. PB-6 交付明细（工具）

**20 工具** = 4 tier × {pickaxe,axe,shovel,hoe,sword}。tier（level, uses, speed, attack, enchant；取自 1.12.2 `AbyssalCraftAPI`）：
- `abyssalnite`(4,1261,10,4,12) 修 abyssalnite_ingot · `refined_coralium`(5,1800,12,5,13) 修 refined_coralium_ingot · `dreadium`(6,2300,14,6,14) 修 dreadium_ingot · `ethaxium`(8,2800,16,8,20) 修 ethaxium_ingot。
- 工具-type modifier（vanilla 约定）：pickaxe(1,-2.8) · axe(6,-3) · shovel(1.5,-3) · hoe(-3,0) · sword(3,-2.4)。id = `<tier>_{pickaxe,axe,shovel,hoe,sword}`。
- **延后**：特殊工具（coralium_longbow/cudgel/dreadium_katana/soul reaper/staff of rending）→ M7；贴图/模型 → PK。

文件：`content/item/tool/ToolItems.java`（registrar + `ALL`，20 工具）+ `platform/ToolCompat.java`（新，CR-15）+ `lang/en_us.json`（20 键）+ `registry/ModRegistries.ALL` 接 `ToolItems.ITEMS`（Gate B 集成）。

**更新（RR-DATA）**：矿注册、`mineable/pickaxe`、vanilla/loader requirement tags 与四级 AC tier 已统一由 `ACTagData`/`ToolCompat` 持有。Forge 1.20 使用 `TierSortingRegistry` 排序 `netherite < abyssalnite < refined_coralium < dreadium < ethaxium`；Neo 1.21 使用 `incorrect_for_*` tags。两端共享旧 harvest level 2/3/4/5 语义，不再保留“暂映射 diamond 位”的占位结论。

## 5d. PB-8 交付明细（建材方块分叉构造变体）

**16 块**（串行接 PB-3；clean id；ctor 全走 `platform/BlockFactory`）：
- **button×5**：`{darkstone,abyssal_stone,coralium_stone}_button`(STONE, 20t, arrows✗) + `{darklands_oak,dreadwood}_button`(OAK, 30t, arrows✓)。
- **pressure_plate×5**：同 5 家族 `<family>_pressure_plate`（石 STONE / 木 OAK；forge `Sensitivity.EVERYTHING`）。
- **sapling×2**：`{darklands_oak,dreadwood}_sapling`（占位 oak grower）。
- **door×2**：`{darklands_oak,dreadwood}_door`（`BlockSetType.OAK`，noOcclusion + cutout 渲染）。
- **fence_gate×2**：`{darklands_oak,dreadwood}_fence_gate`（`WoodType.OAK`）。
- props helper（`BaseBlocks` 内私有）：`redstoneProps(sound)`=`noCollission().strength(0.5F).sound()`（button/plate）；`doorProps()`=`strength(3.0F).sound(WOOD).noOcclusion()`；`saplingProps()`=`noCollission().randomTicks().instabreak().sound(GRASS)`；fence_gate 复用 `wood()`(2/3)。

**贴图**：8 张专属从 1.12 资产改名迁入 —— 门 `door_dlt/drt_lower/upper.png`→`block/*_door_bottom/top.png`、`items/door_dlt/drt.png`→`item/*_door.png`；sapling `dlts/drts.png`→`block/*_sapling.png`（**best-guess**：旧版 sapling 模型/blockstate 缺失，缩写 dlt=darklands/drt=dreadwood 推断，待 PK 复核）。button/plate/gate 复用 PB-3 已铺家族贴图（darkstone / abyssal_stone / coralium_stone / darklands_oak_planks / dreadwood_planks）。

文件：`platform/BlockFactory.java`（新，CR-17）+ 串行扩 PB-3 冻结的 `registry/BaseBlocks`(+16 块 + 3 props helper) / `data/gen/BaseBlockData`(+16 模型) / `platform/BlockModelGen`(+`door()`/`fenceGate()` helper) + `lang/en_us.json`(16 键) + 8 贴图。**`BaseBlocks.BLOCKS/ITEMS` 已由 PB-3 接入 `ModRegistries.ALL` → 本任务零注册改动**。runData 的 `ExistingFileHelper` 严格校验 16 模型引用的贴图存在（漏贴图即 runData FAIL）→ 两端 runData 绿即证贴图齐全。

## 5e. PB-4 交付明细（矿石方块）

**13 矿**（clean `<name>_ore`；`new Block(Properties.of().strength(硬度,抗性).requiresCorrectToolForDrops())` + BlockItem；硬度/抗性一手取自 1.12.2 `BlockHandler`）：
- **needs_iron_tool（8，1.12 level 2）**：coralium(3,6) · abyssalnite(3,6) · abyssal_abyssalnite(3,6) · nitre(3,6) · abyssal_iron(3,6) · abyssal_gold(5,10) · abyssal_diamond(5,10) · abyssal_nitre(3,6)。
- **level 3（diamond）**：abyssal_coralium(3,6)。
- **level 4（netherite / abyssalnite）**：dreadlands_abyssalnite(2.5,20) · dreaded_abyssalnite(2.5,20) · liquified_coralium(10,12)。
- **level 5（refined coralium）**：pearlescent_coralium(8,10)。全 13 入 `mineable/pickaxe`；Dreadium/Ethaxium 位继续覆盖更高建材 requirement。

**掉落表**（一手取自 `BlockACOre.getItemDropped`，最终 owner=`data/gen/OreLootData`；`data/abyssalcraft/{loot_table 1.21 单数|loot_tables 1.20 复数}/blocks/<ore>.json` 双写）：
- coralium_ore / abyssal_coralium_ore → `coralium_gem`（`set_count` uniform 1-3 + `apply_bonus` ore_drops fortune + `explosion_decay`）。
- nitre_ore / abyssal_nitre_ore → `nitre`(1-3 + fortune)；pearlescent_coralium_ore → `coralium_pearl`(1-2 + fortune)；abyssal_diamond_ore → `minecraft:diamond`(1 + fortune)。
- 余 7 矿（abyssalnite / abyssal_abyssalnite / dreadlands_abyssalnite / dreaded_abyssalnite / abyssal_iron / abyssal_gold / liquified_coralium `_ore`）普通掉自身。13 表均先匹配 Silk Touch 掉原矿；普通分支均带 explosion decay，6 材料矿带 Fortune `ore_drops`。

**采集标签**（最终 owner=`data/gen/ACTagData`，单双目录双写）：`mineable/pickaxe`(13) + level 2/3 vanilla tags + level 4 loader tags + level 5/6/8 AC requirement/incorrect tags。Forge 由官方 tier sorting解析，Neo由每个 Tier 的 `incorrect_for_*` 解析。

**模型**：39 = blockstate + 方块模型（`cube_all` 引 **vanilla 占位** emerald/lapis/redstone/nether_quartz/iron/gold/diamond_ore）+ 物品模型。**faithful AC 层叠 overlay 贴图（旧版 `layered_cube`：stone base + `blocks/ores/<name>` overlay）延 PK**。

文件：`content/block/ore/OreBlocks.java`（registrar 13 块 + 13 BlockItem）+ `data/gen/{OreLootData,ACTagData,CookingRecipeData}` + 39 模型 JSON + `lang/en_us.json`(14 键)。旧静态矿 loot/tag 已移除，避免双 owner；矿石冶炼已纳入 RR-DATA 的 53 项审计。

**验证**：RR-DATA provider invariant 为 `ores=13 material=6 self=7 physical=26`；双端真实服务器以 `Block.getDrops` 验普通/Silk Touch/128 次 Fortune III边界与增益，以 `ItemStack.isCorrectToolForDrops` 验9镐×13矿矩阵。模型仍属 T9.2b，不影响数据任务完成。

## 5f. RR-DATA 配方、标签与掉落闭包

### Crafting（401 个有效配方）

- 旧资源目录有 402 个 JSON，其中 `_constants.json` 是 13 项转换常量，不是 recipe；有效基线为 401 = 350 shaped + 28 shapeless + 22 ore shaped + 1 ore shapeless。
- 四态闭包：**305 MIGRATED + 61 REPLACED + 35 BLOCKED + 0 RETIRED = 401**。逐条事实源是 [`rr-data-crafting-audit.csv`](rr-data-crafting-audit.csv)，`LegacyCraftingRecipeData` 每次生成时强校验无漏项、重复或现代 ID 碰撞。
- 61 个 REPLACED 中，60 个已有等价现代配方；另 1 个是旧第二 Darklands log 在扁平化后与 `darklands_oak_log` 合并。`dltplank_alt` 因此不再生成，避免语义重复。
- 35 个 BLOCKED 不伪造内容：3 个需 NBT/component 子系统（两 antidote + spawn egg）；其余依赖未注册内容，包括能量 collector/relay/pedestal、ritual charm/ring/altar、portal/rending/state-transformer、维度 skin/essence、Katana/longbow/staff、crate/tablet/ODB core 等。对应内容注册后由下游任务重新消费审计，不改变本轮“已完成对账”的事实。
- ID 映射以旧 `BlockHandler`/`ItemHandler` 注册链为权威，显示名仅辅助；显式 override 解决 Ethaxium brick 方块与材料 item 同字段冲突。OreDict 输入改为可验证 tag，metadata先扁平化，未知引用直接 BLOCKED。

### Smelting（53 项展开源）

- `AbyssalCrafting` 展开为 **51 MIGRATED + 1 REPLACED + 1 BLOCKED + 0 RETIRED = 53**；逐条事实源是 [`rr-data-smelting-audit.csv`](rr-data-smelting-audit.csv)。
- 唯一 REPLACED 是旧第二 Darklands log 合并到现代 `darklands_oak_log`；唯一 BLOCKED 是未注册的 `coralium_infused_stone`。51条保留旧 XP、200 tick、结果数量，含 `chunk_of_coralium -> 2 refined_coralium_ingot`、`coin -> 4 iron_ingot` 与20条护甲回收。
- 旧配置键 `smelting_recipes` 保留用于配置兼容，但已弃用；现代 recipe存在性由 datapack控制，不再引入 loader-specific 条件配方。

### 完整标签与双版本目录

- `ACTagData` 从当前注册表分类生成 **177 个逻辑 tag / 343 个物理文件**：公共tag双写，11个loader专属 requirement/incorrect tag只写对应版本目录；覆盖两木系与 dead tree、建材分类、mineable、四级工具、材料/晶体和迁移兼容 tags。每个直接值在写盘前按block/item注册表验证。
- 每个 provider 每次同时写 1.20 `recipes/loot_tables/tags/{blocks,items}` 与 1.21 `recipe/loot_table/tags/{block,item}`，避免共享 `src/main/generated` 的 HashCache 跨节点互删；recipe result 唯一 schema差异为 1.20 `item` 与 1.21 `id`。
- 16个 storage recipe改为消费 RR-DATA item tags。生成产物无语义重复、恒等配方、旧缩写、metadata `data` 字段或 `forge:ore_*` serializer泄漏。

### 验证证据（2026-07-24）

- 双端 `compileJava`/`runData`：`RR_DATA_CRAFTING_AUDIT_OK source=401 migrated=305 replaced=61 blocked=35 retired=0`、`RR_DATA_SMELTING_AUDIT_OK source=53 migrated=51 replaced=1 blocked=1 retired=0`、`RR_DATA_TAGS_OK logical=177 physical=343`、`RR_DATA_ORE_LOOT_OK ores=13 material=6 self=7 physical=26`。
- Forge 1.20.1 与 NeoForge 1.21.1 真实服务器及 `/reload` 后均通过代表性 tag storage往返、旧 shaped `aaxe`、多产物 smelting、护甲回收、13矿普通/Silk/Fortune矩阵。临时运行探针在验证后删除。
- 双端 production build成功；最终 JAR中两套 recipe各448、两套 block loot各181，关键文件齐全，JSON解析错误、`dltplank_alt`、临时探针残留均为0。

## 5g. Gate B 创造栏（忠实还原 7 分类页）

**结构**（relay `registry/ModCreativeTabs`，忠实还原 1.12.2 `ACTabs`；M1 不含法术页）：

| 页 id | 标题 | 内容 | 图标 |
|---|---|---|---|
| `blocks` | AbyssalCraft: Blocks | 建材(PB-3/8) + 矿(PB-4) + 3 机器块 | darkstone |
| `items` | AbyssalCraft: Items | 材料基础(PB-1，39) + 杂项(PB-2，9) | abyssalnite_ingot |
| `tools` | AbyssalCraft: Tools | 镐/斧/锹/锄(PB-6，16) | abyssalnite_axe |
| `combat` | AbyssalCraft: Combat | 剑(PB-6，4) + 护甲(PB-7，28) | abyssalnite_sword |
| `food` | AbyssalCraft: Food | 食物(PB-2，15) | abyssal_ghoul_flesh |
| `decorations` | AbyssalCraft: Decorations | 装饰块(PB-5，29) | mural |
| `crystals` | AbyssalCraft: Crystals | 26 元素 x crystal/shard/fragment(PB-1，78) | crystal_coralium |

**机制**：
- **`platform/ModRegistrar.entries()`**（Gate B 增）：`register` 时记录 supplier、返回只读视图。让 relay 遍历各 registrar 注册项填页，免各内容文件各暴露 list。blocks/decorations 页用 `BLOCKS.entries()`（`Block` 是 `ItemLike` → `output.accept(block)` 经其 BlockItem）。
- **填充走 vanilla `CreativeModeTab.Builder.displayItems`**（fork-free，PB-1 已证双端同签名），惰性 lambda（进世界/开菜单时求值）→ 免 `BuildCreativeModeTabContentsEvent` 分叉。
- **剑/工具分页**：`ToolItems.ALL` 按注册名 `_sword` 后缀分（剑 → combat、余 → tools）。
- **`acceptAll` 守卫**：`asItem()!=Items.AIR` 跳过无 BlockItem 方块，防空 stack 崩创造页（消费他人 registrar 的边界防御）。
- **集中管理**：PB-1 `materials` 页 + PB-4 `ores` 页已删并入本 7 页；`MaterialItems` 加 `BASICS`(39) list 供 items 页（晶体走 `CRYSTALS`/`CRYSTAL_SHARDS`/`CRYSTAL_FRAGMENTS`）。CrystalItems 试点件(pilot_crystal/fuel)不入页（同 demo）。

**后续内容任务接入**：新内容入创造页 = 在 `ModCreativeTabs` 对应页 `displayItems` 加一行拉自己的 list（或 registrar `entries()`），**不新建页**（除非新子系统需专属页，如 spells）。

**验证**（两节点）：`compileJava --rerun-tasks`+`runData` 绿 + `runClient` **进世界**：JEI 成分表 forge 1896 / neo 2026 含全部 AC 内容、**无 AC 页被 JEI 标空**（仅 vanilla Operator Utilities 空 = 正常）、无 `ModCreativeTabs`/`displayItems` 报错、`Stopping!` 干净退出。**7 页视觉分组/图标需人工目视**（多数内容贴图/模型延 PK → 创造栏显缺失模型紫黑占位，属预期；ores/materials/crystals 有模型正常显示）。旧 `.materials`/`.ores` lang 键留作无害死键、待 PK-4 清。

## 6. 验证（判据 = run 日志/退出码）

- 两节点 `compileJava --rerun-tasks`：BUILD SUCCESSFUL（含 neoforge 走 `saturationModifier` 分支，证明 `//?` 分叉正确）。
- 两节点 `runServer`：`AbyssalCraft … starting up` + `Done`（forge 2.954s / neo 1.050s）+ `stop` 干净退出；`ITEM` 注册表冻结**零错**（24 id 无冲突、food component 无运行期报错）。
- **待人工目视**（客户端交互，日志无法断言）：创造栏图标（PK 补贴图后）、实际右键进食 + 饱食度变化。

## 7. DoD / 遗留

- [x] 24 物品注册（双端 runServer 冻结零错）+ 食物含 FoodProperties + en_us 名。
- [ ] 贴图/模型（PK）。
- [ ] 进食效果（T7.10）。
- [ ] 狼粮 `wolf_food` 标签（标签阶段）。
- [ ] 创造栏填充（Gate B）。
