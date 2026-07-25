package com.shinoow.abyssalcraft.content.block.shoggoth;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.content.entity.shoggoth.AbstractShoggoth;
import com.shinoow.abyssalcraft.content.entity.shoggoth.ShoggothEntities;
import com.shinoow.abyssalcraft.platform.MobSpawnCompat;
import com.shinoow.abyssalcraft.registry.BaseBlocks;

/** Persistent Shoggoth biomass that produces five Shoggoths before hardening into monolith stone. */
public final class ShoggothBiomassBlock extends Block {

    public static final IntegerProperty SPAWNS = IntegerProperty.create("spawns", 0, 4);
    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 13.6D, 16.0D);

    public ShoggothBiomassBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(SPAWNS, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SPAWNS);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, net.minecraft.world.level.BlockGetter level,
                                        BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!(entity instanceof AbstractShoggoth)) {
            entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.4D, 1.0D, 0.4D));
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!level.isClientSide && !oldState.is(this)) {
            level.scheduleTick(pos, this, ACConfig.biomassCooldown.get());
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.is(this)) return;
        scheduleNext(level, pos);
        if (level.getDifficulty() == Difficulty.PEACEFUL
                || !level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)
                || level.getNearestPlayer(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    ACConfig.biomassPlayerDistance.get(), false) == null) return;
        double range = ACConfig.biomassShoggothDistance.get();
        if (level.getEntitiesOfClass(AbstractShoggoth.class, new AABB(pos).inflate(range)).size()
                >= ACConfig.biomassMaxSpawn.get()) return;
        AbstractShoggoth shoggoth = createShoggoth(level, random);
        if (shoggoth == null) return;
        BlockPos spawnPos = findSpawnPosition(level, pos);
        shoggoth.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D,
            random.nextFloat() * 360.0F, 0.0F);
        MobSpawnCompat.finalizeSpawnerSpawn(level, shoggoth);
        if (!level.addFreshEntity(shoggoth)) return;
        resetNearby(level, pos, random);
        int spawned = state.getValue(SPAWNS) + 1;
        level.setBlockAndUpdate(pos, spawned >= 5
            ? BaseBlocks.MONOLITH_STONE.get().defaultBlockState()
            : state.setValue(SPAWNS, spawned));
    }

    private void scheduleNext(ServerLevel level, BlockPos pos) {
        level.scheduleTick(pos, this, ACConfig.biomassCooldown.get());
    }

    private void resetNearby(ServerLevel level, BlockPos pos, RandomSource random) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if ((dx != 0 || dz != 0) && level.getBlockState(pos.offset(dx, 0, dz)).is(this)) {
                    level.scheduleTick(pos.offset(dx, 0, dz), this, 1 + random.nextInt(30));
                }
            }
        }
    }

    private static AbstractShoggoth createShoggoth(ServerLevel level, RandomSource random) {
        EntityType<? extends AbstractShoggoth> type;
        if (!random.nextBoolean()) {
            type = ShoggothEntities.LESSER_SHOGGOTH.get();
        } else {
            type = random.nextInt(100) == 0
                ? ShoggothEntities.GREATER_SHOGGOTH.get()
                : ShoggothEntities.SHOGGOTH.get();
        }
        return type.create(level);
    }

    private static BlockPos findSpawnPosition(ServerLevel level, BlockPos origin) {
        if (isOpen(level, origin.above())) return origin.above();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos candidate = origin.offset(dx, 1, dz);
                if (isOpen(level, candidate)) return candidate;
            }
        }
        return origin.above(level.isEmptyBlock(origin.above(15)) ? 15 : 20);
    }

    private static boolean isOpen(ServerLevel level, BlockPos pos) {
        return level.isEmptyBlock(pos) && level.isEmptyBlock(pos.above());
    }
}