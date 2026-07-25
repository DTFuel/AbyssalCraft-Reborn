package com.shinoow.abyssalcraft.content.item.staff;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.ModRegistrar;

/**
 * The spell-casting staves (upstream content: the first Potential-Energy spell consumer). The pilot
 * {@code spell_staff} proves the PS-7 cast loop; the faithful staff roster (Staff of the Gate variants,
 * scroll selection) is the PS-7b follow-up. Item models/textures are asset-stage (PK) work; this only
 * registers + names the staff. The registrar is attached to the MOD bus through
 * {@link com.shinoow.abyssalcraft.registry.ModRegistries#ALL}.
 */
public final class StaffItems {

    private StaffItems() {}

    /** {@code minecraft:item} registrar in the AbyssalCraft namespace for the staves. */
    public static final ModRegistrar<Item> ITEMS = ModRegistrar.of(Registries.ITEM, AbyssalCraft.MODID);

    public static final Supplier<Item> SPELL_STAFF = ITEMS.register("spell_staff", StaffItem::new);

    /** Every staff, in order (for the creative tab). */
    public static final List<Supplier<Item>> ALL = List.of(SPELL_STAFF);
}
