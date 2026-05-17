package gg.hatchery.listeners;

import gg.hatchery.Hatchery;
import org.bukkit.event.Listener;

/**
 * Recomputes environment points for daycares affected by nearby block changes.
 * Current implementation is a no-op; BreedingEngine scans every tick anyway.
 * TODO: cache env points per-daycare and invalidate only on block changes within scan radius.
 */
public class BlockChangeListener implements Listener {
    @SuppressWarnings("unused")
    private final Hatchery plugin;
    public BlockChangeListener(Hatchery plugin) { this.plugin = plugin; }
}
