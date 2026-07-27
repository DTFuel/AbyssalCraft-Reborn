package com.shinoow.abyssalcraft.system.knowledge;

import java.util.List;

import com.shinoow.abyssalcraft.content.block.ritual.RitualAltarBlockEntity;
import com.shinoow.abyssalcraft.system.energy.structure.StructureHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Ordered modern equivalent of the 1.12.2 Necronomicon action registry. */
public final class NecronomiconActions {

    private static final List<Action> ACTIONS = List.of(
        new Action("ritual", NecronomiconActions::isRitualAltar, NecronomiconActions::performRitual),
        new Action("place_of_power", NecronomiconActions::canFormStructure, NecronomiconActions::formStructure));

    private NecronomiconActions() {}

    public static InteractionResult execute(Player player, Level level, BlockPos pos, int bookType) {
        for (Action action : ACTIONS) {
            if (!action.predicate().test(player, level, pos, bookType)) {
                continue;
            }
            if (!level.isClientSide) {
                action.executor().execute(player, level, pos, bookType);
                if (player instanceof ServerPlayer serverPlayer) {
                    com.shinoow.abyssalcraft.common.handlers.KnowledgeHooks.onAction(serverPlayer, action.id());
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    public static List<String> actionIds() {
        return ACTIONS.stream().map(Action::id).toList();
    }

    private static boolean isRitualAltar(Player player, Level level, BlockPos pos, int bookType) {
        return level.getBlockEntity(pos) instanceof RitualAltarBlockEntity;
    }

    private static void performRitual(Player player, Level level, BlockPos pos, int bookType) {
        ((RitualAltarBlockEntity) level.getBlockEntity(pos)).tryRitual(level, pos, player);
    }

    private static boolean canFormStructure(Player player, Level level, BlockPos pos, int bookType) {
        return StructureHandler.instance().canFormStructure(level, pos, bookType, player);
    }

    private static void formStructure(Player player, Level level, BlockPos pos, int bookType) {
        StructureHandler.instance().formStructure(level, pos, bookType, player);
    }

    private record Action(String id, Predicate predicate, Executor executor) {}

    @FunctionalInterface
    private interface Predicate {
        boolean test(Player player, Level level, BlockPos pos, int bookType);
    }

    @FunctionalInterface
    private interface Executor {
        void execute(Player player, Level level, BlockPos pos, int bookType);
    }
}