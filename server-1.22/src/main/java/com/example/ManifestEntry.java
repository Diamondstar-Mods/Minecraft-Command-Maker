package com.example;

/**
 * Holds manifest data for a downloadable function: description and optional icon item.
 */
public class ManifestEntry {
    public final String description;
    public final String icon; // e.g. "minecraft:diamond_shovel", may be null

    public ManifestEntry(String description, String icon) {
        this.description = description != null ? description : "";
        this.icon = icon;
    }
}
