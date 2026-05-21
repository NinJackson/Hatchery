package gg.hatchery.ui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Tracks which menu each player has open.
 */
public class MenuManager {

    private final ConcurrentMap<UUID, HatcheryMenu> open = new ConcurrentHashMap<>();

    public void track(Player p, HatcheryMenu m) { open.put(p.getUniqueId(), m); }
    public void untrack(Player p)               { open.remove(p.getUniqueId()); }
    public HatcheryMenu get(Player p)           { return open.get(p.getUniqueId()); }

    public void rebuildDaycare(gg.hatchery.daycare.Daycare daycare) {
        for (HatcheryMenu menu : open.values()) {
            if (menu instanceof DaycareMenu) {
                DaycareMenu daycareMenu = (DaycareMenu) menu;
                if (daycareMenu.daycare().getId().equals(daycare.getId())) {
                    daycareMenu.rebuild();
                }
            }
        }
    }

    public boolean isHatcheryMenu(Inventory inv) {
        if (inv == null) return false;
        for (HatcheryMenu m : open.values()) {
            if (m.getInventory().equals(inv)) return true;
        }
        return false;
    }
}
