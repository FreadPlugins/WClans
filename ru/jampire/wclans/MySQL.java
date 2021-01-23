package ru.jampire.wclans;

import org.bukkit.*;

import java.util.concurrent.*;
import java.sql.*;

public class MySQL
{
    public static Connection connection;

    static {
        MySQL.connection = null;
    }

    public static void connect() {
        try {
            if (!Main.plugin.getDataFolder().mkdirs()) {
                Main.plugin.getDataFolder().mkdirs();
            }
            if (Main.config.getString("database").equalsIgnoreCase("sqlite")) {
                Class.forName("org.sqlite.JDBC").newInstance();
                MySQL.connection = DriverManager.getConnection("jdbc:sqlite://" + Main.plugin.getDataFolder().getAbsolutePath() + "/WClans.db");
                executeSync("CREATE TABLE IF NOT EXISTS `clan_list` (`id` INTEGER PRIMARY KEY,`name` varchar(255) NOT NULL UNIQUE,`icon` varchar(255),`leader` varchar(255) NOT NULL UNIQUE,`world` varchar(255) NOT NULL,`x` varchar(255) NOT NULL,`y` varchar(255) NOT NULL,`z` varchar(255) NOT NULL,`yaw` varchar(255) NOT NULL,`pitch` varchar(255) NOT NULL,`maxplayers` varchar(255) NOT NULL,`pvp` tinyint(1) NOT NULL)");
                executeSync("CREATE TABLE IF NOT EXISTS `clan_members` (`id` INTEGER PRIMARY KEY,`clan` varchar(255) NOT NULL,`name` varchar(255) NOT NULL,`isModer` tinyint(1) NOT NULL)");
                executeSync("CREATE TABLE IF NOT EXISTS `clan_standing` (`id` INTEGER PRIMARY KEY,`clan` varchar(255) NOT NULL,`target` varchar(255) NOT NULL,`type` varchar(255) NOT NULL)");
            }
            else {
                Class.forName("com.mysql.jdbc.Driver").newInstance();
                MySQL.connection = DriverManager.getConnection("jdbc:mysql://" + Main.config.getString("mysql.host") + ":" + Main.config.getString("mysql.port") + "/" + Main.config.getString("mysql.database") + "?useUnicode=true&characterEncoding=UTF-8&" + "user=" + Main.config.getString("mysql.username") + "&password=" + Main.config.getString("mysql.password"));
                executeSync("CREATE TABLE IF NOT EXISTS `clan_list` (`id` int(11) NOT NULL AUTO_INCREMENT,`name` varchar(255) BINARY NOT NULL UNIQUE,`icon` varchar(255),`leader` varchar(255) BINARY NOT NULL UNIQUE,`world` varchar(255) NOT NULL,`x` varchar(255) NOT NULL,`y` varchar(255) NOT NULL,`z` varchar(255) NOT NULL,`yaw` varchar(255) NOT NULL,`pitch` varchar(255) NOT NULL,`maxplayers` varchar(255) NOT NULL,`pvp` tinyint(1) NOT NULL,PRIMARY KEY (`id`)) DEFAULT CHARSET=utf8 COLLATE utf8_bin AUTO_INCREMENT=0");
                executeSync("CREATE TABLE IF NOT EXISTS `clan_members` (`id` int(11) NOT NULL AUTO_INCREMENT,`clan` varchar(255) NOT NULL,`name` varchar(255) NOT NULL,`isModer` tinyint(1) NOT NULL,PRIMARY KEY (`id`)) DEFAULT CHARSET=utf8 COLLATE utf8_bin AUTO_INCREMENT=0");
                executeSync("CREATE TABLE IF NOT EXISTS `clan_standing` (`id` int(11) NOT NULL AUTO_INCREMENT,`clan` varchar(255) NOT NULL,`target` varchar(255) NOT NULL,`type` varchar(255) NOT NULL,PRIMARY KEY (`id`)) DEFAULT CHARSET=utf8 COLLATE utf8_bin AUTO_INCREMENT=0");
            }
            Logger.info(Lang.getMessage("mysql_connected"));
        }
        catch (Exception e) {
            Logger.error(Lang.getMessage("mysql_error"));
            e.printStackTrace();
        }
    }

    public static boolean hasConnected() {
        try {
            return !MySQL.connection.isClosed();
        }
        catch (Exception ex) {
            return false;
        }
    }

    public static String strip(String str) {
        str = str.replaceAll("<[^>]*>", "");
        str = str.trim();
        return str;
    }

    public static void executePrepared(final String query, final Object... args) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, (Runnable)new Runnable() {
            @Override
            public void run() {
                if (!MySQL.hasConnected()) {
                    MySQL.connect();
                }
                PreparedStatement preparedStatement = null;
                try {
                    Logger.debug(MySQL.strip(query));
                    preparedStatement = MySQL.connection.prepareStatement(MySQL.strip(query));
                    int i = 1;
                    Object[] args1;
                    for (int length = (args1 = args).length, j = 0; j < length; ++j) {
                        final Object arg = args1[j];
                        preparedStatement.setObject(i, arg);
                        ++i;
                    }
                    preparedStatement.execute();
                }
                catch (Exception e) {
                    try {
                        preparedStatement.close();
                    } catch (SQLException e1) {
                        e1.printStackTrace();
                    }
                    Logger.error(Lang.getMessage("mysql_error2"));
                } finally {
                    try {
                        preparedStatement.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    public static void executeSync(final String query) {
        if (!hasConnected()) {
            connect();
        }
        Statement preparedStatement = null;
        try {
            Logger.debug(strip(query));
            preparedStatement = MySQL.connection.createStatement();
            preparedStatement.execute(strip(query));
        }
        catch (Exception e) {
            try {
                preparedStatement.close();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            Logger.error(Lang.getMessage("mysql_error2"));
        } finally {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static ResultSet executeQuery(final String query) throws Exception {
        if (!hasConnected()) {
            connect();
        }
        Logger.debug(strip(query));
        return MySQL.connection.createStatement().executeQuery(strip(query));
    }

    public static void getClans() {
        Clan.clans.clear();
        ResultSet resultSet = null;
        try {
            resultSet = executeQuery("SELECT * FROM clan_list");
            while (resultSet.next()) {
                final Clan clan = new Clan(resultSet.getString("name"), resultSet.getString("leader"), new CopyOnWriteArrayList<Member>(), new ConcurrentHashMap<Clan, ClanStanding>(), resultSet.getString("world"), resultSet.getDouble("x"), resultSet.getDouble("y"), resultSet.getDouble("z"), resultSet.getFloat("yaw"), resultSet.getFloat("pitch"), resultSet.getInt("maxplayers"), resultSet.getString("pvp").equals("1"));
                Clan.clans.put(resultSet.getString("name"), clan);
            }
            resultSet.getStatement().close();
            resultSet.close();
            resultSet = executeQuery("SELECT list.name AS clan_name, member.name AS member_name, member.isModer AS moder FROM clan_list AS list JOIN clan_members AS member ON member.clan=list.name");
            while (resultSet.next()) {
                final Clan c = Clan.getClan(resultSet.getString("clan_name"));
                if (c == null) {
                    executePrepared("DELETE FROM clan_members WHERE name=? AND clan=?", resultSet.getString("member_name"), resultSet.getString("clan_name"));
                }
                else {
                    final CopyOnWriteArrayList<Member> members = c.getMembers();
                    members.add(new Member(resultSet.getString("member_name"), resultSet.getString("moder").equals("1")));
                    c.setMembers(members);
                    Clan.clans.put(resultSet.getString("clan_name"), c);
                }
            }
            resultSet.getStatement().close();
            resultSet.close();
            resultSet = executeQuery("SELECT list.name AS clan_name, standing.target AS standing_target, standing.type AS standing_type FROM clan_list AS list JOIN clan_standing AS standing ON standing.clan=list.name");
            while (resultSet.next()) {
                final Clan c = Clan.getClan(resultSet.getString("clan_name"));
                final Clan c2 = Clan.getClan(resultSet.getString("standing_target"));
                if (c == null) {
                    executePrepared("DELETE FROM clan_standing WHERE clan=?", resultSet.getString("clan_name"));
                }
                else if (c2 == null) {
                    executePrepared("DELETE FROM clan_standing WHERE target=?", resultSet.getString("standing_target"));
                }
                else {
                    final ConcurrentHashMap<Clan, ClanStanding> standing = c.getStandings();
                    standing.put(c2, ClanStanding.getByName(resultSet.getString("standing_type")));
                    c.setStandings(standing);
                    Clan.clans.put(resultSet.getString("clan_name"), c);
                }
            }
            Logger.info(Lang.getMessage("clan_loaded"));
        }
        catch (Exception e2) {
            Logger.error(Lang.getMessage("clan_load_error"));
            try {
                resultSet.getStatement().close();
                resultSet.close();
            }
            catch (SQLException e) {
                Logger.error(e.getMessage());
            }
            return;
        }
        finally {
            try {
                resultSet.getStatement().close();
                resultSet.close();
            }
            catch (SQLException e) {
                Logger.error(e.getMessage());
            }
        }
        try {
            resultSet.getStatement().close();
            resultSet.close();
        }
        catch (SQLException e) {
            Logger.error(e.getMessage());
        }
    }

    public static void getClansAsync() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(Main.plugin, (Runnable)new Runnable() {
            @Override
            public void run() {
                Clan.clans.clear();
                ResultSet resultSet = null;
                try {
                    resultSet = MySQL.executeQuery("SELECT * FROM clan_list");
                    while (resultSet.next()) {
                        final Clan clan = new Clan(resultSet.getString("name"), resultSet.getString("leader"), new CopyOnWriteArrayList<Member>(), new ConcurrentHashMap<Clan, ClanStanding>(), resultSet.getString("world"), resultSet.getDouble("x"), resultSet.getDouble("y"), resultSet.getDouble("z"), resultSet.getFloat("yaw"), resultSet.getFloat("pitch"), resultSet.getInt("maxplayers"), resultSet.getString("pvp").equals("1"));
                        Clan.clans.put(resultSet.getString("name"), clan);
                    }
                    resultSet.getStatement().close();
                    resultSet.close();
                    resultSet = MySQL.executeQuery("SELECT list.name AS clan_name, member.name AS member_name, member.isModer AS moder FROM clan_list AS list JOIN clan_members AS member ON member.clan=list.name");
                    while (resultSet.next()) {
                        final Clan c = Clan.getClan(resultSet.getString("clan_name"));
                        if (c == null) {
                            MySQL.executePrepared("DELETE FROM clan_members WHERE name=? AND clan=?", resultSet.getString("member_name"), resultSet.getString("clan_name"));
                        }
                        else {
                            final CopyOnWriteArrayList<Member> members = c.getMembers();
                            members.add(new Member(resultSet.getString("member_name"), resultSet.getString("moder").equals("1")));
                            c.setMembers(members);
                            Clan.clans.put(resultSet.getString("clan_name"), c);
                        }
                    }
                    resultSet.getStatement().close();
                    resultSet.close();
                    resultSet = MySQL.executeQuery("SELECT list.name AS clan_name, standing.target AS standing_target, standing.type AS standing_type FROM clan_list AS list JOIN clan_standing AS standing ON standing.clan=list.name");
                    while (resultSet.next()) {
                        final Clan c = Clan.getClan(resultSet.getString("clan_name"));
                        final Clan c2 = Clan.getClan(resultSet.getString("standing_target"));
                        if (c == null) {
                            MySQL.executePrepared("DELETE FROM clan_standing WHERE clan=?", resultSet.getString("clan_name"));
                        }
                        else if (c2 == null) {
                            MySQL.executePrepared("DELETE FROM clan_standing WHERE target=?", resultSet.getString("standing_target"));
                        }
                        else {
                            final ConcurrentHashMap<Clan, ClanStanding> standing = c.getStandings();
                            standing.put(c2, ClanStanding.getByName(resultSet.getString("standing_type")));
                            c.setStandings(standing);
                            Clan.clans.put(resultSet.getString("clan_name"), c);
                        }
                    }
                }
                catch (Exception e2) {
                    Logger.error(Lang.getMessage("clan_load_error"));
                    try {
                        resultSet.getStatement().close();
                        resultSet.close();
                        }
                    catch (SQLException e) {
                        Logger.error(e.getMessage());
                    }
                    return;
                }
                finally {
                    try {
                        resultSet.getStatement().close();
                        resultSet.close();
                        }
                    catch (SQLException e) {
                        Logger.error(e.getMessage());
                    }
                }
                try {
                    resultSet.getStatement().close();
                    resultSet.close();
                    }
                catch (SQLException e) {
                    Logger.error(e.getMessage());
                }
            }
        }, 1200L, 1200L);
    }

    public static void disconnect() {
        try {
            if (MySQL.connection != null) {
                MySQL.connection.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
