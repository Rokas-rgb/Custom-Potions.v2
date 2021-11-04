package me.rockorbonk.candor.custompotionsv2;

import org.bukkit.entity.Item;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;

public abstract class BrewAction {
    public abstract void brew(BrewerInventory inventory, ItemStack item, Item ingredient);
}
