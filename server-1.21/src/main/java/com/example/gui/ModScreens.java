package com.example.gui;

import net.minecraft.screen.ScreenHandlerType;

public class ModScreens {
    // Use vanilla 9x6 chest type — works on ANY client without the mod installed
    public static final ScreenHandlerType<FunctionChestHandler> FUNCTION_CHEST =
        (ScreenHandlerType<FunctionChestHandler>) (Object) ScreenHandlerType.GENERIC_9X6;

    public static void register() {
        // No registration needed — vanilla type already exists
    }
}
