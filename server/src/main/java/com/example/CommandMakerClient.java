package com.example;

import net.fabricmc.api.ClientModInitializer;
import com.example.gui.ModScreens;
import com.example.gui.FunctionChestScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class CommandMakerClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HandledScreens.register(ModScreens.FUNCTION_CHEST, FunctionChestScreen::new);
    }
}
