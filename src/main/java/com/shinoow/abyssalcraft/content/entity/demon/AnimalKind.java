package com.shinoow.abyssalcraft.content.entity.demon;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

/**
 * Per-species data for the demon/evil animal family (owned by PD-4, Stage D2a).
 *
 * <p>The 1.12.2 mod shipped one class per species ({@code EntityDemonCow}, {@code EntityEvilSheep}, ...);
 * they differ only in size, health, and the vanilla-animal ambient/death sounds. This enum carries that
 * data so a single {@link DemonAnimal} / {@link EvilAnimal} class covers all four species -- the kind is
 * baked into each {@code EntityType} factory lambda in {@link DemonEntities}. Values are faithful to the
 * 1.12.2 non-hardcore stats.
 */
public enum AnimalKind {

    CHICKEN("chicken", SoundEvents.CHICKEN_AMBIENT, SoundEvents.CHICKEN_DEATH, 0.3F, 0.7F, 10.0D),
    COW("cow", SoundEvents.COW_AMBIENT, SoundEvents.COW_DEATH, 0.9F, 1.3F, 15.0D),
    PIG("pig", SoundEvents.PIG_AMBIENT, SoundEvents.PIG_DEATH, 0.9F, 0.9F, 15.0D),
    SHEEP("sheep", SoundEvents.SHEEP_AMBIENT, SoundEvents.SHEEP_DEATH, 0.9F, 1.3F, 12.0D);

    private final String id;
    private final SoundEvent ambient;
    private final SoundEvent death;
    private final float width;
    private final float height;
    private final double health;

    AnimalKind(String id, SoundEvent ambient, SoundEvent death, float width, float height, double health) {
        this.id = id;
        this.ambient = ambient;
        this.death = death;
        this.width = width;
        this.height = height;
        this.health = health;
    }

    public String id() { return id; }
    public SoundEvent ambient() { return ambient; }
    public SoundEvent death() { return death; }
    public float width() { return width; }
    public float height() { return height; }
    public double health() { return health; }
}
