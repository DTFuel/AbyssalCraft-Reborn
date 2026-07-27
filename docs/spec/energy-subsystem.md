# 势能 / 能量 (Potential Energy) 子系统规格 (Subsystem Spec)

- 里程碑 / Stage：M7 / Stage S-B
- 关联平行任务：PS-5（框架）+ RR-ENERGY（完整服务端内容）；下游 R4/R5 读取本层
- 状态：RR-ENERGY 当前依赖闭包完成；21网络块、7功能神像、32 charm、Idol、PoP/扰动链已双端验证
- 负责：GitHub Copilot
- 最后更新：2026-07-25

## 1. 概述 / 目标

AbyssalCraft 的 Potential Energy（PE，势能）系统。功能神像产生PE，collector接收，relay定向搬运，container存储并与物品双向交换，pedestal为Necronomicon/Staff充能；depositioner作为有限能量manipulator向collector分发。所有PE值经旧键`PotEnergy`持久化，能量方块物品在破坏/放置间保留PE。

## 2. 范围

- 含：
  - collector/container/pedestal/relay各5档 + depositioner，共21个网络块；同族共享实现与BE type。
  - 7个功能神像；`deity_statue`保留为Cthulhu旧世界兼容入口。
  - 32个ritual charm（8家族×基础/RANGE/DURATION/POWER）与Idol of Fading。
  - 玩家/掉落物/collector路由、relay障碍与红石暂停、物品双向传能、PE方块掉落/放置持久化。
- 明确延后：Energy Container/Depositioner GUI、PEStream/DisruptionMessage客户端FX归R5；Depositioner Stone Tablet处理归T2.9c，不使用Spirit Tablet替代。

## 3. 设计 / 架构

- 关键类：
  - `DeityType`（7 神 Cthulhu/Hastur/J'zahar/Azathoth/Nyarlathotep/Shub-Niggurath/Yog-Sothoth）、`AmplifierType`（RANGE/DURATION/POWER）。
  - `IEnergyContainer`：能量 BE 接口（`getContainedEnergy`/`getMaxEnergy`/`setEnergy` + default `addEnergy`/`consumeEnergy`/`canAcceptPE`/`canTransferPE`）。**同 PC-4 `ItemTransferHost` 模式** —— BE 直接实现、`instanceof` 查询，免 loader 能力机制。
  - `IEnergyCollector extends IEnergyContainer`（采集器 marker）；`IEnergyTransporter extends IEnergyContainer`（+ `getTransferRange`）；`IEnergyManipulator`（`getEnergyQuanta`/`canTransferPE`/`getEnergyCollectors`/`isActive`/`addTolerance` + default `getDeity`/`getAmplifier`）。
  - `PEUtils`：`addEnergy`（溢出返回）/`consumeEnergy`（消耗返回）/`transfer(from,to,amount)`（满溢回退源）/`transferToCollectors(Level,manipulator)`（manipulator 逐 collector BE 喂能）。

## 4. 子系统内契约

- 对外 API：`IEnergyContainer` 等接口供能量方块 BE 实现；`PEUtils` 供网络逻辑复用；`DeityType`/`AmplifierType` 供 statue/charm/PS-9 扰动引用。
- PE stream 视觉 → PS-1 `net.client.PEStreamMessage`（posFrom→posTo）。

### 数值表

| Tier | Collector | Container | Pedestal | Relay容量/范围/抽取/发出 |
|---|---:|---:|---:|---|
| Basic | 1000 | 10000 | 5000 | 500 / 4 / 10 / 20 |
| Overworld | 1500 | 20000 | 7500 | 600 / 6 / 20 / 30 |
| Abyssal Wasteland | 2000 | 60000 | 10000 | 700 / 8 / 30 / 40 |
| Dreadlands | 2500 | 240000 | 12500 | 800 / 10 / 40 / 50 |
| Omothol | 3000 | 1200000 | 15000 | 900 / 12 / 50 / 60 |

Depositioner容量10000，未激活量子15，POWER激活量子`20*max(amplifier,1)`，容差阈值200。神像基础量子5、容差阈值100，PoP成员不累加容差。

## 5. 跨版本 / 加载器要点

- 业务代码仍以vanilla `Level`/`BlockEntity`/`BlockPos`和`instanceof IEnergyContainer`查询，未引入loader能力。
- NBT/组件差异经`BlockEntityCompat`、`ContainerCompat`、`ItemDataCompat`吸收；点燃、mob finalizeSpawn、驯服签名分别经`IgniteCompat`、`MobSpawnCompat`、`TamableCompat`吸收。

## 6. 实现记忆 / 踩坑 (verified gotchas)

- **框架先于内容**（同 PP-1/PC-1/PD-1/PG-0）：1.12.2 能量系统深耦合 deity 雕像 / 能量方块 / idol/charm 物品 / Necronomicon 书（全未移植）→ 先交付接口 + 网络算术（可 selfTest），内容随块/物品落地复用。
- **`transfer` 满溢回退**：`transfer(from,to,amount)` 先从 source 抽，若 target 满不下则把余量加回 source（避免凭空销毁 PE）——selfTest 覆盖（a=20→b 90 满，transfer 20 → 实移 10、a 回到 10）。
- **selfTest 触发**：临时主类 `init` 挂 `PEUtils.selfTest()`，`runData` 即触发 mod init（同 PC-4 `ItemTransfer` selfTest 先例）；neo 一次瞬态并发 mod-load NPE（`buildModContainerFromTOML`，早于 init，非本码）→ 重跑即 PASS。

## 7. 验证 / DoD

- 永久标记：`RR_ENERGY_SELF_TEST_OK blocks=21 statues=7 charms=32 idol=1 pop=3 disruptions=27 blocked=0`。本轮环境因 Gradle 缓存空 Zip 在配置期失败，未重写既有历史验证结论。
- 双端真实ServerLevel：collector→relay→container实移10 PE；实心障碍阻断；红石暂停；pedestal→Necronomicon实移20 PE；Basic/Totem/Archway成型、加成与断裂解绑通过。
- 双端停服重启逐字段恢复：collector 321、container 654、pedestal 777+满能书、relay 111、depositioner 888+tolerance/charm、statue tolerance/charm/PoP、idol 99、PoP `basic` identifier。
- 资源审计：32相关方块、20网络块、8含兼容入口神像、32 charm模型、loot与pickaxe双目录零缺失；8语言文件各620键且集合一致。

## 修订日志

- 2026-07-25：RR-ENERGY完整服务端闭环落地；21网络块、7神像、32 charm、Idol、PE掉落/放置持久化及双端真实Level/重启矩阵通过。GUI/网络FX与Stone Tablet保持后续边界。

- 2026-07-22：PS-5 建框架——`system/energy/**`（能量 BE 接口 + `PEUtils` 网络算术/传输）；两节点编译 + selfTest 双端 PASS。能量方块/物品/采集链延后（依赖未移植块/物品/Necronomicon）。见平行表 PS-5。
