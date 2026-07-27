# Worldgen Validation Tools

RR-WORLD-FIDELITY-AUTO 自动验证框架，用于验证世界生成的性能、保真度和完整性。

## 工具概览

### 1. WorldgenPerformanceSampler (T5.2c/T5.3c)
**目的**: 测量 Abyssal Wasteland 和 Dreadlands 区块生成性能  
**方法**: 固定路线采样，记录 p50/p95 延迟  
**状态**: 已接真实 `ServerStartedEvent` + `ServerLevel`；必须在双端专服运行后才可 PASS

### 2. DarkRealmNoiseOracle (T5.4c)
**目的**: 验证 Dark Realm 洞腔噪声与 1.12.2 逐位匹配  
**方法**: 从仓内旧 `ChunkGeneratorDarkRealm` 和 Minecraft 1.12.2 noise 算法离线重建 raw density baseline  
**状态**: 完成（28 个正/负样本，含 source hash、seed、坐标推导和 density output provenance）

### 3. StructureFixtureValidator (T5.6c)
**目的**: 审计结构模板完整性和 marker 实现  
**方法**: 模板/NBT/palette/loot 审计 + 6 类 marker 真实宿主契约  
**状态**: 完成（crate/spawner/pedestal/sealing lock/biomass/ooze = 6/6）

### 4. EntitySpawnStatistics (T5.8d)
**目的**: 验证自然刷怪生态数据结构  
**方法**: 读取 biome spawn settings，统计 AC entities  
**状态**: 已接真实 biome/entity registry 与 `SpawnCandidateCompat`；必须在双端专服运行后才可 PASS

### 5. WorldgenFinalMatrix (T5.9b)
**目的**: 聚合上述 4 项验证的最终门禁  
**方法**: datagen 离线门验证 oracle + fixtures；server 门使用真实 `ServerLevel` 验证性能和生态  
**状态**: 无 BLOCKED；两门任一失败均不允许 PASS

## 快速开始

### Datagen 验证（当前可用）
```powershell
./gradlew :1.20.1-forge:runData
# 离线门输出: "RR_WORLD_FINAL_MATRIX_PASS"
```

### Server 验证
```powershell
./gradlew :1.20.1-forge:runServer -Dabyssalcraft.rrServerMatrix=true
# 真实 ServerLevel 门输出: "RR_WORLD_SERVER_MATRIX_PASS"
```

### 1.12.2 Oracle 离线重建
```powershell
node scripts/extract_dark_realm_oracle.js
# 校验旧源码锚点与 SHA-256 后重建 baseline，无需启动旧游戏
```

## 验证标准

### 性能阈值
- **p50**: ≤ 100ms（中位数区块生成时间）
- **p95**: ≤ 500ms（尾延迟）
- **warnings**: = 0（无生成错误）

### 噪声保真
- **matches**: = 28/28（Java 生产实现与独立 JS 重建的 raw density 匹配）
- **mismatches**: = 0

### 结构完整性
- **templates**: = 37（legacy 模板数）
- **procedural**: = 2（程序化结构数）
- **markerHosts**: = 6/6

### 刷怪生态
- **biomesWithAC**: > 0（至少一个 biome 有 AC entities）
- **totalACSpawners**: > 0（总 AC spawner 数）

### 非自动验证范围
以下项目归 U-WORLD 用户任务，需真人验收：
- 地形视觉质量（高原/湖盆/山海形态）
- 结构旋转与拼缝正确性
- 天空/雾渲染效果
- 实际 mob 密度与分布合理性
- 玩家 Portal 往返体验

## 输出示例

### Datagen 阶段
```
[WorldgenInvariant] RR_WORLD_INVARIANT_OK cavityHash=e19b7f024699d36e samples=6
[WorldgenFinalMatrix] === RR-WORLD-FIDELITY-AUTO Final Matrix (T5.9b) ===
[T5.4c] Dark Realm Oracle: RR_WORLD_ORACLE_DARK_REALM_OK matches=28 mismatches=0
[T5.6c] Structure Fixtures: RR_WORLD_FIXTURE_OK templates=37 markerHosts=6/6
[SERVER] T5.2c/T5.3c/T5.8d: VERIFIED_BY_REAL_SERVER_MATRIX
Status: PASS | Offline Completed: 2/2 | Failed: 0 | Server Hook: REAL_SERVER_LEVEL
```

### Server 阶段（预期）
```
[T5.2c] RR_WORLD_PERF_AW_OK p50=45ms p95=230ms samples=19 warnings=0
[T5.3c] RR_WORLD_PERF_DL_OK p50=52ms p95=380ms samples=19 warnings=0
[T5.8d] RR_WORLD_SPAWN_STATS_OK biomesChecked=16 biomesWithAC=14 totalACSpawners=47
```

## 错误排查

### "templates=X expectedLegacy=37"
**原因**: 模板数量不匹配  
**解决**: 检查 `scripts/legacy-structures-manifest.json`

## 相关文档

- **完整报告**: `docs/validation/RR-WORLD-FIDELITY-AUTO-REPORT.md`
- **规格文档**: `docs/spec/worldgen-subsystem.md` §15
- **任务定义**: `docs/porting/01-porting-task-plan.md` T5.2c–T5.9b

## 贡献

此验证框架由 Agent WORLD 实现，遵循以下约束：
- ✅ 只写 `validation/world/**`
- ✅ 只读 `world/portal/**`, `content/**`, `platform/**`, `relay`, `lang`
- ✅ 不运行 Gradle/Stonecutter
- ✅ 可运行独立脚本（Node.js）
- ✅ 区分可完成/BLOCKED 项，禁止伪报

---

**Version**: 1.0.0  
**Last Updated**: 2026-07-27  
**Maintainer**: Agent WORLD
