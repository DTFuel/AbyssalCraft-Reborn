# 网络 (Network) 子系统规格 (Subsystem Spec)

- 里程碑 / Stage：M7 / Stage S-A
- 关联平行任务：PS-1（本层）；下游 handler 消费者 PS-5/6/7/8/9
- 状态：框架 + 全23消息序列化已交付；RR-KNOWLEDGE所属的5条necrodata/knowledge handler与协议v2已落地，其余handler仍随所属系统任务完成
- 负责：PS-1
- 最后更新：2026-07-25

## 1. 概述 / 目标

AbyssalCraft 的客户端↔服务端通信层。1.12.2 用一个 `SimpleNetworkWrapper` 通道 + `PacketDispatcher` 注册 23 条消息；移植后收敛为**一个多路复用通道** `net/ACNetwork`，其底层加载器分叉（Forge `SimpleChannel` / NeoForge 1.20.5+ Payload）全部封在 `platform/NetworkChannel`（PA-1）里。玩家可见效果通过各消息驱动（仪式反馈、扰动提示、势能粒子流、菜单同步、法术施放等），但这些效果的落地属于各所属系统任务；本层只保证**消息能被忠实序列化、注册、收发**。

## 2. 范围

- 含：`net/ACNetwork`（通道单例 + 23 消息注册 + `bootstrap`/发送便捷方法）、`net/server/**`（11 条 C→S 消息）、`net/client/**`（12 条 S→C 消息）、每条消息的忠实 `FriendlyByteBuf` 序列化、主类 `init` 里一行 `ACNetwork.bootstrap(modBus)`。
- 不含：
  - `platform/NetworkChannel` 本身（PA-1 冻结面；本层是其**首个运行期消费者**）。
  - 其余未完成消息 `handle` 的业务效果随所属系统落地；知识/死灵的 `PrepareSync/KnowledgeUnlock/NecroDataCap/ShouldSync/SyncNecromancyData` 已实现。

## 3. 设计 / 架构

- 包结构：`net/ACNetwork` · `net/server/*`（C→S）· `net/client/*`（S→C）。
- 关键类与职责：
  - `net/ACNetwork`：`public static final NetworkChannel CHANNEL = NetworkChannel.create("main")`；`static{}` 块按数字 id 注册全 23 消息；`bootstrap(Object modBus)` 触发静态注册再挂 mod-bus；`sendToServer/sendToPlayer/sendToAll` 便捷委托。
  - 每条消息 `implements NetworkChannel.ACPacket`，四要素：①全字段规范构造器（发送侧用）；②`(FriendlyByteBuf)` 解码构造器；③`write(FriendlyByteBuf)`；④`handle(Context)`（按所属任务逐步实现）。
- 数据流：发送侧 `ACNetwork.sendToX(msg)` → `NetworkChannel` 用 `idOf(msg)` + `encodeBody`（`msg.write`）打包进多路复用 `Envelope(id, body)` → 网络 → 接收侧按 id 取解码器 `decoder.apply(buf)` 重建消息 → `ctx.enqueue(() -> msg.handle(ctx))` 在主线程执行。

### 消息表（id 稳定，wire 用数字 id 而非类名）

| id | 类 | 方向 | 字段（序列化） | handler 目标 |
|---|---|---|---|---|
| 0 | FireMessage | C→S | `BlockPos` | mimic_fire 方块（未移植） |
| 1 | UpdateModeMessage | C→S | `int mode, container`（2 varint） | 状态变换器/灵魂石板菜单（未移植） |
| 2 | ToggleStateMessage | C→S | `BlockPos` | **PC-4 `ItemTransferHost`（已移植，接线延后）** |
| 3 | StaffOfRendingMessage | C→S | `int id` + `InteractionHand`（varint + 0/1） | 撕裂 API / staff（未移植） |
| 4 | StaffModeMessage | C→S | （无） | 守门人法杖（未移植） |
| 5 | SpiritTabletMessage | C→S | `int mode1,mode2` + `boolean openFilter,clearPath` | 灵魂石板菜单（未移植） |
| 6 | PrepareSyncMessage | C→S | `UUID` | 死灵能力同步（PS-8） |
| 7 | OpenSpellbookMessage | C→S | （无） | 法术书 GUI（PS-7） |
| 8 | MobSpellMessage | C→S | `int id` + `String spellID` + `int scrollType` | 法术系统（PS-7） |
| 9 | InterdimensionalCageMessage | C→S | `int id` + `InteractionHand` | 能量物品（未移植） |
| 10 | TransferStackMessage | C→S | `int slot` + `ItemStack`（**id+count**） | 物质化器背包菜单（未移植） |
| 11 | WindowPropertyMessage | S→C | `int windowId,property,value`（3 varint） | vanilla 菜单 `ContainerData`（现代自动同步；手动需客户端玩家） |
| 12 | RitualMessage | S→C | `String id,disruption` + `BlockPos` + `boolean failed` | 仪式（PS-6） |
| 13 | RitualStartMessage | S→C | `BlockPos` + `String id` + `int sacrifice,timerMax` | 仪式祭坛（PS-6） |
| 14 | CleansingRitualMessage | S→C | `int x,z,biomeID` + `boolean batched` | 群系净化（未移植） |
| 15 | DisruptionMessage | S→C | `String deity,name` + `BlockPos` | 扰动（PS-9） |
| 16 | EvilSheepMessage | S→C | `UUID` + `String playerName` + `int id` | 邪恶绵羊（未移植） |
| 17 | KnowledgeUnlockMessage | S→C | `int type` + `String data`（协议v2统一 namespaced/string payload） | 知识/死灵（已实现） |
| 18 | NecroDataCapMessage | S→C | `CompoundTag`（NBT） | 死灵能力（PS-8） |
| 19 | PEStreamMessage | S→C | `BlockPos posFrom,posTo` | 势能粒子流（PS-5） |
| 20 | ShouldSyncMessage | S→C | `long` | 死灵同步（PS-8） |
| 21 | SyncNecromancyDataMessage | S→C | `CompoundTag`（NBT） | 死灵（PS-8） |
| 22 | DisplayRoutesMessage | S→C | `CompoundTag`（NBT） | 势能路径渲染（PS-5） |

## 4. 子系统内契约

- 通道名：`ACRef.id("main")` = `abyssalcraft:main`（多路复用信封另有 `abyssalcraft:net_envelope`，全在 compat 内）。
- 消息 id：0–10 为 C→S，11–22 为 S→C，**稳定不可重排**（wire 传数字 id）。通道协议为v2，Forge两端版本谓词精确匹配；新消息追加更大id。
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
- **增量编译陈旧**：新增/移动源文件后 `:1.21.1-neoforge:compileJava` 可能 `UP-TO-DATE` 不真编译 → 用 `--rerun-tasks` 强制（本层实测 neo 首次 UP-TO-DATE，rerun 后才真编）。
- **round-trip 自测判据**：write→decode 构造器→再 write，比较两次字节数组相等（对称即字节稳定），无需 `equals()`。24 项（23 消息 + KnowledgeUnlock 两分支）。

## 7. 验证 / DoD

- 两节点 `compileJava --rerun-tasks`：BUILD SUCCESSFUL；neo `.class` 字节码核 ItemStack fork-free（24 个 net `.class`）。
- 运行期（临时在主类 `init` 挂 `ACNetwork.selfTest()`，**验证后已还原**）：
  - **forge `runServer`**：`network self-test: 24/24 messages round-tripped byte-stable` + `Done (6.816s)` + 干净 stop（bootstrap = Forge `SimpleChannel` 注册无崩）。
  - **neo `runServer`**：`network self-test: 24/24` + bootstrap（Neo Payload `playBidirectional` 注册）无崩——**但未抵 Done**：崩在其后**无关**的 `data/abyssalcraft/dimension_type/mini.json` IntProvider 解析（1.21 要求 `min_inclusive/max_inclusive` 作直接子键，现文件多套一层 `"value"`；forge 1.20.1 同文件正常加载 `abyssalcraft:mini`）→ 非 `net/**` 范畴，属维度/世界数据任务，已登记 02 §7 协调项。
  - **forge `runClient`**：`24/24` + 抵标题屏（Realms 可用性检查线程 = 标题屏判据）。
  - **neo `runClient`**：`24/24` + 抵标题屏（LWJGL backend + OpenAL + ResourceManager reload；缺物品/方块模型告警为未移植资产、非网络、非崩）。
- 未机核项（如实标注）：**实网逐消息 C↔S 同步**（真正跨进程发送→接收→handler 效果）需活的客户端-服务器会话，本机 headless 无法机核；本层运行期实证 = 通道 bootstrap 双端 + 全 23 消息字节稳定 round-trip 双端。各消息 handler 效果随所属系统落地时验证。

## 修订日志

- 2026-07-25：RR-KNOWLEDGE（CR-70）把KnowledgeUnlock统一为String payload并将通道升v2；5条知识/necrodata handler接线完成。
- 2026-07-22：PS-1 建层——`net/ACNetwork` + 23 消息 + 主类 bootstrap；两节点编译 + 4 次启动（forge server/client、neo server/client）self-test 24/24；ItemStack fork（id+count）与 `Context.player()`=发送方两处发现登记。见平行表 CR-38。
