package com.example.gui;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.resources.Identifier;

public class ModScreens {
    public static final MenuType<FunctionChestHandler> FUNCTION_CHEST =
        new MenuType<FunctionChestHandler>(FunctionChestHandler::new, net.minecraft.world.flag.FeatureFlagSet.of());

    public static void register() {
        Registry.register(BuiltInRegistries.MENU,
            Identifier.fromNamespaceAndPath("nekkycommandmaker", "function_chest"),
            FUNCTION_CHEST);
    }
}
