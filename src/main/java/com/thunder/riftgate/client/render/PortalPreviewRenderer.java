package com.thunder.riftgate.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.thunder.riftgate.teleport.RoomManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;

public class PortalPreviewRenderer implements BlockEntityRenderer<BlockEntity> {

    public PortalPreviewRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(BlockEntity blockEntity, float partialTicks, PoseStack poseStack,
                       net.minecraft.client.renderer.MultiBufferSource buffer, int combinedLight, int combinedOverlay) {

        BlockState state = blockEntity.getBlockState();

        if (!(state.getBlock() instanceof DoorBlock)) return;

        boolean isOpen = state.getValue(DoorBlock.OPEN);
        if (!isOpen || !RoomManager.isLinkedDoor(blockEntity.getBlockPos())) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        var playerId = mc.player.getUUID();
        PortalRenderManager.renderPortalPreview(playerId, blockEntity.getBlockPos());
        var fb = PortalRenderManager.getOrCreateFramebuffer(playerId);

        poseStack.pushPose();
        poseStack.translate(0.25, 0.5, 0.501); // door frame inset
        poseStack.scale(0.5F, 1.0F, 1.0F);

        RenderSystem.setShaderTexture(0, fb.getColorTextureId());
        Matrix4f matrix = poseStack.last().pose();

        Tesselator tess = Tesselator.getInstance();
        BufferBuilder bb = tess.getBuilder();
        bb.begin(VertexFormat.Mode.QUADS, RenderType.TEXTURE.get().format());

        bb.vertex(matrix, 0, 1, 0).uv(0, 1).endVertex();
        bb.vertex(matrix, 1, 1, 0).uv(1, 1).endVertex();
        bb.vertex(matrix, 1, 0, 0).uv(1, 0).endVertex();
        bb.vertex(matrix, 0, 0, 0).uv(0, 0).endVertex();

        tess.end();
        poseStack.popPose();
    }
}