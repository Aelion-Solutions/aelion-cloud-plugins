package com.aelion.plugins.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.junit.jupiter.api.Test;

class PlaceholdersTest {

    @Test
    void replacesTokens() {
        String out = Placeholders.apply("Hello %name% (%online%/%max%)", Map.of(
                "name", "Lobby",
                "online", "3",
                "max", "20"
        ));
        assertEquals("Hello Lobby (3/20)", out);
    }
}
