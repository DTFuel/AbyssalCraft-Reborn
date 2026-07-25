# 物品转移子系统规格 (Subsystem Spec)

- 里程碑 / Stage：M2 / R2
- 关联平行任务：RR-MENU-HOST-TRANSFER；T2.8c / T2.9 / T2.9b
- 状态：Spirit Tablet、全 BlockEntity 持久附件与 Spirit Altar 调度已完成并双端验证
- 负责：GitHub Copilot（RR-MENU-HOST）；PC-4（历史引擎基线）
- 最后更新：2026-07-25

## 1. 概述 / 目标

移植 1.12.2 的物品运输系统：玩家用 Spirit Tablet 记录同维路径、过滤器和进出面，把配置写入源方块实体；Spirit Altar 扫描附近宿主并周期性驱动配置。机器本身不自发搬运，Altar 才是执行器。

## 2. 范围

- 含：中性转移引擎、完整多点 route/filter/facing 数据、所有 BlockEntity 的持久 transfer holder、Spirit Tablet 物品/5 槽菜单/Screen、Spirit Altar 方块/BE/调度。
- 不含：State Transformer + Stone Tablet（T2.9c）、Rending Pedestal（T2.9d）、Spellbook（T2.8d）、spirit path HUD/keybind（T6.6b）。

## 3. 设计 / 架构

- `system/transfer/ItemTransfer`：用中性 `CapabilityAccess.ItemView` 执行 simulate→extract→insert；插入不足时把余量安全放回源槽。每配置每轮默认最多搬 1 件。
- `ItemTransferConfiguration`：保存完整 route、exit/entry side、5 槽白名单、`ignoreSubtypes` 与 `matchComponents`；首尾为实际源/目标，中间节点保留路径语义。
- `ItemTransferHost`：持有配置列表与 running 状态，变更通过注入的 `dirty` 回调令 BE 存档。
- `platform/ItemTransferAttachmentCompat`：Forge 给每个 BlockEntity 附 `ICapabilitySerializable<ItemTransferHost>`；NeoForge 注册可序列化 `AttachmentType<ItemTransferHost>`，按需创建 holder。
- `SpiritTabletStorage` / `SpiritTabletInventory`：在 ItemStack 数据中保存 mode、同维 route、入口面、5 槽过滤和两个匹配开关；拒绝 Spirit Tablet、Crystal Bag 与 Shulker Box 等嵌套库存。
- `SpiritTabletItem`：普通右键开真实菜单；潜行右键轮换三种模式。对库存/路点记录、源 BE 应用配置、清空配置、Altar 控制均只在服务端执行。
- `SpiritAltarBlockEntity`：半径 16 扫描；首次/每 400t 重扫；启用时每 20t 调用附近 running host 的 `ItemTransfer.run`。Altar 只持久化 enabled，宿主集合可重建。

## 4. 子系统内契约

- 注册 ID：`abyssalcraft:{spirit_tablet,spirit_tablet_menu,spirit_altar}`；附件 key 为 `abyssalcraft:item_transfer`。
- 配置 NBT：`Route`、`ExitSide`、`EntrySide`、`FilterSubtypes`、`FilterNBT`、`Items`；兼容读取旧 `Origin`/`Destination`、`MatchSubtypes`、`MatchComponents`。
- holder NBT：`Configurations` 列表 + `Running`。Tablet route 同时记录 dimension id，跨维或未加载目标拒绝应用。
- 过滤：空过滤器允许全部；item 必须相同；未忽略 subtype 时比较 damage；启用 components 时按当前 subtype 策略比较组件。
- Tablet Menu：5 过滤槽 + 36 玩家槽；主手打开时锁定所选热栏宿主槽，副手打开不锁热栏；按钮 0/1 分别切换 subtype/components。

## 5. 跨版本 / 加载器要点

- `ItemTransferAttachmentCompat` 是核心 loader 分叉：Forge capability + attach event；NeoForge attachment registry + serializer。业务代码只读 `get/getOrCreate`。
- `CapabilityAccess` 隐藏 Forge/Neo item-handler 类型并暴露 `ItemView`；`ContainerCompat` 隐藏 ItemStack NBT、tag/components 与忽略 damage 比较差异。
- `BlockEntityCompat` 提供 update tag/packet，使 Altar enabled 等生产 BE 状态正确同步。
- 除 `platform/**` 外业务代码零 `//?`。

## 6. 实现记忆 / 踩坑

- 旧 `FilterSubtypes` 的语义是“忽略 subtype”，不是“匹配 subtype”；字段现代命名必须用 `ignoreSubtypes`，但写盘仍保留旧键。
- 忽略 subtype 且匹配 components 时，damage component 也必须排除，否则开关表面开启但 damaged stack 仍不匹配；使用 `ContainerCompat.canStackIgnoringDamage`。
- 全 BE holder 不能仅靠 AC 自有 BE 基类；原版箱子必须通过 Forge attach event / Neo attachment 获得同一持久数据。
- 无玩家 dedicated server 的机器区块不会稳定 entity-tick；真实验证夹具必须 force-load 测试区块，验证后取消。
- Tablet 的客户端构造只有同步占位 ItemStack；按钮的权威修改必须走服务器 `clickMenuButton`，再由 DataSlot 回传。

## 7. 验证 / DoD

- 双端 `runData`：`MenuHostSelfTest` 输出 `RR_MENU_HOST_SELF_TEST_OK`，覆盖配置复制/NBT、damage/components 过滤、Tablet storage/menu 与引擎边界。
- 双端真实专服：原版箱子 seed 时 `source/destination=3/1`，停服重启后运行配置继续转为 `2/2`，证明 Forge capability 与 Neo attachment 的配置/running 持久化。
- Forge/NeoForge 真实联网客户端：生产 Tablet Screen 均为 41 槽；按钮切换后 `subtype=true/components=true`，过滤槽 shift-click 往返；主手锁 1 个宿主槽，切副手重开锁 0，两个开关保持，均输出最终矩阵成功标记。
- 六张每端实际 Screen 截图已目视：界面非空、按钮/文字/槽位不重叠；深色背景标题与物品栏标签已改浅色。
- 双端 production build/JAR 门禁含 Tablet/Altar/Screen 类与资源，临时验证类、命令和 JVM 属性入口为 0。

## 修订日志

- 2026-07-25：RR-MENU-HOST-TRANSFER 完成；T2.8c/T2.9b 双端真实重启与客户端矩阵收口，建立本规格。见 CR-67。