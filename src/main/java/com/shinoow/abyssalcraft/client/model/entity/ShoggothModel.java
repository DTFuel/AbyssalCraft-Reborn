package com.shinoow.abyssalcraft.client.model.entity;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

import com.shinoow.abyssalcraft.content.entity.shoggoth.AbstractShoggoth;

/**
 * Full faithful port of 1.12.2 ModelLesserShoggoth (128x128). Geometry, part
 * hierarchy and the setRotationAngles animation are transcribed verbatim by
 * scripts/convert_modelbase_to_entitymodel.js --full (do not hand-edit; regenerate instead).
 */
public class ShoggothModel extends HierarchicalModel<AbstractShoggoth> {

    private final ModelPart root;
    private final ModelPart headJoint;
    private final ModelPart bodyBase;
    private final ModelPart bodyUpper;
    private final ModelPart lHindTentacle01a;
    private final ModelPart lBodyTentacle01a;
    private final ModelPart bodyMid;
    private final ModelPart rBodyTentacle01a;
    private final ModelPart rHindTentacle03a;
    private final ModelPart rHindTentacle01a;
    private final ModelPart lHindTentacle03a;
    private final ModelPart lHindTentacle02a;
    private final ModelPart rHindTentacle02a;
    private final ModelPart tail01a;
    private final ModelPart mouth01Upper;
    private final ModelPart eye09;
    private final ModelPart eye01;
    private final ModelPart eye11;
    private final ModelPart eye03;
    private final ModelPart eye02;
    private final ModelPart mouth02Upper;
    private final ModelPart eye12;
    private final ModelPart eye10;
    private final ModelPart eye04;
    private final ModelPart mouth03Upper;
    private final ModelPart lHindTentacle01b;
    private final ModelPart lBodyTentacle01b;
    private final ModelPart eye22;
    private final ModelPart lBodyTentacle04a;
    private final ModelPart lBackTentacle01a;
    private final ModelPart eye21;
    private final ModelPart rBodyTentacle03a;
    private final ModelPart lArm01a;
    private final ModelPart rBackTentacle01a;
    private final ModelPart rArm01a;
    private final ModelPart eye20;
    private final ModelPart rBackTentacle02a;
    private final ModelPart eye19;
    private final ModelPart rBodyTentacle04a;
    private final ModelPart lBodyTentacle02a;
    private final ModelPart lBackTentacle02a;
    private final ModelPart lBodyTentacle03a;
    private final ModelPart lBackTentacle02a_1;
    private final ModelPart rBodyTentacle02a;
    private final ModelPart rBackTentacle02a_1;
    private final ModelPart rBodyTentacle01b;
    private final ModelPart rHindTentacle03b;
    private final ModelPart rHindTentacle03;
    private final ModelPart rHindTentacle01b;
    private final ModelPart lHindTentacle03b;
    private final ModelPart lHindTentacle03;
    private final ModelPart lHindTentacle02b;
    private final ModelPart rHindTentacle02b;
    private final ModelPart eye28;
    private final ModelPart tail01b;
    private final ModelPart mouth01Lower;
    private final ModelPart eye05;
    private final ModelPart mouth01Tooth05;
    private final ModelPart mouth01Tooth03;
    private final ModelPart mouth01Snout;
    private final ModelPart mouth01Tooth02;
    private final ModelPart mouth01Tooth04;
    private final ModelPart mouth01Tooth01;
    private final ModelPart eye04_1;
    private final ModelPart mouth01Tooth06;
    private final ModelPart mouth01Teeth01;
    private final ModelPart mouth02Snout;
    private final ModelPart mouth02Lower;
    private final ModelPart mouth02Tooth02;
    private final ModelPart mouth02Tooth01;
    private final ModelPart eye06;
    private final ModelPart mouth02Tooth03;
    private final ModelPart mouth02Teeth01;
    private final ModelPart mouth02Tooth04;
    private final ModelPart mouth02Tooth04_1;
    private final ModelPart mouth03Snout;
    private final ModelPart mouth03Teeth01;
    private final ModelPart mouth03Tooth05;
    private final ModelPart mouth03Tooth02;
    private final ModelPart mouth03Lower;
    private final ModelPart eye08;
    private final ModelPart mouth03Tooth01;
    private final ModelPart mouth03Tooth04;
    private final ModelPart eye07;
    private final ModelPart mouth03Tooth03;
    private final ModelPart lHindTentacle01c;
    private final ModelPart eye25;
    private final ModelPart lBodyTentacle01c;
    private final ModelPart lBodyTentacle04b;
    private final ModelPart lBackTentacle01b;
    private final ModelPart rBodyTentacle03b;
    private final ModelPart lArm01b;
    private final ModelPart eye15;
    private final ModelPart eye13;
    private final ModelPart rBackTentacle01b;
    private final ModelPart rArm01b;
    private final ModelPart eye18;
    private final ModelPart rBackTentacle02b;
    private final ModelPart rBodyTentacle04b;
    private final ModelPart lBodyTentacle02b;
    private final ModelPart lBackTentacle02b;
    private final ModelPart lBodyTentacle03b;
    private final ModelPart lBackTentacle02b_1;
    private final ModelPart rBodyTentacle02b;
    private final ModelPart rBackTentacle02b_1;
    private final ModelPart rBodyTentacle01c;
    private final ModelPart rHindTentacle03c;
    private final ModelPart eye32;
    private final ModelPart rHindTentacle01c;
    private final ModelPart lHindTentacle03c;
    private final ModelPart eye23;
    private final ModelPart lHindTentacle02c;
    private final ModelPart rHindTentacle02c;
    private final ModelPart tail01c;
    private final ModelPart eye27;
    private final ModelPart mouth01Teeth03;
    private final ModelPart mouth01Teeth02;
    private final ModelPart mouth02Teeth02;
    private final ModelPart mouth02Teeth03;
    private final ModelPart mouth02Teeth03_1;
    private final ModelPart mouth03Teeth02;
    private final ModelPart lHindTentacle01d;
    private final ModelPart lBackTentacle01c;
    private final ModelPart lArm01c;
    private final ModelPart rBackTentacle01c;
    private final ModelPart rArm01c;
    private final ModelPart eye17;
    private final ModelPart eye16;
    private final ModelPart rBackTentacle02c;
    private final ModelPart lBodyTentacle02c;
    private final ModelPart lBackTentacle02c;
    private final ModelPart lBackTentacle02c_1;
    private final ModelPart rBodyTentacle02c;
    private final ModelPart rBackTentacle02c_1;
    private final ModelPart eye29;
    private final ModelPart rHindTentacle01d;
    private final ModelPart lHindTentacle02d;
    private final ModelPart eye31;
    private final ModelPart rHindTentacle02d;
    private final ModelPart tail01d;
    private final ModelPart eye26;
    private final ModelPart lHindTentacle01e;
    private final ModelPart lArm01d;
    private final ModelPart eye14;
    private final ModelPart rArm01d;
    private final ModelPart rHindTentacle01e;
    private final ModelPart eye30;
    private final ModelPart lHindTentacle02e;
    private final ModelPart eye24;
    private final ModelPart rHindTentacle02e;
    private final ModelPart tail01e;

    public ShoggothModel(ModelPart root) {
        this.root = root;
        this.headJoint = root.getChild("headJoint");
        this.bodyBase = root.getChild("bodyBase");
        this.bodyUpper = this.headJoint.getChild("bodyUpper");
        this.lHindTentacle01a = this.bodyBase.getChild("lHindTentacle01a");
        this.lBodyTentacle01a = this.bodyBase.getChild("lBodyTentacle01a");
        this.bodyMid = this.bodyBase.getChild("bodyMid");
        this.rBodyTentacle01a = this.bodyBase.getChild("rBodyTentacle01a");
        this.rHindTentacle03a = this.bodyBase.getChild("rHindTentacle03a");
        this.rHindTentacle01a = this.bodyBase.getChild("rHindTentacle01a");
        this.lHindTentacle03a = this.bodyBase.getChild("lHindTentacle03a");
        this.lHindTentacle02a = this.bodyBase.getChild("lHindTentacle02a");
        this.rHindTentacle02a = this.bodyBase.getChild("rHindTentacle02a");
        this.tail01a = this.bodyBase.getChild("tail01a");
        this.mouth01Upper = this.bodyUpper.getChild("mouth01Upper");
        this.eye09 = this.bodyUpper.getChild("eye09");
        this.eye01 = this.bodyUpper.getChild("eye01");
        this.eye11 = this.bodyUpper.getChild("eye11");
        this.eye03 = this.bodyUpper.getChild("eye03");
        this.eye02 = this.bodyUpper.getChild("eye02");
        this.mouth02Upper = this.bodyUpper.getChild("mouth02Upper");
        this.eye12 = this.bodyUpper.getChild("eye12");
        this.eye10 = this.bodyUpper.getChild("eye10");
        this.eye04 = this.bodyUpper.getChild("eye04");
        this.mouth03Upper = this.bodyUpper.getChild("mouth03Upper");
        this.lHindTentacle01b = this.lHindTentacle01a.getChild("lHindTentacle01b");
        this.lBodyTentacle01b = this.lBodyTentacle01a.getChild("lBodyTentacle01b");
        this.eye22 = this.lBodyTentacle01a.getChild("eye22");
        this.lBodyTentacle04a = this.bodyMid.getChild("lBodyTentacle04a");
        this.lBackTentacle01a = this.bodyMid.getChild("lBackTentacle01a");
        this.eye21 = this.bodyMid.getChild("eye21");
        this.rBodyTentacle03a = this.bodyMid.getChild("rBodyTentacle03a");
        this.lArm01a = this.bodyMid.getChild("lArm01a");
        this.rBackTentacle01a = this.bodyMid.getChild("rBackTentacle01a");
        this.rArm01a = this.bodyMid.getChild("rArm01a");
        this.eye20 = this.bodyMid.getChild("eye20");
        this.rBackTentacle02a = this.bodyMid.getChild("rBackTentacle02a");
        this.eye19 = this.bodyMid.getChild("eye19");
        this.rBodyTentacle04a = this.bodyMid.getChild("rBodyTentacle04a");
        this.lBodyTentacle02a = this.bodyMid.getChild("lBodyTentacle02a");
        this.lBackTentacle02a = this.bodyMid.getChild("lBackTentacle02a");
        this.lBodyTentacle03a = this.bodyMid.getChild("lBodyTentacle03a");
        this.lBackTentacle02a_1 = this.bodyMid.getChild("lBackTentacle02a_1");
        this.rBodyTentacle02a = this.bodyMid.getChild("rBodyTentacle02a");
        this.rBackTentacle02a_1 = this.bodyMid.getChild("rBackTentacle02a_1");
        this.rBodyTentacle01b = this.rBodyTentacle01a.getChild("rBodyTentacle01b");
        this.rHindTentacle03b = this.rHindTentacle03a.getChild("rHindTentacle03b");
        this.rHindTentacle03 = this.rHindTentacle03a.getChild("rHindTentacle03");
        this.rHindTentacle01b = this.rHindTentacle01a.getChild("rHindTentacle01b");
        this.lHindTentacle03b = this.lHindTentacle03a.getChild("lHindTentacle03b");
        this.lHindTentacle03 = this.lHindTentacle03a.getChild("lHindTentacle03");
        this.lHindTentacle02b = this.lHindTentacle02a.getChild("lHindTentacle02b");
        this.rHindTentacle02b = this.rHindTentacle02a.getChild("rHindTentacle02b");
        this.eye28 = this.tail01a.getChild("eye28");
        this.tail01b = this.tail01a.getChild("tail01b");
        this.mouth01Lower = this.mouth01Upper.getChild("mouth01Lower");
        this.eye05 = this.mouth01Upper.getChild("eye05");
        this.mouth01Tooth05 = this.mouth01Upper.getChild("mouth01Tooth05");
        this.mouth01Tooth03 = this.mouth01Upper.getChild("mouth01Tooth03");
        this.mouth01Snout = this.mouth01Upper.getChild("mouth01Snout");
        this.mouth01Tooth02 = this.mouth01Upper.getChild("mouth01Tooth02");
        this.mouth01Tooth04 = this.mouth01Upper.getChild("mouth01Tooth04");
        this.mouth01Tooth01 = this.mouth01Upper.getChild("mouth01Tooth01");
        this.eye04_1 = this.mouth01Upper.getChild("eye04_1");
        this.mouth01Tooth06 = this.mouth01Upper.getChild("mouth01Tooth06");
        this.mouth01Teeth01 = this.mouth01Upper.getChild("mouth01Teeth01");
        this.mouth02Snout = this.mouth02Upper.getChild("mouth02Snout");
        this.mouth02Lower = this.mouth02Upper.getChild("mouth02Lower");
        this.mouth02Tooth02 = this.mouth02Upper.getChild("mouth02Tooth02");
        this.mouth02Tooth01 = this.mouth02Upper.getChild("mouth02Tooth01");
        this.eye06 = this.mouth02Upper.getChild("eye06");
        this.mouth02Tooth03 = this.mouth02Upper.getChild("mouth02Tooth03");
        this.mouth02Teeth01 = this.mouth02Upper.getChild("mouth02Teeth01");
        this.mouth02Tooth04 = this.mouth02Upper.getChild("mouth02Tooth04");
        this.mouth02Tooth04_1 = this.mouth02Upper.getChild("mouth02Tooth04_1");
        this.mouth03Snout = this.mouth03Upper.getChild("mouth03Snout");
        this.mouth03Teeth01 = this.mouth03Upper.getChild("mouth03Teeth01");
        this.mouth03Tooth05 = this.mouth03Upper.getChild("mouth03Tooth05");
        this.mouth03Tooth02 = this.mouth03Upper.getChild("mouth03Tooth02");
        this.mouth03Lower = this.mouth03Upper.getChild("mouth03Lower");
        this.eye08 = this.mouth03Upper.getChild("eye08");
        this.mouth03Tooth01 = this.mouth03Upper.getChild("mouth03Tooth01");
        this.mouth03Tooth04 = this.mouth03Upper.getChild("mouth03Tooth04");
        this.eye07 = this.mouth03Upper.getChild("eye07");
        this.mouth03Tooth03 = this.mouth03Upper.getChild("mouth03Tooth03");
        this.lHindTentacle01c = this.lHindTentacle01b.getChild("lHindTentacle01c");
        this.eye25 = this.lHindTentacle01b.getChild("eye25");
        this.lBodyTentacle01c = this.lBodyTentacle01b.getChild("lBodyTentacle01c");
        this.lBodyTentacle04b = this.lBodyTentacle04a.getChild("lBodyTentacle04b");
        this.lBackTentacle01b = this.lBackTentacle01a.getChild("lBackTentacle01b");
        this.rBodyTentacle03b = this.rBodyTentacle03a.getChild("rBodyTentacle03b");
        this.lArm01b = this.lArm01a.getChild("lArm01b");
        this.eye15 = this.lArm01a.getChild("eye15");
        this.eye13 = this.lArm01a.getChild("eye13");
        this.rBackTentacle01b = this.rBackTentacle01a.getChild("rBackTentacle01b");
        this.rArm01b = this.rArm01a.getChild("rArm01b");
        this.eye18 = this.rArm01a.getChild("eye18");
        this.rBackTentacle02b = this.rBackTentacle02a.getChild("rBackTentacle02b");
        this.rBodyTentacle04b = this.rBodyTentacle04a.getChild("rBodyTentacle04b");
        this.lBodyTentacle02b = this.lBodyTentacle02a.getChild("lBodyTentacle02b");
        this.lBackTentacle02b = this.lBackTentacle02a.getChild("lBackTentacle02b");
        this.lBodyTentacle03b = this.lBodyTentacle03a.getChild("lBodyTentacle03b");
        this.lBackTentacle02b_1 = this.lBackTentacle02a_1.getChild("lBackTentacle02b_1");
        this.rBodyTentacle02b = this.rBodyTentacle02a.getChild("rBodyTentacle02b");
        this.rBackTentacle02b_1 = this.rBackTentacle02a_1.getChild("rBackTentacle02b_1");
        this.rBodyTentacle01c = this.rBodyTentacle01b.getChild("rBodyTentacle01c");
        this.rHindTentacle03c = this.rHindTentacle03b.getChild("rHindTentacle03c");
        this.eye32 = this.rHindTentacle03b.getChild("eye32");
        this.rHindTentacle01c = this.rHindTentacle01b.getChild("rHindTentacle01c");
        this.lHindTentacle03c = this.lHindTentacle03b.getChild("lHindTentacle03c");
        this.eye23 = this.lHindTentacle02b.getChild("eye23");
        this.lHindTentacle02c = this.lHindTentacle02b.getChild("lHindTentacle02c");
        this.rHindTentacle02c = this.rHindTentacle02b.getChild("rHindTentacle02c");
        this.tail01c = this.tail01b.getChild("tail01c");
        this.eye27 = this.tail01b.getChild("eye27");
        this.mouth01Teeth03 = this.mouth01Lower.getChild("mouth01Teeth03");
        this.mouth01Teeth02 = this.mouth01Lower.getChild("mouth01Teeth02");
        this.mouth02Teeth02 = this.mouth02Lower.getChild("mouth02Teeth02");
        this.mouth02Teeth03 = this.mouth02Lower.getChild("mouth02Teeth03");
        this.mouth02Teeth03_1 = this.mouth03Lower.getChild("mouth02Teeth03_1");
        this.mouth03Teeth02 = this.mouth03Lower.getChild("mouth03Teeth02");
        this.lHindTentacle01d = this.lHindTentacle01c.getChild("lHindTentacle01d");
        this.lBackTentacle01c = this.lBackTentacle01b.getChild("lBackTentacle01c");
        this.lArm01c = this.lArm01b.getChild("lArm01c");
        this.rBackTentacle01c = this.rBackTentacle01b.getChild("rBackTentacle01c");
        this.rArm01c = this.rArm01b.getChild("rArm01c");
        this.eye17 = this.rArm01b.getChild("eye17");
        this.eye16 = this.rArm01b.getChild("eye16");
        this.rBackTentacle02c = this.rBackTentacle02b.getChild("rBackTentacle02c");
        this.lBodyTentacle02c = this.lBodyTentacle02b.getChild("lBodyTentacle02c");
        this.lBackTentacle02c = this.lBackTentacle02b.getChild("lBackTentacle02c");
        this.lBackTentacle02c_1 = this.lBackTentacle02b_1.getChild("lBackTentacle02c_1");
        this.rBodyTentacle02c = this.rBodyTentacle02b.getChild("rBodyTentacle02c");
        this.rBackTentacle02c_1 = this.rBackTentacle02b_1.getChild("rBackTentacle02c_1");
        this.eye29 = this.rHindTentacle01c.getChild("eye29");
        this.rHindTentacle01d = this.rHindTentacle01c.getChild("rHindTentacle01d");
        this.lHindTentacle02d = this.lHindTentacle02c.getChild("lHindTentacle02d");
        this.eye31 = this.rHindTentacle02c.getChild("eye31");
        this.rHindTentacle02d = this.rHindTentacle02c.getChild("rHindTentacle02d");
        this.tail01d = this.tail01c.getChild("tail01d");
        this.eye26 = this.tail01c.getChild("eye26");
        this.lHindTentacle01e = this.lHindTentacle01d.getChild("lHindTentacle01e");
        this.lArm01d = this.lArm01c.getChild("lArm01d");
        this.eye14 = this.lArm01c.getChild("eye14");
        this.rArm01d = this.rArm01c.getChild("rArm01d");
        this.rHindTentacle01e = this.rHindTentacle01d.getChild("rHindTentacle01e");
        this.eye30 = this.rHindTentacle01d.getChild("eye30");
        this.lHindTentacle02e = this.lHindTentacle02d.getChild("lHindTentacle02e");
        this.eye24 = this.lHindTentacle02d.getChild("eye24");
        this.rHindTentacle02e = this.rHindTentacle02d.getChild("rHindTentacle02e");
        this.tail01e = this.tail01d.getChild("tail01e");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition headJoint = root.addOrReplaceChild("headJoint",
                CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, -0.5F, -0.5F, 1, 1, 1),
                PartPose.offset(0.0F, 1.6F, -4.0F));
        PartDefinition bodyUpper = headJoint.addOrReplaceChild("bodyUpper",
                CubeListBuilder.create().texOffs(0, 1).addBox(-5.5F, -17.0F, -5.5F, 11, 17, 11),
                PartPose.offsetAndRotation(0.0F, 3.1F, 2.0F, 1.5707963267948966F, 0.0F, 0.0F));
        PartDefinition mouth01Upper = bodyUpper.addOrReplaceChild("mouth01Upper",
                CubeListBuilder.create().texOffs(59, 0).addBox(-4.0F, -6.0F, -1.0F, 8, 6, 2),
                PartPose.offset(0.0F, -15.3F, 0.3F));
        PartDefinition mouth01Lower = mouth01Upper.addOrReplaceChild("mouth01Lower",
                CubeListBuilder.create().texOffs(59, 11).addBox(-3.5F, -4.0F, -0.5F, 7, 5, 1),
                PartPose.offsetAndRotation(0.0F, -1.3F, -3.5F, 0.2617993877991494F, 0.0F, 0.0F));
        PartDefinition mouth01Teeth03 = mouth01Lower.addOrReplaceChild("mouth01Teeth03",
                CubeListBuilder.create().texOffs(96, 19).addBox(2.4F, -3.9F, 0.3F, 1, 4, 1),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition mouth01Teeth02 = mouth01Lower.addOrReplaceChild("mouth01Teeth02",
                CubeListBuilder.create().texOffs(80, 19).addBox(-3.4F, -3.9F, 0.3F, 6, 4, 1),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition eye05 = mouth01Upper.addOrReplaceChild("eye05",
                CubeListBuilder.create().texOffs(75, 30).addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2),
                PartPose.offset(-2.3F, -3.2F, 2.9F));
        PartDefinition mouth01Tooth05 = mouth01Upper.addOrReplaceChild("mouth01Tooth05",
                CubeListBuilder.create().mirror().texOffs(105, 10).addBox(-0.5F, -0.5F, -2.9F, 1, 1, 3),
                PartPose.offsetAndRotation(3.4F, -3.4F, -0.4F, 0.0F, -0.19198621771937624F, 0.5235987755982988F));
        PartDefinition mouth01Tooth03 = mouth01Upper.addOrReplaceChild("mouth01Tooth03",
                CubeListBuilder.create().texOffs(105, 10).addBox(-0.5F, -0.5F, -2.9F, 1, 1, 3),
                PartPose.offsetAndRotation(-3.1F, -3.4F, -0.4F, 0.0F, 0.19198621771937624F, 0.0F));
        PartDefinition mouth01Snout = mouth01Upper.addOrReplaceChild("mouth01Snout",
                CubeListBuilder.create().texOffs(81, 0).addBox(-3.5F, -4.5F, -1.5F, 7, 6, 3),
                PartPose.offsetAndRotation(0.0F, -1.3F, 2.4F, 0.5235987755982988F, 0.0F, 0.0F));
        PartDefinition mouth01Tooth02 = mouth01Upper.addOrReplaceChild("mouth01Tooth02",
                CubeListBuilder.create().texOffs(105, 10).addBox(-0.5F, -0.5F, -3.0F, 1, 1, 3),
                PartPose.offsetAndRotation(0.4F, -5.5F, -0.5F, -0.3141592653589793F, 0.2792526803190927F, 0.7853981633974483F));
        PartDefinition mouth01Tooth04 = mouth01Upper.addOrReplaceChild("mouth01Tooth04",
                CubeListBuilder.create().texOffs(105, 10).addBox(-0.5F, -0.5F, -2.2F, 1, 1, 3),
                PartPose.offsetAndRotation(-3.1F, -5.3F, -0.5F, 0.0F, 0.0F, 0.2617993877991494F));
        PartDefinition mouth01Tooth01 = mouth01Upper.addOrReplaceChild("mouth01Tooth01",
                CubeListBuilder.create().mirror().texOffs(105, 17).addBox(-0.5F, -0.5F, -3.7F, 1, 1, 5),
                PartPose.offsetAndRotation(-2.0F, -5.5F, -0.7F, -0.3141592653589793F, -0.2792526803190927F, -0.7853981633974483F));
        PartDefinition eye04_1 = mouth01Upper.addOrReplaceChild("eye04_1",
                CubeListBuilder.create().texOffs(75, 30).addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2),
                PartPose.offset(2.3F, -3.2F, 2.9F));
        PartDefinition mouth01Tooth06 = mouth01Upper.addOrReplaceChild("mouth01Tooth06",
                CubeListBuilder.create().texOffs(105, 17).addBox(-0.5F, -0.5F, -4.4F, 1, 1, 5),
                PartPose.offsetAndRotation(2.7F, -5.5F, -0.5F, -0.3141592653589793F, 0.2792526803190927F, 0.7853981633974483F));
        PartDefinition mouth01Teeth01 = mouth01Upper.addOrReplaceChild("mouth01Teeth01",
                CubeListBuilder.create().texOffs(79, 10).addBox(-3.5F, -0.7F, -2.9F, 7, 5, 2),
                PartPose.offset(0.0F, -5.0F, 0.0F));
        PartDefinition eye09 = bodyUpper.addOrReplaceChild("eye09",
                CubeListBuilder.create().texOffs(61, 30).addBox(-1.5F, -1.5F, -1.5F, 3, 3, 3),
                PartPose.offset(-1.2F, -11.9F, 6.1F));
        PartDefinition eye01 = bodyUpper.addOrReplaceChild("eye01",
                CubeListBuilder.create().texOffs(61, 30).addBox(-1.5F, -1.5F, -1.5F, 3, 3, 3),
                PartPose.offset(5.5F, -14.0F, 5.0F));
        PartDefinition eye11 = bodyUpper.addOrReplaceChild("eye11",
                CubeListBuilder.create().texOffs(75, 30).addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2),
                PartPose.offset(-2.1F, -3.9F, 6.3F));
        PartDefinition eye03 = bodyUpper.addOrReplaceChild("eye03",
                CubeListBuilder.create().texOffs(61, 30).addBox(-1.5F, -1.5F, -1.5F, 3, 3, 3),
                PartPose.offset(5.6F, -5.4F, 2.6F));
        PartDefinition eye02 = bodyUpper.addOrReplaceChild("eye02",
                CubeListBuilder.create().texOffs(61, 30).addBox(-1.5F, -1.5F, -1.5F, 3, 3, 3),
                PartPose.offset(-5.5F, -14.0F, 5.0F));
        PartDefinition mouth02Upper = bodyUpper.addOrReplaceChild("mouth02Upper",
                CubeListBuilder.create().texOffs(59, 0).addBox(-4.0F, -6.0F, -1.0F, 8, 6, 2),
                PartPose.offsetAndRotation(3.8F, -12.2F, -1.8F, 0.0F, 0.0F, 0.9599310885968813F));
        PartDefinition mouth02Snout = mouth02Upper.addOrReplaceChild("mouth02Snout",
                CubeListBuilder.create().texOffs(81, 0).addBox(-3.5F, -4.5F, -1.5F, 7, 6, 3),
                PartPose.offsetAndRotation(0.0F, -1.3F, 2.4F, 0.5235987755982988F, 0.0F, 0.0F));
        PartDefinition mouth02Lower = mouth02Upper.addOrReplaceChild("mouth02Lower",
                CubeListBuilder.create().texOffs(59, 11).addBox(-3.5F, -4.0F, -0.5F, 7, 5, 1),
                PartPose.offsetAndRotation(0.0F, -1.3F, -3.5F, 0.2617993877991494F, 0.0F, 0.0F));
        PartDefinition mouth02Teeth02 = mouth02Lower.addOrReplaceChild("mouth02Teeth02",
                CubeListBuilder.create().texOffs(80, 19).addBox(-3.4F, -3.9F, 0.3F, 6, 4, 1),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition mouth02Teeth03 = mouth02Lower.addOrReplaceChild("mouth02Teeth03",
                CubeListBuilder.create().texOffs(96, 19).addBox(2.4F, -3.9F, 0.3F, 1, 4, 1),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition mouth02Tooth02 = mouth02Upper.addOrReplaceChild("mouth02Tooth02",
                CubeListBuilder.create().texOffs(105, 17).addBox(-0.5F, -0.5F, -4.4F, 1, 1, 5),
                PartPose.offsetAndRotation(2.7F, -5.5F, -0.5F, -0.3141592653589793F, 0.2792526803190927F, 0.7853981633974483F));
        PartDefinition mouth02Tooth01 = mouth02Upper.addOrReplaceChild("mouth02Tooth01",
                CubeListBuilder.create().texOffs(105, 10).addBox(-0.5F, -0.5F, -3.0F, 1, 1, 3),
                PartPose.offsetAndRotation(0.3F, -5.5F, -0.5F, -0.3141592653589793F, 0.2792526803190927F, 0.7853981633974483F));
        PartDefinition eye06 = mouth02Upper.addOrReplaceChild("eye06",
                CubeListBuilder.create().texOffs(61, 30).addBox(-1.5F, -1.5F, -1.5F, 3, 3, 3),
                PartPose.offset(0.4F, -3.0F, 3.5F));
        PartDefinition mouth02Tooth03 = mouth02Upper.addOrReplaceChild("mouth02Tooth03",
                CubeListBuilder.create().mirror().texOffs(105, 17).addBox(-0.5F, -0.5F, -3.7F, 1, 1, 5),
                PartPose.offsetAndRotation(-3.4F, -5.5F, -0.7F, -0.3141592653589793F, -0.2792526803190927F, -0.7853981633974483F));
        PartDefinition mouth02Teeth01 = mouth02Upper.addOrReplaceChild("mouth02Teeth01",
                CubeListBuilder.create().texOffs(79, 10).addBox(-3.5F, -0.7F, -2.9F, 7, 5, 2),
                PartPose.offset(0.0F, -5.0F, 0.0F));
        PartDefinition mouth02Tooth04 = mouth02Upper.addOrReplaceChild("mouth02Tooth04",
                CubeListBuilder.create().mirror().texOffs(105, 17).addBox(-0.5F, -0.5F, -4.7F, 1, 1, 5),
                PartPose.offsetAndRotation(-1.6F, -5.5F, -0.6F, -0.3141592653589793F, -0.2792526803190927F, -0.7853981633974483F));
        PartDefinition mouth02Tooth04_1 = mouth02Upper.addOrReplaceChild("mouth02Tooth04_1",
                CubeListBuilder.create().texOffs(105, 10).addBox(-0.5F, -0.5F, -2.9F, 1, 1, 3),
                PartPose.offsetAndRotation(-3.4F, -3.4F, -0.4F, 0.19198621771937624F, -0.19198621771937624F, 0.8726646259971648F));
        PartDefinition eye12 = bodyUpper.addOrReplaceChild("eye12",
                CubeListBuilder.create().texOffs(75, 30).addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2),
                PartPose.offset(-6.0F, -6.4F, 3.9F));
        PartDefinition eye10 = bodyUpper.addOrReplaceChild("eye10",
                CubeListBuilder.create().texOffs(61, 30).addBox(-1.5F, -1.5F, -1.5F, 3, 3, 3),
                PartPose.offset(3.4F, -7.7F, 6.0F));
        PartDefinition eye04 = bodyUpper.addOrReplaceChild("eye04",
                CubeListBuilder.create().texOffs(75, 30).addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2),
                PartPose.offset(4.8F, -16.8F, 2.7F));
        PartDefinition mouth03Upper = bodyUpper.addOrReplaceChild("mouth03Upper",
                CubeListBuilder.create().mirror().texOffs(59, 0).addBox(-4.0F, -6.0F, -1.0F, 8, 6, 2),
                PartPose.offsetAndRotation(-3.8F, -12.2F, -1.8F, 0.0F, 0.0F, -0.9599310885968813F));
        PartDefinition mouth03Snout = mouth03Upper.addOrReplaceChild("mouth03Snout",
                CubeListBuilder.create().mirror().texOffs(81, 0).addBox(-3.5F, -4.5F, -1.5F, 7, 6, 3),
                PartPose.offsetAndRotation(0.0F, -1.3F, 2.4F, 0.5235987755982988F, 0.0F, 0.0F));
        PartDefinition mouth03Teeth01 = mouth03Upper.addOrReplaceChild("mouth03Teeth01",
                CubeListBuilder.create().mirror().texOffs(79, 10).addBox(-3.5F, -0.7F, -2.9F, 7, 5, 2),
                PartPose.offset(0.0F, -5.0F, 0.0F));
        PartDefinition mouth03Tooth05 = mouth03Upper.addOrReplaceChild("mouth03Tooth05",
                CubeListBuilder.create().mirror().texOffs(105, 17).addBox(-0.5F, -0.5F, -3.8F, 1, 1, 5),
                PartPose.offsetAndRotation(3.4F, -5.1F, -0.4F, 0.08726646259971647F, -0.3141592653589793F, 0.5235987755982988F));
        PartDefinition mouth03Tooth02 = mouth03Upper.addOrReplaceChild("mouth03Tooth02",
                CubeListBuilder.create().texOffs(105, 10).addBox(-0.5F, -0.5F, -3.0F, 1, 1, 3),
                PartPose.offsetAndRotation(0.2F, -5.5F, -0.4F, -0.3141592653589793F, 0.2792526803190927F, 0.7853981633974483F));
        PartDefinition mouth03Lower = mouth03Upper.addOrReplaceChild("mouth03Lower",
                CubeListBuilder.create().mirror().texOffs(59, 11).addBox(-3.5F, -4.0F, -0.5F, 7, 5, 1),
                PartPose.offsetAndRotation(0.0F, -1.3F, -3.5F, 0.2617993877991494F, 0.0F, 0.0F));
        PartDefinition mouth02Teeth03_1 = mouth03Lower.addOrReplaceChild("mouth02Teeth03_1",
                CubeListBuilder.create().mirror().texOffs(96, 19).addBox(-3.3F, -3.9F, 0.3F, 1, 4, 1),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition mouth03Teeth02 = mouth03Lower.addOrReplaceChild("mouth03Teeth02",
                CubeListBuilder.create().mirror().texOffs(80, 19).addBox(-2.7F, -3.9F, 0.3F, 6, 4, 1),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition eye08 = mouth03Upper.addOrReplaceChild("eye08",
                CubeListBuilder.create().texOffs(75, 30).addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2),
                PartPose.offset(3.3F, -4.8F, 1.4F));
        PartDefinition mouth03Tooth01 = mouth03Upper.addOrReplaceChild("mouth03Tooth01",
                CubeListBuilder.create().mirror().texOffs(105, 17).addBox(-0.5F, -0.5F, -4.8F, 1, 1, 5),
                PartPose.offsetAndRotation(-1.7F, -5.5F, -0.7F, -0.3141592653589793F, -0.2792526803190927F, -0.7853981633974483F));
        PartDefinition mouth03Tooth04 = mouth03Upper.addOrReplaceChild("mouth03Tooth04",
                CubeListBuilder.create().texOffs(105, 10).addBox(-0.5F, -0.5F, -3.0F, 1, 1, 3),
                PartPose.offsetAndRotation(2.2F, -5.5F, -0.4F, -0.41887902047863906F, 0.08726646259971647F, 0.7853981633974483F));
        PartDefinition eye07 = mouth03Upper.addOrReplaceChild("eye07",
                CubeListBuilder.create().texOffs(75, 30).addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2),
                PartPose.offset(-1.8F, -3.0F, 3.5F));
        PartDefinition mouth03Tooth03 = mouth03Upper.addOrReplaceChild("mouth03Tooth03",
                CubeListBuilder.create().mirror().texOffs(105, 10).addBox(-0.5F, -0.5F, -3.0F, 1, 1, 3),
                PartPose.offsetAndRotation(-3.3F, -5.5F, -0.4F, -0.3141592653589793F, 0.19198621771937624F, -0.9560913642424937F));
        PartDefinition bodyBase = root.addOrReplaceChild("bodyBase",
                CubeListBuilder.create().texOffs(0, 64).addBox(-7.0F, 0.0F, -4.0F, 14, 12, 16),
                PartPose.offset(0.0F, 12.0F, 0.0F));
        PartDefinition lHindTentacle01a = bodyBase.addOrReplaceChild("lHindTentacle01a",
                CubeListBuilder.create().texOffs(0, 96).addBox(-3.0F, -3.0F, 0.0F, 6, 6, 10),
                PartPose.offsetAndRotation(4.1F, 6.7F, 4.9F, -0.13962634015954636F, 0.20943951023931953F, 0.0F));
        PartDefinition lHindTentacle01b = lHindTentacle01a.addOrReplaceChild("lHindTentacle01b",
                CubeListBuilder.create().texOffs(34, 96).addBox(-2.5F, -2.5F, 0.0F, 5, 5, 8),
                PartPose.offsetAndRotation(0.0F, 0.4F, 9.5F, 0.0F, -0.13962634015954636F, 0.0F));
        PartDefinition lHindTentacle01c = lHindTentacle01b.addOrReplaceChild("lHindTentacle01c",
                CubeListBuilder.create().texOffs(63, 96).addBox(-2.0F, -2.0F, 0.0F, 4, 4, 8),
                PartPose.offsetAndRotation(0.0F, 0.4F, 7.5F, 0.13962634015954636F, -0.06981317007977318F, 0.0F));
        PartDefinition lHindTentacle01d = lHindTentacle01c.addOrReplaceChild("lHindTentacle01d",
                CubeListBuilder.create().texOffs(92, 96).addBox(-1.5F, -1.5F, 0.0F, 3, 3, 9),
                PartPose.offsetAndRotation(0.0F, 0.4F, 7.6F, 0.0F, -0.05235987755982988F, 0.0F));
        PartDefinition lHindTentacle01e = lHindTentacle01d.addOrReplaceChild("lHindTentacle01e",
                CubeListBuilder.create().texOffs(0, 113).addBox(-1.0F, -1.0F, 0.0F, 2, 2, 10),
                PartPose.offsetAndRotation(0.0F, 0.4F, 8.9F, 0.0F, 0.06981317007977318F, 0.0F));
        PartDefinition eye25 = lHindTentacle01b.addOrReplaceChild("eye25",
                CubeListBuilder.create().texOffs(61, 30).addBox(-1.5F, -1.5F, -1.5F, 3, 3, 3),
                PartPose.offset(0.8F, -2.8F, 3.8F));
        PartDefinition lBodyTentacle01a = bodyBase.addOrReplaceChild("lBodyTentacle01a",
                CubeListBuilder.create().texOffs(63, 96).addBox(-2.0F, -2.0F, -8.0F, 4, 4, 8),
                PartPose.offsetAndRotation(4.8F, 4.1F, 0.7F, 0.3665191429188092F, -0.7853981633974483F, 0.0F));
        PartDefinition lBodyTentacle01b = lBodyTentacle01a.addOrReplaceChild("lBodyTentacle01b",
                CubeListBuilder.create().texOffs(93, 97).addBox(-1.5F, -1.5F, -8.0F, 3, 3, 8),
                PartPose.offsetAndRotation(0.0F, 0.0F, -7.0F, 0.05235987755982988F, 0.3665191429188092F, 0.0F));
        PartDefinition lBodyTentacle01c = lBodyTentacle01b.addOrReplaceChild("lBodyTentacle01c",
                CubeListBuilder.create().texOffs(1, 114).addBox(-1.0F, -1.0F, -9.0F, 2, 2, 9),
                PartPose.offsetAndRotation(0.0F, 0.0F, -7.2F, 0.45378560551852565F, -0.08726646259971647F, 0.0F));
        PartDefinition eye22 = lBodyTentacle01a.addOrReplaceChild("eye22",
                CubeListBuilder.create().texOffs(75, 30).addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2),
                PartPose.offset(1.6F, -1.7F, -3.4F));
        PartDefinition bodyMid = bodyBase.addOrReplaceChild("bodyMid",
                CubeListBuilder.create().texOffs(0, 31).addBox(-6.5F, -16.0F, -7.0F, 13, 18, 14),
                PartPose.offsetAndRotation(0.0F, 3.6F, 4.4F, 0.7853981633974483F, 0.0F, 0.0F));
        PartDefinition lBodyTentacle04a = bodyMid.addOrReplaceChild("lBodyTentacle04a",
                CubeListBuilder.create().texOffs(6, 119).addBox(-1.0F, -1.0F, -4.0F, 2, 2, 4),
                PartPose.offsetAndRotation(1.5F, -6.0F, -6.7F, 0.2792526803190927F, -0.10471975511965977F, 0.0F));
        PartDefinition lBodyTentacle04b = lBodyTentacle04a.addOrReplaceChild("lBodyTentacle04b",
                CubeListBuilder.create().texOffs(17, 115).addBox(-0.5F, -0.5F, -4.0F, 1, 1, 4),
                PartPose.offsetAndRotation(0.0F, 0.0F, -3.8F, 0.3141592653589793F, 0.0F, 0.0F));
        PartDefinition lBackTentacle01a = bodyMid.addOrReplaceChild("lBackTentacle01a",
                CubeListBuilder.create().texOffs(94, 98).addBox(-1.5F, -1.5F, 0.0F, 3, 3, 7),
                PartPose.offsetAndRotation(4.7F, -8.9F, 3.4F, -0.5235987755982988F, 0.5235987755982988F, -0.2792526803190927F));
        PartDefinition lBackTentacle01b = lBackTentacle01a.addOrReplaceChild("lBackTentacle01b",
                CubeListBuilder.create().texOffs(3, 116).addBox(-1.0F, -1.0F, 0.0F, 2, 2, 7),
                PartPose.offsetAndRotation(0.0F, 0.0F, 6.3F, -0.41887902047863906F, -0.22689280275926282F, 0.0F));
        PartDefinition lBackTentacle01c = lBackTentacle01b.addOrReplaceChild("lBackTentacle01c",
                CubeListBuilder.create().texOffs(16, 114).addBox(-0.5F, -0.5F, 0.0F, 1, 1, 5),
                PartPose.offsetAndRotation(0.0F, 0.0F, 6.6F, -0.13962634015954636F, -0.22689280275926282F, 0.0F));
        PartDefinition eye21 = bodyMid.addOrReplaceChild("eye21",
                CubeListBuilder.create().texOffs(61, 30).addBox(-1.5F, -1.5F, -1.5F, 3, 3, 3),
                PartPose.offset(6.7F, -10.7F, 1.7F));
        PartDefinition rBodyTentacle03a = bodyMid.addOrReplaceChild("rBodyTentacle03a",
                CubeListBuilder.create().mirror().texOffs(6, 119).addBox(-1.0F, -1.0F, -4.0F, 2, 2, 4),
                PartPose.offsetAndRotation(-4.8F, -7.8F, -6.6F, 0.13962634015954636F, 0.4363323129985824F, 0.0F));
        PartDefinition rBodyTentacle03b = rBodyTentacle03a.addOrReplaceChild("rBodyTentacle03b",
                CubeListBuilder.create().mirror().texOffs(17, 114).addBox(-0.5F, -0.5F, -5.0F, 1, 1, 5),
                PartPose.offsetAndRotation(0.0F, 0.0F, -3.8F, 0.19198621771937624F, -0.2617993877991494F, 0.0F));
        PartDefinition lArm01a = bodyMid.addOrReplaceChild("lArm01a",
                CubeListBuilder.create().texOffs(34, 96).addBox(-2.5F, -2.5F, -8.1F, 5, 5, 8),
                PartPose.offsetAndRotation(4.0F, -11.9F, -2.7F, -0.24434609527920614F, -0.3665191429188092F, 0.0F));
        PartDefinition lArm01b = lArm01a.addOrReplaceChild("lArm01b",
                CubeListBuilder.create().texOffs(63, 96).addBox(-2.0F, -2.0F, -8.0F, 4, 4, 8),
                PartPose.offsetAndRotation(0.0F, 0.0F, -7.8F, 0.06981317007977318F, 0.06981317007977318F, 0.0F));
        PartDefinition lArm01c = lArm01b.addOrReplaceChild("lArm01c",
                CubeListBuilder.create().texOffs(93, 97).addBox(-1.5F, -1.5F, -8.0F, 3, 3, 8),
                PartPose.offsetAndRotation(0.0F, 0.0F, -7.0F, 0.17453292519943295F, 0.13962634015954636F, 0.0F));
        PartDefinition lArm01d = lArm01c.addOrReplaceChild("lArm01d",
                CubeListBuilder.create().texOffs(1, 114).addBox(-1.0F, -1.0F, -9.0F, 2, 2, 9),
                PartPose.offsetAndRotation(0.0F, 0.0F, -7.2F, 0.13962634015954636F, 0.13962634015954636F, 0.0F));
        PartDefinition eye14 = lArm01c.addOrReplaceChild("eye14",
                CubeListBuilder.create().texOffs(75, 30).addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2),
                PartPose.offset(-1.0F, -1.2F, -3.8F));
        PartDefinition eye15 = lArm01a.addOrReplaceChild("eye15",
                CubeListBuilder.create().texOffs(75, 30).addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2),
                PartPose.offset(2.6F, 1.1F, -7.6F));
        PartDefinition eye13 = lArm01a.addOrReplaceChild("eye13",
                CubeListBuilder.create().texOffs(75, 30).addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2),
                PartPose.offset(2.6F, -2.0F, -3.8F));
        PartDefinition rBackTentacle01a = bodyMid.addOrReplaceChild("rBackTentacle01a",
                CubeListBuilder.create().mirror().texOffs(94, 98).addBox(-1.5F, -1.5F, 0.0F, 3, 3, 7),
                PartPose.offsetAndRotation(-4.7F, -8.9F, 3.4F, -0.5235987755982988F, -0.5235987755982988F, 0.2792526803190927F));
        PartDefinition rBackTentacle01b = rBackTentacle01a.addOrReplaceChild("rBackTentacle01b",
                CubeListBuilder.create().mirror().texOffs(3, 116).addBox(-1.0F, -1.0F, 0.0F, 2, 2, 7),
                PartPose.offsetAndRotation(0.0F, 0.0F, 6.3F, -0.41887902047863906F, 0.22689280275926282F, 0.0F));
        PartDefinition rBackTentacle01c = rBackTentacle01b.addOrReplaceChild("rBackTentacle01c",
                CubeListBuilder.create().mirror().texOffs(16, 114).addBox(-0.5F, -0.5F, 0.0F, 1, 1, 5),
                PartPose.offsetAndRotation(0.0F, 0.0F, 6.6F, -0.13962634015954636F, 0.22689280275926282F, 0.0F));
        PartDefinition rArm01a = bodyMid.addOrReplaceChild("rArm01a",
                CubeListBuilder.create().mirror().texOffs(34, 96).addBox(-2.5F, -2.5F, -8.1F, 5, 5, 8),
                PartPose.offsetAndRotation(-4.0F, -11.9F, -2.7F, -0.24434609527920614F, 0.3665191429188092F, 0.0F));
        PartDefinition rArm01b = rArm01a.addOrReplaceChild("rArm01b",
                CubeListBuilder.create().mirror().texOffs(63, 96).addBox(-2.0F, -2.0F, -8.0F, 4, 4, 8),
                PartPose.offsetAndRotation(0.0F, 0.0F, -7.8F, 0.06981317007977318F, -0.06981317007977318F, 0.0F));
        PartDefinition rArm01c = rArm01b.addOrReplaceChild("rArm01c",
                CubeListBuilder.create().mirror().texOffs(93, 97).addBox(-1.5F, -1.5F, -8.0F, 3, 3, 8),
                PartPose.offsetAndRotation(0.0F, 0.0F, -7.0F, 0.17453292519943295F, -0.13962634015954636F, 0.0F));
        PartDefinition rArm01d = rArm01c.addOrReplaceChild("rArm01d",
                CubeListBuilder.create().mirror().texOffs(1, 114).addBox(-1.0F, -1.0F, -9.0F, 2, 2, 9),
                PartPose.offsetAndRotation(0.0F, 0.0F, -7.2F, 0.13962634015954636F, -0.13962634015954636F, 0.0F));
        PartDefinition eye17 = rArm01b.addOrReplaceChild("eye17",
                CubeListBuilder.create().texOffs(75, 30).addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2),
                PartPose.offset(2.0F, 1.1F, -8.2F));
        PartDefinition eye16 = rArm01b.addOrReplaceChild("eye16",
                CubeListBuilder.create().texOffs(61, 30).addBox(-1.5F, -1.5F, -1.5F, 3, 3, 3),
                PartPose.offset(-1.6F, -1.9F, -5.1F));
        PartDefinition eye18 = rArm01a.addOrReplaceChild("eye18",
                CubeListBuilder.create().texOffs(61, 30).addBox(-1.5F, -1.5F, -1.5F, 3, 3, 3),
                PartPose.offset(-2.6F, 1.7F, -3.4F));
        PartDefinition eye20 = bodyMid.addOrReplaceChild("eye20",
                CubeListBuilder.create().texOffs(61, 30).addBox(-1.5F, -1.5F, -1.5F, 3, 3, 3),
                PartPose.offset(-6.7F, -6.6F, 0.7F));
        PartDefinition rBackTentacle02a = bodyMid.addOrReplaceChild("rBackTentacle02a",
                CubeListBuilder.create().mirror().texOffs(94, 98).addBox(-1.5F, -1.5F, 0.0F, 3, 3, 7),
                PartPose.offsetAndRotation(-5.4F, -0.7F, 5.8F, -0.7330382858376184F, -0.3665191429188092F, 0.0F));
        PartDefinition rBackTentacle02b = rBackTentacle02a.addOrReplaceChild("rBackTentacle02b",
                CubeListBuilder.create().mirror().texOffs(4, 117).addBox(-1.0F, -1.0F, 0.0F, 2, 2, 6),
                PartPose.offsetAndRotation(0.0F, 0.0F, 6.3F, -0.41887902047863906F, 0.08726646259971647F, 0.0F));
        PartDefinition rBackTentacle02c = rBackTentacle02b.addOrReplaceChild("rBackTentacle02c",
                CubeListBuilder.create().mirror().texOffs(16, 114).addBox(-0.5F, -0.5F, 0.0F, 1, 1, 5),
                PartPose.offsetAndRotation(0.0F, 0.0F, 5.8F, -0.13962634015954636F, -0.13962634015954636F, 0.0F));
        PartDefinition eye19 = bodyMid.addOrReplaceChild("eye19",
                CubeListBuilder.create().texOffs(61, 30).addBox(-1.5F, -1.5F, -1.5F, 3, 3, 3),
                PartPose.offset(-6.7F, -13.3F, 3.9F));
        PartDefinition rBodyTentacle04a = bodyMid.addOrReplaceChild("rBodyTentacle04a",
                CubeListBuilder.create().mirror().texOffs(6, 119).addBox(-1.0F, -1.0F, -4.0F, 2, 2, 4),
                PartPose.offsetAndRotation(-1.5F, -6.0F, -6.7F, 0.2792526803190927F, 0.10471975511965977F, 0.0F));
        PartDefinition rBodyTentacle04b = rBodyTentacle04a.addOrReplaceChild("rBodyTentacle04b",
                CubeListBuilder.create().mirror().texOffs(17, 115).addBox(-0.5F, -0.5F, -4.0F, 1, 1, 4),
                PartPose.offsetAndRotation(0.0F, 0.0F, -3.8F, 0.3141592653589793F, 0.0F, 0.0F));
        PartDefinition lBodyTentacle02a = bodyMid.addOrReplaceChild("lBodyTentacle02a",
                CubeListBuilder.create().texOffs(96, 100).addBox(-1.5F, -1.5F, -5.0F, 3, 3, 5),
                PartPose.offsetAndRotation(3.5F, -10.4F, -6.8F, 0.05235987755982988F, -0.10471975511965977F, 0.0F));
        PartDefinition lBodyTentacle02b = lBodyTentacle02a.addOrReplaceChild("lBodyTentacle02b",
                CubeListBuilder.create().texOffs(5, 118).addBox(-1.0F, -1.0F, -5.0F, 2, 2, 5),
                PartPose.offsetAndRotation(0.0F, 0.3F, -4.7F, 0.19198621771937624F, 0.08726646259971647F, 0.0F));
        PartDefinition lBodyTentacle02c = lBodyTentacle02b.addOrReplaceChild("lBodyTentacle02c",
                CubeListBuilder.create().texOffs(17, 115).addBox(-0.5F, -0.5F, -4.0F, 1, 1, 4),
                PartPose.offsetAndRotation(0.0F, 0.0F, -4.8F, 0.10471975511965977F, 0.0F, 0.0F));
        PartDefinition lBackTentacle02a = bodyMid.addOrReplaceChild("lBackTentacle02a",
                CubeListBuilder.create().texOffs(95, 99).addBox(-1.5F, -1.5F, 0.0F, 3, 3, 6),
                PartPose.offsetAndRotation(3.5F, -14.5F, 5.9F, -0.5235987755982988F, 0.10471975511965977F, 0.0F));
        PartDefinition lBackTentacle02b = lBackTentacle02a.addOrReplaceChild("lBackTentacle02b",
                CubeListBuilder.create().texOffs(5, 118).addBox(-1.0F, -1.0F, 0.0F, 2, 2, 5),
                PartPose.offsetAndRotation(0.0F, 0.0F, 5.2F, -0.24434609527920614F, -0.08726646259971647F, 0.0F));
        PartDefinition lBackTentacle02c = lBackTentacle02b.addOrReplaceChild("lBackTentacle02c",
                CubeListBuilder.create().texOffs(17, 115).addBox(-0.5F, -0.5F, 0.0F, 1, 1, 4),
                PartPose.offsetAndRotation(0.0F, 0.0F, 4.7F, 0.15707963267948966F, 0.13962634015954636F, 0.0F));
        PartDefinition lBodyTentacle03a = bodyMid.addOrReplaceChild("lBodyTentacle03a",
                CubeListBuilder.create().texOffs(6, 119).addBox(-1.0F, -1.0F, -4.0F, 2, 2, 4),
                PartPose.offsetAndRotation(4.8F, -7.8F, -6.6F, 0.13962634015954636F, -0.4363323129985824F, 0.0F));
        PartDefinition lBodyTentacle03b = lBodyTentacle03a.addOrReplaceChild("lBodyTentacle03b",
                CubeListBuilder.create().texOffs(17, 114).addBox(-0.5F, -0.5F, -5.0F, 1, 1, 5),
                PartPose.offsetAndRotation(0.0F, 0.0F, -3.8F, 0.19198621771937624F, 0.2617993877991494F, 0.0F));
        PartDefinition lBackTentacle02a_1 = bodyMid.addOrReplaceChild("lBackTentacle02a_1",
                CubeListBuilder.create().texOffs(94, 98).addBox(-1.5F, -1.5F, 0.0F, 3, 3, 7),
                PartPose.offsetAndRotation(5.4F, -0.7F, 5.8F, -0.7330382858376184F, 0.3665191429188092F, 0.0F));
        PartDefinition lBackTentacle02b_1 = lBackTentacle02a_1.addOrReplaceChild("lBackTentacle02b_1",
                CubeListBuilder.create().texOffs(4, 117).addBox(-1.0F, -1.0F, 0.0F, 2, 2, 6),
                PartPose.offsetAndRotation(0.0F, 0.0F, 6.3F, -0.41887902047863906F, -0.08726646259971647F, 0.0F));
        PartDefinition lBackTentacle02c_1 = lBackTentacle02b_1.addOrReplaceChild("lBackTentacle02c_1",
                CubeListBuilder.create().texOffs(16, 114).addBox(-0.5F, -0.5F, 0.0F, 1, 1, 5),
                PartPose.offsetAndRotation(0.0F, 0.0F, 5.8F, -0.13962634015954636F, 0.13962634015954636F, 0.0F));
        PartDefinition rBodyTentacle02a = bodyMid.addOrReplaceChild("rBodyTentacle02a",
                CubeListBuilder.create().mirror().texOffs(96, 100).addBox(-1.5F, -1.5F, -5.0F, 3, 3, 5),
                PartPose.offsetAndRotation(-3.5F, -10.4F, -6.8F, 0.05235987755982988F, 0.10471975511965977F, 0.0F));
        PartDefinition rBodyTentacle02b = rBodyTentacle02a.addOrReplaceChild("rBodyTentacle02b",
                CubeListBuilder.create().mirror().texOffs(5, 118).addBox(-1.0F, -1.0F, -5.0F, 2, 2, 5),
                PartPose.offsetAndRotation(0.0F, 0.3F, -4.7F, 0.19198621771937624F, -0.08726646259971647F, 0.0F));
        PartDefinition rBodyTentacle02c = rBodyTentacle02b.addOrReplaceChild("rBodyTentacle02c",
                CubeListBuilder.create().mirror().texOffs(17, 115).addBox(-0.5F, -0.5F, -4.0F, 1, 1, 4),
                PartPose.offsetAndRotation(0.0F, 0.0F, -4.8F, 0.10471975511965977F, 0.0F, 0.0F));
        PartDefinition rBackTentacle02a_1 = bodyMid.addOrReplaceChild("rBackTentacle02a_1",
                CubeListBuilder.create().mirror().texOffs(95, 99).addBox(-1.5F, -1.5F, 0.0F, 3, 3, 6),
                PartPose.offsetAndRotation(-3.5F, -14.5F, 5.9F, -0.5235987755982988F, -0.10471975511965977F, 0.0F));
        PartDefinition rBackTentacle02b_1 = rBackTentacle02a_1.addOrReplaceChild("rBackTentacle02b_1",
                CubeListBuilder.create().mirror().texOffs(5, 118).addBox(-1.0F, -1.0F, 0.0F, 2, 2, 5),
                PartPose.offsetAndRotation(0.0F, 0.0F, 5.2F, -0.24434609527920614F, 0.08726646259971647F, 0.0F));
        PartDefinition rBackTentacle02c_1 = rBackTentacle02b_1.addOrReplaceChild("rBackTentacle02c_1",
                CubeListBuilder.create().mirror().texOffs(17, 115).addBox(-0.5F, -0.5F, 0.0F, 1, 1, 4),
                PartPose.offsetAndRotation(0.0F, 0.0F, 4.7F, 0.15707963267948966F, -0.13962634015954636F, 0.0F));
        PartDefinition rBodyTentacle01a = bodyBase.addOrReplaceChild("rBodyTentacle01a",
                CubeListBuilder.create().mirror().texOffs(63, 96).addBox(-2.0F, -2.0F, -8.0F, 4, 4, 8),
                PartPose.offsetAndRotation(-4.8F, 4.1F, 0.7F, 0.3665191429188092F, 0.7853981633974483F, 0.0F));
        PartDefinition rBodyTentacle01b = rBodyTentacle01a.addOrReplaceChild("rBodyTentacle01b",
                CubeListBuilder.create().mirror().texOffs(93, 97).addBox(-1.5F, -1.5F, -8.0F, 3, 3, 8),
                PartPose.offsetAndRotation(0.0F, 0.0F, -7.0F, 0.05235987755982988F, -0.3665191429188092F, 0.0F));
        PartDefinition rBodyTentacle01c = rBodyTentacle01b.addOrReplaceChild("rBodyTentacle01c",
                CubeListBuilder.create().mirror().texOffs(1, 114).addBox(-1.0F, -1.0F, -9.0F, 2, 2, 9),
                PartPose.offsetAndRotation(0.0F, 0.0F, -7.2F, 0.45378560551852565F, 0.08726646259971647F, 0.0F));
        PartDefinition rHindTentacle03a = bodyBase.addOrReplaceChild("rHindTentacle03a",
                CubeListBuilder.create().mirror().texOffs(63, 96).addBox(-2.0F, -2.0F, 0.0F, 4, 4, 8),
                PartPose.offsetAndRotation(-4.2F, 10.0F, -3.5F, 0.0F, -1.0471975511965976F, 0.0F));
        PartDefinition rHindTentacle03b = rHindTentacle03a.addOrReplaceChild("rHindTentacle03b",
                CubeListBuilder.create().mirror().texOffs(92, 96).addBox(-1.5F, -1.5F, 0.0F, 3, 3, 9),
                PartPose.offsetAndRotation(0.0F, 0.4F, 7.2F, 0.0F, 0.5235987755982988F, 0.0F));
        PartDefinition rHindTentacle03c = rHindTentacle03b.addOrReplaceChild("rHindTentacle03c",
                CubeListBuilder.create().mirror().texOffs(0, 113).addBox(-1.0F, -1.0F, 0.0F, 2, 2, 10),
                PartPose.offsetAndRotation(0.0F, 0.4F, 8.6F, 0.0F, 0.2617993877991494F, 0.0F));
        PartDefinition eye32 = rHindTentacle03b.addOrReplaceChild("eye32",
                CubeListBuilder.create().texOffs(75, 30).addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2),
                PartPose.offset(-1.1F, -1.2F, 3.4F));
        PartDefinition rHindTentacle03 = rHindTentacle03a.addOrReplaceChild("rHindTentacle03",
                CubeListBuilder.create().mirror().texOffs(66, 99).addBox(-2.0F, -2.0F, -3.5F, 4, 4, 5),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7853981633974483F, 0.0F));
        PartDefinition rHindTentacle01a = bodyBase.addOrReplaceChild("rHindTentacle01a",
                CubeListBuilder.create().mirror().texOffs(0, 96).addBox(-3.0F, -3.0F, 0.0F, 6, 6, 10),
                PartPose.offsetAndRotation(-4.1F, 6.7F, 4.9F, -0.13962634015954636F, -0.20943951023931953F, 0.0F));
        PartDefinition rHindTentacle01b = rHindTentacle01a.addOrReplaceChild("rHindTentacle01b",
                CubeListBuilder.create().mirror().texOffs(34, 96).addBox(-2.5F, -2.5F, 0.0F, 5, 5, 8),
                PartPose.offsetAndRotation(0.0F, 0.4F, 9.5F, 0.0F, 0.13962634015954636F, 0.0F));
        PartDefinition rHindTentacle01c = rHindTentacle01b.addOrReplaceChild("rHindTentacle01c",
                CubeListBuilder.create().mirror().texOffs(63, 96).addBox(-2.0F, -2.0F, 0.0F, 4, 4, 8),
                PartPose.offsetAndRotation(0.0F, 0.4F, 7.5F, 0.13962634015954636F, 0.06981317007977318F, 0.0F));
        PartDefinition eye29 = rHindTentacle01c.addOrReplaceChild("eye29",
                CubeListBuilder.create().texOffs(61, 30).addBox(-1.5F, -1.5F, -1.5F, 3, 3, 3),
                PartPose.offset(-1.0F, -2.0F, 0.7F));
        PartDefinition rHindTentacle01d = rHindTentacle01c.addOrReplaceChild("rHindTentacle01d",
                CubeListBuilder.create().mirror().texOffs(92, 96).addBox(-1.5F, -1.5F, 0.0F, 3, 3, 9),
                PartPose.offsetAndRotation(0.0F, 0.4F, 7.6F, 0.0F, 0.05235987755982988F, 0.0F));
        PartDefinition rHindTentacle01e = rHindTentacle01d.addOrReplaceChild("rHindTentacle01e",
                CubeListBuilder.create().mirror().texOffs(0, 113).addBox(-1.0F, -1.0F, 0.0F, 2, 2, 10),
                PartPose.offsetAndRotation(0.0F, 0.4F, 8.9F, 0.0F, -0.06981317007977318F, 0.0F));
        PartDefinition eye30 = rHindTentacle01d.addOrReplaceChild("eye30",
                CubeListBuilder.create().texOffs(75, 30).addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2),
                PartPose.offset(-0.1F, -1.8F, 3.8F));
        PartDefinition lHindTentacle03a = bodyBase.addOrReplaceChild("lHindTentacle03a",
                CubeListBuilder.create().texOffs(63, 96).addBox(-2.0F, -2.0F, 0.0F, 4, 4, 8),
                PartPose.offsetAndRotation(4.2F, 10.0F, -3.5F, 0.0F, 1.0471975511965976F, 0.0F));
        PartDefinition lHindTentacle03b = lHindTentacle03a.addOrReplaceChild("lHindTentacle03b",
                CubeListBuilder.create().texOffs(92, 96).addBox(-1.5F, -1.5F, 0.0F, 3, 3, 9),
                PartPose.offsetAndRotation(0.0F, 0.4F, 7.2F, 0.0F, -0.5235987755982988F, 0.0F));
        PartDefinition lHindTentacle03c = lHindTentacle03b.addOrReplaceChild("lHindTentacle03c",
                CubeListBuilder.create().texOffs(0, 113).addBox(-1.0F, -1.0F, 0.0F, 2, 2, 10),
                PartPose.offsetAndRotation(0.0F, 0.4F, 8.6F, 0.0F, -0.2617993877991494F, 0.0F));
        PartDefinition lHindTentacle03 = lHindTentacle03a.addOrReplaceChild("lHindTentacle03",
                CubeListBuilder.create().texOffs(66, 99).addBox(-2.0F, -2.0F, -3.5F, 4, 4, 5),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7853981633974483F, 0.0F));
        PartDefinition lHindTentacle02a = bodyBase.addOrReplaceChild("lHindTentacle02a",
                CubeListBuilder.create().texOffs(0, 96).addBox(-3.0F, -3.0F, 0.0F, 6, 6, 10),
                PartPose.offsetAndRotation(4.2F, 9.5F, -1.9F, 0.0F, 0.5235987755982988F, 0.0F));
        PartDefinition lHindTentacle02b = lHindTentacle02a.addOrReplaceChild("lHindTentacle02b",
                CubeListBuilder.create().texOffs(34, 96).addBox(-2.5F, -2.5F, 0.0F, 5, 5, 8),
                PartPose.offsetAndRotation(0.0F, 0.4F, 9.5F, 0.0F, -0.10471975511965977F, 0.0F));
        PartDefinition eye23 = lHindTentacle02b.addOrReplaceChild("eye23",
                CubeListBuilder.create().texOffs(75, 30).addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2),
                PartPose.offset(2.1F, -2.5F, 3.5F));
        PartDefinition lHindTentacle02c = lHindTentacle02b.addOrReplaceChild("lHindTentacle02c",
                CubeListBuilder.create().texOffs(63, 96).addBox(-2.0F, -2.0F, 0.0F, 4, 4, 8),
                PartPose.offsetAndRotation(0.0F, 0.4F, 7.5F, 0.0F, -0.3490658503988659F, 0.0F));
        PartDefinition lHindTentacle02d = lHindTentacle02c.addOrReplaceChild("lHindTentacle02d",
                CubeListBuilder.create().texOffs(92, 96).addBox(-1.5F, -1.5F, 0.0F, 3, 3, 9),
                PartPose.offsetAndRotation(0.0F, 0.4F, 7.6F, 0.0F, -0.06981317007977318F, 0.0F));
        PartDefinition lHindTentacle02e = lHindTentacle02d.addOrReplaceChild("lHindTentacle02e",
                CubeListBuilder.create().texOffs(0, 113).addBox(-1.0F, -1.0F, 0.0F, 2, 2, 10),
                PartPose.offsetAndRotation(0.0F, 0.4F, 8.9F, 0.0F, 0.06981317007977318F, 0.0F));
        PartDefinition eye24 = lHindTentacle02d.addOrReplaceChild("eye24",
                CubeListBuilder.create().texOffs(75, 30).addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2),
                PartPose.offset(1.6F, -0.2F, 3.6F));
        PartDefinition rHindTentacle02a = bodyBase.addOrReplaceChild("rHindTentacle02a",
                CubeListBuilder.create().mirror().texOffs(0, 96).addBox(-3.0F, -3.0F, 0.0F, 6, 6, 10),
                PartPose.offsetAndRotation(-4.2F, 9.5F, -1.9F, 0.0F, -0.5235987755982988F, 0.0F));
        PartDefinition rHindTentacle02b = rHindTentacle02a.addOrReplaceChild("rHindTentacle02b",
                CubeListBuilder.create().mirror().texOffs(34, 96).addBox(-2.5F, -2.5F, 0.0F, 5, 5, 8),
                PartPose.offsetAndRotation(0.0F, 0.4F, 9.5F, 0.0F, 0.10471975511965977F, 0.0F));
        PartDefinition rHindTentacle02c = rHindTentacle02b.addOrReplaceChild("rHindTentacle02c",
                CubeListBuilder.create().mirror().texOffs(63, 96).addBox(-2.0F, -2.0F, 0.0F, 4, 4, 8),
                PartPose.offsetAndRotation(0.0F, 0.4F, 7.5F, 0.0F, 0.3490658503988659F, 0.0F));
        PartDefinition eye31 = rHindTentacle02c.addOrReplaceChild("eye31",
                CubeListBuilder.create().texOffs(75, 30).addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2),
                PartPose.offset(-1.5F, -1.8F, 3.8F));
        PartDefinition rHindTentacle02d = rHindTentacle02c.addOrReplaceChild("rHindTentacle02d",
                CubeListBuilder.create().mirror().texOffs(92, 96).addBox(-1.5F, -1.5F, 0.0F, 3, 3, 9),
                PartPose.offsetAndRotation(0.0F, 0.4F, 7.6F, 0.0F, 0.06981317007977318F, 0.0F));
        PartDefinition rHindTentacle02e = rHindTentacle02d.addOrReplaceChild("rHindTentacle02e",
                CubeListBuilder.create().mirror().texOffs(0, 113).addBox(-1.0F, -1.0F, 0.0F, 2, 2, 10),
                PartPose.offsetAndRotation(0.0F, 0.4F, 8.9F, 0.0F, -0.06981317007977318F, 0.0F));
        PartDefinition tail01a = bodyBase.addOrReplaceChild("tail01a",
                CubeListBuilder.create().texOffs(0, 96).addBox(-3.0F, -3.0F, 0.0F, 6, 6, 10),
                PartPose.offsetAndRotation(0.0F, 3.7F, 11.3F, -0.2792526803190927F, 0.0F, 0.0F));
        PartDefinition eye28 = tail01a.addOrReplaceChild("eye28",
                CubeListBuilder.create().texOffs(75, 30).addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2),
                PartPose.offset(-0.9F, -3.3F, 3.8F));
        PartDefinition tail01b = tail01a.addOrReplaceChild("tail01b",
                CubeListBuilder.create().texOffs(34, 96).addBox(-2.5F, -2.5F, 0.0F, 5, 5, 8),
                PartPose.offsetAndRotation(0.0F, 0.4F, 9.5F, -0.06981317007977318F, 0.0F, 0.0F));
        PartDefinition tail01c = tail01b.addOrReplaceChild("tail01c",
                CubeListBuilder.create().texOffs(63, 96).addBox(-2.0F, -2.0F, 0.0F, 4, 4, 8),
                PartPose.offsetAndRotation(0.0F, 0.4F, 7.5F, 0.2792526803190927F, 0.0F, 0.0F));
        PartDefinition tail01d = tail01c.addOrReplaceChild("tail01d",
                CubeListBuilder.create().texOffs(92, 96).addBox(-1.5F, -1.5F, 0.0F, 3, 3, 9),
                PartPose.offsetAndRotation(0.0F, 0.4F, 7.6F, 0.08726646259971647F, 0.0F, 0.0F));
        PartDefinition tail01e = tail01d.addOrReplaceChild("tail01e",
                CubeListBuilder.create().texOffs(0, 113).addBox(-1.0F, -1.0F, 0.0F, 2, 2, 10),
                PartPose.offset(0.0F, 0.4F, 8.9F));
        PartDefinition eye26 = tail01c.addOrReplaceChild("eye26",
                CubeListBuilder.create().texOffs(61, 30).addBox(-1.5F, -1.5F, -1.5F, 3, 3, 3),
                PartPose.offset(1.0F, -2.0F, 3.8F));
        PartDefinition eye27 = tail01b.addOrReplaceChild("eye27",
                CubeListBuilder.create().texOffs(75, 30).addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2),
                PartPose.offset(-2.0F, -2.5F, 3.8F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(AbstractShoggoth entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        float swingProgress = entity.getAttackAnim(ageInTicks - entity.tickCount);
		headJoint.yRot = netHeadYaw / (180F / (float)Math.PI);
		headJoint.xRot = headPitch / (180F / (float)Math.PI);

		mouth01Upper.xRot = -Mth.cos(ageInTicks * 0.25F) * 0.2F + 0.25F;
		mouth01Upper.yRot = 0.0F;
		mouth01Lower.xRot = 0.2617993877991494F + (Mth.cos(ageInTicks * 0.25F) * 0.2F + 0.25F) * 2;
		mouth01Lower.yRot = 0.0F;

		mouth02Upper.xRot = -Mth.cos(ageInTicks * 0.23F) * 0.2F + 0.25F;
		mouth02Upper.yRot = 0.0F;
		mouth02Lower.xRot = 0.2617993877991494F + (Mth.cos(ageInTicks * 0.23F) * 0.2F + 0.25F) * 2;
		mouth02Lower.yRot = 0.0F;

		mouth03Upper.xRot = -Mth.cos(ageInTicks * 0.27F) * 0.2F + 0.25F;
		mouth03Upper.yRot = 0.0F;
		mouth03Lower.xRot = 0.2617993877991494F + (Mth.cos(ageInTicks * 0.27F) * 0.2F + 0.25F) * 2;
		mouth03Lower.yRot = 0.0F;

		float animation = Mth.sin((limbSwing * 0.4F + 2) * 1.5F) * 0.3F * limbSwingAmount * 0.3F;
		float flap = Mth.sin(entity.tickCount * 0.2F) * 0.3F;
		float flap2 = Mth.cos(entity.tickCount * 0.2F) * 0.4F;

		lHindTentacle01a.yRot = flap *0.05F + 0.1f + animation * 0.4f;// + 0.20943951023931953F
		lHindTentacle01b.yRot = lHindTentacle01a.yRot * 1.5F - 0.1f;
		lHindTentacle01c.yRot = lHindTentacle01b.yRot * 1.75F - 0.1f;
		lHindTentacle01d.yRot = lHindTentacle01c.yRot * 2F;
		lHindTentacle01e.yRot = lHindTentacle01d.yRot * 2.25F;

		lHindTentacle02a.yRot = flap *0.05F + 0.3f + animation * 0.4f;// + 0.20943951023931953F
		lHindTentacle02b.yRot = lHindTentacle02a.yRot * 1.5F - 0.35f;
		lHindTentacle02c.yRot = lHindTentacle02b.yRot * 1.65F - 0.2f;
		lHindTentacle02d.yRot = lHindTentacle02c.yRot * 1.8F - 0.1f;
		lHindTentacle02e.yRot = lHindTentacle02d.yRot * 2.05F;

		lHindTentacle03a.yRot = flap *0.05F + 0.9f + animation * 0.4f;// + 0.20943951023931953F
		lHindTentacle03b.yRot = lHindTentacle03a.yRot * 1.5F - 1.7f;
		lHindTentacle03c.yRot = lHindTentacle03b.yRot * 1.75F + 0.3f;

		rHindTentacle01a.yRot = flap *0.05F - 0.1f + animation * 0.4f;// + 0.20943951023931953F
		rHindTentacle01b.yRot = rHindTentacle01a.yRot * 1.5F + 0.1f;
		rHindTentacle01c.yRot = rHindTentacle01b.yRot * 1.75F + 0.1f;
		rHindTentacle01d.yRot = rHindTentacle01c.yRot * 2F;
		rHindTentacle01e.yRot = rHindTentacle01d.yRot * 2.25F;

		rHindTentacle02a.yRot = flap *0.05F - 0.3f + animation * 0.4f;// + 0.20943951023931953F
		rHindTentacle02b.yRot = rHindTentacle02a.yRot * 1.5F + 0.35f;
		rHindTentacle02c.yRot = rHindTentacle02b.yRot * 1.65F + 0.2f;
		rHindTentacle02d.yRot = rHindTentacle02c.yRot * 1.8F + 0.1f;
		rHindTentacle02e.yRot = rHindTentacle02d.yRot * 2.05F;

		rHindTentacle03a.yRot = flap *0.05F - 0.9f + animation * 0.4f;// + 0.20943951023931953F
		rHindTentacle03b.yRot = rHindTentacle03a.yRot * 1.5F + 1.7f;
		rHindTentacle03c.yRot = rHindTentacle03b.yRot * 1.75F - 0.3f;

		tail01a.yRot = flap *0.1F + animation * 0.4f;// + 0.20943951023931953F
		tail01b.yRot = tail01a.yRot * 1.5F;
		tail01c.yRot = tail01b.yRot * 1.75F;
		tail01d.yRot = tail01c.yRot * 2F;
		tail01e.yRot = tail01d.yRot * 2.25F;

		lBackTentacle01a.yRot = flap *0.2F + animation * 0.5f;// + 0.5235987755982988F
		lBackTentacle01b.yRot = lBackTentacle01a.yRot * 1.5F;
		lBackTentacle01c.yRot = lBackTentacle01b.yRot * 1.75F;

		lBackTentacle01a.xRot = -0.13962634015954636F - flap2 * 0.5F;
		lBackTentacle01b.xRot = -0.41887902047863906F - flap2 * 0.75F;
		lBackTentacle01c.xRot = -0.13962634015954636F - flap2 * 1F;

		lBackTentacle02a.yRot = flap *0.2F + animation * 0.5f;// + 0.5235987755982988F
		lBackTentacle02b.yRot = lBackTentacle02a.yRot * 1.5F;
		lBackTentacle02c.yRot = lBackTentacle02b.yRot * 1.75F;

		lBackTentacle02a.xRot = -0.5235987755982988F - flap2 * 0.5F;
		lBackTentacle02b.xRot = -0.24434609527920614F - flap2 * 0.75F;
		lBackTentacle02c.xRot = 0.15707963267948966F - flap2 * 1F;

		lBackTentacle02a_1.yRot = flap *0.2F + animation * 0.5f;// + 0.5235987755982988F
		lBackTentacle02b_1.yRot = lBackTentacle02a_1.yRot * 1.5F;
		lBackTentacle02c_1.yRot = lBackTentacle02b_1.yRot * 1.75F;

		lBackTentacle02a_1.xRot = -0.7330382858376184F - flap2 * 0.5F;
		lBackTentacle02b_1.xRot = -0.41887902047863906F - flap2 * 0.75F;
		lBackTentacle02c_1.xRot = -0.13962634015954636F - flap2 * 1F;

		rBackTentacle01a.yRot = -flap *0.2F + animation * 0.5f;// + 0.5235987755982988F
		rBackTentacle01b.yRot = rBackTentacle01a.yRot * 1.5F;
		rBackTentacle01c.yRot = rBackTentacle01b.yRot * 1.75F;

		rBackTentacle01a.xRot = -0.13962634015954636F - flap2 * 0.5F;
		rBackTentacle01b.xRot = -0.41887902047863906F - flap2 * 0.75F;
		rBackTentacle01c.xRot = -0.13962634015954636F - flap2 * 1F;

		rBackTentacle02a.yRot = -flap *0.2F + animation * 0.5f;// + 0.5235987755982988F
		rBackTentacle02b.yRot = rBackTentacle02a.yRot * 1.5F;
		rBackTentacle02c.yRot = rBackTentacle02b.yRot * 1.75F;

		rBackTentacle02a.xRot = -0.7330382858376184F - flap2 * 0.5F;
		rBackTentacle02b.xRot = -0.41887902047863906F - flap2 * 0.75F;
		rBackTentacle02c.xRot = -0.13962634015954636F - flap2 * 1F;

		rBackTentacle02a_1.yRot = -flap *0.2F + animation * 0.5f;// + 0.5235987755982988F
		rBackTentacle02b_1.yRot = rBackTentacle02a_1.yRot * 1.5F;
		rBackTentacle02c_1.yRot = rBackTentacle02b_1.yRot * 1.75F;

		rBackTentacle02a_1.xRot = -0.5235987755982988F - flap2 * 0.5F;
		rBackTentacle02b_1.xRot = -0.24434609527920614F - flap2 * 0.75F;
		rBackTentacle02c_1.xRot = 0.15707963267948966F - flap2 * 1F;

		rArm01a.xRot = -0.24434609527920614F + Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 2.0F * limbSwingAmount * 0.5F;
		lArm01a.xRot = -0.24434609527920614F + Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;

		lArm01a.yRot = flap *0.1F - 0.3f;// + 0.20943951023931953F
		lArm01b.yRot = lArm01a.yRot * 1.5F + 0.36f;
		lArm01c.yRot = lArm01b.yRot * 1.75F + 0.2f;
		lArm01d.yRot = lArm01c.yRot * 2F + 0.1f;

		rArm01a.yRot = -flap *0.1F + 0.3f;// + 0.20943951023931953F
		rArm01b.yRot = rArm01a.yRot * 1.5F - 0.36f;
		rArm01c.yRot = rArm01b.yRot * 1.75F - 0.2f;
		rArm01d.yRot = rArm01c.yRot * 2F - 0.1f;

		rArm01a.zRot = 0.0F;
		lArm01a.zRot = 0.0F;

		rBodyTentacle01a.xRot = 0.3665191429188092F + Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 2.0F * limbSwingAmount * 0.5F;
		lBodyTentacle01a.xRot = 0.3665191429188092F + Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;

		rBodyTentacle01a.yRot = -flap *0.1F + 0.78f;// + 0.20943951023931953F
		rBodyTentacle01b.yRot = rBodyTentacle01a.yRot * 1.5F - 1.26f;
		rBodyTentacle01c.yRot = rBodyTentacle01b.yRot * 1.75F + 0.1f;

		lBodyTentacle01a.yRot = flap *0.1F - 0.78f;// + 0.20943951023931953F
		lBodyTentacle01b.yRot = lBodyTentacle01a.yRot * 1.5F + 1.26f;
		lBodyTentacle01c.yRot = lBodyTentacle01b.yRot * 1.75F - 0.1f;

		rBodyTentacle01a.zRot = 0.0F;
		lBodyTentacle01a.zRot = 0.0F;

		lBodyTentacle02a.yRot = flap *0.2F + animation;// + 0.20943951023931953F
		lBodyTentacle02b.yRot = lBodyTentacle02a.yRot * 1.5F;
		lBodyTentacle02c.yRot = lBodyTentacle02b.yRot * 1.75F;

		lBodyTentacle03a.yRot = -flap *0.3F - 0.4f + 0.05f + animation;// + 0.20943951023931953F
		lBodyTentacle03b.yRot = lBodyTentacle03a.yRot * 1.5F + 0.8f;

		lBodyTentacle04a.yRot = -flap *0.4F - 0.08f + animation;// + 0.20943951023931953F
		lBodyTentacle04b.yRot = lBodyTentacle04a.yRot * 1.5F;

		rBodyTentacle02a.yRot = -flap *0.2F + animation;// + 0.20943951023931953F
		rBodyTentacle02b.yRot = rBodyTentacle02a.yRot * 1.5F;
		rBodyTentacle02c.yRot = rBodyTentacle02b.yRot * 1.75F;

		rBodyTentacle03a.yRot = flap *0.3F + 0.4f - 0.07f + animation;// + 0.20943951023931953F
		rBodyTentacle03b.yRot = rBodyTentacle03a.yRot * 1.5F - 0.8f;

		rBodyTentacle04a.yRot = flap *0.4F + 0.03f + animation;// + 0.20943951023931953F
		rBodyTentacle04b.yRot = rBodyTentacle04a.yRot * 1.5F;

		float f6;
		float f7;

		if (swingProgress > -9990.0F)
		{
			f6 = swingProgress;
			f6 = 1.0F - swingProgress;
			f6 *= f6;
			f6 *= f6;
			f6 = 1.0F - f6;
			f7 = Mth.sin(f6 * (float)Math.PI);
			float f8 = Mth.sin(swingProgress * (float)Math.PI) * -(headJoint.xRot - 0.7F) * 0.75F;
			rArm01a.xRot = (float)(rArm01a.xRot + (f7 * 1.2D +  f8));
			rBodyTentacle01a.xRot = (float)(rBodyTentacle01a.xRot + (f7 * 1.2D + f8));
			rArm01a.zRot = Mth.sin(swingProgress * (float)Math.PI) * -0.4F;
			rBodyTentacle01a.zRot = Mth.sin(swingProgress * (float)Math.PI) * -0.4F;
			lArm01a.xRot = (float)(lArm01a.xRot + (f7 * 1.2D + f8));
			lBodyTentacle01a.xRot = (float)(lBodyTentacle01a.xRot + (f7 * 1.2D + f8));
			lArm01a.zRot = Mth.sin(swingProgress * (float)Math.PI) * 0.4F;
			lBodyTentacle01a.zRot = Mth.sin(swingProgress * (float)Math.PI) * 0.4F;
		}
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
