package com.aelion.plugins.signs.config;

public enum AnimationSyncMode {
    GLOBAL,
    PER_SIGN;

    public static AnimationSyncMode fromConfig(String raw) {
        if (raw != null && "per-sign".equalsIgnoreCase(raw.trim())) {
            return PER_SIGN;
        }
        return GLOBAL;
    }
}
