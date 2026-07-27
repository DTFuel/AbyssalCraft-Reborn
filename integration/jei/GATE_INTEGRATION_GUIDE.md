## RR-JEI-AUTO Gate Integration Guide (TP.5b / T8.1b)

### 摘要

**Agent JEI** 已完成 RR-JEI-AUTO 的全部非用户实现。新增 6 个 JEI 分类（总计 10 个），涵盖两燃料分类、Rending、Creation/Transformation Ritual、精选 Spell 展示，以及永久审计系统。所有代码位于 `integration/jei/**`（除 `ACJEIPlugin.java` relay）。

### 需要 Gate Integrator 在 ACJEIPlugin 中添加的精确调用

#### 1. 新增 RecipeType 常量（在类顶部，现有 4 个之后）

```java
// Fuel categories (RR-JEI-AUTO)
private static final RecipeType<FuelRecipe> CRYSTALLIZER_FUEL_JEI =
    RecipeType.create(AbyssalCraft.MODID, "crystallizer_fuel", FuelRecipe.class);
private static final RecipeType<FuelRecipe> TRANSMUTATOR_FUEL_JEI =
    RecipeType.create(AbyssalCraft.MODID, "transmutator_fuel", FuelRecipe.class);

// Rending (RR-JEI-AUTO)
private static final RecipeType<RendingRecipe> RENDING_JEI =
    RecipeType.create(AbyssalCraft.MODID, "rending", RendingRecipe.class);

// Rituals (RR-JEI-AUTO)
private static final RecipeType<RitualManifest> CREATION_RITUAL_JEI =
    RecipeType.create(AbyssalCraft.MODID, "creation_ritual", RitualManifest.class);
private static final RecipeType<RitualManifest> TRANSFORMATION_RITUAL_JEI =
    RecipeType.create(AbyssalCraft.MODID, "transformation_ritual", RitualManifest.class);

// Spells (RR-JEI-AUTO)
private static final RecipeType<SpellManifest> SPELL_JEI =
    RecipeType.create(AbyssalCraft.MODID, "spell", SpellManifest.class);
```

#### 2. 在 registerCategories() 中添加（现有 4 个 addRecipeCategories 之后）

```java
// RR-JEI-AUTO: Fuel categories
reg.addRecipeCategories(new CrystallizerFuelCategory(gui, CRYSTALLIZER_FUEL_JEI));
reg.addRecipeCategories(new TransmutatorFuelCategory(gui, TRANSMUTATOR_FUEL_JEI));

// RR-JEI-AUTO: Rending
reg.addRecipeCategories(new RendingCategory(gui, RENDING_JEI));

// RR-JEI-AUTO: Rituals
reg.addRecipeCategories(new CreationRitualCategory(gui, CREATION_RITUAL_JEI));
reg.addRecipeCategories(new TransformationRitualCategory(gui, TRANSFORMATION_RITUAL_JEI));

// RR-JEI-AUTO: Spells
reg.addRecipeCategories(new SpellCategory(gui, SPELL_JEI));
```

#### 3. 在 registerRecipes() 中添加（现有 4 个 addRecipes 之后，level != null 块内）

```java
// RR-JEI-AUTO: Fuel recipes
reg.addRecipes(CRYSTALLIZER_FUEL_JEI, CrystallizerFuelCategory.getAllFuels());
reg.addRecipes(TRANSMUTATOR_FUEL_JEI, TransmutatorFuelCategory.getAllFuels());

// RR-JEI-AUTO: Rending recipes
reg.addRecipes(RENDING_JEI, DataRecipeCompat.allOfType(level, ModRecipes.RENDING.get()));

// RR-JEI-AUTO: Ritual recipes
reg.addRecipes(CREATION_RITUAL_JEI, CreationRitualCategory.getCreationRituals());
reg.addRecipes(TRANSFORMATION_RITUAL_JEI, TransformationRitualCategory.getTransformationRituals());

// RR-JEI-AUTO: Spell recipes
reg.addRecipes(SPELL_JEI, SpellCategory.getValuableSpells());
```

#### 4. 在 registerRecipeCatalysts() 中添加（现有 4 个 addRecipeCatalyst 之后）

```java
// RR-JEI-AUTO: Fuel catalysts (same as machine catalysts)
reg.addRecipeCatalyst(new ItemStack(Crystallizers.CRYSTALLIZER.get()), CRYSTALLIZER_FUEL_JEI);
reg.addRecipeCatalyst(new ItemStack(Transmutators.TRANSMUTATOR.get()), TRANSMUTATOR_FUEL_JEI);

// RR-JEI-AUTO: Rending catalyst (staff of rending - base tier)
// Note: Rending Pedestal is not yet implemented (归 RR-CLIENT-GUI-AUTO)
// Using staff as catalyst for now
try {
    ItemStack staffOfRending = new ItemStack(
        net.minecraft.core.registries.BuiltInRegistries.ITEM.get(ACRef.id("staff_of_rending")));
    if (!staffOfRending.isEmpty()) {
        reg.addRecipeCatalyst(staffOfRending, RENDING_JEI);
    }
} catch (Exception e) {
    // Staff of rending not yet registered, skip catalyst
}

// RR-JEI-AUTO: Ritual catalysts (ritual altar)
try {
    ItemStack ritualAltar = new ItemStack(
        net.minecraft.core.registries.BuiltInRegistries.ITEM.get(ACRef.id("ritual_altar")));
    if (!ritualAltar.isEmpty()) {
        reg.addRecipeCatalyst(ritualAltar, CREATION_RITUAL_JEI);
        reg.addRecipeCatalyst(ritualAltar, TRANSFORMATION_RITUAL_JEI);
    }
} catch (Exception e) {
    // Ritual altar not yet registered, skip catalyst
}

// RR-JEI-AUTO: Spell catalyst (necronomicon)
try {
    ItemStack necronomicon = new ItemStack(
        net.minecraft.core.registries.BuiltInRegistries.ITEM.get(ACRef.id("necronomicon")));
    if (!necronomicon.isEmpty()) {
        reg.addRecipeCatalyst(necronomicon, SPELL_JEI);
    }
} catch (Exception e) {
    // Necronomicon not yet registered, skip catalyst
}
```

#### 5. 需要添加的 imports

```java
import com.shinoow.abyssalcraft.system.ritual.RitualManifest;
import com.shinoow.abyssalcraft.system.spell.SpellManifest;
```

### 需要添加的语言键（不在本任务范围，但需记录）

Gate Integrator 或后续任务需在 `en_us.json` 添加以下键：

```json
"jei.abyssalcraft.crystallizer_fuel": "Crystallizer Fuel",
"jei.abyssalcraft.transmutator_fuel": "Transmutator Fuel",
"jei.abyssalcraft.rending": "Rending",
"jei.abyssalcraft.rending_energy": "Energy: %s PE",
"jei.abyssalcraft.essence_type": "Essence: %s",
"jei.abyssalcraft.creation_ritual": "Creation Ritual",
"jei.abyssalcraft.transformation_ritual": "Transformation Ritual",
"jei.abyssalcraft.ritual_energy": "PE: %s",
"jei.abyssalcraft.ritual_book_type": "Book Tier: %s",
"jei.abyssalcraft.ritual_dimension": "Dimension: %s",
"jei.abyssalcraft.spell": "Spell Inscription",
"jei.abyssalcraft.spell_energy": "PE Cost: %s",
"jei.abyssalcraft.spell_target.entity": "Target: Entity",
"jei.abyssalcraft.spell_target.entity_or_self": "Target: Entity or Self",
"jei.abyssalcraft.spell_target.block": "Target: Block",
"jei.abyssalcraft.spell_target.self": "Target: Self",
"jei.abyssalcraft.scroll_type": "Scroll: %s",
"jei.abyssalcraft.fuel_time": "Burn Time: %ss"
```

### 调用审计系统（可选，用于验证）

在 ACJEIPlugin 或测试代码中可调用：

```java
// 全量审计
JEIAuditGate.AuditResult audit = JEIAuditGate.runFullAudit();
System.out.println(audit);  // 输出分类数量、UID、配方计数、错误列表

// 分类状态闭包
JEIAuditGate.CategoryStatusClosure closure = JEIAuditGate.checkLegacyCategories();
System.out.println(closure);  // 输出每个分类的状态（NEW/RETAINED/DEFERRED等）
```

### 验证建议

1. **编译验证**：两节点 `compileJava` 应无错误
2. **JEI 面板验证**（需真人客户端）：
   - 打开 JEI 面板，应看到 10 个 AbyssalCraft 分类
   - 点击各分类图标，应显示对应配方/燃料/ritual/spell
   - 点击催化剂物品，应跳转到对应分类
3. **审计验证**（可在代码中调用）：
   - `JEIAuditGate.runFullAudit().passed()` 应返回 `true`
   - 所有分类计数应匹配预期（如 3 creation rituals、1 transformation ritual、8 valuable spells 等）

### 剩余仅 U-JEI 项（非本任务）

- **Infusion rituals（40 个）**：数量过多，已标记 `DEFERRED_TOO_MANY`，不展示
- **其余 6 个 spell**：价值较低（compass、floating、entropy 等），未选入精选列表
- **Rending Pedestal 机器宿主**：归 RR-CLIENT-GUI-AUTO，当前使用 staff of rending 作为临时催化剂
- **语言键本地化**：需在 `en_us.json` 添加上述键（不在 Agent JEI 任务范围）
