package com.example;

import com.google.inject.Inject;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.nio.file.Path;

/**
 * Sponge entry point for Command Maker.
 * Shares core manager classes (AliasManager, SyntaxManager, PermissionManager,
 * UpdateChecker) with the Paper/Bukkit implementation.
 */
@Plugin("cmdmaker")
public class CommandMakerSponge {

    private final PluginContainer container;
    private final Logger logger;

    @Inject
    public CommandMakerSponge(PluginContainer container, Logger logger) {
        this.container = container;
        this.logger = logger;
    }

    @Listener
    public void onServerStarting(final StartingEngineEvent<?> event) {
        // Use the Sponge config directory for data storage
        Path dataFolder = Path.of("config", "CommandMaker");

        java.util.logging.Logger jLogger = java.util.logging.Logger.getLogger("CommandMaker");

        AliasManager.init(dataFolder, jLogger);
        SyntaxManager.init(dataFolder, jLogger);
        PermissionManager.init(dataFolder);
        UpdateChecker.check(container.metadata().version().toString(), jLogger);

        logger.info("Nek's Command Maker (Sponge) initialized! Use Paper/Bukkit for full GUI support.");
    }
}
