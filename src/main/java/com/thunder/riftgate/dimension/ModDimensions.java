package com.thunder.riftgate.dimension;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class ModDimensions {
    public static final ResourceKey<Level> INTERIOR_DIM_KEY =
            ResourceKey.create(Registries.DIMENSION, new ResourceLocation("riftgate", "interior"));

    public static void register() {
        // Dimension auto-registered via JSON
    }
}