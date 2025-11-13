package com.thunder.riftgate.client.render;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.thunder.riftgate.client.render.RoomPreviewCache;
import com.thunder.riftgate.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import com.mojang.blaze3d.vertex.VertexSorting;

import java.util.HashMap;
import java.util.UUID;

public class PortalRenderManager {
    public static final int WIDTH = 128;
    public static final int HEIGHT = 128;

    private static final HashMap<UUID, TextureTarget> playerTargets = new HashMap<>();

    public static TextureTarget getOrCreateFramebuffer(UUID playerId) {
        return playerTargets.computeIfAbsent(playerId, id -> {
            TextureTarget fb = new TextureTarget(WIDTH, HEIGHT, true, true);
            fb.setClearColor(0f, 0f, 0f, 1f);
            return fb;
        });
    }

    public static boolean renderPortalPreview(UUID playerId, BlockPos doorPos, Direction facing) {
        if (ModConfig.CLIENT.portalRenderMode.get() != ModConfig.PortalRenderMode.SEE_THROUGH) {
            return false;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return false;
        }

        RoomPreviewCache.RoomPreviewSnapshot snapshot = RoomPreviewCache.get(playerId);
        if (snapshot == null || snapshot.blocks().isEmpty()) {
            return false;
        }

        TextureTarget fb = getOrCreateFramebuffer(playerId);
        fb.bindWrite(true);
        fb.clear(Minecraft.ON_OSX);

        Direction viewDirection = facing == null ? Direction.SOUTH : facing.getOpposite();
        Vec3 forward = Vec3.atLowerCornerOf(viewDirection.getNormal()).scale(0.45);
        Vec3 cameraPos = new Vec3(
                doorPos.getX() + 0.5 - forward.x,
                doorPos.getY() + 1.4,
                doorPos.getZ() + 0.5 - forward.z
        );

        float yaw = viewDirection.toYRot();

        setupMatrices(cameraPos, yaw);
        renderSnapshot(mc, doorPos, snapshot);
        restoreMatrices(mc);

        return true;
    }

    private static void setupMatrices(Vec3 cameraPos, float yaw) {
        RenderSystem.viewport(0, 0, WIDTH, HEIGHT);
        RenderSystem.enableDepthTest();
        RenderSystem.backupProjectionMatrix();
        Matrix4f projection = new Matrix4f().setPerspective((float)Math.toRadians(60.0F), (float) WIDTH / HEIGHT, 0.05F, 50.0F);
        RenderSystem.setProjectionMatrix(projection, VertexSorting.DISTANCE_TO_ORIGIN);

        Matrix4fStack mvStack = RenderSystem.getModelViewStack();
        mvStack.pushMatrix();
        mvStack.identity();
        mvStack.rotateY((float)Math.toRadians(180F - yaw));
        mvStack.translate((float)-cameraPos.x, (float)-cameraPos.y, (float)-cameraPos.z);
        RenderSystem.applyModelViewMatrix();
    }

    private static void restoreMatrices(Minecraft mc) {
        Matrix4fStack mvStack = RenderSystem.getModelViewStack();
        mvStack.popMatrix();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.restoreProjectionMatrix();
        RenderSystem.disableDepthTest();
        mc.getMainRenderTarget().bindWrite(true);
        RenderSystem.viewport(0, 0, mc.getWindow().getWidth(), mc.getWindow().getHeight());
    }

    private static void renderSnapshot(Minecraft mc, BlockPos doorPos, RoomPreviewCache.RoomPreviewSnapshot snapshot) {
        BlockRenderDispatcher dispatcher = mc.getBlockRenderer();
        ByteBufferBuilder builder = new ByteBufferBuilder(262144);
        MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(builder);
        PoseStack poseStack = new PoseStack();

        for (RoomPreviewCache.BlockInstance block : snapshot.blocks()) {
            BlockState state = block.state();
            if (state == null || state.isAir()) {
                continue;
            }
            poseStack.pushPose();
            poseStack.translate(
                    doorPos.getX() + block.dx(),
                    doorPos.getY() + block.dy(),
                    doorPos.getZ() + block.dz()
            );
            dispatcher.renderSingleBlock(state, poseStack, buffer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }

        buffer.endBatch();
    }
}
