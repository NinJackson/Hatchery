package gg.hatchery.listeners;

import gg.hatchery.Hatchery;
import gg.hatchery.commands.HatcheryItems;
import gg.hatchery.config.MainConfig;
import gg.hatchery.daycare.Daycare;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Locale;
import java.util.Set;

public class DaycareLifecycleListener implements Listener {

    private final Hatchery plugin;

    public DaycareLifecycleListener(Hatchery plugin) { this.plugin = plugin; }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (!isDaycareBlock(e.getBlock())) return;

        Player p = e.getPlayer();
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

        plugin.getDaycareManager().register(p.getUniqueId(), e.getBlock().getLocation());
        p.sendMessage(plugin.getConfigManager().getMessages().get("daycare.placed"));
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Daycare d = plugin.getDaycareManager().atLocation(e.getBlock().getLocation());
        if (d == null) return;

        MainConfig main = plugin.getConfigManager().getMain();
        if (main.isUpgradeDropOnBreak() && d.getUpgradeLevel() > 0) {
            Location dropLoc = e.getBlock().getLocation().add(0.5, 0.5, 0.5);
            ItemStack stack = HatcheryItems.upgradeItem(plugin, d.getUpgradeLevel());
            if (stack != null && dropLoc.getWorld() != null) {
                dropLoc.getWorld().dropItemNaturally(dropLoc, stack);
            }
        }
        plugin.getDaycareManager().unregister(d);
        e.getPlayer().sendMessage(plugin.getConfigManager().getMessages().get("daycare.removed"));
    }

    /**
     * A block counts as a daycare if its namespaced material key matches any of
     * the IDs in {@code daycare.blocks} (or the legacy {@code daycare.block}).
     * Defaults to all 16 Pixelmon {@code <colour>_day_care} variants.
     */
    private boolean isDaycareBlock(Block b) {
        if (b.getType() == Material.AIR) return false;
        Set<String> expected = plugin.getConfigManager().getMain().getDaycareBlocks();
        if (expected.isEmpty()) return false;
        String key = b.getType().getKey().toString().toLowerCase(Locale.ROOT);
        return expected.contains(key);
    }
}
