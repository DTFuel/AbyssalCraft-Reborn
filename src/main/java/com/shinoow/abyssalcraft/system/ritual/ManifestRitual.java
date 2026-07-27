package com.shinoow.abyssalcraft.system.ritual;

import com.shinoow.abyssalcraft.platform.ItemDataCompat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Runtime ritual backed directly by one immutable {@link RitualManifest}. */
public final class ManifestRitual extends Ritual {

    private final RitualManifest manifest;

    public ManifestRitual(RitualManifest manifest) {
        super(manifest.id(), manifest.bookType(), manifest.dimension(), manifest.requiredEnergy(),
            manifest.requiresSacrifice(), manifest.center(),
            manifest.offeringLayout().toArray(RitualIngredient[]::new));
        this.manifest = manifest;
        if (manifest.research() != null) setResearch(manifest.research());
    }

    public RitualManifest manifest() {
        return manifest;
    }

    @Override
    public boolean canStart(Level level, BlockPos altar, Player player) {
        boolean generic = switch (manifest.kind()) {
            case INFUSION, CREATION, TRANSFORMATION -> true;
            default -> false;
        };
        if (generic) return true;
        if (!(level.getBlockEntity(altar) instanceof RitualHost host)) return false;
        RitualBehavior behavior = RitualBehaviorRegistry.instance().get(manifest.id());
        return behavior != null && behavior.canStart(this, level, altar, player, host);
    }

    @Override
    public void complete(Level level, BlockPos altar, Player player) {
        if (level.isClientSide || !(level.getBlockEntity(altar) instanceof RitualHost host)) return;
        switch (manifest.kind()) {
            case INFUSION, CREATION -> host.setRitualCenter(result(host.ritualCenter()));
            case TRANSFORMATION -> host.fillRitualPedestals(result(ItemStack.EMPTY));
            default -> {
                RitualBehavior behavior = RitualBehaviorRegistry.instance().get(manifest.id());
                if (behavior == null) throw new IllegalStateException("Missing specialized ritual behavior: " + manifest.id());
                behavior.complete(this, level, altar, player, host);
            }
        }
    }

    private ItemStack result(ItemStack center) {
        if (manifest.result() == null || !BuiltInRegistries.ITEM.containsKey(manifest.result())) {
            throw new IllegalStateException("Missing ritual result item: " + manifest.id());
        }
        ItemStack result = new ItemStack(BuiltInRegistries.ITEM.get(manifest.result()));
        if (!manifest.copyCenterData() || center.isEmpty()) return result;

        CompoundTag source = ItemDataCompat.copyData(center);
        CompoundTag target = ItemDataCompat.copyData(result);
        for (String key : manifest.copiedDataKeys()) {
            Tag value = source.get(key);
            if (value != null) target.put(key, value.copy());
        }
        ItemDataCompat.setData(result, target);
        return result;
    }
}