package ru.jampire.wclans;

import java.util.*;

public class Cooldown
{
    private String player;
    private long cooldown;
    private String key;
    public static HashMap<String, Cooldown> cooldowns;
    
    static {
        Cooldown.cooldowns = new HashMap<String, Cooldown>();
    }
    
    public Cooldown(final String player, final long cooldown, final String key) {
        this.player = player;
        this.cooldown = cooldown;
        this.key = key;
    }
    
    public String getPlayer() {
        return this.player;
    }
    
    public long getCooldown() {
        return this.cooldown;
    }
    
    public String getKey() {
        return this.key;
    }
    
    public static void setCooldown(final String player, final long cooldown, final String title) {
        Cooldown.cooldowns.put(String.valueOf(player) + title, new Cooldown(player, System.currentTimeMillis() + cooldown, title));
    }
    
    public static boolean hasCooldown(final String player, final String title) {
        return Cooldown.cooldowns.get(String.valueOf(player) + title) != null && Cooldown.cooldowns.get(String.valueOf(player) + title).getCooldown() > System.currentTimeMillis();
    }
    
    public static long getCooldown(final String player, final String title) {
        return (Cooldown.cooldowns.get(String.valueOf(player) + title).getCooldown() - System.currentTimeMillis()) / 1000L;
    }
}
