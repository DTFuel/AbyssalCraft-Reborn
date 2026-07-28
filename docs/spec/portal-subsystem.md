# Portal 子系统规格

- 里程碑 / Stage：M5 + M7 / R4
- 关联任务：T5.7b、T7.6b、RR-RITUAL-SPELL-PORTAL
- 状态：实现与双端自动 Gate 完成；真人玩家往返/目标锚点/破坏/重启矩阵待 R4-LIVE-GATE
- 最后更新：2026-07-26

## 1. 范围

现代 Portal 链由四部分组成：

- `DimensionDataRegistry`：七个可参与传送的维度、稳定顺序、颜色、最低 Gateway Key 等级、双向连通边和可选 portal mob/overlay。
- `GatewayKeyItem`：三级 Gateway Key 与 Silver Key；目标以 namespaced dimension 字符串存入 `ItemDataCompat`。潜行右击仅在四个 AbyssalCraft 维度间按等级循环，普通右击方块由服务端校验连通性后放置传送门。
- `PortalAnchorBlock` / `PortalAnchorBlockEntity`：普通与 unchained Anchor，共用持久 BE；保存 destination、颜色、关联 portal UUID，并维护 `PortalAnchorIndex`。
- `DimensionPortal` / `DimensionTeleport`：同步 destination/unchained，拒绝 Boss、骑乘、死亡与冷却实体，通过 `TeleportCompat` 跨版本传送。

Portal Ritual 属仪式 manifest 的 specialized handler：验证 Gateway Key 当前目标与连通等级，转换祭坛结构，返还 Key，写入 Anchor destination 并生成关联 Portal。

## 2. 连通合同

默认图包含七维、六条双向边：

1. Overworld ↔ Abyssal Wasteland，tier 0
2. Abyssal Wasteland ↔ Dreadlands，tier 1
3. Dreadlands ↔ Omothol，tier 2
4. Omothol ↔ Dark Realm，tier 2
5. Overworld ↔ Nether，tier 0
6. Overworld ↔ End，tier 0

Nether/End 是否进入 Key 循环由 `vanilla_handling` 控制。普通 Anchor 必须满足图连接；unchained Anchor 可绕过边限制，但目标仍必须已注册、已允许且不同于当前维度。

## 3. 生命周期与权限

- Silver Key（tier 3）在 Anchor 上切换 active；客户端不提交颜色、连接结果或 portal UUID。
- 激活成功后 BE 写 destination/color/portal UUID，设置 `ACTIVE=true` 并登记到 `PortalAnchorIndex`。
- 每 20 tick 校验关联实体；UUID 丢失、实体死亡或 anchor 不匹配时重新生成。
- 失活/破坏时同时按 UUID 与局部 anchor position 清理 Portal，并移除索引。
- Portal 为玩家使用 `portalCooldown`，其他实体使用原版 cooldown；传送前后都设置冷却。
- Boss/Elite、骑乘者/载具、其他 Portal、死亡/移除实体不会传送。
- 单次 Portal 仅在乘客确实到达目标维度后销毁。

## 4. 跨版本边界

- `TeleportCompat`：Forge 1.20.1 `ITeleporter/PortalInfo` 与 NeoForge 1.21.1 `DimensionTransition`。
- `BlockEntityCompat`：Anchor save/load 与 update packet 的 HolderLookup 差异。
- destination 与 anchor position 使用 `ResourceKey<Level>` / `BlockPos`，业务代码不保存旧 int dimension ID。
- Portal renderer 读取同步后的 `DimensionData` 颜色/overlay；专用服务器不加载 client renderer。

## 5. 验证

自动 Gate：

- Forge/Neo `compileJava --rerun-tasks` 与 `runData` 全绿。
- `RR_PORTAL_SELF_TEST_OK dimensions=7 edges=6 keyTiers=4`。
- 永久自测覆盖七维、六边、三级负门控、无效 ID、Omothol overlay 与关键颜色。
- 双端 production JAR 含 Anchor、Portal、renderer 与资源。

仍需真人 Gate：

- Forge/Neo 各完成普通三 tier 和 Silver/unchained 的玩家正反向传送。
- 验证目标 Anchor 搜索、无目标时安全落点、背包/朝向、冷却、Boss/骑乘负测。
- 破坏 Anchor 清理 Portal；停服重启后 destination、ACTIVE、UUID 重建一致。
- 目视 Portal 颜色、overlay、Key tooltip 与激活/失活反馈。