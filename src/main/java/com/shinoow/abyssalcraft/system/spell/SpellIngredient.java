package com.shinoow.abyssalcraft.system.spell;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.ContainerCompat;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;

/** Lazily resolved inscription reagent used before deferred item registries freeze. */
public final class SpellIngredient {

    private static final SpellIngredient EMPTY = new SpellIngredient(List.of(), null, 0, false);

    private final List<ResourceLocation> itemIds;
    private final ResourceLocation tagId;
    private final int count;
    private final boolean strictData;

    private SpellIngredient(List<ResourceLocation> itemIds, ResourceLocation tagId,
                            int count, boolean strictData) {
        this.itemIds = List.copyOf(itemIds);
        this.tagId = tagId;
        this.count = count;
        this.strictData = strictData;
    }

    public static SpellIngredient empty() {
        return EMPTY;
    }

    public static SpellIngredient item(String id) {
        return item(id, 1);
    }

    public static SpellIngredient item(String id, int count) {
        return new SpellIngredient(List.of(ACRef.parse(id)), null, positive(count), false);
    }

    public static SpellIngredient tag(String id) {
        return new SpellIngredient(List.of(), ACRef.parse(id), 1, false);
    }

    public static SpellIngredient anyOf(String... ids) {
        List<ResourceLocation> parsed = new ArrayList<>(ids.length);
        for (String id : ids) parsed.add(ACRef.parse(id));
        return new SpellIngredient(parsed, null, 1, false);
    }

    public SpellIngredient strict() {
        return new SpellIngredient(itemIds, tagId, count, true);
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    public int count() {
        return count;
    }

    public boolean strictData() {
        return strictData;
    }

    public Set<ResourceLocation> referencedItems() {
        return Set.copyOf(new LinkedHashSet<>(itemIds));
    }

    public ResourceLocation tagId() {
        return tagId;
    }

    public boolean matches(ItemStack provided) {
        if (isEmpty()) return provided.isEmpty();
        if (provided.isEmpty() || provided.getCount() < count) return false;
        if (tagId != null) return provided.is(TagKey.create(Registries.ITEM, tagId));
        ResourceLocation providedId = BuiltInRegistries.ITEM.getKey(provided.getItem());
        for (ResourceLocation id : itemIds) {
            if (id.equals(providedId)) {
                if (!strictData) return true;
                ItemStack required = new ItemStack(BuiltInRegistries.ITEM.get(id), count);
                return ContainerCompat.canStack(provided.copyWithCount(1), required.copyWithCount(1));
            }
        }
        return false;
    }

    public ItemStack example() {
        if (tagId != null) {
            return BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, tagId))
                .flatMap(tag -> tag.stream().findFirst())
                .map(holder -> new ItemStack(holder.value(), count))
                .orElse(ItemStack.EMPTY);
        }
        if (itemIds.isEmpty() || !BuiltInRegistries.ITEM.containsKey(itemIds.get(0))) return ItemStack.EMPTY;
        return new ItemStack(BuiltInRegistries.ITEM.get(itemIds.get(0)), count);
    }

    private static int positive(int count) {
        if (count < 1) throw new IllegalArgumentException("Spell reagent count must be positive");
        return count;
    }
}