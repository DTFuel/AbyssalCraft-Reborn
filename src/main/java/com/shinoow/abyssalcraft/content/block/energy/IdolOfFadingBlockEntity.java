package com.shinoow.abyssalcraft.content.block.energy;

import java.util.function.Supplier;

import com.shinoow.abyssalcraft.content.blockentity.base.TickingBlockEntity;
import com.shinoow.abyssalcraft.content.entity.legacy.LegacyEntities;
import com.shinoow.abyssalcraft.content.entity.legacy.LegacyHostileMob;
import com.shinoow.abyssalcraft.platform.MobSpawnCompat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.state.BlockState;

/** Persistent PE storage and spawn timer for the Idol of Fading. */
public final class IdolOfFadingBlockEntity extends EnergyBlockEntity implements TickingBlockEntity {

    public static final int MAX_ENERGY = 1000;
    public static final int ACTIVATION_INTERVAL = 200;
    public static final float ACTIVATION_GATE = 100.0F;

    private int cooldown;

    public IdolOfFadingBlockEntity(BlockPos pos, BlockState state) {
        super(EnergyBlocks.IDOL_OF_FADING_BE.get(), pos, state, MAX_ENERGY);
    }

    @Override
    public void serverTick() {
        if (!(level instanceof ServerLevel server) || !isValidState(server)) {
            return;
        }
        if (++cooldown >= ACTIVATION_INTERVAL && getContainedEnergy() >= ACTIVATION_GATE) {
            cooldown = 0;
            activate(server);
            setChanged();
        }
    }

    private boolean isValidState(ServerLevel level) {
        return level.getDifficulty() != Difficulty.PEACEFUL
            && level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)
            && !level.canSeeSky(worldPosition.above());
    }

    private void activate(ServerLevel level) {
        if (level.getNearestPlayer(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
            32.0, false) == null || !level.isEmptyBlock(worldPosition.above())
            || !level.isEmptyBlock(worldPosition.above(2))) {
            return;
        }
        int variant = selectVariant(level.random.nextInt(10), level.random.nextInt(3));
        Supplier<EntityType<LegacyHostileMob>> type = switch (variant) {
            case 2 -> LegacyEntities.SHADOW_BEAST;
            case 1 -> LegacyEntities.SHADOW_MONSTER;
            default -> LegacyEntities.SHADOW_CREATURE;
        };
        LegacyHostileMob shadow = type.get().create(level);
        if (shadow == null) {
            return;
        }
        shadow.moveTo(worldPosition.getX(), worldPosition.getY() + 1, worldPosition.getZ(),
            level.random.nextFloat() * 360.0F, 0.0F);
        MobSpawnCompat.finalizeSpawnerSpawn(level, shadow);
        level.addFreshEntity(shadow);
        consumeEnergy(energyCost(variant));
    }

    public static int selectVariant(int beastRoll, int monsterRoll) {
        if (beastRoll == 0) {
            return 2;
        }
        return monsterRoll == 0 ? 1 : 0;
    }

    public static float energyCost(int variant) {
        return switch (variant) {
            case 2 -> 100.0F;
            case 1 -> 50.0F;
            default -> 25.0F;
        };
    }

    @Override
    protected void saveEnergyData(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("Cooldown", cooldown);
    }

    @Override
    protected void loadEnergyData(CompoundTag tag, HolderLookup.Provider registries) {
        cooldown = Math.max(0, tag.getInt("Cooldown"));
    }
}