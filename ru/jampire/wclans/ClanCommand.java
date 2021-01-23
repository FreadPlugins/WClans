package ru.jampire.wclans;

import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.*;

import com.comphenix.protocol.events.*;
import com.comphenix.protocol.wrappers.*;
import com.comphenix.protocol.*;

import java.lang.reflect.*;
import java.util.*;

public class ClanCommand implements CommandExecutor
{
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            final Map<Clan, Integer> sorted = new HashMap<Clan, Integer>();
            for (final Clan c2 : Clan.clans.values()) {
                sorted.put(c2, c2.getMembers().size());
            }
            final List<Map.Entry<Clan, Integer>> entries = new LinkedList<Map.Entry<Clan, Integer>>(sorted.entrySet());
            Collections.sort(entries, new Comparator<Map.Entry<Clan, Integer>>() {
                @Override
                public int compare(final Map.Entry<Clan, Integer> o1, final Map.Entry<Clan, Integer> o2) {
                    return o2.getValue().compareTo(o1.getValue());
                }
            });
            int k = 1;
            sender.sendMessage("§8§m----------§r §c§lТоп Кланов §8§m----------");
            for (final Map.Entry<Clan, Integer> entry : entries) {
                final Clan c3 = entry.getKey();
                sender.sendMessage(Lang.getMessage("command_top_1", k, c3.getName(), c3.getLeader(), entry.getValue()));
                if (k == 10) {
                    break;
                }
                ++k;
            }
            return true;
        }
        Clan userClan = Clan.getClanByName(sender.getName());
        final Player user = (Player)sender;
        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + Lang.getMessage("command_up"));
            if (userClan == null) {
                sender.sendMessage(ChatColor.YELLOW + "/" + label + " " + Lang.getMessage("command_create"));
            }
            else {
                if (userClan.hasLeader(user.getName())) {
                    sender.sendMessage(ChatColor.YELLOW + "/" + label + " " + Lang.getMessage("command_disband"));
                    sender.sendMessage(ChatColor.YELLOW + "/" + label + " " + Lang.getMessage("command_leader"));
                    sender.sendMessage(ChatColor.YELLOW + "/" + label + " " + Lang.getMessage("command_addmoder"));
                    sender.sendMessage(ChatColor.YELLOW + "/" + label + " " + Lang.getMessage("command_delmoder"));
                    sender.sendMessage(ChatColor.YELLOW + "/" + label + " " + Lang.getMessage("command_tag"));
                    sender.sendMessage(ChatColor.YELLOW + "/" + label + " " + Lang.getMessage("command_war"));
                    sender.sendMessage(ChatColor.YELLOW + "/" + label + " " + Lang.getMessage("command_stopwar"));
                    sender.sendMessage(ChatColor.YELLOW + "/" + label + " " + Lang.getMessage("command_ally"));
                    sender.sendMessage(ChatColor.YELLOW + "/" + label + " " + Lang.getMessage("command_stopally"));
                }
                else if (userClan.hasLeader(user.getName()) || userClan.isModer(user.getName())) {
                    sender.sendMessage(ChatColor.YELLOW + "/" + label + " " + Lang.getMessage("command_invite"));
                    sender.sendMessage(ChatColor.YELLOW + "/" + label + " " + Lang.getMessage("command_kick"));
                    sender.sendMessage(ChatColor.YELLOW + "/" + label + " " + Lang.getMessage("command_upgrade"));
                    sender.sendMessage(ChatColor.YELLOW + "/" + label + " " + Lang.getMessage("command_sethome"));
                    sender.sendMessage(ChatColor.YELLOW + "/" + label + " " + Lang.getMessage("command_removehome"));
                    sender.sendMessage(ChatColor.YELLOW + "/" + label + " " + Lang.getMessage("command_msg"));
                    sender.sendMessage(ChatColor.YELLOW + "/" + label + " " + Lang.getMessage("command_pvp"));
                }
                if (userClan.isModer(user.getName())) {
                    sender.sendMessage(ChatColor.YELLOW + "/" + label + " " + Lang.getMessage("command_leave1"));
                }
                else {
                    sender.sendMessage(ChatColor.YELLOW + "/" + label + " " + Lang.getMessage("command_leave2"));
                }
                sender.sendMessage(ChatColor.YELLOW + "/" + label + " " + Lang.getMessage("command_info"));
                sender.sendMessage(ChatColor.YELLOW + "/" + label + " " + Lang.getMessage("command_online"));
                sender.sendMessage(ChatColor.YELLOW + "/" + label + " " + Lang.getMessage("command_home"));
                sender.sendMessage(ChatColor.YELLOW + Lang.getMessage("command_cc"));
            }
            sender.sendMessage(ChatColor.YELLOW + "/" + label + " " + Lang.getMessage("command_list"));
            sender.sendMessage(ChatColor.YELLOW + "/" + label + " " + Lang.getMessage("command_top"));
            if (sender.hasPermission("WClans.admin")) {
                sender.sendMessage(ChatColor.YELLOW + "/" + label + " " + Lang.getMessage("command_reload"));
            }
            sender.sendMessage(ChatColor.YELLOW + Lang.getMessage("command_under"));
            return true;
        }
        if (args.length <= 0) {
            return true;
        }
        if (args[0].equalsIgnoreCase("create")) {
            if (!sender.hasPermission("WClans.create")) {
                sender.sendMessage(Lang.getMessage("command_error38"));
                return true;
            }
            if (args.length == 1) {
                sender.sendMessage(Lang.getMessage("command_error1"));
                return true;
            }
            if (userClan != null) {
                sender.sendMessage(Lang.getMessage("command_error2"));
                return true;
            }
            if (Clan.getClanCaseInsensetive(args[1]) != null) {
                sender.sendMessage(Lang.getMessage("command_error3"));
                return true;
            }
            if (args[1].length() > Main.config.getInt("settings.max_symbols")) {
                sender.sendMessage(Lang.getMessage("command_error4", Main.config.getInt("settings.max_symbols")));
                return true;
            }
            if (args[1].length() < Main.config.getInt("settings.min_symbols")) {
                sender.sendMessage(Lang.getMessage("command_error5", Main.config.getInt("settings.min_symbols")));
                return true;
            }
            if (!ChatColor.stripColor(args[1].replaceAll("&", "§")).matches(Main.config.getString("settings.clan_regex"))) {
                sender.sendMessage(Lang.getMessage("command_error6"));
                return true;
            }
            if (!Clan.checkColors(args[1])) {
                sender.sendMessage(Lang.getMessage("command_error69"));
                return true;
            }
            if (!sender.hasPermission("WClans.free.create")) {
                try {
                    if (!Main.getEconomy().has((OfflinePlayer)user, (double)Main.config.getInt("settings.create_cost"))) {
                        sender.sendMessage(Lang.getMessage("command_error52"));
                        return true;
                    }
                    Main.getEconomy().withdrawPlayer((OfflinePlayer)user, (double)Main.config.getInt("settings.create_cost"));
                }
                catch (Throwable t2) {}
            }
            userClan = Clan.create(args[1], sender.getName());
            userClan.broadcast(Lang.getMessage("clan_created", Clan.getClanByName(sender.getName()).getName()));
            return true;
        }
        else if (args[0].equalsIgnoreCase("disband") || args[0].equalsIgnoreCase("delete")) {
            if (!sender.hasPermission("WClans.disband")) {
                sender.sendMessage(Lang.getMessage("command_error38"));
                return true;
            }
            if (userClan == null) {
                sender.sendMessage(Lang.getMessage("command_error7"));
                return true;
            }
            if (!userClan.hasLeader(sender.getName())) {
                sender.sendMessage(Lang.getMessage("command_error8"));
                return true;
            }
            userClan.broadcast(Lang.getMessage("clan_disband", sender.getName()));
            userClan.disband();
            return true;
        }
        else if (args[0].equalsIgnoreCase("msg")) {
            if (!sender.hasPermission("WClans.msg")) {
                sender.sendMessage(Lang.getMessage("command_error38"));
                return true;
            }
            if (userClan == null) {
                sender.sendMessage(Lang.getMessage("command_error7"));
                return true;
            }
            if (!userClan.hasLeader(sender.getName()) && !userClan.isModer(sender.getName())) {
                sender.sendMessage(Lang.getMessage("command_error9"));
                return true;
            }
            final StringBuilder message = new StringBuilder();
            for (int i = 1; i < args.length; ++i) {
                message.append(String.valueOf(args[i]) + " ");
            }
            if (message.toString().length() <= 3) {
                sender.sendMessage(Lang.getMessage("command_error10"));
                return true;
            }
            ChatColor c = ChatColor.AQUA;
            if (userClan.isModer(sender.getName())) {
                c = ChatColor.GREEN;
            }
            if (userClan.hasLeader(sender.getName())) {
                c = ChatColor.GOLD;
            }
            for (final Member m : userClan.getMembers()) {
                if (Bukkit.getPlayerExact(m.getName()) != null) {
                    Bukkit.getPlayerExact(m.getName()).sendMessage(Lang.getMessage("command_msg_format", Lang.getMessage("clan"), Lang.getMessage("command_msg_1"), c + sender.getName(), message.toString()));
                }
            }
            return true;
        }
        else if (args[0].equalsIgnoreCase("online")) {
            if (!sender.hasPermission("WClans.online")) {
                sender.sendMessage(Lang.getMessage("command_error38"));
                return true;
            }
            if (userClan == null) {
                sender.sendMessage(Lang.getMessage("command_error7"));
                return true;
            }
            sender.sendMessage(Lang.getMessage("command_online_1"));
            for (final Member j : userClan.getMembers()) {
                if (Bukkit.getPlayerExact(j.getName()) != null) {
                    if (sender.getName().equalsIgnoreCase(j.getName())) {
                        sender.sendMessage(ChatColor.YELLOW + " > " + ChatColor.GREEN + j.getName());
                    }
                    else {
                        sender.sendMessage(ChatColor.YELLOW + " - " + j.getName());
                    }
                }
            }
            return true;
        }
        else if (args[0].equalsIgnoreCase("info")) {
            if (!sender.hasPermission("WClans.info")) {
                sender.sendMessage(Lang.getMessage("command_error38"));
                return true;
            }
            if (userClan == null) {
                sender.sendMessage(Lang.getMessage("command_error7"));
                return true;
            }
            sender.sendMessage(Lang.getMessage("command_info_1", userClan.getName(), userClan.getMembers().size(), userClan.getMaxPlayers()));
            sender.sendMessage(Lang.getMessage("command_info_2", userClan.getLeader()));
            final StringBuilder sb = new StringBuilder();
            for (final Member member : userClan.getMembers()) {
                ChatColor cc = ChatColor.GREEN;
                if (member.isModer()) {
                    cc = ChatColor.DARK_GREEN;
                }
                sb.append(cc + member.getName() + ChatColor.GREEN + ", ");
            }
            sender.sendMessage(Lang.getMessage("command_info_3", sb.toString().substring(0, sb.toString().length() - 2)));
            return true;
        }
        else if (args[0].equalsIgnoreCase("addmoder")) {
            if (!sender.hasPermission("WClans.addmoder")) {
                sender.sendMessage(Lang.getMessage("command_error38"));
                return true;
            }
            if (userClan == null) {
                sender.sendMessage(Lang.getMessage("command_error7"));
                return true;
            }
            if (args.length == 1) {
                sender.sendMessage(Lang.getMessage("command_error11"));
                return true;
            }
            if (!userClan.hasLeader(sender.getName())) {
                sender.sendMessage(Lang.getMessage("command_error12"));
                return true;
            }
            if (!userClan.hasClanMember(args[1])) {
                sender.sendMessage(Lang.getMessage("command_error13"));
                return true;
            }
            if (userClan.hasLeader(args[1])) {
                sender.sendMessage(Lang.getMessage("command_error14"));
                return true;
            }
            if (userClan.isModer(args[1])) {
                sender.sendMessage(Lang.getMessage("command_error15"));
                return true;
            }
            userClan.setModer(args[1], true);
            userClan.broadcast(Lang.getMessage("clan_addmoder", args[1]));
            return true;
        }
        else if (args[0].equalsIgnoreCase("delmoder")) {
            if (!sender.hasPermission("WClans.delmoder")) {
                sender.sendMessage(Lang.getMessage("command_error38"));
                return true;
            }
            if (userClan == null) {
                sender.sendMessage(Lang.getMessage("command_error7"));
                return true;
            }
            if (args.length == 1) {
                sender.sendMessage(Lang.getMessage("command_error11"));
                return true;
            }
            if (!userClan.hasLeader(sender.getName())) {
                sender.sendMessage(Lang.getMessage("command_error16"));
                return true;
            }
            if (!userClan.hasClanMember(args[1])) {
                sender.sendMessage(Lang.getMessage("command_error13"));
                return true;
            }
            if (!userClan.isModer(args[1])) {
                sender.sendMessage(Lang.getMessage("command_error17"));
                return true;
            }
            userClan.setModer(args[1], false);
            userClan.broadcast(Lang.getMessage("clan_delmoder", args[1]));
            return true;
        }
        else if (args[0].equalsIgnoreCase("invite")) {
            if (!sender.hasPermission("WClans.invite")) {
                sender.sendMessage(Lang.getMessage("command_error38"));
                return true;
            }
            if (args.length == 1) {
                sender.sendMessage(Lang.getMessage("command_error11"));
                return true;
            }
            if (userClan == null) {
                sender.sendMessage(Lang.getMessage("command_error7"));
                return true;
            }
            if (!userClan.hasLeader(sender.getName()) && !userClan.isModer(sender.getName())) {
                sender.sendMessage(Lang.getMessage("command_error18"));
                return true;
            }
            if (Bukkit.getPlayerExact(args[1]) == null) {
                sender.sendMessage(Lang.getMessage("command_error19"));
                return true;
            }
            if (Clan.hasMember(args[1])) {
                sender.sendMessage(Lang.getMessage("command_error2"));
                return true;
            }
            if (userClan.getMaxPlayers() <= userClan.getMembers().size()) {
                sender.sendMessage(Lang.getMessage("command_error20", userClan.getMaxPlayers()));
                return true;
            }
            if (Request.get(Bukkit.getPlayerExact(args[1])) != null) {
                sender.sendMessage(Lang.getMessage("command_error21"));
                return true;
            }
            Request.send(Bukkit.getPlayerExact(args[1]), sender.getName(), RequestType.INVITE, userClan);
            sender.sendMessage(Lang.getMessage("clan_invite", sender.getName(), args[1]));
            Bukkit.getPlayerExact(args[1]).sendMessage(Lang.getMessage("clan_invite_1", userClan.getName(), sender.getName()));
            try {
                final String json = "{\"text\":\"Нажмите \",\"color\":\"yellow\",\"extra\":[{\"text\":\"[ПРИНЯТЬ]\",\"color\":\"green\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/clan accept\"}},{\"text\":\" чтобы принять приглашение.\",\"color\":\"yellow\"}]}";
                final PacketContainer chat = new PacketContainer(PacketType.Play.Server.CHAT);
                chat.getChatComponents().write(0, (WrappedChatComponent)WrappedChatComponent.fromJson(json));
                ProtocolLibrary.getProtocolManager().sendServerPacket(Bukkit.getPlayerExact(args[1]), chat);
            }
            catch (InvocationTargetException ex) {}
            try {
                final String json = "{\"text\":\"Нажмите \",\"color\":\"yellow\",\"extra\":[{\"text\":\"[ОТКЛОНИТЬ]\",\"color\":\"red\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/clan deny\"}},{\"text\":\" чтобы отклонить приглашение.\",\"color\":\"yellow\"}]}";
                final PacketContainer chat = new PacketContainer(PacketType.Play.Server.CHAT);
                chat.getChatComponents().write(0, (WrappedChatComponent)WrappedChatComponent.fromJson(json));
                ProtocolLibrary.getProtocolManager().sendServerPacket(Bukkit.getPlayerExact(args[1]), chat);
            }
            catch (InvocationTargetException ex2) {}
            return true;
        }
        else if (args[0].equalsIgnoreCase("kick")) {
            if (!sender.hasPermission("WClans.kick")) {
                sender.sendMessage(Lang.getMessage("command_error38"));
                return true;
            }
            if (args.length == 1) {
                sender.sendMessage(Lang.getMessage("command_error11"));
                return true;
            }
            if (userClan == null) {
                sender.sendMessage(Lang.getMessage("command_error7"));
                return true;
            }
            if (!userClan.hasLeader(sender.getName()) && !userClan.isModer(sender.getName())) {
                sender.sendMessage(Lang.getMessage("command_error22"));
                return true;
            }
            if (!userClan.hasClanMember(args[1])) {
                sender.sendMessage(Lang.getMessage("command_error13"));
                return true;
            }
            if (args[1].equalsIgnoreCase(userClan.getLeader())) {
                sender.sendMessage(Lang.getMessage("command_error23"));
                return true;
            }
            userClan.kick(args[1]);
            userClan.broadcast(Lang.getMessage("clan_kick_1", args[1]));
            if (Bukkit.getPlayerExact(args[1]) != null) {
                Bukkit.getPlayerExact(args[1]).sendMessage(Lang.getMessage("clan_kick_2"));
            }
            return true;
        }
        else if (args[0].equalsIgnoreCase("list")) {
            if (!sender.hasPermission("WClans.list")) {
                sender.sendMessage(Lang.getMessage("command_error38"));
                return true;
            }
            if (Clan.clans.size() == 0) {
                sender.sendMessage(Lang.getMessage("command_error42"));
                return true;
            }
            int p = 0;
            if (args.length > 1) {
                try {
                    if (Integer.parseInt(args[1]) < 1) {
                        throw new Exception();
                    }
                    p = Integer.parseInt(args[1]);
                }
                catch (Exception e) {
                    sender.sendMessage(Lang.getMessage("command_error24"));
                    return true;
                }
                p = (p - 1) * 10;
                if (Clan.clans.size() - p < 0) {
                    sender.sendMessage(Lang.getMessage("command_error25"));
                    return true;
                }
            }
            int t = 0;
            sender.sendMessage(Lang.getMessage("clan_list", (int)Math.ceil(p / 10.0) + 1, (int)Math.ceil(Clan.clans.size() / 10.0)));
            for (int k = p; k < Clan.clans.size() && t != 10; ++k) {
                ++t;
                final Clan clan = (Clan)Clan.clans.values().toArray()[k];
                sender.sendMessage(ChatColor.YELLOW + " - " + clan.getName() + ChatColor.YELLOW + " [" + clan.getMembers().size() + "] (" + clan.getLeader() + ")");
            }
            return true;
        }
        else if (args[0].equalsIgnoreCase("leave")) {
            if (!sender.hasPermission("WClans.leave")) {
                sender.sendMessage(Lang.getMessage("command_error38"));
                return true;
            }
            if (userClan == null) {
                sender.sendMessage(Lang.getMessage("command_error7"));
                return true;
            }
            if (userClan.hasLeader(sender.getName())) {
                sender.sendMessage(Lang.getMessage("command_error26"));
                return true;
            }
            if (userClan.isModer(sender.getName())) {
                userClan.setModer(sender.getName(), false);
                userClan.broadcast(Lang.getMessage("clan_leave_1", sender.getName()));
            }
            else {
                userClan.broadcast(Lang.getMessage("clan_leave_2", sender.getName()));
                userClan.kick(sender.getName());
            }
            return true;
        }
        else if (args[0].equalsIgnoreCase("home")) {
            if (!sender.hasPermission("WClans.home")) {
                sender.sendMessage(Lang.getMessage("command_error38"));
                return true;
            }
            if (userClan == null) {
                sender.sendMessage(Lang.getMessage("command_error7"));
                return true;
            }
            if (!userClan.hasHome()) {
                sender.sendMessage(Lang.getMessage("command_error27"));
                return true;
            }
            Warm.addPlayer(user, userClan);
            return true;
        }
        else if (args[0].equalsIgnoreCase("removehome")) {
            if (!sender.hasPermission("WClans.removehome")) {
                sender.sendMessage(Lang.getMessage("command_error38"));
                return true;
            }
            if (userClan == null) {
                sender.sendMessage(Lang.getMessage("command_error7"));
                return true;
            }
            if (!userClan.hasLeader(sender.getName()) && !userClan.isModer(sender.getName())) {
                sender.sendMessage(Lang.getMessage("command_error28"));
                return true;
            }
            if (!userClan.hasHome()) {
                sender.sendMessage(Lang.getMessage("command_error27"));
                return true;
            }
            userClan.setHome(null, 0.0, 0.0, 0.0, 0.0f, 0.0f);
            userClan.broadcast(Lang.getMessage("clan_removehome", sender.getName()));
            return true;
        }
        else if (args[0].equalsIgnoreCase("sethome")) {
            if (!sender.hasPermission("WClans.sethome")) {
                sender.sendMessage(Lang.getMessage("command_error38"));
                return true;
            }
            final Player pl = (Player)sender;
            if (userClan == null) {
                sender.sendMessage(Lang.getMessage("command_error7"));
                return true;
            }
            if (!userClan.hasLeader(sender.getName()) && !userClan.isModer(sender.getName())) {
                sender.sendMessage(Lang.getMessage("command_error29"));
                return true;
            }
            if (!Main.getWG().canBuild(pl, pl.getLocation())) {
                sender.sendMessage(Lang.getMessage("command_error30"));
                return true;
            }
            userClan.setHome(pl.getWorld().getName(), pl.getLocation().getX(), pl.getLocation().getY(), pl.getLocation().getZ(), pl.getLocation().getYaw(), pl.getLocation().getPitch());
            userClan.broadcast(Lang.getMessage("clan_sethome", sender.getName()));
            return true;
        }
        else if (args[0].equalsIgnoreCase("leader")) {
            if (!sender.hasPermission("WClans.leader")) {
                sender.sendMessage(Lang.getMessage("command_error38"));
                return true;
            }
            if (userClan == null) {
                sender.sendMessage(Lang.getMessage("command_error7"));
                return true;
            }
            if (!userClan.hasLeader(user.getName())) {
                sender.sendMessage(Lang.getMessage("command_error31"));
                return true;
            }
            if (args.length == 1) {
                sender.sendMessage(Lang.getMessage("command_error11"));
                return true;
            }
            if (Clan.getClanByName(args[1]) == null) {
                sender.sendMessage(Lang.getMessage("command_error13"));
                return true;
            }
            if (!Clan.getClanByName(args[1]).getName().equalsIgnoreCase(userClan.getName())) {
                sender.sendMessage(Lang.getMessage("command_error33"));
                return true;
            }
            if (args[1].equalsIgnoreCase(sender.getName())) {
                sender.sendMessage(Lang.getMessage("command_error34"));
                return true;
            }
            if (userClan.isModer(args[1])) {
                userClan.setModer(args[1], false);
            }
            userClan.setLeader(args[1]);
            userClan.broadcast(Lang.getMessage("clan_leader", sender.getName(), args[1]));
            return true;
        }
        else if (args[0].equalsIgnoreCase("top")) {
            if (!sender.hasPermission("WClans.top")) {
                sender.sendMessage(Lang.getMessage("command_error38"));
                return true;
            }
            if (Clan.clans.size() == 0) {
                sender.sendMessage(Lang.getMessage("command_error42"));
                return true;
            }
            final Map<Clan, Integer> sorted = new HashMap<Clan, Integer>();
            for (final Clan c2 : Clan.clans.values()) {
                sorted.put(c2, c2.getMembers().size());
            }
            final List<Map.Entry<Clan, Integer>> entries = new LinkedList<Map.Entry<Clan, Integer>>(sorted.entrySet());
            Collections.sort(entries, new Comparator<Map.Entry<Clan, Integer>>() {
                @Override
                public int compare(final Map.Entry<Clan, Integer> o1, final Map.Entry<Clan, Integer> o2) {
                    return o2.getValue().compareTo(o1.getValue());
                }
            });
            int k = 1;
            for (final Map.Entry<Clan, Integer> entry : entries) {
                final Clan c3 = entry.getKey();
                sender.sendMessage(Lang.getMessage("command_top_1", k, c3.getName(), c3.getLeader(), entry.getValue()));
                if (k == 10) {
                    break;
                }
                ++k;
            }
            return true;
        }
        else if (args[0].equalsIgnoreCase("war")) {
            if (!sender.hasPermission("WClans.war")) {
                sender.sendMessage(Lang.getMessage("command_error38"));
                return true;
            }
            if (userClan == null) {
                sender.sendMessage(Lang.getMessage("command_error7"));
                return true;
            }
            if (!userClan.hasLeader(user.getName())) {
                sender.sendMessage(Lang.getMessage("command_error53"));
                return true;
            }
            if (args.length == 1) {
                sender.sendMessage(Lang.getMessage("command_error56"));
                return true;
            }
            final Clan c4 = Clan.getClanCaseInsensetive(args[1]);
            if (c4 == null) {
                sender.sendMessage(Lang.getMessage("command_error57"));
                return true;
            }
            if (c4 == userClan) {
                sender.sendMessage(Lang.getMessage("command_error64"));
                return true;
            }
            if (c4.getClanStanding(userClan) == ClanStanding.ALLY || userClan.getClanStanding(c4) == ClanStanding.ALLY) {
                sender.sendMessage(Lang.getMessage("command_error61"));
                return true;
            }
            if (c4.getClanStanding(userClan) == ClanStanding.WAR || userClan.getClanStanding(c4) == ClanStanding.WAR) {
                sender.sendMessage(Lang.getMessage("command_error62"));
                return true;
            }
            if (Bukkit.getPlayerExact(c4.getLeader()) == null) {
                sender.sendMessage(Lang.getMessage("command_error63"));
                return true;
            }
            if (Request.get(Bukkit.getPlayerExact(c4.getLeader())) != null) {
                sender.sendMessage(Lang.getMessage("command_error21"));
                return true;
            }
            Request.send(Bukkit.getPlayerExact(c4.getLeader()), sender.getName(), RequestType.WAR, userClan, c4);
            sender.sendMessage(Lang.getMessage("clan_war_invite_1", c4.getName()));
            Bukkit.getPlayerExact(c4.getLeader()).sendMessage(Lang.getMessage("clan_war_invite", userClan.getName()));
            try {
                final String json2 = "{\"text\":\"Нажмите \",\"color\":\"yellow\",\"extra\":[{\"text\":\"[ПРИНЯТЬ]\",\"color\":\"green\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/clan accept\"}},{\"text\":\" для начала войны.\",\"color\":\"yellow\"}]}";
                final PacketContainer chat2 = new PacketContainer(PacketType.Play.Server.CHAT);
                chat2.getChatComponents().write(0, (WrappedChatComponent)WrappedChatComponent.fromJson(json2));
                ProtocolLibrary.getProtocolManager().sendServerPacket(Bukkit.getPlayerExact(c4.getLeader()), chat2);
            }
            catch (Throwable t3) {}
            try {
                final String json2 = "{\"text\":\"Нажмите \",\"color\":\"yellow\",\"extra\":[{\"text\":\"[ОТКЛОНИТЬ]\",\"color\":\"red\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/clan deny\"}},{\"text\":\" для отмены войны.\",\"color\":\"yellow\"}]}";
                final PacketContainer chat2 = new PacketContainer(PacketType.Play.Server.CHAT);
                chat2.getChatComponents().write(0, (WrappedChatComponent)WrappedChatComponent.fromJson(json2));
                ProtocolLibrary.getProtocolManager().sendServerPacket(Bukkit.getPlayerExact(c4.getLeader()), chat2);
            }
            catch (Throwable t4) {}
            return true;
        }
        else if (args[0].equalsIgnoreCase("ally")) {
            if (!sender.hasPermission("WClans.ally")) {
                sender.sendMessage(Lang.getMessage("command_error38"));
                return true;
            }
            if (userClan == null) {
                sender.sendMessage(Lang.getMessage("command_error7"));
                return true;
            }
            if (!userClan.hasLeader(user.getName())) {
                sender.sendMessage(Lang.getMessage("command_error55"));
                return true;
            }
            if (args.length == 1) {
                sender.sendMessage(Lang.getMessage("command_error56"));
                return true;
            }
            final Clan c4 = Clan.getClanCaseInsensetive(args[1]);
            if (c4 == null) {
                sender.sendMessage(Lang.getMessage("command_error57"));
                return true;
            }
            if (c4 == userClan) {
                sender.sendMessage(Lang.getMessage("command_error64"));
                return true;
            }
            if (c4.getClanStanding(userClan) == ClanStanding.ALLY || userClan.getClanStanding(c4) == ClanStanding.ALLY) {
                sender.sendMessage(Lang.getMessage("command_error58"));
                return true;
            }
            if (c4.getClanStanding(userClan) == ClanStanding.WAR || userClan.getClanStanding(c4) == ClanStanding.WAR) {
                sender.sendMessage(Lang.getMessage("command_error59"));
                return true;
            }
            if (Bukkit.getPlayerExact(c4.getLeader()) == null) {
                sender.sendMessage(Lang.getMessage("command_error63"));
                return true;
            }
            if (Request.get(Bukkit.getPlayerExact(c4.getLeader())) != null) {
                sender.sendMessage(Lang.getMessage("command_error21"));
                return true;
            }
            Request.send(Bukkit.getPlayerExact(c4.getLeader()), sender.getName(), RequestType.ALLY, userClan, c4);
            sender.sendMessage(Lang.getMessage("clan_allie_invite_1", c4.getName()));
            Bukkit.getPlayerExact(c4.getLeader()).sendMessage(Lang.getMessage("clan_allie_invite", userClan.getName()));
            try {
                final String json2 = "{\"text\":\"Нажмите \",\"color\":\"yellow\",\"extra\":[{\"text\":\"[ПРИНЯТЬ]\",\"color\":\"green\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/clan accept\"}},{\"text\":\" для принятия дружбы.\",\"color\":\"yellow\"}]}";
                final PacketContainer chat2 = new PacketContainer(PacketType.Play.Server.CHAT);
                chat2.getChatComponents().write(0, (WrappedChatComponent)WrappedChatComponent.fromJson(json2));
                ProtocolLibrary.getProtocolManager().sendServerPacket(Bukkit.getPlayerExact(c4.getLeader()), chat2);
            }
            catch (Throwable t5) {}
            try {
                final String json2 = "{\"text\":\"Нажмите \",\"color\":\"yellow\",\"extra\":[{\"text\":\"[ОТКЛОНИТЬ]\",\"color\":\"red\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/clan deny\"}},{\"text\":\" для отказа.\",\"color\":\"yellow\"}]}";
                final PacketContainer chat2 = new PacketContainer(PacketType.Play.Server.CHAT);
                chat2.getChatComponents().write(0, (WrappedChatComponent)WrappedChatComponent.fromJson(json2));
                ProtocolLibrary.getProtocolManager().sendServerPacket(Bukkit.getPlayerExact(c4.getLeader()), chat2);
            }
            catch (Throwable t6) {}
            return true;
        }
        else if (args[0].equalsIgnoreCase("stopally")) {
            if (!sender.hasPermission("WClans.stopally")) {
                sender.sendMessage(Lang.getMessage("command_error38"));
                return true;
            }
            if (userClan == null) {
                sender.sendMessage(Lang.getMessage("command_error7"));
                return true;
            }
            if (!userClan.hasLeader(user.getName())) {
                sender.sendMessage(Lang.getMessage("command_error65"));
                return true;
            }
            if (args.length == 1) {
                sender.sendMessage(Lang.getMessage("command_error56"));
                return true;
            }
            final Clan c4 = Clan.getClanCaseInsensetive(args[1]);
            if (c4 == null) {
                sender.sendMessage(Lang.getMessage("command_error57"));
                return true;
            }
            if (c4 == userClan) {
                sender.sendMessage(Lang.getMessage("command_error64"));
                return true;
            }
            if (c4.getClanStanding(userClan) != ClanStanding.ALLY || userClan.getClanStanding(c4) != ClanStanding.ALLY) {
                sender.sendMessage(Lang.getMessage("command_error67"));
                return true;
            }
            userClan.setStanding(c4, ClanStanding.NORMAL);
            c4.setStanding(userClan, ClanStanding.NORMAL);
            userClan.broadcast(Lang.getMessage("clan_stopally_1", c4.getName()));
            c4.broadcast(Lang.getMessage("clan_stopally_2", userClan.getName()));
            return true;
        }
        else if (args[0].equalsIgnoreCase("stopwar")) {
            if (!sender.hasPermission("WClans.stopwar")) {
                sender.sendMessage(Lang.getMessage("command_error38"));
                return true;
            }
            if (userClan == null) {
                sender.sendMessage(Lang.getMessage("command_error7"));
                return true;
            }
            if (!userClan.hasLeader(user.getName())) {
                sender.sendMessage(Lang.getMessage("command_error66"));
                return true;
            }
            if (args.length == 1) {
                sender.sendMessage(Lang.getMessage("command_error56"));
                return true;
            }
            final Clan c4 = Clan.getClanCaseInsensetive(args[1]);
            if (c4 == null) {
                sender.sendMessage(Lang.getMessage("command_error57"));
                return true;
            }
            if (c4 == userClan) {
                sender.sendMessage(Lang.getMessage("command_error64"));
                return true;
            }
            if (c4.getClanStanding(userClan) != ClanStanding.WAR || userClan.getClanStanding(c4) != ClanStanding.WAR) {
                sender.sendMessage(Lang.getMessage("command_error68"));
                return true;
            }
            userClan.setStanding(c4, ClanStanding.NORMAL);
            c4.setStanding(userClan, ClanStanding.NORMAL);
            userClan.broadcast(Lang.getMessage("clan_stopwar_1", c4.getName()));
            c4.broadcast(Lang.getMessage("clan_stopwar_2", userClan.getName()));
            return true;
        }
        else if (args[0].equalsIgnoreCase("tag")) {
            if (!sender.hasPermission("WClans.tag")) {
                sender.sendMessage(Lang.getMessage("command_error38"));
                return true;
            }
            if (userClan == null) {
                sender.sendMessage(Lang.getMessage("command_error7"));
                return true;
            }
            if (!userClan.hasLeader(user.getName())) {
                sender.sendMessage(Lang.getMessage("command_error31"));
                return true;
            }
            if (args.length == 1) {
                sender.sendMessage(Lang.getMessage("command_error1"));
                return true;
            }
            if (args[1].length() > Main.config.getInt("settings.max_symbols")) {
                sender.sendMessage(Lang.getMessage("command_error4", Main.config.getInt("settings.max_symbols")));
                return true;
            }
            if (args[1].length() < Main.config.getInt("settings.min_symbols")) {
                sender.sendMessage(Lang.getMessage("command_error5", Main.config.getInt("settings.min_symbols")));
                return true;
            }
            if (!ChatColor.stripColor(args[1].replaceAll("&", "§")).matches(Main.config.getString("settings.clan_regex"))) {
                sender.sendMessage(Lang.getMessage("command_error6"));
                return true;
            }
            if (userClan == Clan.getClan(args[1])) {
                sender.sendMessage(Lang.getMessage("command_error49"));
                return true;
            }
            if (Clan.getClanCaseInsensetive(args[1]) != null) {
                sender.sendMessage(Lang.getMessage("command_error48"));
                return true;
            }
            if (!Clan.checkColors(args[1])) {
                sender.sendMessage(Lang.getMessage("command_error69"));
                return true;
            }
            try {
                if (!Main.getEconomy().has((OfflinePlayer)user, (double)Main.config.getInt("settings.change_cost"))) {
                    sender.sendMessage(Lang.getMessage("command_error52"));
                    return true;
                }
                Main.getEconomy().withdrawPlayer((OfflinePlayer)user, (double)Main.config.getInt("settings.change_cost"));
            }
            catch (Throwable t7) {}
            userClan.broadcast(Lang.getMessage("clan_changed", Clan.getClanByName(sender.getName()).getName(), args[1]));
            Clan.setName(userClan, args[1]);
            return true;
        }
        else if (args[0].equalsIgnoreCase("upgrade")) {
            if (!sender.hasPermission("WClans.upgrade")) {
                sender.sendMessage(Lang.getMessage("command_error38"));
                return true;
            }
            if (userClan == null) {
                sender.sendMessage(Lang.getMessage("command_error7"));
                return true;
            }
            if (!userClan.hasLeader(sender.getName()) && !userClan.isModer(sender.getName())) {
                sender.sendMessage(Lang.getMessage("command_error35"));
                return true;
            }
            if (userClan.getMaxPlayers() >= Main.config.getInt("settings.max_upgrade")) {
                sender.sendMessage(Lang.getMessage("command_error36"));
                return true;
            }
            try {
                if (!Main.getEconomy().has((OfflinePlayer)user, (double)Main.config.getInt("settings.upgrade_cost"))) {
                    sender.sendMessage(Lang.getMessage("command_error52"));
                    return true;
                }
                Main.getEconomy().withdrawPlayer((OfflinePlayer)user, (double)Main.config.getInt("settings.upgrade_cost"));
            }
            catch (Throwable t8) {}
            userClan.upgrade(1);
            userClan.broadcast(Lang.getMessage("clan_upgrade", sender.getName()));
            return true;
        }
        else if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("WClans.reload")) {
                sender.sendMessage(Lang.getMessage("command_error38"));
                return true;
            }
            ConfigHandler.configInit();
            sender.sendMessage(Lang.getMessage("command_reload_1"));
            return true;
        }
        else if (args[0].equalsIgnoreCase("pvp")) {
            if (!sender.hasPermission("WClans.pvp")) {
                sender.sendMessage(Lang.getMessage("command_error38"));
                return true;
            }
            if (userClan == null) {
                sender.sendMessage(Lang.getMessage("command_error7"));
                return true;
            }
            if (!userClan.hasLeader(sender.getName()) && !userClan.isModer(sender.getName())) {
                sender.sendMessage(Lang.getMessage("command_error37"));
                return true;
            }
            userClan.setPvP(!userClan.isPvP());
            userClan.broadcast(userClan.isPvP() ? Lang.getMessage("clan_pvp_1", sender.getName()) : Lang.getMessage("clan_pvp_2", sender.getName()));
            return true;
        }
        else if (args[0].equalsIgnoreCase("accept")) {
            if (Request.get((Player)sender) == null) {
                sender.sendMessage(Lang.getMessage("command_error40"));
                return true;
            }
            Request.accept(Request.get((Player)sender));
            return true;
        }
        else {
            if (!args[0].equalsIgnoreCase("deny")) {
                sender.sendMessage(Lang.getMessage("command_error41"));
                return true;
            }
            if (Request.get((Player)sender) == null) {
                sender.sendMessage(Lang.getMessage("command_error40"));
                return true;
            }
            Request.delete(Request.get((Player)sender));
            return true;
        }
    }
}
