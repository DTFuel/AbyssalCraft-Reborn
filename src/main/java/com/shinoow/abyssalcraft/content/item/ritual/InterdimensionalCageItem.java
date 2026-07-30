package com.shinoow.abyssalcraft.content.item.ritual;

import java.util.List;

import com.shinoow.abyssalcraft.net.ACNetwork;
import com.shinoow.abyssalcraft.net.server.InterdimensionalCageMessage;
import com.shinoow.abyssalcraft.platform.ItemDataCompat;
import com.shinoow.abyssalcraft.system.spell.SpellUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/** PE-powered cage that stores one non-boss living entity as item custom data. */
public final class InterdimensionalCageItem extends RitualEnergyItem {

    public static final String ENTITY_KEY = "Entity";
    public static final String ENTITY_NAME_KEY = "EntityName";

    public InterdimensionalCageItem() {
        super(1000);
    }

    public static boolean hasCapturedEntity(ItemStack stack) {
        return ItemDataCompat.copyData(stack).contains(ENTITY_KEY, CompoundTag.TAG_COMPOUND);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        CompoundTag root = ItemDataCompat.copyData(stack);
        if (hasCapturedEntity(stack)) {
            if (level instanceof ServerLevel server) release(server, player, stack, root);
        } else if (level.isClientSide) {
            LivingEntity target = SpellUtils.rayTraceTarget(player, 3.0F);
            if (target != null) ACNetwork.sendToServer(new InterdimensionalCageMessage(target.getId(), hand));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    private static void release(ServerLevel level, Player player, ItemStack stack, CompoundTag root) {
        HitResult hit = player.pick(5.0D, 0.0F, true);
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK) return;
        BlockPos pos = blockHit.getBlockPos().relative(blockHit.getDirection());
        if (!player.mayUseItemAt(pos, blockHit.getDirection(), stack)) return;
        CompoundTag entityTag = root.getCompound(ENTITY_KEY).copy();
        Entity entity = EntityType.create(entityTag, level).orElse(null);
        if (entity == null) return;
        entity.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D,
            level.random.nextFloat() * 360.0F, 0.0F);
        if (level.addFreshEntity(entity)) {
            root.remove(ENTITY_KEY);
            root.remove(ENTITY_NAME_KEY);
            ItemDataCompat.setData(stack, root);
        }
    }

    @Override
    protected void appendTooltip(ItemStack stack, List<Component> tooltip, TooltipFlag flag) {
        String name = ItemDataCompat.getString(stack, ENTITY_NAME_KEY);
        if (!name.isEmpty()) tooltip.add(Component.literal(name).withStyle(ChatFormatting.GOLD));
        super.appendTooltip(stack, tooltip, flag);
    }

    public static float energyCost(LivingEntity target) {
        return energyCost(target.getBbWidth(), target.getBbHeight());
    }

    public static float energyCost(float width, float height) {
        return height * width * width * 100.0F;
    }
}