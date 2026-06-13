package com.example;

public class ManifestEntry {
    public final String description;
    public final String icon;

    public ManifestEntry(String description, String icon) {
        this.description = description != null ? description : "";
        this.icon = icon;
    }
}
