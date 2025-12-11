package com.example;

/**
 * Placeholder ExampleMod for the `forge` subproject.
 *
 * The original implementation targets Fabric and references Fabric/Minecraft
 * APIs that are not available until ForgeGradle and mappings are configured.
 * Replace this placeholder with a proper Forge port when ready.
 */
public class ExampleMod {
	public static final String MOD_ID = "cmdmaker";

	// Minimal no-op initialization method. Real Forge code should be added
	// in the proper Forge entrypoint and event handlers.
	public void onInitialize() {
		// Placeholder: nothing here yet.
	}
}


	/**
	 * Register /syntax command to view and test custom syntax definitions
	 */
	private void registerSyntaxCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(
			net.minecraft.server.command.CommandManager.literal("syntax")
				.executes(ctx -> {
					ServerCommandSource source = ctx.getSource();
					Map<String, CommandSyntax> syntaxes = SyntaxManager.getAllSyntaxes();
					if (syntaxes.isEmpty()) {
						source.sendFeedback(() -> net.minecraft.text.Text.literal("§cNo custom syntaxes defined."), false);
						return 0;
					}
					source.sendFeedback(() -> net.minecraft.text.Text.literal("§6Available Custom Syntaxes:"), false);
					for (String name : syntaxes.keySet()) {
						CommandSyntax syntax = syntaxes.get(name);
						String desc = syntax.getDescription().isEmpty() ? "" : " - " + syntax.getDescription();
						source.sendFeedback(() -> net.minecraft.text.Text.literal("  §f" + name + "§7: §e" + syntax.getPattern() + desc), false);
					}
					return 1;
				})
		);
	}
}