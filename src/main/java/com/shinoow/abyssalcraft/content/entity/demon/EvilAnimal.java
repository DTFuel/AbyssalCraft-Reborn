package com.shinoow.abyssalcraft.content.entity.demon;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.shinoow.abyssalcraft.content.entity.base.ACMob;
import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.content.entity.shoggoth.AbstractShoggoth;
import com.shinoow.abyssalcraft.net.ACNetwork;
import com.shinoow.abyssalcraft.net.client.EvilSheepMessage;
import com.shinoow.abyssalcraft.platform.ShearableCompat;

/**
 * Evil animal (owned by PD-4, Stage D2a) -- a hostile farm animal (ported from 1.12.2
 * {@code EntityEvilAnimal} + its four subclasses) that, when it dies, transforms into the matching
 * {@link DemonAnimal} (the signature "evil begets demon" mechanic). Shares the fire-immune melee body of
 * {@link DemonAnimal}; species looks/sounds/health come from {@link AnimalKind}.
 *
 * <p>The death transform honors its config and the Shoggoth-kill exception; shearing always transforms,
 * matching the legacy distinction between death and shearing paths.
 */
public class EvilAnimal extends ACMob implements ShearableCompat {

    private final AnimalKind kind;
    private boolean convertedByShearing;
    private UUID killedPlayerId;
    private String killedPlayerName = "";

    public EvilAnimal(EntityType<? extends Monster> type, Level level, AnimalKind kind) {
        super(type, level);
        this.kind = kind;
    }

    public AnimalKind kind() {
        return kind;
    }

    public void setKilledPlayer(UUID playerId, String playerName) {
        killedPlayerId = playerId;
        killedPlayerName = playerName == null ? "" : playerName;
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt && kind == AnimalKind.SHEEP && target instanceof Player player && !player.isAlive()) {
            setKilledPlayer(player.getUUID(), player.getGameProfile().getName());
            syncKilledPlayer();
        }
        return hurt;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && kind == AnimalKind.SHEEP && killedPlayerId != null && tickCount % 100 == 0) {
            syncKilledPlayer();
        }
    }

    private void syncKilledPlayer() {
        if (!(level() instanceof ServerLevel server) || killedPlayerId == null) return;
        for (net.minecraft.server.level.ServerPlayer player : server.players()) {
            if (player.distanceToSqr(this) <= 16384.0D) {
                ACNetwork.sendToPlayer(player, new EvilSheepMessage(killedPlayerId, killedPlayerName, getId()));
            }
        }
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (killedPlayerId != null) tag.putUUID("PlayerUUID", killedPlayerId);
        if (!killedPlayerName.isEmpty()) tag.putString("PlayerName", killedPlayerName);
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        killedPlayerId = tag.hasUUID("PlayerUUID") ? tag.getUUID("PlayerUUID") : null;
        killedPlayerName = tag.getString("PlayerName");
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, true));
        goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return kind.ambient();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return kind.death();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.GHAST_HURT;
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (!super.checkSpawnRules(level, spawnType)) return false;
        if (!com.shinoow.abyssalcraft.config.ACConfig.evilAnimalNewMoonSpawning.get()) return true;
        return level instanceof Level actual && actual.isNight()
            && actual.dimensionType().moonPhase(actual.getDayTime()) == 0;
    }

    @Override
    public void die(DamageSource source) {
        boolean transform = !isDeadOrDying() && !convertedByShearing
            && ACConfig.demonAnimalsSpawnOnDeath.get()
            && !(source.getEntity() instanceof AbstractShoggoth)
            && level() instanceof ServerLevel;
        super.die(source);
        if (transform) convertToDemon((ServerLevel) level());
    }

    @Override
    public boolean acIsShearable(Player player, ItemStack item, Level level, net.minecraft.core.BlockPos pos) {
        return isAlive();
    }

    @Override
    public List<ItemStack> acOnSheared(Player player, ItemStack item, Level level,
                                      net.minecraft.core.BlockPos pos) {
        return shear(player);
    }

    private List<ItemStack> shear(Player player) {
        int count = 1 + getRandom().nextInt(3);
        List<ItemStack> drops = new ArrayList<>(count);
        for (int index = 0; index < count; index++) drops.add(new ItemStack(shearingDrop()));
        playSound(SoundEvents.SHEEP_SHEAR, 1.0F, 1.0F);
        playSound(SoundEvents.GHAST_HURT, 1.0F, 0.2F);
        if (level() instanceof ServerLevel server) {
            convertedByShearing = true;
            convertToDemon(server);
            discard();
        }
        return drops;
    }

    private net.minecraft.world.item.Item shearingDrop() {
        return switch (kind) {
            case CHICKEN -> Items.FEATHER;
            case COW -> Items.LEATHER;
            case PIG -> Items.PORKCHOP;
            case SHEEP -> Items.WHITE_WOOL;
        };
    }

    private void convertToDemon(ServerLevel server) {
        DemonAnimal demon = DemonEntities.demonType(kind).get().create(server);
        if (demon != null) {
            demon.moveTo(getX(), getY(), getZ(), getYRot(), getXRot());
            //? if <1.21 {
            demon.finalizeSpawn(server, server.getCurrentDifficultyAt(blockPosition()),
                MobSpawnType.CONVERSION, null, null);
            //?} else {
            /*demon.finalizeSpawn(server, server.getCurrentDifficultyAt(blockPosition()),
                MobSpawnType.CONVERSION, null);
            *///?}
            server.addFreshEntity(demon);
        }
    }
}
