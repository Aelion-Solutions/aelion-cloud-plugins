package com.aelion.plugins.signs.config;

/**
 * Visual occupancy / lifecycle state for a wall sign.
 */
public enum SignLayoutState {
    SEARCHING,
    STARTING,
    EMPTY,
    ONLINE,
    FULL;

    public String configKey() {
        return name().toLowerCase();
    }

    public static SignLayoutState fromConfigKey(String key) {
        if (key == null) {
            return SEARCHING;
        }
        return switch (key.trim().toLowerCase()) {
            case "starting" -> STARTING;
            case "empty" -> EMPTY;
            case "online" -> ONLINE;
            case "full" -> FULL;
            default -> SEARCHING;
        };
    }
}
