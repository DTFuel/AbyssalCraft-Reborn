package com.shinoow.abyssalcraft.system.energy.disruption;

import com.shinoow.abyssalcraft.content.entity.boss.BossEntities;
import com.shinoow.abyssalcraft.content.entity.demon.DemonEntities;
import com.shinoow.abyssalcraft.content.entity.legacy.LegacyEntities;
import com.shinoow.abyssalcraft.content.entity.shoggoth.ShoggothEntities;
import com.shinoow.abyssalcraft.platform.MobEffectCompat;
import com.shinoow.abyssalcraft.system.effect.ACEffects;
import com.shinoow.abyssalcraft.system.energy.DeityType;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * The concrete AbyssalCraft disruptions (owned by PS-9), faithful to the 1.12.2
 * {@code AbyssalCrafting.addDisruptions()} registrations. {@link #bootstrap()} runs once at mod init and
 * fills the {@link DisruptionHandler} singleton (the trigger -- a PE manipulator drawing energy without a
 * Place of Power -- lands with the unported PS-5 manipulator block).
 *
 * <p>Twenty-two entries are implemented. The five unsupported entries remain explicitly BLOCKED in
 * {@link DisruptionAudit}: ooze, random swarm/spawn, and the two invisible deity swarms.
 */
public final class Disruptions {

    private Disruptions() {}

    public static void bootstrap() {
        DisruptionHandler handler = DisruptionHandler.instance();

        // Spawn family pilot: a bolt of lightning at the manipulator (faithful DisruptionLightning).
        handler.registerDisruption(new LightningDisruption("lightning", null));

        // Potion family (faithful DisruptionPotion: every living entity within ~16 blocks, 600 ticks).
        handler.registerDisruption(new PotionDisruption("poisonPotion", null,
            () -> new MobEffectInstance(MobEffects.POISON, 600)));
        handler.registerDisruption(new PotionDisruption("slownessPotion", null,
            () -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 600)));
        handler.registerDisruption(new PotionDisruption("weaknessPotion", null,
            () -> new MobEffectInstance(MobEffects.WEAKNESS, 600)));
        handler.registerDisruption(new PotionDisruption("witherPotion", null,
            () -> new MobEffectInstance(MobEffects.WITHER, 600)));
        // Coralium plague (PS-4 ACEffects); the Holder-wrapping fork lives in MobEffectCompat. The 1.12.2
        // AC immunity list (EntityUtil.isEntityImmune) is AC-specific and deferred with the immunity data.
        handler.registerDisruption(new PotionDisruption("coraliumPotion", null,
            () -> MobEffectCompat.effectInstance(ACEffects.CORALIUM_PLAGUE, 600, 0)));

        // Player-targeted family (faithful DisruptionFreeze/DisruptionTeleportRandomly/DisruptionFamine).
        handler.registerDisruption(new PlayerDisruption("freeze", null,
            (player, level) -> player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 14))));
        handler.registerDisruption(new PlayerDisruption("randomTeleport", null,
            Disruptions::teleportRandomly));
        handler.registerDisruption(new PlayerDisruption("famineAzathoth", DeityType.AZATHOTH,
            (player, level) -> player.getFoodData().setFoodLevel(0)));
        handler.registerDisruption(new PlayerDisruption("famineShuNiggurath", DeityType.SHUBNIGGURATH,
            (player, level) -> player.getFoodData().setFoodLevel(0)));

        handler.registerDisruption(new SpawnDisruption("spawnShoggoth", null,
            ShoggothEntities.LESSER_SHOGGOTH, 1));
        handler.registerDisruption(new SwarmDisruption("swarmShadow", null,
            LegacyEntities.SHADOW_CREATURE, LegacyEntities.SHADOW_MONSTER, LegacyEntities.SHADOW_BEAST));
        handler.registerDisruption(new SwarmDisruption("swarmSheep", DeityType.SHUBNIGGURATH,
            DemonEntities.EVIL_SHEEP, () -> EntityType.SHEEP));
        handler.registerDisruption(new SpawnDisruption("spawnShubOffspring", DeityType.SHUBNIGGURATH,
            BossEntities.SHUB_OFFSPRING, 1));
        handler.registerDisruption(new DisplaceEntitiesDisruption());
        handler.registerDisruption(new PotentialEnergyDisruption());
        handler.registerDisruption(new DrainNearbyPEDisruption());
        handler.registerDisruption(new AnimalCorruptionDisruption());
        handler.registerDisruption(new SacrificeCorruptionDisruption(
            "sacrificeCorruptionJzahar", DeityType.JZAHAR));
        handler.registerDisruption(new SacrificeCorruptionDisruption(
            "sacrificeCorruptionYogSothoth", DeityType.YOGSOTHOTH));
        handler.registerDisruption(new FireDisruption());
        handler.registerDisruption(new FireRainDisruption());
    }

    /** Faithful {@code DisruptionTeleportRandomly}: fling the player up to 32 blocks away (vanilla ender maths). */
    private static void teleportRandomly(Player player, Level level) {
        double x = player.getX() + (level.random.nextDouble() - 0.5D) * 64.0D;
        double y = player.getY() + (level.random.nextInt(64) - 32);
        double z = player.getZ() + (level.random.nextDouble() - 0.5D) * 64.0D;
        player.randomTeleport(x, y, z, true);
    }
}
