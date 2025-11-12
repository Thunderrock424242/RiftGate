package com.thunder.riftgate.client.render;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import com.thunder.riftgate.MainModClass.RiftGate;
import com.thunder.riftgate.config.ModConfig;
import com.thunder.riftgate.dimension.ModDimensions;
import com.thunder.riftgate.teleport.RoomManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import java.util.UUID;

@EventBusSubscriber(modid = RiftGate.MOD_ID, value = Dist.CLIENT)
public final class DoorPortalRenderer {
    private static final ResourceLocation FALLBACK_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/block/nether_portal.png");

    private DoorPortalRenderer() {
    }

    @SubscribeEvent
    public static void renderDoors(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }

        if (mc.level.dimension().equals(ModDimensions.INTERIOR_DIM_KEY)) {
            return;
        }

        UUID playerId = mc.player.getUUID();
        BlockPos doorPos = RoomManager.getLinkedDoorForPlayer(playerId);
        if (doorPos == null || !mc.level.isLoaded(doorPos)) {
            return;
        }

        BlockState state = mc.level.getBlockState(doorPos);
        if (!(state.getBlock() instanceof DoorBlock) || state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
            return;
        }

        if (!state.getValue(DoorBlock.OPEN) || RoomManager.isDoorLocked(doorPos) || !RoomManager.isLinkedDoor(doorPos)) {
            return;
        }

        Direction facing = state.getValue(DoorBlock.FACING);
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        var cameraPos = event.getCamera().getPosition();
        poseStack.translate(
                doorPos.getX() - cameraPos.x,
                doorPos.getY() - cameraPos.y,
                doorPos.getZ() - cameraPos.z
        );
        poseStack.translate(0.5F, 0, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        poseStack.translate(-0.25F, 0, 0.501F);
        poseStack.scale(0.5F, 1.0F, 1.0F);

        boolean liveView = false;
        if (ModConfig.CLIENT.portalRenderMode.get() == ModConfig.PortalRenderMode.SEE_THROUGH) {
            liveView = PortalRenderManager.renderPortalPreview(playerId, doorPos, facing);
        }

        if (liveView) {
            TextureTarget fb = PortalRenderManager.getOrCreateFramebuffer(playerId);
            RenderSystem.setShaderTexture(0, fb.getColorTextureId());
        } else {
            RenderSystem.setShaderTexture(0, FALLBACK_TEXTURE);
        }

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();
        RenderSystem.disableCull();

        drawQuad(poseStack);
        poseStack.translate(0, 0, -0.0025F);
        drawQuad(poseStack);

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    private static void drawQuad(PoseStack poseStack) {
        Matrix4f matrix = poseStack.last().pose();
        ByteBufferBuilder byteBuffer = new ByteBufferBuilder(256);
        BufferBuilder builder = new BufferBuilder(byteBuffer, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.addVertex(matrix, 0, 2, 0).setUv(0, 0);
        builder.addVertex(matrix, 1, 2, 0).setUv(1, 0);
        builder.addVertex(matrix, 1, 0, 0).setUv(1, 1);
        builder.addVertex(matrix, 0, 0, 0).setUv(0, 1);
        BufferUploader.drawWithShader(builder.build());
    }
}
