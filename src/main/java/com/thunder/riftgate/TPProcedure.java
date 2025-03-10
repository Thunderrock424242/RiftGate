package com.thunder.riftgate;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.portal.DimensionTransition;

public class TPProcedure {

    public static void execute(Entity entity, ServerLevel targetDimension, double x, double y, double z) {
        if (entity == null || targetDimension == null) {
            return;
        }

        // Check if the entity is already in the correct dimension
        if (entity.level() == targetDimension) {
            entity.teleportTo(x + 0.5, y, z + 0.5);
        } else {
            // Create a DimensionTransition manually and apply it
            DimensionTransition transition = new DimensionTransition(
                    targetDimension,       // Target world
                    new Vec3(x + 0.5, y, z + 0.5),  // Position
                    Vec3.ZERO,             // Keeps velocity unchanged
                    entity.getYRot(),      // Keep current rotation (yaw)
                    entity.getXRot(),      // Keep current rotation (pitch)
                    false,                 // Not a missing respawn block case
                    DimensionTransition.DO_NOTHING // No extra transition behavior
            );

            entity.changeDimension(transition);
        }
    }
}