package com.shinoow.abyssalcraft.content.item.misc;

import com.shinoow.abyssalcraft.content.entity.misc.MiscEntities;
import com.shinoow.abyssalcraft.content.entity.misc.PSDLTracker;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.world.ACDimensions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;

/** Eye-like locator for the nearest Abyssal Wasteland stronghold. */
public final class PowerstoneTrackerItem extends Item {

    private static final TagKey<Structure> TARGETS =
        TagKey.create(Registries.STRUCTURE, ACRef.id("abyssal_stronghold"));

    public PowerstoneTrackerItem() {
        super(new Item.Properties());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.dimension() != ACDimensions.ABYSSAL_WASTELAND) {
            return InteractionResultHolder.fail(stack);
        }
        if (!(level instanceof ServerLevel server)) {
            return InteractionResultHolder.success(stack);
        }

        BlockPos target = server.findNearestMapStructure(TARGETS, player.blockPosition(), 100, false);
        if (target == null) return InteractionResultHolder.fail(stack);

        PSDLTracker tracker = MiscEntities.POWERSTONE_TRACKER.get().create(server);
        if (tracker == null) return InteractionResultHolder.fail(stack);
        tracker.setPos(player.getX(), player.getY(0.5D), player.getZ());
        tracker.moveTowards(target);
        server.addFreshEntity(tracker);
        server.playSound(null, player.blockPosition(), SoundEvents.ENDER_EYE_LAUNCH,
            SoundSource.NEUTRAL, 0.5F, 0.4F / (player.getRandom().nextFloat() * 0.4F + 0.8F));
        server.levelEvent(null, 1003, player.blockPosition(), 0);
        if (!player.getAbilities().instabuild) stack.shrink(1);
        return InteractionResultHolder.success(stack);
    }
}