package com.shinoow.abyssalcraft.content.entity.boss;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import com.shinoow.abyssalcraft.content.block.shoggoth.ShoggothBlocks;
import com.shinoow.abyssalcraft.content.entity.ai.WorshipGoal;
import com.shinoow.abyssalcraft.content.item.book.BookItems;
import com.shinoow.abyssalcraft.content.item.misc.MiscItems;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.ArmorDurabilityCompat;
import com.shinoow.abyssalcraft.platform.MerchantOfferCompat;
import com.shinoow.abyssalcraft.platform.ShearableCompat;
import com.shinoow.abyssalcraft.registry.ModSounds;

/** Seven-profession Remnant merchant with persistent offers, anger and one-time shearing. */
public class RemnantMob extends EliteMob implements Merchant, ShearableCompat {

    private static final int RESTOCK_DELAY = 40;
    private static final int RESTOCK_REGEN_DURATION = 200;

    private static final EntityDataAccessor<Integer> PROFESSION =
        SynchedEntityData.defineId(RemnantMob.class, EntityDataSerializers.INT);

    @Nullable
    private UUID angerTarget;
    private int angerTime;
    @Nullable
    private Player tradingPlayer;
    private MerchantOffers offers;
    private int wealth;
    private int villagerXp;
    private int restockTime;
    private boolean restockOffers;
    private boolean sheared;

    public RemnantMob(EntityType<? extends Monster> type, Level level) {
        super(type, level, EliteKind.REMNANT);
    }

    //? if <1.21 {
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(PROFESSION, 0);
    }
    //?} else {
    /*@Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(PROFESSION, 0);
    }
    *///?}

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(4, new OpenDoorGoal(this, true));
        goalSelector.addGoal(8, new WorshipGoal(this, ShoggothBlocks.WORSHIP_TARGETS, 0.5D, 8,
            ModSounds.event("remnant.priest.chant")));
    }

    public void enrage(boolean alertNearby, LivingEntity enemy) {
        if (enemy == null || !enemy.isAlive()) return;
        angerTarget = enemy.getUUID();
        angerTime = 600;
        setTarget(enemy);
        if (alertNearby) {
            for (RemnantMob remnant : level().getEntitiesOfClass(RemnantMob.class,
                    getBoundingBox().inflate(16.0D), Entity::isAlive)) {
                if (remnant != this) remnant.enrage(false, enemy);
            }
        }
    }

    public boolean isAngry() {
        return angerTime > 0;
    }

    public int getProfession() {
        return entityData.get(PROFESSION);
    }

    public void setProfession(int profession) {
        entityData.set(PROFESSION, Math.max(0, Math.min(6, profession)));
        offers = null;
    }

    @Override
    protected String legacyLootTable() {
        return switch (getProfession()) {
            case 1 -> "remnant_librarian";
            case 2 -> "remnant_priest";
            case 3 -> "remnant_blacksmith";
            case 4 -> "remnant_butcher";
            case 5 -> "remnant_banker";
            case 6 -> "remnant_master_blacksmith";
            default -> "remnant";
        };
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        tickRestock();
        if (angerTime > 0) angerTime--;
        if (angerTime == 0) {
            angerTarget = null;
            if (getTarget() != null && !getTarget().isAlive()) setTarget(null);
        } else if (getTarget() == null && angerTarget != null && level() instanceof ServerLevel server
                && server.getEntity(angerTarget) instanceof LivingEntity living && living.isAlive()) {
            setTarget(living);
        }
    }

    private void tickRestock() {
        if (tradingPlayer != null || restockTime <= 0) return;
        if (--restockTime > 0) return;
        if (restockOffers && offers != null) {
            if (offers.size() > 1) {
                for (int index = 0; index < offers.size(); index++) {
                    MerchantOffer candidate = offers.get(index);
                    if (candidate.isOutOfStock() && !isSpiritTabletShard(candidate.getResult())) {
                        int additionalUses = getRandom().nextInt(6) + getRandom().nextInt(6) + 2;
                        offers.set(index, MerchantOfferCompat.increaseMaxUses(candidate, additionalUses));
                    }
                }
            }
            restockOffers = false;
            addDefaultRestockOffer();
            addEffect(new MobEffectInstance(MobEffects.REGENERATION, RESTOCK_REGEN_DURATION));
        }
    }

    private void addDefaultRestockOffer() {
        MerchantOffers generated = createOffers();
        if (offers == null) offers = new MerchantOffers();
        if (generated.isEmpty()) return;
        int start = getRandom().nextInt(generated.size());
        for (int offset = 0; offset < generated.size(); offset++) {
            MerchantOffer candidate = generated.get((start + offset) % generated.size());
            boolean duplicate = offers.stream().anyMatch(existing -> hasSameTradeIds(existing, candidate));
            if (!duplicate) {
                offers.add(candidate);
                return;
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!level().isClientSide && source.getEntity() instanceof LivingEntity attacker) {
            enrage(true, attacker);
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return effect.getEffect() != MobEffects.POISON && super.canBeAffected(effect);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!isAlive() || isAngry() || player.isSecondaryUseActive()) return super.mobInteract(player, hand);
        if (!hasNecronomicon(player)) {
            playSound(ModSounds.event("remnant.no"), 1.0F, 1.0F);
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if (!level().isClientSide) {
            setTradingPlayer(player);
            openTradingScreen(player, getDisplayName(), 1);
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, SpawnGroupData spawnData
                                        //? if <1.21 {
                                        , CompoundTag tag
                                        //?}
    ) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, spawnType, spawnData
            //? if <1.21 {
            , tag
            //?}
        );
        setProfession(getRandom().nextInt(7));
        return result;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("AngerTime", angerTime);
        if (angerTarget != null) tag.putUUID("AngerTarget", angerTarget);
        tag.putInt("Profession", getProfession());
        tag.putInt("Money", wealth);
        tag.putInt("VillagerXp", villagerXp);
        tag.putInt("RestockTime", restockTime);
        tag.putBoolean("RestockOffers", restockOffers);
        tag.putBoolean("Sheared", sheared);
        if (offers != null) tag.put("Offers", MerchantOfferCompat.save(offers));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        angerTime = Math.max(0, tag.getInt("AngerTime"));
        angerTarget = tag.hasUUID("AngerTarget") ? tag.getUUID("AngerTarget") : null;
        entityData.set(PROFESSION, Math.max(0, Math.min(6, tag.getInt("Profession"))));
        wealth = Math.max(0, tag.getInt("Money"));
        villagerXp = Math.max(0, tag.getInt("VillagerXp"));
        restockTime = Math.max(0, tag.getInt("RestockTime"));
        restockOffers = tag.getBoolean("RestockOffers");
        sheared = tag.getBoolean("Sheared");
        offers = tag.contains("Offers") ? MerchantOfferCompat.load(tag.getCompound("Offers")) : null;
    }

    @Override
    public void setTradingPlayer(@Nullable Player player) {
        tradingPlayer = player;
    }

    @Override
    @Nullable
    public Player getTradingPlayer() {
        return tradingPlayer;
    }

    @Override
    public MerchantOffers getOffers() {
        if (offers == null) offers = new MerchantOffers();
        if (offers.isEmpty()) addDefaultRestockOffer();
        return offers;
    }

    @Override
    public void overrideOffers(MerchantOffers offers) {
        this.offers = offers;
    }

    @Override
    public void notifyTrade(MerchantOffer offer) {
        offer.increaseUses();
        ItemStack result = offer.getResult();
        if (result.isDamageableItem()) {
            offer.increaseUses();
            offer.increaseUses();
            offer.increaseUses();
        }
        if (isSpiritTabletShard(result)) offer.increaseUses();
        if (offer.getBaseCostA().is(item("coin"))) wealth += offer.getBaseCostA().getCount();
        if (offers != null && !offers.isEmpty()
            && hasSameTradeIds(offer, offers.get(offers.size() - 1))) {
            restockTime = RESTOCK_DELAY;
            restockOffers = true;
        }
        playSound(ModSounds.event("remnant.yes"), 1.0F, getVoicePitch());
    }

    @Override
    public void notifyTradeUpdated(ItemStack stack) {
        playSound(ModSounds.event(stack.isEmpty() ? "remnant.no" : "remnant.yes"), 1.0F, getVoicePitch());
    }

    @Override
    public int getVillagerXp() {
        return villagerXp;
    }

    @Override
    public void overrideXp(int xp) {
        villagerXp = Math.max(0, xp);
    }

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return ModSounds.event("remnant.yes");
    }

    @Override
    public boolean isClientSide() {
        return level().isClientSide;
    }

    @Override
    public boolean canRestock() {
        return true;
    }

    @Override
    public boolean acIsShearable(Player player, ItemStack stack, Level level, BlockPos pos) {
        return isAlive() && !sheared;
    }

    @Override
    public List<ItemStack> acOnSheared(Player player, ItemStack stack, Level level, BlockPos pos) {
        return shear(player, stack);
    }

    private List<ItemStack> shear(Player player, ItemStack shears) {
        int count = 1 + getRandom().nextInt(3);
        List<ItemStack> drops = new ArrayList<>(count);
        for (int index = 0; index < count; index++) drops.add(new ItemStack(item("eldritch_scale")));
        playSound(SoundEvents.SHEEP_SHEAR, 1.0F, 1.0F);
        playSound(SoundEvents.ITEM_BREAK, 1.0F, 1.0F);
        InteractionHand hand = player.getMainHandItem() == shears ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        ArmorDurabilityCompat.damageHeld(shears, 5, player, hand);
        sheared = true;
        return drops;
    }

    private MerchantOffers createOffers() {
        MerchantOffers generated = new MerchantOffers();
        switch (getProfession()) {
            case 0 -> farmerOffers(generated);
            case 1 -> librarianOffers(generated);
            case 2 -> priestOffers(generated);
            case 3 -> smithOffers(generated, false);
            case 4 -> butcherOffers(generated);
            case 5 -> bankerOffers(generated);
            case 6 -> smithOffers(generated, true);
            default -> farmerOffers(generated);
        }
        return generated;
    }

    private void farmerOffers(MerchantOffers list) {
        buy(list, Items.WHEAT, range(18, 22));
        buy(list, Items.WHITE_WOOL, range(14, 22));
        buy(list, Items.CHICKEN, range(14, 18));
        sell(list, 1, Items.BREAD, range(2, 4));
        sell(list, 1, Items.APPLE, range(4, 8));
        sell(list, 3, Items.SHEARS, 1);
        sell(list, 3, Items.FLINT_AND_STEEL, 1);
        add(list, new ItemStack(Items.GRAVEL, 10), new ItemStack(MiscItems.TOKEN_OF_JZAHAR.get()),
            new ItemStack(Items.FLINT, 4 + getRandom().nextInt(2)), 12);
    }

    private void librarianOffers(MerchantOffers list) {
        buy(list, Items.PAPER, range(24, 36));
        buy(list, Items.BOOK, range(11, 13));
        sell(list, 3, Items.BOOKSHELF, 1);
        sell(list, 10, Items.COMPASS, 1);
        sell(list, 10, Items.CLOCK, 1);
        sell(list, 10, BookItems.NECRONOMICON.get(), 1);
        sell(list, 11, BookItems.ABYSSAL_WASTELAND_NECRONOMICON.get(), 1);
        sell(list, 12, BookItems.DREADLANDS_NECRONOMICON.get(), 1);
        sell(list, 14, BookItems.OMOTHOL_NECRONOMICON.get(), 1);
    }

    private void priestOffers(MerchantOffers list) {
        buy(list, Items.ROTTEN_FLESH, range(16, 28));
        buy(list, MiscItems.CORALIUM_PLAGUED_FLESH.get(), range(16, 28));
        buy(list, item("dread_fragment"), range(16, 28));
        buy(list, MiscItems.OMOTHOL_GHOUL_FLESH.get(), range(32, 60));
        sell(list, 7, Items.ENDER_EYE, 1);
        sell(list, 4, Items.EXPERIENCE_BOTTLE, 1);
        sell(list, 3, Items.GLOWSTONE, 1);
    }

    private void smithOffers(MerchantOffers list, boolean master) {
        buy(list, Items.COAL, range(16, 24));
        buy(list, item("abyssalnite_ingot"), range(8, 10));
        buy(list, item("refined_coralium_ingot"), range(8, 10));
        buy(list, item("dreadium_ingot"), range(8, 10));
        buy(list, item("ethaxium_ingot"), range(4, 6));
        sell(list, 12, item("ethaxium_sword"), 1);
        sell(list, 10, item("ethaxium_pickaxe"), 1);
        sell(list, 9, item("ethaxium_axe"), 1);
        sell(list, 7, item("ethaxium_shovel"), 1);
        sell(list, 7, item("ethaxium_hoe"), 1);
        sell(list, 5, item("ethaxium_boots"), 1);
        sell(list, 5, item("ethaxium_helmet"), 1);
        sell(list, 11, item("ethaxium_chestplate"), 1);
        sell(list, 9, item("ethaxium_leggings"), 1);
        if (master) {
            sellArmorSet(list, "plated_coralium", 5, 11, 9);
            sellArmorSet(list, "dreadium_samurai", 8, 18, 13);
        }
        sellShard(list, MiscItems.SPIRIT_TABLET_SHARD_0.get());
        sellShard(list, MiscItems.SPIRIT_TABLET_SHARD_1.get());
        sellShard(list, MiscItems.SPIRIT_TABLET_SHARD_2.get());
        sellShard(list, MiscItems.SPIRIT_TABLET_SHARD_3.get());
    }

    private void sellArmorSet(MerchantOffers list, String prefix, int small, int chest, int legs) {
        sell(list, small, item(prefix + "_boots"), 1);
        sell(list, small, item(prefix + "_helmet"), 1);
        sell(list, chest, item(prefix + "_chestplate"), 1);
        sell(list, legs, item(prefix + "_leggings"), 1);
    }

    private void sellShard(MerchantOffers list, Item shard) {
        add(list, new ItemStack(MiscItems.TOKEN_OF_JZAHAR.get(), 64), ItemStack.EMPTY,
            new ItemStack(shard), 1);
    }

    private void butcherOffers(MerchantOffers list) {
        buy(list, Items.COAL, range(16, 24));
        buy(list, Items.PORKCHOP, range(14, 18));
        buy(list, Items.BEEF, range(14, 18));
        buy(list, Items.CHICKEN, range(14, 18));
        sell(list, 1, Items.COOKIE, range(7, 10));
        sell(list, 2, Items.GOLDEN_CARROT, 1);
        sell(list, 2, Items.PUMPKIN_PIE, 1);
        sell(list, 3, Items.RABBIT_STEW, 1);
        sell(list, 3, Items.CAKE, 1);
    }

    private void bankerOffers(MerchantOffers list) {
        exchange(list, Items.IRON_INGOT, 3, item("coin"), 6);
        exchange(list, Items.IRON_INGOT, 6, item("coin"), 12);
        exchange(list, Items.GOLD_INGOT, 3, item("coin"), 12);
        exchange(list, Items.GOLD_INGOT, 6, item("coin"), 24);
        exchange(list, Items.EMERALD, 1, item("coin"), 8);
        exchange(list, Items.EMERALD, 2, item("coin"), 16);
        exchange(list, Items.DIAMOND, 1, item("coin"), 32);
        exchange(list, Items.DIAMOND, 2, item("coin"), 64);
        exchange(list, item("coin"), 32, MiscItems.TOKEN_OF_JZAHAR.get(), 1);
        exchange(list, item("coin"), 64, MiscItems.TOKEN_OF_JZAHAR.get(), 2);
    }

    private void buy(MerchantOffers list, Item item, int count) {
        exchange(list, item, count, MiscItems.TOKEN_OF_JZAHAR.get(), 1);
    }

    private void sell(MerchantOffers list, int tokens, Item item, int count) {
        exchange(list, MiscItems.TOKEN_OF_JZAHAR.get(), tokens, item, count);
    }

    private void exchange(MerchantOffers list, Item cost, int costCount, Item result, int resultCount) {
        add(list, new ItemStack(cost, costCount), ItemStack.EMPTY, new ItemStack(result, resultCount), 12);
    }

    private void add(MerchantOffers list, ItemStack first, ItemStack second, ItemStack result, int maxUses) {
        if (first.isEmpty() || result.isEmpty() || first.is(Items.AIR) || result.is(Items.AIR)) return;
        list.add(MerchantOfferCompat.offer(first, second, result, maxUses, 1, 0.05F));
    }

    private int range(int min, int max) {
        return min >= max ? min : min + getRandom().nextInt(max - min + 1);
    }

    private static Item item(String path) {
        return BuiltInRegistries.ITEM.get(ACRef.id(path));
    }

    private static boolean isSpiritTabletShard(ItemStack stack) {
        return stack.is(MiscItems.SPIRIT_TABLET_SHARD_0.get())
            || stack.is(MiscItems.SPIRIT_TABLET_SHARD_1.get())
            || stack.is(MiscItems.SPIRIT_TABLET_SHARD_2.get())
            || stack.is(MiscItems.SPIRIT_TABLET_SHARD_3.get());
    }

    private static boolean hasSameTradeIds(MerchantOffer first, MerchantOffer second) {
        ItemStack firstSecondCost = first.getCostB();
        ItemStack secondSecondCost = second.getCostB();
        return first.getBaseCostA().is(second.getBaseCostA().getItem())
            && first.getResult().is(second.getResult().getItem())
            && (firstSecondCost.isEmpty() && secondSecondCost.isEmpty()
                || !firstSecondCost.isEmpty() && !secondSecondCost.isEmpty()
                    && firstSecondCost.is(secondSecondCost.getItem()));
    }

    private static boolean hasNecronomicon(Player player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            Item item = player.getInventory().getItem(slot).getItem();
            if (BookItems.ALL.stream().anyMatch(book -> book.get() == item)) return true;
        }
        return false;
    }

    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        return getProfession() == 2 && getRandom().nextBoolean()
            ? ModSounds.event("remnant.priest.chant") : null;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.event("shadow.death");
    }
}