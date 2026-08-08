package com.shinoow.abyssalcraft.client.necronomicon;

import java.util.function.UnaryOperator;

import com.mojang.blaze3d.systems.RenderSystem;
import com.shinoow.abyssalcraft.net.ACNetwork;
import com.shinoow.abyssalcraft.net.server.NecronomiconPageActionMessage;
import com.shinoow.abyssalcraft.net.server.OpenSpellbookMessage;
import com.shinoow.abyssalcraft.platform.ACRef;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import vazkii.patchouli.api.IComponentRenderContext;
import vazkii.patchouli.api.ICustomComponent;
import vazkii.patchouli.api.IVariable;

/** Patchouli template component for server-validated page study and Spellbook actions. */
public final class PatchouliActionComponent implements ICustomComponent {

    private String action = "";
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
        if (!"spellbook".equals(action)) return;
        boolean hovered = context.isAreaHovered(mouseX, mouseY, x, y, 100, 18);
        graphics.fill(x, y, x + 100, y + 18, hovered ? 0xB0705040 : 0x90604030);
        graphics.drawCenteredString(Minecraft.getInstance().font,
            Component.translatable("container.abyssalcraft.spellbook").withStyle(context.getFont()),
            x + 50, y + 5, hovered ? 0xFFF2CF : 0xE8D8B0);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public void onDisplayed(IComponentRenderContext context) {
        if (!dispatched && "study".equals(action) && !page.isBlank()) {
            ACNetwork.sendToServer(new NecronomiconPageActionMessage(ACRef.parse(page)));
            dispatched = true;
        }
    }

    @Override
    public boolean mouseClicked(IComponentRenderContext context, double mouseX, double mouseY,
                                int mouseButton) {
        if (mouseButton == 0 && "spellbook".equals(action)
            && context.isAreaHovered((int) mouseX, (int) mouseY, x, y, 100, 18)) {
            ACNetwork.sendToServer(new OpenSpellbookMessage());
            return true;
        }
        return false;
    }

    //? if >=1.21 {
    /*@Override
    public void onVariablesAvailable(UnaryOperator<IVariable> lookup,
                                     net.minecraft.core.HolderLookup.Provider registries) {
        action = lookup.apply(IVariable.wrap(action, registries)).asString();
        page = lookup.apply(IVariable.wrap(page, registries)).asString();
        dispatched = false;
    }
    *///?} else {
    @Override
    public void onVariablesAvailable(UnaryOperator<IVariable> lookup) {
        action = lookup.apply(IVariable.wrap(action)).asString();
        page = lookup.apply(IVariable.wrap(page)).asString();
        dispatched = false;
    }
    //?}
}