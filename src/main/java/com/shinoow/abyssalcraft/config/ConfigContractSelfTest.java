package com.shinoow.abyssalcraft.config;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.shinoow.abyssalcraft.platform.ConfigCompat;

/** Headless T8.2c contract: mapping, parsing, validation, save and reload. */
public final class ConfigContractSelfTest {

    private static final Set<String> FORBIDDEN_CONSUMER_SYMBOLS = Set.of(
        "Audit", "SelfTest", "ConfigScreen", "ConfigEditorModel");

    private ConfigContractSelfTest() {}

    public static void run() {
        Set<String> fields = new HashSet<>();
        Map<Supplier<?>, String> suppliers = new IdentityHashMap<>();
        for (Field field : ACConfig.class.getDeclaredFields()) {
            if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers())
                    && Supplier.class.isAssignableFrom(field.getType())) {
                fields.add(field.getName());
                try {
                    Supplier<?> supplier = (Supplier<?>) field.get(null);
                    require(supplier != null, "config field is not initialized: " + field.getName());
                    require(suppliers.put(supplier, field.getName()) == null,
                        "config fields share an Entry: " + field.getName());
                } catch (IllegalAccessException ex) {
                    throw new IllegalStateException("Cannot inspect config field " + field.getName(), ex);
                }
            }
        }
        Set<String> paths = new HashSet<>();
        Map<Supplier<?>, ConfigCompat.Entry<?>> entriesByIdentity = new IdentityHashMap<>();
        for (ConfigCompat.Entry<?> entry : ConfigCompat.entries()) {
            require(paths.add(entry.path()), "duplicate config path " + entry.path());
            require(entriesByIdentity.put(entry, entry) == null, "duplicate config Entry identity " + entry.path());
        }
        require(fields.size() == ConfigClosureAudit.DEFINED, "config field count changed: " + fields.size());
        require(fields.size() == 145, "config contract must contain exactly 145 unique keys");
        require(paths.size() == fields.size(), "config field-to-entry mapping is incomplete");
        require(entriesByIdentity.keySet().equals(suppliers.keySet()),
            "ACConfig reflection fields and registered Entry identities differ");

        Map<String, ConfigClosureAudit.Consumer> consumers = ConfigClosureAudit.consumersByKey();
        Set<String> accountedKeys = new HashSet<>(consumers.keySet());
        require(accountedKeys.addAll(ConfigClosureAudit.blockedKeys()),
            "a config key is both consumed and blocked");
        require(accountedKeys.equals(fields), "consumer and blocked maps do not cover ACConfig reflection fields");
        consumers.forEach((key, consumer) -> {
            require(consumer != null && !consumer.owner().isBlank() && !consumer.symbol().isBlank(),
                "empty production consumer for " + key);
            require(FORBIDDEN_CONSUMER_SYMBOLS.stream().noneMatch(consumer.symbol()::contains),
                "audit-only consumer is forbidden for " + key + ": " + consumer.symbol());
        });
        Map<String, ConfigClosureAudit.Consumer> flattenedOwners = new LinkedHashMap<>();
        ConfigClosureAudit.consumersByOwner().forEach((owner, owned) -> owned.forEach((key, symbol) ->
            require(flattenedOwners.put(key, new ConfigClosureAudit.Consumer(owner, symbol)) == null,
                "owner view duplicates config key " + key)));
        require(flattenedOwners.equals(consumers), "owner and key consumer views differ");
        require(ConfigClosureAudit.blockedByKey().keySet().equals(ConfigClosureAudit.blockedKeys()),
            "blocked owner map differs from blocked keys");

        require(ConfigClosureAudit.blockedKeys().isEmpty(),
            "config closure BLOCKED by prohibited owner: " + ConfigClosureAudit.blockedByKey());
        require(consumers.size() == 145, "config contract must contain exactly 145 production consumers");

        require(ComplexConfig.parseDimensionMappings(java.util.List.of(
            "abyssalcraft:abyssal_wasteland;1;Wasteland")).get(
                com.shinoow.abyssalcraft.platform.ACRef.id("abyssal_wasteland")).bookType() == 1,
            "dimension book mapping parse failed");
        require(ComplexConfig.parseDimensionMappings(java.util.List.of(
            "abyssalcraft:abyssal_wasteland;1", "abyssalcraft:abyssal_wasteland;2")).isEmpty(),
            "duplicate dimension book mapping did not fail closed");
        require(ComplexConfig.parseDimensionMappings(java.util.List.of(
            "abyssalcraft:abyssal_wasteland;5")).isEmpty(),
            "invalid dimension book mapping did not fail closed");

        ConfigEditorModel model = new ConfigEditorModel();
        require(model.validate("general.hardcore_mode").isEmpty(), "boolean parse failed");
        model.setValue("general.knowledge_sync_delay", "19");
        require(!model.validate("general.knowledge_sync_delay").isEmpty(), "range validation failed");
        model.reload();
        String original = model.value("client.particle_block");
        model.setValue("client.particle_block", Boolean.toString(!Boolean.parseBoolean(original)));
        require(model.save().isEmpty(), "save failed");
        require(model.value("client.particle_block").equals(Boolean.toString(!Boolean.parseBoolean(original))),
            "save/reload did not expose the new value");
        model.setValue("client.particle_block", original);
        require(model.save().isEmpty(), "restore save failed");

        System.out.printf("RR_CONFIG_SELF_TEST_OK defined=%d consumed=%d blocked=%d owners=%d screen=modlist save=ok reload=ok%n",
            fields.size(), consumers.size(), ConfigClosureAudit.blockedCount(),
            ConfigClosureAudit.consumersByOwner().size());
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}