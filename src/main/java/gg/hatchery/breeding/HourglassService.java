package gg.hatchery.breeding;

import gg.hatchery.Hatchery;
import gg.hatchery.config.HourglassConfig;
import gg.hatchery.daycare.Daycare;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class HourglassService {

    private final Hatchery plugin;

    public HourglassService(Hatchery plugin) {
        this.plugin = plugin;
    }

    public void tryApply(Player player, Daycare daycare, ItemStack held, String tierId) {
        HourglassConfig.Hourglass tier = plugin.getConfigManager().getHourglasses().get(tierId);
        if (tier == null) return;

        if (tier.permission != null && !tier.permission.isEmpty() && !player.hasPermission(tier.permission)) {
            player.sendMessage(plugin.getConfigManager().getMessages().get("hourglass.no-permission"));
            return;
        }
        if (daycare.getPairJson() == null) {
            player.sendMessage(plugin.getConfigManager().getMessages().get("hourglass.no-active-breeding"));
            return;
        }

        daycare.addProgress(tier.ticksAdded);
        plugin.getStorage().saveDaycare(daycare);
        if (tier.consume && held != null) {
            held.setAmount(Math.max(0, held.getAmount() - 1));
        }
        plugin.getMenuManager().rebuildDaycare(daycare);
        player.sendMessage(plugin.getConfigManager().getMessages().get("hourglass.used",
                "ticks", tier.ticksAdded));
    }
}
