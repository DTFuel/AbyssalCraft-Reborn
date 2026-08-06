package com.shinoow.abyssalcraft.platform;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

//? if forge {
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
//?} else {
/*import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
*///?}

/**
 * Compat: networking (loader axis - the deepest divergence).
 *
 * <p>Forge uses a classic {@code SimpleChannel}; NeoForge uses the 1.20.5+ payload system. Both are
 * hidden behind one multiplexed envelope, so mod messages only implement the shared {@link ACPacket}
 * and never touch loader network APIs. Call {@link #bootstrap(Object)} once from the mod constructor,
 * passing the MOD event bus.
 *
 * <p>Compile-verified on both nodes; the runtime message flow is exercised when the first real
 * messages land in Stage S-A ({@code net/**}).
 */
public final class NetworkChannel {

    public enum Direction { SERVER_BOUND, CLIENT_BOUND }

    /** A message; implementations are shared across loaders. */
    public interface ACPacket {
        void write(FriendlyByteBuf buf);
        void handle(Context ctx);
    }

    /** Handler context over {@code NetworkEvent.Context} / {@code IPayloadContext}. */
    public interface Context {
        /** Player whose connection carried the message (server: the sender), or {@code null}. */
        Player player();
        void enqueue(Runnable task);
    }

    private static final int PROTOCOL = 3;
    private static final int MAX_BODY_BYTES = 1 << 20;

    private final ResourceLocation channelId;
    private final Map<Integer, Registration<? extends ACPacket>> registrations = new HashMap<>();
    private final Map<Class<?>, Integer> idByType = new HashMap<>();

    //? if forge {
    private SimpleChannel channel;
    //?}

    private NetworkChannel(ResourceLocation id) {
        this.channelId = id;
    }

    public static NetworkChannel create(String name) {
        return new NetworkChannel(ACRef.id(name));
    }

    /** Register a message type with a numeric id, direction and decoder. */
    public <M extends ACPacket> void register(int id, Class<M> type, Direction direction,
                                              Function<FriendlyByteBuf, M> decoder) {
        if (id < 0 || registrations.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate or invalid packet id: " + id);
        }
        if (idByType.containsKey(type)) {
            throw new IllegalArgumentException("Duplicate packet type: " + type.getName());
        }
        registrations.put(id, new Registration<>(direction, decoder));
        idByType.put(type, id);
    }

    /** Stable wire ids currently registered on this channel. */
    public Set<Integer> registeredIds() {
        return Set.copyOf(registrations.keySet());
    }

    /** Number of registered packet types. */
    public int registeredCount() {
        return registrations.size();
    }

    /** Stable wire id assigned to {@code type}, or {@code -1} when it is not registered. */
    public int registeredId(Class<? extends ACPacket> type) {
        return idByType.getOrDefault(type, -1);
    }

    /** Registered reception direction for {@code type}, or {@code null} when it is not registered. */
    public Direction registeredDirection(Class<? extends ACPacket> type) {
        int id = registeredId(type);
        Registration<? extends ACPacket> registration = registrations.get(id);
        return registration == null ? null : registration.direction();
    }

    /** Encode, decode and encode a packet again for permanent wire-format validation. */
    public byte[] roundTrip(ACPacket msg) {
        int id = idOf(msg);
        byte[] first = encodeBody(msg);
        FriendlyByteBuf input = new FriendlyByteBuf(Unpooled.wrappedBuffer(first));
        ACPacket decoded = registrations.get(id).decoder().apply(input);
        if (input.isReadable()) {
            throw new IllegalStateException("Packet decoder left trailing bytes for id " + id);
        }
        return encodeBody(decoded);
    }

    private int idOf(ACPacket msg) {
        Integer id = idByType.get(msg.getClass());
        if (id == null) {
            throw new IllegalArgumentException("Unregistered packet type: " + msg.getClass().getName());
        }
        return id;
    }

    private byte[] encodeBody(ACPacket msg) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        msg.write(buf);
        if (buf.readableBytes() > MAX_BODY_BYTES) {
            throw new IllegalArgumentException("Packet body exceeds " + MAX_BODY_BYTES + " bytes");
        }
        byte[] body = new byte[buf.readableBytes()];
        buf.readBytes(body);
        return body;
    }

    private boolean dispatch(int id, byte[] body, Direction receptionDirection, Context ctx) {
        Registration<? extends ACPacket> registration = registrations.get(id);
        if (registration == null || registration.direction() != receptionDirection
                || body == null || body.length > MAX_BODY_BYTES) {
            return false;
        }
        FriendlyByteBuf input = new FriendlyByteBuf(Unpooled.wrappedBuffer(body));
        ACPacket msg = registration.decoder().apply(input);
        if (input.isReadable()) {
            return false;
        }
        ctx.enqueue(() -> msg.handle(ctx));
        return true;
    }

    /** Exercise the production direction gate without running the packet handler. */
    public boolean testDirectionGate(ACPacket msg, Direction receptionDirection) {
        boolean[] enqueued = { false };
        boolean accepted = dispatch(idOf(msg), encodeBody(msg), receptionDirection, new Context() {
            @Override public Player player() { return null; }
            @Override public void enqueue(Runnable task) { enqueued[0] = true; }
        });
        return accepted && enqueued[0];
    }

    private record Registration<M extends ACPacket>(Direction direction,
                                                      Function<FriendlyByteBuf, M> decoder) {}

    //? if forge {
    /** Wire the Forge SimpleChannel and register the multiplexed envelope. */
    public void bootstrap(Object modBus) {
        channel = NetworkRegistry.newSimpleChannel(
            channelId, () -> Integer.toString(PROTOCOL),
            Integer.toString(PROTOCOL)::equals, Integer.toString(PROTOCOL)::equals);
        channel.registerMessage(0, Envelope.class,
                (env, buf) -> { buf.writeVarInt(env.id); buf.writeByteArray(env.body); },
            buf -> new Envelope(buf.readVarInt(), buf.readByteArray(MAX_BODY_BYTES)),
                (env, ctxSupplier) -> {
                    NetworkEvent.Context ctx = ctxSupplier.get();
                    Direction direction = ctx.getDirection().getReceptionSide().isServer()
                        ? Direction.SERVER_BOUND : Direction.CLIENT_BOUND;
                    dispatch(env.id, env.body, direction, new Context() {
                        @Override public Player player() { return ctx.getSender(); }
                        @Override public void enqueue(Runnable task) { ctx.enqueueWork(task); }
                    });
                    ctx.setPacketHandled(true);
                });
    }

    public void sendToServer(ACPacket msg) {
        channel.sendToServer(new Envelope(idOf(msg), encodeBody(msg)));
    }

    public void sendToPlayer(ServerPlayer target, ACPacket msg) {
        channel.send(PacketDistributor.PLAYER.with(() -> target), new Envelope(idOf(msg), encodeBody(msg)));
    }

    public void sendToAll(ACPacket msg) {
        channel.send(PacketDistributor.ALL.noArg(), new Envelope(idOf(msg), encodeBody(msg)));
    }

    private static final class Envelope {
        final int id;
        final byte[] body;
        Envelope(int id, byte[] body) { this.id = id; this.body = body; }
    }
    //?} else {
    /*public void bootstrap(Object modBus) {
        ((IEventBus) modBus).addListener(this::registerPayloads);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Integer.toString(PROTOCOL));
        registrar.playBidirectional(Envelope.TYPE, Envelope.STREAM_CODEC, (envelope, ctx) -> {
            Direction direction = ctx.flow() == PacketFlow.SERVERBOUND
                ? Direction.SERVER_BOUND : Direction.CLIENT_BOUND;
            dispatch(envelope.id(), envelope.body(), direction, new Context() {
                    @Override public Player player() { return ctx.player(); }
                    @Override public void enqueue(Runnable task) { ctx.enqueueWork(task); }
            });
        });
    }

    public void sendToServer(ACPacket msg) {
        PacketDistributor.sendToServer(new Envelope(idOf(msg), encodeBody(msg)));
    }

    public void sendToPlayer(ServerPlayer target, ACPacket msg) {
        PacketDistributor.sendToPlayer(target, new Envelope(idOf(msg), encodeBody(msg)));
    }

    public void sendToAll(ACPacket msg) {
        PacketDistributor.sendToAllPlayers(new Envelope(idOf(msg), encodeBody(msg)));
    }

    private record Envelope(int id, byte[] body) implements CustomPacketPayload {
        static final CustomPacketPayload.Type<Envelope> TYPE =
                new CustomPacketPayload.Type<>(ACRef.id("net_envelope"));
        static final StreamCodec<FriendlyByteBuf, Envelope> STREAM_CODEC = StreamCodec.of(
                (buf, env) -> { buf.writeVarInt(env.id()); buf.writeByteArray(env.body()); },
            buf -> new Envelope(buf.readVarInt(), buf.readByteArray(MAX_BODY_BYTES)));

        @Override public CustomPacketPayload.Type<Envelope> type() { return TYPE; }
    }
    *///?}
}
