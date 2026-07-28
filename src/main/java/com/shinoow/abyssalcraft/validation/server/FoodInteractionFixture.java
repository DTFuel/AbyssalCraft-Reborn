package com.shinoow.abyssalcraft.validation.server;

import com.shinoow.abyssalcraft.content.item.misc.MiscItems;
import com.shinoow.abyssalcraft.platform.ItemDataCompat;
import com.shinoow.abyssalcraft.platform.MobEffectCompat;
import com.shinoow.abyssalcraft.system.effect.ACEffects;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Real-level consumption checks for the legacy effect foods and ten-dose antidotes. */
public final class FoodInteractionFixture {

    private static final String ANTIDOTE_USES = "AbyssalCraftAntidoteUses";

    private FoodInteractionFixture() {}

    public static void run(ServerLevel level) {
        Mob consumer = consumer(level);
        MiscItems.CORALIUM_PLAGUED_FLESH.get().finishUsingItem(
            new ItemStack(MiscItems.CORALIUM_PLAGUED_FLESH.get()), level, consumer);
        requireEffect(consumer.getEffect(MobEffects.HUNGER), 600, 1, "plagued flesh hunger");
        requireEffect(consumer.getEffect(MobEffects.CONFUSION), 600, 0, "plagued flesh confusion");
        requireEffect(MobEffectCompat.getEffect(consumer, ACEffects.CORALIUM_PLAGUE),
            600, 0, "plagued flesh coralium plague");

        consumer = consumer(level);
        consume(MiscItems.ANTI_PLAGUED_FLESH.get(), level, consumer);
        requireEffect(consumer.getEffect(MobEffects.SATURATION), 600, 1, "anti-plagued flesh saturation");
        requireEffect(consumer.getEffect(MobEffects.REGENERATION), 600, 0, "anti-plagued flesh regeneration");

        consumer = consumer(level);
        consume(MiscItems.GHOUL_FLESH.get(), level, consumer);
        requireEffect(consumer.getEffect(MobEffects.HUNGER), 600, 1, "ghoul flesh hunger");
        requireEffect(consumer.getEffect(MobEffects.CONFUSION), 600, 0, "ghoul flesh confusion");

        consumer = consumer(level);
        consume(MiscItems.ABYSSAL_GHOUL_FLESH.get(), level, consumer);
        requireEffect(consumer.getEffect(MobEffects.HUNGER), 600, 1, "abyssal ghoul flesh hunger");
        requireEffect(consumer.getEffect(MobEffects.CONFUSION), 600, 0, "abyssal ghoul flesh confusion");
        requireEffect(MobEffectCompat.getEffect(consumer, ACEffects.CORALIUM_PLAGUE),
            600, 0, "abyssal ghoul flesh coralium plague");

        consumer = consumer(level);
        consume(MiscItems.DREADED_GHOUL_FLESH.get(), level, consumer);
        requireEffect(consumer.getEffect(MobEffects.HUNGER), 600, 1, "dreaded ghoul flesh hunger");
        requireEffect(consumer.getEffect(MobEffects.CONFUSION), 600, 0, "dreaded ghoul flesh confusion");
        requireEffect(MobEffectCompat.getEffect(consumer, ACEffects.DREAD_PLAGUE),
            600, 0, "dreaded ghoul flesh dread plague");

        consumer = consumer(level);
        consume(MiscItems.OMOTHOL_GHOUL_FLESH.get(), level, consumer);
        requireEffect(consumer.getEffect(MobEffects.WEAKNESS), 100, 0, "Omothol ghoul flesh weakness");
        requireEffect(consumer.getEffect(MobEffects.HUNGER), 400, 1, "Omothol ghoul flesh hunger");
        requireEffect(consumer.getEffect(MobEffects.CONFUSION), 300, 0, "Omothol ghoul flesh confusion");
        requireEffect(consumer.getEffect(MobEffects.BLINDNESS), 40, 0, "Omothol ghoul flesh blindness");
        requireEffect(consumer.getEffect(MobEffects.NIGHT_VISION), 40, 0, "Omothol ghoul flesh night vision");

        consumer = consumer(level);
        consume(MiscItems.SHADOW_GHOUL_FLESH.get(), level, consumer);
        requireEffect(consumer.getEffect(MobEffects.HUNGER), 600, 1, "shadow ghoul flesh hunger");
        requireEffect(consumer.getEffect(MobEffects.CONFUSION), 600, 0, "shadow ghoul flesh confusion");
        requireEffect(consumer.getEffect(MobEffects.BLINDNESS), 600, 0, "shadow ghoul flesh blindness");
        requireEffect(consumer.getEffect(MobEffects.NIGHT_VISION), 600, 0, "shadow ghoul flesh night vision");

        consumer = consumer(level);
        consume(MiscItems.ANTI_GHOUL_FLESH.get(), level, consumer);
        requireEffect(consumer.getEffect(MobEffects.SATURATION), 600, 1, "anti-ghoul flesh saturation");
        requireEffect(consumer.getEffect(MobEffects.REGENERATION), 600, 0, "anti-ghoul flesh regeneration");

        consumer = consumer(level);
        consumer.addEffect(MobEffectCompat.effectInstance(ACEffects.CORALIUM_PLAGUE, 600, 0));
        ItemStack coralium = new ItemStack(MiscItems.CORALIUM_ANTIDOTE.get());
        for (int remaining = 9; remaining >= 1; remaining--) {
            coralium = MiscItems.CORALIUM_ANTIDOTE.get().finishUsingItem(coralium, level, consumer);
            require(coralium.is(MiscItems.CORALIUM_ANTIDOTE.get()),
                "coralium antidote returned the wrong container before its final dose");
            require(ItemDataCompat.getInt(coralium, ANTIDOTE_USES, 10) == remaining,
                "coralium antidote dose count changed");
        }
        coralium = MiscItems.CORALIUM_ANTIDOTE.get().finishUsingItem(coralium, level, consumer);
        require(coralium.is(Items.GLASS_BOTTLE), "coralium antidote did not return its glass bottle");
        require(!MobEffectCompat.hasEffect(consumer, ACEffects.CORALIUM_PLAGUE),
            "coralium antidote did not clear coralium plague");
        requireEffect(MobEffectCompat.getEffect(consumer, ACEffects.CORALIUM_ANTIDOTE),
            1200, 0, "coralium antidote effect");

        consumer.addEffect(MobEffectCompat.effectInstance(ACEffects.DREAD_PLAGUE, 600, 1));
        ItemStack dread = new ItemStack(MiscItems.DREAD_ANTIDOTE.get());
        dread = MiscItems.DREAD_ANTIDOTE.get().finishUsingItem(dread, level, consumer);
        require(ItemDataCompat.getInt(dread, ANTIDOTE_USES, 10) == 9,
            "dread antidote did not consume exactly one dose");
        require(!MobEffectCompat.hasEffect(consumer, ACEffects.DREAD_PLAGUE),
            "dread antidote did not clear dread plague");
        requireEffect(MobEffectCompat.getEffect(consumer, ACEffects.DREAD_ANTIDOTE),
            1200, 0, "dread antidote effect");

        System.out.println("RR_FOOD_INTERACTION_OK effectFoods=8 antidotes=2 doses=10 container=glass_bottle");
    }

    private static Mob consumer(ServerLevel level) {
        Mob consumer = EntityType.PIG.create(level);
        if (consumer == null) throw new IllegalStateException("RR_SERVER_MATRIX_FAIL could not create food fixture consumer");
        return consumer;
    }

    private static void consume(net.minecraft.world.item.Item item, ServerLevel level, Mob consumer) {
        item.finishUsingItem(new ItemStack(item), level, consumer);
    }

    private static void requireEffect(MobEffectInstance effect, int minimumDuration,
                                      int amplifier, String label) {
        require(effect != null && effect.getDuration() >= minimumDuration
            && effect.getAmplifier() == amplifier, label + " mismatch");
    }

    private static void require(boolean condition, String reason) {
        if (!condition) throw new IllegalStateException("RR_SERVER_MATRIX_FAIL " + reason);
    }
}