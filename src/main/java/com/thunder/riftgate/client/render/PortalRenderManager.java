package com.thunder.riftgate.client.render;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import com.thunder.riftgate.config.ModConfig;
import com.thunder.riftgate.dimension.ModDimensions;
import com.thunder.riftgate.teleport.RoomManager;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PortalRenderManager {
    public static final int WIDTH = 128;
    public static final int HEIGHT = 128;

    private static final HashMap<UUID, TextureTarget> playerTargets = new HashMap<>();
    private static final Method SET_POSITION;
    private static final Method SET_ROTATION;

    static {
        try {
            SET_POSITION = Camera.class.getDeclaredMethod("setPosition", double.class, double.class, double.class);
            SET_POSITION.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Unable to resolve Camera#setPosition", e);
        }

        Method rotation;
        try {
            rotation = Camera.class.getDeclaredMethod("setRotation", float.class, float.class);
            rotation.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            rotation = null;
        }
        SET_ROTATION = rotation;
    }

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
        MinecraftServer server = mc.getSingleplayerServer();
        if (mc.level == null || mc.player == null || server == null) return false;

        if (mc.level.dimension().equals(ModDimensions.INTERIOR_DIM_KEY)) return false;

        ServerLevel interiorLevel = server.getLevel(ModDimensions.INTERIOR_DIM_KEY);
        if (interiorLevel == null) return false;

        BlockPos targetPos = RoomManager.getInteriorRoom(playerId, server);

        TextureTarget fb = getOrCreateFramebuffer(playerId);
        fb.bindWrite(true);
        fb.clear(Minecraft.ON_OSX);

        Camera camera = new Camera();
        camera.setup(interiorLevel, mc.player, false, false, 0F);

        Direction viewDirection = facing == null ? Direction.SOUTH : facing;
        Vec3 forward = Vec3.atLowerCornerOf(viewDirection.getNormal()).scale(0.45);

        try {
            SET_POSITION.invoke(camera,
                    targetPos.getX() + 0.5 - forward.x,
                    targetPos.getY() + 1.5,
                    targetPos.getZ() + 0.5 - forward.z);
            if (SET_ROTATION != null) {
                SET_ROTATION.invoke(camera, viewDirection.toYRot(), 0F);
            }
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }

        PoseStack poseStack = new PoseStack();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();

        AABB renderArea = new AABB(targetPos).inflate(16);
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
        return true;
    }
}