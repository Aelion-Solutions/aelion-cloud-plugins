package com.aelion.plugins.signs.util;

/**
 * Java 8–friendly blank check (String.isBlank is Java 11+).
 */
public final class Strings {

    private Strings() {
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
