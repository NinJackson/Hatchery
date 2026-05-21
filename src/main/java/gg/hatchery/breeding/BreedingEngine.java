package gg.hatchery.breeding;

import gg.hatchery.Hatchery;
import gg.hatchery.config.MainConfig;
import gg.hatchery.daycare.Daycare;
import gg.hatchery.pixelmon.PixelmonHook;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ticks active daycares: accrues progress, generates eggs, fires particles.
 */
public class BreedingEngine {

    private final Hatchery plugin;
    private final EnvironmentScanner scanner;
    private final Map<UUID, Integer> envPointsCache = new ConcurrentHashMap<>();
    private final Map<UUID, String> envPointsKey = new ConcurrentHashMap<>();
    private BukkitTask task;

    public BreedingEngine(Hatchery plugin) {
        this.plugin = plugin;
        this.scanner = new EnvironmentScanner(plugin);
    }

    public EnvironmentScanner scanner() { return scanner; }

    public int envPointsFor(Daycare daycare, List<String> types) {
        String key = String.join(",", types.stream().sorted().toList()) + "@" + daycare.getUpgradeLevel();
        if (!key.equals(envPointsKey.get(daycare.getId()))) {
            envPointsCache.remove(daycare.getId());
            envPointsKey.put(daycare.getId(), key);
        }
        return envPointsCache.computeIfAbsent(daycare.getId(),
                id -> scanner.totalPoints(daycare, types));
    }

    public void invalidateEnvCache(Daycare daycare) {
        envPointsCache.remove(daycare.getId());
    }

    public boolean forceEgg(Daycare daycare) {
        MainConfig main = plugin.getConfigManager().getMain();
        if (daycare.getPairJson() == null) return false;
        if (daycare.getEggCount() >= main.getMaxEggsPerDaycare()) return false;
        if (!plugin.getPixelmonHook().isCompatible(daycare.decodedPair()[0], daycare.decodedPair()[1])) return false;

        daycare.setProgressPoints(0);
        daycare.setEggCount(daycare.getEggCount() + 1);
        plugin.getPixelmonHook().notifyEggReady(daycare);
        plugin.getStorage().saveDaycare(daycare);
        plugin.getMenuManager().rebuildDaycare(daycare);
        return true;
    }

    public void start() {
        int seconds = Math.max(1, plugin.getConfigManager().getMain().getTickIntervalSeconds());
        this.task = plugin.getServer().getScheduler().runTaskTimer(
                plugin, this::tick, 20L * seconds, 20L * seconds);
    }

    public void stop() {
        if (task != null) task.cancel();
    }

    public void tick() {
        MainConfig main = plugin.getConfigManager().getMain();
        PixelmonHook hook = plugin.getPixelmonHook();
        int basePoints = main.getBasePointsNeeded();

        for (Daycare d : plugin.getDaycareManager().all()) {
            tickDaycare(d, main, hook, basePoints);
        }
    }

    private void tickDaycare(Daycare daycare, MainConfig main, PixelmonHook hook, int basePoints) {
        if (!daycare.isChunkLoaded()) return;   // requirement: pause when chunk unloaded
        if (daycare.getPairJson() == null) return;   // no active pair
        if (daycare.getEggCount() >= main.getMaxEggsPerDaycare()) return;

        List<String> types = hook.getPairTypes(daycare);
        if (types.isEmpty()) return;

        int envPoints = envPointsFor(daycare, types);
        MainConfig.SatisfactionLevel sat = main.resolveSatisfaction(envPoints);

        int gained = progressGainFor(sat);
        daycare.addProgress(gained);

        spawnBreedingParticle(daycare, main.getBreedingParticle());

        if (daycare.getProgressPoints() >= basePoints) {
            daycare.setProgressPoints(0);
            daycare.setEggCount(daycare.getEggCount() + 1);
            hook.notifyEggReady(daycare);
            spawnParticle(daycare, main.getEggReadyParticle());
        }

        if (daycare.isDirty()) {
            plugin.getStorage().saveDaycare(daycare);
            plugin.getMenuManager().rebuildDaycare(daycare);
        }
    }

    private int progressGainFor(MainConfig.SatisfactionLevel sat) {
        return Math.max(1, (int) Math.round(sat.speedMultiplier));
    }

    private void spawnBreedingParticle(Daycare daycare, MainConfig.ParticleConfig particle) {
        if (!particle.enabled) return;
        long now = System.currentTimeMillis();
        long intervalMillis = Math.max(0, particle.intervalSeconds) * 1000L;
        if (daycare.getLastBreedingParticleTick() != Long.MIN_VALUE
                && now - daycare.getLastBreedingParticleTick() < intervalMillis) {
            return;
        }
        if (spawnParticle(daycare, particle)) {
            daycare.setLastBreedingParticleTick(now);
        }
    }

    private boolean spawnParticle(Daycare daycare, MainConfig.ParticleConfig particle) {
        if (!particle.enabled) return false;
        Location loc = daycare.toLocation();
        if (loc == null || loc.getWorld() == null) return false;
        loc.getWorld().spawnParticle(
                particle.type, loc.clone().add(particle.offsetX, particle.offsetY, particle.offsetZ),
                particle.count);
        return true;
    }
}
