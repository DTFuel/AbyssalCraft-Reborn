package com.shinoow.abyssalcraft.platform;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.shinoow.abyssalcraft.system.transfer.ItemTransferHost;
//? if forge {
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
//?} else {
/*import java.util.function.Supplier;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
*///?}

/** Persistent item-transfer configuration attached to every block entity. */
public final class ItemTransferAttachmentCompat {

    private static boolean attached;

    private ItemTransferAttachmentCompat() {}

    //? if forge {
    private static final Capability<ItemTransferHost> ITEM_TRANSFER =
        CapabilityManager.get(new CapabilityToken<ItemTransferHost>() {});
    private static final ResourceLocation KEY = ACRef.id("item_transfer");

    private static final class Provider implements ICapabilitySerializable<CompoundTag> {
        private final ItemTransferHost holder;
        private final LazyOptional<ItemTransferHost> optional;

        private Provider(BlockEntity blockEntity) {
            holder = new ItemTransferHost(blockEntity::setChanged);
            optional = LazyOptional.of(() -> holder);
        }

        private void invalidate() {
            optional.invalidate();
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
            return capability == ITEM_TRANSFER ? optional.cast() : LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            return holder.save(null);
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            holder.load(tag, null);
        }
    }

    public static void attach(IEventBus modBus) {
        attached = true;
        modBus.addListener((RegisterCapabilitiesEvent event) -> event.register(ItemTransferHost.class));
        MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class,
            (AttachCapabilitiesEvent<BlockEntity> event) -> {
                Provider provider = new Provider(event.getObject());
                event.addCapability(KEY, provider);
                event.addListener(provider::invalidate);
            });
    }

    public static ItemTransferHost get(BlockEntity blockEntity) {
        return blockEntity.getCapability(ITEM_TRANSFER).orElse(null);
    }

    public static ItemTransferHost getOrCreate(BlockEntity blockEntity) {
        return get(blockEntity);
    }
    //?} else {
    /*private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
        DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES,
            com.shinoow.abyssalcraft.AbyssalCraft.MODID);

    private static final Supplier<AttachmentType<ItemTransferHost>> ITEM_TRANSFER = ATTACHMENTS.register(
        "item_transfer", () -> AttachmentType.builder(ItemTransferAttachmentCompat::newHolder)
            .serialize(new IAttachmentSerializer<CompoundTag, ItemTransferHost>() {
                @Override
                public ItemTransferHost read(IAttachmentHolder holder, CompoundTag tag,
                                             HolderLookup.Provider registries) {
                    ItemTransferHost transfer = newHolder(holder);
                    transfer.load(tag, registries);
                    return transfer;
                }

                @Override
                public CompoundTag write(ItemTransferHost transfer, HolderLookup.Provider registries) {
                    return transfer.isEmpty() ? null : transfer.save(registries);
                }
            }).build());

    private static ItemTransferHost newHolder(IAttachmentHolder holder) {
        if (!(holder instanceof BlockEntity blockEntity)) {
            throw new IllegalArgumentException("Item transfer data can only attach to a BlockEntity");
        }
        return new ItemTransferHost(blockEntity::setChanged);
    }

    public static void attach(IEventBus modBus) {
        attached = true;
        ATTACHMENTS.register(modBus);
    }

    public static ItemTransferHost get(BlockEntity blockEntity) {
        return blockEntity.getExistingDataOrNull(ITEM_TRANSFER.get());
    }

    public static ItemTransferHost getOrCreate(BlockEntity blockEntity) {
        return blockEntity.getData(ITEM_TRANSFER.get());
    }
    *///?}

    public static boolean isAttached() {
        return attached;
    }
}