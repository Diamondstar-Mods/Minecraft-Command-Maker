package com.example;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandSyntax {
    private final String name;
    private final String pattern;
    private final List<String> parameters;
    private String description;

    public CommandSyntax(String name, String pattern, String description) {
        this.name = name;
        this.pattern = pattern;
        this.description = description;
        this.parameters = parseParameters(pattern);
    }

    private List<String> parseParameters(String pattern) {
        List<String> params = new ArrayList<>();
        Pattern paramPattern = Pattern.compile("<([^>]+)>");
        Matcher matcher = paramPattern.matcher(pattern);
        while (matcher.find()) {
            params.add(matcher.group(1));
        }
        return params;
    }

    public Pattern getRegexPattern() {
        String regex = pattern
            .replaceAll("<[^>]+>", "(.+)")
            .replaceAll("/", "\\\\/");
        return Pattern.compile("^" + regex + "$");
    }

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

    public String substituteParameters(String command, Map<String, String> params) {
        String result = command;
        for (String param : this.parameters) {
            String placeholder = "${" + name + "_" + param + "}";
            String value = params.getOrDefault(param, "");
            result = result.replace(placeholder, value);
        }
        return result;
    }

    public String getName() { return name; }
    public String getPattern() { return pattern; }
    public List<String> getParameters() { return parameters; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
