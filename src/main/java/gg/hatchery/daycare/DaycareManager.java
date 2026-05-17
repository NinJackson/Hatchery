package gg.hatchery.daycare;

import gg.hatchery.Hatchery;
import gg.hatchery.config.MainConfig;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DaycareManager {

    private final Hatchery plugin;
    private final Map<UUID, Daycare> byId   = new ConcurrentHashMap<>();
    private final Map<String, Daycare> byLoc = new ConcurrentHashMap<>();   // world:x:y:z

    public DaycareManager(Hatchery plugin) { this.plugin = plugin; }

    public void loadAll() {
        for (Daycare d : plugin.getStorage().loadAllDaycares()) {
            byId.put(d.getId(), d);
            byLoc.put(locKey(d.getWorldName(), d.getX(), d.getY(), d.getZ()), d);
        }
        plugin.getLogger().info("Loaded " + byId.size() + " daycare(s).");
    }

    public void saveAll() {
        for (Daycare d : byId.values()) plugin.getStorage().saveDaycare(d);
    }

    public Daycare atLocation(Location loc) {
        if (loc.getWorld() == null) return null;
        return byLoc.get(locKey(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
    }

    public List<Daycare> ownedBy(UUID owner) {
        List<Daycare> out = new ArrayList<>();
        for (Daycare d : byId.values()) if (d.getOwner().equals(owner)) out.add(d);
        return out;
    }

    public Daycare register(UUID owner, Location loc) {
        UUID id = UUID.randomUUID();
        Daycare d = new Daycare(id, owner, loc.getWorld().getName(),
                loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        byId.put(id, d);
        byLoc.put(locKey(d.getWorldName(), d.getX(), d.getY(), d.getZ()), d);
        plugin.getStorage().saveDaycare(d);
        return d;
    }

    public void unregister(Daycare d) {
        byId.remove(d.getId());
        byLoc.remove(locKey(d.getWorldName(), d.getX(), d.getY(), d.getZ()));
        plugin.getStorage().deleteDaycare(d.getId());
    }

    public int maxDaycaresFor(Player p) {
        MainConfig main = plugin.getConfigManager().getMain();
        int base = main.getMaxPerPlayerDefault();
        String metaKey = main.getPermissionMetaKey();
        // LuckPerms meta lookup
        try {
            LuckPerms lp = LuckPermsProvider.get();
            User u = lp.getUserManager().getUser(p.getUniqueId());
            if (u != null) {
                CachedMetaData md = u.getCachedData().getMetaData();
                String v = md.getMetaValue(metaKey);
                if (v != null) {
                    try { return Math.max(base, Integer.parseInt(v)); } catch (NumberFormatException ignored) {}
                }
            }
        } catch (Throwable ignored) {
            // LuckPerms not present or query failed
        }
        return base;
    }

    public boolean isWorldBlacklisted(String world) {
        return containsIgnoreCase(plugin.getConfigManager().getMain().getBlacklistedWorlds(), world);
    }

    public boolean isVanillaBreedingWorld(String world) {
        return containsIgnoreCase(plugin.getConfigManager().getMain().getVanillaBreedingWorlds(), world);
    }

    public Collection<Daycare> all() { return byId.values(); }

    public Daycare get(UUID id) { return byId.get(id); }

    private boolean containsIgnoreCase(Collection<String> values, String needle) {
        if (needle == null) return false;
        for (String value : values) {
            if (needle.equalsIgnoreCase(value)) return true;
        }
        return false;
    }

    private static String locKey(String world, int x, int y, int z) {
        return world + ":" + x + ":" + y + ":" + z;
    }
}
