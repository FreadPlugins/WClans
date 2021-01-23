package ru.jampire.wclans;

import org.bukkit.entity.*;
import java.util.*;

public class Request
{
    private Player player;
    private String sender;
    private long time;
    private Object[] data;
    private RequestType type;
    public static ArrayList<Request> requests;
    
    static {
        Request.requests = new ArrayList<Request>();
    }
    
    public Request(final Player player, final String sender, final RequestType type, final long time, final Object... data) {
        this.player = player;
        this.sender = sender;
        this.type = type;
        this.time = time;
        this.data = data;
    }
    
    public Object[] getData() {
        return this.data;
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public String getSender() {
        return this.sender;
    }
    
    public RequestType getType() {
        return this.type;
    }
    
    public long getTime() {
        return this.time;
    }
    
    public static Request get(final Player pl) {
        for (final Request req : Request.requests) {
            if (req.getPlayer().equals(pl)) {
                return req;
            }
        }
        return null;
    }
    
    public static void send(final Player player, final String sender, final RequestType type, final Object... data) {
        Request.requests.add(new Request(player, sender, type, System.currentTimeMillis(), data));
    }
    
    public static void accept(final Request req) {
        if (req.getType() == RequestType.INVITE) {
            final Clan c = (Clan)req.getData()[0];
            c.invite(req.getPlayer().getName());
            c.broadcast(Lang.getMessage("clan_join", req.getPlayer().getName()));
        }
        else if (req.getType() == RequestType.WAR) {
            final Clan c2 = (Clan)req.getData()[0];
            final Clan c3 = (Clan)req.getData()[1];
            c2.setStanding(c3, ClanStanding.WAR);
            c3.setStanding(c2, ClanStanding.WAR);
            c2.broadcast(Lang.getMessage("clan_war", c3.getName()));
            c3.broadcast(Lang.getMessage("clan_war", c2.getName()));
        }
        else if (req.getType() == RequestType.ALLY) {
            final Clan c2 = (Clan)req.getData()[0];
            final Clan c3 = (Clan)req.getData()[1];
            c2.setStanding(c3, ClanStanding.ALLY);
            c3.setStanding(c2, ClanStanding.ALLY);
            c2.broadcast(Lang.getMessage("clan_allie", c3.getName()));
            c3.broadcast(Lang.getMessage("clan_allie", c2.getName()));
        }
        Request.requests.remove(req);
    }
    
    public static void deny(final Request req) {
        Request.requests.remove(req);
    }
    
    public static void delete(final Request req) {
        req.time = 0L;
    }
}
