package com.thunder.riftgate.client.render;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import com.thunder.riftgate.dimension.ModDimensions;
import com.thunder.riftgate.teleport.RoomManager;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
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

    public static void renderPortalPreview(UUID playerId, BlockPos doorPos) {
        Minecraft mc = Minecraft.getInstance();
        MinecraftServer server = mc.getSingleplayerServer();
        if (mc.level == null || mc.player == null || server == null) return;

        if (mc.level.dimension().equals(ModDimensions.INTERIOR_DIM_KEY)) return;

        ServerLevel interiorLevel = server.getLevel(ModDimensions.INTERIOR_DIM_KEY);
        if (interiorLevel == null) return;

        BlockPos targetPos = RoomManager.getInteriorRoom(playerId, server);

        TextureTarget fb = getOrCreateFramebuffer(playerId);
        fb.bindWrite(true);
        fb.clear(Minecraft.ON_OSX);

        Camera camera = new Camera();
        camera.setup(interiorLevel, mc.player, false, false, 0F);

        try {
            Method setPos = Camera.class.getDeclaredMethod("setPosition", double.class, double.class, double.class);
            setPos.setAccessible(true);
            setPos.invoke(camera, targetPos.getX() + 0.5, targetPos.getY() + 1.6, targetPos.getZ() + 0.5);
        } catch (Exception e) {
            e.printStackTrace();
        }

        PoseStack poseStack = new PoseStack();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();

        AABB renderArea = new AABB(targetPos).inflate(16); // 16 block radius
        List<Entity> entities = interiorLevel.getEntities(null, renderArea);

        for (Entity entity : entities) {
            if (entity != null && entity.isAlive()) {
                double dx = entity.getX() - targetPos.getX();
                double dy = entity.getY() - targetPos.getY();
                double dz = entity.getZ() - targetPos.getZ();
                dispatcher.render(entity, dx, dy, dz, 0F, 1F, poseStack, buffer, 15728880);
            }
        }

        buffer.endBatch();
        mc.getMainRenderTarget().bindWrite(true);
    }
}