package com.shinoow.abyssalcraft.content.entity.misc;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Level;

import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.config.ComplexConfig;
import com.shinoow.abyssalcraft.config.ContentConfigMatrix;
import com.shinoow.abyssalcraft.content.entity.boss.BossMob;
import com.shinoow.abyssalcraft.content.entity.boss.EliteMob;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.ACSimpleEntity;
import com.shinoow.abyssalcraft.system.portal.DimensionData;
import com.shinoow.abyssalcraft.system.portal.DimensionDataRegistry;
import com.shinoow.abyssalcraft.world.portal.DimensionTeleport;
import com.shinoow.abyssalcraft.world.ACDimensions;

/**
 * Dimension portal entity (1.12.2 {@code portal}) and its single-use variant ({@code singleportal}),
 * collapsed into one class via the {@code singleUse} flag (baked into each {@link EntityType} factory).
 * These are the standing portals that ferry entities between AbyssalCraft dimensions.
 *
 * <p>The destination and unchained state are synchronized for rendering and persisted with the optional
 * anchor position. Active {@code PortalAnchorBlockEntity} instances own a portal UUID and recreate a
 * missing linked entity. Each server tick this entity validates passengers, applies cooldown, rejects
 * bosses and riding entities, then delegates the cross-version transfer to {@link DimensionTeleport}.
 * The single-use variant discards itself after the first successful passenger.
 */
public class DimensionPortal extends ACSimpleEntity {

    private static final EntityDataAccessor<String> DESTINATION =
        SynchedEntityData.defineId(DimensionPortal.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> UNCHAINED =
        SynchedEntityData.defineId(DimensionPortal.class, EntityDataSerializers.BOOLEAN);

    private final boolean singleUse;
    private BlockPos anchorPos;

    public DimensionPortal(EntityType<?> type, Level level, boolean singleUse) {
        super(type, level);
        this.singleUse = singleUse;
        setNoGravity(true);
    }

    /** Whether this portal despawns after a single use (the {@code singleportal} variant). */
    public boolean isSingleUse() {
        return singleUse;
    }

    /** Set the dimension this portal ferries entities to (1.12.2 {@code setDestination}). */
    public DimensionPortal setDestination(ResourceKey<Level> dimension) {
        entityData.set(DESTINATION, dimension.location().toString());
        return this;
    }

    /** The dimension this portal ferries to, or {@code null} until one is assigned. */
    public ResourceKey<Level> getDestination() {
        return DimensionDataRegistry.instance().parseRegisteredDimension(entityData.get(DESTINATION))
            .orElse(null);
    }

    public DimensionPortal setUnchained(boolean unchained) {
        entityData.set(UNCHAINED, unchained);
        return this;
    }

    public boolean isUnchained() {
        return entityData.get(UNCHAINED);
    }

    public DimensionData getDimensionData() {
        ResourceKey<Level> destination = getDestination();
        DimensionData data = destination == null ? null : DimensionDataRegistry.instance().get(destination).orElse(null);
        if (data == null || !destination.equals(ACDimensions.ABYSSAL_WASTELAND)) return data;
        int[] rgb = ComplexConfig.portalColor();
        int color = 0xFF000000 | rgb[0] << 16 | rgb[1] << 8 | rgb[2];
        return new DimensionData(data.dimension(), data.displayKey(), color, data.minimumGatewayTier(),
            data.minimumBookType(),
            data.connectedDimensions(), data.portalMob(), data.overlay());
    }

    public DimensionPortal setAnchor(BlockPos anchorPos) {
        this.anchorPos = anchorPos.immutable();
        return this;
    }

    public boolean isAnchoredAt(BlockPos pos) {
        return anchorPos != null && anchorPos.equals(pos);
    }

    @Override
    protected void defineSimpleSyncedData(SyncedDataBuilder builder) {
        builder.define(DESTINATION, "");
        builder.define(UNCHAINED, false);
    }

    @Override
    public void tick() {
        super.tick();
        ResourceKey<Level> destination = getDestination();
        if (level().isClientSide || destination == null) return;

        tickPortalSpawn(destination);

        List<Entity> touching = level().getEntities(this, getBoundingBox(),
            e -> !(e instanceof DimensionPortal)
                && !(e instanceof BossMob)
                && !(e instanceof EliteMob)
                && e.isAlive()
                && !e.isRemoved()
                && !e.isPassenger()
                && !e.isVehicle()
                && !e.isOnPortalCooldown());
        for (Entity e : touching) {
            int cooldown = e instanceof ServerPlayer ? ACConfig.portalCooldown.get() : e.getPortalCooldown();
            e.setPortalCooldown(cooldown);
            Entity teleported = DimensionTeleport.teleport(e, destination);
            teleported.setPortalCooldown(cooldown);
            if (singleUse && teleported.level().dimension().equals(destination)) {
                discard();
                return;
            }
        }
    }

    private void tickPortalSpawn(ResourceKey<Level> destination) {
        if (tickCount % 10 != 0 || !(level() instanceof ServerLevel server)
                || destination.equals(level().dimension()) || !server.getGameRules().getBoolean("doMobSpawning")) return;
        DimensionData data = getDimensionData();
        if (data == null || data.portalMob().isEmpty()) return;
        if (ContentConfigMatrix.portalSpawnsNearPlayer()
                && server.getNearestPlayer(getX(), getY(), getZ(), 32.0D, false) == null) return;
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(data.portalMob().get()).orElse(null);
        if (type == null || server.getEntities(type, new AABB(blockPosition()).inflate(16.0D), Entity::isAlive).size() >= 10
                || random.nextInt(2000) >= server.getDifficulty().getId()) return;
        Entity entity = type.create(server);
        if (entity == null) return;
        BlockPos spawn = blockPosition().above();
        entity.moveTo(spawn.getX() + 0.5D, spawn.getY(), spawn.getZ() + 0.5D,
            random.nextFloat() * 360.0F, 0.0F);
        if (entity instanceof Mob mob) mob.finalizeSpawn(server, server.getCurrentDifficultyAt(spawn),
            MobSpawnType.PORTAL, null, null);
        if (server.addFreshEntity(entity)) entity.setPortalCooldown();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        ResourceKey<Level> destination = getDestination();
        if (destination != null)
            tag.putString("Destination", destination.location().toString());
        tag.putBoolean("Unchained", isUnchained());
        if (anchorPos != null) tag.putLong("AnchorPos", anchorPos.asLong());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("Destination"))
            setDestination(ResourceKey.create(Registries.DIMENSION, ACRef.parse(tag.getString("Destination"))));
        setUnchained(tag.getBoolean("Unchained"));
        anchorPos = tag.contains("AnchorPos") ? BlockPos.of(tag.getLong("AnchorPos")) : null;
    }
}
