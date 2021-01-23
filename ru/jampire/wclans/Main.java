package ru.jampire.wclans;

import org.bukkit.plugin.java.*;
import org.bukkit.configuration.file.*;
import com.sk89q.worldguard.bukkit.*;
import org.bukkit.*;
import net.milkbowl.vault.economy.*;
import org.bukkit.plugin.*;
import org.bukkit.command.*;
import java.util.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;

public class Main extends JavaPlugin
{
    public static FileConfiguration config;
    public static Plugin plugin;
    
    public static WorldGuardPlugin getWG() {
        final Plugin plugin = Bukkit.getPluginManager().getPlugin("WorldGuard");
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            return null;
        }
        return (WorldGuardPlugin)plugin;
    }
    
    public static Economy getEconomy() throws Exception {
        final RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServicesManager().getRegistration(Economy.class);
        return (Economy)economyProvider.getProvider();
    }
    
    public void onEnable() {
        final long time = System.currentTimeMillis();
        Main.plugin = (Plugin)this;
        ConfigHandler.configInit();
        MySQL.connect();
        MySQL.getClans();
        MySQL.getClansAsync();
        this.getCommand("clan").setExecutor((CommandExecutor)new ClanCommand());
        this.getCommand("cc").setExecutor((CommandExecutor)new CCCommand());
        Bukkit.getScheduler().runTaskTimer((Plugin)this, (Runnable)new Runnable() {
            @Override
            public void run() {
                final ArrayList<Request> toDelete = new ArrayList<Request>();
                for (final Request r : Request.requests) {
                    if (System.currentTimeMillis() - r.getTime() >= 15000L) {
                        toDelete.add(r);
                    }
                }
                for (final Request r : toDelete) {
                    final Player pl = Bukkit.getPlayerExact(r.getSender());
                    switch (r.getType()) {
                        case ALLY: {
                            r.getPlayer().sendMessage(Lang.getMessage("invite_allie_canceled"));
                            final Clan ca = (Clan)r.getData()[1];
                            if (pl != null) {
                                pl.sendMessage(Lang.getMessage("invite_allie_canceled2", ca.getName()));
                                break;
                            }
                            break;
                        }
                        case INVITE: {
                            r.getPlayer().sendMessage(Lang.getMessage("invite_canceled"));
                            if (pl != null) {
                                pl.sendMessage(Lang.getMessage("invite_canceled2", r.getPlayer().getName()));
                                break;
                            }
                            break;
                        }
                        case WAR: {
                            r.getPlayer().sendMessage(Lang.getMessage("invite_war_canceled"));
                            final Clan cw = (Clan)r.getData()[1];
                            if (pl != null) {
                                pl.sendMessage(Lang.getMessage("invite_war_canceled2", cw.getName()));
                                break;
                            }
                            break;
                        }
                    }
                    Request.deny(r);
                }
            }
        }, 0L, 20L);
        Bukkit.getPluginManager().registerEvents((Listener)new EventListener(), (Plugin)this);
        Logger.info(Lang.getMessage("plugin_enabled", System.currentTimeMillis() - time));
    }
    
    public void onDisable() {
        MySQL.disconnect();
        Logger.info(Lang.getMessage("plugin_disabled"));
    }
}
