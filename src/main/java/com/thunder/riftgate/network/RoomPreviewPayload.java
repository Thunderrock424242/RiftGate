package com.thunder.riftgate.network;

import com.thunder.riftgate.MainModClass.RiftGate;
import com.thunder.riftgate.client.render.RoomPreviewCache;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record RoomPreviewPayload(UUID playerId, List<BlockEntry> blocks) implements CustomPacketPayload {
    public static final Type<RoomPreviewPayload> TYPE = new Type<>(RiftGate.resource("room_preview"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RoomPreviewPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public RoomPreviewPayload decode(RegistryFriendlyByteBuf buffer) {
            UUID playerId = buffer.readUUID();
            int size = buffer.readVarInt();
            List<BlockEntry> blocks = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                byte dx = buffer.readByte();
                byte dy = buffer.readByte();
                byte dz = buffer.readByte();
                int stateId = buffer.readVarInt();
                blocks.add(new BlockEntry(dx, dy, dz, stateId));
            }
            return new RoomPreviewPayload(playerId, blocks);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, RoomPreviewPayload payload) {
            buffer.writeUUID(payload.playerId);
            buffer.writeVarInt(payload.blocks.size());
            for (BlockEntry block : payload.blocks) {
                buffer.writeByte(block.dx);
                buffer.writeByte(block.dy);
                buffer.writeByte(block.dz);
                buffer.writeVarInt(block.stateId);
            }
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RoomPreviewPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            List<RoomPreviewCache.BlockInstance> instances = new ArrayList<>(payload.blocks.size());
            for (BlockEntry entry : payload.blocks) {
                BlockState state = Block.BLOCK_STATE_REGISTRY.byId(entry.stateId);
                if (state == null || state.isAir()) {
                    continue;
                }
                instances.add(new RoomPreviewCache.BlockInstance(entry.dx, entry.dy, entry.dz, state));
            }
            RoomPreviewCache.update(payload.playerId, instances);
        });
    }

    public static void send(ServerPlayer player, List<BlockEntry> blocks) {
        PacketDistributor.sendToPlayer(player, new RoomPreviewPayload(player.getUUID(), blocks));
    }

    public record BlockEntry(byte dx, byte dy, byte dz, int stateId) {}
}
