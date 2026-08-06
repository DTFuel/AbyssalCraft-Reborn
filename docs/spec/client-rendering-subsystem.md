# 客户端渲染子系统规格 (Client Rendering Subsystem Spec)

> 覆盖 Stage H1 的 PH-2（天空/雾）、PH-3（粒子）、PH-4（音效）。实体/护甲/BER 的 RR-RENDER 详见 `entity-subsystem.md` §12.6；PH-1（机器屏幕）与 PH-5/PH-6 另见各自 spec。

## 1. 概述 / 目标

忠实移植 AbyssalCraft 1.12.2 的三块纯客户端表现层，并把加载器差异全部收进 `platform/`：

- **天空/雾**：4 个维度（Abyssal Wasteland / Dreadlands / Dark Realm / Omothol）各自的雾色（+ 天空盒）。1.12.2 由 `WorldProvider.getFogColor` / `getSkyRenderer`（`ACSkyRenderer`）实现。
- **粒子**：自定义粒子（`ACParticleFX` / `PEStreamParticleFX` / `ItemRitualParticle`）。
- **音效**：45 个 `SoundEvent` + `sounds.json`（106 个 .ogg）+ 字幕。

## 2. 范围

**已交付**：
- PH-4 音效 ☑：`registry/ModSounds`（45 `SoundEvent`）+ `assets/abyssalcraft/sounds.json` + 106 `.ogg` + `en_us.json` 41 条 subtitle。
- PH-2 雾色 ☑ / 天空盒 ☑（RR-CLIENT-FX/CR-73）：`client/sky/ACDimensionEffects` extends `platform/DimensionSkyCompat`（renderSky 双端签名 fork + 即时六面天空盒）+ `client/sky/ACDimensionSkies`（4 维注册 tinted 天空盒，色取自 `ClientVars`）+ `platform/DimensionEffectsCompat`（事件 fork）；3 张天空贴图（Dark Realm 复用 omothol_sky）。四维双端目视 = T6.3c（人工，待）。
- PH-3 框架 + 3 粒子 ☑：`registry/ModParticles` 注册 `abyssal_fx`、`blue_flame`、`pe_stream`；`client/particle/{ACFadeParticle,BlueFlameParticle,PEStreamParticle}` + `platform/ParticleCompat` 提供客户端实现。BlueFlame + ItemRitual（vanilla `item` payload）由仪式系统发射；PEStream 由 RR-NET 触发专用粒子。
- 世界空间线条 Shader ☑：`LineEffectRenderer` 将线段扩成带双端盖的八面世界空间棱柱，按侧面方向明暗显示体积；`rendertype_line` 核心 Shader 在起止顶点颜色之间做纯色插值，仅由效果寿命统一淡出；`LineRenderCompat` 负责 Shader 注册、透明世界阶段和双版本顶点 API；首个消费者为成功命中的 Staff of Rending，四个等级分别使用独立同色系渐变。

**RR-CLIENT-FX 收口（CR-73，2026-07-26）**：
- 天空盒（原 PH-2b）☑：`platform/DimensionSkyCompat` 吸收 Forge `IForgeDimensionSpecialEffects.renderSky(PoseStack…)` ↔ Neo `IDimensionSpecialEffectsExtension.renderSky(Matrix4f…)` 签名 fork，以及 1.20 `Tesselator.getBuilder()/vertex().uv().endVertex()/end()` ↔ 1.21 `begin()/addVertex().setUv()/buildOrThrow()` 顶点 fork；`getPositionTexShader`+`setShaderColor` 绘 ±100、16×16 UV 六面天空盒，`SkyType.NONE` 屏蔽 vanilla 天体。四维贴图/色：AW `abyssal_wasteland_sky` 0/105/45、Dreadlands `dreadlands_sky` 100/14/14、Omothol `omothol_sky` 40/30/40、Dark Realm 复用 `omothol_sky` 30/20/30，色实时取自 `ClientVars`（reload 生效；完整 clientvars 仍 T6.6d）。
- 粒子（原 PH-3b）☑：BlueFlame 与 ItemRitual 由仪式系统发射。PEStream 归 RR-NET 触发，但使用专用 `PEStreamParticle`：每格 15 个采样点，按客户端粒子设置以 1/2/3 步长降采样，寿命 20 tick、0.65 阻尼、随机延寿，三组旧紫/绿/蓝 RGB，并按 `generic_7..0` 换帧。玩家、掉落物、collector 成功接收以及 relay 成功输出 PE 都发送流。
- 声音（原 PH-4b 收尾）☑：45/45 事件均有生产触发；`AbstractShoggoth.playStepSound` 补最后一个未接的 `shoggoth.step`（0.15/1.0），并修正 `sounds.json` 中 `jzahar.shout` 指向缺失键→现有 `.shouts` 译文键。
- Gate：永久 `data/gen/ClientFxSelfTest`（+`ClientFxValidationData`）输出 `RR_CLIENT_FX_SELF_TEST_OK skies=3 particles=3 sounds=45 ogg=106 subtitles=41 rituals=62`，并锁定 8 帧顺序与 75/38/25 采样数。
- **仍待（人工）**：T6.3c 四维天空/雾双端目视、T6.5c 声音/字幕双端听觉矩阵。

**延后（诚实 · 见平行表）**：
- **PH-1b 当前事实**：Crystal Bag、Spirit Tablet 与 Spellbook 屏幕均已完成；剩余是 energy/depositioner/rending 等屏幕（归 RR-CLIENT-GUI）。1.12.2 无专属粒子贴图（原用 vanilla atlas 帧）。

## 3. 设计 / 架构

**音效（fork-free）**：`ModRegistrar<SoundEvent>` over `Registries.SOUND_EVENT`；每个事件 `SoundEvent.createVariableRangeEvent(ACRef.id(id))`，`id` 与 `sounds.json` 键一致（点号在 RL path 合法）。`EVENTS` map 存 `id -> Supplier<SoundEvent>` 供下游实体/方块接线。经 `ModRegistries.ALL` 挂载。

**天空/雾**：`ACDimensionEffects extends DimensionSpecialEffects`，构造 `super(Float.NaN, false, SkyType.NONE, false, false)`；`getBrightnessDependentFogColor` 返回常量雾色（`null` 时退回 overworld 亮度缩放，供 AW），`isFoggyAt` 返回是否厚雾。`ACDimensionSkies` 用中性 sink 注册 4 维。`DimensionEffectsCompat` 只在 `attach` 内 import 分叉事件。**关键**：每维 `dimension_type` 的 `effects` 字段须指向注册的 id（本子系统把 4 维 effects 改为 `abyssalcraft:<dim>`，`mini` 保持 `minecraft:overworld`）。

**粒子**：`ModParticles` 注册 `SimpleParticleType`（`new SimpleParticleType(false){}` 匿名子类绕 protected 构造）；`ACFadeParticle extends TextureSheetParticle`（把 1.12.2 `BufferBuilder` 即时绘制重写为 sprite 驱动：0.1x 初速、0.75 基准尺寸、`age/lifetime*32` 渐入、摩擦、`ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT`）+ 内嵌 `Provider(SpriteSet)`；`ParticleCompat` 在 `attach` 内 import 分叉 `RegisterParticleProvidersEvent`，中性 sink `registerSpriteSet`。需 `assets/.../particles/<name>.json` 描述符 + `textures/particle/<name>.png` sprite。

**线条 Shader**：服务端仅在 Staff of Rending 确认撕裂成功后发送 `LineEffectMessage(start,end,startArgb,endArgb,durationTicks)`；客户端将效果加入最多 128 条的短寿命队列，在 `AFTER_TRANSLUCENT_BLOCKS` 批量绘制。CPU 为每条直线生成八个侧面和两个端盖，固定方向明暗与背面剔除让截面保持可读的三维体积。起止 RGB 由顶点插值形成纯色渐变，Shader 不再按长度或边缘修改 alpha；整体 alpha 仅按寿命平方衰减。后续折线/曲线可把单段替换为采样点链，网络方向、双颜色和 RenderType 契约无需改变。

## 4. 子系统内契约

- 三块全为纯客户端：经主类 `SideExecutor.runWhenClient` 内 `DimensionEffectsCompat.attach` / `ParticleCompat.attach` 挂 MOD 总线；`ModSounds`/`ModParticles` 的注册器经 `ModRegistries.ALL`（两侧都注册，音效/粒子类型是通用注册项）。
- 忠实 1.12.2 雾色常量：Dreadlands `(0.2, 0.03, 0.03)`；Dark Realm / Omothol `0xA080A0 * 0.15`≈`(0.094, 0.075, 0.094)`；AW 无 `getFogColor` override → 默认亮度雾。厚雾（`doesXZShowFog`）：Dreadlands、Omothol 开。
- 天空 tint RGB（供未来天空盒）：AW `(0,105,45)`、Dreadlands `(100,14,14)`、Omothol `(40,30,40)`。

## 5. 跨版本 / 加载器要点

- **fork-free**：`SoundEvent.createVariableRangeEvent` / `Registries.SOUND_EVENT` / `DimensionSpecialEffects` / `Vec3` / `SkyType` / `TextureSheetParticle` / `ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT`（1.21.1 仍存）/ `SimpleParticleType` 匿名子类——1.20.1 与 1.21.1 同签名，均以两节点 `compileJava` 实证。
- **仅 import 分叉**（3 个 client 事件，Forge `net.minecraftforge.client.event.*` ↔ NeoForge `net.neoforged.neoforge.client.event.*`，方法签名同）：`RegisterDimensionSpecialEffectsEvent.register(ResourceLocation, DimensionSpecialEffects)`、`RegisterParticleProvidersEvent.registerSpriteSet(ParticleType, SpriteParticleRegistration)`。
- **线条渲染分叉**：`RegisterShadersEvent` / `RenderLevelStageEvent` 仅包名分叉；顶点提交为 1.20 `vertex/uv/color/endVertex` ↔ 1.21 `addVertex/setColor/setUv`，统一格式使用两端共有的 `POSITION_TEX_COLOR`。
- **深分叉（延后）**：即时模式天空盒绘制（`Tessellator`/`BufferBuilder` → 1.21 新管线），故天空盒渲染未移植。

## 6. 实现记忆 / 踩坑 (verified gotchas)

- `dimension_type` 的 `effects` 若不指向已注册的 effects id，vanilla 静默回退 overworld 效果（runClient 会打 fallback 警告）——须两侧对齐；本子系统 runClient 日志**无** fallback 警告 = 4 维 effects 均已绑定。
- `SimpleParticleType` 构造是 `protected`：跨包用 `new SimpleParticleType(false){}` 匿名子类。
- 注册粒子类型必须同时给 provider + 描述符 + sprite，否则 atlas 缺 sprite；三种 AC 粒子均具备完整注册链。
- Forge 1.20 的 `BufferBuilder` 要求属性严格按 `POSITION_TEX_COLOR` 的字段顺序提交；写成 `vertex/color/uv/endVertex` 虽能编译，但第一次真实绘制会抛 `Not filled all elements of the vertex`。必须保持 `vertex/uv/color/endVertex`，NeoForge 1.21 则使用具名 setter。
- 忠实保留 1.12.2 音效 quirk：`sounds.json` 引用 `chant.yog_sothoth_1/_2`、`hastur_1/_2`、`jzahar.shout`，而 lang 键为 `yog_sothoth`/`hastur`/`shouts`（不匹配即无字幕，与旧版一致）。

## 7. 验证 / DoD

- 两节点 `compileJava` EXIT=0（锁定上述所有 API + 3 事件 fork）。
- **forge `runClient`** 进世界后干净退出 BUILD SUCCESSFUL：Sound engine started 且**无** `sounds.json` 解析错；`particles.png-atlas` 建成且**无** `abyssal_fx` sprite 错；4 个 AC 维度加载且**无** DimensionSpecialEffects fallback 警告。
- **线条运行期 smoke**：Forge 进入实际世界并连续提交测试光束，日志无 `BufferBuilder`、Shader 或 FATAL 异常；临时测试入口在验证后删除。
- **人工目视（未做）**：AC 维度内实际雾色/天空观感、`/particle abyssalcraft:abyssal_fx` 生成观感、音效播放——headless 不能开窗/入维/截图。

## 修订日志

- 2026-08-06：新增世界空间线条核心 Shader、短寿命批处理与 `LineEffectMessage` 数据流；首接 Staff of Rending。线条随后升级为带端盖八面棱柱、四阶双颜色纯色渐变和侧面明暗，并完成 Forge 世界内顶点提交回归。
- 2026-07-30：恢复 1.12.2 `PEStreamParticleFX` 专用三色粒子、20 tick/0.65 阻尼、8 帧动画与每格 15 点轨迹；补齐神像向玩家、掉落物和 collector 的成功传能通知。

## 8. Energy Pedestal BER (RR-BER-R4-HOSTS)

> Owner: Agent BER · Task: RR-BER-R4-HOSTS (Stage R4) · CR-TBD

### 8.1 概述 / 目标

忠实移植 1.12.2 `TileEntitySingletonInventoryBlockRenderer` 对 **energy pedestal** 的浮空物品渲染。这是 R3/R4 前唯一真实存在且确实需要特殊渲染的 energy 宿主。

### 8.2 范围与审计结果

**审计结论**：
- **需要 BER**：**EnergyPedestalBlockEntity** — 单槽库存（存储能量物品），玩家右键放入/取出，物品浮空展示在基座上方并旋转。1.12.2 使用 `TileEntitySingletonInventoryBlockRenderer` 渲染。
- **不需要 BER**（带证据）：
  - **EnergyContainerBlockEntity** — 双槽库存（输入/输出能量物品），**有 GUI**（`MenuProvider`），玩家通过 `EnergyContainerMenu`/`EnergyContainerScreen` 交互，不需要浮空展示。
  - **EnergyDepositionerBlockEntity** — 双槽库存（Stone Tablet 处理），**有 GUI**（`MenuProvider`），玩家通过 `EnergyDepositionerMenu`/`EnergyDepositionerScreen` 交互，不需要浮空展示。
  - **其他 energy 宿主** — DeityStatueBlockEntity（无库存，充能区域效果）、EnergyCollectorBlockEntity（无库存，收集 PE）、EnergyRelayBlockEntity（无库存，传输 PE）、IdolOfFadingBlockEntity（无库存，区域效果）、PlaceOfPowerBaseBlockEntity（无库存，多方块结构基座）— 均无单槽展示库存，不需要 BER。

**不属于本任务**：
- Research Table / Ritual Pedestal / Rending Pedestal — 已在 R2/R3 完成（`ResearchTableRenderer`/`RitualPedestalRenderer`/`RendingPedestalRenderer`）。
- 断头（severed heads）、Jzahar spawner、sealing lock、ODB — 属于后续内容，不在 R4 范围。

### 8.3 实现

**文件**：`client/render/block/EnergyPedestalRenderer.java`（新增）

**设计**：
- 继承 `BlockEntityRenderer<EnergyPedestalBlockEntity>`
- 读取 `pedestal.getStoredItem()`（继承自 `InventoryBlockEntity.getStoredItem()`，等价于 1.12.2 `ISingletonInventory`）
- 浮空高度：`BlockItem` 0.56F，其他物品 0.37F（忠实 `RitualPedestalRenderer` 高度）
- 旋转：基于世界时间 `getLevel().getGameTime() + partialTick`（Y 轴连续旋转）
- 渲染位置：中心 (0.5, 1.5, 0.5)，Z/X 翻转 180°，然后下移 height，最后 Y 旋转
- 使用 `ItemRenderer.renderStatic`，`ItemDisplayContext.GROUND`，`OverlayTexture.NO_OVERLAY`

**注册**：由 Gate Integrator 在 `ACBlockEntityRenderers.register` 添加一行：
```java
renderers.registerBlockEntity(EnergyBlocks.ENERGY_PEDESTAL_BE.get(), EnergyPedestalRenderer::new);
```

**跨版本 / 加载器要点**：
- **完全 fork-free**：`BlockEntityRenderer`/`BlockEntityRendererProvider.Context`/`PoseStack`/`Axis`/`ItemRenderer`/`ItemDisplayContext`/`OverlayTexture` 在 1.20.1 与 1.21.1 同签名
- 零平台分叉代码，无需修改 `platform/`

### 8.4 验证 / DoD

- **编译**：两节点 `compileJava` EXIT=0，`EnergyPedestalRenderer` 零告警
- **静态检查**：
  - `EnergyPedestalBlockEntity extends InventoryEnergyBlockEntity extends InventoryBlockEntity`（✓ 有 `getStoredItem()`）
  - `EnergyBlocks.ENERGY_PEDESTAL_BE` 已注册（✓ R3 RR-ENERGY）
  - 注册行由 Gate Integrator 添加到 `ACBlockEntityRenderers.java`（Gate 前置）
- **运行期**（待 Gate）：两节点 `runClient` 加载 `EnergyPedestalRenderer` 零错，pedestal 放入能量物品后浮空旋转可见（人工目视）

### 8.5 建议验证命令

由 Gate Integrator 或后续验证执行：

```mcfunction
# Forge/NeoForge 各测试一次
/give @s abyssalcraft:energy_pedestal
/give @s abyssalcraft:cthulhu_charm  # 示例能量物品
# 放置 pedestal，右键放入 charm，观察浮空旋转渲染
```

**预期结果**：
- Pedestal 上方可见 charm 物品模型
- 物品持续 Y 轴旋转
- BlockItem（如放 crystal cluster）高度 0.56F，普通物品 0.37F
- 移除物品后渲染消失

**阻塞因素（无）**：
- `EnergyPedestalBlockEntity` 已在 R3 完成（RR-ENERGY）✓
- `InventoryBlockEntity.getStoredItem()` 已存在（PC-1 框架）✓
- `ItemRenderer`/`PoseStack` 均为 vanilla fork-free API ✓

## 修订日志

- **2026-07-27**：添加 §8 Energy Pedestal BER（RR-BER-R4-HOSTS，Agent BER）。审计证明只有 EnergyPedestalBlockEntity 需要 BER；EnergyContainer/Depositioner 有 GUI 不需要浮空渲染；其他 energy 宿主无展示库存。新增 `EnergyPedestalRenderer.java`（忠实 `RitualPedestalRenderer` 模式），注册行交 Gate Integrator。
- **2026-07-28 / T4.6d Agent 自动闭包**：冻结注册表实际存在的 25 个 AbyssalCraft `BlockEntityType`（不是按旧 TESR 清单猜测）。需要且已注册 BER 的 4 个为 `research_table`、`ritual_pedestal`、`rending_pedestal`、`energy_pedestal`；其余 21 个不需要 BER：
  - GUI/容器内部展示：`crate`、`crystallizer`、`energy_container`、`energy_depositioner`、`materializer`、`sequential_brewing_stand`、`state_transformer`、`transmutator`；
  - 静态 blockstate/model 或无展示库存的逻辑宿主：`deity_statue`、`energy_collector`、`energy_relay`、`idol_of_fading`、`multi_block`、`ritual_altar`、`spirit_altar`、`tombstone`；
  - 无方块的框架 smoke 类型：`directional`、`inventory`、`machine`；
  - `sealing_lock`：BE 仅持久化 unlocked/marker，外观与碰撞由 `LOCKED` blockstate 的静态模型表达；`portal_anchor`：BE 仅保存目的地/颜色/portal UUID，动态 portal 由独立 `DimensionPortal` 实体渲染。二者均不添加 BER。
  - Legacy 分类：四断头（`dghead`/`phead`/`whead`/`ohead`）为 **REPLACED**，现代实现是带 `facing` variant 的静态 blockstate/model，无 `BlockEntityType`；Jzahar spawner 为 **REPLACED**，结构 marker 放置 vanilla `Blocks.SPAWNER`，使用 vanilla spawner BE/renderer。没有无理由永久 BLOCKED 项，也没有等待虚构宿主的 DEFERRED 项。
  - 永久自动门：client runtime 精确断言上述 4 个 BER；server datagen 的 `BlockEntityRendererHostAudit` 仅依赖通用注册表，断言 `registered=4/noBer=21/total=25/replacedLegacy=5/deferredLegacy=0`，不加载 client 类。
- 2026-07-24：记录 RR-RENDER-AUTO 双端 client load gate：Forge/Neo 均抵 Sound Engine + atlas，实体 renderer/model layer/BER/GeckoLib 零异常；游戏内视觉仍独立待验，详情见 entity subsystem §12.6。
- 2026-07-22：初版（PH-2/PH-3/PH-4，Stage H1，CR-52）。雾色 + 音效 + 粒子框架双端交付；天空盒渲染与带数据粒子延后。
