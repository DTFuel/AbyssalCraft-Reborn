package com.shinoow.abyssalcraft.validation.server;

import com.shinoow.abyssalcraft.config.ConfigContractSelfTest;
import com.shinoow.abyssalcraft.content.entity.behavior.EntityBehaviorSelfTest;
import com.shinoow.abyssalcraft.content.machine.MachineSelfTest;
import com.shinoow.abyssalcraft.net.NetworkSelfTest;
import com.shinoow.abyssalcraft.system.knowledge.KnowledgeSystemSelfTest;
import com.shinoow.abyssalcraft.system.portal.PortalSelfTest;
import com.shinoow.abyssalcraft.system.ritual.ResurrectionLiveFixture;
import com.shinoow.abyssalcraft.system.ritual.RitualManifestSelfTest;
import com.shinoow.abyssalcraft.system.spell.SpellManifestSelfTest;
import com.shinoow.abyssalcraft.validation.world.StructureFixtureValidator;
import com.shinoow.abyssalcraft.validation.world.WorldgenFinalMatrix;
import com.shinoow.abyssalcraft.world.ACDimensions;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

/** Single property-gated, restart-aware orchestration for RR-SERVER/T11.2. */
public final class ServerMatrixOrchestrator {

    public static final String ENABLE_PROPERTY = "abyssalcraft.rrServerMatrix";

    private ServerMatrixOrchestrator() {}

    public static void run(MinecraftServer server) {
        if (!Boolean.getBoolean(ENABLE_PROPERTY)) return;

        ServerLevel overworld = requireLevel(server.overworld(), "minecraft:overworld");
        ServerMatrixState state = ServerMatrixState.get(overworld);
        int phase = state.phase();
        require(phase == 0 || phase == 1, "invalid persistent phase=" + phase);

        runPermanentFixtures(server);
        ResurrectionLiveFixture.Result resurrection = ResurrectionLiveFixture.run(server);
        if (phase == 0) {
            require(resurrection == ResurrectionLiveFixture.Result.PENDING_RESTART,
                "resurrection fixture did not request restart");
            state.setPhase(1);
            server.saveEverything(true, true, true);
            System.out.println("RR_SERVER_MATRIX_PENDING phase=new_world restart=automatic");
            server.halt(false);
            return;
        }

        require(resurrection == ResurrectionLiveFixture.Result.COMPLETE,
            "resurrection fixture did not complete after restart");
        state.setPhase(0);
        server.saveEverything(true, true, true);
        System.out.println("RR_SERVER_MATRIX_OK phases=2 structures="
            + StructureFixtureValidator.totalStructureCoverage()
            + " entityLoot=69 resolvedLoot=97 network=24 config=145 knowledge=401 restart=ok");
        server.halt(false);
    }

    private static void runPermanentFixtures(MinecraftServer server) {
        ServerLevel wasteland = requireLevel(server.getLevel(ACDimensions.ABYSSAL_WASTELAND),
            "abyssalcraft:abyssal_wasteland");
        ServerLevel dreadlands = requireLevel(server.getLevel(ACDimensions.DREADLANDS),
            "abyssalcraft:dreadlands");
        require(WorldgenFinalMatrix.executeServerMatrix(wasteland, dreadlands)
            .startsWith("RR_WORLD_SERVER_MATRIX_PASS"), "world fixture failed");
        require(StructureFixtureValidator.legacyTemplateCount() == 37
            && StructureFixtureValidator.totalStructureCoverage() >= 37,
            "structure coverage is below the 37-structure contract");
        EntityBehaviorSelfTest.run();
        NaturalSpawnServerFixture.run(server);
        ServerRuntimeDataFixture.run(server);
        MachineSelfTest.run(server.registryAccess());
        RitualManifestSelfTest.run();
        SpellManifestSelfTest.run();
        PortalSelfTest.run();
        NetworkSelfTest.run();
        ConfigContractSelfTest.run();
        KnowledgeSystemSelfTest.run(server.registryAccess());
        require(KnowledgeSystemSelfTest.manifestPageCount() == 401,
            "knowledge manifest is not 401 pages");
    }

    private static ServerLevel requireLevel(ServerLevel level, String id) {
        if (level == null) throw new IllegalStateException("RR_SERVER_MATRIX_FAIL missing level " + id);
        return level;
    }

    private static void require(boolean condition, String reason) {
        if (!condition) throw new IllegalStateException("RR_SERVER_MATRIX_FAIL " + reason);
    }
}