package com.shinoow.abyssalcraft.system.spell;

import java.util.ArrayList;
import java.util.List;

import com.shinoow.abyssalcraft.platform.MobEffectCompat;
import com.shinoow.abyssalcraft.platform.LiquidCoraliumCompat;
import com.shinoow.abyssalcraft.platform.PlayerRespawnCompat;
import com.shinoow.abyssalcraft.platform.TamableCompat;
import com.shinoow.abyssalcraft.content.block.deco.DecoBlocks;
import com.shinoow.abyssalcraft.content.block.shoggoth.ShoggothBlocks;
import com.shinoow.abyssalcraft.content.entity.misc.CompassTentacle;
import com.shinoow.abyssalcraft.content.entity.misc.MiscEntities;
import com.shinoow.abyssalcraft.content.entity.boss.ACBossMob;
import com.shinoow.abyssalcraft.content.entity.boss.EliteMob;
import com.shinoow.abyssalcraft.registry.BaseBlocks;
import com.shinoow.abyssalcraft.registry.ModSounds;
import com.shinoow.abyssalcraft.system.energy.IEnergyContainerItem;
import com.shinoow.abyssalcraft.system.effect.ACDamageTypes;
import com.shinoow.abyssalcraft.world.ACDimensions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
//? if <1.21 {
import net.minecraft.world.entity.MobType;
//?} else {
/*import net.minecraft.tags.EntityTypeTags;
*///?}
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Source-faithful server behaviors for the fourteen manifest spells. */
public final class SpellBehaviors {

    private SpellBehaviors() {}

    public static void bootstrap() {
        SpellBehaviorRegistry registry = SpellBehaviorRegistry.instance();
        registry.register("lifedrain", entity(SpellBehaviors::lifeDrain));
        registry.register("graspofcthulhu", entity(SpellBehaviors::grasp));
        registry.register("invisibility", entity(SpellBehaviors::invisibility));
        registry.register("detachment", entity(SpellBehaviors::detachment));
        registry.register("stealvigor", entity(SpellBehaviors::stealVigor));
        registry.register("sirenssong", entity(SpellBehaviors::sirensSong));
        registry.register("undeathtodust", entity(SpellBehaviors::undeathToDust));
        registry.register("teleporthostiles", entity(SpellBehaviors::teleportHostiles));
        registry.register("entropy", block(SpellBehaviors::canEntropy, SpellBehaviors::entropy));
        registry.register("mining", block(SpellBehaviors::canMine, SpellBehaviors::mine));
        registry.register("oozeremoval", self(SpellBehaviors::canRemoveOoze, SpellBehaviors::removeOoze));
        registry.register("floating", self(context -> true, SpellBehaviors::floating));
        registry.register("teleportHome", self(SpellBehaviors::canTeleportHome, SpellBehaviors::teleportHome));
        registry.register("compass", self(context -> context.level().dimension() == ACDimensions.OMOTHOL,
            SpellBehaviors::compass));
    }

    private static SpellBehavior entity(EntityEffect effect) {
        return new SpellBehavior() {
            @Override
            public boolean canCast(ManifestSpell spell, SpellCastContext context) {
                return context.entityTarget() != null && effect.canApply(spell, context, context.entityTarget());
            }

            @Override
            public void cast(ManifestSpell spell, SpellCastContext context) {
                effect.apply(spell, context, context.entityTarget());
            }
        };
    }

    private static SpellBehavior block(java.util.function.BiPredicate<ManifestSpell, SpellCastContext> canCast,
                                       java.util.function.BiConsumer<ManifestSpell, SpellCastContext> cast) {
        return new SpellBehavior() {
            @Override public boolean canCast(ManifestSpell spell, SpellCastContext context) {
                return context.blockTarget() != null && canCast.test(spell, context);
            }
            @Override public void cast(ManifestSpell spell, SpellCastContext context) { cast.accept(spell, context); }
        };
    }

    private static SpellBehavior self(java.util.function.Predicate<SpellCastContext> canCast,
                                      java.util.function.Consumer<SpellCastContext> cast) {
        return new SpellBehavior() {
            @Override public boolean canCast(ManifestSpell spell, SpellCastContext context) {
                return canCast.test(context);
            }
            @Override public void cast(ManifestSpell spell, SpellCastContext context) { cast.accept(context); }
        };
    }

    private static boolean lifeDrain(ManifestSpell spell, SpellCastContext context, LivingEntity target) {
        return target.isAlive();
    }

    private static boolean grasp(ManifestSpell spell, SpellCastContext context, LivingEntity target) {
        return target.isAlive();
    }

    private static boolean invisibility(ManifestSpell spell, SpellCastContext context, LivingEntity target) {
        return !target.hasEffect(MobEffects.INVISIBILITY);
    }

    private static boolean detachment(ManifestSpell spell, SpellCastContext context, LivingEntity target) {
        for (EquipmentSlot slot : armorSlots()) if (!target.getItemBySlot(slot).isEmpty()) return true;
        return false;
    }

    private static boolean stealVigor(ManifestSpell spell, SpellCastContext context, LivingEntity target) {
        return target.isAlive();
    }

    private static boolean sirensSong(ManifestSpell spell, SpellCastContext context, LivingEntity target) {
        return target instanceof TamableAnimal animal && !animal.isOwnedBy(context.caster())
            || target instanceof AbstractHorse horse
                && !context.caster().getUUID().equals(horse.getOwnerUUID());
    }

    private static boolean undeathToDust(ManifestSpell spell, SpellCastContext context, LivingEntity target) {
        return
            //? if <1.21 {
            target.getMobType() == MobType.UNDEAD
            //?} else {
            /*target.getType().is(EntityTypeTags.UNDEAD)
            *///?}
            && !isBoss(target);
    }

    private static boolean teleportHostiles(ManifestSpell spell, SpellCastContext context, LivingEntity target) {
        return target instanceof Animal animal && animal.isBaby();
    }

    private static boolean canEntropy(ManifestSpell spell, SpellCastContext context) {
        BlockPos pos = context.blockTarget().getBlockPos();
        return context.level().mayInteract(context.caster(), pos)
            && entropyResult(context.level().getBlockState(pos)) != null;
    }

    private static void entropy(ManifestSpell spell, SpellCastContext context) {
        BlockPos pos = context.blockTarget().getBlockPos();
        BlockState result = entropyResult(context.level().getBlockState(pos));
        if (result == null) return;
        context.level().setBlock(pos, result, 3);
        if (context.energySource().getItem() instanceof IEnergyContainerItem energy) {
            energy.addEnergy(context.energySource(), 5 + context.level().random.nextInt(5));
        }
    }

    private static BlockState entropyResult(BlockState state) {
        if (state.is(BaseBlocks.DARKSTONE.get())) return BaseBlocks.DARKSTONE_COBBLESTONE.get().defaultBlockState();
        if (state.is(BaseBlocks.ABYSSAL_STONE.get())) return BaseBlocks.ABYSSAL_COBBLESTONE.get().defaultBlockState();
        if (state.is(BaseBlocks.CORALIUM_STONE.get())) return BaseBlocks.CORALIUM_COBBLESTONE.get().defaultBlockState();
        if (state.is(BaseBlocks.DREADSTONE.get())) return BaseBlocks.DREADSTONE_COBBLESTONE.get().defaultBlockState();
        if (state.is(BaseBlocks.ELYSIAN_STONE.get())) return BaseBlocks.ELYSIAN_COBBLESTONE.get().defaultBlockState();
        if (state.is(Blocks.STONE)) return Blocks.COBBLESTONE.defaultBlockState();
        if (state.is(Blocks.COBBLESTONE) || state.is(BaseBlocks.DARKSTONE_COBBLESTONE.get())
            || state.is(BaseBlocks.ABYSSAL_COBBLESTONE.get()) || state.is(BaseBlocks.CORALIUM_COBBLESTONE.get())
            || state.is(BaseBlocks.DREADSTONE_COBBLESTONE.get()) || state.is(BaseBlocks.ELYSIAN_COBBLESTONE.get())) {
            return Blocks.GRAVEL.defaultBlockState();
        }
        if (state.is(Blocks.GRAVEL)) return Blocks.SAND.defaultBlockState();
        if (state.is(Blocks.SAND)) return Blocks.AIR.defaultBlockState();
        return null;
    }

    private static boolean canMine(ManifestSpell spell, SpellCastContext context) {
        BlockPos hit = context.blockTarget().getBlockPos();
        return context.level().mayInteract(context.caster(), hit)
            && (miningInner(context.level().getBlockState(hit)) != null
                || miningOuter(context.level().getBlockState(hit)) != null);
    }

    private static void mine(ManifestSpell spell, SpellCastContext context) {
        BlockHitResult hit = context.blockTarget();
        Direction direction = hit.getDirection().getOpposite();
        int quality = context.quality().quality() + 1;
        float budget = 500.0F * quality + (context.quality().quality() >= ScrollType.GREATER.quality() ? 1000.0F : 0.0F);
        int depth = 128 * quality + (context.quality().quality() >= ScrollType.GREATER.quality() ? 256 : 0);
        float spent = 0;
        for (int layer = 0; layer < depth && spent < budget; layer++) {
            BlockPos center = hit.getBlockPos().relative(direction, layer);
            int radius = quality;
            for (int first = -radius; first <= radius && spent < budget; first++) {
                for (int second = -radius; second <= radius && spent < budget; second++) {
                    BlockPos pos = plane(center, direction.getAxis(), first, second);
                    spent += transformMiningBlock(context, pos, true);
                }
            }
            int outerRadius = radius + 1;
            for (int first = -outerRadius; first <= outerRadius && spent < budget; first++) {
                for (int second = -outerRadius; second <= outerRadius && spent < budget; second++) {
                    if (Math.abs(first) <= radius && Math.abs(second) <= radius) continue;
                    BlockPos pos = plane(center, direction.getAxis(), first, second);
                    spent += transformMiningBlock(context, pos, false);
                }
            }
        }
    }

    private static float transformMiningBlock(SpellCastContext context, BlockPos pos, boolean inner) {
        if (!context.level().mayInteract(context.caster(), pos)) return 0.0F;
        BlockState state = context.level().getBlockState(pos);
        BlockState result = inner ? miningInner(state) : miningOuter(state);
        if (result == null) return 0.0F;
        float hardness = Math.max(0.0F, state.getDestroySpeed(context.level(), pos));
        context.level().setBlock(pos, result, 3);
        return hardness * (state.getFluidState().isEmpty() ? 2.0F : 0.5F);
    }

    private static BlockPos plane(BlockPos center, Direction.Axis axis, int first, int second) {
        return switch (axis) {
            case X -> center.offset(0, first, second);
            case Y -> center.offset(first, 0, second);
            case Z -> center.offset(first, second, 0);
        };
    }

    private static BlockState miningInner(BlockState state) {
        if (state.is(Blocks.STONE) || state.is(Blocks.DIRT) || state.is(BaseBlocks.DARKSTONE.get())
            || state.is(BaseBlocks.ABYSSAL_STONE.get()) || state.is(BaseBlocks.DREADSTONE.get())
            || state.is(BaseBlocks.ELYSIAN_STONE.get()) || state.is(BaseBlocks.OMOTHOL_STONE.get())
            || state.is(BaseBlocks.MONOLITH_STONE.get()) || state.is(Blocks.GRAVEL)
            || state.is(Blocks.SANDSTONE) || state.is(DecoBlocks.DREADLANDS_DIRT.get())) {
            return Blocks.MAGMA_BLOCK.defaultBlockState();
        }
        if (!state.getFluidState().isEmpty()) return Blocks.AIR.defaultBlockState();
        if (state.is(BaseBlocks.CORALIUM_STONE.get())) return LiquidCoraliumCompat.BLOCK.get().defaultBlockState();
        return miningOuter(state);
    }

    private static BlockState miningOuter(BlockState state) {
        if (state.is(Blocks.COBBLESTONE)) return Blocks.STONE.defaultBlockState();
        if (state.is(BaseBlocks.DARKSTONE_COBBLESTONE.get())) return BaseBlocks.DARKSTONE.get().defaultBlockState();
        if (state.is(BaseBlocks.ABYSSAL_COBBLESTONE.get())) return BaseBlocks.ABYSSAL_STONE.get().defaultBlockState();
        if (state.is(BaseBlocks.CORALIUM_COBBLESTONE.get())) return BaseBlocks.CORALIUM_STONE.get().defaultBlockState();
        if (state.is(BaseBlocks.DREADSTONE_COBBLESTONE.get())) return BaseBlocks.DREADSTONE.get().defaultBlockState();
        if (state.is(BaseBlocks.ELYSIAN_COBBLESTONE.get())) return BaseBlocks.ELYSIAN_STONE.get().defaultBlockState();
        if (state.is(Blocks.SAND)) return Blocks.GLASS.defaultBlockState();
        if (state.is(Blocks.RED_SAND)) return Blocks.RED_STAINED_GLASS.defaultBlockState();
        if (state.is(DecoBlocks.ABYSSAL_SAND.get())) return DecoBlocks.ABYSSAL_SAND_GLASS.get().defaultBlockState();
        return null;
    }

    private static boolean canRemoveOoze(SpellCastContext context) {
        int distance = 6 * (context.quality().quality() + 1);
        return BlockPos.betweenClosedStream(context.caster().blockPosition().offset(-distance, -distance, -distance),
            context.caster().blockPosition().offset(distance, distance, distance))
            .anyMatch(pos -> context.level().getBlockState(pos).is(ShoggothBlocks.SHOGGOTH_OOZE.get()));
    }

    private static void removeOoze(SpellCastContext context) {
        int distance = 6 * (context.quality().quality() + 1);
        BlockPos.betweenClosedStream(context.caster().blockPosition().offset(-distance, -distance, -distance),
            context.caster().blockPosition().offset(distance, distance, distance))
            .filter(pos -> context.level().getBlockState(pos).is(ShoggothBlocks.SHOGGOTH_OOZE.get()))
            .forEach(pos -> context.level().removeBlock(pos, false));
    }

    private static void floating(SpellCastContext context) {
        Vec3 movement = context.caster().getDeltaMovement();
        context.caster().setDeltaMovement(movement.x, 0.119D, movement.z);
        context.caster().resetFallDistance();
        context.caster().hurtMarked = true;
    }

    private static boolean canTeleportHome(SpellCastContext context) {
        return context.caster().getRespawnPosition() != null
            && context.caster().getServer().getLevel(context.caster().getRespawnDimension()) != null;
    }

    private static void teleportHome(SpellCastContext context) {
        if (!PlayerRespawnCompat.teleportHome(context.caster())) {
            throw new IllegalStateException("Player respawn target became unavailable");
        }
    }

    private static void compass(SpellCastContext context) {
        CompassTentacle tentacle = MiscEntities.COMPASS_TENTACLE.get().create(context.level());
        if (tentacle == null) throw new IllegalStateException("Unable to create Compass Tentacle");
        tentacle.moveTo(context.caster().getX(), context.caster().getY(), context.caster().getZ());
        if (!context.level().addFreshEntity(tentacle)) throw new IllegalStateException("Unable to spawn Compass Tentacle");
        context.level().playSound(null, context.caster().blockPosition(), ModSounds.event("misc.compass"),
            SoundSource.PLAYERS, 3.0F, 1.0F);
        context.caster().displayClientMessage(net.minecraft.network.chat.Component.literal("Ftah J'zahar fhtagn, nog"), false);
    }

    private interface EntityEffect {
        boolean canApply(ManifestSpell spell, SpellCastContext context, LivingEntity target);

        default void apply(ManifestSpell spell, SpellCastContext context, LivingEntity target) {
            switch (spell.id()) {
                case "lifedrain" -> {
                    float amount = 5.0F + 2.5F * context.quality().quality();
                    if (target.hurt(ACDamageTypes.attributedSource(context.caster(), ACDamageTypes.SPELL), amount)) {
                        context.caster().heal(amount);
                        particleLine(context.level(), context.caster(), target);
                    }
                }
                case "graspofcthulhu" -> {
                    target.setDeltaMovement(Vec3.ZERO);
                    target.setSprinting(false);
                    target.addEffect(MobEffectCompat.vanillaEffect(MobEffects.MOVEMENT_SLOWDOWN, 10, 14));
                    if (target.tickCount % 20 == 0 && target.getHealth() > 1.0F) {
                        target.hurt(ACDamageTypes.attributedSource(context.caster(), ACDamageTypes.SPELL),
                            Math.max(0, context.quality().quality()));
                    }
                }
                case "invisibility" -> target.addEffect(
                    MobEffectCompat.vanillaEffect(MobEffects.INVISIBILITY, 6000, 0));
                case "detachment" -> detach(context, target);
                case "stealvigor" -> vigor(context, target);
                case "sirenssong" -> tame(context, target);
                case "undeathtodust" -> {
                    target.discard();
                    context.level().sendParticles(ParticleTypes.EXPLOSION, target.getX(), target.getY() + 2.0D,
                        target.getZ(), 1, 0, 0, 0, 0);
                }
                case "teleporthostiles" -> teleportHostilesEffect(context, target);
                default -> throw new IllegalStateException("Unknown entity spell " + spell.id());
            }
        }
    }

    private static void detach(SpellCastContext context, LivingEntity target) {
        List<EquipmentSlot> occupied = new ArrayList<>();
        for (EquipmentSlot slot : armorSlots()) if (!target.getItemBySlot(slot).isEmpty()) occupied.add(slot);
        int casts = context.quality().quality() > ScrollType.MODERATE.quality() ? 2 : 1;
        for (int count = 0; count < casts && !occupied.isEmpty(); count++) {
            EquipmentSlot slot = occupied.remove(context.level().random.nextInt(occupied.size()));
            ItemStack removed = target.getItemBySlot(slot);
            target.setItemSlot(slot, ItemStack.EMPTY);
            target.spawnAtLocation(removed);
        }
    }

    private static void vigor(SpellCastContext context, LivingEntity target) {
        int amplifier = context.quality().quality() + 1;
        int duration = 600 + 600 * amplifier;
        target.addEffect(MobEffectCompat.vanillaEffect(MobEffects.WEAKNESS, duration, amplifier));
        target.addEffect(MobEffectCompat.vanillaEffect(MobEffects.MOVEMENT_SLOWDOWN, duration, amplifier));
        target.addEffect(MobEffectCompat.vanillaEffect(MobEffects.DIG_SLOWDOWN, duration, amplifier));
        context.caster().addEffect(MobEffectCompat.vanillaEffect(MobEffects.DAMAGE_BOOST, duration, amplifier));
        context.caster().addEffect(MobEffectCompat.vanillaEffect(MobEffects.MOVEMENT_SPEED, duration, amplifier));
        context.caster().addEffect(MobEffectCompat.vanillaEffect(MobEffects.DIG_SPEED, duration, amplifier));
    }

    private static void tame(SpellCastContext context, LivingEntity target) {
        if (target instanceof TamableAnimal animal) animal.tame(context.caster());
        else if (target instanceof AbstractHorse horse) TamableCompat.tame(horse, context.caster());
        target.setHealth(target.getMaxHealth());
    }

    private static void teleportHostilesEffect(SpellCastContext context, LivingEntity target) {
        if (!target.hurt(ACDamageTypes.attributedSource(context.caster(), ACDamageTypes.SPELL), 200.0F)) return;
        AABB area = context.caster().getBoundingBox().inflate(50.0D);
        for (LivingEntity mob : context.level().getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity.getType().getCategory() == MobCategory.MONSTER && entity.isAlive())) {
            teleportRandomly(context.level(), context.caster(), mob);
        }
    }

    private static boolean teleportRandomly(ServerLevel level, LivingEntity caster, LivingEntity entity) {
        for (int attempt = 0; attempt < 20; attempt++) {
            double x = entity.getX() + (level.random.nextDouble() - 0.5D) * 96.0D;
            double y = entity.getY() + level.random.nextInt(64) - 32;
            double z = entity.getZ() + (level.random.nextDouble() - 0.5D) * 96.0D;
            if (caster.distanceToSqr(x, y, z) >= 2500.0D && entity.randomTeleport(x, y, z, true)) {
                level.playSound(null, entity.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
                    SoundSource.PLAYERS, 1.0F, 1.0F);
                return true;
            }
        }
        return false;
    }

    private static List<EquipmentSlot> armorSlots() {
        return List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
    }

    private static void particleLine(ServerLevel level, LivingEntity caster, LivingEntity target) {
        Vec3 start = caster.getEyePosition();
        Vec3 delta = target.getEyePosition().subtract(start);
        int points = Math.max(1, (int) (delta.length() * 15.0D));
        for (int index = 0; index < points; index++) {
            Vec3 point = start.add(delta.scale(index / (double) points));
            level.sendParticles(ParticleTypes.FLAME, point.x, point.y, point.z, 1, 0, 0, 0, 0);
        }
    }

    private static boolean isBoss(LivingEntity target) {
        return target instanceof ACBossMob || target instanceof EliteMob
            || target instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon
            || target instanceof net.minecraft.world.entity.boss.wither.WitherBoss;
    }
}