package com.aelion.plugins.signs.config;

public enum ForcefieldShape {
    SPHERE,
    BOX;

    public static ForcefieldShape fromConfig(String raw) {
        if (raw != null && "box".equalsIgnoreCase(raw.trim())) {
            return BOX;
        }
        return SPHERE;
    }
}
