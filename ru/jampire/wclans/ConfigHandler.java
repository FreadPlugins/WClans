package ru.jampire.wclans;

import java.nio.charset.*;
import java.nio.file.*;
import java.nio.*;
import org.apache.commons.lang.*;
import org.bukkit.*;
import java.io.*;
import org.bukkit.configuration.file.*;

public class ConfigHandler
{
    public static void winConvert(final File f) throws Exception {
        final Path p = Paths.get(f.toURI());
        ByteBuffer bb = ByteBuffer.wrap(Files.readAllBytes(p));
        final CharBuffer cb = Charset.forName("UTF-8").decode(bb);
        bb = Charset.forName("windows-1251").encode(cb);
        Files.write(p, bb.array(), new OpenOption[0]);
    }
    
    public static void configInit() {
        final File fconfig = new File(Main.plugin.getDataFolder(), "config.yml");
        final File flang = new File(Main.plugin.getDataFolder(), "lang.yml");
        if (!Main.plugin.getDataFolder().mkdirs()) {
            Main.plugin.getDataFolder().mkdirs();
        }
        if (!fconfig.exists()) {
            final InputStream config = Main.class.getResourceAsStream("/config.yml");
            try {
                final FileOutputStream fos = new FileOutputStream(fconfig);
                byte[] buff = new byte[65536];
                int n;
                while ((n = config.read(buff)) > 0) {
                    fos.write(buff, 0, n);
                    fos.flush();
                }
                fos.close();
                buff = null;
                if (SystemUtils.IS_OS_WINDOWS) {
                    winConvert(fconfig);
                }
            }
            catch (Exception e) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Error: " + e);
            }
        }
        if (!flang.exists()) {
            final InputStream lang = Main.class.getResourceAsStream("/lang.yml");
            try {
                final FileOutputStream fos = new FileOutputStream(flang);
                byte[] buff = new byte[65536];
                int n;
                while ((n = lang.read(buff)) > 0) {
                    fos.write(buff, 0, n);
                    fos.flush();
                }
                fos.close();
                buff = null;
                if (SystemUtils.IS_OS_WINDOWS) {
                    winConvert(flang);
                }
            }
            catch (Exception e) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Error: " + e);
            }
        }
        Main.config = (FileConfiguration)YamlConfiguration.loadConfiguration(fconfig);
        Lang.load(YamlConfiguration.loadConfiguration(flang));
    }
}
