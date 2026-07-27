package com.aelion.plugins.signs.config;

public enum AnimationMode {
    LOOP,
    PINGPONG;

    public static AnimationMode fromConfig(String raw) {
        if (raw != null && "pingpong".equalsIgnoreCase(raw.trim())) {
            return PINGPONG;
        }
        return LOOP;
    }
}
