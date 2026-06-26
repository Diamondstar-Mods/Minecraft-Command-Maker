package com.example;

import com.google.gson.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class CooldownManager {
    private static Logger logger;
    private static Path configFile;
    private static final Map<String, Map<UUID, Long>> playerCooldowns = new ConcurrentHashMap<>();
    private static final Map<String, Long> globalCooldowns = new ConcurrentHashMap<>();
    private static final Map<String, CooldownConfig> config = new LinkedHashMap<>();

    public static class CooldownConfig {
        public String alias; public String type; public int seconds; public String message;
        public CooldownConfig(String a, String t, int s, String m) { alias=a; type=t; seconds=s; message=m!=null?m:"&cWait {remaining}s"; }
    }

    public static void init(Plugin plugin) { init(plugin.getDataFolder().toPath(), plugin.getLogger()); }
    public static void init(Path df, Logger log) { logger=log; configFile=df.resolve("cooldowns.json"); loadConfig(); logger.info("CooldownManager initialized"); }

    public static boolean checkCooldown(String alias, Player player) {
        CooldownConfig cfg = config.get(alias);
        if (cfg == null) return true;
        long now = System.currentTimeMillis();
        if ("global".equals(cfg.type)) {
            Long exp = globalCooldowns.get(alias);
            if (exp != null && now < exp) { sendMsg(player, cfg, (exp-now)/1000); return false; }
            globalCooldowns.put(alias, now+cfg.seconds*1000L);
        } else {
            Map<UUID,Long> m = playerCooldowns.computeIfAbsent(alias, k->new ConcurrentHashMap<>());
            Long exp = m.get(player.getUniqueId());
            if (exp != null && now < exp) { sendMsg(player, cfg, (exp-now)/1000); return false; }
            m.put(player.getUniqueId(), now+cfg.seconds*1000L);
        }
        return true;
    }

    public static long getRemaining(String alias, Player player) {
        CooldownConfig cfg = config.get(alias); if (cfg==null||player==null) return 0;
        long now = System.currentTimeMillis();
        if ("global".equals(cfg.type)) { Long e=globalCooldowns.get(alias); return e!=null&&now<e?(e-now)/1000:0; }
        Map<UUID,Long> m=playerCooldowns.get(alias); if(m==null)return 0;
        Long e=m.get(player.getUniqueId()); return e!=null&&now<e?(e-now)/1000:0;
    }

    public static void setCooldown(String alias, String type, int seconds, String message) {
        config.put(alias, new CooldownConfig(alias, type, seconds, message)); saveConfig();
    }
    public static boolean clearCooldown(String alias) { playerCooldowns.remove(alias); globalCooldowns.remove(alias); boolean r=config.remove(alias)!=null; if(r)saveConfig(); return r; }
    public static Map<String, CooldownConfig> getConfiguredCooldowns() { return Collections.unmodifiableMap(config); }

    private static void sendMsg(Player p, CooldownConfig cfg, long rem) {
        p.sendMessage(cfg.message.replace("{remaining}",String.valueOf(rem)).replace("&","§"));
    }

    private static void loadConfig() {
        config.clear();
        try { if(!Files.exists(configFile)) createDefaultConfig(); String j=new String(Files.readAllBytes(configFile)); JsonElement e=JsonParser.parseString(j);
            if(e.isJsonObject()&&e.getAsJsonObject().has("cooldowns")){ JsonObject c=e.getAsJsonObject().getAsJsonObject("cooldowns");
                for(Map.Entry<String,JsonElement> en:c.entrySet()){ try{ JsonObject o=en.getValue().getAsJsonObject();
                    config.put(en.getKey(),new CooldownConfig(en.getKey(),o.has("type")?o.get("type").getAsString():"player",o.has("seconds")?o.get("seconds").getAsInt():30,o.has("message")?o.get("message").getAsString():null));
                }catch(Exception ex){ logger.warning("Bad cooldown: "+en.getKey()); } } } } catch(Exception ex){ logger.severe("Failed load cooldowns: "+ex.getMessage()); }
    }
    public static void saveConfig() {
        try { Files.createDirectories(configFile.getParent()); JsonObject r=new JsonObject(); JsonObject c=new JsonObject();
            for(CooldownConfig cf:config.values()){ JsonObject o=new JsonObject(); o.addProperty("type",cf.type); o.addProperty("seconds",cf.seconds); o.addProperty("message",cf.message); c.add(cf.alias,o); }
            r.add("cooldowns",c); Files.write(configFile,new GsonBuilder().setPrettyPrinting().create().toJson(r).getBytes(),StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
        } catch(Exception e){ logger.severe("Failed save cooldowns: "+e.getMessage()); }
    }
    private static void createDefaultConfig() {
        try { Files.createDirectories(configFile.getParent()); JsonObject r=new JsonObject(); JsonObject c=new JsonObject(); JsonObject ex=new JsonObject();
            ex.addProperty("type","player"); ex.addProperty("seconds",30); ex.addProperty("message","&cWait {remaining}s before using this again!"); c.add("example",ex);
            r.add("cooldowns",c); Files.write(configFile,new GsonBuilder().setPrettyPrinting().create().toJson(r).getBytes(),StandardOpenOption.CREATE_NEW);
        } catch(Exception e){ logger.severe("Failed create cooldowns config: "+e.getMessage()); }
    }
}
