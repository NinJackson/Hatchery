package gg.hatchery.listeners;

import gg.hatchery.Hatchery;
import gg.hatchery.daycare.Daycare;
import gg.hatchery.ui.DaycareMenu;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class DaycareInteractListener implements Listener {

    private final Hatchery plugin;
    public DaycareInteractListener(Hatchery plugin) { this.plugin = plugin; }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getClickedBlock() == null) return;
        Daycare d = plugin.getDaycareManager().atLocation(e.getClickedBlock().getLocation());
        if (d == null) return;
        if (plugin.getDaycareManager().isVanillaBreedingWorld(e.getClickedBlock().getWorld().getName())) return;
        e.setCancelled(true);
        // TODO: if held item is upgrade -> apply upgrade; if hourglass -> advance ticks; else open GUI
        DaycareMenu.open(plugin, e.getPlayer(), d);
    }
}
