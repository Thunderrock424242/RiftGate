package com.thunder.riftgate.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetworking {
    private static final String NETWORK_VERSION = "1";

    private ModNetworking() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(NETWORK_VERSION);
        registrar.playToServer(BindDoorPayload.TYPE, BindDoorPayload.STREAM_CODEC, BindDoorPayload::handle);
        registrar.playToClient(RoomPreviewPayload.TYPE, RoomPreviewPayload.STREAM_CODEC, RoomPreviewPayload::handle);
    }
}
