package com.shinoow.abyssalcraft.system.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import com.shinoow.abyssalcraft.system.cap.necrodata.NecroData;
import com.shinoow.abyssalcraft.system.cap.necrodata.NecroDataCapability;
import com.shinoow.abyssalcraft.system.knowledge.KnowledgeSync;

/**
 * AbyssalCraft commands (owned by PJ-3 / Stage J). Fork-free: Brigadier ({@code com.mojang.brigadier})
 * and {@link Commands}/{@link CommandSourceStack} are identical on 1.20.1 and 1.21, so the whole builder
 * is loader-neutral. The loader axis (the {@code RegisterCommandsEvent} package) lives only in
 * {@code platform/CommandCompat}, which calls {@link #register}.
 *
 * <p>Faithful to the 1.12.2 {@code CommandUnlockAllKnowledge} ({@code /acunlockallknowledge}): toggles the
 * player's "all knowledge" flag on their necrodata (PS-2). The server-side knowledge gating (PS-8
 * {@code KnowledgeGate}) reflects it immediately; the client Necronomicon GUI updates once the necrodata
 * client-sync lands (the {@code NecroDataCapMessage} handler is a tracked PS-1/PS-8 stub).
 */
public final class ACCommands {

    private ACCommands() {}

    /** Register every AbyssalCraft command onto the server dispatcher (called from {@code RegisterCommandsEvent}). */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("acunlockallknowledge")
                .requires(source -> source.hasPermission(2))
                .executes(ctx -> unlockAllKnowledge(ctx.getSource())));
    }

    private static int unlockAllKnowledge(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        NecroData data = NecroDataCapability.get(player);
        boolean unlock = toggleAllKnowledge(data);
        com.shinoow.abyssalcraft.platform.ResearchAdvancementCompat.synchronize(player);
        KnowledgeSync.full(player);
        source.sendSuccess(() -> Component.literal(unlock
            ? "All knowledge has been unlocked!"
            : "All knowledge has been re-locked... kinda!"), false);
        return Command.SINGLE_SUCCESS;
    }

    static boolean toggleAllKnowledge(NecroData data) {
        boolean unlock = !data.hasUnlockedAllKnowledge();
        data.unlockAllKnowledge(unlock);
        return unlock;
    }
}
