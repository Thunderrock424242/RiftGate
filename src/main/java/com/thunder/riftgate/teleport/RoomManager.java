package com.thunder.riftgate.teleport;

import net.minecraft.core.BlockPos;
import com.thunder.riftgate.dimension.ModDimensions;
import com.thunder.riftgate.teleport.RoomGenerator;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.HashMap;
import java.util.UUID;
import java.util.EnumSet;

public class RoomManager {
    private static final HashMap<UUID, BlockPos> playerRooms = new HashMap<>();
    private static final HashMap<BlockPos, UUID> linkedDoors = new HashMap<>();
    private static final ResourceKey<Level> DIMENSION = ModDimensions.INTERIOR_DIM_KEY;

    public static BlockPos getInteriorRoom(UUID playerId, MinecraftServer server) {
        return playerRooms.computeIfAbsent(playerId, id -> {
            int baseX = (id.hashCode() & 0xFFFFF) % 16000;
            int baseZ = ((id.hashCode() >> 1) & 0xFFFFF) % 16000;
            BlockPos doorPos = new BlockPos(baseX, 100, baseZ);

            ServerLevel level = server.getLevel(DIMENSION);
            if (level != null) {
                generateBarrierBox(level, doorPos);
                RoomGenerator.generateRoom(level, doorPos);
            }

            return doorPos;
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

        ServerLevel interior = server.getLevel(DIMENSION);
        if (interior == null) return;

        BlockPos targetPos = getInteriorRoom(player.getUUID(), server);
        player.teleportTo(interior, targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5,
                EnumSet.noneOf(RelativeMovement.class), player.getYRot(), player.getXRot());
    }

    private static void generateBarrierBox(ServerLevel level, BlockPos center) {
        int minX = center.getX() - 128;
        int maxX = center.getX() + 128;
        int minZ = center.getZ() - 128;
        int maxZ = center.getZ() + 128;

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

    public static void teleportEntity(Entity entity, BlockPos fromDoor) {
        MinecraftServer server = entity.level().getServer();
        if (server == null) return;

        ServerLevel interior = server.getLevel(DIMENSION);
        if (interior == null) return;

        UUID ownerId = (entity instanceof ServerPlayer player)
                ? player.getUUID()
                : linkedDoors.getOrDefault(fromDoor, null);

        if (ownerId == null) return;

        BlockPos targetPos = getInteriorRoom(ownerId, server);
        entity.teleportTo(
                interior,
                targetPos.getX() + 0.5,
                targetPos.getY(),
                targetPos.getZ() + 0.5,
                EnumSet.noneOf(RelativeMovement.class),
                entity.getYRot(),
                entity.getXRot()
        );
    }
    public static boolean roomExists(UUID playerId) {
        return playerRooms.containsKey(playerId);
    }

    public static BlockPos getOrCreateRoomOrigin(UUID playerId) {
        return playerRooms.computeIfAbsent(playerId, id -> {
            int baseX = (id.hashCode() & 0xFFFFF) % 16000;
            int baseZ = ((id.hashCode() >> 1) & 0xFFFFF) % 16000;
            return new BlockPos(baseX, 100, baseZ);
        });
    }
}