package gg.hatchery.commands;

import gg.hatchery.Hatchery;
import gg.hatchery.daycare.Daycare;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class HatcheryAdminCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBS = Arrays.asList(
            "reload", "list", "give-hourglass", "give-upgrade", "force-egg", "remove");

    private final Hatchery plugin;
    public HatcheryAdminCommand(Hatchery plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("hatchery.admin")) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "Hatchery v" + plugin.getDescription().getVersion());
            sender.sendMessage(ChatColor.GRAY + "Subcommands: " + String.join(", ", SUBS));
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "reload":
                return reload(sender);
            case "list":
                return list(sender);
            case "give-hourglass":
                return giveHourglass(sender, args);
            case "give-upgrade":
                return giveUpgrade(sender, args);
            case "force-egg":
                return forceEgg(sender, args);
            case "remove":
                return remove(sender, args);
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand: " + args[0]);
                return true;
        }
    }

    private boolean reload(CommandSender sender) {
        try {
            plugin.getConfigManager().reload();
            sender.sendMessage(ChatColor.GREEN + "Configs reloaded.");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Reload failed: " + e.getMessage());
        }
        return true;
    }

    private boolean list(CommandSender sender) {
        int n = plugin.getDaycareManager().all().size();
        sender.sendMessage(ChatColor.YELLOW + "Total daycares: " + n);
        for (Daycare daycare : plugin.getDaycareManager().all()) {
            sender.sendMessage(ChatColor.GRAY + daycare.getId().toString()
                    + ChatColor.DARK_GRAY + " | " + ChatColor.AQUA + ownerName(daycare.getOwner())
                    + ChatColor.DARK_GRAY + " | " + ChatColor.WHITE
                    + daycare.getWorldName() + " (" + daycare.getX() + ", "
                    + daycare.getY() + ", " + daycare.getZ() + ")"
                    + ChatColor.DARK_GRAY + " | " + ChatColor.YELLOW
                    + "eggs=" + daycare.getEggCount() + ", upgrades=" + daycare.getUpgradeLevel());
        }
        return true;
    }

    private boolean giveHourglass(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /hatchery give-hourglass <player> <tier> [amount]");
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player is not online: " + args[1]);
            return true;
        }
        int amount = parseAmount(args, 3);
        ItemStack item = HatcheryItems.hourglassItem(plugin, args[2], amount);
        if (item == null) {
            sender.sendMessage(ChatColor.RED + "Unknown hourglass tier: " + args[2]);
            return true;
        }
        target.getInventory().addItem(item).values().forEach(leftover ->
                target.getWorld().dropItemNaturally(target.getLocation(), leftover));
        sender.sendMessage(ChatColor.GREEN + "Gave " + amount + " " + args[2]
                + " hourglass(es) to " + target.getName() + ".");
        return true;
    }

    private boolean giveUpgrade(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /hatchery give-upgrade <player> [amount]");
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player is not online: " + args[1]);
            return true;
        }
        int amount = parseAmount(args, 2);
        ItemStack item = HatcheryItems.upgradeItem(plugin, amount);
        target.getInventory().addItem(item).values().forEach(leftover ->
                target.getWorld().dropItemNaturally(target.getLocation(), leftover));
        sender.sendMessage(ChatColor.GREEN + "Gave " + amount + " upgrade item(s) to " + target.getName() + ".");
        return true;
    }

    private boolean forceEgg(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /hatchery force-egg <player|daycare-id>");
            return true;
        }
        Daycare daycare = resolveDaycare(args[1]);
        if (daycare == null) {
            sender.sendMessage(ChatColor.RED + "No matching daycare found.");
            return true;
        }
        if (!plugin.getBreedingEngine().forceEgg(daycare)) {
            sender.sendMessage(ChatColor.RED + "Could not force an egg for that daycare.");
            return true;
        }
        sender.sendMessage(ChatColor.GREEN + "Forced egg for daycare " + daycare.getId() + ".");
        return true;
    }

    private boolean remove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /hatchery remove <daycare-id>");
            return true;
        }
        Daycare daycare;
        try {
            daycare = plugin.getDaycareManager().get(UUID.fromString(args[1]));
        } catch (IllegalArgumentException e) {
            daycare = null;
        }
        if (daycare == null) {
            sender.sendMessage(ChatColor.RED + "No daycare with id: " + args[1]);
            return true;
        }
        plugin.getDaycareManager().unregister(daycare);
        plugin.getBreedingEngine().invalidateEnvCache(daycare);
        sender.sendMessage(ChatColor.GREEN + "Removed daycare " + daycare.getId() + ".");
        return true;
    }

    private Daycare resolveDaycare(String token) {
        try {
            Daycare byId = plugin.getDaycareManager().get(UUID.fromString(token));
            if (byId != null) return byId;
        } catch (IllegalArgumentException ignored) {
        }

        Player online = Bukkit.getPlayerExact(token);
        UUID ownerId = online == null ? Bukkit.getOfflinePlayer(token).getUniqueId() : online.getUniqueId();
        List<Daycare> owned = plugin.getDaycareManager().ownedBy(ownerId);
        return owned.isEmpty() ? null : owned.get(0);
    }

    private int parseAmount(String[] args, int index) {
        if (args.length <= index) return 1;
        try {
            return Math.max(1, Integer.parseInt(args[index]));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private String ownerName(UUID owner) {
        Player online = Bukkit.getPlayer(owner);
        if (online != null) return online.getName();
        OfflinePlayer offline = Bukkit.getOfflinePlayer(owner);
        return offline.getName() == null ? owner.toString() : offline.getName();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!sender.hasPermission("hatchery.admin")) return Collections.emptyList();
        if (args.length == 1) {
            return matching(SUBS, args[0]);
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        if (args.length == 2) {
            if (sub.equals("give-hourglass") || sub.equals("give-upgrade") || sub.equals("force-egg")) {
                List<String> names = new ArrayList<>();
                for (Player player : Bukkit.getOnlinePlayers()) names.add(player.getName());
                if (sub.equals("force-egg")) {
                    for (Daycare daycare : plugin.getDaycareManager().all()) names.add(daycare.getId().toString());
                }
                return matching(names, args[1]);
            }
            if (sub.equals("remove")) {
                List<String> ids = new ArrayList<>();
                for (Daycare daycare : plugin.getDaycareManager().all()) ids.add(daycare.getId().toString());
                return matching(ids, args[1]);
            }
        }
        if (args.length == 3 && sub.equals("give-hourglass")) {
            return matching(new ArrayList<>(plugin.getConfigManager().getHourglasses().getAll().keySet()), args[2]);
        }
        return Collections.emptyList();
    }

    private List<String> matching(List<String> values, String prefix) {
        String lower = prefix.toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        for (String value : values) {
            if (value.toLowerCase(Locale.ROOT).startsWith(lower)) out.add(value);
        }
        return out;
    }
}
