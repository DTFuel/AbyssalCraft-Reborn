# AbyssalCraft 1.12.2 — 详细项目索引

> 索引对象：`docs/AbyssalCraft-1.12.2/`
> 生成方式：对目录逐层枚举（`list_dir` / `file_search` / `read_file` / `grep_search`）后整理。
> 计数说明：Java 源码 870（main）+ 14（第三方 API 桩）；资源 png 644、模型 640、配方 402、方块状态 280、音效 106、战利品 78、结构 36、进度 9、语言 8。
> 所有 Java 类清单为**完整枚举**；贴图/模型/配方/方块状态因数量庞大以"子目录 + 计数 + 命名约定"形式给出（含完整子目录树）。

---

## 目录

1. 项目概述
2. 构建与工程配置
3. 顶层文件与 `.github`
4. 主类与初始化生命周期（`AbyssalCraft.java` + `init/`）
5. 公开 API 源码（`src/main/java/.../api`，162）
6. 第三方 API 桩（`src/api/java`，14）
7. `common` 包（核心实现，462）
8. `client` 包（客户端，151）
9. `lib` 包（内部库，56）
10. `integration/jei` 包（JEI，30）
11. 资源：语言 / 进度 / 方块状态 / 模型 / 配方 / 贴图 / 音效 / 战利品 / 结构 / 其它
12. 游戏内容与系统总览（维度 / 生物群系 / 实体 / 仪式 / 法术 / 扰动 / 护甲 / 势能）
13. 统计总表

---

## 1. 项目概述

| 项目 | 值 |
|---|---|
| 模组名 | AbyssalCraft |
| 作者 | Shinoow |
| modid | `abyssalcraft` |
| 平台 | Minecraft **Forge** |
| MC 版本 | 1.12.2 |
| Forge 版本 | 14.23.5.2846 |
| 模组版本 | 2.0.0-BETA-7（`release_type = beta`） |
| 主类 | `com.shinoow.abyssalcraft.AbyssalCraft` |
| 客户端代理 | `com.shinoow.abyssalcraft.client.ClientProxy` |
| 服务端/通用代理 | `com.shinoow.abyssalcraft.common.CommonProxy` |
| GUI 工厂 | `com.shinoow.abyssalcraft.client.config.ACGuiFactory` |
| 可选依赖 | JEI `jei_1.12.2:4.16.1.302`（`deobfCompile`，运行期 `after:jei@[4.11.0,)`） |
| 硬依赖 | Forge（`required-after:forge`） |
| 更新检查 | `updateJSON = https://raw.githubusercontent.com/Shinoow/AbyssalCraft/master/version.json` |
| 代码许可 | GNU LGPL v3 |
| 资源许可 | 贴图/模型/音效版权归 Shinoow，保留所有权利（All Rights Reserved） |
| 签名 | 构建期可选 `SignJar`（`certificateFingerprint = "cert_fingerprint"`，占位符在编译期替换） |
| 主题 | 克苏鲁 / 亡灵 / 跨维度探索，「Surpass the boundaries of sanity」 |

`@Mod` 注解要点（`AbyssalCraft.java`）：
- `dependencies = "required-after:forge@[forgeversion,);after:jei@[4.11.0,)"`
- `acceptedMinecraftVersions = "[1.12.2]"`
- `useMetadata = false`、`certificateFingerprint = "cert_fingerprint"`
- 常量：`version = "ac_version"`、`modid = "abyssalcraft"`、`name = "AbyssalCraft"`（`ac_version`/`forgeversion`/`cert_fingerprint` 由 `build.gradle` 的 `replaceIn` 在编译期文本替换）。

贡献者署名（`mcmod.info` credits）：shinoow（编码）、Uberorb / Tiktalik / Dylan4ever / Seth0067 / DTFuel（贴图）、DamienDarkside / PinkMustard / Funwayguy（音效）、Cybercat5555（模型/贴图）、Mike-U5 / Funwayguy（代码）。

---

## 2. 构建与工程配置

**构建栈**：ForgeGradle `2.3-SNAPSHOT` + CurseGradle `1.2.0`（应用插件 `java`、`net.minecraftforge.gradle.forge`、`com.matthewprenger.cursegradle`、`maven`、`maven-publish`）。

**`build.properties`**（构建期读取为 `props`）：
```
mc_version=1.12.2
forge_version=14.23.5.2846
ac_version=2.0.0-BETA-7
release_type=beta
```

**`gradle.properties`**：`org.gradle.jvmargs=-Xmx3G`、`org.gradle.daemon=false`（反编译需要大内存）。

**`build.gradle` 关键点**：
- `group = com.shinoow.abyssalcraft`，`archivesBaseName = AbyssalCraft`，`version = "${mc_version}-${ac_version}"`。
- 源/目标兼容性 Java `1.8`；`mappings = "stable_39"`；`runDir = "run"`；`useDepAts = true`。
- `replaceIn "AbyssalCraft.java"`：替换 `forgeversion` / `ac_version` / `cert_fingerprint`。
- `processResources`：对 `mcmod.info`、`version.properties` 执行 `expand`（注入 version/acversion/mcversion/forgeversion），排除 `**/Thumbs.db`。
- 自定义任务：`srcJar`（classifier `sources`）、`apiJar`（classifier `api`，仅打包 `com/shinoow/abyssalcraft/api/**`）、`signJar`（`dependsOn reobfJar`，`build.dependsOn signJar`）、`prepareFiles`（生成源码 RAR）、`copyApiJar`、`copySrcJar`。
- 仓库：`progwml6`（JEI）、forge maven。
- `curseforge`：项目 id `53686`，`changelogType = html`，`addArtifact apiJar`，可选依赖 `jei`。
- `publishing`：发布 `components.java` + `srcJar` + `apiJar`。

**版本/元数据文件**：
- `version.json` — Forge 更新 JSON（`promos` 覆盖 1.8.9–1.12.2 各分支的 latest/recommended；`1.12.2-latest = 2.0.0-BETA-7`、`1.12.2-recommended = 1.11.3`）+ 各版本 HTML changelog。
- `version.txt` — `1.9.1.3`。
- `changelog.html` — CurseForge 发布用变更日志（HTML）。
- `src/main/resources/version.properties` — `abyssalcraft.version/forge.version/minecraft.version`（编译期占位替换）。
- `src/main/resources/mcmod.info` — FML 模组描述（`modListVersion 2`，logo `assets/abyssalcraft/textures/logo.png`）。
- `src/main/resources/pack.mcmeta` — 资源包元数据。

**Gradle Wrapper**：`gradlew`、`gradlew.bat`、`gradle/wrapper/gradle-wrapper.properties`。

---

## 3. 顶层文件与 `.github`

**根目录文件**：
- 配置/脚本：`build.gradle`、`build.properties`、`gradle.properties`、`gradlew`、`gradlew.bat`、`.gitattributes`、`.gitignore`
- 文档：`README.md`、`ideas.md`、`changelog.html`
- 许可：`LICENSE`（LGPLv3 全文）、`LICENSE_ASSETS.md`
- 元数据：`version.json`、`version.txt`、`patrons.json`（Patreon 赞助者）、`supporters.txt`

**`.github/`**：
- `ISSUE_TEMPLATE/bug_report.md`
- `ISSUE_TEMPLATE/suggestion.md`

---

## 4. 主类与初始化生命周期

**`src/main/java/com/shinoow/abyssalcraft/`（根）**：`AbyssalCraft.java`（`@Mod` 入口）。

主类通过一个 `List<ILifeCycleHandler>` 顺序驱动各阶段（`preInit`/`init`/`postInit`/`loadComplete`/`serverStart`/`serverStarting`），注册顺序为：`InitHandler.INSTANCE` → `BlockHandler` → `WorldHandler` → `ItemHandler` → `MiscHandler` → `EntityHandler` → `IntegrationHandler`；静态块启用 `FluidRegistry.enableUniversalBucket()`；注册命令 `CommandUnlockAllKnowledge`；IMC 由 `IMCHandler` 处理。

**`init/`（8 个生命周期处理器）**：
`ILifeCycleHandler`（接口）、`InitHandler`、`BlockHandler`、`ItemHandler`、`EntityHandler`、`WorldHandler`、`MiscHandler`、`IntegrationHandler`。

---

## 5. 公开 API 源码（`src/main/java/com/shinoow/abyssalcraft/api`，162 个）

> 该包即 `apiJar` 打包内容，供其它模组进行集成。

- **`api`（根）**：`AbyssalCraftAPI`、`APIUtils`、`CommonReferences`、`IMCHelper`、`package-info`
- **`api/armor`**：`ArmorData`、`ArmorDataCollection`、`ArmorDataRegistry`、`ColorData`、`TextureData`、`package-info`
- **`api/biome`**：`ACBiomes`、`IAbyssalWastelandBiome`、`IDarklandsBiome`、`IDreadlandsBiome`、`package-info`
- **`api/block`**：`ACBlocks`、`ICrystalBlock`、`IRitualAltar`、`IRitualPedestal`、`ISingletonInventory`、`SingletonInventoryUtil`、`package-info`
- **`api/dimension`**：`DimensionData`、`DimensionDataRegistry`、`IAbyssalWorldProvider`、`package-info`
- **`api/energy`**：`EnergyEnum`、`IAmplifierCharm`、`IEnergyAmplifier`、`IEnergyBlock`、`IEnergyCollector`、`IEnergyContainer`、`IEnergyContainerItem`、`IEnergyManipulator`、`IEnergyRelayBlock`、`IEnergyTransporter`、`IEnergyTransporterItem`、`IIdol`、`PEUtils`、`package-info`
- **`api/energy/disruption`**：`DisruptionEntry`、`DisruptionHandler`、`DisruptionPotion`、`Disruptions`、`DisruptionSpawn`、`DisruptionSwarm`、`package-info`
- **`api/energy/structure`**：`IPlaceOfPower`、`IStructureBase`、`IStructureComponent`、`StructureHandler`、`package-info`
- **`api/entity`**：`ACEntities`、`EntityUtil`、`IAntiEntity`、`ICoraliumEntity`、`IDreadEntity`、`IEliteEntity`、`IOmotholEntity`、`IShoggothEntity`、`package-info`
- **`api/event`**：`ACEvents`、`FuelBurnTimeEvent`、`RitualEvent`、`package-info`
- **`api/integration`**：`ACPlugin`、`IACPlugin`、`package-info`
- **`api/internal`**：`DummyMethodHandler`、`DummyNecroDataHandler`、`IInternalMethodHandler`、`IInternalNecroDataHandler`、`package-info`
- **`api/item`**：`ACItems`、`ICrystal`、`package-info`
- **`api/knowledge`**：`AlwaysLockedResearchItem`、`DefaultResearchItem`、`IResearchable`、`IResearchableItem`、`IResearchItem`、`KnowledgeType`、`ResearchItem`、`ResearchItems`、`ResearchRegistry`、`package-info`
- **`api/knowledge/condition`**：`ArtifactCondition`、`BaseUnlockCondition`、`BiomeCondition`、`BiomePredicateCondition`、`ConditionProcessorRegistry`、`DefaultCondition`、`DimensionCondition`、`EntityCondition`、`EntityPredicateCondition`、`IConditionProcessor`、`ImpossibleCondition`、`IUnlockCondition`、`MandatoryMultiEntityCondition`、`MiscCondition`、`MultiBiomeCondition`、`MultiEntityCondition`、`NecronomiconCondition`、`PageCondition`、`UnlockConditions`、`WhisperCondition`、`package-info`
- **`api/knowledge/condition/caps`**：`INecroDataCapability`、`NecroDataCapability`、`NecroDataCapabilityProvider`、`NecroDataCapabilityStorage`、`package-info`
- **`api/necronomicon`**：`Chapters`、`CraftingStack`、`GuiInstance`、`INecroData`、`INecronomiconAction`、`NecroData`、`NecronomiconActionRegistry`、`Pages`、`package-info`
- **`api/recipe`**：`AnvilForging`、`AnvilForgingRecipes`、`AnvilForgingType`、`Crystallization`、`CrystallizerRecipes`、`Materialization`、`MaterializerRecipes`、`Transmutation`、`TransmutatorRecipes`、`package-info`
- **`api/rending`**：`Rending`、`RendingRegistry`、`package-info`
- **`api/ritual`**：`EnumRitualParticle`、`NecronomiconCreationRitual`、`NecronomiconEnchantmentRitual`、`NecronomiconInfusionRitual`、`NecronomiconPotionAoERitual`、`NecronomiconPotionRitual`、`NecronomiconRitual`、`NecronomiconSummonRitual`、`NecronomiconTransformationRitual`、`RitualRegistry`、`Rituals`、`package-info`
- **`api/spell`**：`EntityTargetSpell`、`IScroll`、`Spell`、`SpellEnum`、`SpellRegistry`、`Spells`、`SpellUtils`、`package-info`
- **`api/transfer`**：`ItemTransferConfiguration`、`package-info`
- **`api/transfer/caps`**：`IItemTransferCapability`、`ItemTransferCapability`、`ItemTransferCapabilityProvider`、`ItemTransferCapabilityStorage`、`package-info`

---

## 6. 第三方 API 桩（`src/api/java`，14 个）

> 随源码分发的软依赖接口，用于编译期集成（运行期由对应模组提供实现）。

- **`invtweaks/api`（11）**：`IItemTree`、`IItemTreeCategory`、`IItemTreeItem`、`IItemTreeListener`、`InvTweaksAPI`、`SortingMethod`；`container/`：`ChestContainer`、`ContainerSection`、`ContainerSectionCallback`、`IgnoreContainer`、`InventoryContainer`
- **`thaumcraft/api`（3）**：`crafting/IInfusionStabiliser`、`crafting/IInfusionStabiliserExt`、`items/IVisDiscountGear`

---

## 7. `common` 包（核心实现，`src/main/java/com/shinoow/abyssalcraft/common`，462 个）

- **`common`（根，2）**：`AbyssalCrafting`（合成/配方注册）、`CommonProxy`
- **`common/actions`（3）**：`CreateAltarAction`、`CreatePlaceOfPowerAction`、`PerformRitualAction`
- **`common/datafix`（2）**：`BlockFlattening`、`BlockFlatteningDefinitions`
- **`common/command`（1）**：`CommandUnlockAllKnowledge`
- **`common/handlers`（8）**：`AbyssalCraftEventHooks`、`GuiHandler`、`IMCHandler`、`InternalMethodHandler`、`InternalNecroDataHandler`、`KnowledgeEventHandler`、`PlagueEventHandler`、`PurgeEventHandler`
- **`common/pathfinding`（2）**：`PatchedPathNavigateClimber`、`PatchedPathNavigateGround`
- **`common/potion`（2）**：`PotionBuilder`、`PotionEffectUtil`
- **`common/enchantments`（5）**：`EnchantmentBlindingLight`、`EnchantmentIronWall`、`EnchantmentLightPierce`、`EnchantmentMultiRend`、`EnchantmentSapping`
- **`common/util`（8）**：`ACLogger`、`ArmorUtil`、`BiomeUtil`、`ExplosionUtil`、`GuiInstanceBase`、`ShapedFluidContainerRecipe`、`SoftDepUtil`、`StructureUtil`

### 7.1 方块 `common/blocks`

- **`common/blocks`（根，61）**：`BlockAbyssalSand`、`BlockAbyssalSandGlass`、`BlockAltar`、`BlockAntiliquid`、`BlockCalcifiedStone`、`BlockCLiquid`、`BlockCrate`、`BlockCrystalCluster`、`BlockCrystallizer`、`BlockDecorativeStatue`、`BlockDGhead`、`BlockDreadGrass`、`BlockDreadlandsDirt`、`BlockDreadlandsMuck`、`BlockEnergyCollector`、`BlockEnergyContainer`、`BlockEnergyDepositioner`、`BlockEnergyPedestal`、`BlockEnergyRelay`、`BlockEthaxiumPillar`、`BlockFusedAbyssalSand`、`BlockHouse`、`BlockIdolOfFading`、`BlockLuminousThistle`、`BlockMaterializer`、`BlockMimicFire`、`BlockMonolithPillar`、`BlockMultiblock`、`BlockMural`、`BlockODB`、`BlockODBcore`、`BlockPortalAnchor`、`BlockPSDL`、`BlockRendingPedestal`、`BlockResearchTable`、`BlockRitualAltar`、`BlockRitualPedestal`、`BlockSacrificialAltar`、`BlockSealingLock`、`BlockSequentialBrewingStand`、`BlockShoggothBiomass`、`BlockShoggothOoze`、`BlockSingleMobSpawner`、`BlockSolidLava`、`BlockSpiritAltar`、`BlockStateTransformer`、`BlockStatue`、`BlockSummoningStatue`、`BlockSummoningStatueBase`、`BlockSummoningStatueTop`、`BlockTieredEnergyCollector`、`BlockTieredEnergyContainer`、`BlockTieredEnergyPedestal`、`BlockTieredEnergyRelay`、`BlockTieredSacrificialAltar`、`BlockTombstone`、`BlockTransmutator`、`BlockUnchainedPortalAnchor`、`BlockUnlockedSealingLock`、`BlockWastelandsThorn`、`IngotBlock`
- **`common/blocks/baseblocks`（19）**：`BlockACBasic`、`BlockACBrick`、`BlockACButton`、`BlockACCobblestone`、`BlockACDoor`、`BlockACDoubleSlab`、`BlockACFence`、`BlockACFenceGate`、`BlockACHorizontal`、`BlockACLeaves`、`BlockACLog`、`BlockACOre`、`BlockACPressureplate`、`BlockACSapling`、`BlockACSingleSlab`、`BlockACSlab`、`BlockACStairs`、`BlockACStone`、`BlockACWall`
- **`common/blocks/itemblock`（9）**：`ItemBlockAC`、`ItemBlockColorName`、`ItemCrystalClusterBlock`、`ItemDecorativeStatueBlock`、`ItemODB`、`ItemPEContainerBlock`、`ItemRendingPedestalBlock`、`ItemShoggothOoze`、`ItemSlabAC`
- **`common/blocks/tile`（37，方块实体）**：`TileEntityChagarothSpawner`、`TileEntityCrate`、`TileEntityCrystallizer`、`TileEntityDGhead`、`TileEntityDreadguardSpawner`、`TileEntityEnergyCollector`、`TileEntityEnergyContainer`、`TileEntityEnergyDepositioner`、`TileEntityEnergyPedestal`、`TileEntityEnergyRelay`、`TileEntityGatekeeperMinionSpawner`、`TileEntityIdolOfFading`、`TileEntityJzaharSpawner`、`TileEntityMaterializer`、`TileEntityMultiblock`、`TileEntityOhead`、`TileEntityPhead`、`TileEntityPortalAnchor`、`TileEntityRendingPedestal`、`TileEntityResearchTable`、`TileEntityRitualAltar`、`TileEntityRitualPedestal`、`TileEntitySacrificialAltar`、`TileEntitySequentialBrewingStand`、`TileEntityShoggothBiomass`、`TileEntitySpiritAltar`、`TileEntityStateTransformer`、`TileEntityStatue`、`TileEntityTieredEnergyCollector`、`TileEntityTieredEnergyContainer`、`TileEntityTieredEnergyPedestal`、`TileEntityTieredEnergyRelay`、`TileEntityTieredSacrificialAltar`、`TileEntityTombstone`、`TileEntityTransmutator`、`TileEntityUnlockedSealingLock`、`TileEntityWhead`

### 7.2 实体 `common/entity`

- **`common/entity`（根，26）**：`EntityAbyssalZombie`、`EntityChagaroth`、`EntityChagarothFist`、`EntityChagarothSpawn`、`EntityCoraliumSquid`、`EntityDragonBoss`、`EntityDragonMinion`、`EntityDreadguard`、`EntityDreadling`、`EntityDreadSpawn`、`EntityGatekeeperMinion`、`EntityGreaterDreadSpawn`、`EntityGreaterShoggoth`、`EntityJzahar`、`EntityLesserDreadbeast`、`EntityLesserShoggoth`、`EntityRemnant`、`EntityRemnantTrader`、`EntitySacthoth`、`EntityShadowBeast`、`EntityShadowCreature`、`EntityShadowMonster`、`EntityShoggoth`、`EntityShoggothBase`、`EntityShubOffspring`、`EntitySkeletonGoliath`
- **`common/entity/ai`（6）**：`EntityAIAntiCreeperSwell`、`EntityAIAttackRangedBowAnti`、`EntityAIChagarothAttackMelee`、`EntityAIShoggothAttackMelee`、`EntityAIShoggothBuildMonolith`、`EntityAIWorship`
- **`common/entity/anti`（11）**：`EntityAntiAbyssalZombie`、`EntityAntiBat`、`EntityAntiChicken`、`EntityAntiCow`、`EntityAntiCreeper`、`EntityAntiGhoul`、`EntityAntiPig`、`EntityAntiPlayer`、`EntityAntiSkeleton`、`EntityAntiSpider`、`EntityAntiZombie`
- **`common/entity/base`（2）**：`EntityClimbingMobBase`、`EntityMobBase`
- **`common/entity/demon`（10）**：`EntityDemonAnimal`、`EntityDemonChicken`、`EntityDemonCow`、`EntityDemonPig`、`EntityDemonSheep`、`EntityEvilAnimal`、`EntityEvilChicken`、`EntityEvilCow`、`EntityEvilpig`、`EntityEvilSheep`
- **`common/entity/ghoul`（6）**：`EntityDepthsGhoul`、`EntityDreadedGhoul`、`EntityGhoul`、`EntityGhoulBase`、`EntityOmotholGhoul`、`EntityShadowGhoul`
- **`common/entity/misc`（10）**：`EntityBlackHole`、`EntityCompassTentacle`、`EntityGatekeeperEssence`、`EntityImplosion`、`EntityODBcPrimed`、`EntityODBPrimed`、`EntityPortal`、`EntityPSDLTracker`、`EntitySinglePortal`、`EntitySpiritItem`
- **`common/entity/projectile`（5）**：`EntityAcidProjectile`、`EntityCoraliumArrow`、`EntityDreadedCharge`、`EntityDreadSlug`、`EntityInkProjectile`

### 7.3 物品 `common/items`

- **`common/items`（根，30）**：`AbyssalCraftTool`、`ItemAntidote`、`ItemAntiFood`、`ItemCoin`、`ItemCoraliumBow`、`ItemCoraliumcluster`、`ItemCorb`、`ItemCorflesh`、`ItemCrystalBag`、`ItemCudgel`、`ItemDeprecated`、`ItemDreadiumKatana`、`ItemEmbossedRing`、`ItemEthaxiumPickaxe`、`ItemFaceBook`、`ItemGatekeeperEssence`、`ItemGatewayKey`、`ItemGhoulFlesh`、`ItemInterdimensionalCage`、`ItemNecronomicon`、`ItemOC`、`ItemPage`、`ItemPowerstoneTracker`、`ItemScriptures`、`ItemScroll`、`ItemSoulReaper`、`ItemSpiritTablet`、`ItemStaff`、`ItemStaffOfRending`、`ItemStoneTablet`
- **`common/items/armor`（8）**：`ItemAbyssalniteArmor`、`ItemACArmor`、`ItemCoraliumArmor`、`ItemCoraliumPArmor`、`ItemDepthsArmor`、`ItemDreadiumArmor`、`ItemDreadiumSamuraiArmor`、`ItemEthaxiumArmor`

### 7.4 世界与维度 `common/world`

- **`common/world`（根，16）**：`AbyssalCraftWorldGenerator`、`ACExplosion`、`BiomeProviderAbyssalWasteland`、`BiomeProviderDreadlands`、`ChunkGeneratorAbyssalWasteland`、`ChunkGeneratorDarkRealm`、`ChunkGeneratorDreadlands`、`ChunkGeneratorOmothol`、`DarklandsStructureGenerator`、`TeleporterAC`、`TeleporterHomeSpell`、`TeleporterSinglePortal`、`WorldProviderAbyssalWasteland`、`WorldProviderDarkRealm`、`WorldProviderDreadlands`、`WorldProviderOmothol`
- **`common/world/biome`（21）**：`BiomeAbyssalDesert`、`BiomeAbyssalPlateau`、`BiomeAbyssalSwamp`、`BiomeAbyssalWasteland`、`BiomeAbyssalWastelandBase`、`BiomeCoraliumLake`、`BiomeCorSwamp`、`BiomeDarklands`、`BiomeDarklandsBase`、`BiomeDarklandsForest`、`BiomeDarklandsHills`、`BiomeDarklandsMountains`、`BiomeDarklandsPlains`、`BiomeDarkRealm`、`BiomeDreadlands`、`BiomeDreadlandsBase`、`BiomeForestDreadlands`、`BiomeMountainDreadlands`、`BiomeOceanDreadlands`、`BiomeOmothol`、`BiomePurged`
- **`common/world/data`（1）**：`NecromancyWorldSavedData`
- **`common/world/gen`（13）**：`MapGenCavesAC`、`MapGenCavesDreadlands`、`MapGenRavineAC`、`WorldGenAbyLake`、`WorldGenAbyssalStalagmite`、`WorldGenAntimatterLake`、`WorldGenDeadTree`、`WorldGenDLT`、`WorldGenDreadlandsStalagmite`、`WorldGenDrT`、`WorldGenNoTree`、`WorldGenShoggothMonolith`、`WorldGenTreeAC`
- **`common/world/gen/layer`（8）**：`GenLayerAW`、`GenLayerBiomesAW`、`GenLayerBiomesDL`、`GenLayerDL`、`GenLayerHillsAW`、`GenLayerHillsDL`、`GenLayerRiverAW`、`GenLayerRiverDL`

### 7.5 结构 `common/structures`

- **`common/structures`（根，3）**：`StructureGraveyard`、`StructureHouse`、`StructureShoggothPit`
- **`common/structures/abyss`（2）**：`Abyruin`、`Chains`
- **`common/structures/abyss/stronghold`（2）**：`MapGenAbyStronghold`、`StructureAbyStrongholdPieces`
- **`common/structures/dreadlands`（2）**：`chagarothlair`、`StructureLairEntrance`
- **`common/structures/dreadlands/mineshaft`（3）**：`MapGenDreadlandsMine`、`StructureDreadlandsMinePieces`、`StructureDreadlandsMineStart`
- **`common/structures/omothol`（5）**：`StructureCity`、`StructureJzaharTemple`、`StructureStorage`、`StructureTemple`、`StructureTower`
- **`common/structures/overworld`（12）**：`AChouse1`、`AChouse2`、`ACscion1`、`ACscion2`、`StructureCircularShrine`、`StructureCircularShrineColumns`、`StructureDarklandsBase`、`StructureDarkShrine`、`StructureElevatedShrine`、`StructureElevatedShrineLarge`、`StructureRitualGrounds`、`StructureRitualGroundsColumns`
- **`common/structures/pe`（3，力量之地多方块）**：`ArchwayStructure`、`BasicStructure`、`TotemPoleStructure`

### 7.6 仪式 / 法术 / 扰动

- **`common/ritual`（13，具体仪式实现）**：`NecronomiconBreedingRitual`、`NecronomiconCleansingRitual`、`NecronomiconCorruptionRitual`、`NecronomiconCuringRitual`、`NecronomiconDreadSpawnRitual`、`NecronomiconHouseRitual`、`NecronomiconInfestingRitual`、`NecronomiconMassEnchantRitual`、`NecronomiconPortalRitual`、`NecronomiconPurgingRitual`、`NecronomiconRespawnJzaharRitual`、`NecronomiconResurrectionRitual`、`NecronomiconWeatherRitual`
- **`common/spells`（14）**：`CompassSpell`、`DetachmentSpell`、`EntropySpell`、`FloatingSpell`、`GraspofCthulhuSpell`、`InvisibilitySpell`、`LifeDrainSpell`、`MiningSpell`、`OozeRemovalSpell`、`SirensSongSpell`、`StealVigorSpell`、`TeleportHomeSpell`、`TeleportHostilesSpell`、`UndeathtoDustSpell`
- **`common/disruptions`（16，势能扰动效果）**：`DisruptionAnimalCorruption`、`DisruptionDisplaceEntities`、`DisruptionDrainNearbyPE`、`DisruptionFamine`、`DisruptionFire`、`DisruptionFireRain`、`DisruptionFreeze`、`DisruptionInvisibleSwarm`、`DisruptionLightning`、`DisruptionMonolith`、`DisruptionOoze`、`DisruptionPotentialEnergy`、`DisruptionRandomSpawn`、`DisruptionRandomSwarm`、`DisruptionSacrificeCorruption`、`DisruptionTeleportRandomly`

### 7.7 容器 / 网络

- **`common/inventory`（30）**：
  - 容器（12）：`ContainerCrystalBag`、`ContainerCrystallizer`、`ContainerEnergyContainer`、`ContainerEnergyDepositioner`、`ContainerMaterializer`、`ContainerRendingPedestal`、`ContainerResearchTable`、`ContainerSequentialBrewingStand`、`ContainerSpellbook`、`ContainerSpiritTablet`、`ContainerStateTransformer`、`ContainerTransmutator`
  - 内部库存（5）：`InventoryCrystalBag`、`InventoryMaterializer`、`InventorySpellbook`、`InventorySpellbookOutput`、`InventorySpiritTablet`
  - 槽位（13）：`SlotCrystal`、`SlotCrystalBag`、`SlotCrystallizer`、`SlotDepositionerOutput`、`SlotEnergyContainer`、`SlotMaterializer`、`SlotNecronomicon`、`SlotNoInventory`、`SlotRendingOutput`、`SlotRendingStaff`、`SlotSpellOutput`、`SlotStoneTablet`、`SlotTransmutator`
- **`common/network`（根，2）**：`AbstractMessage`、`PacketDispatcher`
- **`common/network/client`（12，S→C）**：`CleansingRitualMessage`、`DisplayRoutesMessage`、`DisruptionMessage`、`EvilSheepMessage`、`KnowledgeUnlockMessage`、`NecroDataCapMessage`、`PEStreamMessage`、`RitualMessage`、`RitualStartMessage`、`ShouldSyncMessage`、`SyncNecromancyDataMessage`、`WindowPropertyMessage`
- **`common/network/server`（11，C→S）**：`FireMessage`、`InterdimensionalCageMessage`、`MobSpellMessage`、`OpenSpellbookMessage`、`PrepareSyncMessage`、`SpiritTabletMessage`、`StaffModeMessage`、`StaffOfRendingMessage`、`ToggleStateMessage`、`TransferStackMessage`、`UpdateModeMessage`

---

## 8. `client` 包（客户端，`src/main/java/com/shinoow/abyssalcraft/client`，151 个）

- **`client`（根，1）**：`ClientProxy`
- **`client/config`（2）**：`ACConfigGUI`、`ACGuiFactory`
- **`client/util`（1）**：`NecronomiconGuiHelper`
- **`client/handlers`（3）**：`AbyssalCraftClientEventHooks`、`ArmorDataReloadListener`、`ClientVarsReloadListener`
- **`client/particles`（4）**：`ACParticleFX`、`BlueFlameParticle`、`ItemRitualParticle`、`PEStreamParticleFX`
- **`client/gui`（13）**：`GuiCrystalBag`、`GuiCrystallizer`、`GuiEnergyContainer`、`GuiEnergyDepositioner`、`GuiFaceBook`、`GuiMaterializer`、`GuiRendingPedestal`、`GuiResearchTable`、`GuiSequentialBrewingStand`、`GuiSpellbook`、`GuiSpiritTablet`、`GuiStateTransformer`、`GuiTransmutator`
- **`client/gui/necronomicon`（3）**：`GuiNecronomicon`、`GuiNecronomiconRecipeBase`、`GuiNecronomiconSpells`
- **`client/gui/necronomicon/buttons`（4）**：`ButtonCategory`、`ButtonHome`、`ButtonInfo`、`ButtonNextPage`
- **`client/gui/necronomicon/entries`（9）**：`GuiNecronomiconAnvilEntry`、`GuiNecronomiconChapterEntry`、`GuiNecronomiconCrystallizerEntry`、`GuiNecronomiconEntry`、`GuiNecronomiconMaterializerEntry`、`GuiNecronomiconPlacesOfPowerEntry`、`GuiNecronomiconRitualEntry`、`GuiNecronomiconSpellEntry`、`GuiNecronomiconTransmutatorEntry`
- **`client/model/block`（2）**：`ModelDGhead`、`ModelJzaharSpawner`
- **`client/model/item`（1）**：`ModelDreadiumSamuraiArmor`
- **`client/model/player`（1）**：`ModelStarSpawnPlayer`
- **`client/model/entity`（28）**：`ModelAntiBat`、`ModelAntiSkeleton`、`ModelChagaroth`、`ModelChagarothFist`、`ModelChagarothSpawn`、`ModelDemonSheep1`、`ModelDemonSheep2`、`ModelDragonBoss`、`ModelDragonMinion`、`ModelDreadling`、`ModelDreadSpawn`、`ModelDreadTentacles`、`ModelEvilSheep1`、`ModelEvilSheep2`、`ModelGatekeeperMinion`、`ModelGhoul`、`ModelGhoulArmor`、`ModelJzahar`、`ModelLesserShoggoth`、`ModelRemnant`、`ModelRemnantTrader`、`ModelSacthoth`、`ModelShadowBeast`、`ModelShadowCreature`、`ModelShadowMonster`、`ModelShubOffspring`、`ModelSkeletonGoliath`、`ModelSkeletonGoliathArmor`
- **`client/render/sky`（1）**：`ACSkyRenderer`
- **`client/render/item`（1）**：`RenderCoraliumArrow`
- **`client/render/block`（5）**：`RenderODB`、`RenderODBc`、`TileEntityJzaharSpawnerRenderer`、`TileEntityResearchTableRenderer`、`TileEntityUnlockedSealingLockRenderer`
- **`client/render/entity`（54）**：`RenderAbyssalZombie`、`RenderAntiAbyssalZombie`、`RenderAntiBat`、`RenderAntiChicken`、`RenderAntiCow`、`RenderAntiCreeper`、`RenderAntiGhoul`、`RenderAntiPig`、`RenderAntiPlayer`、`RenderAntiSkeleton`、`RenderAntiSpider`、`RenderAntiZombie`、`RenderBlackHole`、`RenderChagaroth`、`RenderChagarothFist`、`RenderChagarothSpawn`、`RenderCompassTentacle`、`RenderCoraliumSquid`、`RenderDemonChicken`、`RenderDemonCow`、`RenderDemonPig`、`RenderDemonSheep`、`RenderDepthsGhoul`、`RenderDragonBoss`、`RenderDragonMinion`、`RenderDreadedCharge`、`RenderDreadedGhoul`、`RenderDreadguard`、`RenderDreadling`、`RenderDreadSpawn`、`RenderEvilChicken`、`RenderEvilCow`、`RenderEvilPig`、`RenderEvilSheep`、`RenderGatekeeperMinion`、`RenderGhoul`、`RenderGhoulBase`、`RenderGreaterDreadSpawn`、`RenderImplosion`、`RenderJzahar`、`RenderLesserDreadbeast`、`RenderOmotholGhoul`、`RenderPortal`、`RenderRemnant`、`RenderRemnantTrader`、`RenderSacthoth`、`RenderShadowBeast`、`RenderShadowCreature`、`RenderShadowGhoul`、`RenderShadowMonster`、`RenderShoggoth`、`RenderShubOffspring`、`RenderSinglePortal`、`RenderSkeletonGoliath`
- **`client/render/entity/layers`（18）**：`LayerAbyssalZombieEyes`、`LayerAsorahDeath`、`LayerAsorahEyes`、`LayerDemonSheepWool`、`LayerDreadguardArmor`、`LayerDreadTentacles`、`LayerEvilSheepWool`、`LayerEyes`、`LayerGhoulArmor`、`LayerGhoulHeldItem`、`LayerJzaharDeath`、`LayerSacthothHeldItem`、`LayerShoggothEyes`、`LayerShubOffspringEyes`、`LayerSkeletonGoliathArmor`、`LayerSkeletonGoliathHeldItem`、`LayerSpectralDragonEyes`、`LayerStarSpawnTentacles`

---

## 9. `lib` 包（内部库/常量，`src/main/java/com/shinoow/abyssalcraft/lib`，56 个）

- **`lib`（根，11）**：`ACAchievements`、`ACClientVars`、`ACConfig`、`ACLib`、`ACLoot`、`ACSounds`、`ACTabs`、`Crystals`、`ItemPoses`、`NecronomiconResources`、`NecronomiconText`
- **`lib/block`（2）**：`BlockSingletonInventory`、`BlockTiltablePedestal`
- **`lib/item`（10）**：`ItemACAxe`、`ItemACBasic`、`ItemACHoe`、`ItemACPickaxe`、`ItemACShovel`、`ItemACSword`、`ItemCharm`、`ItemCrystal`、`ItemCrystalFragment`、`ItemCrystalShard`
- **`lib/tileentity`（3）**：`TEDirectional`、`TileEntityIdolBase`、`TileEntitySingleMobSpawner`
- **`lib/util`（12）**：`ClientVars`、`IHiddenRitual`、`MultiblockUtil`、`NecroDataJsonUtil`、`ParticleUtil`、`RitualUtil`、`ScheduledProcess`、`Scheduler`、`SoundUtil`、`SpecialTextUtil`、`SpiritItemUtil`、`TranslationUtil`
- **`lib/util/blocks`（2）**：`BlockUtil`、`ITieredBlock`
- **`lib/util/items`（2）**：`IOuterArmor`、`IStaffOfRending`
- **`lib/client`（3）**：`GuiRenderHelper`、`LovecraftFont`、`MultiblockRenderData`
- **`lib/client/model`（1）**：`ModelArmoredBase`
- **`lib/client/render`（2）**：`TileEntityDirectionalRenderer`、`TileEntitySingletonInventoryBlockRenderer`
- **`lib/client/render/data`（3）**：`AltarPose`、`ItemRenderingPose`、`RendingPedestalPose`
- **`lib/client/render/entity/layers`（2）**：`LayerACArmorBase`、`LayerOuterBipedArmor`
- **`lib/world`（1）**：`TeleporterDarkRealm`
- **`lib/world/biome`（2）**：`IAlternateSpawnList`、`IControlledSpawnList`

---

## 10. `integration/jei` 包（JEI 集成，`src/main/java/com/shinoow/abyssalcraft/integration`，30 个）

- **`integration/jei`（根，1）**：`ACJEIPlugin`
- **`integration/jei/util`（5）**：`ACRecipeBackgrounds`、`ACRecipeCategoryBase`、`ACRecipeCategoryUid`、`ACRecipeMaker`、`JEIUtils`
- **`integration/jei/anvil`（2）**：`AnvilForgingRecipeCategory`、`AnvilForgingRecipeWrapper`
- **`integration/jei/crystallizer`（5）**：`CrystallizationCategory`、`CrystallizationRecipeWrapper`、`CrystallizerFuelCategory`、`CrystallizerFuelRecipeWrapper`、`CrystallizerRecipeCategory`
- **`integration/jei/materializer`（2）**：`MaterializationRecipeCategory`、`MaterializationRecipeWrapper`
- **`integration/jei/rending`（2）**：`RendingRecipeCategory`、`RendingRecipeWrapper`
- **`integration/jei/ritual`（6）**：`CreationRitualRecipeCategory`、`CreationRitualRecipeWrapper`、`RitualRecipeCategory`、`RitualRecipeWrapper`、`TransformationRitualRecipeCategory`、`TransformationRitualRecipeWrapper`
- **`integration/jei/spell`（2）**：`SpellRecipeCategory`、`SpellRecipeWrapper`
- **`integration/jei/transmutator`（5）**：`TransmutationCategory`、`TransmutationRecipeWrapper`、`TransmutatorFuelCategory`、`TransmutatorFuelRecipeWrapper`、`TransmutatorRecipeCategory`

---

## 11. 资源（`src/main/resources`）

**资源根**：`mcmod.info`、`pack.mcmeta`、`version.properties`。
**资产根 `assets/abyssalcraft/`**：`clientvars.json`（客户端变量，热重载）、`sounds.json`（音效注册表）。

### 11.1 语言 `lang/`（8，`.lang`）
`en_us`（翻译源）、`es_es`、`fr_fr`、`ja_jp`、`ko_kr`、`ru_ru`、`zh_cn`、`zh_tw`。

### 11.2 进度 `advancements/`（9，`.json`）
`root`、`dreadium`、`ethaxium`、`mine_abyssal_coralium`、`mine_abyssal_ores`、`mine_abyssalnite`、`mine_coralium`、`mine_dreadlands_ores`、`shadow_gems`。

### 11.3 方块状态 `blockstates/`（280，`.json`）
扁平目录，每个方块（含各类楼梯/台阶/栅栏/墙/按钮/压力板/门/晶簇/雕像/多方块的变体与 tiered 维度前缀 `overworld_/abyssal_wasteland_/dreadlands_/omothol_`）一份。命名与方块注册名一致（如 `ritual_pedestal_stone.json`、`overworld_energy_relay.json`、`*_crystal_cluster.json`、`decorative*statue.json`）。

### 11.4 模型 `models/`（640，`.json`）
- `models/block/` — 方块模型（含 `layered_*` 分层模型、`summoning_statue/` 子目录等）。
- `models/item/` — 物品与方块物品模型（命名镜像 `ACItems`/`ACBlocks` 注册 id，如 `dreadiumsamuraiplate.json`、`ritualpedestal_0..7.json`、`crystal*.json`）。

### 11.5 配方 `recipes/`（402，`.json`）
扁平目录的原版合成/熔炼配方 + `_constants.json`（datagen 常量）。包含大量 `_alt`/`_alt_alt` 等价配方变体，以及晶体系统（`crystal_*`、`crystalshard_*`、`crystalfragment_*`、`crystalcluster*_*`）、装备（`cor*`、`dreadium*`、`ethaxium*`、`a*` 深渊石套）与建材（各 `*brick*`/`*cobblestone*`/`*slab*`/`*stairs*`/`*fence*`）系列。

### 11.6 贴图 `textures/`（644，`.png`）
子目录树：
- `textures/`（根）：`logo.png`
- `textures/armor/`：`default`、`abyssalnite_1/2`、`coralium_1/2`、`coraliump_1/2`、`depths_1/2_inner/outer`、`dreadium_1/2`、`dreadiums_1/2`、`ethaxium_1/2`；子目录 `ghoul/`、`skeleton_goliath/`（各含 base/chainmail/leather 变体与 overlay）
- `textures/blocks/`：方块贴图 + 子目录 `altar/`、`coralium_bricks/`、`ethaxium_bricks/`、`ores/`、`pe/`、`portalanchor/`、`ritualaltar/`、`ritualpedestal/`、`summoning_statue/`
- `textures/environment/`：`abyssal_wasteland_sky`、`dreadlands_sky`、`omothol_sky`
- `textures/font/`：`aklo`（Lovecraft/Aklo 字体）
- `textures/gui/`：GUI 贴图 + 子目录 `container/`、`necronomicon/`（再含 `biomes/`、`pe/`、`structures/`、`transport/`）
- `textures/items/`：物品贴图 + 子目录 `charms/`、`oblivion_catalyst/`、`scrolls/`、`spirit_tablet/`、`transmutation_gem/`
- `textures/misc/`：`coraliumblur`、`potionfx`
- `textures/model/`：实体/方块实体模型贴图 + 子目录 `anti/`、`blocks/`、`boss/`、`elite/`、`ghoul/`、`remnant/`（含 `trader/`）、`shoggoth/`
- `textures/particles/`：`blueflame`

### 11.7 音效 `sounds/`（106，`.ogg`；经 `sounds.json` 注册）
按文件夹组织：
- `sounds/abyzombie/`：`idle1–5`、`hurt1–5`、`death`
- `sounds/antiplayer/`：`hurt`
- `sounds/chants/`（吟唱，用于仪式/BOSS）：`cthugha_1–4`、`cthulhu_1–4`、`hastur_1–4`、`hastur_2`、`hastur_2_1–4`、`sleeping_1–4`、`yog_sothoth_1–4`、`yog_sothoth_2`、`yog_sothoth_2_1–4`
- `sounds/dreadguard/`：`idle1–3`、`hit1–4`、`death`、`barf`
- `sounds/ghoul/`：`hurt1–5`、`death`；子目录 `normal/`、`wilson/`、`pete/`、`orange/`（各 `idle*`）
- `sounds/jzahar/`：`shout`、`implosion`、`earthquake`、`charge`、`blast`、`black_hole`
- `sounds/misc/`：`compass`
- `sounds/remnant/`：`yes`、`no`、`scream`
- `sounds/shadow/`：`hurt1–3`、`death`；子目录 `sacthoth/death`
- `sounds/shoggoth/`：`idle1–5`、`hurt1–5`、`death`

### 11.8 战利品表 `loot_tables/`（78，`.json`）
**`loot_tables/entities/`（69）**：`abyssal_anti_zombie`、`abyssal_shoggoth`、`abyssal_zombie`、`anti_bat`、`anti_chicken`、`anti_cow`、`anti_creeper`、`anti_ghoul`、`anti_pig`、`anti_player`、`anti_skeleton`、`anti_spider`、`anti_zombie`、`asorah`、`chagaroth`、`coralium_infested_squid`、`demon_chicken`、`demon_cow`、`demon_pig`、`demon_sheep`、`depths_ghoul`、`depths_ghoul_orange`、`depths_ghoul_pete`、`depths_ghoul_wilson`、`dread_ghoul`、`dread_spawn`、`dreaded_shoggoth`、`dreadguard`、`dreadling`、`evil_chicken`、`evil_cow`、`evil_pig`、`evil_sheep`、`fist_of_chagaroth`、`ghoul`、`greater_abyssal_shoggoth`、`greater_dread_spawn`、`greater_dreaded_shoggoth`、`greater_omothol_shoggoth`、`greater_shadow_shoggoth`、`greater_shoggoth`、`jzahar`、`lesser_abyssal_shoggoth`、`lesser_dreadbeast`、`lesser_dreaded_shoggoth`、`lesser_omothol_shoggoth`、`lesser_shadow_shoggoth`、`lesser_shoggoth`、`minion_of_the_gatekeeper`、`omothol_ghoul`、`omothol_shoggoth`、`remnant`、`remnant_banker`、`remnant_blacksmith`、`remnant_butcher`、`remnant_librarian`、`remnant_master_blacksmith`、`remnant_priest`、`sacthoth`、`shadow_beast`、`shadow_creature`、`shadow_ghoul`、`shadow_monster`、`shadow_shoggoth`、`shoggoth`、`shub_offspring`、`skeleton_goliath`、`spawn_of_chagaroth`、`spectral_dragon`
**`loot_tables/chests/`（9）**：`mineshaft`、`stronghold_corridor`、`stronghold_crossing`；`omothol/`：`blacksmith`、`farmhouse`、`house`、`library`、`storage_junk`、`storage_treasure`

### 11.9 结构 `structures/`（36，`.nbt`）
- `structures/temple/`（7）：`jzahartemple_back`、`jzahartemple_front_left`、`jzahartemple_front_middle`、`jzahartemple_front_right`、`jzahartemple_middle_left`、`jzahartemple_middle_middle`、`jzahartemple_middle_right`
- `structures/shrine/`（1）：`dark_shrine`
- `structures/graveyard/`（3）：`graveyard_large`、`graveyard_medium`、`graveyard_small`
- `structures/shoggothlair/`（3）：`shoggothlair_1`、`shoggothlair_2`、`shoggothlair_3`
- `structures/chagarothlair/`（7）：`chagarothlair_back`、`chagarothlair_entrance`、`chagarothlair_front`、`chagarothlair_middle`、`chagarothlair_middle_left`、`chagarothlair_middle_right`、`chagarothlair_top`
- `structures/omothol/`（15）：`bar`、`blacksmith`、`church`、`crates_1`、`crates_2`、`crates_3`、`crates_4`、`farm`、`farmhouse`、`house`、`library`、`storage`、`temple`、`tower_1`、`tower_2`

---

## 12. 游戏内容与系统总览（由类清单派生）

- **维度（4）**：Abyssal Wasteland（深渊荒原）、Dreadlands（恐惧之地）、Omothol、Dark Realm；各含 `WorldProvider*`/`ChunkGenerator*`，前两者另有 `BiomeProvider*` + `GenLayer*` 多群系。
- **生物群系（21 类）**：深渊荒原系（Desert/Plateau/Swamp/Wasteland/Base）、Darklands 系（含 Forest/Hills/Mountains/Plains）、Dreadlands 系（含 Forest/Mountain/Ocean）、Coralium Lake、CorSwamp、Dark Realm、Omothol、Purged。
- **机器 / 多方块**：Crystallizer（结晶器）、Materializer（物质化器）、Transmutator（嬗变器）、Sequential Brewing Stand、Research Table、Ritual Altar + Pedestal（仪式）、Rending Pedestal（撕裂）、Sacrificial Altar（含 tiered）、State Transformer、Spirit Altar、Summoning Statue、Portal Anchor（+Unchained）、Idol of Fading。
- **PE 势能系统**：Energy Collector/Container/Pedestal/Relay/Depositioner（均含 tiered 4 级）、Idol、Amplifier Charm、Places of Power（`structures/pe`：Archway/Basic/TotemPole）、16 种 Disruption 扰动。
- **知识 / 死灵之书**：研究系统（`api/knowledge` + `condition/` 解锁条件 + `caps/` 能力持久化）；书本变体贴图/模型：`necronomicon`、`necronomicon_cor/dre/omt`、`abyssalnomicon`；章节/页面/动作注册（`Chapters`/`Pages`/`NecronomiconActionRegistry`）。
- **仪式（13 具体）/ 法术（14）/ 附魔（5：BlindingLight、IronWall、LightPierce、MultiRend、Sapping）**。
- **实体**：BOSS（Chagaroth + Fist/Spawn、Jzahar/Asorah、Sacthoth、Skeleton Goliath、Dragon Boss/Spectral Dragon、Shub Offspring）、Ghoul 家族（6）、Shoggoth 家族（Lesser/Greater + 维度变体）、Anti 反物质生物（11）、Demon/Evil 动物（10）、Remnant（含 7 种交易村民职业）、misc（黑洞/传送门/爆炸物）与 5 种 projectile。
- **护甲套（8）**：Abyssalnite、AC 基础、Coralium、Coralium-Plated、Depths、Dreadium、Dreadium Samurai、Ethaxium。
- **软集成**：JEI（9 类配方分类 + 燃料）、InvTweaks、Thaumcraft（API 桩）。

---

## 13. 统计总表

| 类别 | 数量 | 位置 |
|---|---:|---|
| Java 源码（主） | 870 | `src/main/java/com/shinoow/abyssalcraft/**` |
| ├ `api` | 162 | 公开 API（`apiJar`） |
| ├ `common` | 462 | 核心实现 |
| ├ `client` | 151 | 客户端 |
| ├ `lib` | 56 | 内部库/常量 |
| ├ `integration` | 30 | JEI |
| ├ `init` | 8 | 生命周期处理器 |
| └ 根 | 1 | `AbyssalCraft.java` |
| Java 源码（第三方 API 桩） | 14 | `src/api/java/**`（invtweaks 11 + thaumcraft 3） |
| 贴图 `.png` | 644 | `assets/abyssalcraft/textures/**` |
| 模型 `.json` | 640 | `assets/abyssalcraft/models/**`（block + item） |
| 配方 `.json` | 402 | `assets/abyssalcraft/recipes/**` |
| 方块状态 `.json` | 280 | `assets/abyssalcraft/blockstates/**` |
| 音效 `.ogg` | 106 | `assets/abyssalcraft/sounds/**` |
| 战利品 `.json` | 78 | `assets/abyssalcraft/loot_tables/**`（entities 69 + chests 9） |
| 结构 `.nbt` | 36 | `assets/abyssalcraft/structures/**` |
| 进度 `.json` | 9 | `assets/abyssalcraft/advancements/**` |
| 语言 `.lang` | 8 | `assets/abyssalcraft/lang/**` |
| 其它资产 | 2 | `clientvars.json`、`sounds.json` |
| 资源元数据 | 3 | `mcmod.info`、`pack.mcmeta`、`version.properties` |

**合计**：Java 884 个（870 + 14）；资产 ~2205 个（644+640+402+280+106+78+36+9+8+2）。

> 备注：Java 类清单为逐包完整枚举（各子包计数之和与 `file_search` 返回的总数逐一吻合：api 162 / common 462 / client 151 / lib 56 / integration 30）。贴图/模型/配方/方块状态因体量以"子目录 + 计数 + 命名约定"形式给出，未逐一列出每个文件名；如需某资源目录的逐文件清单可另行展开。
