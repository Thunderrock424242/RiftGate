package com.thunder.riftgate.events;

import com.thunder.riftgate.items.ModItems;
import com.thunder.riftgate.teleport.RoomManager;
import com.thunder.riftgate.dimension.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber
public class EntityPortalHandler {
    private static final Set<Entity> recentlyTeleported = new HashSet<>();
    private static final Map<UUID, Integer> playerTeleportCooldowns = new HashMap<>();
    private static final Map<UUID, Integer> warningCooldowns = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        Level level = player.level();
        if (level.isClientSide()) {
            return;
        }

        UUID playerId = player.getUUID();
        tickCooldown(playerTeleportCooldowns, playerId);
        tickCooldown(warningCooldowns, playerId);

        if (playerTeleportCooldowns.containsKey(playerId)) {
            return;
        }

        if (player.isShiftKeyDown() || player.isSpectator() || player.isSleeping()) {
            return;
        }

        if (isHoldingKey(player)) {
            return;
        }

        var expandedBox = player.getBoundingBox().inflate(0.1);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        BlockPos min = BlockPos.containing(expandedBox.minX, expandedBox.minY, expandedBox.minZ);
        BlockPos max = BlockPos.containing(expandedBox.maxX, expandedBox.maxY, expandedBox.maxZ);

        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            cursor.set(pos.getX(), pos.getY(), pos.getZ());
            BlockState state = level.getBlockState(cursor);
            if (!(state.getBlock() instanceof DoorBlock)) {
                continue;
            }

            if (state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
                continue;
            }

            if (!state.getValue(DoorBlock.OPEN)) {
                continue;
            }

            if (RoomManager.isDoorLocked(cursor)) {
                continue;
            }

            boolean isLinkedDoor = RoomManager.isLinkedDoor(cursor);
            boolean isRoomDoor = RoomManager.isRoomDoor(cursor);

            if (!isLinkedDoor && !isRoomDoor) {
                continue;
            }

            UUID ownerId = isLinkedDoor
                    ? RoomManager.getLinkedDoorOwner(cursor)
                    : RoomManager.getRoomDoorOwner(cursor);

            if (ownerId == null) {
                continue;
            }

            if (!ownerId.equals(playerId)) {
                if (!warningCooldowns.containsKey(playerId)) {
                    player.displayClientMessage(Component.literal("This door is attuned to another Rift Room."), true);
                    warningCooldowns.put(playerId, 40);
                }
                continue;
            }

            if (isLinkedDoor && !level.dimension().equals(ModDimensions.INTERIOR_DIM_KEY)) {
                RoomManager.enterRoom(player, cursor.immutable());
                playerTeleportCooldowns.put(playerId, 10);
                return;
            }

            if (isRoomDoor) {
                RoomManager.exitRoom(player, cursor.immutable());
                playerTeleportCooldowns.put(playerId, 10);
                return;
            }
        }
    }

    @SubscribeEvent
    public static void onEntityTick(LevelTickEvent.Post event) {
        Level level = event.getLevel();
        if (level.isClientSide()) return;

        for (Entity entity : level.getEntitiesOfClass(Entity.class, level.getWorldBorder().getCollisionShape().bounds())) {
            if (!entity.isAlive() || entity instanceof Player) continue;

            BlockPos pos = entity.blockPosition();
            BlockState state = level.getBlockState(pos);

            if (!(state.getBlock() instanceof DoorBlock)) continue;
            boolean isOpen = state.getValue(DoorBlock.OPEN);

            boolean isLinked = RoomManager.isLinkedDoor(pos);
            boolean isRoomDoor = RoomManager.isRoomDoor(pos);

            if (!isLinked && !isRoomDoor) continue;

            if (recentlyTeleported.contains(entity)) continue;

            boolean canEnter = false;

            if (entity instanceof Animal) {
                if (isOpen || entity.hasCustomName()) {
                    canEnter = true;
                }
            }

            if (canEnter) {
                recentlyTeleported.add(entity);
                Objects.requireNonNull(entity.getServer()).execute(() -> {
                    if (isLinked) {
                        RoomManager.teleportEntity(entity, pos);
                    } else if (isRoomDoor) {
                        RoomManager.exitRoom(entity, pos);
                    }
                    Objects.requireNonNull(level.getServer()).execute(() -> recentlyTeleported.remove(entity));
                });
            }
        }
    }

    private static void tickCooldown(Map<UUID, Integer> map, UUID id) {
        Integer ticks = map.get(id);
        if (ticks == null) {
            return;
        }
        if (ticks <= 1) {
            map.remove(id);
        } else {
            map.put(id, ticks - 1);
        }
    }

    private static boolean isHoldingKey(Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            if (ModItems.isKeyItem(player.getItemInHand(hand))) {
                return true;
            }
        }
        return false;
    }
}