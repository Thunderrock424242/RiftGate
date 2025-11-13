package com.thunder.riftgate.blockentity;

import com.thunder.riftgate.blocks.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class RiftDoorBlockEntity extends BlockEntity {
    public RiftDoorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RIFT_DOOR.get(), pos, state);
    }
}
