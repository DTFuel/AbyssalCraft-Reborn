package com.shinoow.abyssalcraft.content.entity.boss;

/**
 * The eight AbyssalCraft "elite" bosses / boss minions that do NOT carry a health bar (owned by PD-7,
 * Stage D2b): collapsed to data, faithful 1.12.2 registry ids + attribute values (non-hardcore) +
 * spawn-egg colours. These extend {@link com.shinoow.abyssalcraft.content.entity.base.ACMob} directly.
 *
 * <p>Simplifications: the Chagaroth spawn's wall-climbing and the Dragon Minion's flight +
 * {@code MultiPartEntityPart} hitboxes are deferred (dragon minion shrunk from 1.12.2 8x3); the
 * Remnant's villager-style trading ({@code IMerchant}) + shearing ({@code IShearable}), and each
 * minion's summon-by-boss behaviour, are deferred until their subsystems land. Armor toughness (only
 * the Dreadguard had 4) is omitted as a trivial stat.
 */
public enum EliteKind {

    DREADGUARD("dreadguard", 60.0D, 10.0D, 0.23D, 42.0D, 20.0D, 0.0D, 1.0F, 3.0F, true, 0xE60000, 0xCC0000),
    SKELETON_GOLIATH("gskeleton", 60.0D, 10.0D, 0.23D, 42.0D, 0.0D, 0.3D, 1.0F, 4.5F, false, 0xD6D6C9, 0xC6C7AD),
    REMNANT("remnant", 50.0D, 10.0D, 0.3D, 64.0D, 0.0D, 0.2D, 0.6F, 1.95F, false, 0x133133, 0x342122),
    SHUB_OFFSPRING("shuboffspring", 40.0D, 4.0D, 0.25D, 16.0D, 0.0D, 0.0D, 1.0F, 2.9F, false, 0x2B2929, 0x211F1D),
    GATEKEEPER_MINION("jzaharminion", 100.0D, 18.0D, 0.28D, 64.0D, 0.0D, 0.2D, 0.8F, 2.7F, false, 0x133133, 0x342122),
    CHAGAROTH_FIST("chagarothfist", 40.0D, 7.5D, 0.23D, 16.0D, 0.0D, 0.3D, 1.0F, 2.0F, true, 0xE60000, 0xCC0000),
    CHAGAROTH_SPAWN("chagarothspawn", 30.0D, 8.0D, 0.45D, 16.0D, 0.0D, 0.1D, 0.6F, 0.6F, true, 0xE60000, 0xCC0000),
    DRAGON_MINION("dragonminion", 30.0D, 8.0D, 0.3D, 16.0D, 0.0D, 0.0D, 8.0F, 3.0F, true, 0x433434, 0x344344);

    private final String id;
    private final double health;
    private final double attack;
    private final double speed;
    private final double followRange;
    private final double armor;
    private final double knockbackResistance;
    private final float width;
    private final float height;
    private final boolean fireImmune;
    private final int eggPrimary;
    private final int eggSecondary;

    EliteKind(String id, double health, double attack, double speed, double followRange, double armor,
              double knockbackResistance, float width, float height, boolean fireImmune,
              int eggPrimary, int eggSecondary) {
        this.id = id;
        this.health = health;
        this.attack = attack;
        this.speed = speed;
        this.followRange = followRange;
        this.armor = armor;
        this.knockbackResistance = knockbackResistance;
        this.width = width;
        this.height = height;
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
    public boolean fireImmune() { return fireImmune; }
    public int eggPrimary() { return eggPrimary; }
    public int eggSecondary() { return eggSecondary; }
}
