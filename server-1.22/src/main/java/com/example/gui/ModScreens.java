package com.example.gui;

import net.minecraft.world.inventory.MenuType;

public class ModScreens {
    // Use vanilla 9x6 chest type — works on ANY client without the mod installed
    @SuppressWarnings("unchecked")
    public static final MenuType<FunctionChestHandler> FUNCTION_CHEST =
        (MenuType<FunctionChestHandler>) (Object) MenuType.GENERIC_9x6;

    public static void register() {
        // No registration needed — vanilla type already exists
    }
}
