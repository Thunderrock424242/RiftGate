package com.thunder.riftgate.blockentity;

import com.thunder.riftgate.MainModClass.RiftGate;
import com.thunder.riftgate.blocks.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, RiftGate.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RiftDoorBlockEntity>> RIFT_DOOR = BLOCK_ENTITIES.register(
            "rift_door",
            () -> BlockEntityType.Builder.of(RiftDoorBlockEntity::new, ModBlocks.RIFT_DOOR.get()).build(null));

    private ModBlockEntities() {
    }
}
