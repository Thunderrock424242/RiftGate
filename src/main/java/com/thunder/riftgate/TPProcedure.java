package com.thunder.riftgate;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;

public class TPProcedure {
    public TPProcedure() {
    }

    public static void execute(Entity entity, ServerLevel targetDimension, double x, double y, double z) {
        if (entity != null && targetDimension != null) {
            if (entity.level() == targetDimension) {
                entity.teleportTo(x + (double)0.5F, y, z + (double)0.5F);
            } else {
                DimensionTransition transition = new DimensionTransition(targetDimension, new Vec3(x + (double)0.5F, y, z + (double)0.5F), Vec3.ZERO, entity.getYRot(), entity.getXRot(), false, DimensionTransition.DO_NOTHING);
                entity.changeDimension(transition);
            }

        }
    }
}
