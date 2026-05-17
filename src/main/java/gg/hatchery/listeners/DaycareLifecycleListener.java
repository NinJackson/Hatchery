package gg.hatchery.listeners;

import gg.hatchery.Hatchery;
import gg.hatchery.daycare.Daycare;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.List;
import java.util.Locale;

public class DaycareLifecycleListener implements Listener {

    private final Hatchery plugin;

    public DaycareLifecycleListener(Hatchery plugin) { this.plugin = plugin; }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (!isDaycareBlock(e.getBlock())) return;

        Player p = e.getPlayer();
        if (plugin.getDaycareManager().isVanillaBreedingWorld(e.getBlock().getWorld().getName())) {
            return;
        }

        if (plugin.getDaycareManager().isWorldBlacklisted(e.getBlock().getWorld().getName())) {
            p.sendMessage(plugin.getConfigManager().getMessages().get("daycare.disabled-world"));
            e.setCancelled(true);
            return;
        }

        List<Daycare> owned = plugin.getDaycareManager().ownedBy(p.getUniqueId());
        int max = plugin.getDaycareManager().maxDaycaresFor(p);
        if (owned.size() >= max) {
            p.sendMessage(plugin.getConfigManager().getMessages().get("daycare.max-reached", "max", max));
            e.setCancelled(true);
            return;
        }

        Daycare d = plugin.getDaycareManager().register(p.getUniqueId(), e.getBlock().getLocation());
        p.sendMessage(plugin.getConfigManager().getMessages().get("daycare.placed"));
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Daycare d = plugin.getDaycareManager().atLocation(e.getBlock().getLocation());
        if (d == null) return;

        // Optionally drop accumulated upgrade items
        if (plugin.getConfigManager().getMain().isUpgradeDropOnBreak() && d.getUpgradeLevel() > 0) {
            // TODO: drop d.getUpgradeLevel() copies of the upgrade item
        }
        plugin.getDaycareManager().unregister(d);
        e.getPlayer().sendMessage(plugin.getConfigManager().getMessages().get("daycare.removed"));
    }

    private boolean isDaycareBlock(Block b) {
        if (b.getType() == Material.AIR) return false;
        String actual = b.getType().getKey().toString().toLowerCase(Locale.ROOT);
        for (String expected : plugin.getConfigManager().getMain().getDaycareBlocks()) {
            if (actual.equals(expected)) return true;
        }
        return false;
    }
}
