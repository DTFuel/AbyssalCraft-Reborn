package com.shinoow.abyssalcraft.system.knowledge;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

import javax.imageio.ImageIO;

import com.shinoow.abyssalcraft.system.energy.structure.EnergyStructures;
import com.shinoow.abyssalcraft.system.ritual.RitualManifestCatalog;
import com.shinoow.abyssalcraft.system.spell.SpellManifestCatalog;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Auditable Necronomicon page manifest: every page assigned by the 1.12.2 page handler plus every entry
 * in the modern ritual, spell and Place of Power catalogs, including explicit non-active owner states.
 *
 * <p>This replaces the manual 1.12.2 {@code Chapters}/{@code Pages} registration with a data-driven
 * approach: page manifests → automatic {@link com.shinoow.abyssalcraft.client.necronomicon.NecronomiconEntry}
 * tree → GUI display with research gates.
 */
public final class NecronomiconPageManifest {

    /** Page type categories (matching 1.12.2 chapter structure). */
    public enum PageType {
        INFORMATION,     // Lore/dimension/entity info
        RECIPE,          // Crafting recipes
        RITUAL,          // Ritual instructions
        SPELL,           // Spell descriptions
        PLACE_OF_POWER,  // PoP mechanics
        PROGRESSION,     // Progression guides
        MATERIAL,        // Material descriptions
        STRUCTURE        // Structure info
    }

    public enum OwnerStatus {
        ACTIVE,
        MISSING,
        BLOCKED
    }

    public record ContentRef(
        String kind,
        String owner,
        String reference,
        OwnerStatus status,
        String reason
    ) {
        public ContentRef {
            if (kind == null || kind.isBlank() || owner == null || owner.isBlank()
                || reference == null || reference.isBlank() || status == null) {
                throw new IllegalArgumentException("incomplete Necronomicon content reference");
            }
            reason = reason == null ? "" : reason;
            if (status != OwnerStatus.ACTIVE && reason.isBlank()) {
                throw new IllegalArgumentException("non-active Necronomicon content requires a reason");
            }
        }
    }

    public record LegacyFields(
        String legacyId,
        int sourceOrder,
        int pageNumber,
        int bookType,
        String titleReference,
        String textReference,
        String visualKind,
        String visualReference,
        String researchReference,
        String constructor
    ) {}

    public record ImageContent(
        ResourceLocation texture,
        int textureWidth,
        int textureHeight,
        int u,
        int v,
        int width,
        int height
    ) {
        public ImageContent {
            if (texture == null || textureWidth <= 0 || textureHeight <= 0 || width <= 0 || height <= 0
                || u < 0 || v < 0 || u + width > textureWidth || v + height > textureHeight) {
                throw new IllegalArgumentException("invalid Necronomicon image content");
            }
        }
    }

    /** A single page manifest entry. */
    public record PageEntry(
        ResourceLocation id,           // Page ID (e.g., abyssalcraft:azathoth_1)
        String titleKey,               // Lang key for title
        PageType type,                 // Page category
        ResourceLocation researchId,   // Required research (null = always visible)
        ItemStack icon,                // Display icon
        List<ResourceLocation> relatedPages, // Cross-references
        ContentRef content,
        String textKey,
        LegacyFields legacyFields,
        ImageContent image,
        int requiredBookType
    ) {
        public PageEntry {
            if (requiredBookType < 0 || requiredBookType > 4) {
                throw new IllegalArgumentException("invalid required Necronomicon book type for " + id);
            }
        }

        public PageEntry(ResourceLocation id, String titleKey, PageType type,
                         ResourceLocation researchId, ItemStack icon, ContentRef content,
                         int requiredBookType) {
            this(id, titleKey, type, researchId, icon, List.of(), content, null, null, null,
                requiredBookType);
        }
    }

    private static final List<PageEntry> PAGES = new ArrayList<>();
    private static boolean bootstrapped = false;

    private NecronomiconPageManifest() {}

    /** Bootstrap the 1.12.2 pages and every entry in the modern content catalogs. */
    public static synchronized void bootstrap(net.minecraft.core.HolderLookup.Provider registries) {
        if (bootstrapped) {
            return;
        }

        for (LegacyNecronomiconPageManifest.LegacyPage legacy : LegacyNecronomiconPageManifest.pages()) {
            String id = legacy.legacyId().toLowerCase(java.util.Locale.ROOT);
            NecronomiconItemVisuals.Resolution itemVisual = NecronomiconItemVisuals.resolve(registries, legacy);
            boolean resolvedItem = "ITEM".equals(legacy.visualKind());
            boolean resolvedImage = "IMAGE".equals(legacy.visualKind());
            boolean resolvedRecipe = "RECIPE".equals(legacy.visualKind());
            ItemStack icon = resolvedItem ? itemVisual.stack().orElse(ItemStack.EMPTY) : ItemStack.EMPTY;
            ContentRef content = resolvedImage
                ? active("image", "necronomicon-image-renderer", legacy.visualReference())
                : resolvedItem
                ? new ContentRef("item", itemVisual.owner(), itemVisual.reference(), itemVisual.status(),
                    itemVisual.reason())
                : resolvedRecipe
                ? recipeContent(legacy)
                : new ContentRef(legacy.visualKind().toLowerCase(java.util.Locale.ROOT), legacy.owner(),
                    legacy.visualReference(), legacy.status(), legacy.reason());
            ImageContent image = resolvedImage ? loadImage(legacy.visualReference()) : null;
            PAGES.add(new PageEntry(resId("legacy/" + id), legacy.titleKey(), legacyType(legacy.legacyId()),
                null, icon, List.of(), content,
                legacy.textKey(), new LegacyFields(legacy.legacyId(), legacy.sourceOrder(), legacy.pageNumber(),
                    legacy.bookType(), legacy.titleReference(), legacy.textReference(), legacy.visualKind(),
                    legacy.visualReference(), legacy.researchReference(), legacy.constructor()), image,
                legacy.bookType()));
        }

        RitualManifestCatalog.entries().forEach(ritual ->
            registerPage(catalogId("ritual", ritual.id()), "ac.ritual." + ritual.id(), PageType.RITUAL,
                ritual.research(), active("ritual", "RitualManifestCatalog",
                    "id=" + ritual.id() + "; legacyId=" + ritual.legacyId() + "; order=" + ritual.order()
                        + "; kind=" + ritual.kind() + "; bookType=" + ritual.bookType()
                        + "; dimension=" + ritual.dimension().location() + "; pe=" + ritual.requiredEnergy()
                        + "; sacrifice=" + ritual.requiresSacrifice() + "; center=" + ritual.center()
                        + "; offerings=" + ritual.offeringLayout() + "; result=" + ritual.result()),
                ritual.bookType()));
        SpellManifestCatalog.entries().forEach(spell ->
            registerPage(catalogId("spell", spell.id()), "ac.spell." + spell.id(), PageType.SPELL,
                spell.research(), active("spell", "SpellManifestCatalog",
                    "id=" + spell.id() + "; aliases=" + spell.aliases() + "; order=" + spell.order()
                        + "; bookType=" + spell.bookType() + "; pe=" + spell.requiredEnergy()
                        + "; scroll=" + spell.scrollType() + "; target=" + spell.targetType()
                        + "; charging=" + spell.requiresCharging() + "; reagents=" + spell.reagentLayout()
                        + "; parent=" + spell.parentId() + "; glyph=" + spell.glyph()), spell.bookType()));
        EnergyStructures.ALL.forEach(structure ->
            registerPage(catalogId("place_of_power", structure.getIdentifier()), structure.getDescriptionKey(),
                PageType.PLACE_OF_POWER, structure.getResearchId(),
                active("place_of_power", "EnergyStructures",
                    "id=" + structure.getIdentifier() + "; bookType=" + structure.getBookType()
                        + "; blocksKey=" + structure.getRequiredBlockNamesKey()
                        + "; activation=" + structure.getActivationPointForRender()), structure.getBookType()));

        bootstrapped = true;
    }

    private static ContentRef recipeContent(LegacyNecronomiconPageManifest.LegacyPage legacy) {
        NecronomiconRecipePages.Definition definition = NecronomiconRecipePages.definition(legacy.legacyId());
        if (definition == null) {
            return new ContentRef("recipe", legacy.owner(), legacy.legacyId(), OwnerStatus.BLOCKED,
                "legacy recipe page has no audited modern mapping");
        }
        String reference = definition.active()
            ? definition.recipeIds().stream().map(ResourceLocation::toString)
                .collect(java.util.stream.Collectors.joining(","))
            : legacy.legacyId();
        return new ContentRef("recipe", legacy.owner(), reference,
            definition.active() ? OwnerStatus.ACTIVE : OwnerStatus.BLOCKED, definition.blockedReason());
    }

    public static List<PageEntry> pages() {
        return List.copyOf(PAGES);
    }

    public static List<PageEntry> pagesByType(PageType type) {
        return PAGES.stream().filter(page -> page.type == type).toList();
    }

    /** Resolve only production-ready pages; BLOCKED content is never a completed page action. */
    public static Optional<PageEntry> findActionable(ResourceLocation id) {
        return PAGES.stream().filter(page -> page.id().equals(id)
            && page.content().status() == OwnerStatus.ACTIVE).findFirst();
    }

    public static boolean isAvailableForBook(PageEntry page, int bookType) {
        return bookType >= 0 && bookType <= 4 && bookType >= page.requiredBookType();
    }

    private static void registerPage(String id, String titleKey, PageType type, ResourceLocation researchId,
            ContentRef content, int requiredBookType) {
        PAGES.add(new PageEntry(resId(id), titleKey, type, researchId, ItemStack.EMPTY, content,
            requiredBookType));
    }

    private static ContentRef active(String kind, String owner, String reference) {
        return new ContentRef(kind, owner, reference, OwnerStatus.ACTIVE, "");
    }

    private static ImageContent loadImage(String reference) {
        ResourceLocation texture = com.shinoow.abyssalcraft.platform.ACRef.parse(reference);
        String resource = "assets/" + texture.getNamespace() + "/" + texture.getPath();
        try (InputStream stream = NecronomiconPageManifest.class.getClassLoader().getResourceAsStream(resource)) {
            if (stream == null) throw new IllegalStateException("missing Necronomicon image " + reference);
            BufferedImage image = ImageIO.read(stream);
            if (image == null) throw new IllegalStateException("undecodable Necronomicon image " + reference);
            return new ImageContent(texture, image.getWidth(), image.getHeight(), 0, 0, 256, 256);
        } catch (IOException exception) {
            throw new IllegalStateException("unable to read Necronomicon image " + reference, exception);
        }
    }

    static String catalogId(String category, String id) {
        return category + "/" + id.toLowerCase(java.util.Locale.ROOT);
    }

    private static PageType legacyType(String id) {
        if (id.startsWith("CRAFTING_")) return PageType.RECIPE;
        if (id.startsWith("RITUAL_")) return PageType.RITUAL;
        if (id.startsWith("SPELL_")) return PageType.SPELL;
        if (id.startsWith("MATERIAL_")) return PageType.MATERIAL;
        if (id.startsWith("PROGRESSION_") || id.startsWith("PE_UPGRADING_")) return PageType.PROGRESSION;
        if (id.startsWith("PLACES_OF_POWER_")) return PageType.PLACE_OF_POWER;
        if (id.equals("SHOGGOTH_LAIR") || id.equals("GRAVEYARD") || id.equals("DARK_SHRINE")
            || id.equals("DARK_STRUCTURE") || id.equals("LAIR_OF_CHAGAROTH_1")) return PageType.STRUCTURE;
        return PageType.INFORMATION;
    }

    private static ResourceLocation resId(String path) {
        return com.shinoow.abyssalcraft.platform.ACRef.id(path);
    }
}
