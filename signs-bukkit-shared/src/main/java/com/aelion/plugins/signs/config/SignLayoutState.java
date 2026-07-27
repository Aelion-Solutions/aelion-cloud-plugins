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
        String normalized = key.trim().toLowerCase();
        if ("starting".equals(normalized)) {
            return STARTING;
        }
        if ("empty".equals(normalized)) {
            return EMPTY;
        }
        if ("online".equals(normalized)) {
            return ONLINE;
        }
        if ("full".equals(normalized)) {
            return FULL;
        }
        return SEARCHING;
    }
}
