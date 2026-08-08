package com.shinoow.abyssalcraft.client.necronomicon;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import com.shinoow.abyssalcraft.net.ACNetwork;
import com.shinoow.abyssalcraft.net.server.NecronomiconPageActionMessage;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.ResearchAdvancementCompat;
import com.shinoow.abyssalcraft.system.energy.AmplifierType;
import com.shinoow.abyssalcraft.system.energy.structure.EnergyStructures;
import com.shinoow.abyssalcraft.system.energy.structure.IPlaceOfPower;
import com.shinoow.abyssalcraft.system.knowledge.IResearchItem;
import com.shinoow.abyssalcraft.system.knowledge.KnowledgeContent;
import com.shinoow.abyssalcraft.system.knowledge.NecronomiconPageManifest;
import com.shinoow.abyssalcraft.system.ritual.RitualIngredient;
import com.shinoow.abyssalcraft.system.ritual.RitualManifest;
import com.shinoow.abyssalcraft.system.ritual.RitualManifestCatalog;
import com.shinoow.abyssalcraft.system.spell.SpellIngredient;
import com.shinoow.abyssalcraft.system.spell.SpellManifest;
import com.shinoow.abyssalcraft.system.spell.SpellManifestCatalog;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import vazkii.patchouli.api.IComponentRenderContext;
import vazkii.patchouli.api.ICustomComponent;
import vazkii.patchouli.api.IVariable;

/** Renders authoritative ritual, spell, and Place of Power manifests inside Patchouli. */
public final class PatchouliManifestComponent implements ICustomComponent {

    private String kind = "";
    private String id = "";
    private String page = "";
    private transient int x;
    private transient int y;
    private transient boolean dispatched;

    @Override
    public void build(int componentX, int componentY, int pageNum) {
        x = componentX;
        y = componentY;
    }

    @Override
    public void render(GuiGraphics graphics, IComponentRenderContext context, float partialTick,
                       int mouseX, int mouseY) {
        switch (kind) {
            case "ritual" -> renderRitual(graphics, context, mouseX, mouseY);
            case "spell" -> renderSpell(graphics, context, mouseX, mouseY);
            case "place_of_power" -> renderStructure(graphics, context, mouseX, mouseY);
            case "research" -> renderResearch(graphics);
            default -> drawLines(graphics, List.of(Component.literal(id)), y);
        }
    }

    @Override
    public void onDisplayed(IComponentRenderContext context) {
        if (!dispatched && !page.isBlank()) {
            ACNetwork.sendToServer(new NecronomiconPageActionMessage(ACRef.parse(page)));
            dispatched = true;
        }
    }

    private void renderRitual(GuiGraphics graphics, IComponentRenderContext context,
                              int mouseX, int mouseY) {
        RitualManifest ritual = RitualManifestCatalog.get(id);
        if (ritual == null) {
            drawLines(graphics, List.of(Component.literal(id)), y);
            return;
        }
        int lineY = y;
        drawCentered(graphics, ritualTitle(ritual), lineY);
        lineY += 14;
        List<Component> details = new ArrayList<>();
        details.add(Component.translatable("gui.abyssalcraft.necronomicon.patchouli.ritual.kind",
                Component.translatable("jei.abyssalcraft.ritual_kind."
                    + ritual.kind().name().toLowerCase(java.util.Locale.ROOT))));
        details.add(Component.translatable("gui.abyssalcraft.necronomicon.patchouli.energy",
            format(ritual.requiredEnergy())));
        details.add(Component.translatable("gui.abyssalcraft.necronomicon.patchouli.book_tier", ritual.bookType()));
        details.add(Component.translatable("gui.abyssalcraft.necronomicon.patchouli.dimension",
                ritual.dimension() == null ? Component.translatable("gui.abyssalcraft.necronomicon.patchouli.any")
                    : Component.literal(ritual.dimension().location().toString())));
        details.add(Component.translatable("gui.abyssalcraft.necronomicon.patchouli.sacrifice",
            Component.translatable(ritual.requiresSacrifice() ? "options.on" : "options.off")));
        if (ritual.result() != null) {
            details.add(Component.translatable("gui.abyssalcraft.necronomicon.patchouli.ritual.result",
                registryDescription(ritual.result())));
        }
        if (!ritual.actionTargets().isEmpty()) {
            details.add(Component.translatable("gui.abyssalcraft.necronomicon.patchouli.ritual.targets",
                joinedDescriptions(ritual.actionTargets())));
        }
        lineY = drawLines(graphics, details, lineY);
        lineY += 3;
        graphics.drawString(Minecraft.getInstance().font,
            Component.translatable("gui.abyssalcraft.necronomicon.patchouli.ritual.layout"),
            x, lineY, 0x333333, false);
        lineY += 11;
        renderIngredient(graphics, context, ritual.center(), x + 42, lineY + 18, mouseX, mouseY);
        for (int slot = 0; slot < ritual.offeringLayout().size(); slot++) {
            double angle = Math.PI * 2.0D * slot / ritual.offeringLayout().size() - Math.PI / 2.0D;
            int itemX = x + 42 + (int) Math.round(Math.cos(angle) * 35.0D);
            int itemY = lineY + 18 + (int) Math.round(Math.sin(angle) * 25.0D);
            renderIngredient(graphics, context, ritual.offeringLayout().get(slot), itemX, itemY, mouseX, mouseY);
        }
    }

    private void renderSpell(GuiGraphics graphics, IComponentRenderContext context,
                             int mouseX, int mouseY) {
        SpellManifest spell = SpellManifestCatalog.get(id);
        if (spell == null) {
            drawLines(graphics, List.of(Component.literal(id)), y);
            return;
        }
        int lineY = y;
        drawCentered(graphics, Component.translatable("ac.spell." + spell.id()), lineY);
        lineY += 14;
        List<Component> details = List.of(
            Component.translatable("gui.abyssalcraft.necronomicon.patchouli.energy", format(spell.requiredEnergy())),
            Component.translatable("gui.abyssalcraft.necronomicon.patchouli.book_tier", spell.bookType()),
            Component.translatable("gui.abyssalcraft.necronomicon.patchouli.spell.scroll",
                Component.translatable("jei.abyssalcraft.scroll_type." + spell.scrollType().name().toLowerCase(java.util.Locale.ROOT))),
            Component.translatable("gui.abyssalcraft.necronomicon.patchouli.spell.target",
                Component.translatable("jei.abyssalcraft.spell_target." + spell.targetType().name().toLowerCase(java.util.Locale.ROOT))),
            Component.translatable("gui.abyssalcraft.necronomicon.patchouli.spell.charging",
                Component.translatable(spell.requiresCharging() ? "options.on" : "options.off")));
        lineY = drawLines(graphics, details, lineY);
        lineY += 5;
        graphics.drawString(Minecraft.getInstance().font,
            Component.translatable("gui.abyssalcraft.necronomicon.patchouli.spell.reagents"),
            x, lineY, 0x333333, false);
        lineY += 12;
        int itemX = x;
        for (SpellIngredient reagent : spell.reagentLayout()) {
            if (!reagent.isEmpty()) {
                ItemStack stack = reagent.example();
                if (!stack.isEmpty()) context.renderItemStack(graphics, itemX, lineY, mouseX, mouseY, stack);
            }
            itemX += 20;
        }
    }

    private void renderStructure(GuiGraphics graphics, IComponentRenderContext context,
                                 int mouseX, int mouseY) {
        IPlaceOfPower structure = EnergyStructures.ALL.stream()
            .filter(candidate -> candidate.getIdentifier().equals(id)).findFirst().orElse(null);
        if (structure == null) {
            drawLines(graphics, List.of(Component.literal(id)), y);
            return;
        }
        int lineY = y;
        drawCentered(graphics, Component.translatable(
            NecronomiconPageManifest.placeOfPowerTitleKey(structure)), lineY);
        lineY += 15;
        List<Component> details = new ArrayList<>();
        details.add(Component.translatable("gui.abyssalcraft.necronomicon.patchouli.book_tier",
            structure.getBookType()));
        details.add(Component.translatable("gui.abyssalcraft.necronomicon.patchouli.structure.blocks",
            Component.translatable(structure.getRequiredBlockNamesKey())));
        details.add(Component.translatable("gui.abyssalcraft.necronomicon.patchouli.structure.activation",
            structure.getActivationPointForRender().toShortString()));
        for (AmplifierType type : AmplifierType.values()) {
            float amplifier = structure.getAmplifier(type);
            if (amplifier != 0.0F) {
                details.add(Component.translatable("gui.abyssalcraft.necronomicon.patchouli.structure.amplifier",
                    Component.translatable("gui.abyssalcraft.necronomicon.patchouli.amplifier."
                        + type.name().toLowerCase(java.util.Locale.ROOT)), format(amplifier)));
            }
        }
        lineY = drawLines(graphics, details, lineY);
        lineY += 6;
        int itemX = x;
        for (ItemStack stack : structureStacks(structure)) {
            context.renderItemStack(graphics, itemX, lineY, mouseX, mouseY, stack);
            itemX += 20;
            if (itemX > x + 80) {
                itemX = x;
                lineY += 20;
            }
        }
    }

    private void renderResearch(GuiGraphics graphics) {
        IResearchItem research = KnowledgeContent.researches().stream()
            .filter(candidate -> candidate.getID().getPath().equals(id)).findFirst().orElse(null);
        if (research == null) {
            drawLines(graphics, List.of(Component.literal(id)), y);
            return;
        }
        drawCentered(graphics, Component.translatable(research.getName()), y);
        drawLines(graphics, List.of(Component.translatable(
            ResearchAdvancementCompat.conditionTranslationKey(research),
            ResearchAdvancementCompat.conditionTarget(research))), y + 18);
    }

    private static List<ItemStack> structureStacks(IPlaceOfPower structure) {
        Map<ResourceLocation, ItemStack> stacks = new LinkedHashMap<>();
        for (BlockState[][] layer : structure.getRenderData()) {
            for (BlockState[] row : layer) {
                for (BlockState state : row) {
                    if (state == null) continue;
                    ItemStack stack = new ItemStack(state.getBlock());
                    if (!stack.isEmpty()) {
                        stacks.putIfAbsent(BuiltInRegistries.ITEM.getKey(stack.getItem()), stack);
                    }
                }
            }
        }
        return new ArrayList<>(stacks.values());
    }

    private static Component ritualTitle(RitualManifest ritual) {
        if (ritual.result() == null) {
            return Component.translatable(NecronomiconPageManifest.ritualTitleKey(ritual));
        }
        if (ritual.result() != null && BuiltInRegistries.ITEM.containsKey(ritual.result())) {
            return BuiltInRegistries.ITEM.get(ritual.result()).getDescription();
        }
        if (ritual.actionTargets().size() == 1) {
            ResourceLocation target = ritual.actionTargets().get(0);
            if (BuiltInRegistries.ENTITY_TYPE.containsKey(target)) {
                return BuiltInRegistries.ENTITY_TYPE.get(target).getDescription();
            }
        }
        return Component.translatable("jei.abyssalcraft.ritual_kind."
            + ritual.kind().name().toLowerCase(java.util.Locale.ROOT));
    }

    private static Component registryDescription(ResourceLocation id) {
        if (BuiltInRegistries.ITEM.containsKey(id)) return BuiltInRegistries.ITEM.get(id).getDescription();
        if (BuiltInRegistries.ENTITY_TYPE.containsKey(id)) {
            return BuiltInRegistries.ENTITY_TYPE.get(id).getDescription();
        }
        return Component.literal(id.toString());
    }

    private static Component joinedDescriptions(List<ResourceLocation> ids) {
        net.minecraft.network.chat.MutableComponent text = Component.empty();
        for (int index = 0; index < ids.size(); index++) {
            if (index > 0) text.append(", ");
            text.append(registryDescription(ids.get(index)));
        }
        return text;
    }

    private static void renderIngredient(GuiGraphics graphics, IComponentRenderContext context,
                                         RitualIngredient ingredient, int x, int y,
                                         int mouseX, int mouseY) {
        if (ingredient.isEmpty()) return;
        ItemStack stack = ingredient.example();
        if (!stack.isEmpty()) context.renderItemStack(graphics, x, y, mouseX, mouseY, stack);
    }

    private void drawCentered(GuiGraphics graphics, Component text, int lineY) {
        graphics.drawCenteredString(Minecraft.getInstance().font, text, x + 50, lineY, 0x333333);
    }

    private int drawLines(GuiGraphics graphics, List<Component> lines, int lineY) {
        for (Component line : lines) {
            for (var sequence : Minecraft.getInstance().font.split(line, 100)) {
                graphics.drawString(Minecraft.getInstance().font, sequence, x, lineY, 0x333333, false);
                lineY += Minecraft.getInstance().font.lineHeight;
            }
        }
        return lineY;
    }

    private static String format(float value) {
        return value == (int) value ? Integer.toString((int) value) : Float.toString(value);
    }

    //? if >=1.21 {
    /*@Override
    public void onVariablesAvailable(UnaryOperator<IVariable> lookup,
                                     net.minecraft.core.HolderLookup.Provider registries) {
        kind = lookup.apply(IVariable.wrap(kind, registries)).asString();
        id = lookup.apply(IVariable.wrap(id, registries)).asString();
        page = lookup.apply(IVariable.wrap(page, registries)).asString();
        dispatched = false;
    }
    *///?} else {
    @Override
    public void onVariablesAvailable(UnaryOperator<IVariable> lookup) {
        kind = lookup.apply(IVariable.wrap(kind)).asString();
        id = lookup.apply(IVariable.wrap(id)).asString();
        page = lookup.apply(IVariable.wrap(page)).asString();
        dispatched = false;
    }
    //?}
}
