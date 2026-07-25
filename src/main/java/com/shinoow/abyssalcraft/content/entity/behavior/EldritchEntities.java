package com.shinoow.abyssalcraft.content.entity.behavior;

import com.shinoow.abyssalcraft.content.entity.boss.BossKind;
import com.shinoow.abyssalcraft.content.entity.boss.BossMob;
import com.shinoow.abyssalcraft.content.entity.boss.EliteKind;
import com.shinoow.abyssalcraft.content.entity.boss.EliteMob;
import com.shinoow.abyssalcraft.content.entity.ghoul.GhoulEntities;
import com.shinoow.abyssalcraft.content.entity.legacy.LegacyEntities;
import com.shinoow.abyssalcraft.content.entity.shoggoth.AbstractShoggoth;

import net.minecraft.world.entity.LivingEntity;

/** Modern equivalent of the legacy IOmotholEntity || IShoggothEntity check. */
public final class EldritchEntities {

    private EldritchEntities() {}

    public static boolean isEldritch(LivingEntity entity) {
        if (entity instanceof AbstractShoggoth) return true;
        if (entity.getType() == GhoulEntities.OMOTHOL_GHOUL.get()
                || entity.getType() == GhoulEntities.SHADOW_GHOUL.get()
                || entity.getType() == LegacyEntities.SHADOW_CREATURE.get()
                || entity.getType() == LegacyEntities.SHADOW_MONSTER.get()
                || entity.getType() == LegacyEntities.SHADOW_BEAST.get()) return true;
        if (entity instanceof BossMob boss) {
            return boss.kind() == BossKind.JZAHAR || boss.kind() == BossKind.SACTHOTH;
        }
        return entity instanceof EliteMob elite
            && (elite.kind() == EliteKind.REMNANT || elite.kind() == EliteKind.GATEKEEPER_MINION);
    }
}