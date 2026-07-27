package com.shinoow.abyssalcraft.system.spell;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Base of a Necronomicon spell (owned by PS-7), faithful to the 1.12.2 {@code api.spell.Spell}:
 * an inscribable/castable action with a book-tier gate, a required Potential Energy amount (drawn from
 * an energy-holding item, PS-5), a set of inscription reagents, a required scroll quality and an optional
 * parent spell. Concrete subtypes decide the effect in {@link #castSpellServer}/{@link #castSpellClient}.
 *
 * <p>The knowledge gate (1.12.2 {@code IResearchable}) is decoupled here to an optional research id, so
 * PS-7 does not depend on the parallel knowledge subsystem (PS-8); PS-8 reads {@link #researchId()} to gate
 * the spell. The required energy is checked against the caster's energy item (PS-5) by the deferred cast glue.
 */
public abstract class Spell {

    private final String id;
    private final int bookType;
    private final float requiredEnergy;
    private final List<SpellIngredient> reagentLayout;
    private int color;
    private boolean nbtSensitive;
    private boolean requiresCharging;
    private boolean canBeCastByOthers;
    private Spell parent;
    private ResourceLocation glyph;
    private ResourceLocation researchId;
    private ScrollType scrollType = ScrollType.BASIC;

    protected Spell(String id, int bookType, float requiredEnergy, ItemStack... reagents) {
        this(id, bookType, requiredEnergy, java.util.Arrays.stream(reagents)
            .map(stack -> SpellIngredient.item(net.minecraft.core.registries.BuiltInRegistries.ITEM
                .getKey(stack.getItem()).toString(), stack.getCount()))
            .toList());
    }

    protected Spell(String id, int bookType, float requiredEnergy, List<SpellIngredient> reagents) {
        this.id = id;
        this.bookType = bookType;
        this.requiredEnergy = requiredEnergy;
        this.reagentLayout = List.copyOf(reagents);
    }

    protected Spell(String id, float requiredEnergy, ItemStack... reagents) {
        this(id, 0, requiredEnergy, reagents);
    }

    public Spell setColor(int color) {
        this.color = color;
        return this;
    }

    /** Require identical item NBT/components on the reagents (a stricter match; honoured by content). */
    public Spell setNBTSensitive() {
        this.nbtSensitive = true;
        return this;
    }

    /** Require the spell to charge (hold) before casting rather than a single click. */
    public Spell setRequiresCharging() {
        this.requiresCharging = true;
        return this;
    }

    /** Require {@code parent} to be inscribed first (tiered / evolved spells). */
    public Spell setParent(Spell parent) {
        this.parent = parent;
        return this;
    }

    public Spell setGlyph(ResourceLocation glyph) {
        this.glyph = glyph;
        return this;
    }

    public Spell setScrollType(ScrollType scrollType) {
        this.scrollType = scrollType;
        return this;
    }

    /** Whether non-players (Remnants, MoTGK, J'zahar) may cast this spell. */
    public Spell setCanBeCastByOthers(boolean canBeCastByOthers) {
        this.canBeCastByOthers = canBeCastByOthers;
        return this;
    }

    /** Gate this spell behind a research id (PS-8). */
    public Spell setResearch(ResourceLocation researchId) {
        this.researchId = researchId;
        return this;
    }

    public String id() {
        return id;
    }

    public int bookType() {
        return bookType;
    }

    public float requiredEnergy() {
        return requiredEnergy;
    }

    /** The reagents consumed to inscribe this spell. */
    public List<ItemStack> reagents() {
        return reagentLayout.stream().filter(reagent -> !reagent.isEmpty())
            .map(SpellIngredient::example).toList();
    }

    public List<SpellIngredient> reagentLayout() {
        return reagentLayout;
    }

    public int color() {
        return color;
    }

    public boolean isNBTSensitive() {
        return nbtSensitive;
    }

    public boolean requiresCharging() {
        return requiresCharging;
    }

    public boolean canOthersCast() {
        return canBeCastByOthers;
    }

    public Spell parent() {
        return parent;
    }

    public ResourceLocation glyph() {
        return glyph;
    }

    /** Optional knowledge/research gate id (read by PS-8), or {@code null}. */
    public ResourceLocation researchId() {
        return researchId;
    }

    public ScrollType scrollType() {
        return scrollType;
    }

    public String translationKey() {
        return "ac.spell." + id;
    }

    /** Whether {@code provided} reagents satisfy this spell's reagents (item match, order-free). */
    public boolean matches(List<ItemStack> provided) {
        List<SpellIngredient> reagents = reagentLayout.stream().filter(reagent -> !reagent.isEmpty()).toList();
        if (provided.size() != reagents.size()) {
            return false;
        }
        List<ItemStack> remaining = new ArrayList<>(provided);
        for (SpellIngredient needed : reagents) {
            boolean found = false;
            for (int i = 0; i < remaining.size(); i++) {
                if (needed.matches(remaining.get(i))) {
                    remaining.remove(i);
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    /** Override to gate whether the spell may be cast in the current context. */
    public abstract boolean canCastSpell(Level level, BlockPos pos, Player player, ScrollType scrollType);

    /** Dispatches to {@link #castSpellServer}/{@link #castSpellClient} by side. */
    public void castSpell(Level level, BlockPos pos, Player player, ScrollType scrollType) {
        if (!level.isClientSide) {
            castSpellServer(level, pos, player, scrollType);
        } else {
            castSpellClient(level, pos, player, scrollType);
        }
    }

    /** Override to do something server-side when cast. */
    protected abstract void castSpellServer(Level level, BlockPos pos, Player player, ScrollType scrollType);

    /** Override to do something client-side when cast. */
    protected abstract void castSpellClient(Level level, BlockPos pos, Player player, ScrollType scrollType);

    /** Override to let a non-player entity cast this spell. */
    public void castSpellOther(Level level, BlockPos pos, LivingEntity caster, ScrollType scrollType) {
    }
}
