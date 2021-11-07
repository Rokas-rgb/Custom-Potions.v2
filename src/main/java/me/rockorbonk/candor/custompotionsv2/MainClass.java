package me.rockorbonk.candor.custompotionsv2;

import org.bukkit.Material;
import org.bukkit.block.BrewingStand;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class MainClass extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new BrewingRecipe.PotionEvent(), this);
        new BrewingRecipe(Material.STICK, new CustomPotion() {
            @Override
            public void brew(BrewerInventory inventory, ItemStack item, Item ingredient) {

            }
        });
    }

    public class BrewingRecipe {

        private static BrewingRecipe[] recipes;
        private final ItemStack ingredient;
    private final ItemStack fuel;

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
        for (BrewingRecipe recipe: BrewingRecipe.recipes) {
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
            runTaskTimer((Plugin) recipe, 0L, 1L);
        }

        @Override
        public void run() {
            if(current == 0) {
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

    public static class PotionEvent implements Listener {

        @EventHandler
        public void customPotionItemStackClick(InventoryClickEvent event) {

            Inventory inv = event.getClickedInventory();

            if(inv == null || inv.getType() != InventoryType.BREWING) {
                return;
            }

            if(!(event.getClick() == ClickType.LEFT || event.getClick() == ClickType.RIGHT)) {
                return;
            }

            ItemStack is = event.getCurrentItem();
            ItemStack is2 = event.getCursor();

            if(event.getClick() == ClickType.RIGHT && is.isSimilar(is2)) {
                return;
            }

            event.setCancelled(true);

            Player p = (Player) (event.getView().getPlayer());

            boolean compare = is.isSimilar(is2);
            ClickType type = event.getClick();

            int firstAmount = is.getAmount();
            int secondAmount = is2.getAmount();

            int stack = is.getMaxStackSize();
            int half = firstAmount / 2;

            int clickedSlot = event.getSlot();

            if(type == ClickType.LEFT) {

                if(is == null || (is != null && is.getType() == Material.AIR)) {

                    p.setItemOnCursor(is);
                    inv.setItem(clickedSlot, is2);

                } else if(compare) {

                    int used = stack - firstAmount;
                    if(secondAmount <= used) {
                        is.setAmount(firstAmount + secondAmount);
                        p.setItemOnCursor(null);
                        }

                    } else {

                    int used = stack - firstAmount;

                    is2.setAmount(secondAmount - used);
                    is.setAmount(firstAmount + used);
                    p.setItemOnCursor(is2);
                    }

                } else if(!compare) {
                    inv.setItem(clickedSlot, is2);
                    p.setItemOnCursor(is);

                } else if ((is2 != null && is.getType() != Material.AIR) && (is2 == null || (is2 != null && is.getType() == Material.AIR))) {

                    ItemStack isClone = is.clone();
                    isClone.setAmount(is.getAmount() % 2 == 0 ? firstAmount - half : firstAmount - half - 1);
                    p.setItemOnCursor(isClone);

                    is.setAmount(firstAmount - half);

                } else if(compare) {

                    if((firstAmount + 1) <= stack) {

                        is2.setAmount(secondAmount - 1);
                        is.setAmount(firstAmount + 1);

                    }
                } else if(!compare) {

                inv.setItem(clickedSlot, is2);
                p.setItemOnCursor(is);

                }
            if (((BrewerInventory) inv).getIngredient() == null) {
                return;
                }

                BrewingRecipe recipe = BrewingRecipe.getRecipe((BrewerInventory) inv);

            if(recipe == null) {
                return;
                }

            recipe.startBrewing((BrewerInventory) inv);
            }
        }
    }
}