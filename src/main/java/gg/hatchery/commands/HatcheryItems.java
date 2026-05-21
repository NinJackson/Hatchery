package gg.hatchery.commands;

import gg.hatchery.Hatchery;
import gg.hatchery.config.HourglassConfig;
import gg.hatchery.config.MainConfig;
import gg.hatchery.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public final class HatcheryItems {

    private HatcheryItems() {}

    public static ItemStack hourglassItem(Hatchery plugin, String tierId, int amount) {
        HourglassConfig.Hourglass tier = plugin.getConfigManager().getHourglasses().get(tierId);
        if (tier == null) return null;
        Material material = resolveMaterial(tier.baseItem, Material.SAND);
        return new ItemBuilder(new ItemStack(material, Math.max(1, amount)))
                .name(tier.displayName)
                .lore(tier.lore)
                .tag(new NamespacedKey(plugin, "hourglass_id"), tierId)
                .build();
    }

    public static ItemStack upgradeItem(Hatchery plugin, int amount) {
        MainConfig main = plugin.getConfigManager().getMain();
        Material material = resolveMaterial(main.getUpgradeItem(), Material.DIAMOND_BLOCK);
        return new ItemBuilder(new ItemStack(material, Math.max(1, amount)))
                .name(main.getUpgradeDisplayName())
                .lore(main.getUpgradeLore())
                .tag(new NamespacedKey(plugin, "upgrade_id"), "primary")
                .build();
    }

    private static Material resolveMaterial(String id, Material fallback) {
        Material material = Material.matchMaterial(id);
        if (material == null && id != null && id.contains(":")) {
            material = Material.matchMaterial(id.substring(id.indexOf(':') + 1));
        }
        return material == null ? fallback : material;
    }
}
