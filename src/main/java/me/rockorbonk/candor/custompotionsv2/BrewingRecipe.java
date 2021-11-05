package me.rockorbonk.candor.custompotionsv2;

import org.bukkit.Material;
import org.bukkit.block.BrewingStand;
import org.bukkit.entity.Item;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class BrewingRecipe {

    private ItemStack ingredient;
    private ItemStack fuel;

    private int fuelSet;
    private int fuelCharge;

    private BrewAction action;
    private BrewClock clock;

    private boolean perfect;

    public BrewingRecipe(ItemStack ingredient, ItemStack fuel, BrewAction action, boolean perfect, int fuelSet, int fuelCharge) {
        this.ingredient = ingredient;
        this.fuel = (fuel == null ? new ItemStack(Material.AIR) : fuel);
        this.setFuelSet(fuelSet);
        this.setFuelCharge(fuelCharge);
        this.action = action;
        this.perfect = perfect;
    }

    public BrewingRecipe(Material ingredient, BrewAction action) {
        this(new ItemStack(ingredient), null, action, true, 40, 0);
    }

    public ItemStack getIngredient() {return ingredient;}

    public ItemStack getFuel() {return fuel;}

    public BrewAction getAction() {return action;}

    public void setAction(BrewAction action) {this.action = action;}

    public BrewClock getClock() {return clock;}

    public void setClock(BrewClock clock) {this.clock = clock;}

    public boolean isPerfect() {return perfect;}

    public void setPerfect(boolean perfect) {this.perfect = perfect;}

    public static BrewingRecipe getRecipe(BrewerInventory inventory) {
        for (BrewingRecipe recipe: Brewery.recipes) {
            if (inventory.getFuel() == null) {
                if(!recipe.isPerfect() && inventory.getIngredient().getType() == recipe.getIngredient().getType()) {
                    return recipe;
                }
                if(recipe.isPerfect()&& inventory.getIngredient().isSimilar(recipe.getIngredient())) {
                    return recipe;
                }
                else{
                    if(!recipe.isPerfect() && inventory.getIngredient().getType() == recipe.getIngredient().getType() &&
                            inventory.getFuel().getType() == recipe.getIngredient().getType()) {
                        return recipe;
                    }
                    if (recipe.isPerfect() && inventory.getIngredient().isSimilar(recipe.getIngredient()) &&
                            inventory.getFuel().isSimilar(recipe.getFuel())) {
                        return recipe;
                    }
                }
            }
        }
        return null;
    }

    public void startBrewing(BrewerInventory inventory) {
       clock = new BrewClock(this, inventory, 400);
    }

    public int getFuelSet() {return fuelSet;}

    public void setFuelSet(int fuelSet) {this.fuelSet = fuelSet;}

    public int getFuelCharge() {return fuelCharge;}

    public void setFuelCharge(int fuelCharge) {this.fuelCharge = fuelCharge;}


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
            runTaskTimer(BrewingRecipe.getInstance(), 0L, 1L);
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
                    int count = 0;
                    while (inventory.getFuel().getAmount() > 0 && stand.getFuelLevel() + recipe.fuelCharge < 100) {
                        stand.setFuelLevel(stand.getFuelLevel() + recipe.fuelSet);
                        count++;
                    }
                    if(inventory.getFuel().getAmount() == 0) {
                        newFuel = new ItemStack(Material.AIR);
                    } else{
                        stand.setFuelLevel(100);
                        newFuel.setAmount(inventory.getFuel().getAmount() - count);
                    }
                } else{
                    newFuel = new ItemStack(Material.AIR);
                }
                inventory.setFuel(newFuel);
                for (int i = 0; i < 3; i++) {
                    if(inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) {
                        continue;
                    }
                    recipe.getAction().brew(inventory, inventory.getItem(i), (Item) ingredient);
                }
                stand.setFuelLevel(stand.getFuelLevel() - recipe.fuelCharge);
                cancel();
                return;
            }
            current--;
            stand.setBrewingTime(current);
            stand.update(true);
        }
        public boolean searchChanged(ItemStack[] before, ItemStack[] after, boolean mode) {
            for (int i = 0; i < before.length; i++) {
                if((before[i] != null && after[i] == null || (before[i] == null && after[i] != null))) {
                    return false;
                } else {
                    if(mode && !before[i].isSimilar(after[i])) {
                        return false;
                    } else if(!mode && !(before[i].getType() == after[i].getType())) {
                        return false;
                    }
                }
            }
            return true;
        }
    }
}
