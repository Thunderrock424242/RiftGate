package com.thunder.riftgate.events;

import com.thunder.riftgate.items.ModItems;
import com.thunder.riftgate.teleport.RoomManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

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

        if (heldItem.is(ModItems.RIFT_GATE_KEY.get())) {
            if (!RoomManager.isLinkedDoor(pos)) {
                RoomManager.linkDoor(player.getUUID(), pos);
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("This door is now linked to your Rift Room."));
                event.setCanceled(true);
            }
        } else if (RoomManager.isLinkedDoor(pos)) {
            RoomManager.enterRoom((ServerPlayer) player, pos);
            event.setCanceled(true);
        }
    }
}