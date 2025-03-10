package com.thunder.riftgate;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class TPProcedure {

    public static void execute(Entity entity, ServerLevel targetDimension, double x, double y, double z) {
        if (entity == null || targetDimension == null) {
            return;
        }

        // Check if the entity is already in the correct dimension
        if (entity.level() == targetDimension) {
            entity.teleportTo(x + 0.5, y, z + 0.5);
        } else {
            entity.changeDimension(targetDimension, new PortalInfo(
                    new Vec3(x + 0.5, y, z + 0.5),  // Position
                    Vec3.ZERO,                      // Motion (keeps current velocity)
                    entity.getYRot(),               // Yaw
                    entity.getXRot()                // Pitch
            ), PortalTravelAgent.DEFAULT);
        }
    }
}