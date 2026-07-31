package com.shinoow.abyssalcraft.content.entity.base;

//? if <1.21 {
import net.minecraft.resources.ResourceLocation;
//?} else {
/*import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
*///?}
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

import com.shinoow.abyssalcraft.content.entity.boss.ACBossMob;
import com.shinoow.abyssalcraft.content.entity.boss.EliteMob;
import com.shinoow.abyssalcraft.platform.ACRef;

/**
 * Base class for AbyssalCraft hostile mobs (owned by PD-1, Stage D1).
 *
 * <p>Faithful successor to 1.12.2 {@code common.entity.base.EntityMobBase} ({@code extends EntityMob}
 * = modern {@link Monster}). It is the shared root that the concrete mob families (anti / demon /
 * ghoul / shoggoth / bosses) subclass in Stage D2a. Every subclass supplies its own attribute values
 * through {@link #createAttributes()} (overridden or composed) and registers them via
 * {@code registry/ModEntities} into the mod-bus attribute-creation event.
 *
 * <p>The class is concrete on purpose (mirroring {@code EntityMobBase}): {@code ModEntities} registers
 * it directly as the {@code pilot_mob} example so the framework is provable with {@code /summon} before
 * any concrete entity exists (same smoke-test idiom as the PC-1 block-entity bases).
 *
 * <p>The legacy hardcore armor-piercing player chip is applied here so concrete mobs share the old
 * {@code EntityMobBase} path, including its ordinary, elite and boss damage tiers.
 */
public class ACMob extends Monster {

    public ACMob(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        HardcoreMeleeDamage.applyChip(this, target, hardcoreChipBaseDamage());
        return super.doHurtTarget(target);
    }

    protected float hardcoreChipBaseDamage() {
        return this instanceof ACBossMob ? 4.5F : this instanceof EliteMob ? 3.0F : 1.5F;
    }

    /** Optional legacy table basename used by stateful entities with collapsed modern EntityTypes. */
    protected String legacyLootTable() {
        return null;
    }

    //? if <1.21 {
    @Override
    protected ResourceLocation getDefaultLootTable() {
        String table = legacyLootTable();
        return table == null ? super.getDefaultLootTable() : ACRef.id("entities/" + table);
    }
    //?} else {
    /*@Override
    protected ResourceKey<LootTable> getDefaultLootTable() {
        String table = legacyLootTable();
        return table == null ? super.getDefaultLootTable()
            : ResourceKey.create(Registries.LOOT_TABLE, ACRef.id("entities/" + table));
    }
    *///?}

    /**
     * Default attribute template for AbyssalCraft mobs: the vanilla monster baseline
     * ({@link Monster#createMonsterAttributes()}). Subclasses override to set faithful health /
     * damage / speed / follow-range values, then wire the builder into the attribute-creation event
     * via {@code registry/ModEntities}.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes();
    }
}
