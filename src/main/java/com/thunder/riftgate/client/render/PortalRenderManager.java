package com.thunder.riftgate.client.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.thunder.riftgate.teleport.RoomManager;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.UUID;

public class PortalRenderManager {
    public static final int WIDTH = 128;
    public static final int HEIGHT = 128;

    private static final HashMap<UUID, RenderTarget> playerTargets = new HashMap<>();

    public static RenderTarget getOrCreateFramebuffer(UUID playerId) {
        return playerTargets.computeIfAbsent(playerId, id -> {
            RenderTarget fb = new RenderTarget(true);
            fb.resize(WIDTH, HEIGHT, Minecraft.getInstance().getMainRenderTarget().isStencilEnabled());
            return fb;
        });
    }

    public static void renderPortalPreview(UUID playerId, BlockPos doorPos) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        if (mc.level.dimension().location().toString().equals("riftgate:pocket")) return;

        RenderTarget portalFB = getOrCreateFramebuffer(playerId);
        portalFB.bindWrite(true);

        // Get the linked position
        BlockPos targetPos = RoomManager.getInteriorRoom(playerId, mc.getSingleplayerServer());

        Camera camera = new Camera();
        camera.setup(mc.level, mc.player, false, false, 0F);
        camera.setPosition(targetPos.getX() + 0.5, targetPos.getY() + 1.62, targetPos.getZ() + 0.5);

        GameRenderer gameRenderer = mc.gameRenderer;
        LevelRenderer levelRenderer = mc.levelRenderer;

        levelRenderer.renderLevel(
                new net.minecraft.client.renderer.RenderTypeBuffers.Impl(mc.renderBuffers().bufferSource()),
                camera,
                0F,
                1L,
                false,
                camera.getPosition()
        );

        mc.getMainRenderTarget().bindWrite(true);
    }
}