package com.example.gui;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class ModScreens {
    public static final ScreenHandlerType<FunctionChestHandler> FUNCTION_CHEST =
        new ScreenHandlerType<FunctionChestHandler>(FunctionChestHandler::new, net.minecraft.resource.featuretoggle.FeatureSet.empty());

    public static void register() {
        Registry.register(Registries.SCREEN_HANDLER,
            new Identifier("nekkycommandmaker", "function_chest"),
            FUNCTION_CHEST);
    }
}
