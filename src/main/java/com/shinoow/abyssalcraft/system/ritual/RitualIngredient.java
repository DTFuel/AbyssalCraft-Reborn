package com.shinoow.abyssalcraft.system.ritual;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.ContainerCompat;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * A lazily resolved ritual ingredient. Ritual content is bootstrapped before deferred registries freeze, so
 * AC items are represented by registry ids and resolved only when a ritual is matched or displayed.
 */
public final class RitualIngredient {

    private static final RitualIngredient EMPTY = new RitualIngredient(List.of(), null, 0, false, null);
    private static final RitualIngredient ANY_ITEM = new RitualIngredient(List.of(), null, 1, false, null);

    private final List<ResourceLocation> itemIds;
    private final ResourceLocation tagId;
    private final int count;
    private final boolean strictData;
    private final Supplier<List<ItemStack>> stackSupplier;

    private RitualIngredient(List<ResourceLocation> itemIds, ResourceLocation tagId, int count,
                             boolean strictData, Supplier<List<ItemStack>> stackSupplier) {
        this.itemIds = List.copyOf(itemIds);
        this.tagId = tagId;
        this.count = count;
        this.strictData = strictData;
        this.stackSupplier = stackSupplier;
    }

    public static RitualIngredient empty() {
        return EMPTY;
    }

    /** Match any non-empty stack; special ritual handlers may apply a stricter semantic predicate. */
    public static RitualIngredient anyItem() {
        return ANY_ITEM;
    }

    public static RitualIngredient item(String id) {
        return item(ACRef.parse(id), 1);
    }

    public static RitualIngredient item(ResourceLocation id) {
        return item(id, 1);
    }

    public static RitualIngredient item(String id, int count) {
        return item(ACRef.parse(id), count);
    }

    public static RitualIngredient item(ResourceLocation id, int count) {
        return new RitualIngredient(List.of(id), null, positive(count), false, null);
    }

    public static RitualIngredient anyOf(String... ids) {
        return anyOf(1, ids);
    }

    public static RitualIngredient anyOf(int count, String... ids) {
        List<ResourceLocation> parsed = new ArrayList<>(ids.length);
        for (String id : ids) {
            parsed.add(ACRef.parse(id));
        }
        return new RitualIngredient(parsed, null, positive(count), false, null);
    }

    public static RitualIngredient tag(String tagId) {
        return tag(tagId, 1);
    }

    public static RitualIngredient tag(String tagId, int count) {
        return new RitualIngredient(List.of(), ACRef.parse(tagId), positive(count), false, null);
    }

    public static RitualIngredient stack(ItemStack stack, boolean strictData) {
        ItemStack template = stack.copy();
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(template.getItem());
        return stack(id, template.getCount(), strictData, () -> template.copy());
    }

    public static RitualIngredient stack(String id, int count, boolean strictData, Supplier<ItemStack> stack) {
        return stack(ACRef.parse(id), count, strictData, stack);
    }

    public static RitualIngredient stack(ResourceLocation id, int count, boolean strictData,
                                          Supplier<ItemStack> stack) {
        return new RitualIngredient(List.of(id), null, positive(count), strictData,
            () -> List.of(stack.get().copyWithCount(positive(count))));
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    public boolean isAnyItem() {
        return this == ANY_ITEM;
    }

    public int count() {
        return count;
    }

    public boolean strictData() {
        return strictData;
    }

    public List<ResourceLocation> itemIds() {
        return itemIds;
    }

    public ResourceLocation tagId() {
        return tagId;
    }

    public Set<ResourceLocation> referencedItems() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(itemIds));
    }

    public boolean isResolvable() {
        if (isEmpty() || isAnyItem() || tagId != null) {
            return true;
        }
        for (ResourceLocation id : itemIds) {
            if (!BuiltInRegistries.ITEM.containsKey(id)) {
                return false;
            }
        }
        return true;
    }

    public boolean matches(ItemStack provided) {
        if (isEmpty()) {
            return provided.isEmpty();
        }
        if (isAnyItem()) {
            return !provided.isEmpty();
        }
        if (provided.isEmpty() || provided.getCount() < count) {
            return false;
        }
        if (tagId != null) {
            return provided.is(TagKey.create(Registries.ITEM, tagId));
        }
        for (ItemStack required : alternatives()) {
            if (ItemStack.isSameItem(provided, required) && (!strictData || sameData(provided, required))) {
                return true;
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
        List<ItemStack> alternatives = alternatives();
        return alternatives.isEmpty() ? ItemStack.EMPTY : alternatives.get(0).copy();
    }

    private List<ItemStack> alternatives() {
        if (stackSupplier != null) {
            List<ItemStack> supplied = stackSupplier.get();
            List<ItemStack> copies = new ArrayList<>(supplied.size());
            for (ItemStack stack : supplied) {
                if (!stack.isEmpty()) {
                    copies.add(stack.copyWithCount(count));
                }
            }
            return copies;
        }
        List<ItemStack> stacks = new ArrayList<>(itemIds.size());
        for (ResourceLocation id : itemIds) {
            if (BuiltInRegistries.ITEM.containsKey(id)) {
                Item item = BuiltInRegistries.ITEM.get(id);
                stacks.add(new ItemStack(item, count));
            }
        }
        return stacks;
    }

    private static boolean sameData(ItemStack provided, ItemStack required) {
        ItemStack normalizedProvided = provided.copyWithCount(1);
        ItemStack normalizedRequired = required.copyWithCount(1);
        return ContainerCompat.canStack(normalizedProvided, normalizedRequired);
    }

    private static int positive(int count) {
        if (count < 1) {
            throw new IllegalArgumentException("Ritual ingredient count must be positive");
        }
        return count;
    }
}
