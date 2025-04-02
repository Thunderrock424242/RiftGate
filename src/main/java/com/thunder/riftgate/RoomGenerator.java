package com.thunder.riftgate;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

public class RoomGenerator {
    public static void generateRoom(ServerLevel level, BlockPos origin) {
        int width = 7;
        int height = 5;
        int depth = 7;

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
    }
}