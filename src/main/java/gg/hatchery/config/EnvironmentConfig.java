package gg.hatchery.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EnvironmentConfig {

    /** type-id (lowercase) -> { block-id -> points } */
    private final Map<String, Map<String, Integer>> typePoints = new HashMap<>();

    /** Universal bonuses applied to all types. */
    private final Map<String, Integer> universalBonuses = new HashMap<>();

    public EnvironmentConfig(YamlConfiguration y) {
        ConfigurationSection types = y.getConfigurationSection("type-points");
        if (types != null) {
            for (String type : types.getKeys(false)) {
                ConfigurationSection s = types.getConfigurationSection(type);
                if (s == null) continue;
                Map<String, Integer> blocks = new HashMap<>();
                for (String key : s.getKeys(false)) {
                    blocks.put(key.toLowerCase(Locale.ROOT), s.getInt(key, 0));
                }
                typePoints.put(type.toLowerCase(Locale.ROOT), blocks);
            }
        }
        ConfigurationSection uni = y.getConfigurationSection("universal-bonuses");
        if (uni != null) {
            for (String key : uni.getKeys(false)) {
                universalBonuses.put(key.toLowerCase(Locale.ROOT), uni.getInt(key, 0));
            }
        }
    }

    public int pointsFor(String pokemonType, String blockId) {
        int total = universalBonuses.getOrDefault(blockId.toLowerCase(Locale.ROOT), 0);
        Map<String, Integer> typeMap = typePoints.get(pokemonType.toLowerCase(Locale.ROOT));
        if (typeMap != null) {
            total += typeMap.getOrDefault(blockId.toLowerCase(Locale.ROOT), 0);
        }
        return total;
    }

    public Map<String, Map<String, Integer>> getTypePoints() { return typePoints; }
    public Map<String, Integer> getUniversalBonuses()        { return universalBonuses; }
}
