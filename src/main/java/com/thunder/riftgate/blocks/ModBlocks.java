package com.thunder.riftgate.blocks;

import com.thunder.riftgate.MainModClass.RiftGate;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, RiftGate.MOD_ID);

    public static final DeferredHolder<Block, Block> RIFT_DOOR = BLOCKS.register("rift_door",
            () -> new RiftDoorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(3.0F)
                    .noOcclusion()
                    .pushReaction(PushReaction.DESTROY)
                    .ignitedByLava(),
                    BlockSetType.OAK));

    private ModBlocks() {
    }
}
