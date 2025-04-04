package com.thunder.riftgate.items;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, "riftgate");

    public static final DeferredHolder<Item, Item> RIFT_GATE_KEY = ITEMS.register("rift_gate_key",
            RiftGateKeyItem::new);

    // You no longer register inside this class — it's done in the mod initializer!
}
