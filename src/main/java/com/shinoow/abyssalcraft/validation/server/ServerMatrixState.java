package com.shinoow.abyssalcraft.validation.server;

import com.shinoow.abyssalcraft.platform.SavedDataCompat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

/** Persistent phase marker for the restart half of RR-SERVER/T11.2. */
public final class ServerMatrixState extends SavedDataCompat {

    private static final String DATA_NAME = "abyssalcraft_rr_server_matrix";

    private int phase;

    public static ServerMatrixState get(ServerLevel level) {
        return SavedDataCompat.getOrCreate(level, DATA_NAME, ServerMatrixState::new, ServerMatrixState::load);
    }

    public int phase() {
        return phase;
    }

    public void setPhase(int phase) {
        this.phase = phase;
        setDirty();
    }

    @Override
    protected CompoundTag saveData(CompoundTag tag) {
        tag.putInt("Phase", phase);
        return tag;
    }

    private static ServerMatrixState load(CompoundTag tag) {
        ServerMatrixState state = new ServerMatrixState();
        state.phase = tag.getInt("Phase");
        return state;
    }
}