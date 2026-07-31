package com.shinoow.abyssalcraft.content.item.tool;

import com.shinoow.abyssalcraft.content.entity.projectile.CoraliumArrow;
import com.shinoow.abyssalcraft.content.entity.projectile.ProjectileEntities;

//? if >=1.21 {
/*import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
*///?} else {
import net.minecraft.world.entity.projectile.AbstractArrow;
//?}
import net.minecraft.world.item.BowItem;

/** Coralium longbow retaining vanilla ammunition semantics while firing the legacy projectile. */
public final class CoraliumLongbowItem extends BowItem {

    public CoraliumLongbowItem(Properties properties) {
        super(properties);
    }

    //? if >=1.21 {
    /*@Override
    protected Projectile createProjectile(Level level, LivingEntity shooter, ItemStack weapon,
            ItemStack ammunition, boolean isCrit) {
        CoraliumArrow coraliumArrow = new CoraliumArrow(ProjectileEntities.CORALIUM_ARROW.get(), level);
        coraliumArrow.setOwner(shooter);
        coraliumArrow.setCritArrow(isCrit);
        return coraliumArrow;
    }
    *///?} else {
    @Override
    public AbstractArrow customArrow(AbstractArrow arrow) {
        CoraliumArrow coraliumArrow = new CoraliumArrow(ProjectileEntities.CORALIUM_ARROW.get(), arrow.level());
        coraliumArrow.setOwner(arrow.getOwner());
        coraliumArrow.moveTo(arrow.getX(), arrow.getY(), arrow.getZ(), arrow.getYRot(), arrow.getXRot());
        return coraliumArrow;
    }
    //?}
}