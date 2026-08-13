package com.aelion.plugins.signs.platform;

import org.bukkit.block.Sign;

/**
 * Shared 1.8-era sign line writes. Modern bands prefer {@link SignsPlatform#writeSignLines}.
 */
final class SignWrites {

    private SignWrites() {
    }

    static String lineAt(String[] lines, int index) {
        if (lines == null || index < 0 || index >= lines.length || lines[index] == null) {
            return "";
        }
        return lines[index];
    }

    @SuppressWarnings("deprecation")
    static boolean writeLegacy(Sign sign, String[] lines) {
        for (int i = 0; i < 4; i++) {
            sign.setLine(i, lineAt(lines, i));
        }
        return sign.update(true);
    }
}
