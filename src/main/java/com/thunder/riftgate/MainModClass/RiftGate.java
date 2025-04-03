package com.thunder.riftgate.MainModClass;

import com.mojang.brigadier.CommandDispatcher;
import com.thunder.riftgate.events.DoorEventHandler;
import com.thunder.riftgate.dimension.ModDimensions;
import com.thunder.riftgate.items.ModCreativeTabs;
import com.thunder.riftgate.items.ModItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
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
     * Instantiates a new Wilderness odyssey api main mod class.
     *
     * @param modEventBus the mod event bus
     * @param container   the container
     */
    public RiftGate(IEventBus modEventBus, ModContainer container) {
        LOGGER.info("WildernessOdysseyAPI initialized. I will also start to track mod conflicts");
        // Register mod setup and creative tabs
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);

        ModDimensions.register();
        NeoForge.EVENT_BUS.register(DoorEventHandler.class);

        // Register global events
        NeoForge.EVENT_BUS.register(this);

    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            System.out.println("Wilderness Odyssey setup complete!");
        });
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
            if (event.getTab() == ModCreativeTabs.RIFT_GATE_TAB.get()) {
                event.accept(ModItems.RIFT_GATE_KEY.get());
            }
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
            ItemProperties.register(
                    ModItems.RIFT_GATE_KEY.get(),
                    ResourceLocation.parse(MOD_ID + ":active"),
                    (stack, world, entity, seed) -> 1.0F // Always active glow
            );

            // TODO: Future - Register BlockEntityRenderers, custom shaders, etc.
        });
    }
    }
