package com.shinoow.abyssalcraft.client.render.entity.boss;

import java.util.Optional;
import java.util.function.Function;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

import com.shinoow.abyssalcraft.content.entity.boss.BossMob;

import software.bernie.geckolib.cache.object.GeoBone;

/**
 * Faithful procedural ports of the 1.12.2 boss model {@code setRotationAngles} animations (owned by
 * PE-4b), applied to the GeckoLib bones each frame from {@code GeoModel.setCustomAnimations}.
 *
 * <p><b>Fork-free.</b> It only reads vanilla entity state ({@code walkAnimation}, head rotation) and the
 * version-agnostic {@link GeoBone} setters (verified identical across GeckoLib 4.8.4 / 4.9.2); the single
 * {@code AnimationState} package fork stays in the tiny {@code setCustomAnimations} overrides that call in
 * here.
 *
 * <p><b>Axis convention</b> (user-calibrated on the biped walkers). GeckoLib's bone axes are reversed
 * relative to the 1.12.2 {@code ModelRenderer} on <b>both</b> X and Y (the geo Y-flip inverts the runtime
 * rotation handedness), so an absolute 1.12.2 {@code part.rotateAngleX/Y = v} maps to
 * {@code bone.setRotX(-v)} / {@code bone.setRotY(-v)} (Z untested, assume kept). Symmetric oscillations
 * (arm / leg / tentacle swings, {@code cos}/{@code sin} around 0) look identical under a sign flip, so those
 * keep the 1.12.2 formula verbatim; only absolute rotations (head yaw/pitch tracking) need the negation.
 * GeckoLib does <b>not</b> reset bones before {@code setCustomAnimations}, so an absolute {@code setRotX(v)}
 * never accumulates but drops the geo rest pose; the additive helpers add the oscillation on top of the fixed
 * {@code getInitialSnapshot().getRotX()} rest instead, which both preserves the rest and avoids accumulation.
 */
public final class BossAnimations {

    private static final float DEG_TO_RAD = (float) Math.PI / 180.0F;
    private static final float PI = (float) Math.PI;

    private BossAnimations() {}

    /** Entry point: compute the vanilla animation inputs from the entity, then dispatch per boss id. */
    public static void apply(String id, Function<String, Optional<GeoBone>> bones, LivingEntity entity,
                             float partialTick) {
        float limbSwing = entity.walkAnimation.position(partialTick);
        float limbSwingAmount = Math.min(entity.walkAnimation.speed(partialTick), 1.0F);
        float age = entity.tickCount + partialTick;
        float bodyYaw = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        float headYaw = Mth.rotLerp(partialTick, entity.yHeadRotO, entity.yHeadRot);
        float netHeadYaw = Mth.wrapDegrees(headYaw - bodyYaw);
        float headPitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        int idMod = Math.floorMod(entity.getId(), 10);
        float attack = entity.getAttackAnim(partialTick);

        switch (id) {
            // Vanilla-biped walkers (ModelSkeletonGoliath; Dreadguard reused a vanilla ModelZombie).
            case "gskeleton" -> biped(bones, "head", "rightarm", "leftarm", "rightleg", "leftleg",
                    limbSwing, limbSwingAmount, netHeadYaw, headPitch, 1.0F);
            case "dreadguard" -> biped(bones, "head", "rightArm", "leftArm", "rightLeg", "leftLeg",
                    limbSwing, limbSwingAmount, netHeadYaw, headPitch, 1.0F);
            case "remnant" -> remnant(bones, limbSwing, limbSwingAmount, netHeadYaw, headPitch, age, idMod);
            case "jzaharminion" -> jzaharMinion(bones, limbSwing, limbSwingAmount, netHeadYaw, headPitch, age, idMod);
            case "shadowboss" -> sacthoth(bones, limbSwing, limbSwingAmount, netHeadYaw, headPitch, age, idMod, attack);
            case "chagarothfist" -> headTrack(bones, "eye", netHeadYaw, netHeadYaw);
            case "chagarothspawn" -> chagarothSpawn(bones, limbSwing, limbSwingAmount);
                case "jzahar" -> jzahar(bones, netHeadYaw, headPitch, age, attack,
                    entity instanceof BossMob boss ? boss.getACDeathTime() : 0);
                case "chagaroth" -> chagaroth(bones, limbSwingAmount, netHeadYaw, headPitch,
                    entity instanceof BossMob boss ? boss.getACDeathTime() : 0);
            case "shuboffspring" -> shubOffspring(bones, limbSwing, limbSwingAmount, netHeadYaw, headPitch, age);
            default -> { }
        }
    }

    // ---- shared helpers ----

    /** Absolute head/eye tracking (rest pose is ~0). Both axes negated (GeckoLib is reversed vs 1.12.2). */
    private static void headTrack(Function<String, Optional<GeoBone>> bones, String name, float yawDeg, float pitchDeg) {
        bones.apply(name).ifPresent(b -> {
            b.setRotY(-yawDeg * DEG_TO_RAD);
            b.setRotX(-pitchDeg * DEG_TO_RAD);
        });
    }

    /** Additive rotations: add the 1.12.2 oscillation on top of the bone's fixed geo rest pose (the initial
     *  snapshot -- NOT the live value, which GeckoLib does not reset before setCustomAnimations, so using it
     *  would accumulate every frame and spin the bone). */
    private static void addX(Function<String, Optional<GeoBone>> bones, String name, float v) {
        bones.apply(name).ifPresent(b -> b.setRotX(b.getInitialSnapshot().getRotX() + v));
    }

    private static void addY(Function<String, Optional<GeoBone>> bones, String name, float v) {
        bones.apply(name).ifPresent(b -> b.setRotY(b.getInitialSnapshot().getRotY() + v));
    }

    private static void addZ(Function<String, Optional<GeoBone>> bones, String name, float v) {
        bones.apply(name).ifPresent(b -> b.setRotZ(b.getInitialSnapshot().getRotZ() + v));
    }

    private static void addXZ(Function<String, Optional<GeoBone>> bones, String name, float x, float z) {
        bones.apply(name).ifPresent(b -> {
            b.setRotX(b.getInitialSnapshot().getRotX() + x);
            b.setRotZ(b.getInitialSnapshot().getRotZ() + z);
        });
    }

    /** Additive vertical position offset on the bone's geo rest (1.12.2 rotationPointY bob). */
    private static void posY(Function<String, Optional<GeoBone>> bones, String name, float v) {
        bones.apply(name).ifPresent(b -> b.setPosY(b.getInitialSnapshot().getOffsetY() + v));
    }

    private static void hidden(Function<String, Optional<GeoBone>> bones, String name, boolean hidden) {
        bones.apply(name).ifPresent(b -> {
            b.setHidden(hidden);
            b.setChildrenHidden(hidden);
        });
    }

    private static void resetRotation(Function<String, Optional<GeoBone>> bones, String name) {
        bones.apply(name).ifPresent(b -> {
            b.setRotX(b.getInitialSnapshot().getRotX());
            b.setRotY(b.getInitialSnapshot().getRotY());
            b.setRotZ(b.getInitialSnapshot().getRotZ());
        });
    }

    /** entityId-randomised idle tentacle sway (1.12.2 ModelRemnant / ModelSacthoth pattern). */
    private static void idleTent(Function<String, Optional<GeoBone>> bones, String name, float age, float speed,
                                 boolean sinForX, float ampXdeg, float ampZdeg) {
        float v = age * speed;
        float x = (sinForX ? Mth.sin(v) : Mth.cos(v)) * ampXdeg * DEG_TO_RAD;
        float z = (sinForX ? Mth.cos(v) : Mth.sin(v)) * ampZdeg * DEG_TO_RAD;
        addXZ(bones, name, x, z);
    }

    /** Vanilla biped walk (head tracks; arms + legs swing opposite) -- 1.12.2 ModelSkeletonGoliath math. */
    private static void biped(Function<String, Optional<GeoBone>> bones, String head, String rightArm,
                              String leftArm, String rightLeg, String leftLeg, float limbSwing,
                              float limbSwingAmount, float netHeadYaw, float headPitch, float legFactor) {
        headTrack(bones, head, netHeadYaw, headPitch);
        float swing = limbSwing * 0.6662F;
        addX(bones, rightArm, Mth.cos(swing + PI) * 2.0F * limbSwingAmount * 0.5F);
        addX(bones, leftArm, Mth.cos(swing) * 2.0F * limbSwingAmount * 0.5F);
        addX(bones, rightLeg, Mth.cos(swing) * 1.4F * limbSwingAmount * legFactor);
        addX(bones, leftLeg, Mth.cos(swing + PI) * 1.4F * limbSwingAmount * legFactor);
    }

    // ---- per-boss ----

    /** Remnant: biped (half-amplitude legs) + idle face tentacles + walking leg-tentacles + skirt flap. 1.12.2 ModelRemnant. */
    private static void remnant(Function<String, Optional<GeoBone>> bones, float limbSwing, float limbSwingAmount,
                                float netHeadYaw, float headPitch, float age, int idMod) {
        biped(bones, "head", "rightarm", "leftarm", "rightLegJoint", "leftLegJoint",
                limbSwing, limbSwingAmount, netHeadYaw, headPitch, 0.5F);
        float speed = 0.03F * idMod;
        idleTent(bones, "tentacle1", age, speed, false, 10.5F, 6.5F);
        idleTent(bones, "tentacle2", age, speed, true, 10.5F, 6.5F);
        idleTent(bones, "tentacle3", age, speed, true, 10.5F, 6.5F);
        idleTent(bones, "tentacle4", age, speed, false, 10.5F, 6.5F);
        // Walking leg-tentacles (X + Z; right side Z+, left side Z-), 1.12.2 leftLegB/rightLegB.
        float swing = limbSwing * 0.6662F;
        float a = Mth.cos(swing) * 1.4F * limbSwingAmount;
        float b = Mth.cos(swing + PI) * 1.4F * limbSwingAmount;
        addXZ(bones, "rightLegB1", a, a); addXZ(bones, "rightLegB2", b, b);
        addXZ(bones, "rightLegB3", b, b); addXZ(bones, "rightLegB4", a, a);
        addXZ(bones, "leftLegB1", a, -a); addXZ(bones, "leftLegB2", b, -b);
        addXZ(bones, "leftLegB3", b, -b); addXZ(bones, "leftLegB4", a, -a);
        // Skirt tentacles flapping while moving (Y splay + X wave), 1.12.2 leftLegT/rightLegT.
        float flapY = Mth.sin(age * 0.2F) * 0.3F * 10.5F * DEG_TO_RAD;
        float flapX = Mth.cos(age * 0.2F) * 0.4F * 6.5F * DEG_TO_RAD * limbSwingAmount;
        skirt(bones, "leftLegT1", flapY + 0.3F, flapX, limbSwingAmount);
        skirt(bones, "leftLegT2", flapY - 0.3F, flapX, limbSwingAmount);
        skirt(bones, "leftLegT3", flapY - 0.3F, flapX, limbSwingAmount);
        skirt(bones, "leftLegT4", flapY + 0.3F, flapX, limbSwingAmount);
        skirt(bones, "rightLegT1", flapY - 0.3F, flapX, limbSwingAmount);
        skirt(bones, "rightLegT2", flapY + 0.3F, flapX, limbSwingAmount);
        skirt(bones, "rightLegT3", flapY + 0.3F, flapX, limbSwingAmount);
        skirt(bones, "rightLegT4", flapY - 0.3F, flapX, limbSwingAmount);
    }

    /** A skirt leg-tentacle: Y splay (scaled by movement) + X wave. */
    private static void skirt(Function<String, Optional<GeoBone>> bones, String name, float y, float x, float lsa) {
        addY(bones, name, y * lsa);
        addX(bones, name, x);
    }

    /** Gatekeeper Minion: head + arm swing, idle face tentacles, walking leg-tentacles. 1.12.2 ModelGatekeeperMinion. */
    private static void jzaharMinion(Function<String, Optional<GeoBone>> bones, float limbSwing,
                                     float limbSwingAmount, float netHeadYaw, float headPitch, float age, int idMod) {
        headTrack(bones, "head", netHeadYaw, headPitch);
        float swing = limbSwing * 0.6662F;
        addX(bones, "rightarm1", Mth.cos(swing + PI) * 2.0F * limbSwingAmount * 0.5F);
        addX(bones, "leftarm1", Mth.cos(swing) * 2.0F * limbSwingAmount * 0.5F);
        float speed = 0.03F * idMod;
        idleTent(bones, "tentacle1", age, speed, false, 10.5F, 6.5F);
        idleTent(bones, "tentacle2", age, speed, true, 10.5F, 6.5F);
        idleTent(bones, "tentacle3", age, speed, true, 10.5F, 6.5F);
        idleTent(bones, "tentacle4", age, speed, false, 10.5F, 6.5F);
        float a = Mth.cos(swing) * 1.4F * limbSwingAmount;
        float b = Mth.cos(swing + PI) * 1.4F * limbSwingAmount;
        addXZ(bones, "rltentacle1", a, a);
        addXZ(bones, "rltentacle2", b, b);
        addXZ(bones, "rltentacle3", b, b);
        addXZ(bones, "rltentacle4", a, a);
        addXZ(bones, "lltentacle1", a, -a);
        addXZ(bones, "lltentacle2", b, -b);
        addXZ(bones, "lltentacle3", b, -b);
        addXZ(bones, "lltentacle4", a, -a);
    }

    /** Sacthoth (shadowboss): biped walk + five idle tentacles + a left-arm melee swing. 1.12.2 ModelSacthoth. */
    private static void sacthoth(Function<String, Optional<GeoBone>> bones, float limbSwing, float limbSwingAmount,
                                 float netHeadYaw, float headPitch, float age, int idMod, float attack) {
        biped(bones, "head", "rightarm1", "leftarm1", "rightleg", "leftleg",
                limbSwing, limbSwingAmount, netHeadYaw, headPitch, 1.0F);
        idleTent(bones, "tentacle1", age, 0.01F * idMod, true, 4.5F, 2.5F);
        idleTent(bones, "tentacle2", age, 0.02F * idMod, true, 4.5F, 2.5F);
        idleTent(bones, "tentacle3", age, 0.03F * idMod, true, 4.5F, 2.5F);
        idleTent(bones, "tentacle4", age, 0.04F * idMod, true, 4.5F, 2.5F);
        idleTent(bones, "tentacle5", age, 0.04F * idMod, true, 4.5F, 2.5F);
        if (attack > 0.0F) {
            float bodyY = Mth.sin(Mth.sqrt(limbSwingAmount) * PI * 2.0F) * 0.2F;
            addY(bones, "rightarm1", bodyY);
            float ease = 1.0F - attack;
            ease = 1.0F - ease * ease * ease * ease;
            float f13 = Mth.sin(attack * PI) * -(-headPitch * DEG_TO_RAD - 0.7F) * 0.75F;
            float walkLeft = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;
            addX(bones, "leftarm1", walkLeft - (Mth.sin(ease * PI) * 1.2F + f13));
            addY(bones, "leftarm1", bodyY * 3.0F);
            addZ(bones, "leftarm1", Mth.sin(attack * PI) * -0.4F);
        }
    }

    /** Spawn of Chagaroth: six-legged crawl (legs swing on Y). 1.12.2 ModelChagarothSpawn. */
    private static void chagarothSpawn(Function<String, Optional<GeoBone>> bones, float limbSwing, float limbSwingAmount) {
        float swing = limbSwing * 0.6662F;
        float front = Mth.cos(swing + PI) * 2.0F * limbSwingAmount * 0.5F;
        float left = Mth.cos(swing) * 2.0F * limbSwingAmount * 0.5F;
        float back = Mth.cos(swing) * 1.4F * limbSwingAmount;
        float right = Mth.cos(swing + PI) * 1.4F * limbSwingAmount;
        for (int i = 1; i <= 3; i++) {
            addY(bones, "frontleg" + i, front);
            addY(bones, "leftleg" + i, left);
            addY(bones, "backleg" + i, back);
            addY(bones, "rightleg" + i, right);
        }
    }

    /**
     * Jzahar: head track + nine face tentacles swaying + eight three-segment leg-tentacles waving + a staff
     * raise on melee. 1.12.2 ModelJzahar; the stateful curl/release (passedMax/reverseNum) is dropped for a
     * stateless wave since the GeoModel is a shared singleton.
     */
    private static void jzahar(Function<String, Optional<GeoBone>> bones, float netHeadYaw, float headPitch,
                               float age, float attack, int deathTime) {
        headTrack(bones, "head", netHeadYaw, headPitch);
        float faceSwing = Mth.sin(age * 0.0599F) * 4.5F * DEG_TO_RAD;
        for (String n : new String[] {"fT1", "fT12", "fT13", "fT2", "fT22", "fT23", "fT3", "fT32", "fT33"})
            addX(bones, n, faceSwing);
        // Eight leg-tentacles wave (tip vs segments in opposite phase; 1-4 forward, 5-8 reversed).
        float swing = Mth.sin(age * 0.12F) * 40.5F * DEG_TO_RAD;
        float swingX = Math.max(swing, 0.0873F);
        for (int i = 1; i <= 8; i++)
            legTentacle(bones, i, i >= 5, swingX);
        // Book + staff bob vertically (1.12.2 rotationPointY = rest + cos((6+age)*0.25)); the book is
        // otherwise idle. Position offset, so it does not clash with the staff's rotation below.
        float bob = Mth.cos((6.0F + age) * 0.25F);
        posY(bones, "abyssalnomicon", bob);
        posY(bones, "staff1", bob);
        // Staff raise during a melee swing (attack = getAttackAnim, 0..1).
        float ease = 1.0F - attack;
        ease = 1.0F - ease * ease * ease * ease;
        addX(bones, "staff1", Mth.sin(ease * PI) * 1.2F + Mth.sin(attack * PI) * 0.75F);
        addY(bones, "staff1", Mth.sin(Mth.sqrt(attack) * PI * 2.0F) * 0.2F
                + Mth.sin(Mth.sqrt(ease) * PI * 2.0F) * 0.4F);

        boolean dying = deathTime > 0 && deathTime <= 800;
        hidden(bones, "eye1", dying);
        hidden(bones, "abyssalnomicon", dying);
        hidden(bones, "staff1", dying);
        for (int index = 1; index <= 3; index++) hidden(bones, "fT" + index, dying);
        for (int index = 1; index <= 8; index++) hidden(bones, "tentacle" + index, dying);
        if (dying) {
            bones.apply("head").ifPresent(b -> b.setRotX(-20.0F));
            posY(bones, "body", 18.5F + Mth.cos((6.0F + age) * 0.25F));
        } else {
            posY(bones, "body", 0.0F);
        }
    }

    /** One Jzahar leg-tentacle (base + two segments + foot), a stateless bending wave. */
    private static void legTentacle(Function<String, Optional<GeoBone>> bones, int i, boolean reverse, float swingX) {
        float base = reverse ? swingX : -swingX;
        float seg = reverse ? -swingX : swingX;
        addX(bones, "tentacle" + i, base);
        addX(bones, "tentacle" + i + "2", seg);
        addX(bones, "tentacle" + i + "3", seg);
        addX(bones, "foot" + i, seg);
    }

    /** Chagaroth: middle head track + the eight wall tentacles waving while moving. 1.12.2 ModelChagaroth. */
    private static void chagaroth(Function<String, Optional<GeoBone>> bones, float limbSwingAmount,
                                  float netHeadYaw, float headPitch, int deathTime) {
        headTrack(bones, "middlehead", netHeadYaw, headPitch);
        for (int i = 1; i <= 8; i++) {
            float phase = (i % 2 == 1) ? PI : 0.0F;
            addX(bones, "walltentacle" + i, Mth.cos(headPitch * 0.6662F + phase) * 2.0F * limbSwingAmount * 0.5F);
            addY(bones, "walltentacle" + i, Mth.cos(netHeadYaw * 0.6662F + phase) * 2.0F * limbSwingAmount * 0.5F);
        }

        boolean dying = deathTime > 0 && deathTime <= 200;
        for (int index = 1; index <= 8; index++) hidden(bones, "walltentacle" + index, dying);
        hidden(bones, "leftwall", dying);
        hidden(bones, "rightwall", dying);
        if (!dying) {
            resetRotation(bones, "lefthead");
            resetRotation(bones, "righthead");
            bones.apply("middlehead").ifPresent(b ->
                b.setRotZ(b.getInitialSnapshot().getRotZ()));
            return;
        }

        float rotation = deathTime;
        rotateChagarothHead(bones, "lefthead",
            (180.0F + rotation) / PI, (180.0F - rotation) / PI, (180.0F + rotation) / PI);
        rotateChagarothHead(bones, "middlehead",
            (180.0F - rotation) / PI, (180.0F + rotation) / PI, (180.0F - rotation) / PI);
        rotateChagarothHead(bones, "righthead",
            (180.0F + rotation) / PI, (180.0F + rotation) / PI, (180.0F + rotation) / PI);
    }

    private static void rotateChagarothHead(Function<String, Optional<GeoBone>> bones, String name,
                                            float x, float y, float z) {
        bones.apply(name).ifPresent(b -> {
            b.setRotX(-x);
            b.setRotY(-y);
            b.setRotZ(z);
        });
    }

    /**
     * Shub Offspring: head track + splayed leg swing + idle spikes. 1.12.2 ModelShubOffspring, dropping the
     * constant rest offsets (GeckoLib already bakes them into the geo) and keeping only the oscillation.
     */
    private static void shubOffspring(Function<String, Optional<GeoBone>> bones, float limbSwing,
                                      float limbSwingAmount, float netHeadYaw, float headPitch, float age) {
        headTrack(bones, "headJoint", netHeadYaw, headPitch);
        float swing = limbSwing * 0.6662F;
        addZ(bones, "rLeg01a", Mth.cos(swing) * 0.7F * limbSwingAmount);
        addZ(bones, "lLeg01a", Mth.cos(swing + PI) * 0.7F * limbSwingAmount);
        addY(bones, "lLeg02a", Mth.cos(swing + PI) * 0.7F * limbSwingAmount);
        float flap = Mth.sin(age * 0.2F) * 0.3F;
        float flap2 = Mth.cos(age * 0.2F) * 0.4F;
        float flap3 = Mth.cos(age * 0.2F) * 0.5F;
        float anim = Mth.sin((limbSwing * 0.4F + 2.0F) * 1.5F) * 0.3F * limbSwingAmount * 0.3F;
        addY(bones, "lspike01a", flap * 0.1F + anim * 0.4F);
        addY(bones, "lspike02a", flap2 * 0.1F + anim * 0.4F);
        addY(bones, "lspike03a", flap3 * 0.1F + anim * 0.4F);
        addY(bones, "lspike04a", flap * 0.1F + anim * 0.4F);
        addY(bones, "lspike05a", flap2 * 0.1F + anim * 0.4F);
        addY(bones, "lspike06a", flap3 * 0.1F + anim * 0.4F);
        addY(bones, "rspike01a", flap * 0.05F + anim * 0.4F);
        addY(bones, "rspike02a", flap2 * 0.05F + anim * 0.4F);
        addY(bones, "rspike03a", flap3 * 0.05F + anim * 0.4F);
        addY(bones, "rspike04a", flap * 0.05F + anim * 0.4F);
        addY(bones, "rspike05a", flap2 * 0.05F + anim * 0.4F);
        addY(bones, "rspike06a", flap3 * 0.05F + anim * 0.4F);
    }
}
