package com.thunder.riftgate.MainModClass;

import com.mojang.brigadier.CommandDispatcher;
import com.thunder.riftgate.config.ModConfig;
import com.thunder.riftgate.events.DoorEventHandler;
import com.thunder.riftgate.dimension.ModDimensions;
import com.thunder.riftgate.items.ModCreativeTabs;
import com.thunder.riftgate.items.ModItems;
import com.thunder.riftgate.client.input.ClientKeyHandler;
import com.thunder.riftgate.network.ModNetworking;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

@Mod(RiftGate.MOD_ID)
public class RiftGate {
    /**
     * The constant LOGGER.
     */
    public static final Logger LOGGER = LogManager.getLogger("riftgate");

    /**
     * The constant MOD_ID.
     */
    public static final String MOD_ID = "riftgate";
    private static final Map<CustomPacketPayload.Type<?>, NetworkMessage<?>> MESSAGES = new HashMap<>();

    private record NetworkMessage<T extends CustomPacketPayload>(StreamCodec<? extends FriendlyByteBuf, T> reader,
                                                                 IPayloadHandler<T> handler) {
    }
    /**
     * Instantiates a new RiftGate main mod class.
     *
     * @param modEventBus the mod event bus
     * @param container   the container
     */
    public RiftGate(IEventBus modEventBus, ModContainer container) {
        LOGGER.info("RiftGate mod initialized; starting mod conflict tracking");
        // Register mod setup and creative tabs
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);
        // Register client-only setup
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(ModNetworking::register);
        if (FMLEnvironment.dist.isClient()) {
            ClientKeyHandler.init(modEventBus);
        }

        ModItems.ITEMS.register(modEventBus);
        // Same for blocks, creative tabs, etc.
        ModCreativeTabs.TABS.register(modEventBus);

        ModDimensions.register();
        NeoForge.EVENT_BUS.register(DoorEventHandler.class);

        container.registerConfig(net.neoforged.fml.config.ModConfig.Type.CLIENT, ModConfig.CLIENT_SPEC);

        // Register global events
        NeoForge.EVENT_BUS.register(this);

    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            System.out.println("RiftGate setup complete!");
        });
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.RIFT_GATE_KEY.get());
        }
    }

    /**
     * On server starting.
     *
     * @param event the event
     */
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {


    }

    /**
     * On register commands.
     *
     * @param event the event
     */
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
    }

    /**
     * On server stopping.
     *
     * @param event the event
     */
    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {

    }

    private void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Register custom item property for glowing overlay on portal key
            ResourceLocation activeProperty = ResourceLocation.parse(MOD_ID + ":active");
            ItemProperties.register(
                    ModItems.RIFT_GATE_KEY.get(),
                    activeProperty,
                    (stack, world, entity, seed) -> 1.0F // Always active glow
            );
            ItemProperties.register(
                    ModItems.PORTAL_KEY.get(),
                    activeProperty,
                    (stack, world, entity, seed) -> 1.0F
            );

            // TODO: Future - Register BlockEntityRenderers, custom shaders, etc.
        });
    }
    public static Component translation(String key) {
        return Component.translatable("text." + MOD_ID + "." + key);
    }

    public static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
