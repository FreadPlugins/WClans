package ru.jampire.wclans;

import java.util.concurrent.*;

import org.bukkit.*;

import java.util.*;
import java.util.regex.*;

public class Clan
{
    private String name;
    private String leader;
    private CopyOnWriteArrayList<Member> members;
    private ConcurrentHashMap<Clan, ClanStanding> standing;
    private String world;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private int maxplayers;
    private boolean pvp;
    public static ConcurrentHashMap<String, Clan> clans;
    public static final Pattern STRIP_COLOR_PATTERN;
    
    static {
        Clan.clans = new ConcurrentHashMap<String, Clan>();
        STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + String.valueOf("§") + "[0-9A-FK-OR]");
    }
    
    public Clan(final String name, final String leader, final CopyOnWriteArrayList<Member> members, final ConcurrentHashMap<Clan, ClanStanding> standing, final String world, final double x, final double y, final double z, final float yaw, final float pitch, final int maxplayers, final boolean pvp) {
        this.name = name;
        this.leader = leader;
        this.members = members;
        this.standing = standing;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.maxplayers = maxplayers;
        this.pvp = pvp;
    }
    
    public String getRealName() {
        return this.name;
    }
    
    public String getName() {
        return String.valueOf(this.name.replace("&", "§")) + ChatColor.RESET;
    }
    
    public String getLeader() {
        return this.leader;
    }
    
    public CopyOnWriteArrayList<Member> getMembers() {
        return this.members;
    }
    
    public ConcurrentHashMap<Clan, ClanStanding> getStandings() {
        return this.standing;
    }
    
    public Location getHome() {
        return new Location(Bukkit.getWorld(this.world), this.x, this.y, this.z, this.yaw, this.pitch);
    }
    
    public int getMaxPlayers() {
        return this.maxplayers;
    }
    
    public boolean isPvP() {
        return this.pvp;
    }
    
    public void setPvP(final boolean pvp) {
        this.pvp = pvp;
        MySQL.executePrepared("UPDATE clan_list SET pvp=? WHERE name=?", pvp ? "1" : "0", this.name);
    }
    
    public void setMembers(final CopyOnWriteArrayList<Member> members) {
        this.members = members;
    }
    
    public void setStandings(final ConcurrentHashMap<Clan, ClanStanding> standing) {
        this.standing = standing;
    }
    
    public static void setName(final Clan clan, final String name) {
        MySQL.executePrepared("UPDATE clan_list SET name=? WHERE name=?", name, clan.getRealName());
        MySQL.executePrepared("UPDATE clan_members SET clan=? WHERE clan=?", name, clan.getRealName());
        Clan.clans.remove(clan.getRealName());
        clan.name = name;
        Clan.clans.put(name, clan);
    }
    
    public static Clan getClan(final String clan) {
        return Clan.clans.get(clan);
    }
    
    public static Clan getClanCaseInsensetive(final String clan) {
        for (final Clan c : Clan.clans.values()) {
            if (ChatColor.stripColor(c.getName()).equalsIgnoreCase(ChatColor.stripColor(clan.replaceAll("&", "§")))) {
                return c;
            }
        }
        return null;
    }
    
    public static Clan getClanByName(final String player) {
        for (final Clan c : Clan.clans.values()) {
            for (final Member mem : c.getMembers()) {
                if (mem.getName().equalsIgnoreCase(player)) {
                    return c;
                }
            }
        }
        return null;
    }
    
    public void invite(final String member) {
        this.members.add(new Member(member.toLowerCase(), false));
        MySQL.executePrepared("INSERT INTO clan_members (clan, name, isModer) VALUES (?, ?, ?)", this.name, member.toLowerCase(), 0);
    }
    
    public void setStanding(final Clan clan, final ClanStanding type) {
        if (clan == this) {
            return;
        }
        if (this.standing.get(clan) == null) {
            if (type == ClanStanding.NORMAL) {
                return;
            }
            this.standing.put(clan, type);
            MySQL.executePrepared("INSERT INTO clan_standing (clan, target, type) VALUES (?, ?, ?)", this.name, clan.getRealName(), type);
        }
        else {
            if (type == ClanStanding.NORMAL) {
                this.standing.remove(clan);
                MySQL.executePrepared("DELETE FROM clan_standing WHERE clan=? AND target=?", this.name, clan.getRealName());
                return;
            }
            this.standing.put(clan, type);
            MySQL.executePrepared("UPDATE clan_standing SET type=? WHERE clan=? AND target=?", type, this.name, clan.getRealName());
        }
    }
    
    public void kick(final String member) {
        if (this.isModer(member)) {
            this.setModer(member, false);
        }
        final CopyOnWriteArrayList<Member> members = this.members;
        Member my = null;
        for (final Member memb : members) {
            if (memb.getName().equalsIgnoreCase(member)) {
                my = memb;
            }
        }
        members.remove(my);
        this.members = members;
        MySQL.executePrepared("DELETE FROM clan_members WHERE clan=? AND name=?", this.name, member.toLowerCase());
    }
    
    public void setModer(final String name, final boolean isModer) {
        for (final Member m : this.members) {
            if (m.getName().equalsIgnoreCase(name)) {
                m.setModer(isModer);
            }
        }
        MySQL.executePrepared("UPDATE clan_members SET isModer=? WHERE clan=? AND name=?", isModer ? "1" : "0", this.name, name.toLowerCase());
    }
    
    public boolean isModer(final String name) {
        for (final Member m : this.members) {
            if (m.getName().equalsIgnoreCase(name) && m.isModer()) {
                return true;
            }
        }
        return false;
    }
    
    public static Clan create(final String clan, final String leader) {
        final CopyOnWriteArrayList<Member> members = new CopyOnWriteArrayList<Member>();
        members.add(new Member(leader.toLowerCase(), false));
        final Clan c = new Clan(clan, leader.toLowerCase(), members, new ConcurrentHashMap<Clan, ClanStanding>(), null, 0.0, 0.0, 0.0, 0.0f, 0.0f, Main.config.getInt("settings.default_max"), true);
        Clan.clans.put(clan, c);
        MySQL.executePrepared("INSERT INTO clan_list (name, leader, world, x, y, z, yaw, pitch, maxplayers, pvp) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", clan, leader.toLowerCase(), "null", 0, 0, 0, 0, 0, Main.config.getInt("settings.default_max"), 1);
        MySQL.executePrepared("INSERT INTO clan_members (clan, name, isModer) VALUES (?, ?, ?)", clan, leader.toLowerCase(), 0);
        return c;
    }
    
    public void disband() {
        Clan.clans.remove(this.name);
        MySQL.executePrepared("DELETE FROM clan_list WHERE name=?", this.name);
        MySQL.executePrepared("DELETE FROM clan_members WHERE clan=?", this.name);
    }
    
    public void setLeader(final String leader) {
        this.leader = leader.toLowerCase();
        MySQL.executePrepared("UPDATE clan_list SET leader=? WHERE name=?", leader.toLowerCase(), this.name);
    }
    
    public void setHome(final String world, final double x, final double y, final double z, final float yaw, final float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        MySQL.executePrepared("UPDATE clan_list SET world=?, x=?, y=?, z=?, yaw=?, pitch=? WHERE name=?", world, x, y, z, yaw, pitch, this.name);
    }
    
    public boolean hasLeader(final String player) {
        return this.getLeader().equalsIgnoreCase(player);
    }
    
    public boolean hasHome() {
        return (this.x != 0.0 || this.y != 0.0 || this.z != 0.0 || this.yaw != 0.0f || this.pitch != 0.0f) && Bukkit.getWorld(this.world) != null;
    }
    
    public boolean hasClanMember(final String player) {
        for (final Member mem : this.members) {
            if (mem.getName().equalsIgnoreCase(player)) {
                return true;
            }
        }
        return false;
    }
    
    public ClanStanding getClanStanding(final Clan clan) {
        for (final Map.Entry<Clan, ClanStanding> standing : this.standing.entrySet()) {
            if (standing.getKey() == clan) {
                return standing.getValue();
            }
        }
        return ClanStanding.NORMAL;
    }
    
    public static boolean hasMember(final String player) {
        for (final Clan c : Clan.clans.values()) {
            for (final Member mem : c.getMembers()) {
                if (mem.getName().equalsIgnoreCase(player)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public static boolean checkColors(final String clan) {
        final Matcher m = Clan.STRIP_COLOR_PATTERN.matcher(clan);
        while (m.find()) {
            if (!ChatColor.getByChar(m.group().replace("§", "")).isColor()) {
                return false;
            }
        }
        return true;
    }
    
    public void upgrade(final int i) {
        this.maxplayers += i;
        MySQL.executePrepared("UPDATE clan_list SET maxplayers=? WHERE name=?", this.maxplayers, this.name);
    }
    
    public void broadcast(final String message) {
        for (final Member m : this.members) {
            if (Bukkit.getPlayerExact(m.getName()) != null) {
                Bukkit.getPlayerExact(m.getName()).sendMessage(Lang.getMessage("command_broadcast_format", Lang.getMessage("clan"), message));
            }
        }
    }
}
