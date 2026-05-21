package gg.hatchery.config;

import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public class MainConfig {

    /* storage */
    private final String storageType;
    private final String storageFile;
    private final String mysqlHost;
    private final int    mysqlPort;
    private final String mysqlDatabase;
    private final String mysqlUser;
    private final String mysqlPassword;

    /* breeding */
    private final int    tickIntervalSeconds;
    private final int    basePointsNeeded;
    private final int    maxEggsPerDaycare;

    /* daycare */
    private final Set<String> daycareBlocks;     // normalized lowercase namespaced IDs
    private final int    maxPerPlayerDefault;
    private final String permissionMetaKey;
    private final int    baseScanRadius;

    /* upgrade */
    private final String upgradeItem;
    private final String upgradeDisplayName;
    private final List<String> upgradeLore;
    private final int    upgradeRadiusPerLevel;
    private final int    upgradeMaxLevels;
    private final boolean upgradeDropOnBreak;

    /* worlds */
    private final List<String> blacklistedWorlds;
    private final List<String> vanillaBreedingWorlds;

    /* satisfaction */
    private final List<SatisfactionLevel> satisfactionLevels;

    /* particles */
    private final ParticleConfig breedingParticle;
    private final ParticleConfig eggReadyParticle;

    /* gui */
    private final String guiFillerItem;

    public MainConfig(YamlConfiguration y) {
        this.storageType     = y.getString("storage.type", "sqlite");
        this.storageFile     = y.getString("storage.file", "data.db");
        this.mysqlHost       = y.getString("storage.mysql.host", "127.0.0.1");
        this.mysqlPort       = y.getInt   ("storage.mysql.port", 3306);
        this.mysqlDatabase   = y.getString("storage.mysql.database", "hatchery");
        this.mysqlUser       = y.getString("storage.mysql.user", "hatchery");
        this.mysqlPassword   = y.getString("storage.mysql.password", "");

        this.tickIntervalSeconds = y.getInt("breeding.tick-interval-seconds", 60);
        this.basePointsNeeded    = y.getInt("breeding.base-points-needed", 100);
        this.maxEggsPerDaycare   = y.getInt("breeding.max-eggs-per-daycare", 1);

        // ---- daycare.blocks (list)  with legacy fallback to daycare.block (single) ----
        Set<String> blocks = new LinkedHashSet<>();
        List<String> listed = y.getStringList("daycare.blocks");
        if (listed != null) {
            for (String s : listed) {
                if (s != null && !s.isEmpty()) blocks.add(s.toLowerCase(Locale.ROOT));
            }
        }
        String legacy = y.getString("daycare.block", null);
        if (legacy != null && !legacy.isEmpty()) blocks.add(legacy.toLowerCase(Locale.ROOT));
        if (blocks.isEmpty()) {
            // Fall back to all 16 Pixelmon day_care colour variants.
            for (String c : DEFAULT_DAYCARE_COLOURS) {
                blocks.add("pixelmon:" + c + "_day_care");
            }
        }
        this.daycareBlocks = Collections.unmodifiableSet(blocks);

        this.maxPerPlayerDefault = y.getInt   ("daycare.max-per-player-default", 1);
        this.permissionMetaKey   = y.getString("daycare.permission-meta-key", "hatchery.maxdaycares");
        this.baseScanRadius      = y.getInt   ("daycare.base-scan-radius", 5);

        this.upgradeItem         = y.getString("daycare.upgrade.item", "minecraft:diamond_block");
        this.upgradeDisplayName  = y.getString("daycare.upgrade.display-name", "&b&lDaycare Environment Upgrade");
        this.upgradeLore         = y.getStringList("daycare.upgrade.lore");
        this.upgradeRadiusPerLevel = y.getInt("daycare.upgrade.radius-per-level", 2);
        this.upgradeMaxLevels    = y.getInt   ("daycare.upgrade.max-upgrades", 5);
        this.upgradeDropOnBreak  = y.getBoolean("daycare.upgrade.drop-on-break", true);

        this.blacklistedWorlds      = y.getStringList("worlds.blacklisted");
        this.vanillaBreedingWorlds  = y.getStringList("worlds.use-vanilla-pixelmon");

        this.satisfactionLevels = loadSatisfactionLevels(y);

        this.breedingParticle = new ParticleConfig(y.getConfigurationSection("particles.during-breeding"));
        this.eggReadyParticle = new ParticleConfig(y.getConfigurationSection("particles.egg-ready"));

        this.guiFillerItem = y.getString("gui.filler-item", "minecraft:black_stained_glass_pane");
    }

    private List<SatisfactionLevel> loadSatisfactionLevels(YamlConfiguration y) {
        List<SatisfactionLevel> list = new ArrayList<>();
        List<?> raw = y.getList("satisfaction-levels");
        if (raw == null) return list;
        for (Object o : raw) {
            if (!(o instanceof java.util.Map)) continue;
            java.util.Map<?, ?> m = (java.util.Map<?, ?>) o;
            list.add(new SatisfactionLevel(
                    Integer.parseInt(Objects.toString(m.get("threshold"), "0")),
                    Objects.toString(m.get("name"), "Unknown"),
                    Objects.toString(m.get("color"), "&7"),
                    Double.parseDouble(Objects.toString(m.get("speed-mult"), "1.0"))
            ));
        }
        Collections.sort(list, (a, b) -> Integer.compare(a.threshold, b.threshold));
        return list;
    }

    public SatisfactionLevel resolveSatisfaction(int points) {
        SatisfactionLevel current = satisfactionLevels.isEmpty()
                ? new SatisfactionLevel(0, "Unknown", "&7", 1.0)
                : satisfactionLevels.get(0);
        for (SatisfactionLevel lv : satisfactionLevels) {
            if (points >= lv.threshold) current = lv;
            else break;
        }
        return current;
    }

    public String getStorageType()           { return storageType; }
    public String getStorageFile()           { return storageFile; }
    public String getMysqlHost()             { return mysqlHost; }
    public int    getMysqlPort()             { return mysqlPort; }
    public String getMysqlDatabase()         { return mysqlDatabase; }
    public String getMysqlUser()             { return mysqlUser; }
    public String getMysqlPassword()         { return mysqlPassword; }
    public int    getTickIntervalSeconds()   { return tickIntervalSeconds; }
    public int    getBasePointsNeeded()      { return basePointsNeeded; }
    public int    getMaxEggsPerDaycare()     { return maxEggsPerDaycare; }
    public Set<String> getDaycareBlocks()    { return daycareBlocks; }
    /** Legacy single-block accessor — returns the first configured block id. */
    @Deprecated
    public String getDaycareBlock()          { return daycareBlocks.isEmpty() ? "" : daycareBlocks.iterator().next(); }
    public int    getMaxPerPlayerDefault()   { return maxPerPlayerDefault; }
    public String getPermissionMetaKey()     { return permissionMetaKey; }
    public int    getBaseScanRadius()        { return baseScanRadius; }
    public String getUpgradeItem()           { return upgradeItem; }
    public String getUpgradeDisplayName()    { return upgradeDisplayName; }
    public List<String> getUpgradeLore()     { return upgradeLore; }
    public int    getUpgradeRadiusPerLevel() { return upgradeRadiusPerLevel; }
    public int    getUpgradeMaxLevels()      { return upgradeMaxLevels; }
    public boolean isUpgradeDropOnBreak()    { return upgradeDropOnBreak; }
    public List<String> getBlacklistedWorlds()     { return blacklistedWorlds; }
    public List<String> getVanillaBreedingWorlds() { return vanillaBreedingWorlds; }
    public List<SatisfactionLevel> getSatisfactionLevels() { return satisfactionLevels; }
    public ParticleConfig getBreedingParticle() { return breedingParticle; }
    public ParticleConfig getEggReadyParticle() { return eggReadyParticle; }
    public String getGuiFillerItem()        { return guiFillerItem; }

    public static final class SatisfactionLevel {
        public final int    threshold;
        public final String name;
        public final String color;
        public final double speedMultiplier;
        public SatisfactionLevel(int threshold, String name, String color, double mult) {
            this.threshold = threshold;
            this.name = name;
            this.color = color;
            this.speedMultiplier = mult;
        }
    }

    public static final class ParticleConfig {
        public final boolean enabled;
        public final Particle type;
        public final int count;
        public final int intervalSeconds;
        public final double offsetX, offsetY, offsetZ;
        public ParticleConfig(ConfigurationSection s) {
            if (s == null) {
                enabled = false; type = Particle.HEART;
                count = 0; intervalSeconds = 0;
                offsetX = offsetY = offsetZ = 0;
                return;
            }
            this.enabled         = s.getBoolean("enabled", true);
            Particle p;
            try { p = Particle.valueOf(s.getString("type", "HEART").toUpperCase(Locale.ROOT)); }
            catch (Exception e) { p = Particle.HEART; }
            this.type            = p;
            this.count           = s.getInt("count", 3);
            this.intervalSeconds = s.getInt("interval-seconds", 5);
            this.offsetX         = s.getDouble("offset.x", 0.5);
            this.offsetY         = s.getDouble("offset.y", 1.0);
            this.offsetZ         = s.getDouble("offset.z", 0.5);
        }
    }

    /** Default Pixelmon day_care block colours (Pixelmon 9.x). */
    private static final String[] DEFAULT_DAYCARE_COLOURS = {
            "black", "blue", "brown", "cyan",
            "gray", "green", "light_blue", "light_gray",
            "lime", "magenta", "orange", "pink",
            "purple", "red", "white", "yellow"
    };
}
