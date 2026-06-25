package com.example;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Cross-platform scheduler that works on Paper, Folia, Purpur, and Spigot.
 *
 * Folia replaced {@code Bukkit.getScheduler()} with region-based schedulers.
 * This helper detects the platform and delegates to the correct API.
 */
public class SchedulerHelper {

    private static Boolean folia = null;
    private static Object globalRegionScheduler = null;
    private static Object asyncScheduler = null;

    public static boolean isFolia() {
        if (folia == null) {
            try {
                Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
                folia = true;
            } catch (ClassNotFoundException e) {
                folia = false;
            }
        }
        return folia;
    }

    /**
     * Run a task on the next server tick (global region on Folia).
     */
    public static void runTask(Plugin plugin, Runnable task) {
        if (isFolia()) {
            runFoliaGlobal(plugin, task);
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    /**
     * Run a task after a delay (global region on Folia).
     */
    public static void runTaskLater(Plugin plugin, Runnable task, long delayTicks) {
        if (isFolia()) {
            runFoliaGlobalDelayed(plugin, task, delayTicks);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
        }
    }

    // -- Folia reflection helpers --

    private static void runFoliaGlobal(Plugin plugin, Runnable task) {
        try {
            if (globalRegionScheduler == null) {
                globalRegionScheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
            }
            // GlobalRegionScheduler.run(Plugin, Consumer<ScheduledTask>)
            globalRegionScheduler.getClass()
                .getMethod("run", Plugin.class, Consumer.class)
                .invoke(globalRegionScheduler, plugin, (Consumer<Object>) t -> task.run());
        } catch (Exception e) {
            // Fallback — shouldn't happen if isFolia() was true
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    private static void runFoliaGlobalDelayed(Plugin plugin, Runnable task, long delayTicks) {
        try {
            if (globalRegionScheduler == null) {
                globalRegionScheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
            }
            // GlobalRegionScheduler.runDelayed(Plugin, Consumer<ScheduledTask>, long)
            globalRegionScheduler.getClass()
                .getMethod("runDelayed", Plugin.class, Consumer.class, long.class)
                .invoke(globalRegionScheduler, plugin, (Consumer<Object>) t -> task.run(), delayTicks);
        } catch (Exception e) {
            Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
        }
    }
}
