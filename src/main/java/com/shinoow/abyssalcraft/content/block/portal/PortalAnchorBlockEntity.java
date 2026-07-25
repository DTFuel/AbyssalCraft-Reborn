package com.shinoow.abyssalcraft.content.block.portal;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.content.entity.misc.DimensionPortal;
import com.shinoow.abyssalcraft.content.entity.misc.MiscEntities;
import com.shinoow.abyssalcraft.platform.BlockEntityCompat;
import com.shinoow.abyssalcraft.system.portal.DimensionData;
import com.shinoow.abyssalcraft.system.portal.DimensionDataRegistry;
import com.shinoow.abyssalcraft.system.portal.PortalAnchorIndex;

/** Persistent destination and entity linkage for a Portal Anchor. */
public final class PortalAnchorBlockEntity extends BlockEntityCompat {

    private ResourceKey<Level> destination;
    private int color = 0xFFFFFFFF;
    private UUID portalUuid;
    private int validationTicks;

    public PortalAnchorBlockEntity(BlockPos pos, BlockState state) {
        super(PortalBlocks.PORTAL_ANCHOR_BE.get(), pos, state);
    }

    public ResourceKey<Level> getDestination() {
        return destination;
    }

    public int getColor() {
        return color;
    }

    public UUID getPortalUuid() {
        return portalUuid;
    }

    public ActivationResult toggle(Player player, ResourceKey<Level> requestedDestination,
                                   int gatewayTier) {
        if (!(level instanceof ServerLevel server)) return ActivationResult.INVALID_TARGET;
        if (getBlockState().getValue(PortalAnchorBlock.ACTIVE)) {
            removePortal(true);
            return ActivationResult.DEACTIVATED;
        }
        DimensionDataRegistry registry = DimensionDataRegistry.instance();
        boolean includeVanilla = ACConfig.vanilla_handling.get();
        if (!registry.isAvailableForGatewayTier(requestedDestination, gatewayTier, includeVanilla)
            || requestedDestination.equals(server.dimension())) {
            return ActivationResult.INVALID_TARGET;
        }
        boolean unchained = isUnchained();
        if (!unchained && !registry.areDimensionsConnected(
            server.dimension(), requestedDestination, gatewayTier)) {
            return ActivationResult.INCOMPATIBLE;
        }
        DimensionData data = registry.get(requestedDestination).orElse(null);
        if (data == null) return ActivationResult.INVALID_TARGET;
        destination = requestedDestination;
        color = data.color();
        if (!spawnPortal(server, unchained)) return ActivationResult.SPAWN_FAILED;
        server.setBlock(worldPosition, getBlockState().setValue(PortalAnchorBlock.ACTIVE, true), 3);
        PortalAnchorIndex.get(server).register(worldPosition, destination);
        sync();
        return ActivationResult.ACTIVATED;
    }

    public void removePortal(boolean updateState) {
        if (level instanceof ServerLevel server) {
            Entity linked = portalUuid == null ? null : server.getEntity(portalUuid);
            if (linked instanceof DimensionPortal) linked.discard();
            AABB area = new AABB(worldPosition.above()).inflate(0.5D);
            server.getEntitiesOfClass(DimensionPortal.class, area,
                portal -> portal.isAnchoredAt(worldPosition)).forEach(Entity::discard);
            PortalAnchorIndex.get(server).unregister(worldPosition);
            if (updateState && getBlockState().getBlock() instanceof PortalAnchorBlock
                && getBlockState().getValue(PortalAnchorBlock.ACTIVE)) {
                server.setBlock(worldPosition,
                    getBlockState().setValue(PortalAnchorBlock.ACTIVE, false), 3);
            }
        }
        portalUuid = null;
        sync();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  PortalAnchorBlockEntity anchor) {
        if (!(level instanceof ServerLevel server) || !state.getValue(PortalAnchorBlock.ACTIVE)) return;
        if (++anchor.validationTicks < 20) return;
        anchor.validationTicks = 0;
        if (anchor.destination == null
            || DimensionDataRegistry.instance().get(anchor.destination).isEmpty()) {
            anchor.removePortal(true);
            return;
        }
        PortalAnchorIndex.get(server).register(pos, anchor.destination);
        Entity linked = anchor.portalUuid == null ? null : server.getEntity(anchor.portalUuid);
        if (!(linked instanceof DimensionPortal portal)
            || !portal.isAlive()
            || !portal.isAnchoredAt(pos)) {
            anchor.spawnPortal(server, anchor.isUnchained());
            anchor.sync();
        }
    }

    private boolean spawnPortal(ServerLevel server, boolean unchained) {
        if (destination == null) return false;
        DimensionPortal portal = MiscEntities.PORTAL.get().create(server);
        if (portal == null) return false;
        portal.setDestination(destination).setUnchained(unchained).setAnchor(worldPosition);
        portal.setPos(worldPosition.getX() + 0.5D, worldPosition.getY() + 1.0D,
            worldPosition.getZ() + 0.5D);
        if (!server.addFreshEntity(portal)) return false;
        portalUuid = portal.getUUID();
        return true;
    }

    private boolean isUnchained() {
        return getBlockState().getBlock() instanceof PortalAnchorBlock anchor && anchor.isUnchained();
    }

    private void sync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel server && destination != null
            && getBlockState().getValue(PortalAnchorBlock.ACTIVE)) {
            PortalAnchorIndex.get(server).register(worldPosition, destination);
        }
    }

    @Override
    protected void saveData(CompoundTag tag, HolderLookup.Provider registries) {
        if (destination != null) tag.putString("Destination", destination.location().toString());
        tag.putInt("Color", color);
        if (portalUuid != null) tag.putUUID("Portal", portalUuid);
    }

    @Override
    protected void loadData(CompoundTag tag, HolderLookup.Provider registries) {
        destination = DimensionDataRegistry.instance()
            .parseRegisteredDimension(tag.getString("Destination")).orElse(null);
        color = tag.contains("Color") ? tag.getInt("Color") : 0xFFFFFFFF;
        portalUuid = tag.hasUUID("Portal") ? tag.getUUID("Portal") : null;
    }

    public enum ActivationResult {
        ACTIVATED("message.abyssalcraft.portal.activated"),
        DEACTIVATED("message.abyssalcraft.portal.deactivated"),
        INVALID_TARGET("message.abyssalcraft.portal.invalid_target"),
        INCOMPATIBLE("message.abyssalcraft.portal.incompatible"),
        SPAWN_FAILED("message.abyssalcraft.portal.spawn_failed");

        private final String translationKey;

        ActivationResult(String translationKey) {
            this.translationKey = translationKey;
        }

        public String translationKey() {
            return translationKey;
        }
    }
}