package com.example;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Logger;

public class ConditionManager {
    private static final Logger LOGGER = Logger.getLogger("cmdmaker-conditions");

    public static String evaluate(String command, Player player) {
        if (command == null || !command.startsWith("if:")) return command;
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("^if:([a-z_]+)\\(([^)]*)\\)\\s+(.+)");
        java.util.regex.Matcher m = p.matcher(command);
        if (!m.find()) return "";
        String type = m.group(1), args = m.group(2)!=null?m.group(2).trim():"", rest = m.group(3);
        String trueBranch, elseBranch = null;
        int ei = rest.lastIndexOf(" else ");
        if (ei >= 0) { trueBranch = rest.substring(0,ei).trim(); elseBranch = rest.substring(ei+6).trim(); }
        else trueBranch = rest.trim();

        boolean result = eval(type, args, player);
        return result ? trueBranch : (elseBranch != null ? elseBranch : "");
    }

    private static boolean eval(String type, String args, Player p) {
        try { switch(type) {
            case "xp": return p != null && !args.isEmpty() && p.getLevel() >= Integer.parseInt(args.trim());
            case "health": if(p==null||args.isEmpty())return false; float pc=Float.parseFloat(args.trim()); return (p.getHealth()/p.getMaxHealth()*100f)>=pc;
            case "perm": return !args.isEmpty() && (p==null||p.hasPermission(args.trim()));
            case "hasitem": return checkItem(p, args);
            case "op": return p != null && p.isOp();
            case "player": return p != null && p.getName().equalsIgnoreCase(args.trim());
            case "dimension": return p != null && p.getWorld().getName().equalsIgnoreCase(args.trim());
            case "cooldown": return p != null && CooldownManager.getRemaining(args.trim(), p) <= 0;
            case "var": return true; // simplified — full integration via VariableManager
            default: LOGGER.warning("Unknown condition: "+type); return false;
        } } catch(Exception e){ return false; }
    }

    private static boolean checkItem(Player p, String args) {
        if (p == null || args.isEmpty()) return false;
        String id = args.trim().toUpperCase().replace("MINECRAFT:","");
        Material mat = Material.getMaterial(id);
        if (mat == null) return false;
        return p.getInventory().contains(mat);
    }
}
