package com.example;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;

public class PlaceholderManager {
    private static Logger logger;
    private static boolean papiAvailable = false;
    private static final Map<String, Function<Player, String>> customPlaceholders = new LinkedHashMap<>();

    public static void init(Plugin plugin) { init(plugin.getLogger()); }
    public static void init(Logger log) {
        logger = log;
        try { Class.forName("me.clip.placeholderapi.PlaceholderAPI"); papiAvailable=true; logger.info("PlaceholderAPI detected — bridging placeholders"); }
        catch(ClassNotFoundException e){ papiAvailable=false; logger.info("PlaceholderAPI not found — using built-in only"); }
    }

    public static String resolve(String text, Player player) {
        if (text == null || text.isEmpty()) return text;
        // Try PAPI first if available (via reflection)
        if (papiAvailable) {
            try {
                Class<?> papiClass = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
                var method = papiClass.getMethod("setPlaceholders", Player.class, String.class);
                text = (String) method.invoke(null, player, text);
            } catch(Exception ignored){}
        }
        // Then resolve our built-in ones
        if (!text.contains("%")) return text;
        StringBuilder sb = new StringBuilder(); int i=0;
        while (i < text.length()) {
            int pct = text.indexOf('%', i);
            if (pct < 0) { sb.append(text.substring(i)); break; }
            sb.append(text, i, pct);
            int close = text.indexOf('%', pct+1);
            if (close < 0) { sb.append(text.substring(pct)); break; }
            String ph = text.substring(pct+1, close);
            String r = resolveSingle(ph, player);
            sb.append(r != null ? r : "%"+ph+"%");
            i = close+1;
        }
        return sb.toString();
    }

    private static String resolveSingle(String ph, Player p) {
        String k = ph.toLowerCase();
        if (p != null) {
            switch(k) {
                case "player": return p.getName();
                case "player_uuid": return p.getUniqueId().toString();
                case "player_health": return String.format("%.1f", p.getHealth());
                case "player_max_health": return String.format("%.1f", p.getMaxHealth());
                case "player_xp": return String.valueOf(p.getTotalExperience());
                case "player_xp_level": return String.valueOf(p.getLevel());
                case "player_food": return String.valueOf(p.getFoodLevel());
                case "player_world": return p.getWorld().getName();
                case "player_gamemode": return p.getGameMode().name().toLowerCase();
                case "player_ping": return String.valueOf(p.getPing());
            }
            if (k.startsWith("cooldown_")) { return String.valueOf(CooldownManager.getRemaining(ph.substring(9), p)); }
        }
        switch(k) {
            case "server_online": return String.valueOf(Bukkit.getOnlinePlayers().size());
            case "server_max": return String.valueOf(Bukkit.getMaxPlayers());
            case "server_tps": try { return String.format("%.1f", java.lang.reflect.Array.getDouble(Bukkit.class.getMethod("getTPS").invoke(null),0)); } catch(Exception e){ return "20.0"; }
        }
        if (p != null && k.startsWith("var_")) { return "TODO"; }
        Function<Player,String> c = customPlaceholders.get(k);
        if (c != null) try { return c.apply(p); } catch(Exception e){ logger.warning("Custom placeholder %"+ph+"% error: "+e.getMessage()); }
        return null;
    }

    public static void registerCustom(String name, Function<Player,String> r) { customPlaceholders.put(name.toLowerCase(), r); }
    public static boolean isPAPIAvailable() { return papiAvailable; }
}
