package com.example;

import com.google.gson.*;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.zip.*;

public class ModuleManager {
    private static Logger logger;
    private static Path packagesPath;

    public static class ModuleInfo { public String name,version,author,description,cmVersion; }

    public static void init(Plugin plugin) { init(plugin.getDataFolder().toPath(), plugin.getLogger()); }
    public static void init(Path df, Logger log) { logger=log; packagesPath=df.resolve("packages"); try{Files.createDirectories(packagesPath);}catch(Exception e){logger.severe("Failed create packages dir");} }

    public static boolean exportModule(String name, boolean includeFunctions, CommandSender sender) {
        try { Files.createDirectories(packagesPath); Path out = packagesPath.resolve(name+".cmk");
            try(ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(out.toFile()))) {
                JsonObject meta = new JsonObject(); meta.addProperty("name",name); meta.addProperty("version","1.0"); meta.addProperty("author",sender.getName()); meta.addProperty("description","Exported from Command Maker"); meta.addProperty("cmVersion","4.0.0");
                addZip(zos,"module.json",new GsonBuilder().setPrettyPrinting().create().toJson(meta).getBytes());
                if(!AliasManager.getAliases().isEmpty()){ JsonObject a=new JsonObject(); for(Map.Entry<String,String> e:AliasManager.getAliases().entrySet()) a.addProperty(e.getKey(),e.getValue()); addZip(zos,"aliases.json",new GsonBuilder().setPrettyPrinting().create().toJson(a).getBytes()); }
                Path fDir=packagesPath.getParent().resolve("Functions"); if(includeFunctions&&Files.exists(fDir)){ try(DirectoryStream<Path> s=Files.newDirectoryStream(fDir,"*.mcfunction")){ for(Path f:s) addZip(zos,"functions/"+f.getFileName().toString(),Files.readAllBytes(f)); } }
            } sender.sendMessage("§a✔ Package exported: §f"+name+".cmk"); return true;
        } catch(Exception e){ logger.severe("Export failed: "+e.getMessage()); sender.sendMessage("§c✖ Export failed: §7"+e.getMessage()); return false; } }

    public static boolean importModule(String name, boolean overwrite, CommandSender sender) {
        try { Path f = packagesPath.resolve(name+".cmk"); if(!Files.exists(f)){ sender.sendMessage("§c✖ Package not found: §f"+name+".cmk"); return false; }
            Map<String,byte[]> entries = new LinkedHashMap<>(); ModuleInfo info = new ModuleInfo();
            try(ZipInputStream zis=new ZipInputStream(new FileInputStream(f.toFile()))){ ZipEntry e; byte[] b=new byte[8192]; while((e=zis.getNextEntry())!=null){ ByteArrayOutputStream baos=new ByteArrayOutputStream(); int l; while((l=zis.read(b))>0)baos.write(b,0,l); entries.put(e.getName(),baos.toByteArray()); zis.closeEntry(); } }
            if(entries.containsKey("module.json")){ JsonObject m=JsonParser.parseString(new String(entries.get("module.json"))).getAsJsonObject(); info.name=m.has("name")?m.get("name").getAsString():name; info.version=m.has("version")?m.get("version").getAsString():"?"; info.author=m.has("author")?m.get("author").getAsString():"?"; info.description=m.has("description")?m.get("description").getAsString():""; }
            List<String> imported=new ArrayList<>(),skipped=new ArrayList<>();
            if(entries.containsKey("aliases.json")){ JsonObject a=JsonParser.parseString(new String(entries.get("aliases.json"))).getAsJsonObject(); for(Map.Entry<String,JsonElement> en:a.entrySet()){ if(AliasManager.hasAlias(en.getKey())&&!overwrite)skipped.add("alias:"+en.getKey()); else{ AliasManager.addAlias(en.getKey(),en.getValue().getAsString()); imported.add("alias:"+en.getKey()); } } }
            for(Map.Entry<String,byte[]> en:entries.entrySet()){ if(en.getKey().startsWith("functions/")&&en.getKey().endsWith(".mcfunction")){ Path dest=packagesPath.getParent().resolve("Functions").resolve(en.getKey().substring(10)); if(Files.exists(dest)&&!overwrite)skipped.add("function:"+en.getKey().substring(10)); else{ Files.createDirectories(dest.getParent()); Files.write(dest,en.getValue(),StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING); imported.add("function:"+en.getKey().substring(10)); } } }
            AliasManager.loadAliases(); sender.sendMessage("§a✔ Imported §f"+info.name+"§a v"+info.version+": §f"+imported.size()+"§a items"); for(String it:imported) sender.sendMessage("  §a+ §f"+it);
            if(!skipped.isEmpty()){ sender.sendMessage("§e⚠ Skipped §f"+skipped.size()+"§e (use --overwrite to replace)"); for(String it:skipped) sender.sendMessage("  §7- §f"+it); }
            return true;
        } catch(Exception e){ logger.severe("Import failed: "+e.getMessage()); sender.sendMessage("§c✖ Import failed: §7"+e.getMessage()); return false; } }

    private static void addZip(ZipOutputStream zos, String name, byte[] data) throws IOException { ZipEntry e=new ZipEntry(name); zos.putNextEntry(e); zos.write(data); zos.closeEntry(); }
}
