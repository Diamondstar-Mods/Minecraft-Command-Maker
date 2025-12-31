package com.example;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a custom command syntax definition
 * Example: "/tpa <player>" with alias "tpa_request" -> "tp ${tpa_player} @s"
 */
public class CommandSyntax {
	private String name;
	private String pattern; // e.g., "/tpa <player>"
	private List<String> parameters; // e.g., ["player"]
	private String description;

	public CommandSyntax(String name, String pattern, String description) {
		this.name = name;
		this.pattern = pattern;
		this.description = description;
		this.parameters = parseParameters(pattern);
	}

	/**
	 * Parse parameter names from pattern
	 * Extracts anything between < and >
	 */
	private List<String> parseParameters(String pattern) {
		List<String> params = new ArrayList<>();
		Pattern paramPattern = Pattern.compile("<([^>]+)>");
		Matcher matcher = paramPattern.matcher(pattern);
		while (matcher.find()) {
			params.add(matcher.group(1));
		}
		return params;
	}

	/**
	 * Convert pattern to regex for matching user input
	 * "/tpa <player>" -> "^/tpa (.+)$"
	 */
	public Pattern getRegexPattern() {
		String regex = pattern
			.replaceAll("<[^>]+>", "(.+)") // Replace <param> with (.+)
			.replaceAll("/", "\\\\/"); // Escape forward slashes
		return Pattern.compile("^" + regex + "$");
	}

	/**
	 * Extract parameter values from user input
	 * Input: "/tpa steve"
	 * Returns: {"player": "steve"}
	 */
	public Map<String, String> extractParameters(String input) {
		Map<String, String> result = new HashMap<>();
		Pattern regex = getRegexPattern();
		Matcher matcher = regex.matcher(input);

		if (matcher.find()) {
			for (int i = 0; i < parameters.size(); i++) {
				result.put(parameters.get(i), matcher.group(i + 1));
			}
		}
		return result;
	}

	/**
	 * Substitute parameters into a command string
	 * Command: "tp ${tpa_player} @s"
	 * Parameters: {"player": "steve"}
	 * Result: "tp steve @s"
	 */
	public String substituteParameters(String command, Map<String, String> parameters) {
		String result = command;
		for (String param : this.parameters) {
			String placeholder = "${" + name + "_" + param + "}";
			String value = parameters.getOrDefault(param, "");
			result = result.replace(placeholder, value);
		}
		return result;
	}

	// Getters
	public String getName() { return name; }
	public String getPattern() { return pattern; }
	public List<String> getParameters() { return parameters; }
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
}
