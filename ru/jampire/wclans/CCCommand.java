package ru.jampire.wclans;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CCCommand implements CommandExecutor
{
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Lang.getMessage("command_error0"));
            return true;
        }
        final Clan userClan = Clan.getClanByName(sender.getName());
        if (userClan == null) {
            sender.sendMessage(Lang.getMessage("command_error7"));
            return true;
        }
        ChatColor c = ChatColor.AQUA;
        if (userClan.isModer(sender.getName())) {
            c = ChatColor.GREEN;
        }
        if (userClan.hasLeader(sender.getName())) {
            c = ChatColor.GOLD;
        }
        final StringBuilder msg = new StringBuilder();
        for (final String arg : args) {
            msg.append(String.valueOf(arg) + " ");
        }
        for (final Member m : userClan.getMembers()) {
            final Player pl = Bukkit.getPlayerExact(m.getName());
            if (pl != null) {
                pl.getPlayer().sendMessage(Lang.getMessage("clanchat_format", Lang.getMessage("clan"), c + sender.getName(), c + msg.toString()));
            }
        }
        return true;
    }
}
