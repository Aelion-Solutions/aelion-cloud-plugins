package com.aelion.plugins.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PlaceholdersTest {

    @Test
    void replacesTokens() {
        Map<String, String> values = new HashMap<String, String>();
        values.put("name", "Lobby");
        values.put("online", "3");
        values.put("max", "20");
        String out = Placeholders.apply("Hello %name% (%online%/%max%)", values);
        assertEquals("Hello Lobby (3/20)", out);
    }
}
