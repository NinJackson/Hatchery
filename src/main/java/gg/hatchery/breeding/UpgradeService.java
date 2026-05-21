package gg.hatchery.breeding;

import gg.hatchery.Hatchery;
import gg.hatchery.config.MainConfig;
import gg.hatchery.daycare.Daycare;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class UpgradeService {

    private final Hatchery plugin;

    public UpgradeService(Hatchery plugin) {
        this.plugin = plugin;
    }

    public void tryApply(Player player, Daycare daycare, ItemStack held) {
        MainConfig main = plugin.getConfigManager().getMain();
        if (daycare.getUpgradeLevel() >= main.getUpgradeMaxLevels()) {
            player.sendMessage(plugin.getConfigManager().getMessages().get("upgrade.max-reached"));
            return;
        }

        daycare.setUpgradeLevel(daycare.getUpgradeLevel() + 1);
        plugin.getBreedingEngine().invalidateEnvCache(daycare);
        plugin.getStorage().saveDaycare(daycare);
        if (held != null) {
            held.setAmount(Math.max(0, held.getAmount() - 1));
        }
        plugin.getMenuManager().rebuildDaycare(daycare);
        player.sendMessage(plugin.getConfigManager().getMessages().get("upgrade.applied",
                "radius", plugin.getBreedingEngine().scanner().scanRadiusFor(daycare)));
    }
}
