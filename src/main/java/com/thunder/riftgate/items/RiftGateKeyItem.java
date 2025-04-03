package com.thunder.riftgate.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public class RiftGateKeyItem extends Item {
    public RiftGateKeyItem() {
        super(new Item.Properties()
                .rarity(Rarity.RARE)
                .stacksTo(1)
                .fireResistant()
                .setNoRepair()); // fixed from setNoRepair() to noRepair()
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true; // Makes it glow like an enchanted item
    }
}
