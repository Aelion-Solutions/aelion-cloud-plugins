package com.aelion.plugins.signs.config;

public final class AnimationConfig {

    private final long tickMs;
    private final AnimationSyncMode syncMode;
    private final boolean onlyWhenPlayersNear;
    private final double playerNearRadius;

    public AnimationConfig(
            long tickMs,
            AnimationSyncMode syncMode,
            boolean onlyWhenPlayersNear,
            double playerNearRadius
    ) {
        this.tickMs = tickMs;
        this.syncMode = syncMode;
        this.onlyWhenPlayersNear = onlyWhenPlayersNear;
        this.playerNearRadius = playerNearRadius;
    }

    public long tickMs() {
        return tickMs;
    }

    public AnimationSyncMode syncMode() {
        return syncMode;
    }

    public boolean onlyWhenPlayersNear() {
        return onlyWhenPlayersNear;
    }

    public double playerNearRadius() {
        return playerNearRadius;
    }
}
