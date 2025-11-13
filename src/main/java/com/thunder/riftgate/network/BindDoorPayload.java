package com.thunder.riftgate.network;

import com.thunder.riftgate.MainModClass.RiftGate;
import com.thunder.riftgate.items.ModItems;
import com.thunder.riftgate.teleport.RoomManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record BindDoorPayload(BlockPos doorPos) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<BindDoorPayload> TYPE =
            new CustomPacketPayload.Type<>(RiftGate.resource("bind_door"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BindDoorPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, BindDoorPayload::doorPos,
                    BindDoorPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(BindDoorPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            BlockPos pos = payload.doorPos();
            BlockState state = player.level().getBlockState(pos);
            if (!(state.getBlock() instanceof DoorBlock)) {
                return;
            }

            BlockPos doorBase = state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;

            if (RoomManager.isLinkedDoor(doorBase)) {
                player.sendSystemMessage(Component.literal("This door is already attuned."));
                return;
            }

            if (!ModItems.isKeyItem(player.getMainHandItem()) && !ModItems.isKeyItem(player.getOffhandItem())) {
                return;
            }

            RoomManager.linkDoor(player.getUUID(), doorBase, player.level());
            RoomManager.getInteriorRoom(player.getUUID(), player.serverLevel().getServer());
            RoomManager.syncRoomPreview(player);
            player.sendSystemMessage(Component.literal("This door is now linked to your Rift Room."));
        });
    }
}
