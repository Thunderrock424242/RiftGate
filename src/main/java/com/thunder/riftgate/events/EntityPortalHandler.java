package com.thunder.riftgate.events;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.HashSet;
import java.util.Set;

@EventBusSubscriber
public class EntityPortalHandler {
    private static final Set<Entity> recentlyTeleported = new HashSet<>();

    @SubscribeEvent
    public static void onEntityTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide()) return;

        Level level = event.level;
        for (Entity entity : level.getEntities(null, entity -> entity.isAlive() && entity.blockPosition() != null)) {
            BlockPos pos = entity.blockPosition();
            BlockState state = level.getBlockState(pos);

            if (!(state.getBlock() instanceof DoorBlock)) continue;
            boolean isOpen = state.getValue(DoorBlock.OPEN);
            if (!RoomManager.isLinkedDoor(pos)) continue;

            // Prevent re-entry spam
            if (recentlyTeleported.contains(entity)) continue;

            boolean canEnter = false;

            if (entity instanceof Player) {
                canEnter = isOpen;
            } else if (entity instanceof TamableAnimal || entity instanceof Animal) {
                if (isOpen) {
                    canEnter = true;
                } else if (entity.hasCustomName()) {
                    canEnter = true; // Pet with name tag
                }
            }

            if (canEnter) {
                recentlyTeleported.add(entity);
                entity.getServer().execute(() -> {
                    RoomManager.teleportEntity(entity, pos);
                    // Remove from cooldown after 1 second
                    entity.level().getServer().execute(() -> recentlyTeleported.remove(entity));
                });
            }
        }
    }
}