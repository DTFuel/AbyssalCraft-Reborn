package com.shinoow.abyssalcraft.system.knowledge;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.EnchantmentCompat;
import com.shinoow.abyssalcraft.system.enchant.ACEnchantments;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Resolves source-derived legacy page item expressions to canonical modern registry items. */
public final class NecronomiconItemVisuals {

    private static final Pattern STACK = Pattern.compile(
        "new ItemStack\\((ACBlocks|ACItems|Blocks)\\.([A-Za-z0-9_]+)\\)");
    private static final Map<String, String> RENAMES = Map.ofEntries(
        Map.entry("crystallizer_idle", "crystallizer"),
        Map.entry("liquid_antimatter", "liquid_antimatter_bucket"),
        Map.entry("liquid_coralium", "liquid_coralium_bucket"),
        Map.entry("transmutator_idle", "transmutator"),
        Map.entry("ritual_altar_stone", "ritual_altar"),
        Map.entry("ritual_pedestal_stone", "ritual_pedestal")
    );

    private NecronomiconItemVisuals() {}

    public static Resolution resolve(net.minecraft.core.HolderLookup.Provider registries,
            LegacyNecronomiconPageManifest.LegacyPage page) {
        if (!"ITEM".equals(page.visualKind())) return Resolution.notApplicable();
        ItemStack enchantedBook = enchantedBook(registries, page.legacyId());
        if (!enchantedBook.isEmpty()) {
            return new Resolution(Optional.of(enchantedBook), "minecraft:enchanted_book",
                NecronomiconPageManifest.OwnerStatus.ACTIVE, "necronomicon-item-renderer", "");
        }
        Matcher matcher = STACK.matcher(page.visualReference());
        if (!matcher.matches()) {
            return Resolution.blocked(page.visualReference(),
                "RR-CONTENT must port the legacy typed item expression before it can be rendered");
        }
        String namespace = matcher.group(1).equals("Blocks") ? "minecraft" : "abyssalcraft";
        String path = RENAMES.getOrDefault(matcher.group(2), matcher.group(2));
        ResourceLocation id = ACRef.of(namespace, path);
        if (!BuiltInRegistries.ITEM.containsKey(id)) {
            return Resolution.blocked(id.toString(),
                "RR-CONTENT must register the canonical item or provide an explicit rename mapping");
        }
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(id));
        if (stack.isEmpty()) {
            return Resolution.blocked(id.toString(),
                "RR-CONTENT registry entry does not produce a renderable ItemStack");
        }
        return new Resolution(Optional.of(stack), id.toString(), NecronomiconPageManifest.OwnerStatus.ACTIVE,
            "necronomicon-item-renderer", "");
    }

    private static ItemStack enchantedBook(net.minecraft.core.HolderLookup.Provider registries, String legacyId) {
        return switch (legacyId) {
            case "ENCHANTMENT_LIGHT_PIERCE" -> EnchantmentCompat.enchantedBook(registries, ACEnchantments.LIGHT_PIERCE, 5);
            case "ENCHANTMENT_IRON_WALL" -> EnchantmentCompat.enchantedBook(registries, ACEnchantments.IRON_WALL, 1);
            case "ENCHANTMENT_SAPPING" -> EnchantmentCompat.enchantedBook(registries, ACEnchantments.SAPPING, 3);
            case "ENCHANTMENT_MULTI_REND" -> EnchantmentCompat.enchantedBook(registries, ACEnchantments.MULTI_REND, 1);
            case "ENCHANTMENT_BLINDING_LIGHT" -> EnchantmentCompat.enchantedBook(registries, ACEnchantments.BLINDING_LIGHT, 1);
            default -> ItemStack.EMPTY;
        };
    }

    public record Resolution(Optional<ItemStack> stack, String reference,
                             NecronomiconPageManifest.OwnerStatus status, String owner, String reason) {
        private static Resolution notApplicable() {
            return new Resolution(Optional.empty(), "not-applicable",
                NecronomiconPageManifest.OwnerStatus.BLOCKED, "RR-CONTENT", "not an item visual");
        }

        private static Resolution blocked(String reference, String reason) {
            return new Resolution(Optional.empty(), reference,
                NecronomiconPageManifest.OwnerStatus.BLOCKED, "RR-CONTENT", reason);
        }
    }
}