package com.shinoow.abyssalcraft.net.client;

import com.shinoow.abyssalcraft.platform.NetworkChannel;
import com.shinoow.abyssalcraft.platform.SideExecutor;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

/** Server &rarr; client: display a short-lived, coloured world-space line effect. */
public final class LineEffectMessage implements NetworkChannel.ACPacket {

    private final Vec3 start;
    private final Vec3 end;
    private final int startColor;
    private final int endColor;
    private final int durationTicks;

    public LineEffectMessage(Vec3 start, Vec3 end, int startColor, int endColor, int durationTicks) {
        this.start = start;
        this.end = end;
        this.startColor = startColor;
        this.endColor = endColor;
        this.durationTicks = durationTicks;
    }

    public LineEffectMessage(FriendlyByteBuf buf) {
        this(new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
            new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
            buf.readInt(), buf.readInt(), buf.readVarInt());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeDouble(start.x);
        buf.writeDouble(start.y);
        buf.writeDouble(start.z);
        buf.writeDouble(end.x);
        buf.writeDouble(end.y);
        buf.writeDouble(end.z);
        buf.writeInt(startColor);
        buf.writeInt(endColor);
        buf.writeVarInt(durationTicks);
    }

    @Override
    public void handle(NetworkChannel.Context ctx) {
        SideExecutor.runWhenClient(() -> () ->
            com.shinoow.abyssalcraft.client.render.effect.LineEffectRenderer.add(
                start, end, startColor, endColor, durationTicks));
    }
}