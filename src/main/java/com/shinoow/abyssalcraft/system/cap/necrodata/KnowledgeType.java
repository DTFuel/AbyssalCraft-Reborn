package com.shinoow.abyssalcraft.system.cap.necrodata;

/**
 * The five knowledge branches tracked by the necrodata capability (owned by PS-2), faithful to the
 * 1.12.2 {@code KnowledgeType}. Knowledge points accumulate per branch; the knowledge/research logic
 * that consumes them lands with the knowledge subsystem (PS-8).
 */
public enum KnowledgeType {
    BASE,
    ABYSSAL,
    DREAD,
    OMOTHOL,
    SHADOW
}
