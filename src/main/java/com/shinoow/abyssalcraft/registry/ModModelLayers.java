package com.shinoow.abyssalcraft.registry;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.ArmorStandArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

import com.shinoow.abyssalcraft.platform.ACRef;

/**
 * Model-layer location registry (owned by PE-1, Stage E1). <b>Client-only</b> (references client model
 * classes; only reached through the client render relay, never on a dedicated server).
 *
 * <p>Holds the {@link ModelLayerLocation}s the mod bakes, plus their {@link LayerDefinition}s. Stage E1
 * ships a single {@link #PLACEHOLDER} cube proving the layer-definition pipeline; Stage E2 (PE-2..6)
 * appends faithful per-entity / armor / BE layers here and registers them via {@code ACEntityRenderers}.
 */
public final class ModModelLayers {

    private ModModelLayers() {}

    /** Placeholder layer baked by the E1 default renderer until E2 supplies real per-entity meshes. */
    public static final ModelLayerLocation PLACEHOLDER = new ModelLayerLocation(ACRef.id("placeholder"), "main");

    /** Ghoul family mesh (PE-3, Stage E2) -- shared by all five ghoul entities. */
    public static final ModelLayerLocation GHOUL = new ModelLayerLocation(ACRef.id("ghoul"), "main");
    public static final ModelLayerLocation GHOUL_ARMOR_INNER = new ModelLayerLocation(ACRef.id("ghoul"), "armor_inner");
    public static final ModelLayerLocation GHOUL_ARMOR_OUTER = new ModelLayerLocation(ACRef.id("ghoul"), "armor_outer");

    /** Shoggoth family mesh (PE-3, Stage E2) -- shared by the three shoggoth entities. */
    public static final ModelLayerLocation SHOGGOTH = new ModelLayerLocation(ACRef.id("shoggoth"), "main");

    /** Abyssal Dragon / Dragon Minion mesh (PE-4b) -- a Java-model port of the procedural ender-dragon. */
    public static final ModelLayerLocation DRAGON = new ModelLayerLocation(ACRef.id("dragon"), "main");

    public static final ModelLayerLocation BILLBOARD = new ModelLayerLocation(ACRef.id("effect/billboard"), "main");
    public static final ModelLayerLocation ODB_CUBE = new ModelLayerLocation(ACRef.id("effect/primed_odb"), "main");
    public static final ModelLayerLocation DREAD_TENTACLE = new ModelLayerLocation(ACRef.id("effect/dread_tentacle"), "main");
    public static final ModelLayerLocation STAR_SPAWN_TENTACLES =
        new ModelLayerLocation(ACRef.id("player/star_spawn_tentacles"), "main");
    public static final ModelLayerLocation DEMON_SHEEP = new ModelLayerLocation(ACRef.id("demon_sheep"), "main");
    public static final ModelLayerLocation DEMON_SHEEP_FUR = new ModelLayerLocation(ACRef.id("demon_sheep"), "fur");
    public static final ModelLayerLocation SAMURAI_INNER = new ModelLayerLocation(ACRef.id("armor/dreadium_samurai"), "inner");
    public static final ModelLayerLocation SAMURAI_OUTER = new ModelLayerLocation(ACRef.id("armor/dreadium_samurai"), "outer");
    public static final ModelLayerLocation DEPTHS_INNER = new ModelLayerLocation(ACRef.id("armor/depths"), "inner");
    public static final ModelLayerLocation DEPTHS_OUTER = new ModelLayerLocation(ACRef.id("armor/depths"), "outer");
    public static final ModelLayerLocation DEPTHS_SKELETON_INNER =
        new ModelLayerLocation(ACRef.id("armor/depths"), "skeleton_inner");
    public static final ModelLayerLocation DEPTHS_SKELETON_OUTER =
        new ModelLayerLocation(ACRef.id("armor/depths"), "skeleton_outer");
    public static final ModelLayerLocation DEPTHS_ARMOR_STAND_INNER =
        new ModelLayerLocation(ACRef.id("armor/depths"), "armor_stand_inner");
    public static final ModelLayerLocation DEPTHS_ARMOR_STAND_OUTER =
        new ModelLayerLocation(ACRef.id("armor/depths"), "armor_stand_outer");
    public static final ModelLayerLocation ABYSSAL_ZOMBIE = new ModelLayerLocation(ACRef.id("abyssal_zombie"), "main");
    public static final ModelLayerLocation ABYSSAL_ZOMBIE_INNER = new ModelLayerLocation(ACRef.id("abyssal_zombie"), "inner_armor");
    public static final ModelLayerLocation ABYSSAL_ZOMBIE_OUTER = new ModelLayerLocation(ACRef.id("abyssal_zombie"), "outer_armor");

    public static final ModelLayerLocation LEGACY_DREADLING = legacy("dreadling");
    public static final ModelLayerLocation LEGACY_DREAD_SPAWN = legacy("dread_spawn");
    public static final ModelLayerLocation LEGACY_DREADBEAST = legacy("lesser_dreadbeast");
    public static final ModelLayerLocation LEGACY_SHADOW_CREATURE = legacy("shadow_creature");
    public static final ModelLayerLocation LEGACY_SHADOW_MONSTER = legacy("shadow_monster");
    public static final ModelLayerLocation LEGACY_SHADOW_BEAST = legacy("shadow_beast");

    private static ModelLayerLocation legacy(String id) {
        return new ModelLayerLocation(ACRef.id("legacy/" + id), "main");
    }

    /** A single 8x8x8 cube -- exercises the layer-definition pipeline; E2 replaces with faithful meshes. */
    public static LayerDefinition placeholder() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("cube",
            CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }

    public static LayerDefinition billboard() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("quad",
            CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -8.0F, 0.0F, 16, 16, 0), PartPose.ZERO);
        return LayerDefinition.create(mesh, 16, 16);
    }

    public static LayerDefinition odbCube() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("cube",
            CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -8.0F, -8.0F, 16, 16, 16), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 32);
    }

    public static LayerDefinition humanoidArmor(float deformation) {
        return LayerDefinition.create(HumanoidModel.createMesh(new CubeDeformation(deformation), 0.0F), 64, 32);
    }

    public static LayerDefinition armorStandArmor(float deformation) {
        return ArmorStandArmorModel.createBodyLayer(new CubeDeformation(deformation));
    }

    public static LayerDefinition skeletonArmor(float deformation) {
        CubeDeformation inflate = new CubeDeformation(deformation);
        MeshDefinition mesh = HumanoidModel.createMesh(inflate, 0.0F);
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16)
            .addBox(-1.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F, inflate),
            PartPose.offset(-5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16).mirror()
            .addBox(-1.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F, inflate),
            PartPose.offset(5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16)
            .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F, inflate),
            PartPose.offset(-2.0F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror()
            .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F, inflate),
            PartPose.offset(2.0F, 12.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    public static LayerDefinition classicHumanoid(float deformation) {
        return LayerDefinition.create(HumanoidModel.createMesh(new CubeDeformation(deformation), 0.0F), 64, 32);
    }
}
