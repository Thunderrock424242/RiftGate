package com.thunder.riftgate.items;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "riftgate");

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> RIFT_GATE_TAB =
            TABS.register("riftgate_tab", () -> CreativeModeTab.builder()
                    .title(Component.literal("Rift Gate"))
                    .icon(() -> new ItemStack(ModItems.RIFT_GATE_KEY.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.RIFT_GATE_KEY.get());
                    })
                    .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
                    .build());
}