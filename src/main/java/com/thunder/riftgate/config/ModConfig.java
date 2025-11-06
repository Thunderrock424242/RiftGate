package com.thunder.riftgate.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ModConfig {
    public enum PortalRenderMode {
        SEE_THROUGH,
        END_PORTAL
    }

    public static final ModConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        CLIENT = new Client(builder);
        CLIENT_SPEC = builder.build();
    }

    public static class Client {
        public final ModConfigSpec.EnumValue<PortalRenderMode> portalRenderMode;

        Client(ModConfigSpec.Builder builder) {
            builder.push("rendering");
            portalRenderMode = builder
                    .comment("Controls how linked doors display the portal view.",
                            "SEE_THROUGH renders a live view into the interior.",
                            "END_PORTAL uses the vanilla End portal texture.")
                    .defineEnum("portalRenderMode", PortalRenderMode.SEE_THROUGH);
            builder.pop();
        }
    }
}
