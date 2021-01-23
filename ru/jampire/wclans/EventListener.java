package ru.jampire.wclans;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.events.DisallowedPVPEvent;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;

public class EventListener implements Listener
{
    @EventHandler
    public void PlayerQuitEvent(final PlayerQuitEvent event) {
        if (Request.get(event.getPlayer()) != null) {
            Request.delete(Request.get(event.getPlayer()));
        }
    }
    
    @EventHandler
    public void PlayerKickEvent(final PlayerKickEvent event) {
        if (Request.get(event.getPlayer()) != null) {
            Request.delete(Request.get(event.getPlayer()));
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void PlayerMoveEvent(final PlayerMoveEvent event) {
        if (event.getFrom().distance(event.getTo()) > 0.0) {
            Warm.cancelWarming(event.getPlayer());
        }
    }
    
    @EventHandler
    public void EntityDamageByEntityEvent(final EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Entity d = event.getDamager();
            if (d instanceof Arrow) {
                d = (Entity)((Arrow)d).getShooter();
            }
            else if (d instanceof ThrownPotion) {
                d = (Entity)((ThrownPotion)d).getShooter();
            }
            if (d instanceof Player) {
                final Player damager = (Player)d;
                final Player attacker = (Player)event.getEntity();
                final Clan userClan = Clan.getClanByName(damager.getName());
                final Clan targetClan = Clan.getClanByName(attacker.getName());
                final ApplicableRegionSet set = Main.getWG().getRegionManager(attacker.getWorld()).getApplicableRegions(attacker.getLocation());
                if (userClan != null && targetClan != null && (userClan.getClanStanding(targetClan) == ClanStanding.ALLY || targetClan.getClanStanding(userClan) == ClanStanding.ALLY)) {
                    damager.sendMessage(Lang.getMessage("damage_in_ally"));
                    event.setCancelled(true);
                    return;
                }
                if (set.getFlag((Flag)DefaultFlag.PVP) == StateFlag.State.ALLOW) {
                    return;
                }
                if (Clan.hasMember(damager.getName()) && Clan.hasMember(attacker.getName()) && userClan.hasClanMember(attacker.getName())) {
                    if (!userClan.isPvP()) {
                        return;
                    }
                    damager.sendMessage(Lang.getMessage("damage_in_clan"));
                    event.setCancelled(true);
                }
            }
        }
    }
    
    @EventHandler
    public void DisallowPvpEvent(final DisallowedPVPEvent event) {
        final Player attacker = event.getAttacker();
        final Player defender = event.getDefender();
        final Clan targetClan = Clan.getClanByName(attacker.getName());
        final Clan userClan = Clan.getClanByName(defender.getName());
        if (userClan != null && targetClan != null && (userClan.getClanStanding(targetClan) == ClanStanding.WAR || targetClan.getClanStanding(userClan) == ClanStanding.WAR)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void PlayerDeathEvent(final PlayerDeathEvent event) {
        try {
            Main.getEconomy();
        }
        catch (Exception e) {
            return;
        }
        final Player defender = event.getEntity();
        final Player killer = event.getEntity().getKiller();
        if (defender != null && killer != null) {
            final Clan targetClan = Clan.getClanByName(killer.getName());
            final Clan userClan = Clan.getClanByName(defender.getName());
            if (userClan != null && targetClan != null && (userClan.getClanStanding(targetClan) == ClanStanding.WAR || targetClan.getClanStanding(userClan) == ClanStanding.WAR)) {
                if (Cooldown.hasCooldown(killer.getName(), "pvp")) {
                    return;
                }
                Cooldown.setCooldown(killer.getName(), Main.config.getInt("settings.kill_cooldown") * 1000, "pvp");
                try {
                    Main.getEconomy().depositPlayer((OfflinePlayer)killer, (double)Main.config.getInt("settings.kill_reward"));
                }
                catch (Throwable t) {}
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void AsyncPlayerChatEvent(final AsyncPlayerChatEvent event) {
        if (Clan.hasMember(event.getPlayer().getName()) && event.getFormat().contains("!clan!")) {
            event.setFormat(event.getFormat().replace("!clan!", Lang.getMessage("clantag_format", Clan.getClanByName(event.getPlayer().getName()).getName())));
        }
        else {
            event.setFormat(event.getFormat().replace("!clan!", ""));
        }
        if (event.getMessage() != null && event.getMessage().length() > 1 && event.getMessage().startsWith("%")) {
            final Clan userClan = Clan.getClanByName(event.getPlayer().getName());
            if (userClan == null) {
                event.getPlayer().sendMessage(Lang.getMessage("command_error7"));
                event.setCancelled(true);
                return;
            }
            event.getRecipients().clear();
            for (final Member m : userClan.getMembers()) {
                final Player pl = Bukkit.getPlayerExact(m.getName());
                if (pl != null) {
                    event.getRecipients().add(pl);
                }
            }
            ChatColor c = ChatColor.AQUA;
            if (userClan.isModer(event.getPlayer().getName())) {
                c = ChatColor.GREEN;
            }
            if (userClan.hasLeader(event.getPlayer().getName())) {
                c = ChatColor.GOLD;
            }
            event.setFormat(Lang.getMessage("clanchat_format", Lang.getMessage("clan"), c + event.getPlayer().getName(), c + "%2$s"));
            event.setMessage(event.getMessage().substring(1, event.getMessage().length()).replace("&", "§"));
        }
    }
}
