package com.aelion.plugins.signs.config;

import java.util.Collections;
import java.util.List;

public final class SignLayoutsHolder {

    private final double animationsPerSecond;
    private final AnimationMode mode;
    private final List<SignLayoutFrame> frames;

    public SignLayoutsHolder(double animationsPerSecond, AnimationMode mode, List<SignLayoutFrame> frames) {
        this.animationsPerSecond = Math.max(0, animationsPerSecond);
        this.mode = mode == null ? AnimationMode.LOOP : mode;
        if (frames == null || frames.isEmpty()) {
            this.frames = Collections.singletonList(
                    new SignLayoutFrame(Collections.<String>emptyList(), null));
        } else {
            this.frames = Collections.unmodifiableList(frames);
        }
    }

    public double animationsPerSecond() {
        return animationsPerSecond;
    }

    public AnimationMode mode() {
        return mode;
    }

    public List<SignLayoutFrame> frames() {
        return frames;
    }

    public SignLayoutFrame frameAt(long animationTick) {
        int n = frames.size();
        if (n == 1 || animationsPerSecond <= 0) {
            return frames.get(0);
        }
        if (mode == AnimationMode.PINGPONG && n > 1) {
            int cycle = (n - 1) * 2;
            int i = (int) (animationTick % cycle);
            if (i < 0) {
                i = 0;
            }
            if (i >= n) {
                i = cycle - i;
            }
            return frames.get(i);
        }
        int index = (int) (animationTick % n);
        if (index < 0) {
            index = 0;
        }
        return frames.get(index);
    }

    public static SignLayoutsHolder empty() {
        return new SignLayoutsHolder(0, AnimationMode.LOOP, Collections.<SignLayoutFrame>emptyList());
    }
}
