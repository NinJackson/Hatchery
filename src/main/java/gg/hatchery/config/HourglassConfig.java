package gg.hatchery.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HourglassConfig {

    public static final class Hourglass {
        public final String  id;
        public final String  baseItem;
        public final String  displayName;
        public final List<String> lore;
        public final int     ticksAdded;
        public final boolean consume;
        public final String  permission;

        public Hourglass(String id, String baseItem, String displayName, List<String> lore,
                         int ticksAdded, boolean consume, String permission) {
            this.id          = id;
            this.baseItem    = baseItem;
            this.displayName = displayName;
            this.lore        = lore;
            this.ticksAdded  = ticksAdded;
            this.consume     = consume;
            this.permission  = permission;
        }
    }

    private final Map<String, Hourglass> hourglasses = new LinkedHashMap<>();

    public HourglassConfig(YamlConfiguration y) {
        ConfigurationSection root = y.getConfigurationSection("hourglasses");
        if (root == null) return;
        for (String id : root.getKeys(false)) {
            ConfigurationSection s = root.getConfigurationSection(id);
            if (s == null) continue;
            hourglasses.put(id, new Hourglass(
                    id,
                    s.getString("base-item", "pixelmon:hourglass"),
                    s.getString("display-name", id),
                    s.getStringList("lore"),
                    s.getInt("ticks-added", 10),
                    s.getBoolean("consume", true),
                    s.getString("permission", "hatchery.hourglass." + id)
            ));
        }
    }

    public Hourglass get(String id) { return hourglasses.get(id); }
    public Map<String, Hourglass> getAll() { return hourglasses; }
    public List<Hourglass> list() { return new ArrayList<>(hourglasses.values()); }
}
