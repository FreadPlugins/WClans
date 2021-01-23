package ru.jampire.wclans;

import java.util.*;
import org.bukkit.entity.*;
import org.bukkit.*;

public class Warm
{
    private static HashMap<String, Integer> players;
    private static HashMap<String, Location> playerloc;
    
    static {
        Warm.players = new HashMap<String, Integer>();
        Warm.playerloc = new HashMap<String, Location>();
    }
    
    public static void addPlayer(final Player player, final Clan clan) {
        if (player.hasPermission("WClans.warm.ignore")) {
            clan(player, clan);
            return;
        }
        if (isWarming(player)) {
            player.sendMessage(Lang.getMessage("warm_alredy"));
            return;
        }
        player.sendMessage(Lang.getMessage("warm_use", Main.config.getInt("settings.warm")));
        final int taskIndex = Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, (Runnable)new WarmTask(player, clan), (long)(Main.config.getInt("settings.warm") * 20));
        Warm.players.put(player.getName(), taskIndex);
        Warm.playerloc.put(player.getName(), player.getLocation());
    }
    
    public static boolean hasMoved(final Player player) {
        final Location curloc = player.getLocation();
        final Location cmdloc = Warm.playerloc.get(player.getName());
        return cmdloc.distanceSquared(curloc) > 0.0;
    }
    
    public static boolean isWarming(final Player player) {
        return Warm.players.containsKey(player.getName());
    }
    
    public static void cancelWarming(final Player player) {
        if (isWarming(player)) {
            Bukkit.getScheduler().cancelTask((int)Warm.players.get(player.getName()));
            Warm.players.remove(player.getName());
            Warm.playerloc.remove(player.getName());
            player.sendMessage(Lang.getMessage("warm_canceled"));
        }
    }
    
    public static void clan(final Player pl, final Clan clan) {
        pl.teleport(clan.getHome());
        pl.sendMessage(Lang.getMessage("clan_teleport"));
    }
    
    private static class WarmTask implements Runnable
    {
        private Player player;
        private Clan clan;
        
        public WarmTask(final Player player, final Clan clan) {
            this.player = player;
            this.clan = clan;
        }
        
        @Override
        public void run() {
            Warm.players.remove(this.player.getName());
            Warm.playerloc.remove(this.player.getName());
            Warm.clan(this.player, this.clan);
        }
    }
}
