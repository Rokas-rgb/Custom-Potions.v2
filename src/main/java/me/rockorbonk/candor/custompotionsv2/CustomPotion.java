package me.rockorbonk.candor.custompotionsv2;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;


public abstract class CustomPotion extends BrewAction {

    public void brew(BrewerInventory inventory, ItemStack item, ItemStack ingredient) {

        if(!(item.getType() == Material.POTION)) {
            return;
        }

        ItemStack potion = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        meta.addCustomEffect(new PotionEffect(PotionEffectType.LUCK, 3400, 1), true);


        ItemStack stick = new ItemStack(Material.STICK);
        ItemMeta stickMeta = stick.getItemMeta();
        stickMeta.setDisplayName(ChatColor.DARK_PURPLE + "Magic Stick");


        if(ingredient.getItemMeta().getDisplayName().equals(ChatColor.DARK_PURPLE + "Magic Stick")) {
            meta.addCustomEffect(new PotionEffect(PotionEffectType.LUCK, 3400, 1), true);
        }

        potion.setItemMeta(meta);
        item.setItemMeta(stickMeta);
    }

    public class PotionListener implements Listener {

        @EventHandler
        public void onPotionDrink(PlayerInteractEvent pie) {
            Player player = pie.getPlayer();

            if(player.getActivePotionEffects().equals(PotionEffectType.LUCK)) {
                if(pie.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                    if(!(pie.isCancelled())) {
                        pie.getClickedBlock().setType(Material.AIR);
                    }
                }
            }
        }
    }
}
