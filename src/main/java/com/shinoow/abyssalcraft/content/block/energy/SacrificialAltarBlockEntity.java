package com.shinoow.abyssalcraft.content.block.energy;

import java.util.ArrayList;
import java.util.List;

import com.shinoow.abyssalcraft.content.blockentity.base.TickingBlockEntity;
import com.shinoow.abyssalcraft.system.enchant.EnchantmentEffects;
import com.shinoow.abyssalcraft.system.energy.EnergyTier;
import com.shinoow.abyssalcraft.system.energy.IEnergyCollector;
import com.shinoow.abyssalcraft.system.energy.IEnergyContainerItem;
import com.shinoow.abyssalcraft.system.energy.PEUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
//? if <1.21 {
import net.minecraft.world.entity.MobType;
//?} else {
/*import net.minecraft.tags.EntityTypeTags;
*///?}
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** Persistent life-force collection and one-slot PE output for every Sacrificial Altar tier. */
public final class SacrificialAltarBlockEntity extends InventoryEnergyBlockEntity
    implements IEnergyCollector, TickingBlockEntity {

    private static final float ITEM_TRANSFER_QUANTA = 20.0F;
    private final List<Mob> targets = new ArrayList<>();
    private int collectionLimit;
    private int cooldown;

    public SacrificialAltarBlockEntity(BlockPos pos, BlockState state) {
        super(EnergyBlocks.SACRIFICIAL_ALTAR_BE.get(), pos, state, 1, capacity(state));
    }

    @Override
    public void serverTick() {
        if (!(level instanceof ServerLevel server)) return;
        if (cooldown > 0) {
            cooldown--;
            setChanged();
        }
        if (PEUtils.transferToItem(this, getStoredItem(), ITEM_TRANSFER_QUANTA) > 0) setChanged();
        collectDeadTargets();
        refreshTargets(server);
        if (collectionLimit >= getMaxEnergy() / 5) {
            collectionLimit = 0;
            cooldown = cooldownTicks(tier());
            setChanged();
        }
    }

    private void refreshTargets(ServerLevel level) {
        targets.removeIf(mob -> mob.isRemoved() && mob.isAlive());
        if (targets.size() >= maxTargets(tier())) return;
        for (Mob mob : level.getEntitiesOfClass(Mob.class, new AABB(worldPosition).inflate(8.0D, 3.0D, 8.0D))) {
            if (targets.size() >= maxTargets(tier())) break;
            if (validTarget(mob) && !targets.contains(mob)) targets.add(mob);
        }
    }

    private void collectDeadTargets() {
        for (int index = targets.size() - 1; index >= 0; index--) {
            Mob mob = targets.get(index);
            if (getContainedEnergy() < getMaxEnergy()) {
                mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20, 0, false, false));
            }
            if (!mob.isAlive()) {
                if (mob.getLastDamageSource() != null && cooldown == 0 && getContainedEnergy() < getMaxEnergy()) {
                    float collected = Math.min(mob.getMaxHealth(), getMaxEnergy() - getContainedEnergy());
                    addEnergy(collected);
                    collectionLimit += (int) mob.getMaxHealth();
                }
                targets.remove(index);
            }
        }
    }

    static boolean validTarget(Mob mob) {
        if (!mob.isAlive() || mob.isBaby() || EnchantmentEffects.isShadow(mob)) return false;
        //? if <1.21 {
        return mob.getMobType() != MobType.UNDEAD;
        //?} else {
        /*return !mob.getType().is(EntityTypeTags.UNDEAD);
        *///?}
    }

    public static int capacity(EnergyTier tier) {
        return 5000 + 2500 * tier.ordinal();
    }

    public static int maxTargets(EnergyTier tier) {
        return tier.ordinal() + 1;
    }

    public static int cooldownTicks(EnergyTier tier) {
        return 1200 - 200 * tier.ordinal();
    }

    private EnergyTier tier() {
        return getBlockState().getBlock() instanceof SacrificialAltarBlock altar
            ? altar.tier()
            : EnergyTier.BASIC;
    }

    private static int capacity(BlockState state) {
        return capacity(state.getBlock() instanceof SacrificialAltarBlock altar
            ? altar.tier()
            : EnergyTier.BASIC);
    }

    @Override
    public boolean canAcceptPE() {
        return false;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.getItem() instanceof IEnergyContainerItem;
    }

    @Override
    protected void saveEnergyData(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("CollectionLimit", collectionLimit);
        tag.putInt("CoolDown", cooldown);
    }

    @Override
    protected void loadEnergyData(CompoundTag tag, HolderLookup.Provider registries) {
        collectionLimit = Math.max(0, tag.getInt("CollectionLimit"));
        cooldown = Math.max(0, tag.getInt("CoolDown"));
    }
}
