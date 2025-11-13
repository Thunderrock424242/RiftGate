package com.thunder.riftgate.teleport;

import net.minecraft.core.BlockPos;
import com.thunder.riftgate.dimension.ModDimensions;
import com.thunder.riftgate.network.RoomPreviewPayload;
import com.thunder.riftgate.teleport.RoomGenerator;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class RoomManager {
    private static final HashMap<UUID, BlockPos> playerRooms = new HashMap<>();
    private static final Map<BlockPos, UUID> linkedDoors = new HashMap<>();
    private static final Map<UUID, BlockPos> playerDoors = new HashMap<>();
    private static final Map<BlockPos, UUID> roomDoors = new HashMap<>();
    private static final Set<BlockPos> lockedDoors = new HashSet<>();
    private static final Set<UUID> generatedRoomShells = new HashSet<>();
    private static final ResourceKey<Level> DIMENSION = ModDimensions.INTERIOR_DIM_KEY;

    public static BlockPos getInteriorRoom(UUID playerId, MinecraftServer server) {
        return playerRooms.computeIfAbsent(playerId, id -> {
            int baseX = (id.hashCode() & 0xFFFFF) % 16000;
            int baseZ = ((id.hashCode() >> 1) & 0xFFFFF) % 16000;
            BlockPos doorPos = new BlockPos(baseX, 100, baseZ);

            ServerLevel level = server.getLevel(DIMENSION);
            if (level != null) {
                if (generatedRoomShells.add(id)) {
                    generateBarrierBox(level, doorPos);
                }
                RoomGenerator.generateRoom(level, doorPos);
            }

            roomDoors.put(doorPos, id);
            return doorPos;
        });
    }

    public static void linkDoor(UUID playerId, BlockPos doorPos, Level level) {
        BlockPos lower = doorPos;
        BlockPos upper = doorPos.above();
        linkedDoors.put(lower, playerId);
        linkedDoors.put(upper, playerId);
        lockedDoors.remove(lower);
        lockedDoors.remove(upper);
        playerDoors.put(playerId, lower);
        ensureDoorHasFloor(level, lower);
    }

    public static boolean isLinkedDoor(BlockPos pos) {
        return linkedDoors.containsKey(pos) || linkedDoors.containsKey(pos.below());
    }

    public static boolean isRoomDoor(BlockPos pos) {
        return roomDoors.containsKey(pos) || roomDoors.containsKey(pos.below());
    }

    public static UUID getLinkedDoorOwner(BlockPos pos) {
        UUID owner = linkedDoors.get(pos);
        if (owner == null) {
            owner = linkedDoors.get(pos.below());
        }
        return owner;
    }

    public static UUID getRoomDoorOwner(BlockPos pos) {
        UUID owner = roomDoors.get(pos);
        if (owner == null) {
            owner = roomDoors.get(pos.below());
        }
        return owner;
    }

    public static boolean isDoorLocked(BlockPos pos) {
        return lockedDoors.contains(pos) || lockedDoors.contains(pos.below());
    }

    private static void addLockForDoor(BlockPos pos) {
        if (pos == null) return;
        lockedDoors.add(pos);
        lockedDoors.add(pos.above());
    }

    private static void removeLockForDoor(BlockPos pos) {
        if (pos == null) return;
        lockedDoors.remove(pos);
        lockedDoors.remove(pos.above());
        lockedDoors.remove(pos.below());
    }

    private static UUID getDoorOwner(BlockPos pos) {
        UUID owner = getLinkedDoorOwner(pos);
        if (owner == null) {
            owner = getRoomDoorOwner(pos);
        }
        return owner;
    }

    public static void lockDoor(BlockPos pos) {
        UUID owner = getDoorOwner(pos);

        addLockForDoor(pos);

        if (owner != null) {
            addLockForDoor(playerDoors.get(owner));
            addLockForDoor(playerRooms.get(owner));
        }
    }

    public static void unlockDoor(BlockPos pos) {
        UUID owner = getDoorOwner(pos);

        removeLockForDoor(pos);

        if (owner != null) {
            removeLockForDoor(playerDoors.get(owner));
            removeLockForDoor(playerRooms.get(owner));
        }
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
        int minY = Math.max(level.getMinBuildHeight(), 0);
        int maxY = Math.min(level.getMaxBuildHeight() - 1, 256);

        if (minX > maxX || minZ > maxZ || minY > maxY) {
            return;
        }

        int minChunkX = minX >> 4;
        int maxChunkX = maxX >> 4;
        int minChunkZ = minZ >> 4;
        int maxChunkZ = maxZ >> 4;

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                boolean edgeChunkX = chunkX == minChunkX || chunkX == maxChunkX;
                boolean edgeChunkZ = chunkZ == minChunkZ || chunkZ == maxChunkZ;
                if (!edgeChunkX && !edgeChunkZ) {
                    continue;
                }

                ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);

                int chunkMinX = Math.max(minX, chunkPos.getMinBlockX());
                int chunkMaxX = Math.min(maxX, chunkPos.getMaxBlockX());
                int chunkMinZ = Math.max(minZ, chunkPos.getMinBlockZ());
                int chunkMaxZ = Math.min(maxZ, chunkPos.getMaxBlockZ());

                if (chunkMinX > chunkMaxX || chunkMinZ > chunkMaxZ) {
                    continue;
                }

                if (isChunkPerimeterComplete(level, chunkPos, minX, maxX, minZ, maxZ, minY, checkPos)) {
                    continue;
                }

                if (chunkPos.getMinBlockX() <= minX && minX <= chunkPos.getMaxBlockX()) {
                    fillVerticalEdge(level, mutable, minX, chunkMinZ, chunkMaxZ, minY, maxY);
                }
                if (chunkPos.getMinBlockX() <= maxX && maxX <= chunkPos.getMaxBlockX()) {
                    fillVerticalEdge(level, mutable, maxX, chunkMinZ, chunkMaxZ, minY, maxY);
                }

                if (chunkPos.getMinBlockZ() <= minZ && minZ <= chunkPos.getMaxBlockZ()) {
                    fillHorizontalEdge(level, mutable, chunkMinX, chunkMaxX, minZ, minX, maxX, minY, maxY);
                }
                if (chunkPos.getMinBlockZ() <= maxZ && maxZ <= chunkPos.getMaxBlockZ()) {
                    fillHorizontalEdge(level, mutable, chunkMinX, chunkMaxX, maxZ, minX, maxX, minY, maxY);
                }

            }
        }
    }

    private static boolean isChunkPerimeterComplete(ServerLevel level, ChunkPos chunkPos,
                                                    int minX, int maxX, int minZ, int maxZ,
                                                    int minY, BlockPos.MutableBlockPos checkPos) {
        int chunkMinX = Math.max(minX, chunkPos.getMinBlockX());
        int chunkMaxX = Math.min(maxX, chunkPos.getMaxBlockX());
        int chunkMinZ = Math.max(minZ, chunkPos.getMinBlockZ());
        int chunkMaxZ = Math.min(maxZ, chunkPos.getMaxBlockZ());

        if (chunkMinX > chunkMaxX || chunkMinZ > chunkMaxZ) {
            return true;
        }

        if (chunkPos.getMinBlockX() <= minX && minX <= chunkPos.getMaxBlockX()) {
            int x = minX;
            for (int z = chunkMinZ; z <= chunkMaxZ; z++) {
                checkPos.set(x, minY, z);
                if (!level.getBlockState(checkPos).is(Blocks.BARRIER)) {
                    return false;
                }
            }
        }

        if (chunkPos.getMinBlockX() <= maxX && maxX <= chunkPos.getMaxBlockX()) {
            int x = maxX;
            for (int z = chunkMinZ; z <= chunkMaxZ; z++) {
                checkPos.set(x, minY, z);
                if (!level.getBlockState(checkPos).is(Blocks.BARRIER)) {
                    return false;
                }
            }
        }

        if (chunkPos.getMinBlockZ() <= minZ && minZ <= chunkPos.getMaxBlockZ()) {
            int z = minZ;
            for (int x = chunkMinX; x <= chunkMaxX; x++) {
                if (x == minX || x == maxX) continue;
                checkPos.set(x, minY, z);
                if (!level.getBlockState(checkPos).is(Blocks.BARRIER)) {
                    return false;
                }
            }
        }

        if (chunkPos.getMinBlockZ() <= maxZ && maxZ <= chunkPos.getMaxBlockZ()) {
            int z = maxZ;
            for (int x = chunkMinX; x <= chunkMaxX; x++) {
                if (x == minX || x == maxX) continue;
                checkPos.set(x, minY, z);
                if (!level.getBlockState(checkPos).is(Blocks.BARRIER)) {
                    return false;
                }
            }
        }

        return true;
    }

    private static void fillVerticalEdge(ServerLevel level, BlockPos.MutableBlockPos mutable,
                                         int x, int startZ, int endZ, int minY, int maxY) {
        for (int z = startZ; z <= endZ; z++) {
            fillColumn(level, mutable, x, z, minY, maxY);
        }
    }

    private static void fillHorizontalEdge(ServerLevel level, BlockPos.MutableBlockPos mutable,
                                           int startX, int endX, int z,
                                           int minX, int maxX, int minY, int maxY) {
        for (int x = startX; x <= endX; x++) {
            if (x == minX || x == maxX) {
                continue;
            }
            fillColumn(level, mutable, x, z, minY, maxY);
        }
    }

    private static void fillColumn(ServerLevel level, BlockPos.MutableBlockPos mutable,
                                   int x, int z, int minY, int maxY) {
        for (int y = minY; y <= maxY; y++) {
            mutable.set(x, y, z);
            if (level.getBlockState(mutable).isAir()) {
                level.setBlockAndUpdate(mutable, Blocks.BARRIER.defaultBlockState());
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
                : getLinkedDoorOwner(fromDoor);

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

    public static void exitRoom(Entity entity, BlockPos doorPos) {
        MinecraftServer server = entity.level().getServer();
        if (server == null) return;

        UUID ownerId = getRoomDoorOwner(doorPos);
        if (ownerId == null) return;

        BlockPos returnDoor = playerDoors.get(ownerId);
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (returnDoor == null || overworld == null) return;

        BlockState doorState = overworld.getBlockState(returnDoor);
        Direction facing = doorState.getBlock() instanceof DoorBlock
                ? doorState.getValue(DoorBlock.FACING)
                : Direction.NORTH;

        double exitX = returnDoor.getX() + 0.5 + facing.getStepX() * 0.6;
        double exitZ = returnDoor.getZ() + 0.5 + facing.getStepZ() * 0.6;

        double groundY = returnDoor.getY();
        BlockPos below = returnDoor.below();
        if (!overworld.isEmptyBlock(below)) {
            groundY = below.getY() + 1;
        }

        entity.teleportTo(
                overworld,
                exitX,
                groundY,
                exitZ,
                EnumSet.noneOf(RelativeMovement.class),
                entity.getYRot(),
                entity.getXRot()
        );
    }

    public static void syncRoomPreview(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        BlockPos doorPos = playerRooms.get(player.getUUID());
        if (doorPos == null) {
            doorPos = getInteriorRoom(player.getUUID(), server);
        }

        ServerLevel level = server.getLevel(DIMENSION);
        if (doorPos == null || level == null) {
            return;
        }

        List<RoomPreviewPayload.BlockEntry> blocks = collectPreviewBlocks(level, doorPos);
        RoomPreviewPayload.send(player, blocks);
    }

    private static List<RoomPreviewPayload.BlockEntry> collectPreviewBlocks(ServerLevel level, BlockPos doorPos) {
        List<RoomPreviewPayload.BlockEntry> entries = new ArrayList<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int dx = -4; dx <= 4; dx++) {
            for (int dy = -1; dy <= 5; dy++) {
                for (int dz = -1; dz <= 6; dz++) {
                    cursor.set(doorPos.getX() + dx, doorPos.getY() + dy, doorPos.getZ() + dz);
                    BlockState state = level.getBlockState(cursor);
                    if (state.isAir()) {
                        continue;
                    }
                    entries.add(new RoomPreviewPayload.BlockEntry(
                            (byte) dx,
                            (byte) dy,
                            (byte) dz,
                            Block.BLOCK_STATE_REGISTRY.getId(state)
                    ));
                }
            }
        }

        return entries;
    }

    private static void ensureDoorHasFloor(Level level, BlockPos doorPos) {
        if (level == null) {
            return;
        }

        BlockPos below = doorPos.below();
        if (level.isEmptyBlock(below)) {
            level.setBlockAndUpdate(below, Blocks.SMOOTH_STONE.defaultBlockState());
        }
    }
    public static boolean roomExists(UUID playerId) {
        return playerRooms.containsKey(playerId);
    }

    public static BlockPos getLinkedDoorForPlayer(UUID playerId) {
        return playerDoors.get(playerId);
    }

    public static BlockPos getOrCreateRoomOrigin(UUID playerId) {
        return playerRooms.computeIfAbsent(playerId, id -> {
            int baseX = (id.hashCode() & 0xFFFFF) % 16000;
            int baseZ = ((id.hashCode() >> 1) & 0xFFFFF) % 16000;
            return new BlockPos(baseX, 100, baseZ);
        });
    }
}