package com.example.gui;

import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class ModScreens {
    public static final ScreenHandlerType<FunctionChestHandler> FUNCTION_CHEST =
        ScreenHandlerRegistry.registerSimple(
            new Identifier("nekkycommandmaker", "function_chest"),
            (syncId, inventory) -> new FunctionChestHandler(syncId, inventory)
        );

    public static void register() {
        // Registered via ScreenHandlerRegistry above
    }
}
