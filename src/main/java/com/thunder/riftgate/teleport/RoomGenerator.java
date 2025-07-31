package com.thunder.riftgate.teleport;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public class RoomGenerator {
    /**
     * Generates a simple interior room. The provided {@code doorPos} represents
     * the inside bottom half of the doorway that players spawn at.
     */
    public static void generateRoom(ServerLevel level, BlockPos doorPos) {
        int width = 7;
        int height = 5;
        int depth = 7;

        // centre of the room is half the depth away from the door
        BlockPos origin = doorPos.offset(0, 0, depth / 2);

        for (int x = -width / 2; x <= width / 2; x++) {
            for (int y = 0; y <= height; y++) {
                for (int z = -depth / 2; z <= depth / 2; z++) {
                    BlockPos current = origin.offset(x, y, z);

                    // Floor and ceiling
                    if (y == 0 || y == height) {
                        level.setBlockAndUpdate(current, Blocks.SMOOTH_STONE.defaultBlockState());
                    }
                    // Walls
                    else if (x == -width / 2 || x == width / 2 || z == -depth / 2 || z == depth / 2) {
                        level.setBlockAndUpdate(current, Blocks.STONE_BRICKS.defaultBlockState());
                    }
                    // Air space
                    else {
                        level.setBlockAndUpdate(current, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }

        // create the exit door on the wall where the player appears
        BlockPos frame1 = doorPos.offset(0, 1, -1);
        level.setBlockAndUpdate(frame1, Blocks.STONE_BRICKS.defaultBlockState());
        level.setBlockAndUpdate(frame1.above(), Blocks.STONE_BRICKS.defaultBlockState());

        level.setBlockAndUpdate(doorPos,
                Blocks.OAK_DOOR.defaultBlockState()
                        .setValue(DoorBlock.FACING, net.minecraft.core.Direction.SOUTH)
                        .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
        level.setBlockAndUpdate(doorPos.above(),
                Blocks.OAK_DOOR.defaultBlockState()
                        .setValue(DoorBlock.FACING, net.minecraft.core.Direction.SOUTH)
                        .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
    }
}