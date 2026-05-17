package gg.hatchery.breeding;

import gg.hatchery.Hatchery;
import gg.hatchery.config.EnvironmentConfig;
import gg.hatchery.config.MainConfig;
import gg.hatchery.daycare.Daycare;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Locale;

/**
 * Scans the area around a daycare and computes environment points
 * for a given Pokemon (or pair). Sync — call on main thread.
 */
public class EnvironmentScanner {

    private final Hatchery plugin;

    public EnvironmentScanner(Hatchery plugin) { this.plugin = plugin; }

    public int scanRadiusFor(Daycare d) {
        MainConfig m = plugin.getConfigManager().getMain();
        return m.getBaseScanRadius() + d.getUpgradeLevel() * m.getUpgradeRadiusPerLevel();
    }

    /** Sum of points for a list of Pokemon types (e.g. ["water","grass"]). */
    public int totalPoints(Daycare d, Iterable<String> types) {
        Location origin = d.toLocation();
        if (origin == null) return 0;
        World w = origin.getWorld();
        if (w == null) return 0;

        int r = scanRadiusFor(d);
        EnvironmentConfig env = plugin.getConfigManager().getEnvironment();

        int total = 0;
        int ox = origin.getBlockX(), oy = origin.getBlockY(), oz = origin.getBlockZ();
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                for (int dy = -2; dy <= 2; dy++) {
                    Block b = w.getBlockAt(ox + dx, oy + dy, oz + dz);
                    if (b.getType() == Material.AIR) continue;
                    String blockId = idOf(b);
                    for (String t : types) {
                        total += env.pointsFor(t, blockId);
                    }
                }
            }
        }
        return total;
    }

    /** Returns a Minecraft/Pixelmon namespaced ID for a block. */
    public static String idOf(Block b) {
        // Bukkit gives us the namespaced material via getType().getKey() for vanilla blocks.
        // For Pixelmon blocks running under Arclight we still see a Material key with namespace "pixelmon:".
        return b.getType().getKey().toString().toLowerCase(Locale.ROOT);
    }
}
