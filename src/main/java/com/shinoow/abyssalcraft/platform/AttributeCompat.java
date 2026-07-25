package com.shinoow.abyssalcraft.platform;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * Compat: AttributeModifier construction (vanilla axis).
 *
 * <p>1.21 replaced the {@code UUID} identity with a {@code ResourceLocation}, dropped the name
 * string, and renamed the {@code Operation} constants. Callers always pass a ResourceLocation id
 * and a loader-neutral {@link Op}; on 1.20.1 a stable UUID is derived from the id.
 */
public final class AttributeCompat {

    private AttributeCompat() {}

    /** Loader-neutral attribute operation (maps to the version-correct vanilla constant). */
    public enum Op { ADD, MULTIPLY_BASE, MULTIPLY_TOTAL }

    public static AttributeModifier modifier(ResourceLocation id, String legacyName, double amount, Op op) {
        //? if >=1.21 {
        /*return new AttributeModifier(id, amount, operation(op));
        *///?} else {
        UUID uuid = UUID.nameUUIDFromBytes(id.toString().getBytes(StandardCharsets.UTF_8));
        return new AttributeModifier(uuid, legacyName, amount, operation(op));
        //?}
    }

    private static AttributeModifier.Operation operation(Op op) {
        //? if >=1.21 {
        /*switch (op) {
            case ADD: return AttributeModifier.Operation.ADD_VALUE;
            case MULTIPLY_BASE: return AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
            case MULTIPLY_TOTAL: return AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
            default: throw new IllegalArgumentException("op");
        }
        *///?} else {
        switch (op) {
            case ADD: return AttributeModifier.Operation.ADDITION;
            case MULTIPLY_BASE: return AttributeModifier.Operation.MULTIPLY_BASE;
            case MULTIPLY_TOTAL: return AttributeModifier.Operation.MULTIPLY_TOTAL;
            default: throw new IllegalArgumentException("op");
        }
        //?}
    }
}
