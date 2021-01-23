package ru.jampire.wclans;

import org.bukkit.*;

public class Logger
{
    public static void info(final Object text) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[" + Main.plugin.getName() + "] " + text);
    }
    
    public static void warning(final Object text) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[" + Main.plugin.getName() + "] " + text);
    }
    
    public static void error(final Object text) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[" + Main.plugin.getName() + "] " + text);
    }
    
    public static void debug(final Object text) {
        if (!Main.config.getBoolean("debug")) {
            return;
        }
        Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[" + Main.plugin.getName() + "] [Debug] " + text);
    }
}
