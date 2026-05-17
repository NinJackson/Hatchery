package gg.hatchery.ui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public interface HatcheryMenu {
    Inventory getInventory();
    void onClick(Player viewer, InventoryClickEvent e);
}
