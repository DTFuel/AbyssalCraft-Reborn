package com.shinoow.abyssalcraft.platform;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
//? if <1.21 {
import net.minecraftforge.entity.PartEntity;
//?} else {
/*import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.neoforged.neoforge.entity.PartEntity;
*///?}

/** Loader-neutral multipart entity boundary for Shoggoths and dragons. */
public final class EntityPartCompat {

    private EntityPartCompat() {}

    public interface Parent {
        boolean hurtPart(Part<?> part, DamageSource source, float amount);
    }

    public static final class Part<T extends Entity & Parent> extends PartEntity<T> {

        private final String name;
        private final EntityDimensions dimensions;

        public Part(T parent, String name, float width, float height) {
            super(parent);
            this.name = name;
            dimensions = EntityDimensions.scalable(width, height);
            refreshDimensions();
        }

        public String name() {
            return name;
        }

        //? if <1.21 {
        @Override
        protected void defineSynchedData() {}
        //?} else {
        /*@Override
        protected void defineSynchedData(SynchedEntityData.Builder builder) {}
        *///?}

        @Override
        protected void readAdditionalSaveData(CompoundTag tag) {}

        @Override
        protected void addAdditionalSaveData(CompoundTag tag) {}

        @Override
        public boolean isPickable() {
            return true;
        }

        @Override
        public ItemStack getPickResult() {
            return getParent().getPickResult();
        }

        @Override
        public boolean hurt(DamageSource source, float amount) {
            return !isInvulnerableTo(source) && getParent().hurtPart(this, source, amount);
        }

        @Override
        public boolean is(Entity entity) {
            return this == entity || getParent() == entity;
        }

        //? if <1.21 {
        @Override
        public Packet<ClientGamePacketListener> getAddEntityPacket() {
            throw new UnsupportedOperationException();
        }
        //?} else {
        /*@Override
        public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
            throw new UnsupportedOperationException();
        }
        *///?}

        @Override
        public EntityDimensions getDimensions(Pose pose) {
            return dimensions;
        }

        @Override
        public boolean shouldBeSaved() {
            return false;
        }
    }
}