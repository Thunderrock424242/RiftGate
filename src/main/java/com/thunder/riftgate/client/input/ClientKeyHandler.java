package com.thunder.riftgate.client.input;

import com.thunder.riftgate.MainModClass.RiftGate;
import com.thunder.riftgate.items.ModItems;
import com.thunder.riftgate.network.BindDoorPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = RiftGate.MOD_ID, value = Dist.CLIENT)
public final class ClientKeyHandler {
    private static final KeyMapping BIND_DOOR_KEY = new KeyMapping(
            "key." + RiftGate.MOD_ID + ".bind_door",
            GLFW.GLFW_KEY_F,
            "key.categories.gameplay"
    );

    private ClientKeyHandler() {}

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(ModEvents::registerKeyMappings);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null) {
            return;
        }

        while (BIND_DOOR_KEY.consumeClick()) {
            if (!isHoldingKey(player)) {
                continue;
            }

            HitResult hitResult = minecraft.hitResult;
            if (!(hitResult instanceof BlockHitResult blockHit) || hitResult.getType() != HitResult.Type.BLOCK) {
                continue;
            }

            if (minecraft.getConnection() != null) {
                minecraft.getConnection().send(new BindDoorPayload(blockHit.getBlockPos()));
            }
        }
    }

    private static boolean isHoldingKey(LocalPlayer player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (ModItems.isKeyItem(stack)) {
                return true;
            }
        }
        return false;
    }

    public static final class ModEvents {
        private ModEvents() {}

        public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(BIND_DOOR_KEY);
        }
    }
}
