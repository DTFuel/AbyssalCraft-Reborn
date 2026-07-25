package com.shinoow.abyssalcraft.client.model.entity;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;

/**
 * Java {@link HierarchicalModel} port of the 1.12.2 {@code ModelDragonBoss} / {@code ModelDragonMinion}
 * (both the vanilla ender-dragon structure) -- owned by PE-4b. Used for {@code dragonboss} +
 * {@code dragonminion} (256x256 textures).
 *
 * <p>The 1.12.2 dragon is <b>procedural</b>: its {@code render()} positions the head via a 5-segment neck
 * spline and the tail via a 12-segment spline (reading the entity's movement history), so it cannot be
 * exported as a static GeckoLib mesh (that renders flat/collapsed). This port instead keeps the faithful
 * box geometry + UVs (identical to vanilla {@code EnderDragonModel}) and bakes a fixed hovering pose into
 * the layer (a short neck to the head, a tapering tail, spread wings, tucked legs).
 *
 * <p>{@link #setupAnim} adds a procedural idle: a slow wing beat (faithful to the vanilla dragon's
 * {@code wing.zRot = (sin+..)*..} flap, damped), a jaw open/close, a serpentine neck + tail sway, a subtle
 * leg bob, and head-yaw/pitch look-tracking distributed across the neck. The original neck/tail splines
 * read the ender-dragon's movement-offset ring buffer, which our custom {@code Mob} lacks, so the sway is
 * driven by {@code ageInTicks} instead. Each frame resets every part to its baked pose then applies the
 * deltas additively. Uses {@link HierarchicalModel} so {@code renderToBuffer} (which diverges
 * 1.20.1 / 1.21.1) is handled by vanilla.
 */
public class DragonModel<T extends Mob> extends HierarchicalModel<T> {

    private static final float DEG_TO_RAD = (float) Math.PI / 180.0F;
    /** Idle wing-beat / undulation speed in radians per tick of {@code ageInTicks} (~1.5s per cycle). */
    private static final float ANIM_SPEED = 0.2F;

    private final ModelPart root;
    private final ModelPart neck;
    private final ModelPart neck2;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart tail;
    private final ModelPart tail2;
    private final ModelPart tail3;
    private final ModelPart wingRight;
    private final ModelPart wingRightTip;
    private final ModelPart wingLeft;
    private final ModelPart wingLeftTip;
    private final ModelPart frontRight;
    private final ModelPart frontLeft;
    private final ModelPart rearRight;
    private final ModelPart rearLeft;

    public DragonModel(ModelPart root) {
        this.root = root;
        ModelPart body = root.getChild("body");
        this.neck = body.getChild("neck");
        this.neck2 = this.neck.getChild("neck2");
        this.head = this.neck2.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.tail = body.getChild("tail");
        this.tail2 = this.tail.getChild("tail2");
        this.tail3 = this.tail2.getChild("tail3");
        this.wingRight = body.getChild("wing_right");
        this.wingRightTip = this.wingRight.getChild("wing_right_tip");
        this.wingLeft = body.getChild("wing_left");
        this.wingLeftTip = this.wingLeft.getChild("wing_left_tip");
        this.frontRight = body.getChild("front_right");
        this.frontLeft = body.getChild("front_left");
        this.rearRight = body.getChild("rear_right");
        this.rearLeft = body.getChild("rear_left");
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        this.root.getAllParts().forEach(ModelPart::resetPose);

        float wave = ageInTicks * ANIM_SPEED;
        float sin = Mth.sin(wave);
        float cos = Mth.cos(wave);

        // Wings: gentle flap around the baked spread (faithful to the vanilla dragon beat, damped); the tip lags.
        this.wingRight.zRot += cos * 0.18F;
        this.wingLeft.zRot -= cos * 0.18F;
        this.wingRight.xRot += sin * 0.08F;
        this.wingLeft.xRot += sin * 0.08F;
        this.wingRightTip.zRot += Mth.cos(wave - 0.6F) * 0.22F;
        this.wingLeftTip.zRot -= Mth.cos(wave - 0.6F) * 0.22F;

        // Jaw: slow open/close (vanilla jaw.xRot = (sin+1)*0.2, damped).
        this.jaw.xRot += (sin + 1.0F) * 0.06F;

        // Neck + head: idle bob plus look-at tracking distributed across the neck.
        float yaw = netHeadYaw * DEG_TO_RAD;
        float pitch = headPitch * DEG_TO_RAD;
        this.neck.yRot += sin * 0.04F + yaw * 0.3F;
        this.neck.xRot += cos * 0.03F + pitch * 0.2F;
        this.neck2.yRot += Mth.sin(wave - 0.5F) * 0.05F + yaw * 0.3F;
        this.neck2.xRot += pitch * 0.2F;
        this.head.yRot += yaw * 0.4F;
        this.head.xRot += pitch * 0.4F;

        // Tail: serpentine sway increasing toward the tip.
        this.tail.yRot += sin * 0.05F;
        this.tail2.yRot += Mth.sin(wave - 0.6F) * 0.07F;
        this.tail3.yRot += Mth.sin(wave - 1.2F) * 0.09F;

        // Legs: subtle bob so the tucked limbs feel alive.
        float bob = sin * 0.03F;
        this.frontRight.xRot += bob;
        this.frontLeft.xRot += bob;
        this.rearRight.xRot += bob;
        this.rearLeft.xRot += bob;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Body (the 24x24x64 trunk + three back scales) -- lifted so the tucked legs hang toward the ground.
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
            .texOffs(0, 0).addBox(-12.0F, 0.0F, -16.0F, 24, 24, 64)
            .texOffs(220, 53).addBox(-1.0F, -6.0F, -10.0F, 2, 6, 12)
            .texOffs(220, 53).addBox(-1.0F, -6.0F, 10.0F, 2, 6, 12)
            .texOffs(220, 53).addBox(-1.0F, -6.0F, 30.0F, 2, 6, 12),
            PartPose.offset(0.0F, -6.0F, 0.0F));

        // Neck -> head -> jaw (curving up-forward off the body front).
        PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create()
            .texOffs(192, 104).addBox(-5.0F, -5.0F, -5.0F, 10, 10, 10)
            .texOffs(48, 0).addBox(-1.0F, -9.0F, -3.0F, 2, 4, 6),
            PartPose.offsetAndRotation(0.0F, 2.0F, -14.0F, -0.35F, 0.0F, 0.0F));
        PartDefinition neck2 = neck.addOrReplaceChild("neck2", CubeListBuilder.create()
            .texOffs(192, 104).addBox(-5.0F, -5.0F, -5.0F, 10, 10, 10)
            .texOffs(48, 0).addBox(-1.0F, -9.0F, -3.0F, 2, 4, 6),
            PartPose.offsetAndRotation(0.0F, 0.0F, -9.0F, 0.15F, 0.0F, 0.0F));
        PartDefinition head = neck2.addOrReplaceChild("head", CubeListBuilder.create()
            .texOffs(176, 44).addBox(-6.0F, -1.0F, -24.0F, 12, 5, 16)
            .texOffs(112, 30).addBox(-8.0F, -8.0F, -10.0F, 16, 16, 16)
            .texOffs(0, 0).mirror().addBox(-5.0F, -12.0F, -4.0F, 2, 4, 6).mirror(false)
            .texOffs(112, 0).addBox(-5.0F, -3.0F, -22.0F, 2, 2, 4)
            .texOffs(0, 0).addBox(3.0F, -12.0F, -4.0F, 2, 4, 6)
            .texOffs(112, 0).addBox(3.0F, -3.0F, -22.0F, 2, 2, 4),
            PartPose.offsetAndRotation(0.0F, 0.0F, -8.0F, 0.2F, 0.0F, 0.0F));
        head.addOrReplaceChild("jaw", CubeListBuilder.create()
            .texOffs(176, 65).addBox(-6.0F, 0.0F, -16.0F, 12, 4, 16),
            PartPose.offset(0.0F, 4.0F, -8.0F));

        // Tail (three tapering segments off the body rear, drooping).
        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create()
            .texOffs(192, 104).addBox(-5.0F, -5.0F, -5.0F, 10, 10, 10)
            .texOffs(48, 0).addBox(-1.0F, -9.0F, -3.0F, 2, 4, 6),
            PartPose.offsetAndRotation(0.0F, 2.0F, 50.0F, 0.15F, 0.0F, 0.0F));
        PartDefinition tail2 = tail.addOrReplaceChild("tail2", CubeListBuilder.create()
            .texOffs(192, 104).addBox(-4.0F, -4.0F, -4.0F, 8, 8, 10)
            .texOffs(48, 0).addBox(-1.0F, -8.0F, -3.0F, 2, 4, 6),
            PartPose.offsetAndRotation(0.0F, 0.0F, 9.0F, 0.2F, 0.0F, 0.0F));
        tail2.addOrReplaceChild("tail3", CubeListBuilder.create()
            .texOffs(192, 104).addBox(-3.0F, -3.0F, -3.0F, 6, 6, 12)
            .texOffs(48, 0).addBox(-1.0F, -6.0F, -2.0F, 2, 3, 6),
            PartPose.offsetAndRotation(0.0F, 0.0F, 8.0F, 0.2F, 0.0F, 0.0F));

        // Wings (bone + membrane + tip), mounted on the mid-back and spread out-and-slightly-up.
        wing(body, "wing_right", false, -12.0F, 0.25F, 0.3F);
        wing(body, "wing_left", true, 12.0F, -0.25F, -0.3F);

        // Legs (bent/tucked, hanging down).
        leg(body, "front_right", false, -12.0F, 16.0F, -6.0F, true);
        leg(body, "front_left", true, 12.0F, 16.0F, -6.0F, true);
        leg(body, "rear_right", false, -16.0F, 12.0F, 34.0F, false);
        leg(body, "rear_left", true, 16.0F, 12.0F, 34.0F, false);

        return LayerDefinition.create(mesh, 256, 256);
    }

    private static void wing(PartDefinition body, String name, boolean mirror, float x, float yRot, float zRot) {
        // The left wing needs genuinely mirrored GEOMETRY: CubeListBuilder.mirror() only flips the texture,
        // not the box position, so reusing addBox(-56,..) for both would land both wings on the same (-x)
        // side. Flip each box's x-origin (-56 -> 0) and the wing-tip pivot (-56 -> +56) for the mirrored side.
        float ox = mirror ? 0.0F : -56.0F;
        float tipX = mirror ? 56.0F : -56.0F;
        PartDefinition wing = body.addOrReplaceChild(name, CubeListBuilder.create().mirror(mirror)
            .texOffs(112, 88).addBox(ox, -4.0F, -4.0F, 56, 8, 8)
            .texOffs(-56, 88).addBox(ox, 0.0F, 2.0F, 56, 0, 56),
            PartPose.offsetAndRotation(x, -1.0F, 8.0F, -0.075F, yRot, zRot));
        wing.addOrReplaceChild(name + "_tip", CubeListBuilder.create().mirror(mirror)
            .texOffs(112, 136).addBox(ox, -2.0F, -2.0F, 56, 4, 4)
            .texOffs(-56, 144).addBox(ox, 0.0F, 2.0F, 56, 0, 56),
            PartPose.offsetAndRotation(tipX, 0.0F, 0.0F, 0.0F, 0.0F, mirror ? 0.6F : -0.6F));
    }

    private static void leg(PartDefinition body, String name, boolean mirror, float x, float y, float z, boolean front) {
        int mainW = front ? 8 : 16, mainH = front ? 24 : 32, mainD = front ? 8 : 16;
        int tipW = front ? 6 : 12, tipH = front ? 24 : 32, tipD = front ? 6 : 12;
        float tipY = front ? 20.0F : 32.0F, tipZ = front ? -1.0F : -4.0F;
        float footY = front ? 23.0F : 31.0F, footZ = front ? 0.0F : 4.0F;
        int footW = front ? 8 : 18, footH = front ? 4 : 6, footD = front ? 16 : 24;
        float legRot = front ? 1.3F : 1.0F, tipRot = front ? -0.5F : 0.5F;
        int mainU = front ? 112 : 0, mainV = front ? 104 : 0;
        int tipU = front ? 226 : 196, tipV = front ? 138 : 0;
        int footU = front ? 144 : 112, footV = front ? 104 : 0;

        PartDefinition leg = body.addOrReplaceChild(name, CubeListBuilder.create().mirror(mirror)
            .texOffs(mainU, mainV).addBox(-mainW / 2.0F, -4.0F, -mainD / 2.0F, mainW, mainH, mainD),
            PartPose.offsetAndRotation(x, y, z, legRot, 0.0F, 0.0F));
        PartDefinition tip = leg.addOrReplaceChild(name + "_tip", CubeListBuilder.create().mirror(mirror)
            .texOffs(tipU, tipV).addBox(-tipW / 2.0F, -2.0F, front ? -3.0F : 0.0F, tipW, tipH, tipD),
            PartPose.offsetAndRotation(0.0F, tipY, tipZ, tipRot, 0.0F, 0.0F));
        tip.addOrReplaceChild(name + "_foot", CubeListBuilder.create().mirror(mirror)
            .texOffs(footU, footV).addBox(-footW / 2.0F, 0.0F, front ? -12.0F : -20.0F, footW, footH, footD),
            PartPose.offsetAndRotation(0.0F, footY, footZ, 0.75F, 0.0F, 0.0F));
    }
}
