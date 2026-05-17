package gg.hatchery.listeners;

import gg.hatchery.Hatchery;
import gg.hatchery.ui.HatcheryMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class InventoryClickListener implements Listener {

    private final Hatchery plugin;
    public InventoryClickListener(Hatchery plugin) { this.plugin = plugin; }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();
        HatcheryMenu menu = plugin.getMenuManager().get(p);
        if (menu == null || !menu.getInventory().equals(e.getView().getTopInventory())) return;
        e.setCancelled(true);
        menu.onClick(p, e);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player)) return;
        Player p = (Player) e.getPlayer();
        HatcheryMenu menu = plugin.getMenuManager().get(p);
        if (menu != null && menu.getInventory().equals(e.getInventory())) {
            plugin.getMenuManager().untrack(p);
        }
    }
}
