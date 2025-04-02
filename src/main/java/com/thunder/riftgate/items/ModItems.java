package com.thunder.riftgate.items;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import static com.thunder.riftgate.MainModClass.RiftGate.MOD_ID;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(NeoForgeRegistries.ITEMS, MOD_ID);

    public static final DeferredHolder<Item, PortalKeyItem> PORTAL_KEY = ITEMS.register("portal_key", PortalKeyItem::new);
}