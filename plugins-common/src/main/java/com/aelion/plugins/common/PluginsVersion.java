package com.aelion.plugins.common;

/**
 * Shared version for first-party cloud plugins.
 * Keep in sync with {@code gradle.properties} until build-time injection exists.
 */
public final class PluginsVersion {

    public static final String VERSION = "0.2.0"; // x-release-please-version

    private PluginsVersion() {
    }
}
