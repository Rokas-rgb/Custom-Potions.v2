package me.rockorbonk.candor.custompotionsv2;

import org.bukkit.Material;
import org.bukkit.block.BrewingStand;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.w3c.dom.html.HTMLImageElement;

public class BrewClock extends BukkitRunnable {
    private BrewerInventory inventory;
    private BrewingRecipe recipe;
    private ItemStack[] before;
    private BrewingStand stand;
    private int current;
    public BrewClock(BrewingRecipe recipe, BrewerInventory inventory, int time) {
        this.recipe = recipe;
        this.inventory = inventory;
        this.stand = inventory.getHolder();
        this.before = inventory.getContents();
        this.current = time;
        runTaskTimer(CustomPotionsV2.getInstance(), 0L, 1L);
    }

    @Override
    public void run() {
        if(current = 0) {
            if(inventory.getIngredient().getAmount() > 1) {
                ItemStack is = inventory.getIngredient();
                is.setAmount(inventory.getIngredient().getAmount() - 1);
                inventory.setIngredient(is);
            } else {
                inventory.setIngredient(new ItemStack(Material.AIR));
            }
            ItemStack newFuel = recipe.getFuel();
            if(recipe.getFuel() != null && recipe.getFuel().getType() != Material.AIR && recipe.getFuel().getAmount() > 0) {
                
            }
        }
    }
}

