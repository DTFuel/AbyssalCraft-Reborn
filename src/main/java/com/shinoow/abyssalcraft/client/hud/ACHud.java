package com.shinoow.abyssalcraft.client.hud;

import com.shinoow.abyssalcraft.platform.ClientHooksCompat;
import com.shinoow.abyssalcraft.system.energy.IEnergyContainerItem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * AC HUD relay (owned by PH-6): registers the client resource-reload listener ({@link ClientVarsManager}) and
 * the AC HUD overlays with the loader compat. The PE meter reads the held energy item ({@link
 * IEnergyContainerItem}, delivered CR-58) and draws its Potential Energy bar &mdash; so charging at a deity
 * statue (CR-59) is now visible. The remaining faithful HUD content (spirit-tablet path/filter, dimension
 * info) still needs its unported tools; those stay deferred (PH-6b).
 */
public final class ACHud {

    private ACHud() {}

    /** Queue the AC client reload listener + HUD overlays (client-side, called from main init). */
    public static void register() {
        ClientHooksCompat.queueReloadListener(ClientVarsManager.instance());
        ClientHooksCompat.queueOverlay("pe_meter", ACHud::renderPeMeter);
    }

    /** Draw the Potential Energy bar for the held energy item (Necronomicon/charm); nothing if none held. */
    private static void renderPeMeter(GuiGraphics graphics, int width, int height) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            return;
        }
        ItemStack stack = heldEnergyItem(player);
        if (stack == null) {
            return;
        }
        IEnergyContainerItem energyItem = (IEnergyContainerItem) stack.getItem();
        int max = energyItem.getMaxEnergy(stack);
        if (max <= 0) {
            return;
        }
        float contained = energyItem.getContainedEnergy(stack);
        int x = 6;
        int y = 6;
        int barWidth = 80;
        int barHeight = 6;
        int filled = (int) (barWidth * Mth.clamp(contained / max, 0F, 1F));
        graphics.fill(x - 1, y - 1, x + barWidth + 1, y + barHeight + 1, 0xFF000000);
        graphics.fill(x, y, x + barWidth, y + barHeight, 0xFF303030);
        graphics.fill(x, y, x + filled, y + barHeight, 0xFF00E0E0);
        graphics.drawString(minecraft.font, "PE " + (int) contained + " / " + max, x, y + barHeight + 2, 0xFF00E0E0);
    }

    /** The held {@link IEnergyContainerItem} (main hand preferred, then off hand), or {@code null}. */
    private static ItemStack heldEnergyItem(Player player) {
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof IEnergyContainerItem) {
            return main;
        }
        ItemStack off = player.getOffhandItem();
        if (off.getItem() instanceof IEnergyContainerItem) {
            return off;
        }
        return null;
    }
}
