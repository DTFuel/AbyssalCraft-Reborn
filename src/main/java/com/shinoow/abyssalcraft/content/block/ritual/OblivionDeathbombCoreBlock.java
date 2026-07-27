package com.shinoow.abyssalcraft.content.block.ritual;

import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.content.entity.misc.MiscEntities;
import com.shinoow.abyssalcraft.content.entity.misc.PrimedODB;
import com.shinoow.abyssalcraft.platform.ArmorDurabilityCompat;
import com.shinoow.abyssalcraft.platform.InteractiveBlockCompat;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Redstone-, fire- and projectile-primed ODB core backed by the persistent PrimedODB entity. */
public final class OblivionDeathbombCoreBlock extends InteractiveBlockCompat {

    private static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);

    public OblivionDeathbombCoreBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!oldState.is(state.getBlock()) && level.hasNeighborSignal(pos)) prime(level, pos, null);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
                                BlockPos neighbor, boolean movedByPiston) {
        if (level.hasNeighborSignal(pos)) prime(level, pos, null);
    }

    @Override
    protected InteractionResult onUseItem(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand) {
        if (!stack.is(Items.FLINT_AND_STEEL) && !stack.is(Items.FIRE_CHARGE)) return InteractionResult.PASS;
        if (!level.isClientSide) {
            prime(level, pos, player);
            if (stack.is(Items.FLINT_AND_STEEL)) {
                ArmorDurabilityCompat.damageHeld(stack, 1, player, hand);
            } else if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected InteractionResult onUse(BlockState state, Level level, BlockPos pos, Player player) {
        return InteractionResult.PASS;
    }

    @Override
    public void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        if (!level.isClientSide && projectile.isOnFire()) {
            prime(level, hit.getBlockPos(), projectile.getOwner() instanceof LivingEntity owner ? owner : null);
        }
    }

    private static void prime(Level level, BlockPos pos, LivingEntity owner) {
        if (level.isClientSide || ACConfig.no_odb_explosions.get() || !level.getBlockState(pos).is(RitualBlocks.ODB_CORE.get())) {
            return;
        }
        PrimedODB primed = MiscEntities.PRIMED_ODB_CORE.get().create(level);
        if (primed == null) return;
        primed.setPos(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
        primed.setOwner(owner);
        level.addFreshEntity(primed);
        level.playSound(null, pos, SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.removeBlock(pos, false);
    }
}