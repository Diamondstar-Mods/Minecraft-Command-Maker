package com.example;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import com.example.gui.AliasDeleteScreen;
import net.minecraft.client.MinecraftClient;

import java.util.HashMap;
import java.util.Map;

public class ExampleModClient implements ClientModInitializer {
    private static Map<String, String> aliases = new HashMap<>();

    public static void setAliases(Map<String, String> aliasMap) {
        aliases = new HashMap<>(aliasMap);
    }

    @Override
    public void onInitializeClient() {
        // Register /deletealiases-gui command to open the alias deletion screen
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("deletealiases-gui")
                    .executes(context -> {
                        MinecraftClient client = MinecraftClient.getInstance();
                        // Send a command to sync aliases from server
                        if (client.player != null) {
                            client.player.networkHandler.sendChatMessage("/deletealias");
                        }
                        // Open the GUI - it will show whatever aliases are currently loaded
                        client.setScreen(new AliasDeleteScreen(aliases, client.currentScreen));
                        return 1;
                    })
            );
        });
    }
}
