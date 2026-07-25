package com.shinoow.abyssalcraft.world.structure;

import net.minecraft.resources.ResourceLocation;

/** Identifies the AC structure currently being placed on this worldgen thread. */
public final class LegacyStructurePlacementContext {

    public enum Palette {
        NONE,
        DREADLANDS_MINESHAFT,
        ABYSSAL_STRONGHOLD
    }

    private static final ThreadLocal<Palette> ACTIVE = ThreadLocal.withInitial(() -> Palette.NONE);

    private LegacyStructurePlacementContext() {}

    public static void enter(ResourceLocation structureId) {
        Palette palette = Palette.NONE;
        if (structureId != null && "abyssalcraft".equals(structureId.getNamespace())) {
            palette = switch (structureId.getPath()) {
                case "dreadlands_mineshaft" -> Palette.DREADLANDS_MINESHAFT;
                case "abyssal_stronghold" -> Palette.ABYSSAL_STRONGHOLD;
                default -> Palette.NONE;
            };
        }
        ACTIVE.set(palette);
    }

    public static Palette active() {
        return ACTIVE.get();
    }

    public static int activeId() {
        return ACTIVE.get().ordinal();
    }

    public static void exit() {
        ACTIVE.remove();
    }
}