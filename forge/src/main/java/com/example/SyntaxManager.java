package com.example;

import java.util.Collections;
import java.util.Map;

/**
 * Placeholder SyntaxManager for the `forge` subproject.
 * The real implementation depends on Gson and other libraries which will be
 * wired in when the ForgeGradle setup is completed.
 */
public class SyntaxManager {
	public static void loadSyntaxDefinitions() {
		// No-op placeholder
	}

	public static Map<String, CommandSyntax> getAllSyntaxes() {
		return Collections.emptyMap();
	}

	public static SyntaxMatch matchInput(String input) {
		return null;
	}

	public static class SyntaxMatch {
		public final String syntaxName;
		public final CommandSyntax syntax;
		public final Map<String, String> parameters;

		public SyntaxMatch(String syntaxName, CommandSyntax syntax, Map<String, String> parameters) {
			this.syntaxName = syntaxName;
			this.syntax = syntax;
			this.parameters = parameters;
		}
	}
}
