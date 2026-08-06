package com.shinoow.abyssalcraft.platform;

import java.io.IOException;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.shinoow.abyssalcraft.client.render.effect.LineEffectRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.phys.Vec3;

//? if forge {
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.IEventBus;
//?} else {
/*import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
*///?}

/** Loader and vertex-API bridge for the custom world-space line shader. */
public final class LineRenderCompat extends RenderType {

    private static ShaderInstance shader;
    private static final RenderType LINE = RenderType.create(
        "abyssalcraft_line",
        DefaultVertexFormat.POSITION_TEX_COLOR,
        VertexFormat.Mode.QUADS,
        4096,
        false,
        true,
        CompositeState.builder()
            .setShaderState(new ShaderStateShard(() -> shader))
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setCullState(CULL)
            .setWriteMaskState(COLOR_WRITE)
            .createCompositeState(false));

    private LineRenderCompat() {
        super("abyssalcraft_line_compat", DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS,
            0, false, false, () -> {}, () -> {});
    }

    public static void attach(IEventBus modBus) {
        modBus.addListener((RegisterShadersEvent event) -> {
            try {
                event.registerShader(new ShaderInstance(event.getResourceProvider(),
                    ACRef.id("rendertype_line"), DefaultVertexFormat.POSITION_TEX_COLOR),
                    instance -> shader = instance);
            } catch (IOException exception) {
                throw new IllegalStateException("Unable to load AbyssalCraft line shader", exception);
            }
        });
        EventBuses.game().addListener((RenderLevelStageEvent event) -> {
            if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
                LineEffectRenderer.render(event.getPoseStack(), partialTick());
            }
        });
    }

    public static boolean ready() {
        return shader != null;
    }

    public static RenderType lineType() {
        return LINE;
    }

    public static void endBatch(MultiBufferSource.BufferSource buffers) {
        buffers.endBatch(LINE);
    }

    public static void vertex(VertexConsumer consumer, Matrix4f pose, Vec3 position,
                              int red, int green, int blue, int alpha, float u, float v) {
        //? if forge {
        consumer.vertex(pose, (float) position.x, (float) position.y, (float) position.z)
            .uv(u, v).color(red, green, blue, alpha).endVertex();
        //?} else {
        /*consumer.addVertex(pose, (float) position.x, (float) position.y, (float) position.z)
            .setColor(red, green, blue, alpha).setUv(u, v);
        *///?}
    }

    private static float partialTick() {
        //? if forge {
        return Minecraft.getInstance().getFrameTime();
        //?} else {
        /*return Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        *///?}
    }
}