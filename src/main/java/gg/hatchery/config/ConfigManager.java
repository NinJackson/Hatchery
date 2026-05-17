package gg.hatchery.config;

import gg.hatchery.Hatchery;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ConfigManager {

    private final Hatchery plugin;

    private MainConfig         main;
    private EnvironmentConfig  environment;
    private MessagesConfig     messages;
    private HourglassConfig    hourglasses;

    public ConfigManager(Hatchery plugin) {
        this.plugin = plugin;
    }

    public void loadAll() throws IOException {
        plugin.getDataFolder().mkdirs();
        save("config.yml");
        save("environment-points.yml");
        save("messages.yml");
        save("hourglasses.yml");

        this.main        = new MainConfig       (yaml("config.yml"));
        this.environment = new EnvironmentConfig(yaml("environment-points.yml"));
        this.messages    = new MessagesConfig   (yaml("messages.yml"));
        this.hourglasses = new HourglassConfig  (yaml("hourglasses.yml"));
    }

    public void reload() throws IOException {
        loadAll();
    }

    private YamlConfiguration yaml(String filename) {
        File f = new File(plugin.getDataFolder(), filename);
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);
        // Merge defaults from jar resource so new keys appear after upgrade.
        try (InputStream in = plugin.getResource(filename)) {
            if (in != null) {
                cfg.setDefaults(YamlConfiguration.loadConfiguration(
                        new InputStreamReader(in, StandardCharsets.UTF_8)));
                cfg.options().copyDefaults(true);
            }
        } catch (IOException ignored) {}
        return cfg;
    }

    private void save(String filename) throws IOException {
        File out = new File(plugin.getDataFolder(), filename);
        if (out.exists()) return;
        try (InputStream in = plugin.getResource(filename)) {
            if (in == null) throw new IOException("Missing resource: " + filename);
            Files.copy(in, out.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public MainConfig         getMain()         { return main;        }
    public EnvironmentConfig  getEnvironment()  { return environment; }
    public MessagesConfig     getMessages()     { return messages;    }
    public HourglassConfig    getHourglasses()  { return hourglasses; }
}
