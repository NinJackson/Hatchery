package gg.hatchery.commands;

import gg.hatchery.Hatchery;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
        switch (args[0].toLowerCase()) {
            case "reload":
                try {
                    plugin.getConfigManager().reload();
                    sender.sendMessage(ChatColor.GREEN + "Configs reloaded.");
                } catch (Exception e) {
                    sender.sendMessage(ChatColor.RED + "Reload failed: " + e.getMessage());
                }
                return true;
            case "list":
                int n = plugin.getDaycareManager().all().size();
                sender.sendMessage(ChatColor.YELLOW + "Total daycares: " + n);
                return true;
            // TODO: implement give-hourglass / give-upgrade / force-egg / remove
            default:
                sender.sendMessage(ChatColor.RED + "Not implemented yet: " + args[0]);
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!sender.hasPermission("hatchery.admin")) return Collections.emptyList();
        if (args.length == 1) {
            List<String> out = new ArrayList<>();
            for (String s : SUBS) if (s.startsWith(args[0].toLowerCase())) out.add(s);
            return out;
        }
        return Collections.emptyList();
    }
}
