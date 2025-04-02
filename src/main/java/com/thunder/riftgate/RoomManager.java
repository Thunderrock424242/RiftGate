package com.thunder.riftgate;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.UUID;

public class RoomManager {
    private static final HashMap<UUID, BlockPos> playerRooms = new HashMap<>();
    private static final HashMap<BlockPos, UUID> linkedDoors = new HashMap<>();

    public static BlockPos getInteriorRoom(UUID playerId) {
        return playerRooms.computeIfAbsent(playerId, id -> new BlockPos(0, 100, 0));
    }

    public static void linkDoor(UUID playerId, BlockPos doorPos) {
        BlockPos room = getInteriorRoom(playerId);
        linkedDoors.put(doorPos, playerId);
    }

    public static boolean isLinkedDoor(BlockPos pos) {
        return linkedDoors.containsKey(pos);
    }

    public static void enterRoom(ServerPlayer player, BlockPos fromDoor) {
        ServerLevel currentLevel = player.serverLevel();
        BlockPos targetPos = getInteriorRoom(player.getUUID());
        player.teleportTo(currentLevel, targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5,
                player.getYRot(), player.getXRot());
    }
}