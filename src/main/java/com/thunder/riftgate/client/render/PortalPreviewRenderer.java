package com.thunder.riftgate.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.thunder.riftgate.teleport.RoomManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class PortalPreviewRenderer implements BlockEntityRenderer<BlockEntity> {

    public PortalPreviewRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(BlockEntity blockEntity, float partialTicks, @NotNull PoseStack poseStack,
                       net.minecraft.client.renderer.@NotNull MultiBufferSource buffer, int combinedLight, int combinedOverlay) {

        BlockState state = blockEntity.getBlockState();
        if (!(state.getBlock() instanceof DoorBlock)) return;

        boolean isOpen = state.getValue(DoorBlock.OPEN);
        if (!isOpen || !RoomManager.isLinkedDoor(blockEntity.getBlockPos())) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        var playerId = mc.player.getUUID();
        PortalRenderManager.renderPortalPreview(playerId, blockEntity.getBlockPos());
        var fb = PortalRenderManager.getOrCreateFramebuffer(playerId);
        int textureId = fb.getColorTextureId();

        poseStack.pushPose();
        poseStack.translate(0.25, 0.5, 0.501); // Inside door frame
        poseStack.scale(0.5F, 1.0F, 1.0F);
        Matrix4f matrix = poseStack.last().pose();

        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, textureId);

        ByteBufferBuilder byteBuffer = new ByteBufferBuilder(256);
        BufferBuilder bb = new BufferBuilder(byteBuffer, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        bb.addVertex(matrix, 0, 1, 0).setUv(0, 1);
        bb.addVertex(matrix, 1, 1, 0).setUv(1, 1);
        bb.addVertex(matrix, 1, 0, 0).setUv(1, 0);
        bb.addVertex(matrix, 0, 0, 0).setUv(0, 0);

        var fallback = ResourceLocation.withDefaultNamespace("textures/misc/white.png");
        mc.renderBuffers().bufferSource().endBatch(RenderType.text(fallback));

        poseStack.popPose();
    }
}