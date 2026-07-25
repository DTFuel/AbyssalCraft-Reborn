package com.shinoow.abyssalcraft.content.entity.legacy;

enum LegacyMobKind {
    ABYSSAL_ZOMBIE("abyssalzombie", 0.6F, 1.8F, 25.0D, 6.0D, 0.23D, 42.0D, 0.0D, 2.0D, true, false, true),
    DREADLING("dreadling", 0.8F, 1.5F, 30.0D, 6.0D, 0.70D, 16.0D, 0.0D, 0.0D, false, true, true),
    DREAD_SPAWN("dreadspawn", 0.6F, 0.6F, 20.0D, 6.0D, 0.30D, 16.0D, 0.0D, 0.0D, true, true, false),
    GREATER_DREAD_SPAWN("greaterdreadspawn", 1.2F, 1.2F, 50.0D, 12.0D, 0.40D, 42.0D, 0.2D, 0.0D, true, true, false),
    LESSER_DREADBEAST("lesserdreadbeast", 1.8F, 1.8F, 100.0D, 18.0D, 0.35D, 42.0D, 0.4D, 0.0D, true, true, false),
    SHADOW_CREATURE("shadowcreature", 0.5F, 1.0F, 20.0D, 6.0D, 0.70D, 16.0D, 0.2D, 0.0D, true, true, false),
    SHADOW_MONSTER("shadowmonster", 0.6F, 1.8F, 40.0D, 8.0D, 0.70D, 42.0D, 0.2D, 0.0D, true, true, false),
    SHADOW_BEAST("shadowbeast", 1.0F, 2.8F, 60.0D, 10.0D, 0.70D, 42.0D, 0.3D, 0.0D, true, true, false);

    final String id;
    final float width;
    final float height;
    final double health;
    final double damage;
    final double speed;
    final double followRange;
    final double knockbackResistance;
    final double armor;
    final boolean breathesUnderwater;
    final boolean fireImmune;
    final boolean undead;

    LegacyMobKind(String id, float width, float height, double health, double damage, double speed,
                  double followRange, double knockbackResistance, double armor,
                  boolean breathesUnderwater, boolean fireImmune, boolean undead) {
        this.id = id;
        this.width = width;
        this.height = height;
        this.health = health;
        this.damage = damage;
        this.speed = speed;
        this.followRange = followRange;
        this.knockbackResistance = knockbackResistance;
        this.armor = armor;
        this.breathesUnderwater = breathesUnderwater;
        this.fireImmune = fireImmune;
        this.undead = undead;
    }
}