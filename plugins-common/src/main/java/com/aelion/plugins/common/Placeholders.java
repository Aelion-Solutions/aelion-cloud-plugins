package com.aelion.plugins.common;

import java.util.Map;

/**
 * Simple %placeholder% replacement for sign lines and messages.
 */
public final class Placeholders {

    private Placeholders() {
    }

    public static String apply(String template, Map<String, String> values) {
        if (template == null) {
            return "";
        }
        if (values == null || values.isEmpty()) {
            return template;
        }
        String out = template;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String key = entry.getKey();
            String token = key.startsWith("%") ? key : "%" + key + "%";
            out = out.replace(token, entry.getValue() == null ? "" : entry.getValue());
        }
        return out;
    }
}
