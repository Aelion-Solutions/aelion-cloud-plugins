package com.aelion.plugins.signs.config;

import java.util.Collections;
import java.util.List;

public final class SignLayoutsHolder {

    private final double animationsPerSecond;
    private final List<SignLayoutFrame> frames;

    public SignLayoutsHolder(double animationsPerSecond, List<SignLayoutFrame> frames) {
        this.animationsPerSecond = Math.max(0, animationsPerSecond);
        this.frames = frames == null || frames.isEmpty()
                ? List.of(new SignLayoutFrame(List.of("", "", "", ""), null))
                : List.copyOf(frames);
    }

    public double animationsPerSecond() {
        return animationsPerSecond;
    }

    public List<SignLayoutFrame> frames() {
        return frames;
    }

    public SignLayoutFrame frameAt(long animationTick) {
        if (frames.size() == 1 || animationsPerSecond <= 0) {
            return frames.get(0);
        }
        int index = (int) (animationTick % frames.size());
        if (index < 0) {
            index = 0;
        }
        return frames.get(index);
    }

    public static SignLayoutsHolder empty() {
        return new SignLayoutsHolder(0, Collections.emptyList());
    }
}
