package com.aelion.plugins.signs.runtime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aelion.plugins.signs.config.AnimationMode;
import com.aelion.plugins.signs.config.SignLayoutFrame;
import com.aelion.plugins.signs.config.SignLayoutsHolder;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class SignRendererTest {

    @Test
    void staticLayoutDoesNotPaintOnAnimationTick() {
        SignLayoutsHolder staticLayout = new SignLayoutsHolder(
                0,
                AnimationMode.LOOP,
                Collections.singletonList(new SignLayoutFrame(Collections.<String>emptyList(), null))
        );
        assertFalse(SignRenderer.paintsOnAnimationTick(staticLayout));
        assertFalse(SignRenderer.paintsOnAnimationTick(SignLayoutsHolder.empty()));
        assertFalse(SignRenderer.paintsOnAnimationTick(null));
    }

    @Test
    void animatedLayoutPaintsOnAnimationTick() {
        SignLayoutsHolder animated = new SignLayoutsHolder(
                1.0,
                AnimationMode.LOOP,
                Collections.singletonList(new SignLayoutFrame(Collections.<String>emptyList(), null))
        );
        assertTrue(SignRenderer.paintsOnAnimationTick(animated));
    }
}
