package com.shinoow.abyssalcraft.client.model.entity;

import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Mob;

public final class DemonSheepModel<T extends Mob> extends QuadrupedModel<T> {

    public DemonSheepModel(ModelPart root) {
        super(root, false, 8.0F, 4.0F, 2.0F, 2.0F, 24);
    }
}