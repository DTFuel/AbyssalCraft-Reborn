package com.shinoow.abyssalcraft.net.server;

import com.shinoow.abyssalcraft.common.handlers.KnowledgeHooks;
import com.shinoow.abyssalcraft.content.item.book.NecronomiconItem;
import com.shinoow.abyssalcraft.platform.NetworkChannel;
import com.shinoow.abyssalcraft.system.cap.necrodata.NecroData;
import com.shinoow.abyssalcraft.system.cap.necrodata.NecroDataCapability;
import com.shinoow.abyssalcraft.system.knowledge.NecronomiconPageManifest;
import com.shinoow.abyssalcraft.system.portal.DimensionDataRegistry;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/** Client request to consume a displayed Necronomicon leaf page; the server validates the manifest entry. */
public final class NecronomiconPageActionMessage implements NetworkChannel.ACPacket {

    private final ResourceLocation pageId;

    public NecronomiconPageActionMessage(ResourceLocation pageId) {
        this.pageId = pageId;
    }

    public NecronomiconPageActionMessage(FriendlyByteBuf buffer) {
        this.pageId = buffer.readResourceLocation();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(pageId);
    }

    @Override
    public void handle(NetworkChannel.Context context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        int bookType = heldBookType(player);
        NecronomiconPageManifest.findActionable(pageId).filter(page -> canRead(player, page, bookType))
            .ifPresent(page -> KnowledgeHooks.onPageStudied(player, page.id().toString()));
    }

    private static int heldBookType(ServerPlayer player) {
        int main = bookType(player.getMainHandItem());
        int offhand = bookType(player.getOffhandItem());
        return Math.max(main, offhand);
    }

    private static int bookType(net.minecraft.world.item.ItemStack stack) {
        return stack.getItem() instanceof NecronomiconItem book ? book.bookType() : -1;
    }

    private static boolean canRead(ServerPlayer player, NecronomiconPageManifest.PageEntry page, int bookType) {
        if (!NecronomiconPageManifest.isAvailableForBook(page, bookType)) {
            return false;
        }
        if (DimensionDataRegistry.instance().requiredBookType(player.level().dimension()).stream()
                .noneMatch(required -> bookType >= required)) {
            return false;
        }
        if (page.researchId() == null) {
            return true;
        }
        NecroData data = NecroDataCapability.get(player);
        return data.hasUnlockedAllKnowledge() || data.getCompletedResearches().contains(page.researchId().toString());
    }
}