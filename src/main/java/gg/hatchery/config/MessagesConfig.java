package gg.hatchery.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.Map;

public class MessagesConfig {

    private final String prefix;
    private final Map<String, String> messages = new HashMap<>();

    public MessagesConfig(YamlConfiguration y) {
        this.prefix = color(y.getString("prefix", ""));
        // Flatten all string keys
        flatten("", y.getValues(true));
    }

    @SuppressWarnings("unchecked")
    private void flatten(String prefixPath, Map<String, Object> values) {
        for (Map.Entry<String, Object> e : values.entrySet()) {
            String key = prefixPath.isEmpty() ? e.getKey() : prefixPath + "." + e.getKey();
            Object v = e.getValue();
            if (v instanceof Map) {
                flatten(key, (Map<String, Object>) v);
            } else if (v instanceof String) {
                messages.put(key, color((String) v));
            }
        }
    }

    public String raw(String key) {
        return messages.getOrDefault(key, "&c<missing message: " + key + ">");
    }

    /** Returns the message with prefix prepended (and {placeholder} replacements applied). */
    public String get(String key, Object... placeholders) {
        return prefix + applyPlaceholders(raw(key), placeholders);
    }

    /** Returns the message WITHOUT the prefix (for GUI lines / lore). */
    public String getRaw(String key, Object... placeholders) {
        return applyPlaceholders(raw(key), placeholders);
    }

    private String applyPlaceholders(String s, Object... placeholders) {
        if (placeholders == null || placeholders.length == 0) return s;
        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            s = s.replace("{" + placeholders[i] + "}", String.valueOf(placeholders[i + 1]));
        }
        return s;
    }

    public String getPrefix() { return prefix; }

    public static String color(String s) {
        if (s == null) return "";
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
