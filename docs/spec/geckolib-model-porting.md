# GeckoLib 模型移植 子系统规格 (GeckoLib Model Porting Spec)

- 里程碑 / Stage：Stage E / PE-4（GeckoLib 骨骼模型移植；Chagaroth 为首个忠实网格）
- 关联平行任务：PE-4（BOSS 渲染，本文主体）、PE-1（渲染注册框架 `EntityRendererCompat`/`ACEntityRenderers`，见 [entity-subsystem.md](entity-subsystem.md) §12）、PD-7（`BossMob`/`GeoEntity` 宿主）；方块场景承 Betweenlands 的 GeoBlock 先例
- 状态：**forge 节点 Chagaroth 目视验证忠实**（形状/朝向/比例/贴图，用户确认）+ 模型**静态**（无动画，属预期）；两节点 `compileJava` 绿；neoforge `runClient` 未目视
- 负责：PE-4（GeckoLib 验证 + Chagaroth 首网格）
- 最后更新：2026-07-22

## 1. 概述 / 目标

把 1.12.2 里用 Java `ModelBase`（entity-style `ModelRenderer` 箱体模型，如 `client.model.entity.ModelChagaroth`）定义几何的模型，用 **GeckoLib** 骨骼模型（`.geo.json`，Bedrock 1.12.0 格式）在运行时忠实重现，而非手抄成 vanilla `MeshDefinition` / 方块模型 JSON。

本子系统交付：**双加载器 GeckoLib 依赖** + **转换脚本**（`scripts/convert_modelbase_to_geo.js`：`ModelBase.java` → `.geo.json`）+ **实体 GeckoLib 三件套模式**（`GeoEntity` 宿主 + `GeoModel` + `GeoEntityRenderer`）并接入 PE-1 渲染注册。首个产物 = **Chagaroth** BOSS 忠实网格（= PE-4 的 GeckoLib 端到端验证 + PE-4b 第一只忠实 boss）。

本项目是 **多加载器**（Architectury Loom + Stonecutter）：一套源码预处理成 Forge 1.20.1 节点与 NeoForge 1.21.1 节点。GeckoLib 的 **坐标、构件名、乃至 API 包路径都随节点不同**，故下文大量篇幅在讲「一套源码同时对两节点编译」。

## 2. 范围

- **含**：GeckoLib 依赖（双加载器 · repo · per-loader · mclib · 无 `initialize()`）、转换脚本 `convert_modelbase_to_geo.js` 及其坐标公式、实体三件套（`content/entity/boss/BossMob`(GeoEntity 宿主) + `client/render/entity/boss/ChagarothGeo{Model,Renderer}`）+ 家族分派 `client/render/entity/BossRenderers` 接入 `ACEntityRenderers`、方块 GeoBlock 变体（简述）、Chagaroth 资产（geo/贴图/空动画）。
- **不含**：**动画**（`registerControllers` 空、空 `.animation.json`；延 PE-4b/后续）、其余 11 BOSS + 各族忠实网格（PE-4b / PE-2..6）、Betweenlands 方块的逐块细节（承其原始参考；本项目暂无 GeckoLib 方块落地）、PE-1 渲染注册框架本体（见 entity-subsystem.md §12）。

## 3. 设计 / 架构

### 3.1 为什么 GeckoLib（方块=硬约束；实体=选择）

- **方块——硬约束**。`ModelBase`-baked 的**方块**几何用**任意角度箱体旋转**；vanilla 1.20.1 方块模型 JSON 的 `rotation` 只允许单轴 ±22.5°/±45°，**无法表达** → 必须 `BlockEntityRenderer` 运行时绘制，GeckoLib 是从原 `ModelRenderer` 数据出发最实际的路子。
- **实体——是选择，非约束**。vanilla 实体模型（`ModelPart`/`LayerDefinition`/`MeshDefinition`）**支持**任意 per-part `xRot/yRot/zRot`，故 1.12.2 实体模型**能**手抄成 `MeshDefinition`。仍选 GeckoLib，因为：① 转换脚本把 `ModelBase` 近乎 1:1 机械转成 `.geo.json`，比手抄 100+ 箱体（`ModelChagaroth` 就 107 箱）快且忠实；② GeckoLib 自带数据驱动动画系统（`.animation.json`），后续加动画不用改渲染类。
- 结论：**方块 → GeckoLib 必需；实体 → GeckoLib 是「复用转换脚本 + 预留动画」的务实选择**。两者共用同一转换脚本与坐标公式（§3.4 / §6）。

### 3.2 实体三件套 + PE-1 接入

一只忠实 GeckoLib 实体 = 三个业务类 + PE-1 分派器里一行。Chagaroth 为样板：

| 类 / 文件 | 职责 |
|---|---|
| 宿主生物类 `content/entity/boss/BossMob.java` | `implements GeoEntity`。持 `AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this)`；`registerControllers(...)`（静态模型 = 空）；`getAnimatableInstanceCache()` 返回 cache。**唯一带 `.core` `//?` 分叉的类**（§5）。|
| `client/render/entity/boss/ChagarothGeoModel.java` | `extends GeoModel<BossMob>`。三个资源 getter 返回 `ACRef.id(...)`：`geo/entity/chagaroth.geo.json`、`textures/model/boss/chagaroth.png`、`animations/entity/empty.animation.json`。|
| `client/render/entity/boss/ChagarothGeoRenderer.java` | `extends GeoEntityRenderer<BossMob>`；构造 `super(context, new ChagarothGeoModel())`。|
| `client/render/entity/BossRenderers.java` | 家族分派（E2 idiom）：注册渲染器并把 `EntityType` 加进 `handled`，让 E1 占位渲染器跳过它。|

静态模型 `GeoModel`：

```java
public class ChagarothGeoModel extends GeoModel<BossMob> {
    private static final ResourceLocation MODEL     = ACRef.id("geo/entity/chagaroth.geo.json");
    private static final ResourceLocation TEXTURE   = ACRef.id("textures/model/boss/chagaroth.png");
    private static final ResourceLocation ANIMATION = ACRef.id("animations/entity/empty.animation.json");
    @Override public ResourceLocation getModelResource(BossMob a)     { return MODEL; }
    @Override public ResourceLocation getTextureResource(BossMob a)   { return TEXTURE; }
    @Override public ResourceLocation getAnimationResource(BossMob a) { return ANIMATION; }
}
```

单只生物用 per-entity 静态路径即可。**若一个类背多变体**（如 `BossMob` 经 `BossKind` 背 4 个 boss），应在 `GeoModel` 内按 `animatable.getType()`/kind 返回不同 geo/贴图，而非一变体一类。

接入 PE-1 的 `EntityRendererCompat`（由 `ACEntityRenderers` 驱动，E2 分派 idiom：各家族注册忠实渲染器 + 把类型加进 `handled`，末尾循环给未处理的 AC 实体注册占位）：

```java
public static void register(EntityRendererCompat.Renderers renderers, Set<EntityType<?>> handled) {
    EntityType<?> chagaroth = BossEntities.CHAGAROTH.get();
    // 注意：方法引用不能直接喂给 register(EntityType<?>, EntityRendererProvider<?>)（通配符捕获编译失败），
    // 必须先赋给一个具型 provider 局部变量。
    EntityRendererProvider<BossMob> provider = ChagarothGeoRenderer::new;
    renderers.register(chagaroth, provider);
    handled.add(chagaroth);
}
```

`ACEntityRenderers.registerRenderers` 里一行 `BossRenderers.register(renderers, handled);`。Forge/NeoForge 的 `registerEntityRenderer` 是 map-put，故给某类型注册真渲染器即**覆盖** E1 占位。

### 3.3 方块 GeoBlock 变体（简述，本项目暂无落地）

同转换脚本、同公式，运行时换成 `BlockEntity` + `GeoBlockRenderer`：块 `implements EntityBlock` + `getRenderShape()=ENTITYBLOCK_ANIMATED` + **务必 `.noOcclusion()`**；共享一个 `GeoBlockEntity` 类型；`GeoModel` 按**块 id** 取 geo/贴图；`GeoBlockRenderer` 覆写 **`actuallyRender(...)`**（非 `render`，见 §6）。资产落 `geo/block/<id>.geo.json` + `textures/block/<id>.png`。

### 3.4 转换脚本 `scripts/convert_modelbase_to_geo.js`

Node.js 脚本（原为 Betweenlands 方块写，本项目扩展支持 AC 实体），解析 1.12.2 `ModelBase` Java 类，输出 GeckoLib `.geo.json`：

```
node scripts/convert_modelbase_to_geo.js <1.12.2 ModelX.java> <out .geo.json> <identifier>
```

解析：`this.textureWidth/Height`、`new ModelRenderer(this,u,v)`(=texOffs)、`setRotationPoint(x,y,z)`、`addBox(ox,oy,oz,w,h,d[,scale])`、旋转、mirror、`parent.addChild(child)`。

**本项目为 `ModelChagaroth` 加的扩展**：① 识别 `setRotation(name,x,y,z)` 私有助手（AC 多个模型用它而非 `setRotateAngle`）；② `NAME.mirror = true` → 输出 per-cube `"mirror": true`（`ModelChagaroth` 每箱都 mirror）。

坐标公式见 §6（公式表 + 符号规则）。

## 4. 子系统内契约

- **实体注册名 / ID**：`abyssalcraft:chagaroth`（`BossKind.CHAGAROTH.id()="chagaroth"`，`BossEntities.CHAGAROTH` 经 `ENTITIES.register(kind.id(), …)`）。尺寸 `2.0×4.8`（`sized(width,height)`，测试需后退/抬头看全身）。
- **资源路径约定**（`ACRef.id(...)` → `abyssalcraft:` 命名空间）：
  - 模型 `assets/abyssalcraft/geo/entity/<id>.geo.json`
  - 贴图 `assets/abyssalcraft/textures/model/<...>/<id>.png`（Chagaroth 为 `.../boss/chagaroth.png`，128×64）
  - 动画 `assets/abyssalcraft/animations/entity/empty.animation.json`（静态共用：`{"format_version":"1.8.0","animations":{"misc.empty":{"loop":true}}}`）
- **`GeoModel<T>` 抽象方法**（两 build 同）：`getModelResource(T)`、`getTextureResource(T)`、`getAnimationResource(T)` → `ResourceLocation`。
- **`GeoEntityRenderer<T>` 构造**（两 build 同）：`(Context, EntityType<? extends T>)` 与 `(Context, GeoModel<T>)`；本项目用后者。
- **对外契约**：给一只实体上 GeckoLib = 让其宿主类 `implements GeoEntity`（实现 3 成员 + `.core` `//?` import 分叉，§5）+ 写 `GeoModel`/`GeoEntityRenderer` 子类 + 在家族分派器注册（provider 先赋具型局部变量）。

## 5. 跨版本 / 加载器要点

### 5.1 依赖（`build.gradle.kts`）

```kotlin
repositories { maven("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/") }   // GeckoLib

// dependencies{}：无 common/multiloader 构件，故每节点各自拉
if (loader == "neoforge") {
    // 4.9.x 把数学内化进 software.bernie.geckolib.loading.math（无外部 mclib）
    "modImplementation"("software.bernie.geckolib:geckolib-neoforge-1.21.1:4.9.2")
} else {
    "modImplementation"("software.bernie.geckolib:geckolib-forge-1.20.1:4.8.4")
    // 4.8.x 依赖 com.eliotlash.mclib（数学）。GeckoLib 生产 jar 用 JarJar 内嵌它，但 dev(classpath)
    // 启动时 Forge 的 JarInJar 定位器会跳过 → 到不了 GeckoLib 的 transforming 类加载器。
    // forgeRuntimeLibrary 把这个纯库放上 dev 运行时 classpath。
    "forgeRuntimeLibrary"("com.eliotlash.mclib:mclib:20")
}
```

- 构件 `software.bernie.geckolib:geckolib-<loader>-<mc>` —— Forge 1.20.1→**4.8.4**、NeoForge 1.21.1→**4.9.2**。
- **无 `GeckoLib.initialize()`**：GeckoLib 4.x 经自身 `@Mod` 自注册，无需在主类构造里手动 init（已核，无此调用仍能渲染）。旧教程里的 `initialize()` 对 4.x 已过时。
- **mods.toml 依赖声明（建议、暂缺）**：发布前应在 `mods.toml` **与** `neoforge.mods.toml` 都声明 `geckolib` 硬依赖（缺失即 fail-fast）。当前 AC **未声明**（dev 下 GeckoLib 在 classpath 上故能跑）。

### 5.2 GeckoLib 4.8.4 vs 4.9.2 —— `.core` 包分裂（唯一破坏跨编译的点）

多数我们用到的类型两 build **同包**（javap 双 jar 核实）：`GeoEntity`(`.animatable`)、`GeckoLibUtil`(`.util`)、`GeoModel`(`.model`)、`GeoEntityRenderer`(`.renderer`)、`GeoBlockEntity`/`GeoBlockRenderer`。

**两个类搬了家**——4.9 去掉了 `.core.` 段：

| 类型 | 4.8.4（Forge 1.20.1） | 4.9.2（NeoForge 1.21.1） |
|---|---|---|
| `AnimatableInstanceCache` | `software.bernie.geckolib.`**`core`**`.animatable.instance` | `software.bernie.geckolib.animatable.instance` |
| `AnimatableManager`（`.ControllerRegistrar`） | `software.bernie.geckolib.`**`core`**`.animation` | `software.bernie.geckolib.animation` |

这两者出现在 `GeoEntity` **强制的 `@Override` 签名**里（`registerControllers`/`getAnimatableInstanceCache`），无法藏进兼容层 → **`implements GeoEntity` 的类必须用 Stonecutter 分叉这两个 import**。这是「`//?` 分叉只应落 `platform/` 或主类」规则的**书面例外**（`content/entity/boss/BossMob.java`）：

```java
import software.bernie.geckolib.animatable.GeoEntity;      // 两端同
import software.bernie.geckolib.util.GeckoLibUtil;         // 两端同
//? if forge {
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
//?} else {
/*import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
*///?}
```

**业务类保持零分叉**：`GeoModel`/`GeoEntityRenderer` 子类的 API 两端一致，只有 `GeoEntity` 宿主需分叉。若日后升级任一端 GeckoLib，须 `javap` 复核这两个包路径。

## 6. 实现记忆 / 踩坑 (verified gotchas)

- **【mclib dev 崩溃 · 一手崩溃栈】** GeckoLib 4.8.x 的 `com.eliotlash.mclib` 是 JarJar 内嵌的；dev(classpath) 启动时 Forge `JarInJarDependencyLocator` 打印 `No dependencies to load found. Skipping!`，mclib 到不了 GeckoLib 类加载器，客户端**首次资源重载即崩**：
  ```
  Rendering overlay → NoClassDefFoundError: software.bernie.geckolib.util.JsonUtil
                    → NoClassDefFoundError: com/eliotlash/mclib/math/IValue
  ```
  修：用 Architectury Loom 的 **`forgeRuntimeLibrary`** 把 `com.eliotlash.mclib:mclib:20` 放上 Forge dev 运行时 classpath。**`runtimeOnly`/`localRuntime` 不够**——它们进 `runtimeClasspath` 但**不进** ModLauncher 交给 mod(transforming) 层的 classpath，GeckoLib 仍看不见（实测两次同样崩）。判据：字符串 `mclib` 须出现在 `versions/1.20.1-forge/.gradle/loom-cache/forge_minecraft_classpath.txt`。**若改了依赖仍崩**：删该缓存文件让 Loom 重生成——`generateDLIConfig`/`configureClientLaunch` 会以 `UP-TO-DATE` 保留旧 classpath。NeoForge 4.9.2 无此依赖（数学已内化）。
- **【转换公式 · MC ModelRenderer → GeckoLib geo】** 坐标单位 = 像素（16=1 块）。`absRot` = 沿 `addChild` 父链累加 `rotationPoint`（子 `setRotationPoint` 相对父）。几何**在 X/Z 上以像素 0 居中**，Y 翻转使模型 `Y=0` 落在原点底：

  | geo.json 字段 | 值 |
  |---|---|
  | `description.texture_width/height` | 模型 `textureWidth`/`textureHeight` |
  | bone `pivot` | `[ absRotX, 24 − absRotY, absRotZ ]` |
  | bone `rotation`（度） | `[ +radX·180/π, −radY·180/π, +radZ·180/π ]` ← **X、Z 保号；仅 Y 取负** |
  | bone `parent` | `addChild` 得到的父渲染器名（若有） |
  | cube `origin` | `[ absRotX+ox, 24 − (absRotY+oy+h), absRotZ+oz ]` |
  | cube `size` / `uv` | `[w,h,d]` / `[texU,texV]`（Bedrock box-uv） |
  | cube `mirror` / `inflate` | 渲染器 `mirror=true` 时置 true / `addBox` 第 7 参 scale 非零时置 |

- **【旋转符号规则 · 关键】** 因几何 Y 翻转（`24 − mc_y`），**X、Z 保留原符号、仅 Y 取负**。全取负在对称模型上看着没事，但会把非对称模型翻里朝外/上下颠倒。此约定原在 Betweenlands `offering_table` 校准（全取负把腿翻内），**在 AC Chagaroth 上复验通过**（直立、比例对），故对**实体**同样成立，非只方块。（脚本文件头注释里 `[-degX,-degY,-degZ]` 是**陈旧错注**；以代码 `degX/degY/degZ` 为准。）
- **【通配符捕获】** 方法引用不能直接喂 `register(EntityType<?>, EntityRendererProvider<?>)`（通配符捕获，编译失败）→ 先 `EntityRendererProvider<BossMob> provider = XGeoRenderer::new;` 再传（§3.2）。
- **【GeckoLibCache 在资源重载期加载全部 geo/animation】** 早于任何实体生成 → 坏 geo/animation 会在启动 loading overlay 处崩（mclib 崩即在此暴露）；好处是转换脚本坏输出 fail-fast。
- **【EnderDragon 式 ModelBase — 转换脚本已扩展支持】** dragonboss/dragonminion 的 1.12.2 model 用 vanilla `ModelDragon` 式 `setTextureOffset("bone.sub",u,v)` + **具名** `addBox("sub", x,y,z,w,h,d)`（String 首参）+ 局部 float 变量坐标表达式（如 `-8.0F + f1`）。**初版**转换脚本认 numeric-first `addBox(x,y,z,w,h,d)` → 误解析 → 坐标 NaN → `JSON.stringify(NaN)="null"` → GeckoLib `Cube` 反序列化 `getAsDouble(JsonNull)` **资源重载崩**（`GeckoLibException`，forge runClient 实证捕获）。**已扩 `convert_modelbase_to_geo.js`**：(1) 收集局部 `float NAME = VAL;` 变量表；(2) `setTextureOffset("bone.sub",u,v)` → UV 映射；(3) `evalNum()` 算术求值器（去 fFdD 后缀、替变量、安全 eval，解 `-8.0F + f1`）；(4) String `new ModelRenderer(this,"name")` 构造；(5) 具名 `addBox` 取 per-box UV（`texOffs[bone.sub]`）；(6) `setRotationPoint` 走求值器；(7) 删了废弃的 `cube.uv=[r.texU,r.texV]` 覆盖行（本就支持 per-box uv）。**标准 numeric 模型不受影响**（求值器对纯数字与旧 `num()` 等价，8 boss bone 数不变）。**注意静态 rest-pose 局限**：EnderDragon 式模型的 neck/tail 是**程序化**的（在 `render()` 循环里逐段定位、无 `setRotationPoint`）→ 转出的静态 geo 把它们渲在原点（可能与体重叠/与头断裂）；head 靠 box 坐标前移。故 dragon 静态网格可能不完美，需人工目视。**【已证 + 已改 Java 模型（CR-49）】** 用户目视确认：dragon 转出的静态 geo 果然渲成**扁平/塌陷**（程序化样条无法烘进静态网格）。**故弃 GeckoLib、改忠实 Java 模型**——`client/model/entity/DragonModel`（`extends HierarchicalModel<Mob>`，忠实盒子/UV 承 1.12.2 = vanilla `EnderDragonModel`，**手编固定悬停姿势**代替程序化样条）+ `client/render/entity/boss/DragonRenderer`（`extends MobRenderer<Mob,DragonModel>`，fork-free，vanilla 处理 `renderToBuffer` 的 1.20↔1.21 分叉，承 PE-3 GhoulModel 先例）。删两 geo、从 `Boss/EliteGeoModel` 摘除。**转换脚本对 EnderDragon 式的扩展仍保留**（其它程序化-静态混合模型或有用），但**程序化 neck/tail 的实体不应走 GeckoLib 静态 geo，应走 Java `HierarchicalModel` 手编姿势**。姿势/比例为作者手编近似，需用户 `/summon` 截图逐轮调。
- **【转换 geo 后必跑 runClient 查 GeckoLibException】** valid-JSON + bone-count 自检**查不出** NaN-as-double / GeckoLib 语义错（NaN 被 `JSON.stringify` 写成 `null`，JSON 仍合法）；GeckoLibCache 资源重载加载全部 geo → 1 个坏 geo 在标题屏**前**崩。转换后务必 forge `runClient` grep `GeckoLibException`（PE-4b 即靠此捕获 dragon 的 null bug；单纯 `ConvertFrom-Json` 通过不够）。
- **【转换脚本贴图尺寸自测路径仍是 Betweenlands 硬编码】** 它从写死的 `thebetweenlands` 贴图目录读真实 PNG 尺寸；AC 路径命不中 → 回退到模型**声明**的 `textureWidth/Height`。Chagaroth 恰好声明 128×64 == 实际贴图，故没事；若未来某模型声明尺寸与实际 PNG 不符，改箱体 UV 或把脚本 `pngSize(...)` 路径改成 `assets/abyssalcraft/textures/...`。
- **【方块 GeoBlockRenderer 覆写坑】** 覆写 **`actuallyRender(...)`** 而非 `render(...)`（后者 raw-type erasure 与 `BlockEntityRenderer<T>` 桥接冲突）；`getFacing` 会按 `FACING` 属性自动定向，BER 须自行重贴 `OffsetType` 偏移。

## 7. 验证 / DoD

- **两节点 `compileJava --rerun-tasks`**：BUILD SUCCESSFUL（全 GeckoLib 接入 + `.core` `//?` 分叉编译器双端核实一致）。
- **forge `runClient`**：加 `forgeRuntimeLibrary` mclib 后，客户端过 GeckoLib 资源加载、抵**标题屏零 GeckoLib 报错**（日志 `OpenAL initialized`/`Sound engine started`；无 `JsonUtil`/`IValue` 崩）。**修 mclib 前**同栈崩两次（一手崩溃栈见 §6）。
- **forge in-world 目视（用户确认）**：`/summon abyssalcraft:chagaroth ~ ~ ~5` → Chagaroth 网格**形状/朝向/比例/贴图全对**；模型**静止**（`registerControllers` 空 + 空 `.animation.json`，属预期，非 bug）。资产：`chagaroth.geo.json`（107 bones / 38 非零旋转 / 每箱 mirror）从 `ModelChagaroth`(107 addBox) 转出。
- **PE-4b（2026-07-22，续 PE-4）**：转换脚本跑 10 boss `ModelBase` → **9 GeckoLib 忠实网格**（chagaroth + jzahar + shadowboss + 6 elite: gskeleton/remnant/shuboffspring/jzaharminion/chagarothfist/chagarothspawn，经 `Boss/EliteGeoModel` 按实体 `getType()` id 解析 geo+贴图）两节点 `compileJava` BUILD SUCCESSFUL + **forge `runClient` 经 GeckoLibCache 全 9 geo 加载零 `GeckoLibException`**、过资源重载抵 texture atlas/Sound engine；`EliteMob` 加 `GeoEntity`（同 BossMob `.core` `//?` 例外）；dreadguard = 手写 `dreadguard.geo.json`（标准 biped，1.12.2 用 `RenderBiped`+`ModelZombie`）；**2 EnderDragon 式 dragon（dragonboss/dragonminion）改 Java `HierarchicalModel`（`DragonModel`+`DragonRenderer`，非 GeckoLib）**——程序化 neck/tail 塌平静态网格，见 §6/CR-49。**精确视觉全靠人工目视**（headless 只证加载不崩/层烘焙不崩、不证网格好看/对位）。
- **未机核项**：① **动画**（当前静态，PE-4b/后续）；② **neoforge `runClient` 未目视**（4.9.2 无 mclib 问题，但网格渲染需单独启 `:1.21.1-neoforge:runClient` 目视复验）；③ 其余 11 BOSS + 各族忠实网格（PE-4b/PE-2..6）。
- **视觉测试交接约定**：需目视时**不用命令自动操控游戏**，改为给用户明确的「测什么 + 操作步骤 + 观察点」，由用户手动跑并反馈；本方可负责非视觉部分（依赖/编译/资源/日志确认能启动到标题屏且无报错）。
- **RR-RENDER-AUTO（2026-07-24）**：`EliteGeoRenderer` 新增 Goliath 装备槽甲、Dreadguard 固定 overlay 与 Dread carrier layer；`BossGeoRenderer` 新增 Jzahar death rays，`BossAnimations` 恢复 Jzahar/Chagaroth 长死亡骨骼状态。Forge/Neo GeckoLib 4.8.4/4.9.2 均 compile + client resource reload 零异常；视觉仍须实际 summon 复核。

## 修订日志

- 2026-07-24（RR-RENDER-AUTO / CR-65）：补 Gecko 怪物甲、carrier 与 Boss death layer；服务端权威同步死亡阶段；双端 compile/build/runClient 通过，目视仍按 §7 交接。
- 2026-07-22（PE-4b 续 · dragon idle 动画）：用户确认 dragon 静态模型「基本没问题」→ 加 `DragonModel.setupAnim` **程序化 idle 动画**。1.12.2 的 neck/tail 靠 `getMovementOffsets`（末影龙运动历史环形缓冲）逐段定位，我们的自定义 `Mob` 没有 → 改用 `ageInTicks` 驱动的正弦摆动。每帧先 `root.getAllParts().forEach(ModelPart::resetPose)`（两端 vanilla API）复位到烘焙姿势，再**加性**叠加：翅膀拍打（`wing.zRot += cos(wave)*0.18`，翼尖相位滞后 0.6）+ 下颚开合（`jaw.xRot += (sin+1)*0.06`，忠实 vanilla `jaw.rotateAngleX=(sin+1)*0.2` 减幅）+ 脖/尾蛇形摆（yRot 正弦波、越近尾尖幅度越大）+ 四腿微动 + 头部 yaw/pitch 追踪（`netHeadYaw`/`headPitch` 度→弧度，分摊 neck 0.3 / neck2 0.3 / head 0.4）。`ANIM_SPEED=0.2`（≈1.5s/周期）可调。构造器改为 `getChild` 取各部件引用（名字全核对 `createBodyLayer`）。两节点 `compileJava` 绿（`resetPose`/`getAllParts`/`Mth` 两端存在）+ forge `runClient` 干净启动无 `NoSuchElement`/烘焙错。**动画观感 = 人工目视**（速度/幅度按反馈可调）。
- 2026-07-22（PE-4b 续 · 2 dragon 改 Java 模型 / CR-49）：用户目视——dragon 转出的静态 GeckoLib geo **渲成扁平/塌陷**（1.12.2 dragon 的 neck/tail 是 `render()` 内程序化样条、无法烘进静态网格）。**弃 GeckoLib、改忠实 Java 模型**：新增 `client/model/entity/DragonModel`（`extends HierarchicalModel<Mob>`，盒子/UV 承 1.12.2 = vanilla `EnderDragonModel`，**手编固定悬停姿势**）+ `client/render/entity/boss/DragonRenderer`（`extends MobRenderer<Mob,DragonModel>`，**fork-free**——`renderToBuffer` 的 1.20↔1.21 分叉交 vanilla，承 PE-3 GhoulModel 先例）；`ModModelLayers` +DRAGON 层、`ACEntityRenderers` 注册 `DragonModel::createBodyLayer`、`BossRenderers` 新 `dragon()` helper 注册两龙并从 GeckoLib 列表摘除；删 `geo/entity/{dragonboss,dragonminion}.geo.json` + 从 `Boss/EliteGeoModel` TEXTURES 摘除。**GeckoLib 侧余 10 boss 网格不变**。两节点 `compileJava` 绿 + forge `runClient` 抵标题屏 0 crash（DRAGON 层烘焙在标题屏前成功，无 `bakeLayer`/`No model for layer` 异常）。**姿势/比例为手编近似，需用户 `/summon` 截图逐轮调**。
- 2026-07-22（PE-4b 续 · 视觉修正）：用户目视反馈——**8 个转换网格全对**（转换脚本对标准 entity 模型可靠），仅 3 个非-GeckoLib 的坏。**全部修正 → 12 boss 全 GeckoLib**：①**dreadguard** 之前用 vanilla `HumanoidModel` + 64×32 旧皮肤（现代 64×64 模型的左臂/腿+外层采样到空的下半 → 缺块/花屏）→ **手写 `dreadguard.geo.json`**（标准 biped、64×32 UV、左肢 mirror）→ `EliteGeoModel`，按普通 elite 注册；②**dragonboss/dragonminion** 之前 stand-in 箱体（256×256 龙 UV 花屏）→ **扩转换脚本支持 EnderDragon 式**（局部 float 变量 + `setTextureOffset` + 算术求值 + String ctor + 具名 addBox per-box UV，见 §6）→ 转出 geo 接 `Boss/EliteGeoModel`。两节点 `compileJava` 绿 + forge `runClient` 全 **12 geo** 零 `GeckoLibException`、抵并保持标题屏。（**注：dragon 的 geo 方案后被证塌平、改 Java 模型，见上一条 CR-49**。）dragon 静态 rest-pose（程序化 neck/tail）+ 动画 + 精确视觉待人工目视。
- 2026-07-22（PE-4b）：转换脚本跑 10 boss `ModelBase` → **9 GeckoLib 忠实网格**（chagaroth + jzahar + shadowboss + 6 elite）+ `EliteMob` 加 `GeoEntity`（`.core` `//?` 例外，同 BossMob）+ 4 类 `Boss/EliteGeo{Model,Renderer}`（按 `getType()` id 解析）；dreadguard = vanilla `HumanoidModel`。**2 EnderDragon 式 dragon 回退 stand-in**（`setTextureOffset`+具名 addBox 转换脚本不兼容 → NaN→JsonNull→GeckoLib 资源重载崩，forge runClient 实证捕获，见 §6）。两节点 compile 绿 + forge runClient 全 9 geo 零 `GeckoLibException`。视觉 + 2 dragon 网格 + 动画待人工目视/后续。
- 2026-07-22：初版。承 Betweenlands GeckoLib 方块参考改写为 AC 通用（方块+实体、侧重实体）版；落地 GeckoLib 双加载器依赖（含 mclib `forgeRuntimeLibrary` 修复）、`.core` 包分裂分叉、转换脚本 `setRotation`+mirror 扩展、Chagaroth 三件套 + PE-1 接入；forge 目视验证 Chagaroth 忠实（静态）。
