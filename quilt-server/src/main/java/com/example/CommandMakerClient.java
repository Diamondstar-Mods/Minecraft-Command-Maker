package com.example;

import net.fabricmc.api.ClientModInitializer;
import com.example.gui.AliasDeleteScreen;
import net.minecraft.client.MinecraftClient;

import java.util.HashMap;
import java.util.Map;

public class CommandMakerClient implements ClientModInitializer {
	private static Map<String, String> aliases = new HashMap<>();

	public static void setAliases(Map<String, String> aliasMap) {
		aliases = new HashMap<>(aliasMap);
	}

	public static Map<String, String> getAliases() {
		return new HashMap<>(aliases);
	}

	@Override
	public void onInitializeClient() {
		// Client-side initialization goes here
	}
}

