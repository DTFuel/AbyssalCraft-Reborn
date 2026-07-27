package com.shinoow.abyssalcraft.platform;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

import org.joml.Matrix4f;

//? if forge {
import com.mojang.blaze3d.vertex.PoseStack;
//?}

/**
 * Compat: custom dimension skybox render (loader + version axis). <b>Client-only</b>.
 *
 * <p>Both loaders expose a {@code renderSky} extension on {@link DimensionSpecialEffects}, but the
 * signature differs: Forge's {@code IForgeDimensionSpecialEffects.renderSky} takes a {@code PoseStack}
 * (the camera-rotated model-view) as the fourth argument, while NeoForge's
 * {@code IDimensionSpecialEffectsExtension.renderSky} takes the frustum {@code Matrix4f} directly. This
 * base extracts the model-view matrix and hands business subclasses a fork-free
 * {@link #skyTexture()} / {@link #skyRed()}-{@link #skyBlue()} contract. The immediate-mode cube draw
 * also forks: 1.20.1 uses {@code Tesselator.getBuilder()} + {@code vertex().uv().endVertex()} +
 * {@code BufferUploader.drawWithShader(buffer.end())}; 1.21 uses {@code Tesselator.begin(...)} +
 * {@code addVertex().setUv()} + {@code buffer.buildOrThrow()}. Faithful to the 1.12.2
 * {@code ACSkyRenderer}: a tinted 6-face cube at &plusmn;100 with a 16&times;16 UV repeat drawn over the
 * void sky ({@code SkyType.NONE}), replacing vanilla's celestial rendering when {@link #skyTexture()} is
 * non-null (returning {@code true} from {@code renderSky}).
 *
 * <p>Never referenced on a dedicated server: the client sky package is only registered inside
 * {@link SideExecutor#runWhenClient} via {@link DimensionEffectsCompat}.
 */
public abstract class DimensionSkyCompat extends DimensionSpecialEffects {

    protected DimensionSkyCompat(float cloudLevel, boolean hasGround, SkyType skyType,
                                 boolean forceBrightLightmap, boolean constantAmbientLight) {
        super(cloudLevel, hasGround, skyType, forceBrightLightmap, constantAmbientLight);
    }

    /** The skybox cube texture, or {@code null} to leave vanilla's void sky ({@code SkyType}) untouched. */
    protected abstract ResourceLocation skyTexture();

    /** Per-channel skybox tint (0-255), read each frame so a {@code clientvars.json} reload retints live. */
    protected abstract int skyRed();

    protected abstract int skyGreen();

    protected abstract int skyBlue();

    /** Live skybox brightness multiplier used by dimension-specific render policy. */
    protected float skyBrightness() {
        return 1.0F;
    }

    //? if forge {
    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, PoseStack poseStack,
                             Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
        return renderCustomSky(poseStack.last().pose(), setupFog);
    }
    //?} else {
    /*@Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, Matrix4f frustumMatrix,
                             Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
        return renderCustomSky(frustumMatrix, setupFog);
    }
    *///?}

    private boolean renderCustomSky(Matrix4f frustumMatrix, Runnable setupFog) {
        ResourceLocation texture = skyTexture();
        if (texture == null) {
            return false;
        }
        setupFog.run();
        float brightness = Math.max(0.0F, Math.min(1.0F, skyBrightness()));
        drawSkyBox(frustumMatrix, texture, skyRed() / 255.0F * brightness,
            skyGreen() / 255.0F * brightness, skyBlue() / 255.0F * brightness);
        return true;
    }

    private static void drawSkyBox(Matrix4f frustumMatrix, ResourceLocation texture, float red, float green,
                                   float blue) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(red, green, blue, 1.0F);
        for (int face = 0; face < 6; face++) {
            Matrix4f matrix = new Matrix4f(frustumMatrix);
            switch (face) {
                case 1 -> matrix.rotate(Axis.XP.rotationDegrees(90.0F));
                case 2 -> matrix.rotate(Axis.XP.rotationDegrees(-90.0F));
                case 3 -> matrix.rotate(Axis.XP.rotationDegrees(180.0F));
                case 4 -> matrix.rotate(Axis.ZP.rotationDegrees(90.0F));
                case 5 -> matrix.rotate(Axis.ZP.rotationDegrees(-90.0F));
                default -> { /* face 0: no rotation */ }
            }
            emitFace(matrix);
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    private static void emitFace(Matrix4f matrix) {
        //? if >=1.21 {
        /*BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.addVertex(matrix, -100.0F, -100.0F, -100.0F).setUv(0.0F, 0.0F);
        buffer.addVertex(matrix, -100.0F, -100.0F, 100.0F).setUv(0.0F, 16.0F);
        buffer.addVertex(matrix, 100.0F, -100.0F, 100.0F).setUv(16.0F, 16.0F);
        buffer.addVertex(matrix, 100.0F, -100.0F, -100.0F).setUv(16.0F, 0.0F);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        *///?} else {
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(matrix, -100.0F, -100.0F, -100.0F).uv(0.0F, 0.0F).endVertex();
        buffer.vertex(matrix, -100.0F, -100.0F, 100.0F).uv(0.0F, 16.0F).endVertex();
        buffer.vertex(matrix, 100.0F, -100.0F, 100.0F).uv(16.0F, 16.0F).endVertex();
        buffer.vertex(matrix, 100.0F, -100.0F, -100.0F).uv(16.0F, 0.0F).endVertex();
        BufferUploader.drawWithShader(buffer.end());
        //?}
    }
}
