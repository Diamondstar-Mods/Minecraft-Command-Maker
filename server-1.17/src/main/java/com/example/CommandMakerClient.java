package com.example;

import net.fabricmc.api.ClientModInitializer;
import com.example.gui.ModScreens;
import com.example.gui.FunctionChestScreen;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;

public class CommandMakerClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ScreenRegistry.register(ModScreens.FUNCTION_CHEST, FunctionChestScreen::new);
    }
}
