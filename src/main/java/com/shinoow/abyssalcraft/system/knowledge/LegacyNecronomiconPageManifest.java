package com.shinoow.abyssalcraft.system.knowledge;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;

/** Loads the source-derived 1.12.2 page declarations packaged with the modern client. */
public final class LegacyNecronomiconPageManifest {

    private static final String RESOURCE = "assets/abyssalcraft/necronomicon/legacy-pages.json";
    private static final List<LegacyPage> PAGES = load();

    private LegacyNecronomiconPageManifest() {}

    public static List<LegacyPage> pages() {
        return PAGES;
    }

    private static List<LegacyPage> load() {
        try (InputStream stream = LegacyNecronomiconPageManifest.class.getClassLoader()
                .getResourceAsStream(RESOURCE)) {
            if (stream == null) throw new IllegalStateException("Missing resource " + RESOURCE);
            LegacyPage[] pages = new Gson().fromJson(
                new InputStreamReader(stream, StandardCharsets.UTF_8), LegacyPage[].class);
            if (pages == null) throw new IllegalStateException("Empty resource " + RESOURCE);
            return List.copyOf(Arrays.asList(pages));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read " + RESOURCE, exception);
        }
    }

    public record LegacyPage(
        String legacyId,
        int sourceOrder,
        int pageNumber,
        int bookType,
        String titleReference,
        String titleKey,
        String textReference,
        String textKey,
        String visualKind,
        String visualReference,
        String researchReference,
        String constructor,
        NecronomiconPageManifest.OwnerStatus status,
        String owner,
        String reason
    ) {}
}