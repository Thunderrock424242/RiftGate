package com.thunder.riftgate.events;

import com.thunder.riftgate.teleport.RoomManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber
public final class PlayerLifecycleHandler {
    private PlayerLifecycleHandler() {}

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (RoomManager.roomExists(player.getUUID())) {
            RoomManager.syncRoomPreview(player);
        }
    }
}
