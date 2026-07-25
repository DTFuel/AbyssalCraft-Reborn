# 附魔 (Enchantment) 子系统规格 (Subsystem Spec)

- 里程碑 / Stage：M7 / Stage S-A
- 关联平行任务：PS-3（本层）；下游 PS-11（附魔效果 event hook）
- 状态：5 附魔双端可获得并验证（编译 + runServer `/enchant` 探针 + runClient）；效果延后 PS-11 / 未移植 Staff
- 负责：PS-3
- 最后更新：2026-07-22

## 1. 概述 / 目标

AbyssalCraft 的 5 个附魔（`blinding_light` / `iron_wall` / `light_pierce` / `multi_rend` / `sapping`，忠实 1.12.2 `AbyssalCraftAPI`）。玩家可见效果：可在附魔台/铁砧获得、显示名、附于对应装备。本层交付**可获得性 + metadata**；**效果逻辑**（受击零动量、致盲、对暗影生物加伤）落在事件处理子系统（PS-11），`multi_rend`/`sapping` 驱动未移植的 Staff of Rending。

## 2. 范围

- 含：`platform/EnchantmentCompat`（附魔注册的 loader fork）、`system/enchant/ACEnchantments`（fork-free `ResourceKey` 引用）、`data/abyssalcraft/enchantment/*.json`（1.21 datapack 定义）、`data/abyssalcraft/tags/item/enchantable/staff_of_rending.json`（Staff 分类空 tag）、5 en_us 键、主类 `bootstrap`。
- 不含：**附魔效果逻辑**（iron_wall / blinding_light / light_pierce）——属事件处理子系统 PS-11；`multi_rend`/`sapping` 的 rending 效果——属未移植的 Staff of Rending + rending 系统。

## 3. 设计 / 架构

- 包结构：`platform/EnchantmentCompat`（fork）· `system/enchant/ACEnchantments`（fork-free 引用）· `data/abyssalcraft/enchantment/*.json`（1.21 数据）。
- 关键类与职责：
  - `platform/EnchantmentCompat`：**1.20.1** `DeferredRegister<Enchantment>`(`Registries.ENCHANTMENT`) + 通用 `AbyssalEnchantment extends Enchantment`（参数化 rarity / category / slot / `getMinCost`·`getMaxCost` 公式 / `getMaxLevel`）注册 5；**1.21** `bootstrap` no-op（附魔来自 datapack）。`bootstrap(Object modBus)` 从主类调用。
  - `system/enchant/ACEnchantments`：5 `ResourceKey<Enchantment>` 常量（`ResourceKey.create(Registries.ENCHANTMENT, ACRef.id(name))`，两端稳定），业务/效果码统一引用。
  - 5 datapack JSON（1.21）：忠实 metadata（`description` / `supported_items` / `weight` / `max_level` / `min_cost` / `max_cost` / `anvil_cost` / `slots`），无 `effects`（定义型，同 vanilla `fortune`）。

## 4. 子系统内契约

- 附魔 id：`abyssalcraft:{blinding_light,iron_wall,light_pierce,multi_rend,sapping}`（两端一致；1.20.1 代码注册名 / 1.21 JSON 文件名）。
- i18n：`enchantment.abyssalcraft.<name>`（1.20.1 由注册 id 派生 / 1.21 JSON `description.translate`）。
- 忠实 metadata（1.12.2 → 现代）：

| 附魔 | rarity(→weight) | maxLevel | slot | supported_items(1.21) / category(1.20.1) | minCost/maxCost |
|---|---|---|---|---|---|
| blinding_light | COMMON(10) | 1 | offhand | `minecraft:shield` / BREAKABLE | 14 / 44 |
| iron_wall | UNCOMMON(5) | 1 | chest | `#minecraft:enchantable/chest_armor` / ARMOR_CHEST | 14 / 44 |
| light_pierce | COMMON(10) | 5 | mainhand | `#minecraft:enchantable/sharp_weapon` / WEAPON（+exclusive damage） | 5+8·(l-1) / +20 |
| multi_rend | RARE(2) | 1 | mainhand | `#abyssalcraft:enchantable/staff_of_rending`(空) | 20 / 40 |
| sapping | UNCOMMON(5) | 3 | mainhand | `#abyssalcraft:enchantable/staff_of_rending`(空) | 12+8·(l-1) / +20 |

- 对外 API：`ACEnchantments.{BLINDING_LIGHT,IRON_WALL,LIGHT_PIERCE,MULTI_REND,SAPPING}`（`ResourceKey<Enchantment>`），供 PS-11 效果 hook 用 `EnchantmentHelper` 查等级。

## 5. 跨版本 / 加载器要点

- 触及的兼容层：新增 `platform/EnchantmentCompat`（本层 fork 边界）。
- **硬 fork（网络之后第二深）**：**1.20.1 附魔是代码**（`Enchantment` 子类，注册进常规 `BuiltInRegistries.ENCHANTMENT`）；**1.21 附魔全数据驱动**（datapack registry，从 `data/<ns>/enchantment/*.json` 加载，无 `Enchantment` 类可继承、无代码注册）。故附魔**定义**无法共享——1.20.1 代码类（放 `EnchantmentCompat` fork 内）/ 1.21 JSON。**唯一共享** = `ResourceKey<Enchantment>`（vanilla，两端稳定，放 fork-free `ACEnchantments`）。
- javap / schema 核实：1.20.1 `Enchantment(Rarity,EnchantmentCategory,EquipmentSlot[])` + `getMinCost`/`getMaxCost`/`getMaxLevel` + `EnchantmentCategory.create(String,Predicate<Item>)`（javap forge jar）；1.21 JSON schema 提取 vanilla `fortune`(definition-only 证)/`sharpness`(damage effect) 核字段（`description`/`supported_items`/`weight`/`max_level`/`min_cost{base,per_level_above_first}`/`max_cost`/`anvil_cost`/`slots`/`exclusive_set`）。
- `//?` 分叉点：**业务零 `//?`**。附魔注册 fork 全在 `EnchantmentCompat`；`ACEnchantments` 只用 vanilla `ResourceKey`/`Registries`。

## 6. 实现记忆 / 踩坑 (verified gotchas)

- **同 id 双路径**：1.20.1 代码注册 `abyssalcraft:iron_wall` / 1.21 JSON `enchantment/iron_wall.json` → 各 loader 走各自路径、id 一致；1.20.1 忽略 `data/<ns>/enchantment/` 文件夹（无该 datapack registry），1.21 忽略代码注册路径（`EnchantmentCompat` neo 分支 no-op）。
- **1.21 JSON schema 强校验**：datapack 附魔 JSON 由游戏 codec 在世界加载时校验；schema 错 → 附魔注册表加载失败（同 `dimension_type/mini.json` 崩先例）→ `runServer` 抵 `Done` 即证 5 JSON schema 全对。
- **Staff of Rending 未移植**：multi_rend/sapping 的 supported 分类 = 1.20.1 `EnchantmentCategory.create(item→false)`（匹配无）/ 1.21 空 tag → 现附于任何物品皆不可（忠实：仅 Staff，Staff 未移植）；Staff 落地时填 tag 即生效。
- **`/enchant` 只查 mainhand**：headless 探针须把目标物品放 `HandItems[0]`（主手），`supported_items` 校验的是物品**类型**而非槽位（故钻胸甲放主手也能被 iron_wall 附上）。

## 7. 验证 / DoD

- 两节点 `compileJava`：BUILD SUCCESSFUL。
- **forge `runServer`**：`Done`（5 代码附魔注册进 `ENCHANTMENT` 冻结无崩）+ 干净 stop。
- **neo `runServer`**：`Done`（5 datapack 附魔 JSON 解析入附魔注册表零 schema 错 + Staff 空 tag 加载）+ 干净 stop。
- **两节点 `/enchant` 探针**（summon 僵尸持钻剑/钻胸甲）：`Applied enchantment Light Pierce III`(forge)/`I`(neo) + `Applied enchantment Iron Wall to Zombie's item` 双端——**注册 + applicable（supported tag/category）+ i18n（显示名）+ maxLevel 四通**。
- 两节点 `runClient`：抵标题屏（附魔注册/datapack 不破客户端加载）。
- 未机核项（如实标注）：**附魔效果**（iron_wall 受击零动量、blinding_light 致盲、light_pierce 对暗影生物加伤）延后 PS-11 event hook，本层未实现/未验证；`multi_rend`/`sapping` 效果待未移植 Staff of Rending；附魔台/铁砧的实际获得流程 = 人工目视。

## 修订日志

- 2026-07-22：PS-3 建层——`platform/EnchantmentCompat`（1.20 类 / 1.21 datapack fork）+ `system/enchant/ACEnchantments` + 5 JSON + Staff 空 tag + 5 lang；两节点编译 + runServer `/enchant` 探针（light_pierce/iron_wall 双端 Applied）+ runClient 标题屏。效果延 PS-11。见平行表 CR-45。
