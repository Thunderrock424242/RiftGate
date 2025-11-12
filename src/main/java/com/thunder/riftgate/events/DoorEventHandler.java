package com.thunder.riftgate.events;

import com.thunder.riftgate.items.ModItems;
import com.thunder.riftgate.teleport.RoomManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.UUID;

@EventBusSubscriber
public class DoorEventHandler {

    @SubscribeEvent
    public static void onDoorInteract(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        Player player = event.getEntity();
        ItemStack heldItem = player.getItemInHand(event.getHand());

        if (!(state.getBlock() instanceof DoorBlock) || level.isClientSide) return;
        BlockPos doorBase = state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;

        boolean isLinkedDoor = RoomManager.isLinkedDoor(doorBase);
        boolean isRoomDoor = RoomManager.isRoomDoor(doorBase);
        if (!isLinkedDoor && !isRoomDoor) {
            if (ModItems.isKeyItem(heldItem)) {
                RoomManager.linkDoor(player.getUUID(), doorBase);
                player.sendSystemMessage(Component.literal("This door is now linked to your Rift Room."));
                event.setCanceled(true);
            }
            return;
        }

        UUID ownerId = isLinkedDoor
                ? RoomManager.getLinkedDoorOwner(doorBase)
                : RoomManager.getRoomDoorOwner(doorBase);
        boolean isOwner = ownerId != null && ownerId.equals(player.getUUID());
        boolean isLocked = RoomManager.isDoorLocked(doorBase);

        if (player.isShiftKeyDown() && isOwner && !ModItems.isKeyItem(heldItem)) {
            if (!isLocked) {
                RoomManager.lockDoor(doorBase);
                closeDoor(level, doorBase);
                player.sendSystemMessage(Component.literal("Door locked."));
            } else {
                player.sendSystemMessage(Component.literal("Door is already locked."));
            }
            event.setCanceled(true);
            return;
        }

        if (ModItems.isKeyItem(heldItem)) {
            if (!isLinkedDoor) {
                RoomManager.linkDoor(player.getUUID(), doorBase);
                player.sendSystemMessage(Component.literal("This door is now linked to your Rift Room."));
                event.setCanceled(true);
                return;
            }

            if (isLocked && isOwner) {
                RoomManager.unlockDoor(doorBase);
                player.sendSystemMessage(Component.literal("Door unlocked."));
                event.setCanceled(true);
                return;
            }
        }

        if (isLocked) {
            event.setCanceled(true);
            if (isOwner) {
                player.sendSystemMessage(Component.literal("Your door is locked. Use your key to unlock it."));
            } else {
                player.sendSystemMessage(Component.literal("This door is locked."));
            }
        }
    }

    private static void closeDoor(Level level, BlockPos basePos) {
        BlockState lower = level.getBlockState(basePos);
        if (lower.getBlock() instanceof DoorBlock) {
            if (lower.getValue(DoorBlock.OPEN)) {
                level.setBlockAndUpdate(basePos, lower.setValue(DoorBlock.OPEN, false));
            }
        }
        BlockPos upperPos = basePos.above();
        BlockState upper = level.getBlockState(upperPos);
        if (upper.getBlock() instanceof DoorBlock) {
            if (upper.getValue(DoorBlock.OPEN)) {
                level.setBlockAndUpdate(upperPos, upper.setValue(DoorBlock.OPEN, false));
            }
        }
    }
}