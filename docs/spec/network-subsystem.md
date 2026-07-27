# 网络 (Network) 子系统规格 (Subsystem Spec)

- 里程碑 / Stage：M7 / Stage S-A
- 关联平行任务：PS-1（本层）；下游 handler 消费者 PS-5/6/7/8/9
- 状态：**冻结23条legacy消息 + 1条modern extension；19条MIGRATED、5条REPLACED、0条BLOCKED；双向接收门禁与永久datagen审计已就位**
- 负责：PS-1
- 最后更新：2026-07-27

## 1. 概述 / 目标

AbyssalCraft 的客户端↔服务端通信层。1.12.2 用一个 `SimpleNetworkWrapper` 通道 + `PacketDispatcher` 注册 23 条消息；移植后冻结这 23 条 legacy 目录，并追加 `NecronomiconPageActionMessage` 作为第 1 条 modern extension。全部消息收敛为**一个多路复用通道** `net/ACNetwork`，其底层加载器分叉（Forge `SimpleChannel` / NeoForge 1.20.5+ Payload）全部封在 `platform/NetworkChannel`（PA-1）里。

## 2. 范围

- 含：`net/ACNetwork`（通道单例 + 24 消息注册 + 方向元数据 + `bootstrap`/发送便捷方法）、`net/server/**`（12 条 C→S，含 1 条 modern extension）、`net/client/**`（12 条 S→C）、每条消息的忠实 `FriendlyByteBuf` 序列化与完整 handler、主类 `init` 里一行 `ACNetwork.bootstrap(modBus)`、`net/{NetworkMessageAudit,NetworkSelfTest}.java` 永久审计、`data/gen/NetworkValidationData.java` datagen 入口。
- 不含：
  - `platform/NetworkChannel` 本身（PA-1 冻结面；本层是其**首个运行期消费者**）。
  - 各消息 handler 实现随所属系统任务交付（Fire/Rending/Cage/Tablet/Ritual/Knowledge/Necrodata/PE/Disruption/EvilSheep 分属PS-5/6/7/8/9等）；本层只保证消息能被忠实序列化、注册、收发、在正确线程执行。

## 3. 设计 / 架构

- 包结构：`net/ACNetwork` · `net/server/*`（C→S）· `net/client/*`（S→C）。
- 关键类与职责：
  - `net/ACNetwork`：`public static final NetworkChannel CHANNEL = NetworkChannel.create("main")`；`static{}` 块按数字 id 注册全 23 消息；`bootstrap(Object modBus)` 触发静态注册再挂 mod-bus；`sendToServer/sendToPlayer/sendToAll` 便捷委托。
  - 每条消息 `implements NetworkChannel.ACPacket`，四要素：①全字段规范构造器（发送侧用）；②`(FriendlyByteBuf)` 解码构造器；③`write(FriendlyByteBuf)`；④`handle(Context)`（按所属任务逐步实现）。
- 数据流：发送侧 `ACNetwork.sendToX(msg)` → `NetworkChannel` 用 `idOf(msg)` + `encodeBody`（`msg.write`）打包进多路复用 `Envelope(id, body)` → 网络 → 接收侧先从 Forge reception side / Neo payload context flow 得到实际方向并与注册元数据比较 → 方向正确才取 decoder、重建消息并排队 handler。反向消息在 decode 和 handler 之前拒绝。

### 消息表（id 稳定，wire 用数字 id 而非类名）

| id | 类 | 方向 | 状态 | handler 目标 |
|---|---|---|---|---|
| 0 | FireMessage | C→S | MIGRATED | 服务端权威mimic_fire扑灭+声音 |
| 1 | UpdateModeMessage | C→S | REPLACED | 服务端菜单按钮（StateTransformer/SpiritTablet） |
| 2 | ToggleStateMessage | C→S | MIGRATED | 服务端权威ItemTransferHost开关+粒子 |
| 3 | StaffOfRendingMessage | C→S | MIGRATED | 服务端权威Staff of Rending撕裂+多目标 |
| 4 | StaffModeMessage | C→S | MIGRATED | 服务端权威Gatekeeper Staff模式切换 |
| 5 | SpiritTabletMessage | C→S | MIGRATED | 服务端权威Spirit Tablet菜单设置 |
| 6 | PrepareSyncMessage | C→S | MIGRATED | 服务端推送necrodata给请求者 |
| 7 | OpenSpellbookMessage | C→S | MIGRATED | 服务端解析持书手并开Spellbook菜单 |
| 8 | MobSpellMessage | C→S | MIGRATED | 服务端重验卷轴+目标+PE后施法 |
| 9 | InterdimensionalCageMessage | C→S | MIGRATED | 服务端权威捕获目标+PE扣费 |
| 10 | TransferStackMessage | C→S | REPLACED | 服务端虚拟结果槽与配方提交 |
| 11 | WindowPropertyMessage | S→C | REPLACED | 客户端ContainerData（现代自动同步） |
| 12 | RitualMessage | S→C | MIGRATED | 客户端仪式完成反馈（粒子+声音） |
| 13 | RitualStartMessage | S→C | MIGRATED | 客户端仪式开始仪式+连线 |
| 14 | CleansingRitualMessage | S→C | REPLACED | 客户端群系刷新（服务端发resend） |
| 15 | DisruptionMessage | S→C | REPLACED | 客户端扰动反馈（服务端执行效果） |
| 16 | EvilSheepMessage | S→C | MIGRATED | 客户端evil sheep主人链接 |
| 17 | KnowledgeUnlockMessage | S→C | MIGRATED | 客户端知识解锁 |
| 18 | NecroDataCapMessage | S→C | MIGRATED | 客户端necrodata覆写 |
| 19 | PEStreamMessage | S→C | MIGRATED | 客户端PE粒子流 |
| 20 | ShouldSyncMessage | S→C | MIGRATED | 客户端请求necrodata同步 |
| 21 | SyncNecromancyDataMessage | S→C | MIGRATED | 客户端necrodata完整同步 |
| 22 | DisplayRoutesMessage | S→C | MIGRATED | 客户端transfer路径粒子流 |
| 23 | NecronomiconPageActionMessage | C→S | MIGRATED | modern extension；服务端权威页面动作 |

## 4. 子系统内契约

- 通道名：`ACRef.id("main")` = `abyssalcraft:main`（多路复用信封另有 `abyssalcraft:net_envelope`，全在 compat 内）。
- 消息 id：legacy 目录固定为 0–22；0–10 为 C→S，11–22 为 S→C。id 23 是 Necronomicon page action modern extension（C→S）。全目录**稳定不可重排**（wire 传数字 id）。通道协议保持 v2：本次仅增加本地注册方向元数据和接收门禁，Envelope 仍是同一 `id + body` wire bytes；若未来改变 Envelope 编码才升级协议。
- 方向门禁：注册必须声明 `NetworkChannel.Direction`。Forge 从 `NetworkEvent.Context#getDirection().getReceptionSide()` 判定接收端；Neo 从 payload context `flow()` 判定 packet flow。方向不符时不得调用 decoder、不得 enqueue、不得执行 handler。
- `ACPacket` 契约：`write(FriendlyByteBuf)` + `handle(Context)`；解码经 `(FriendlyByteBuf)` 构造器（注册为 `Function<FriendlyByteBuf,M>`）。
- **`NetworkChannel.Context.player()` = 发送方玩家**（服务端侧）：C→S handler 拿到的是发送者，正确；**S→C（client-bound）handler 若需接收方（客户端）玩家，`Context.player()` 在 Forge 侧返回 `getSender()`（客户端接收时为 null）→ 须用 `SideExecutor` 客户端侧取 `Minecraft.getInstance().player`**（Neo 侧 `Context.player()` 返回接收方玩家，但为跨加载器一致，client-bound handler 一律走 SideExecutor）。
- 对外 API：其他系统发消息用 `ACNetwork.sendToServer/sendToPlayer/sendToAll`；认领 handler 时编辑对应消息类的 `handle` 体。

## 5. 跨版本 / 加载器要点

- 触及的兼容层：仅 `platform/NetworkChannel`（PA-1，本层不改，只消费）+ `platform/ACRef`（ResourceLocation）。
- javap / jar 核实结论（双 jar 逐一核）：
  - **`FriendlyByteBuf.writeItem(ItemStack)/readItem()` 仅 Forge 1.20.1 有；NeoForge 1.21 无**（1.21 的 ItemStack 网络化改用 `RegistryFriendlyByteBuf` + `ItemStack.STREAM_CODEC`，因组件系统需注册表访问）。→ `TransferStackMessage` 的 ItemStack **不走 `writeItem/readItem`**，改 fork-free 的 item 注册 id（`BuiltInRegistries.ITEM.getKey().toString()` → `writeUtf`）+ count（`writeVarInt`）；读回 `new ItemStack(BuiltInRegistries.ITEM.get(ACRef.parse(id)), count)`。**代价**：丢每-stack 组件/NBT（对该消息延后的「材料袋移动纯材料 stack」用途可接受；若将来需带组件转移，须由 compat 提供注册表感知缓冲）。
  - `writeNbt`：Forge `writeNbt(CompoundTag)` / Neo `writeNbt(Tag)`——`CompoundTag` 是 `Tag`，传 `CompoundTag` 两端都编译通过；`readNbt()` 两端均返回 `CompoundTag`（可空）。
  - `writeBlockPos/readBlockPos`、`writeUUID/readUUID`、`writeUtf/readUtf`、`writeVarInt/readVarInt`、`writeLong/readLong`、`writeBoolean/readBoolean`——两端同签名。
- `//?` 分叉点：**业务零 `//?`**。加载器网络分叉全在 `platform/NetworkChannel`；ItemStack fork 用版本稳定的 `BuiltInRegistries` + `ACRef.parse` 规避。

## 6. 实现记忆 / 踩坑 (verified gotchas)

- **`writeItem/readItem` 是加载器分叉的隐雷**（编译报错级证据：neo 编译若用 `buf.writeItem` 会 `cannot find symbol`）。凡消息带 ItemStack，用 id+count（或将来 compat 的注册表感知编解码），别直接调 `FriendlyByteBuf` 的 item 方法。neo 字节码核 `TransferStackMessage.write`：`getKey`→`writeUtf`→`writeVarInt`，无 `writeItem`。
- **`Context.player()` 语义**：发送方（服务端）。client-bound handler 需客户端玩家 → SideExecutor（见 §4）。这是 23 条消息 handler 现全延后的部分原因（另一原因是目标系统未移植）。
- **客户端字段不是权限**：`MobSpellMessage` 为兼容 wire 保留旧 spell ID/scroll type，但 handler 明确不读取它们来决定效果；权威数据来自发送者正在使用的 `ScrollItem`。`OpenSpellbookMessage` 同样无 hand/book tier 字段，服务端自行选择真实持书手。
- **增量编译陈旧**：新增/移动源文件后 `:1.21.1-neoforge:compileJava` 可能 `UP-TO-DATE` 不真编译 → 用 `--rerun-tasks` 强制（本层实测 neo 首次 UP-TO-DATE，rerun 后才真编）。
- **round-trip 自测判据**：write→decode 构造器→再 write，比较两次字节数组相等（对称即字节稳定），无需 `equals()`。28 项（24 消息，KnowledgeUnlock 覆盖多个分支）。
- **方向门禁自测判据**：24 个目录类型各做正确方向与反向方向两次 dispatch；正确方向必须完成 decode 并 enqueue，反向方向必须在 decode 前返回且不 enqueue。输出计数从 audit 实际方向逐项累计，不在测试中硬编码 12/12。

## 7. 验证 / DoD

- **两节点 `compileJava --rerun-tasks`**：BUILD SUCCESSFUL；neo `.class` 字节码核 ItemStack fork-free（24 个 net `.class`）。
- **永久 datagen 审计**（`data/gen/NetworkValidationData.java` → `NetworkSelfTest.run()`）：
  - `NetworkMessageAudit.validate(ACNetwork.CHANNEL)`：23 legacy + 1 modern extension 完整、id闭区间0–23、wire id与注册方向稳定、19/5/0审计结果。
  - 28轮round-trip测试：write→decode→write字节稳定。
  - 24×2方向矩阵输出 `RR_NET_DIRECTION_GATE_OK serverBound=12 clientBound=12 rejected=24`。
  - 双端 `runData` 输出 `RR_NET_SELF_TEST_OK messages=24 migrated=19 replaced=5 blocked=0 roundTrips=28`。
- **handler 实现审计**（`docs/spec/rr-net-message-audit.csv`）：
  - 18条MIGRATED：服务端权威、权限/方向/线程正确、客户端反馈经SideExecutor。
  - 5条REPLACED：UpdateMode/TransferStack菜单现代化、WindowProperty自动同步、CleansingRitual服务端resend、Disruption服务端执行。
  - 0条BLOCKED：无废弃消息。
- **实网验证遗留**：真正跨进程C↔S同步需活客户端-服务器会话（本机headless不可机核）；临时实网fixture（RRNetValidation/RRNetClientValidation）已删除，实网验证随各系统集成测试覆盖。

## 修订日志

- 2026-07-27：R5 NET 方向契约——注册表增加方向元数据；Forge reception side / Neo payload context 在 decode 前拒绝反向 Envelope；永久自测增加 24×2 正负矩阵。协议保持 v2，目录冻结为 23 legacy + 1 modern extension（19/5/0，12 C→S + 12 S→C）。
- 2026-07-27：RR-NET-AUTO / T7.1c 完成——全23条 legacy 消息handler已实现（18/5/0），删除临时实网验证fixture（RRNetValidation/RRNetClientValidation），建立永久datagen审计（NetworkValidationData→NetworkSelfTest→NetworkMessageAudit），清理ACClientSetup临时hook。
- 2026-07-26：R4 接通 OpenSpellbook、MobSpell、StaffMode、RitualStart、Ritual handler；MobSpell 包内 spell/quality 降为不受信目标提示，客户端仪式状态经 SideExecutor 分发。
- 2026-07-25：RR-KNOWLEDGE（CR-70）把KnowledgeUnlock统一为String payload并将通道升v2；5条知识/necrodata handler接线完成。
- 2026-07-22：PS-1 建层——`net/ACNetwork` + 23 消息 + 主类 bootstrap；两节点编译 + 4 次启动（forge server/client、neo server/client）self-test 24/24；ItemStack fork（id+count）与 `Context.player()`=发送方两处发现登记。见平行表 CR-38。
