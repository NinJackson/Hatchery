package gg.hatchery.daycare;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

/**
 * Persistent state for a single Pixelmon Day Care block.
 * Pokemon pair is stored as opaque JSON written/read by PixelmonHook.
 */
public class Daycare {

    private final UUID id;
    private final UUID owner;
    private final String worldName;
    private final int x, y, z;

    private int upgradeLevel;
    private int progressPoints;
    private int eggCount;
    private String pairJson;   // serialized by PixelmonHook (null if no pair)

    public Daycare(UUID id, UUID owner, String worldName, int x, int y, int z) {
        this.id = id;
        this.owner = owner;
        this.worldName = worldName;
        this.x = x; this.y = y; this.z = z;
    }

    public static Daycare fromRow(UUID id, UUID owner, String world,
                                  int x, int y, int z,
                                  int upgrades, int points, int eggs, String pairJson) {
        Daycare d = new Daycare(id, owner, world, x, y, z);
        d.upgradeLevel   = upgrades;
        d.progressPoints = points;
        d.eggCount       = eggs;
        d.pairJson       = pairJson;
        return d;
    }

    public Location toLocation() {
        World w = Bukkit.getWorld(worldName);
        return w == null ? null : new Location(w, x, y, z);
    }

    public boolean isChunkLoaded() {
        World w = Bukkit.getWorld(worldName);
        if (w == null) return false;
        return w.isChunkLoaded(x >> 4, z >> 4);
    }

    public UUID   getId()             { return id; }
    public UUID   getOwner()          { return owner; }
    public String getWorldName()      { return worldName; }
    public int    getX()              { return x; }
    public int    getY()              { return y; }
    public int    getZ()              { return z; }
    public int    getUpgradeLevel()   { return upgradeLevel; }
    public int    getProgressPoints() { return progressPoints; }
    public int    getEggCount()       { return eggCount; }
    public String getPairJson()       { return pairJson; }

    public void setUpgradeLevel(int v)   { this.upgradeLevel   = v; }
    public void setProgressPoints(int v) { this.progressPoints = v; }
    public void setEggCount(int v)       { this.eggCount       = v; }
    public void setPairJson(String s)    { this.pairJson       = s; }

    public void addProgress(int p) {
        this.progressPoints += p;
        if (this.progressPoints < 0) this.progressPoints = 0;
    }
}
