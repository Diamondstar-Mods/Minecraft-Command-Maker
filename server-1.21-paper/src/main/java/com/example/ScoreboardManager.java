package com.example;

import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;

public class ScoreboardManager {
    private static Logger logger;
    private static Path configFile;
    private static Plugin plugin;
    private static final Map<String, ScoreboardConfig> config = new LinkedHashMap<>();
    private static int tickCounter = 0;
    private static boolean initialized = false;

    public static class ScoreboardConfig {
        public String name, displayName, slot; public int updateInterval; public boolean enabled; public List<String> lines;
        public ScoreboardConfig(String n) { name=n; displayName="Scoreboard"; slot="sidebar"; updateInterval=20; enabled=false; lines=new ArrayList<>(); }
    }

    public static void init(Plugin pl) { init(pl.getDataFolder().toPath(), pl.getLogger(), pl); }
    public static void init(Path df, Logger log, Plugin pl) { logger=log; configFile=df.resolve("scoreboards.json"); plugin=pl; loadConfig(); initialized=true; startTicker(); logger.info("ScoreboardManager initialized"); }

    public static void reload() { removeAll(); config.clear(); loadConfig(); createAll(); logger.info("ScoreboardManager reloaded"); }
    public static Map<String, ScoreboardConfig> getConfiguredScoreboards() { return Collections.unmodifiableMap(config); }

    private static void startTicker() {
        new BukkitRunnable(){ public void run(){ tickCounter++; updateAll(); } }.runTaskTimer(plugin, 1L, 1L);
    }

    private static void updateAll() {
        for (ScoreboardConfig cfg : config.values()) {
            if (!cfg.enabled || cfg.updateInterval <= 0 || tickCounter % cfg.updateInterval != 0) continue;
            org.bukkit.scoreboard.Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
            Objective obj = sb.getObjective(cfg.name); if (obj == null) continue;
            String title = resolve(cfg.displayName).replace("&","§"); if (title.length()>32) title=title.substring(0,32);
            obj.setDisplayName(title);
            // Clear old scores
            for (String entry : new HashSet<>(sb.getEntries())) {
                if (entry.startsWith("cm"+hash(cfg.name))) sb.resetScores(entry);
            }
            // Set new lines
            int sv = cfg.lines.size()-1;
            for (int i=0; i<cfg.lines.size()&&i<15; i++) {
                String line = resolve(cfg.lines.get(i)).replace("&","§"); if (line.length()>40) line=line.substring(0,40);
                String entry = "cm"+hash(cfg.name)+"l"+i;
                org.bukkit.scoreboard.Team t = sb.getTeam("cm"+hash(cfg.name)+"t"+i);
                if (t==null) t=sb.registerNewTeam("cm"+hash(cfg.name)+"t"+i);
                t.setPrefix(line); t.addEntry(entry);
                obj.getScore(entry).setScore(sv);
                sv--;
            }
        }
    }

    private static void createAll() {
        org.bukkit.scoreboard.Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        for (ScoreboardConfig cfg : config.values()) {
            if (!cfg.enabled) { logger.info("Scoreboard '"+cfg.name+"' disabled — skipping"); continue; }
            Objective obj = sb.getObjective(cfg.name); if (obj!=null) obj.unregister();
            String title = resolve(cfg.displayName).replace("&","§"); if (title.length()>32) title=title.substring(0,32);
            obj = sb.registerNewObjective(cfg.name, "dummy", title);
            DisplaySlot slot = "list".equals(cfg.slot)?DisplaySlot.PLAYER_LIST:"belowname".equals(cfg.slot)?DisplaySlot.BELOW_NAME:DisplaySlot.SIDEBAR;
            obj.setDisplaySlot(slot);
            logger.info("Created scoreboard '"+cfg.name+"'");
        }
    }

    private static void removeAll() {
        org.bukkit.scoreboard.Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        for (ScoreboardConfig cfg : config.values()) { Objective o=sb.getObjective(cfg.name); if(o!=null)o.unregister(); }
    }

    private static String resolve(String t) {
        if (t==null) return "";
        t=t.replace("${player_count}",String.valueOf(Bukkit.getOnlinePlayers().size()));
        t=t.replace("${server_online}",String.valueOf(Bukkit.getOnlinePlayers().size()));
        t=t.replace("${server_max}",String.valueOf(Bukkit.getMaxPlayers()));
        t=t.replace("${tps}",String.format("%.1f",calcTPS()));
        return t;
    }

    private static double calcTPS() { try { return java.lang.reflect.Array.getDouble(Bukkit.class.getMethod("getTPS").invoke(null),0); } catch(Exception e){ return 20.0; } }
    private static int hash(String s) { int h=0; for(int i=0;i<s.length();i++)h=h*31+s.charAt(i); return Math.abs(h)%100000; }

    private static void loadConfig() {
        config.clear();
        try { if(!Files.exists(configFile)) createDefaultConfig(); String j=new String(Files.readAllBytes(configFile)); JsonElement e=JsonParser.parseString(j);
            if(e.isJsonObject()&&e.getAsJsonObject().has("scoreboards")){ JsonObject s=e.getAsJsonObject().getAsJsonObject("scoreboards");
                for(Map.Entry<String,JsonElement> en:s.entrySet()){ try{ JsonObject o=en.getValue().getAsJsonObject(); ScoreboardConfig c=new ScoreboardConfig(en.getKey());
                    c.displayName=o.has("displayName")?o.get("displayName").getAsString():en.getKey(); c.slot=o.has("slot")?o.get("slot").getAsString():"sidebar";
                    c.updateInterval=o.has("updateInterval")?o.get("updateInterval").getAsInt():20; c.enabled=o.has("enabled")?o.get("enabled").getAsBoolean():false;
                    if(o.has("lines")&&o.get("lines").isJsonArray()) for(JsonElement l:o.getAsJsonArray("lines")) c.lines.add(l.getAsString());
                    config.put(en.getKey(),c);
                }catch(Exception ex){ logger.warning("Bad scoreboard: "+en.getKey()); } } } } catch(Exception ex){ logger.severe("Failed load scoreboards: "+ex.getMessage()); }
    }

    private static void createDefaultConfig() {
        try { Files.createDirectories(configFile.getParent()); JsonObject r=new JsonObject(); JsonObject s=new JsonObject(); JsonObject ex=new JsonObject();
            ex.addProperty("displayName","&6&lServer Info"); ex.addProperty("slot","sidebar"); ex.addProperty("updateInterval",20); ex.addProperty("enabled",false);
            JsonArray l=new JsonArray(); l.add("&6&lMy Server"); l.add("&f"); l.add("&7Players: &a${player_count}/${server_max}"); l.add("&7TPS: &a${tps}"); l.add("&f"); l.add("&7Powered by"); l.add("&eCommand Maker"); ex.add("lines",l);
            s.add("server_info",ex); r.add("scoreboards",s); Files.write(configFile,new GsonBuilder().setPrettyPrinting().create().toJson(r).getBytes(),StandardOpenOption.CREATE_NEW);
        } catch(Exception e){ logger.severe("Failed create scoreboards: "+e.getMessage()); }
    }
}
