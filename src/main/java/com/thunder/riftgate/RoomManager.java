package com.thunder.riftgate;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.UUID;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;


public class RoomManager {
    private static final HashMap<UUID, BlockPos> playerRooms = new HashMap<>();
    private static final HashMap<BlockPos, UUID> linkedDoors = new HashMap<>();

    private static final ResourceKey<Level> POCKET_DIM = ResourceKey.create(
            net.minecraft.core.registries.Registries.DIMENSION,
            new net.minecraft.resources.ResourceLocation("riftgate", "pocket")
    );

    public static BlockPos getInteriorRoom(UUID playerId, MinecraftServer server) {
        return playerRooms.computeIfAbsent(playerId, id -> {
            int baseX = (id.hashCode() & 0xFFFFF) % 16000;
            int baseZ = ((id.hashCode() >> 1) & 0xFFFFF) % 16000;
            BlockPos origin = new BlockPos(baseX, 100, baseZ);

            ServerLevel level = server.getLevel(POCKET_DIM);
            if (level != null) {
                generateBarrierBox(level, origin, 128); // 256x256 area
            }

            return origin;
        });
    }

    public static void linkDoor(UUID playerId, BlockPos doorPos) {
        linkedDoors.put(doorPos, playerId);
    }

    public static boolean isLinkedDoor(BlockPos pos) {
        return linkedDoors.containsKey(pos);
    }

    public static void enterRoom(ServerPlayer player, BlockPos fromDoor) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        ServerLevel pocket = server.getLevel(POCKET_DIM);
        if (pocket == null) return;

        BlockPos targetPos = getInteriorRoom(player.getUUID(), server);
        player.teleportTo(pocket, targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5,
                player.getYRot(), player.getXRot());
    }

    private static void generateBarrierBox(ServerLevel level, BlockPos center, int halfSize) {
        int minX = center.getX() - halfSize;
        int maxX = center.getX() + halfSize;
        int minZ = center.getZ() - halfSize;
        int maxZ = center.getZ() + halfSize;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = 0; y <= 256; y++) {
                    boolean isEdgeX = (x == minX || x == maxX);
                    boolean isEdgeZ = (z == minZ || z == maxZ);
                    if (isEdgeX || isEdgeZ) {
                        BlockPos pos = new BlockPos(x, y, z);
                        if (level.getBlockState(pos).isAir()) {
                            level.setBlockAndUpdate(pos, Blocks.BARRIER.defaultBlockState());
                        }
                    }
                }
            }
        }
    }
}