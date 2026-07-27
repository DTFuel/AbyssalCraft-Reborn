package com.shinoow.abyssalcraft.system.ritual;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Base of a Necronomicon ritual (owned by PS-6), faithful to the 1.12.2 {@code api.ritual.NecronomiconRitual}:
 * a set of offerings placed on pedestals around an altar, a required Potential Energy amount, an optional
 * living sacrifice, and a book-tier / dimension gate. Concrete subtypes decide the product in
 * {@link #complete(Level, BlockPos, Player)}.
 *
 * <p>The knowledge gate (1.12.2 {@code IResearchable}) is decoupled here to an optional research id, so
 * PS-6 does not depend on the parallel knowledge subsystem (PS-8); PS-8 reads {@link #researchId()} to gate
 * the ritual. The required energy is checked against the altar's {@code IEnergyContainer} (PS-5).
 */
public abstract class Ritual {

    private final String name;
    private final int bookType;
    private final ResourceKey<Level> dimension; // null = any dimension
    private final float requiredEnergy;
    private final boolean requiresSacrifice;
    private final RitualIngredient center;
    private final List<RitualIngredient> offeringLayout;
    private final List<RitualIngredient> offerings;
    private ResourceLocation researchId;

    protected Ritual(String name, int bookType, ResourceKey<Level> dimension, float requiredEnergy,
                     boolean requiresSacrifice, ItemStack... offerings) {
        this(name, bookType, dimension, requiredEnergy, requiresSacrifice, RitualIngredient.empty(),
            Arrays.stream(offerings).map(stack -> RitualIngredient.stack(stack, false))
                .toArray(RitualIngredient[]::new));
    }

    protected Ritual(String name, int bookType, ResourceKey<Level> dimension, float requiredEnergy,
                     boolean requiresSacrifice, RitualIngredient center, RitualIngredient... offerings) {
        this.name = name;
        this.bookType = bookType;
        this.dimension = dimension;
        this.requiredEnergy = requiredEnergy;
        this.requiresSacrifice = requiresSacrifice;
        this.center = center;
        this.offeringLayout = List.of(offerings);
        this.offerings = this.offeringLayout.stream().filter(ingredient -> !ingredient.isEmpty()).toList();
    }

    /** Produce the ritual's result at the altar. Called once the offerings + energy + sacrifice are satisfied. */
    public abstract void complete(Level level, BlockPos altar, Player player);

    /** Server-side behavior-specific precondition checked before offerings are consumed. */
    public boolean canStart(Level level, BlockPos altar, Player player) {
        return true;
    }

    public String name() {
        return name;
    }

    public int bookType() {
        return bookType;
    }

    public ResourceKey<Level> dimension() {
        return dimension;
    }

    public float requiredEnergy() {
        return requiredEnergy;
    }

    public boolean requiresSacrifice() {
        return requiresSacrifice;
    }

    /** The non-empty offerings consumed by this ritual (matched as an order-free multiset). */
    public List<RitualIngredient> offerings() {
        return offerings;
    }

    /** The original eight-position visual layout, including empty slots used only by the book UI. */
    public List<RitualIngredient> offeringLayout() {
        return offeringLayout;
    }

    /** The item placed on the altar, or an empty ingredient when no center item is required. */
    public RitualIngredient center() {
        return center;
    }

    public boolean matchesCenter(ItemStack provided) {
        return center.isEmpty() ? provided.isEmpty() : center.matches(provided);
    }

    /** Optional knowledge/research gate id (read by PS-8), or {@code null}. */
    public ResourceLocation researchId() {
        return researchId;
    }

    /** Gate this ritual behind a research id (PS-8). */
    public Ritual setResearch(ResourceLocation researchId) {
        this.researchId = researchId;
        return this;
    }

    /** Whether {@code provided} (pedestal contents) satisfy this ritual's offerings (item match, order-free). */
    public boolean matches(List<ItemStack> provided) {
        if (provided.size() != offerings.size()) {
            return false;
        }
        List<ItemStack> remaining = new ArrayList<>();
        for (ItemStack stack : provided) {
            if (!stack.isEmpty()) {
                remaining.add(stack);
            }
        }
        if (remaining.size() != offerings.size()) {
            return false;
        }
        for (RitualIngredient needed : offerings) {
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
}
