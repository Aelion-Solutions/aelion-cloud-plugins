package com.aelion.plugins.signs.runtime;

import com.aelion.plugins.common.Placeholders;
import com.aelion.plugins.signs.config.AnimationConfig;
import com.aelion.plugins.signs.config.AnimationSyncMode;
import com.aelion.plugins.signs.config.SignLayoutFrame;
import com.aelion.plugins.signs.config.SignLayoutState;
import com.aelion.plugins.signs.config.SignLayoutsHolder;
import com.aelion.plugins.signs.config.SignsConfig;
import com.aelion.plugins.signs.model.ManagedSign;
import com.aelion.plugins.signs.platform.SignsPlatform;
import com.aelion.plugins.signs.util.ColorMessages;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public final class SignRenderer {

    private final SignsConfig config;
    private final SignsPlatform platform;
    private long globalAnimationTick;
    private final Map<String, Long> perSignTicks = new HashMap<String, Long>();

    public SignRenderer(SignsConfig config, SignsPlatform platform) {
        this.config = config;
        this.platform = platform;
    }

    public void tickAnimation() {
        globalAnimationTick++;
    }

    public void advanceAnimation(ManagedSign managed) {
        if (config.animation().syncMode() != AnimationSyncMode.PER_SIGN) {
            return;
        }
        Long current = perSignTicks.get(managed.key());
        long next = current == null ? 0L : current + 1L;
        perSignTicks.put(managed.key(), next);
    }

    public void render(ManagedSign managed) {
        Location location = managed.location();
        if (location == null || location.getWorld() == null) {
            return;
        }
        World world = location.getWorld();
        if (!isChunkLoaded(world, location.getBlockX(), location.getBlockZ())) {
            return;
        }

        AnimationConfig anim = config.animation();
        if (anim.onlyWhenPlayersNear() && !playersNear(location, anim.playerNearRadius())) {
            return;
        }

        Block block = location.getBlock();
        if (!(block.getState() instanceof Sign)) {
            return;
        }
        Sign sign = (Sign) block.getState();

        SignLayoutState state = SignLayoutState.fromConfigKey(managed.wallState());
        SignLayoutsHolder holder = config.layout(managed.targetGroup(), state);

        long animTick;
        if (anim.syncMode() == AnimationSyncMode.PER_SIGN) {
            Long current = perSignTicks.get(managed.key());
            animTick = current == null ? 0L : current;
        } else {
            animTick = globalAnimationTick;
        }

        long frameIndex = animTick;
        if (holder.animationsPerSecond() > 0) {
            double ticksPerFrame = Math.max(
                    1.0,
                    (1000.0 / Math.max(1L, anim.tickMs())) / holder.animationsPerSecond());
            frameIndex = (long) (animTick / ticksPerFrame);
        }
        SignLayoutFrame frame = holder.frameAt(frameIndex);

        Map<String, String> values = new HashMap<String, String>();
        values.put("name", managed.assignedDisplayName() == null ? managed.targetGroup() : managed.assignedDisplayName());
        values.put("group", managed.targetGroup());
        values.put("online", String.valueOf(managed.assignedOnline()));
        values.put("max", String.valueOf(managed.assignedMax()));
        values.put("status", state.configKey());

        for (int i = 0; i < 4; i++) {
            String line = Placeholders.apply(frame.lines().get(i), values);
            sign.setLine(i, ColorMessages.color(line));
        }
        sign.update(true, false);

        Material behind = frame.blockMaterial();
        if (behind != null && behind != Material.AIR && behind.isBlock()) {
            platform.setBehindBlock(block, behind);
        }
    }

    private static boolean isChunkLoaded(World world, int blockX, int blockZ) {
        return world.isChunkLoaded(blockX >> 4, blockZ >> 4);
    }

    private static boolean playersNear(Location location, double radius) {
        double r2 = radius * radius;
        for (Player player : location.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(location) <= r2) {
                return true;
            }
        }
        return false;
    }
}
