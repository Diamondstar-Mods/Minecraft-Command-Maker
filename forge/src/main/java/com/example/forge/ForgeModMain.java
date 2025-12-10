package com.example.forge;

/**
 * Minimal Forge entrypoint class. This class intentionally does not reference
 * any Forge API types so it can be compiled without Forge on the classpath.
 *
 * It exists so the FORGE jar contains at least one class matching the
 * `mods.toml` entrypoint, preventing Forge from rejecting the jar as "mods
 * that were not found". This is a placeholder — we'll port full Forge
 * functionality later.
 */
public class ForgeModMain {
    public ForgeModMain() {
        // Avoid doing Forge-specific logic here; keep constructor inert.
        System.out.println("CMDMaker Forge stub loaded (no-op)");
    }
}
