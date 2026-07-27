# 集成子系统规格 (Integration Subsystem Spec)

> 覆盖 Stage J（M8）：PJ-1 JEI、PJ-2 配置、PJ-3 命令、PJ-4 进度 + IMC/插件 API。

## 1. 概述 / 目标

把 AbyssalCraft 的"对外集成层"移植到双加载器：合成配方的 JEI 展示、全量配置、命令、进度（advancements），以及（延后的）IMC/插件 API。加载器差异全部收进 `platform/`。

## 2. 范围

**已交付**：
- **PJ-3 命令 ☑**：`system/command/ACCommands`（fork-free Brigadier）+ `platform/CommandCompat`（`RegisterCommandsEvent` fork）。命令 `/acunlockallknowledge`（忠实 1.12.2 `CommandUnlockAllKnowledge`）。
- **PJ-2 配置 ☑**：`config/ACConfig` 全 130 标量选项（COMMON 126 + CLIENT 4，12 类）经 `platform/ConfigCompat.Builder`。
- **PJ-2b 非标量配置 ☑**：blacklist/carrier/immunity/transformation/dimension mapping/RGB/矿参数以受校验 list/map 格式解析为 `ComplexConfig` 不可变快照，并在本模组 common config loading/reloading 刷新。
- **PJ-4 进度 ☑**：`data/abyssalcraft/advancements`（1.20.1 复数）+ `advancement`（1.21 单数）双目录各 9 进度。
- **PJ-1 JEI ◐**：`integration/jei/ACJEIPlugin`（PP-5 ※ 接力）+ 3 机器分类（PP-5）+ 新 `AnvilForgingCategory`；`platform/DataRecipeCompat.allOfType` 枚举 data-recipe。

**延后（硬阻塞，均分离为显式后续任务，见平行表「Stage J 延后收尾」）**：
- **PJ-1b**：JEI rending / ritual(creation+transformation) / spell 分类——rending 输入是实体（JEI 无原生实体槽）+ pedestal 机器未移植；ritual/spell 的 `RitualRegistry`/`SpellRegistry` 是 PS-6/PS-7 框架、零具体配方。
- **PJ-2c**：所有保留配置的跨系统生产消费者与游戏内配置 GUI；本轮仅 plague 名单、demon transformation 等已接消费者。
- ~~**PJ-4b**~~ **RR-ADV-API 已完成**：双端 `IACPlugin` ServiceLoader/显式注册、五项实体扩展生产 sink 与 Forge 旧 IMC 桥已落地；13项配方/Crystal/Ghoul贴图旧 key 明确迁移到 datapack/resource pack。

## 3. 设计 / 架构

**命令**：`ACCommands.register(CommandDispatcher<CommandSourceStack>)` 用 `Commands.literal(...).requires(hasPermission(2)).executes(...)` 建命令（fork-free）；`unlockAllKnowledge` 经 `NecroDataCapability.get(player)` toggle `hasUnlockedAllKnowledge`。`CommandCompat.attach()` 在 game-bus 订阅 `RegisterCommandsEvent`（唯一分叉点，仅 import），主类 init 调用。

**配置**：标量选项是 `Supplier<Boolean/Integer/Double>`；非标量经 `defineStringList/defineIntList` 存储，`ComplexConfig.reload()` 统一校验 ResourceLocation/字段/概率并发布不可变快照。`KnowledgeSetupCompat` 只响应本模组 common spec 的 loading/reloading。

**进度**：双目录数据包 JSON 由 `AdvancementKnowledge` 事实清单永久审计。Forge 1.20.1 使用 `advancements/` 与对象式 inventory predicate；NeoForge 1.21.1 使用 `advancement/`、`display.icon.id` 与字符串式 predicate。`root` 已恢复真实 Necronomicon 触发与现有 darkstone 背景。两端 `AdvancementEarnEvent` 将9项完成状态幂等写入 `advancementTriggers`，知识增量 type 7 + full snapshot 即时刷新已打开的书，并在登录时从 vanilla progress 回填旧玩家。

**插件 API**：`integration/api/{IACPlugin,ACPluginContext,ACPluginRegistry}` 以 `META-INF/services` 自动发现，按稳定ID在 `ServerAboutToStartEvent` 原子发布不可变快照；失败provider回滚。Shoggoth食物与两类瘟疫 immunity/carrier 均接入真实消费端。Forge `IMCCompat` 在 `InterModProcessEvent` 桥接5个旧实体key；NeoForge 21.1无loader IMC，使用类型化API。完整契约见 [`advancement-plugin-api.md`](advancement-plugin-api.md)。

**JEI**：`@JeiPlugin` 自动发现（缺 JEI 不崩，非 mods.toml 硬依赖，API modCompileOnly）。分类 extends JEI `IRecipeCategory`，`registerCategories`/`registerRecipes`（经 `RecipeCompat.allOfType`/`DataRecipeCompat.allOfType` 枚举配方）/`registerRecipeCatalysts`（催化剂方块）三段注册。

## 4. 子系统内契约

- 命令名 `acunlockallknowledge`（忠实 1.12.2），权限 lvl2。
- 配置路径 snake_case（`should_spread` 等），12 类：general/dimensions/mobs/rituals/shoggoth/worldgen/silly_settings/wet_noodle/mod_compat/spells/modules/ghoul + client。
- 进度 id 沿用 1.12.2（root/mine_*/ethaxium/dreadium/shadow_gems），item id 用现代注册名（`coralium_ore` 等）。PJ-4 初交付时因书未移植曾临时以 `dreadium_ingot` 代替 root；RR-ADV-API 已恢复 `necronomicon`。
- 上述 root 占位已在 RR-ADV-API 撤销：当前 icon/criterion 均为 `abyssalcraft:necronomicon`。
- JEI 分类 uid + lang 键 `gui.jei.category.abyssalcraft.<name>`。

## 5. 跨版本 / 加载器要点

- **命令 fork**：`RegisterCommandsEvent` Forge `net.minecraftforge.event` ↔ NeoForge `net.neoforged.neoforge.event`（`getDispatcher()` 同），仅 import；Brigadier / `Commands` / `CommandSourceStack` / `getPlayerOrException` / `sendSuccess(Supplier,boolean)` 两端同。
- **配置 fork**：`ForgeConfigSpec` ↔ `ModConfigSpec`（builder 面 + 值 `Supplier` 同），全封 `ConfigCompat`。
- **进度 fork**：目录名、icon字段和 inventory predicate codec 均不同，分别由双目录资源承载；Forge 曾有的9条 `Expected item to be a JsonObject` 已归零。
- **插件 fork**：ServiceLoader/API双端相同；仅Forge拥有`InterModProcessEvent`旧IMC桥，NeoForge 21.1无等价loader IMC。
- **JEI**：坐标/包按 loader + mc 版本分（build.gradle.kts；forge JEI 15 / neoforge JEI 19），API modCompileOnly + full jar modLocalRuntime；`DataRecipeCompat.allOfType` 的 `Container`↔`RecipeInput` fork 同既有 recipe 层。

## 6. 实现记忆 / 踩坑 (verified gotchas)

- `SimpleParticleType`-式无关；命令 `executes` 的 lambda 可抛 `CommandSyntaxException`（`getPlayerOrException` 抛之），Brigadier 接受。
- 配置的 "Incorrect key ... corrected to default" WARN 是**首次生成 toml 的正常行为**（空文件→全默认），非错误。
- `nightconfig WritingException: An I/O error occured` 是 **run/ 目录被并发 server 争用**（同时锁 `logs/latest.log`），非配置内容错——隔离 run 目录/端口后重跑即过（本子系统 355 行 toml 已成功生成）。
- `RecipeCompat.allOfType` 仅收 `ProcessingRecipe`（extends `RecipeCompat`）；`AnvilForgingRecipe` extends `DataRecipeCompat`（另一基类）→ 需 `DataRecipeCompat` 自带的 `allOfType`（本次新增）。
- necrodata client-sync handler 已由RR-KNOWLEDGE落地；命令执行后通过`KnowledgeSync.full`发送权威快照。完整有/无权限玩家与客户端书显示仍归T8.3b真人验收。
- RR-ADV-API 进一步让完整/增量同步在书已打开时调用 `rebuildWidgets`，命令与进度页面无需关闭重开。

## 7. 验证 / DoD

- 两节点 `compileJava` EXIT=0（命令/配置/JEI anvil + `DataRecipeCompat.allOfType` + `CommandCompat`/`RegisterCommandsEvent` fork）。
- **forge `runServer` `Done`**：配置生成 355 行 `abyssalcraft-common.toml`（12 类忠实默认+注释）+ 9 advancement 零加载错 + 控制台 `/acunlockallknowledge` → 「A player is required to run this command here」（证命令注册+抵执行，非 Unknown command）。
- **人工目视（未做）**：JEI 面板内分类/催化剂/跳转、带玩家跑命令解锁+客户端 GUI 反映、配置 GUI 改值——headless 不能开 JEI/带玩家/开菜单。CLIENT 配置（4 项）在 runClient 生成（未单独跑）。
- **RR-ADV-API 自动/实网**：双端 `compileJava/runData` 输出 `RR_ADV_API_SELF_TEST_OK advancements=9 schemas=2 retainedImc=5 retiredImc=13 pluginLifecycle=ok commandToggle=ok`；Forge/Neo外部fixture mod均由ServiceLoader发现并命中五类真实消费端，Forge另有5个真实IMC消息；双端9进度专服加载无解析错。真实联网 `RRAdvClient` 覆盖无权限拒绝、OP两次toggle、9进度事件与书页即时同步、重复幂等和同名重连持久化。

## 修订日志

- 2026-07-26：RR-ADV-API 完成进度双schema修复、9项独立Progression知识、命令/书即时同步、IACPlugin/ServiceLoader、Forge五项旧IMC与双端永久/外部消费者Gate。
- 2026-07-25：RR-KNOWLEDGE（CR-70）完成PJ-2b非标量定义/解析/reload与necrodata客户端同步；当时PJ-2c全消费者+GUI、T8.3b真人命令矩阵待办，后者已于2026-07-26由RR-ADV-API收口。
- 2026-07-22：初版（PJ-1..4，Stage J，CR-56）。命令 + 配置 + 进度双端交付并 runServer 实证；JEI 4 分类；rending/ritual/spell JEI、配置非标量、IMC/IACPlugin 延后分离为 PJ-1b/2b/4b。
