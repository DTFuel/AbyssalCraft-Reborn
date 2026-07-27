package com.shinoow.abyssalcraft.net.server;

import com.shinoow.abyssalcraft.content.item.scroll.ScrollItem;
import com.shinoow.abyssalcraft.platform.NetworkChannel;
import com.shinoow.abyssalcraft.system.spell.ManifestSpell;
import com.shinoow.abyssalcraft.system.spell.Spell;
import com.shinoow.abyssalcraft.system.spell.SpellRegistry;
import com.shinoow.abyssalcraft.system.spell.SpellUtils;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Client &rarr; server entity-target hint. The server ignores the packet spell id/quality and resolves
 * both from the sender's actively charged scroll before validating range, target and PE.
 */
public class MobSpellMessage implements NetworkChannel.ACPacket {

    private final int id;
    private final String spellID;
    private final int scrollType;

    public MobSpellMessage(int id, String spellID, int scrollType) {
        this.id = id;
        this.spellID = spellID;
        this.scrollType = scrollType;
    }

    public MobSpellMessage(FriendlyByteBuf buf) {
        this.id = buf.readVarInt();
        this.spellID = buf.readUtf(128);
        this.scrollType = buf.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(id);
        buf.writeUtf(spellID);
        buf.writeVarInt(scrollType);
    }

    @Override
    public void handle(NetworkChannel.Context ctx) {
        if (!(ctx.player() instanceof ServerPlayer player) || !player.isUsingItem()) return;
        ItemStack stack = player.getUseItem();
        if (!(stack.getItem() instanceof ScrollItem scroll)) return;
        Spell registered = SpellRegistry.instance().getSpell(ScrollItem.spellId(stack));
        if (!(registered instanceof ManifestSpell spell) || !spell.requiresCharging()
            || player.getTicksUsingItem() + 1 < scroll.requiredUseTicks(stack)) return;
        if (!(player.level().getEntity(id) instanceof LivingEntity target)) return;
        if (SpellUtils.castManifest(player.level(), player, spell, stack, scroll.getScrollType(stack), target)) {
            ScrollItem.consumeScroll(player, stack);
            player.stopUsingItem();
        }
    }
}
