package com.shinoow.abyssalcraft.platform;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

//? if forge {
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
//?} else {
/*import java.util.function.Supplier;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
*///?}

/**
 * Compat: per-player persistent data (loader axis - the largest divergence).
 *
 * <p>Forge attaches an entity <em>capability</em>; NeoForge 1.21 replaced entity capabilities with
 * <em>data attachments</em>. Both are hidden here behind one neutral {@link CompoundTag} stored on the
 * player, so business code (the necrodata capability, {@code system/cap/**}) only reads/writes vanilla
 * NBT and never touches the loader machinery. The stored tag is serialized on world save and copied to
 * the respawned player on death (Forge {@code PlayerEvent.Clone} / NeoForge {@code copyOnDeath}).
 *
 * <p>Call {@link #bootstrap(Object)} once from the mod constructor with the MOD event bus.
 * Client sync of the tag is driven by the mod network layer (PS-1 necrodata messages), not here.
 */
public final class PlayerDataCompat {

    private PlayerDataCompat() {}

    //? if forge {
    private static final Capability<Holder> NECRO_DATA = CapabilityManager.get(new CapabilityToken<Holder>() {});
    private static final ResourceLocation KEY = ACRef.id("necrodata");

    /** Self-hosting capability provider wrapping the player's neutral necrodata tag. */
    public static final class Holder implements ICapabilitySerializable<CompoundTag> {
        private CompoundTag tag = new CompoundTag();
        private final LazyOptional<Holder> optional = LazyOptional.of(() -> this);

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
            return cap == NECRO_DATA ? optional.cast() : LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            return tag.copy();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            tag = nbt;
        }
    }

    public static void bootstrap(Object modBus) {
        ((IEventBus) modBus).addListener((RegisterCapabilitiesEvent event) -> event.register(Holder.class));
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, (AttachCapabilitiesEvent<Entity> event) -> {
            if (event.getObject() instanceof Player) {
                event.addCapability(KEY, new Holder());
            }
        });
        MinecraftForge.EVENT_BUS.addListener((PlayerEvent.Clone event) -> {
            if (event.isWasDeath()) {
                event.getOriginal().reviveCaps();
                setTag(event.getEntity(), getTag(event.getOriginal()).copy());
                event.getOriginal().invalidateCaps();
            }
        });
    }

    public static CompoundTag getTag(Player player) {
        return player.getCapability(NECRO_DATA).map(h -> h.tag).orElseGet(CompoundTag::new);
    }

    public static void setTag(Player player, CompoundTag tag) {
        player.getCapability(NECRO_DATA).ifPresent(h -> h.tag = tag);
    }
    //?} else {
    /*private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, com.shinoow.abyssalcraft.AbyssalCraft.MODID);

    private static final Supplier<AttachmentType<CompoundTag>> NECRO_DATA = ATTACHMENTS.register(
            "necrodata", () -> AttachmentType.builder(() -> new CompoundTag()).serialize(CompoundTag.CODEC).copyOnDeath().build());

    public static void bootstrap(Object modBus) {
        ATTACHMENTS.register((IEventBus) modBus);
    }

    public static CompoundTag getTag(Player player) {
        return player.getData(NECRO_DATA.get());
    }

    public static void setTag(Player player, CompoundTag tag) {
        player.setData(NECRO_DATA.get(), tag);
    }
    *///?}
}
