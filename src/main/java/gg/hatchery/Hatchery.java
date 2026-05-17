package gg.hatchery;

import gg.hatchery.breeding.BreedingEngine;
import gg.hatchery.commands.DaycaresCommand;
import gg.hatchery.commands.HatcheryAdminCommand;
import gg.hatchery.config.ConfigManager;
import gg.hatchery.daycare.DaycareManager;
import gg.hatchery.listeners.BlockChangeListener;
import gg.hatchery.listeners.DaycareInteractListener;
import gg.hatchery.listeners.DaycareLifecycleListener;
import gg.hatchery.listeners.InventoryClickListener;
import gg.hatchery.pixelmon.PixelmonHook;
import gg.hatchery.storage.Storage;
import gg.hatchery.storage.SqliteStorage;
import gg.hatchery.storage.MysqlStorage;
import gg.hatchery.ui.MenuManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class Hatchery extends JavaPlugin {

    private static Hatchery instance;

    private ConfigManager configManager;
    private Storage storage;
    private DaycareManager daycareManager;
    private BreedingEngine breedingEngine;
    private PixelmonHook pixelmonHook;
    private MenuManager menuManager;

    @Override
    public void onEnable() {
        instance = this;

        // --- Config ---
        this.configManager = new ConfigManager(this);
        try {
            configManager.loadAll();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to load configuration. Disabling.", e);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // --- Storage ---
        String type = configManager.getMain().getStorageType();
        try {
            this.storage = "mysql".equalsIgnoreCase(type)
                    ? new MysqlStorage(this, configManager.getMain())
                    : new SqliteStorage(this);
            this.storage.init();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to initialize storage backend (" + type + "). Disabling.", e);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // --- Pixelmon hook ---
        this.pixelmonHook = new PixelmonHook(this);
        if (!pixelmonHook.isPixelmonPresent()) {
            getLogger().severe("Pixelmon is not loaded. Hatchery requires Pixelmon to function. Disabling.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // --- Managers ---
        this.daycareManager = new DaycareManager(this);
        this.daycareManager.loadAll();

        this.menuManager = new MenuManager();

        this.breedingEngine = new BreedingEngine(this);
        this.breedingEngine.start();

        // --- Listeners ---
        Bukkit.getPluginManager().registerEvents(new DaycareLifecycleListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BlockChangeListener(this), this);
        Bukkit.getPluginManager().registerEvents(new DaycareInteractListener(this), this);
        Bukkit.getPluginManager().registerEvents(new InventoryClickListener(this), this);

        // --- Commands ---
        getCommand("daycares").setExecutor(new DaycaresCommand(this));
        HatcheryAdminCommand admin = new HatcheryAdminCommand(this);
        getCommand("hatchery").setExecutor(admin);
        getCommand("hatchery").setTabCompleter(admin);

        getLogger().info("Hatchery enabled (storage=" + type + ").");
    }

    @Override
    public void onDisable() {
        if (breedingEngine != null) breedingEngine.stop();
        if (daycareManager != null) daycareManager.saveAll();
        if (storage != null) storage.close();
        instance = null;
    }

    public static Hatchery get() {
        return instance;
    }

    public ConfigManager getConfigManager()   { return configManager; }
    public Storage         getStorage()         { return storage; }
    public DaycareManager  getDaycareManager()  { return daycareManager; }
    public BreedingEngine  getBreedingEngine()  { return breedingEngine; }
    public PixelmonHook    getPixelmonHook()    { return pixelmonHook; }
    public MenuManager     getMenuManager()     { return menuManager; }
}
