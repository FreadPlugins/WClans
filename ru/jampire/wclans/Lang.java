package ru.jampire.wclans;

import org.bukkit.configuration.file.*;
import java.util.*;

public class Lang
{
    private static Map<String, String> language;
    
    static {
        Lang.language = new HashMap<String, String>();
    }
    
    public static void load(final YamlConfiguration langYml) {
        if (Main.config.getString("language") == null) {
            Logger.error("Language " + Main.config.getString("language") + " not found.");
            return;
        }
        for (final Map.Entry<String, Object> entry : langYml.getConfigurationSection(Main.config.getString("language")).getValues(false).entrySet()) {
            Lang.language.put(String.valueOf(Main.config.getString("language")) + "." + entry.getKey(), String.valueOf(entry.getValue()));
        }
    }
    
    public static String getMessage(final String target) {
        if (Main.config.getString("language") == null) {
            Logger.error("Language " + Main.config.getString("language") + " not found.");
            return "";
        }
        if (Lang.language.get(String.valueOf(Main.config.getString("language")) + "." + target) == null) {
            Logger.error("String " + Main.config.getString("language") + "." + target + " is null.");
            return "";
        }
        return Lang.language.get(String.valueOf(Main.config.getString("language")) + "." + target);
    }
    
    public static String getMessage(final String target, final Object... arg1) {
        if (Main.config.getString("language") == null) {
            Logger.error("Language " + Main.config.getString("language") + " not found.");
            return "";
        }
        if (Lang.language.get(String.valueOf(Main.config.getString("language")) + "." + target) == null) {
            Logger.error("String " + Main.config.getString("language") + "." + target + " is null.");
            return "";
        }
        return String.format(Lang.language.get(String.valueOf(Main.config.getString("language")) + "." + target), arg1);
    }
}
