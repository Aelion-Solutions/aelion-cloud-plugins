package com.aelion.plugins.signs.config;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public final class ForcefieldConfig {

    private final boolean enabled;
    private final ForcefieldShape shape;
    private final double radius;
    private final double height;
    private final double yOffset;
    private final double strength;
    private final double verticalStrength;
    private final long checkIntervalTicks;
    private final long cooldownMs;
    private final String bypassPermission;
    private final Set<SignLayoutState> applyWhen;
    private final boolean particlesEnabled;
    private final String particleType;
    private final int particleCount;
    private final boolean soundEnabled;
    private final String soundType;
    private final float soundVolume;
    private final float soundPitch;

    public ForcefieldConfig(
            boolean enabled,
            ForcefieldShape shape,
            double radius,
            double height,
            double yOffset,
            double strength,
            double verticalStrength,
            long checkIntervalTicks,
            long cooldownMs,
            String bypassPermission,
            Set<SignLayoutState> applyWhen,
            boolean particlesEnabled,
            String particleType,
            int particleCount,
            boolean soundEnabled,
            String soundType,
            float soundVolume,
            float soundPitch
    ) {
        this.enabled = enabled;
        this.shape = shape == null ? ForcefieldShape.SPHERE : shape;
        this.radius = radius;
        this.height = height;
        this.yOffset = yOffset;
        this.strength = strength;
        this.verticalStrength = verticalStrength;
        this.checkIntervalTicks = checkIntervalTicks;
        this.cooldownMs = cooldownMs;
        this.bypassPermission = bypassPermission;
        this.applyWhen = applyWhen == null || applyWhen.isEmpty()
                ? EnumSet.allOf(SignLayoutState.class)
                : EnumSet.copyOf(applyWhen);
        this.particlesEnabled = particlesEnabled;
        this.particleType = particleType;
        this.particleCount = particleCount;
        this.soundEnabled = soundEnabled;
        this.soundType = soundType;
        this.soundVolume = soundVolume;
        this.soundPitch = soundPitch;
    }

    public boolean enabled() {
        return enabled;
    }

    public ForcefieldShape shape() {
        return shape;
    }

    public double radius() {
        return radius;
    }

    public double height() {
        return height;
    }

    public double yOffset() {
        return yOffset;
    }

    public double strength() {
        return strength;
    }

    public double verticalStrength() {
        return verticalStrength;
    }

    public long checkIntervalTicks() {
        return checkIntervalTicks;
    }

    public long cooldownMs() {
        return cooldownMs;
    }

    public String bypassPermission() {
        return bypassPermission;
    }

    public Set<SignLayoutState> applyWhen() {
        return Collections.unmodifiableSet(applyWhen);
    }

    public boolean particlesEnabled() {
        return particlesEnabled;
    }

    public String particleType() {
        return particleType;
    }

    public int particleCount() {
        return particleCount;
    }

    public boolean soundEnabled() {
        return soundEnabled;
    }

    public String soundType() {
        return soundType;
    }

    public float soundVolume() {
        return soundVolume;
    }

    public float soundPitch() {
        return soundPitch;
    }
}
