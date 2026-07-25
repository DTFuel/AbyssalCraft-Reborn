package com.shinoow.abyssalcraft.content.entity.projectile;

import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.content.entity.shoggoth.AbstractShoggoth;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.ACThrowableProjectile;
import com.shinoow.abyssalcraft.platform.ArmorDurabilityCompat;
import com.shinoow.abyssalcraft.registry.BaseBlocks;
import com.shinoow.abyssalcraft.system.effect.ACDamageTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

/**
 * Acid projectile (1.12.2 {@code acidprojectile}), spat by shoggoths. Faithful vanilla flight via
 * {@link ThrowableProjectile}; restores armor-piercing acid damage, armor corrosion and block melting.
 */
public class AcidProjectile extends ACThrowableProjectile {

    public AcidProjectile(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (level().isClientSide || !(result.getEntity() instanceof LivingEntity living)
                || living instanceof AbstractShoggoth) return;
        var source = ACDamageTypes.projectile(this, ACDamageTypes.ACID);
        if (ACConfig.shieldsBlockAcid.get() && living.isBlocking() && living.isDamageSourceBlocked(source)) {
            ArmorDurabilityCompat.damageHeld(living.getUseItem(), 12, living, living.getUsedItemHand());
            return;
        }
        int damage = getOwner() instanceof LivingEntity owner && owner.isBaby() ? 3 : 6;
        if (living.hurt(source, damage)) corrodeArmor(living, damage == 3 ? 4 : 8);
        if (ACConfig.hardcoreMode.get() && living instanceof Player) {
            living.hurt(ACDamageTypes.projectile(this, ACDamageTypes.ACID), 1.0F);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        melt(result.getBlockPos());
    }

    private void corrodeArmor(LivingEntity target, int amount) {
        for (EquipmentSlot slot : new EquipmentSlot[] {
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack armor = target.getItemBySlot(slot);
            String id = BuiltInRegistries.ITEM.getKey(armor.getItem()).getPath();
            if (!id.startsWith("ethaxium_")) ArmorDurabilityCompat.damage(armor, amount, target, slot);
        }
    }

    private void melt(BlockPos pos) {
        if (level().isClientSide || ACConfig.no_acid_breaking_blocks.get()
                || !level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) return;
        BlockState state = level().getBlockState(pos);
        float hardness = state.getDestroySpeed(level(), pos);
        if (state.isAir() || state.hasBlockEntity() || hardness < 0.0F
                || hardness >= ACConfig.acidResistanceHardness.get()) return;
        if (state.is(BaseBlocks.MONOLITH_STONE.get())
                || BuiltInRegistries.BLOCK.getKey(state.getBlock()).equals(ACRef.id("shoggoth_ooze"))
                || BuiltInRegistries.BLOCK.getKey(state.getBlock()).equals(ACRef.id("shoggoth_biomass"))) return;
        level().destroyBlock(pos, false, getOwner(), 512);
    }
}
