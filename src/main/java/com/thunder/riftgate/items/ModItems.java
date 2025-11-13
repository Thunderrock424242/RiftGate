package com.thunder.riftgate.items;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, "riftgate");

    public static final DeferredHolder<Item, Item> RIFT_GATE_KEY = ITEMS.register("rift_gate_key",
            RiftGateKeyItem::new);

    public static boolean isKeyItem(ItemStack stack) {
        return stack.is(RIFT_GATE_KEY.get());
    }

    // You no longer register inside this class — it's done in the mod initializer!
}
