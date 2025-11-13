package com.thunder.riftgate.teleport;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import com.thunder.riftgate.blocks.ModBlocks;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public class RoomGenerator {
    /**
     * Generates a minimal interior consisting of a freestanding door on a small
     * platform. The provided {@code doorPos} represents the inside bottom half
     * of the doorway that players spawn at.
     */
    public static void generateRoom(ServerLevel level, BlockPos doorPos) {
        // Build a simple 5x5 smooth-stone platform under the doorway so players
        // always have solid ground but keep the area otherwise open.
        BlockPos floorBase = doorPos.below();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                BlockPos floorPos = floorBase.offset(dx, 0, dz);
                level.setBlockAndUpdate(floorPos, Blocks.SMOOTH_STONE.defaultBlockState());

                // Clear headroom above the platform to remove any previously
                // generated structure blocks.
                for (int dy = 1; dy <= 4; dy++) {
                    BlockPos airPos = floorPos.above(dy);
                    if (!airPos.equals(doorPos) && !airPos.equals(doorPos.above())) {
                        level.setBlockAndUpdate(airPos, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }

        // Ensure the doorway itself is free of stray blocks before placing the
        // Rift door.
        level.setBlockAndUpdate(doorPos, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(doorPos.above(), Blocks.AIR.defaultBlockState());

        level.setBlockAndUpdate(doorPos,
                ModBlocks.RIFT_DOOR.get().defaultBlockState()
                        .setValue(DoorBlock.FACING, net.minecraft.core.Direction.SOUTH)
                        .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
        level.setBlockAndUpdate(doorPos.above(),
                ModBlocks.RIFT_DOOR.get().defaultBlockState()
                        .setValue(DoorBlock.FACING, net.minecraft.core.Direction.SOUTH)
                        .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
    }
}