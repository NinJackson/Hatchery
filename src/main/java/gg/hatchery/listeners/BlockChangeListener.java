package gg.hatchery.listeners;

import gg.hatchery.Hatchery;
import gg.hatchery.daycare.Daycare;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.world.StructureGrowEvent;

public class BlockChangeListener implements Listener {

    private final Hatchery plugin;

    public BlockChangeListener(Hatchery plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        invalidateNear(event.getBlock().getLocation());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        invalidateNear(event.getBlock().getLocation());
    }

    @EventHandler(ignoreCancelled = true)
    public void onFluid(BlockFromToEvent event) {
        invalidateNear(event.getToBlock().getLocation());
    }

    @EventHandler(ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent event) {
        invalidateNear(event.getBlock().getLocation());
    }

    @EventHandler(ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event) {
        event.getBlocks().forEach(blockState -> invalidateNear(blockState.getLocation()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onFade(BlockFadeEvent event) {
        invalidateNear(event.getBlock().getLocation());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBurn(BlockBurnEvent event) {
        invalidateNear(event.getBlock().getLocation());
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        invalidateNear(event.getBlock().getLocation());
    }

    private void invalidateNear(Location changed) {
        if (changed.getWorld() == null) return;
        for (Daycare daycare : plugin.getDaycareManager().all()) {
            Location daycareLoc = daycare.toLocation();
            if (daycareLoc == null || daycareLoc.getWorld() == null) continue;
            if (!daycareLoc.getWorld().equals(changed.getWorld())) continue;

            int radius = plugin.getBreedingEngine().scanner().scanRadiusFor(daycare) + 1;
            if (Math.abs(daycareLoc.getBlockX() - changed.getBlockX()) <= radius
                    && Math.abs(daycareLoc.getBlockY() - changed.getBlockY()) <= radius
                    && Math.abs(daycareLoc.getBlockZ() - changed.getBlockZ()) <= radius) {
                plugin.getBreedingEngine().invalidateEnvCache(daycare);
            }
        }
    }
}
