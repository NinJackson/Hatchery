package gg.hatchery.commands;

import gg.hatchery.Hatchery;
import gg.hatchery.daycare.Daycare;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class DaycaresCommand implements CommandExecutor {

    private final Hatchery plugin;
    public DaycaresCommand(Hatchery plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Players only.");
            return true;
        }
        Player p = (Player) sender;
        List<Daycare> owned = plugin.getDaycareManager().ownedBy(p.getUniqueId());
        int max = plugin.getDaycareManager().maxDaycaresFor(p);

        p.sendMessage(plugin.getConfigManager().getMessages().getRaw("gui.title")
                + ChatColor.GRAY + " — " + ChatColor.WHITE + owned.size() + ChatColor.GRAY + " / "
                + ChatColor.WHITE + max);
        if (owned.isEmpty()) {
            p.sendMessage(ChatColor.GRAY + "You don't own any daycares yet. Place a ranch block to register one.");
            return true;
        }
        int i = 1;
        for (Daycare d : owned) {
            p.sendMessage(ChatColor.YELLOW + "#" + i++ + ChatColor.GRAY + "  "
                    + d.getWorldName() + " (" + d.getX() + ", " + d.getY() + ", " + d.getZ() + ")  "
                    + ChatColor.AQUA + "eggs: " + d.getEggCount());
        }
        return true;
    }
}
