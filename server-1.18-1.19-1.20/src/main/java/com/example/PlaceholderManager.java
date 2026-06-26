package com.example;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.function.Function;

public class PlaceholderManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("cmdmaker-placeholders");
    private static final Map<String, Function<PlaceholderContext, String>> customPlaceholders = new LinkedHashMap<>();

    public static class PlaceholderContext {
        public final ServerPlayerEntity player; public final MinecraftServer server;
        public PlaceholderContext(ServerPlayerEntity p) { player=p; server=p!=null?p.getCommandSource().getServer():null; }
        public PlaceholderContext(ServerPlayerEntity p, MinecraftServer s) { player=p; server=s; }
    }

    public static void initialize() { LOGGER.info("PlaceholderManager initialized (legacy — no PAPI bridge)"); }

    public static String resolvePlaceholders(String text, ServerPlayerEntity player) {
        if (text==null||text.isEmpty()||!text.contains("%")) return text;
        PlaceholderContext ctx = new PlaceholderContext(player);
        StringBuilder sb = new StringBuilder(); int i=0;
        while (i<text.length()) { int pct=text.indexOf('%',i); if(pct<0){sb.append(text.substring(i));break;} sb.append(text,i,pct); int close=text.indexOf('%',pct+1); if(close<0){sb.append(text.substring(pct));break;} String ph=text.substring(pct+1,close); String r=resolveSingle(ph,ctx); sb.append(r!=null?r:"%"+ph+"%"); i=close+1; }
        return sb.toString();
    }
    public static String resolvePlaceholders(String text, net.minecraft.server.command.ServerCommandSource src) { ServerPlayerEntity p=null; try{p=src.getPlayer();}catch(Exception ignored){} return resolvePlaceholders(text,p); }
    public static void registerCustomPlaceholder(String n, Function<PlaceholderContext,String> r) { customPlaceholders.put(n.toLowerCase(),r); }
    public static boolean unregisterCustomPlaceholder(String n) { return customPlaceholders.remove(n.toLowerCase())!=null; }
    public static boolean isPlaceholderAPIAvailable() { return false; }

    private static String resolveSingle(String ph, PlaceholderContext ctx) {
        String k=ph.toLowerCase();
        if(ctx.player!=null){ switch(k){ case "player":return ctx.player.getName().getString(); case "player_uuid":return ctx.player.getUuid().toString(); case "player_health":return String.format("%.1f",ctx.player.getHealth()); case "player_max_health":return String.format("%.1f",ctx.player.getMaxHealth()); case "player_xp":return String.valueOf(ctx.player.totalExperience); case "player_xp_level":return String.valueOf(ctx.player.experienceLevel); case "player_food":return String.valueOf(ctx.player.getHungerManager().getFoodLevel()); case "player_world":return ctx.player.getCommandSource().getWorld().getRegistryKey().getValue().toString(); case "player_gamemode":return ctx.player.interactionManager.getGameMode().name(); case "player_ping":return String.valueOf(ctx.player.networkHandler.getLatency()); } if(k.startsWith("var_")){return"TODO";} if(k.startsWith("cooldown_")){return String.valueOf(CooldownManager.getRemaining(ph.substring(9),ctx.player));} }
        if(ctx.server!=null){ switch(k){ case "server_online":return String.valueOf(ctx.server.getPlayerManager().getPlayerList().size()); case "server_max":return String.valueOf(ctx.server.getPlayerManager().getMaxPlayerCount()); case "server_tps":return String.format("%.1f",getAvgTPS(ctx.server)); } }
        Function<PlaceholderContext,String> c=customPlaceholders.get(k); if(c!=null)try{return c.apply(ctx);}catch(Exception e){LOGGER.warn("Custom placeholder error: {}",e.getMessage());}
        return null;
    }
    private static float getAvgTPS(MinecraftServer s){ try{ long[] t=s.getTickTimes(); if(t!=null&&t.length>0){ long sum=0;int cnt=0;for(int i=t.length-1;i>=0&&cnt<100;i--){sum+=t[i];cnt++;} if(cnt>0){float avg=sum/(float)cnt/1000000f;return avg>0?Math.min(1000f/avg,20f):20f;} } }catch(Exception ignored){} return 20.0f; }
}
