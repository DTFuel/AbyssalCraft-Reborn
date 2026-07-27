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
- PH-3 框架 + 3 粒子 ☑：`registry/ModParticles`（`abyssal_fx` + `blue_flame` `SimpleParticleType`）+ `client/particle/{ACFadeParticle,BlueFlameParticle}` + `platform/ParticleCompat`（事件 fork）+ 描述符与贴图。BlueFlame + ItemRitual（vanilla `item` payload）由 `client/ritual/ClientRitualEffects` 8 基座发射；PEStream 由 RR-NET（`ClientNetworkEffects.peStream`）。

**RR-CLIENT-FX 收口（CR-73，2026-07-26）**：
- 天空盒（原 PH-2b）☑：`platform/DimensionSkyCompat` 吸收 Forge `IForgeDimensionSpecialEffects.renderSky(PoseStack…)` ↔ Neo `IDimensionSpecialEffectsExtension.renderSky(Matrix4f…)` 签名 fork，以及 1.20 `Tesselator.getBuilder()/vertex().uv().endVertex()/end()` ↔ 1.21 `begin()/addVertex().setUv()/buildOrThrow()` 顶点 fork；`getPositionTexShader`+`setShaderColor` 绘 ±100、16×16 UV 六面天空盒，`SkyType.NONE` 屏蔽 vanilla 天体。四维贴图/色：AW `abyssal_wasteland_sky` 0/105/45、Dreadlands `dreadlands_sky` 100/14/14、Omothol `omothol_sky` 40/30/40、Dark Realm 复用 `omothol_sky` 30/20/30，色实时取自 `ClientVars`（reload 生效；完整 clientvars 仍 T6.6d）。
- 粒子（原 PH-3b）☑：BlueFlame（`blue_flame` type + `BlueFlameParticle`，迁 blueflame.png）与 ItemRitual（vanilla `ItemParticleOption(ITEM, stack)`）由 `ClientRitualEffects` 在 8 基座每 tick 发 BlueFlame+smoke、每 3 tick 发 ItemRitual 向祭坛，供品由 `RitualManifestCatalog.offeringLayout()`+`RitualIngredient.example()` 重建（不扩 `RitualStartMessage`）。PEStream 归 RR-NET（`PEUtils` 发 `PEStreamMessage` → `ClientNetworkEffects.peStream`），未重复实现。
- 声音（原 PH-4b 收尾）☑：45/45 事件均有生产触发；`AbstractShoggoth.playStepSound` 补最后一个未接的 `shoggoth.step`（0.15/1.0），并修正 `sounds.json` 中 `jzahar.shout` 指向缺失键→现有 `.shouts` 译文键。
- Gate：永久 `data/gen/ClientFxSelfTest`（+`ClientFxValidationData`）双端 runData 输出 `RR_CLIENT_FX_SELF_TEST_OK skies=3 particles=2 sounds=45 ogg=106 subtitles=41 rituals=62`；双端 compile/build/JAR 通过。
- **仍待（人工）**：T6.3c 四维天空/雾双端目视、T6.5c 声音/字幕双端听觉矩阵。

**延后（诚实 · 见平行表）**：
- **PH-1b 当前事实**：Crystal Bag、Spirit Tablet 与 Spellbook 屏幕均已完成；剩余是 energy/depositioner/rending 等屏幕（归 RR-CLIENT-GUI）。1.12.2 无专属粒子贴图（原用 vanilla atlas 帧）。

## 3. 设计 / 架构

**音效（fork-free）**：`ModRegistrar<SoundEvent>` over `Registries.SOUND_EVENT`；每个事件 `SoundEvent.createVariableRangeEvent(ACRef.id(id))`，`id` 与 `sounds.json` 键一致（点号在 RL path 合法）。`EVENTS` map 存 `id -> Supplier<SoundEvent>` 供下游实体/方块接线。经 `ModRegistries.ALL` 挂载。

**天空/雾**：`ACDimensionEffects extends DimensionSpecialEffects`，构造 `super(Float.NaN, false, SkyType.NONE, false, false)`；`getBrightnessDependentFogColor` 返回常量雾色（`null` 时退回 overworld 亮度缩放，供 AW），`isFoggyAt` 返回是否厚雾。`ACDimensionSkies` 用中性 sink 注册 4 维。`DimensionEffectsCompat` 只在 `attach` 内 import 分叉事件。**关键**：每维 `dimension_type` 的 `effects` 字段须指向注册的 id（本子系统把 4 维 effects 改为 `abyssalcraft:<dim>`，`mini` 保持 `minecraft:overworld`）。

**粒子**：`ModParticles` 注册 `SimpleParticleType`（`new SimpleParticleType(false){}` 匿名子类绕 protected 构造）；`ACFadeParticle extends TextureSheetParticle`（把 1.12.2 `BufferBuilder` 即时绘制重写为 sprite 驱动：0.1x 初速、0.75 基准尺寸、`age/lifetime*32` 渐入、摩擦、`ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT`）+ 内嵌 `Provider(SpriteSet)`；`ParticleCompat` 在 `attach` 内 import 分叉 `RegisterParticleProvidersEvent`，中性 sink `registerSpriteSet`。需 `assets/.../particles/<name>.json` 描述符 + `textures/particle/<name>.png` sprite。

## 4. 子系统内契约

- 三块全为纯客户端：经主类 `SideExecutor.runWhenClient` 内 `DimensionEffectsCompat.attach` / `ParticleCompat.attach` 挂 MOD 总线；`ModSounds`/`ModParticles` 的注册器经 `ModRegistries.ALL`（两侧都注册，音效/粒子类型是通用注册项）。
- 忠实 1.12.2 雾色常量：Dreadlands `(0.2, 0.03, 0.03)`；Dark Realm / Omothol `0xA080A0 * 0.15`≈`(0.094, 0.075, 0.094)`；AW 无 `getFogColor` override → 默认亮度雾。厚雾（`doesXZShowFog`）：Dreadlands、Omothol 开。
- 天空 tint RGB（供未来天空盒）：AW `(0,105,45)`、Dreadlands `(100,14,14)`、Omothol `(40,30,40)`。

## 5. 跨版本 / 加载器要点

- **fork-free**：`SoundEvent.createVariableRangeEvent` / `Registries.SOUND_EVENT` / `DimensionSpecialEffects` / `Vec3` / `SkyType` / `TextureSheetParticle` / `ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT`（1.21.1 仍存）/ `SimpleParticleType` 匿名子类——1.20.1 与 1.21.1 同签名，均以两节点 `compileJava` 实证。
- **仅 import 分叉**（3 个 client 事件，Forge `net.minecraftforge.client.event.*` ↔ NeoForge `net.neoforged.neoforge.client.event.*`，方法签名同）：`RegisterDimensionSpecialEffectsEvent.register(ResourceLocation, DimensionSpecialEffects)`、`RegisterParticleProvidersEvent.registerSpriteSet(ParticleType, SpriteParticleRegistration)`。
- **深分叉（延后）**：即时模式天空盒绘制（`Tessellator`/`BufferBuilder` → 1.21 新管线），故天空盒渲染未移植。

## 6. 实现记忆 / 踩坑 (verified gotchas)

- `dimension_type` 的 `effects` 若不指向已注册的 effects id，vanilla 静默回退 overworld 效果（runClient 会打 fallback 警告）——须两侧对齐；本子系统 runClient 日志**无** fallback 警告 = 4 维 effects 均已绑定。
- `SimpleParticleType` 构造是 `protected`：跨包用 `new SimpleParticleType(false){}` 匿名子类。
- 注册粒子类型必须同时给 provider + 描述符 + sprite，否则 atlas 缺 sprite；本子系统只注册了 `abyssal_fx`（三者齐全，`/particle abyssalcraft:abyssal_fx` 可测）。
- 忠实保留 1.12.2 音效 quirk：`sounds.json` 引用 `chant.yog_sothoth_1/_2`、`hastur_1/_2`、`jzahar.shout`，而 lang 键为 `yog_sothoth`/`hastur`/`shouts`（不匹配即无字幕，与旧版一致）。

## 7. 验证 / DoD

- 两节点 `compileJava` EXIT=0（锁定上述所有 API + 3 事件 fork）。
- **forge `runClient`** 进世界后干净退出 BUILD SUCCESSFUL：Sound engine started 且**无** `sounds.json` 解析错；`particles.png-atlas` 建成且**无** `abyssal_fx` sprite 错；4 个 AC 维度加载且**无** DimensionSpecialEffects fallback 警告。
- **人工目视（未做）**：AC 维度内实际雾色/天空观感、`/particle abyssalcraft:abyssal_fx` 生成观感、音效播放——headless 不能开窗/入维/截图。

## 修订日志

- 2026-07-24：记录 RR-RENDER-AUTO 双端 client load gate：Forge/Neo 均抵 Sound Engine + atlas，实体 renderer/model layer/BER/GeckoLib 零异常；游戏内视觉仍独立待验，详情见 entity subsystem §12.6。
- 2026-07-22：初版（PH-2/PH-3/PH-4，Stage H1，CR-52）。雾色 + 音效 + 粒子框架双端交付；天空盒渲染与带数据粒子延后。
