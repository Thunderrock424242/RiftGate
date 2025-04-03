package com.thunder.riftgate.events;

import com.thunder.riftgate.teleport.RoomManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.HashSet;
import java.util.Set;

@EventBusSubscriber
public class EntityPortalHandler {
    private static final Set<Entity> recentlyTeleported = new HashSet<>();

    @SubscribeEvent
    public static void onEntityTick(LevelTickEvent.Post event) {
        Level level = event.getLevel();
        if (level.isClientSide()) return;

        for (Entity entity : level.getEntitiesOfClass(Entity.class, level.getWorldBorder().getCollisionShape().bounds())) {
            if (!entity.isAlive() || entity.blockPosition() == null) continue;

            BlockPos pos = entity.blockPosition();
            BlockState state = level.getBlockState(pos);

            if (!(state.getBlock() instanceof DoorBlock)) continue;
            boolean isOpen = state.getValue(DoorBlock.OPEN);
            if (!RoomManager.isLinkedDoor(pos)) continue;

            if (recentlyTeleported.contains(entity)) continue;

            boolean canEnter = false;

            if (entity instanceof Player) {
                canEnter = isOpen;
            } else if (entity instanceof TamableAnimal || entity instanceof Animal) {
                if (isOpen || entity.hasCustomName()) {
                    canEnter = true;
                }
            }

            if (canEnter) {
                recentlyTeleported.add(entity);
                entity.getServer().execute(() -> {
                    RoomManager.teleportEntity(entity, pos);
                    // Remove from cooldown after 1 second
                    level.getServer().execute(() -> recentlyTeleported.remove(entity));
                });
            }
        }
    }
}