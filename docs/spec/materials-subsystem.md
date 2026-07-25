# materials-subsystem 子系统规格 (Subsystem Spec)

- 里程碑 / Stage：M1 / Stage B1（材料·建材·矿石·装备物品）
- 关联平行任务：PB-1（材料/晶体物品，本次交付）· PB-2（食物/杂项）· PB-3（建材方块）· PB-5（装饰方块）
- 状态：PB-1 已交付并**两节点验证**（build+runClient 117 物品模型/贴图/染色零告警）
- 负责：Agent 6（PB-1）
- 最后更新：2026-07-21

## 1. 概述 / 目标

AbyssalCraft 1.12.2 → 1.20.1/1.21.1 的**材料/晶体物品层**（T1.1）：117 个物品 = 39 基础材料 + 78 元素晶体。作为 M1 内容层的第一块，供后续矿石掉落（PB-4）、工具/护甲（PB-6/7）、配方（T1.9）引用。

## 2. 范围

- 含：39 基础材料（plain `Item`）+ 78 晶体（26 元素 × crystal/shard/fragment，按元素 `ItemColor` 染色）+ 自带 `materials` 创造页 + 真实贴图（从 1.12.2 移植）+ 手写模型 + lang。
- 不含：进食/工具/护甲逻辑（PB-2/6/7）；配方/标签（T1.9/T1.10）；datagen provider（`MaterialItemData` 延 PK，同机器手写先例）；晶体的化学式 tooltip / 特殊右键效果（后续里程碑）。
- **归属边界**：mob/维度掉落类"材料"（shoggoth flesh、essence、skin、anti-food、plagued flesh）归 PB-2（misc/food），不在 PB-1。

## 3. 设计 / 架构

### 物品清单
- 基础 39：ingot×4（abyssalnite/refined_coralium/dreadium/ethaxium）+ nugget×4 + coralium_gem + coralium_pearl + coralium_gem_cluster_2..9（8）+ chunk_of_coralium + dreaded_shard_of_abyssalnite + coralium_brick + ethaxium_brick + charcoal/methane/nitre/sulfur（dust）+ coralium_plate + dreadium_plate + shadow_fragment/shard/gem + shard_of_oblivion + dread_fragment + carbon_cluster + dense_carbon_cluster + dread_cloth + life_crystal + eldritch_scale + coin。
- 晶体 78：26 元素（`Crystals` 序：iron..beryl）× {`crystal_<e>`, `crystal_shard_<e>`, `crystal_fragment_<e>`}。全 plain `Item`。

### 注册（`content/item/material/MaterialItems`）
- `ITEMS`（`ModRegistrar<Item>`）+ `TABS`（`ModRegistrar<CreativeModeTab>`）。私有 `reg(name)` 注册并入 `ALL`（供创造页）。
- 循环注册：`BASIC[]` 39 + 26 元素 ×3（存入 `CRYSTALS`/`CRYSTAL_SHARDS`/`CRYSTAL_FRAGMENTS` 并行列表，供染色）。
- `MATERIALS_TAB`：vanilla `CreativeModeTab.builder().displayItems((p,out)-> ALL.forEach(...))` —— **零分叉**（不走 `BuildCreativeModeTabContentsEvent`）。
- 接线：`ModRegistries.ALL` 追加 `MaterialItems.ITEMS` + `.TABS`。

### 晶体染色（`platform/ClientColorCompat`，客户端）
- 灰度贴图（crystal/crystal_metal/gas/alloy 4 + shard 4 + fragment 1 = 9 张）按元素 `CRYSTAL_COLORS[i]` 用 `ItemColor` 乘算染色。
- `ClientColorCompat.queue(rgb, items...)` 存条目；`attach(modBus)` 挂 `RegisterColorHandlersEvent.Item` 监听器，`event.register((stack,tint)-> tint==0?rgb:0xFFFFFF, items)`。
- `ACClientSetup.registerItemColors()` 循环把 26 元素色 queue（每色覆盖该元素 crystal/shard/fragment 3 件）；主类 `SideExecutor.runWhenClient` 里 `ClientColorCompat.attach(modBus)`（客户端专属、服务端不加载）。

### 贴图 / 模型
- 1.12.2 贴图名 ≠ 注册名 → 读原 `models/item/<旧模型>.json` 的 `layer0` 建映射，`Copy-Item` 复制 48 张到 `textures/item/<注册名>.png`（晶体 9 张按 type 后缀重命名）。
- 手写 117 个 `models/item/<name>.json`（`item/generated` + `layer0`）；晶体按 `getCrystalTypeSuffix`（_metal/_gas/_alloy/""）指向对应 4 张 crystal/shard 之一（fragment 单张）。

## 4. 子系统内契约

- 物品 id：沿用 1.12.2 注册名（如 `abyssalnite_ingot`、`coralium_gem_cluster_2`、`crystal_iron`、`crystal_shard_gold`、`crystal_fragment_oxygen`、`coin`）。
- i18n：`item.abyssalcraft.<id>`；晶体名 "`<Element>` Crystal/Crystal Shard/Crystal Fragment"；创造页 `itemGroup.abyssalcraft.materials`。
- 晶体色表：`CRYSTAL_COLORS[26]`（源 1.12.2 `ACClientVars.crystalColors`，白 = `0xFFFFFF`）。

## 5. 跨版本 / 加载器要点（javap 双 jar 核）

- **`ItemColor` 注册双端同签名**：`net.minecraft.client.color.item.ItemColor.getColor(ItemStack,int)` 两端一致；`RegisterColorHandlersEvent.Item.register(ItemColor, ItemLike...)` 签名一致，**仅事件类包名分叉**（forge `net.minecraftforge.client.event` / neo `net.neoforged.neoforge.client.event`）→ `ClientColorCompat` 只 fork 该 import + `IEventBus` import。
- `CreativeModeTab.builder().displayItems(...)`、`Item`/`Item.Properties`、`ItemStack(ItemLike)` 双端同签名 → 注册 + 创造页业务零 `//?`。

## 6. 实现记忆 / 踩坑 (verified gotchas)

- **1.12.2 贴图名是缩写**（`ai`/`di`/`cg`/`cgca..cgch`/`sf`/`ss`/`sg`/`dsoa`/`acc`/`addc`…）→ 必须读原 item 模型 `layer0` 建映射，勿凭名猜。
- **晶体不需 78 张贴图**：4 type-类别贴图（灰度）+ `ItemColor` 按元素染色即可；模型按后缀指向 4 张之一，色由 handler 给。
- **创造页填充零分叉**：用 `CreativeModeTab.builder().displayItems`（vanilla）而非 `BuildCreativeModeTabContentsEvent`（分叉）；且各内容任务自带 tab（对齐 PA-5 demo tab 先例），并行安全、免抢 `ModCreativeTabs` 主 tab。
- **lang 并发写**：Stage B1 多 agent 同改 `en_us.json` → 用 read-merge-write 脚本（`ConvertFrom-Json` → 加键 → 手拼 JSON 写回）保留他人键，勿整段 replace。
- **registrar 位置**：任务字面写 `registry/MaterialItems.java`，但 §6 矩阵 + `CrystalItems`/`MiscItems` 先例是 `content/item/<cat>/` → 置 `content/item/material/`。

## 7. 验证 / DoD

### PB-1（2026-07-21，两节点，判据 = 日志/退出码 + disk + jar 字节）
- 两节点 `compileJava --rerun-tasks` + `build` 绿；remap jar 含 `MaterialItems`/`ClientColorCompat` class + 抽样 4 模型 + 3 贴图（ZipFile 核）。
- 两节点 `runClient`：117 物品模型/贴图烘焙**零告警**（`Unable to load model` 全为并发 PB-2/PB-5 未完成资产，无一 PB-1 物品）、`ItemColor` 注册零错、抵标题屏干净退出（neo Loaded 1293 recipes）。
- 曾被并发 PB-5 `DecoBlocks` 缺 1.21 `codec()` 短暂卡 neoforge 编译（非 PB-1）；PB-5 修复后两端全绿。
- **未机核（如实标注）**：晶体逐元素染色实际外观 + 创造页可取/可拿属客户端目视，需人工（开 materials 创造页看 117 物品 + 晶体颜色）。

## 8. 遗留 / TODO

- datagen `MaterialItemData`（模型 + lang）延 PK（可复用 PB-3 增强的 `DataGenCompat`）；手写资产先行。
- 配方（crystallization 产晶体链、材料合成）延 T1.9；标签（ingots/nuggets/gems）延 T1.10。
- 晶体化学式 tooltip（`ICrystal.getFormula`）+ 特殊右键（oxygen 补氧、sulfur 抗火）延后续里程碑。

## 修订日志

- 2026-07-21（PB-1）：材料/晶体物品层交付（117 物品 + `ClientColorCompat` 染色 + materials 创造页 + 移植贴图 + 手写模型 + lang）；两节点 build+runClient 零告警。首次建档。见平行表 CR-14。
