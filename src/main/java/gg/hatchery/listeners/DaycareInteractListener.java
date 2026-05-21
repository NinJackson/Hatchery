package gg.hatchery.listeners;

import gg.hatchery.Hatchery;
import gg.hatchery.daycare.Daycare;
import gg.hatchery.ui.DaycareMenu;
import gg.hatchery.util.ItemBuilder;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class DaycareInteractListener implements Listener {

    private final Hatchery plugin;
    public DaycareInteractListener(Hatchery plugin) { this.plugin = plugin; }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getClickedBlock() == null) return;
        Daycare d = plugin.getDaycareManager().atLocation(e.getClickedBlock().getLocation());
        if (d == null) return;
        e.setCancelled(true);

        ItemStack held = e.getItem();
        String hourglassId = ItemBuilder.tagOf(held, new NamespacedKey(plugin, "hourglass_id"));
        if (hourglassId != null) {
            plugin.getHourglassService().tryApply(e.getPlayer(), d, held, hourglassId);
            return;
        }

        String upgradeId = ItemBuilder.tagOf(held, new NamespacedKey(plugin, "upgrade_id"));
        if (upgradeId != null) {
            plugin.getUpgradeService().tryApply(e.getPlayer(), d, held);
            return;
        }

        DaycareMenu.open(plugin, e.getPlayer(), d);
    }
}
