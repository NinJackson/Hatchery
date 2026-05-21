package gg.hatchery.daycare;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import gg.hatchery.pixelmon.PokemonNbtCodec;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Objects;
import java.util.UUID;

/**
 * Persistent state for a single ranch-block daycare.
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
    private transient boolean dirty;
    private transient Pokemon[] decodedPair;
    private transient String decodedFromPairJson;
    private transient long lastBreedingParticleTick = Long.MIN_VALUE;

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
    public boolean isDirty()          { return dirty; }
    public long getLastBreedingParticleTick() { return lastBreedingParticleTick; }

    public void setUpgradeLevel(int v) {
        if (this.upgradeLevel != v) {
            this.upgradeLevel = v;
            markDirty();
        }
    }

    public void setProgressPoints(int v) {
        int clamped = Math.max(0, v);
        if (this.progressPoints != clamped) {
            this.progressPoints = clamped;
            markDirty();
        }
    }

    public void setEggCount(int v) {
        int clamped = Math.max(0, v);
        if (this.eggCount != clamped) {
            this.eggCount = clamped;
            markDirty();
        }
    }

    public void setPairJson(String s) {
        if (!Objects.equals(this.pairJson, s)) {
            this.pairJson = s;
            this.decodedPair = null;
            this.decodedFromPairJson = null;
            markDirty();
        }
    }

    public void markDirty() {
        this.dirty = true;
    }

    public void clearDirty() {
        this.dirty = false;
    }

    public void setLastBreedingParticleTick(long tick) {
        this.lastBreedingParticleTick = tick;
    }

    public void addProgress(int p) {
        setProgressPoints(this.progressPoints + p);
    }

    public Pokemon[] decodedPair() {
        if (pairJson == null) return new Pokemon[]{null, null};
        if (pairJson.equals(decodedFromPairJson) && decodedPair != null) return decodedPair;
        decodedPair = PokemonNbtCodec.decodePair(pairJson);
        decodedFromPairJson = pairJson;
        return decodedPair;
    }
}
