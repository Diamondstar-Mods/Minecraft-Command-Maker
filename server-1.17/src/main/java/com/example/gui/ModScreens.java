package com.example.gui;

import net.minecraft.util.registry.Registry;

import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class ModScreens {
    public static final ScreenHandlerType<FunctionChestHandler> FUNCTION_CHEST =
        new ScreenHandlerType<FunctionChestHandler>(FunctionChestHandler::new);

    public static void register() {
        Registry.register(Registry.SCREEN_HANDLER,
            new Identifier("nekkycommandmaker", "function_chest"),
            FUNCTION_CHEST);
    }
}
