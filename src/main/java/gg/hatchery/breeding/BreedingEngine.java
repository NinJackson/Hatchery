package gg.hatchery.breeding;

import gg.hatchery.Hatchery;
import gg.hatchery.config.MainConfig;
import gg.hatchery.daycare.Daycare;
import gg.hatchery.pixelmon.PixelmonHook;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

/**
 * Ticks active daycares: accrues progress, generates eggs, fires particles.
 */
public class BreedingEngine {

    private final Hatchery plugin;
    private final EnvironmentScanner scanner;
    private BukkitTask task;

    public BreedingEngine(Hatchery plugin) {
        this.plugin = plugin;
        this.scanner = new EnvironmentScanner(plugin);
    }

    public EnvironmentScanner scanner() { return scanner; }

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
            if (plugin.getDaycareManager().isVanillaBreedingWorld(d.getWorldName())) continue;
            if (!d.isChunkLoaded())     continue;   // requirement: pause when chunk unloaded
            if (d.getPairJson() == null) continue;   // no active pair
            if (d.getEggCount() >= main.getMaxEggsPerDaycare()) continue;

            List<String> types = hook.getPairTypes(d.getPairJson());
            if (types.isEmpty()) continue;

            int envPoints = scanner.totalPoints(d, types);
            MainConfig.SatisfactionLevel sat = main.resolveSatisfaction(envPoints);

            int gained = (int) Math.max(1, Math.round(envPoints * sat.speedMultiplier / 10.0));
            d.addProgress(gained);

            // Particles during breeding
            MainConfig.ParticleConfig pc = main.getBreedingParticle();
            if (pc.enabled && d.toLocation() != null) {
                d.toLocation().getWorld().spawnParticle(
                        pc.type, d.toLocation().clone().add(pc.offsetX, pc.offsetY, pc.offsetZ),
                        pc.count);
            }

            if (d.getProgressPoints() >= basePoints) {
                d.setProgressPoints(0);
                d.setEggCount(d.getEggCount() + 1);
                hook.notifyEggReady(d);
                MainConfig.ParticleConfig pe = main.getEggReadyParticle();
                if (pe.enabled && d.toLocation() != null) {
                    d.toLocation().getWorld().spawnParticle(
                            pe.type, d.toLocation().clone().add(pe.offsetX, pe.offsetY, pe.offsetZ),
                            pe.count);
                }
            }
            plugin.getStorage().saveDaycare(d);
        }
    }
}
