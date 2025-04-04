package com.thunder.riftgate.items;

import com.thunder.riftgate.MainModClass.RiftGate;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RiftGate.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> RIFT_TAB =
            TABS.register("riftgate_tab", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.RIFT_GATE_KEY.get()))
                    .title(RiftGate.translation("itemGroup.riftgate_tab"))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.RIFT_GATE_KEY.get());
                    }).build());
}
