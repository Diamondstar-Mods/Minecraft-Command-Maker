package com.example.mixin;

import com.example.CommandMakerRift;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    // This method has no obfuscation issues since we're using the intermediary name via Loom
    @Inject(method = "method_3735", at = @At("RETURN"), remap = false)
    private void afterLoadWorld(CallbackInfo ci) {
        CommandMakerRift.registerCommands((MinecraftServer) (Object) this);
    }
}
