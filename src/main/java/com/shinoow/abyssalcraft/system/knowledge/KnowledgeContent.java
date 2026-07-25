package com.shinoow.abyssalcraft.system.knowledge;

import java.util.ArrayList;
import java.util.List;

import com.shinoow.abyssalcraft.content.item.misc.MiscItems;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.system.cap.necrodata.KnowledgeType;
import com.shinoow.abyssalcraft.system.cap.necrodata.NecroData;
import com.shinoow.abyssalcraft.system.cap.necrodata.NecroDataCapability;
import com.shinoow.abyssalcraft.system.knowledge.condition.BiomePredicateCondition;
import com.shinoow.abyssalcraft.system.knowledge.condition.DimensionCondition;
import com.shinoow.abyssalcraft.system.knowledge.condition.EntityCondition;
import com.shinoow.abyssalcraft.system.knowledge.condition.EntityPredicateCondition;
import com.shinoow.abyssalcraft.system.knowledge.condition.IUnlockCondition;
import com.shinoow.abyssalcraft.system.knowledge.condition.KnowledgePredicate;
import com.shinoow.abyssalcraft.system.knowledge.condition.MandatoryMultiEntityCondition;
import com.shinoow.abyssalcraft.system.knowledge.condition.MiscCondition;
import com.shinoow.abyssalcraft.system.knowledge.condition.MultiEntityCondition;
import com.shinoow.abyssalcraft.system.knowledge.condition.NecronomiconCondition;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Complete 1.12.2 research and unlock-condition catalog. */
public final class KnowledgeContent {

    private static final List<IUnlockCondition> CONDITIONS = new ArrayList<>();
    private static final List<IResearchItem> RESEARCHES = new ArrayList<>();
    private static boolean bootstrapped;
    private static boolean offeringsBootstrapped;

    public static final IUnlockCondition DARKLANDS_BIOME = condition(new BiomePredicateCondition(KnowledgePredicate.DARKLANDS_BIOMES));
    public static final IUnlockCondition CORALIUM_INFESTED_SWAMP = condition(new com.shinoow.abyssalcraft.system.knowledge.condition.UnlockCondition(0, "abyssalcraft:coralium_infested_swamp"));
    public static final IUnlockCondition CORALIUM_BIOMES = condition(new BiomePredicateCondition(KnowledgePredicate.CORALIUM_BIOMES));
    public static final IUnlockCondition ABYSSAL_WASTELAND = condition(new DimensionCondition("abyssalcraft:abyssal_wasteland"));
    public static final IUnlockCondition DREADLANDS = condition(new DimensionCondition("abyssalcraft:dreadlands"));
    public static final IUnlockCondition OMOTHOL = condition(new DimensionCondition("abyssalcraft:omothol"));
    public static final IUnlockCondition DARK_REALM = condition(new DimensionCondition("abyssalcraft:dark_realm"));
    public static final IUnlockCondition NETHER = condition(new DimensionCondition("minecraft:the_nether"));
    public static final IUnlockCondition ABYSSAL_ZOMBIE = condition(new EntityCondition("abyssalcraft:abyssalzombie"));
    public static final IUnlockCondition DEPTHS_GHOUL = condition(new EntityCondition("abyssalcraft:depths_ghoul"));
    public static final IUnlockCondition SACTHOTH = condition(new EntityCondition("abyssalcraft:shadowboss"));
    public static final IUnlockCondition SHADOW_MOBS = condition(new MultiEntityCondition("abyssalcraft:shadowcreature", "abyssalcraft:shadowmonster", "abyssalcraft:shadowbeast"));
    public static final IUnlockCondition SHADOW_CREATURE = condition(new EntityCondition("abyssalcraft:shadowcreature"));
    public static final IUnlockCondition SHADOW_MONSTER = condition(new EntityCondition("abyssalcraft:shadowmonster"));
    public static final IUnlockCondition SHADOW_BEAST = condition(new EntityCondition("abyssalcraft:shadowbeast"));
    public static final IUnlockCondition SKELETON_GOLIATH = condition(new EntityCondition("abyssalcraft:gskeleton"));
    public static final IUnlockCondition SPECTRAL_DRAGON = condition(new EntityCondition("abyssalcraft:dragonminion"));
    public static final IUnlockCondition OMOTHOL_GHOUL = condition(new EntityCondition("abyssalcraft:omothol_ghoul"));
    public static final IUnlockCondition ELITE_DREAD_MOB = condition(new MultiEntityCondition("abyssalcraft:dreadguard", "abyssalcraft:greaterdreadspawn", "abyssalcraft:lesserdreadbeast"));
    public static final IUnlockCondition DREAD_MOB = condition(new EntityPredicateCondition(KnowledgePredicate.DREAD_ENTITIES));
    public static final IUnlockCondition KILLED_ALL_BOSSES = condition(new MandatoryMultiEntityCondition("abyssalcraft:dragonboss", "abyssalcraft:chagaroth", "abyssalcraft:jzahar", "abyssalcraft:shadowboss"));
    public static final IUnlockCondition ANTI_MOB = condition(new EntityPredicateCondition(KnowledgePredicate.ANTI_ENTITIES));
    public static final IUnlockCondition EVIL_ANIMAL = condition(new EntityPredicateCondition(KnowledgePredicate.EVIL_ANIMALS));
    public static final IUnlockCondition SHOGGOTH = condition(new EntityPredicateCondition(KnowledgePredicate.SHOGGOTHS));
    public static final IUnlockCondition SHUB_OFFSPRING = condition(new EntityCondition("abyssalcraft:shuboffspring"));
    public static final IUnlockCondition CORALIUM_INFESTED_SQUID = condition(new EntityCondition("abyssalcraft:coraliumsquid"));
    public static final IUnlockCondition DREAD_SPAWN = condition(new MultiEntityCondition("abyssalcraft:dreadspawn", "abyssalcraft:greaterdreadspawn", "abyssalcraft:lesserdreadbeast"));
    public static final IUnlockCondition DREADLING = condition(new EntityCondition("abyssalcraft:dreadling"));
    public static final IUnlockCondition DEMON_ANIMAL = condition(new EntityPredicateCondition(KnowledgePredicate.DEMON_ANIMALS));
    public static final IUnlockCondition SPAWN_OF_CHAGAROTH = condition(new EntityCondition("abyssalcraft:chagarothspawn"));
    public static final IUnlockCondition FIST_OF_CHAGAROTH = condition(new EntityCondition("abyssalcraft:chagarothfist"));
    public static final IUnlockCondition DREADGUARD = condition(new EntityCondition("abyssalcraft:dreadguard"));
    public static final IUnlockCondition MINION_OF_THE_GATEKEEPER = condition(new EntityCondition("abyssalcraft:jzaharminion"));
    public static final IUnlockCondition CORALIUM_PLAGUE = condition(new MiscCondition("coralium_plague"));
    public static final IUnlockCondition DREAD_PLAGUE = condition(new MiscCondition("dread_plague"));
    public static final IUnlockCondition ABYSSAL_WASTELAND_NECRO = condition(new NecronomiconCondition(1));
    public static final IUnlockCondition DREADLANDS_NECRO = condition(new NecronomiconCondition(2));
    public static final IUnlockCondition OMOTHOL_NECRO = condition(new NecronomiconCondition(3));
    public static final IUnlockCondition ABYSSALNOMICON = condition(new NecronomiconCondition(4));
    public static final IUnlockCondition GHOUL = condition(new EntityCondition("abyssalcraft:ghoul"));
    public static final IUnlockCondition DREADED_GHOUL = condition(new EntityCondition("abyssalcraft:dreaded_ghoul"));
    public static final IUnlockCondition SHADOW_GHOUL = condition(new EntityCondition("abyssalcraft:shadow_ghoul"));

    private KnowledgeContent() {}

    public static synchronized void bootstrap() {
        if (bootstrapped) {
            return;
        }
        register("darklands", DARKLANDS_BIOME);
        register("coralium_infused_swamp", CORALIUM_INFESTED_SWAMP);
        register("coralium_biomes", CORALIUM_BIOMES);
        register("abyssal_wasteland", ABYSSAL_WASTELAND);
        register("dreadlands", DREADLANDS);
        register("omothol", OMOTHOL);
        register("dark_realm", DARK_REALM);
        register("nether", NETHER);
        register("abyssal_zombie", ABYSSAL_ZOMBIE);
        register("depths_ghoul", DEPTHS_GHOUL);
        register("sacthoth", SACTHOTH);
        register("shadow_mobs", SHADOW_MOBS);
        register("shadow_creature", SHADOW_CREATURE);
        register("shadow_monster", SHADOW_MONSTER);
        register("shadow_beast", SHADOW_BEAST);
        register("skeleton_goliath", SKELETON_GOLIATH);
        register("spectral_dragon", SPECTRAL_DRAGON);
        register("omothol_ghoul", OMOTHOL_GHOUL);
        register("elite_dread_mob", ELITE_DREAD_MOB);
        register("dread_mob", DREAD_MOB);
        register("kill_all_bosses", KILLED_ALL_BOSSES);
        register("anti_mob", ANTI_MOB);
        register("evil_animal", EVIL_ANIMAL);
        register("shoggoth", SHOGGOTH);
        register("shub_offspring", SHUB_OFFSPRING);
        register("coralium_infested_squid", CORALIUM_INFESTED_SQUID);
        register("dread_spawn", DREAD_SPAWN);
        register("dreadling", DREADLING);
        register("demon_animal", DEMON_ANIMAL);
        register("spawn_of_chagaroth", SPAWN_OF_CHAGAROTH);
        register("fist_of_chagaroth", FIST_OF_CHAGAROTH);
        register("dreadguard", DREADGUARD);
        register("minion_of_the_gatekeeper", MINION_OF_THE_GATEKEEPER);
        register("coralium_plague", CORALIUM_PLAGUE);
        register("dread_plague", DREAD_PLAGUE);
        register("abyssal_wasteland_necro", ABYSSAL_WASTELAND_NECRO);
        register("dreadlands_necro", DREADLANDS_NECRO);
        register("omothol_necro", OMOTHOL_NECRO);
        register("abyssalnomicon", ABYSSALNOMICON);
        register("ghoul", GHOUL);
        register("dreaded_ghoul", DREADED_GHOUL);
        register("shadow_ghoul", SHADOW_GHOUL);
        bootstrapped = true;
    }

    public static synchronized void bootstrapOfferings() {
        if (offeringsBootstrapped) {
            return;
        }
        ResearchRegistry registry = ResearchRegistry.instance();
        registry.addOffering(KnowledgeType.BASE, new ItemStack(MiscItems.GHOUL_FLESH.get()));
        registry.addOffering(KnowledgeType.BASE, new ItemStack(Items.ROTTEN_FLESH));
        registry.addOffering(KnowledgeType.ABYSSAL, new ItemStack(MiscItems.ABYSSAL_GHOUL_FLESH.get()));
        registry.addOffering(KnowledgeType.ABYSSAL, new ItemStack(MiscItems.CORALIUM_PLAGUED_FLESH.get()));
        registry.addOffering(KnowledgeType.ABYSSAL, new ItemStack(MiscItems.SKIN_OF_THE_ABYSSAL_WASTELAND.get()));
        registry.addOffering(KnowledgeType.DREAD, new ItemStack(MiscItems.DREADED_GHOUL_FLESH.get()));
        registry.addOffering(KnowledgeType.DREAD, new ItemStack(MiscItems.SKIN_OF_THE_DREADLANDS.get()));
        registry.addOffering(KnowledgeType.DREAD, new ItemStack(BuiltInRegistries.ITEM.get(ACRef.id("dread_fragment"))));
        registry.addOffering(KnowledgeType.OMOTHOL, new ItemStack(MiscItems.OMOTHOL_GHOUL_FLESH.get()));
        registry.addOffering(KnowledgeType.OMOTHOL, new ItemStack(MiscItems.SKIN_OF_OMOTHOL.get()));
        registry.addOffering(KnowledgeType.SHADOW, new ItemStack(MiscItems.SHADOW_GHOUL_FLESH.get()));
        offeringsBootstrapped = true;
    }

    public static int completeAvailable(ServerPlayer player, int bookType) {
        NecroData data = NecroDataCapability.get(player);
        int before = data.getCompletedResearches().size();
        for (IResearchItem research : RESEARCHES) {
            KnowledgeGate.isUnlocked(data, research, player, bookType);
        }
        return data.getCompletedResearches().size() - before;
    }

    public static List<IResearchItem> researches() {
        return List.copyOf(RESEARCHES);
    }

    public static List<IUnlockCondition> conditions() {
        return List.copyOf(CONDITIONS);
    }

    private static IUnlockCondition condition(IUnlockCondition condition) {
        CONDITIONS.add(condition);
        return condition;
    }

    private static void register(String id, IUnlockCondition condition) {
        ResearchItem research = new ResearchItem(ACRef.id(id), "ac.research." + id,
            KnowledgeType.BASE, 0, 0);
        research.setUnlockConditions(condition);
        RESEARCHES.add(research);
        ResearchRegistry.instance().registerResearchItem(research);
    }
}