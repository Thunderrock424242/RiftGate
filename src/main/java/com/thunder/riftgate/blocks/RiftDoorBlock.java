package com.thunder.riftgate.blocks;

import com.thunder.riftgate.blockentity.RiftDoorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import org.jetbrains.annotations.Nullable;

public class RiftDoorBlock extends DoorBlock implements EntityBlock {
    public RiftDoorBlock(Properties properties, BlockSetType blockSetType) {
        super(blockSetType, properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RiftDoorBlockEntity(pos, state);
    }
}
