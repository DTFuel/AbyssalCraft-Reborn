package com.shinoow.abyssalcraft.content.entity.boss;

import net.minecraft.world.BossEvent;

/**
 * The four AbyssalCraft bosses that carry a health bar ({@link ACBossMob}), collapsed to data (owned by
 * PD-7, Stage D2b). Faithful 1.12.2 registry ids + attribute values (non-hardcore) + spawn-egg colours.
 *
 * <p>Sacthoth ({@code shadowboss}) and the Dragon Boss are simplified here: Sacthoth's wall-climbing and
 * the dragon's flight + {@code MultiPartEntityPart} hitboxes/size are deferred, so their movement speed
 * and bounds are approximated (dragon shrunk from the 1.12.2 24x12). Every boss's signature attacks
 * (Chagaroth's barf/fist+spawn summons, Jzahar's earthquake/black-hole/implosion/shout, Sacthoth's
 * shadow-flame, the dragon's healing circle) are deferred until their subsystems land.
 */
public enum BossKind {

    CHAGAROTH("chagaroth", 1000.0D, 15.0D, 0.0D, 16.0D, 0.0D, 1.0D, 2.0F, 4.8F, BossEvent.BossBarColor.BLUE, true, 0xE60000, 0xCC0000),
    JZAHAR("jzahar", 500.0D, 30.0D, 0.25D, 80.0D, 10.0D, 0.0D, 1.5F, 5.7F, BossEvent.BossBarColor.BLUE, true, 0x133133, 0x342122),
    SACTHOTH("shadowboss", 300.0D, 15.0D, 0.799D, 160.0D, 0.0D, 0.4D, 1.2F, 3.8F, BossEvent.BossBarColor.BLUE, true, 0x000000, 0xFFFFFF),
    DRAGON_BOSS("dragonboss", 400.0D, 10.0D, 0.3D, 32.0D, 0.0D, 1.0D, 24.0F, 12.0F, BossEvent.BossBarColor.BLUE, true, 0x476767, 0x768833);

    private final String id;
    private final double health;
    private final double attack;
    private final double speed;
    private final double followRange;
    private final double armor;
    private final double knockbackResistance;
    private final float width;
    private final float height;
    private final BossEvent.BossBarColor color;
    private final boolean fireImmune;
    private final int eggPrimary;
    private final int eggSecondary;

    BossKind(String id, double health, double attack, double speed, double followRange, double armor,
             double knockbackResistance, float width, float height, BossEvent.BossBarColor color,
             boolean fireImmune, int eggPrimary, int eggSecondary) {
        this.id = id;
        this.health = health;
        this.attack = attack;
        this.speed = speed;
        this.followRange = followRange;
        this.armor = armor;
        this.knockbackResistance = knockbackResistance;
        this.width = width;
        this.height = height;
        this.color = color;
        this.fireImmune = fireImmune;
        this.eggPrimary = eggPrimary;
        this.eggSecondary = eggSecondary;
    }

    public String id() { return id; }
    public double health() { return health; }
    public double attack() { return attack; }
    public double speed() { return speed; }
    public double followRange() { return followRange; }
    public double armor() { return armor; }
    public double knockbackResistance() { return knockbackResistance; }
    public float width() { return width; }
    public float height() { return height; }
    public BossEvent.BossBarColor color() { return color; }
    public boolean fireImmune() { return fireImmune; }
    public int eggPrimary() { return eggPrimary; }
    public int eggSecondary() { return eggSecondary; }
}
