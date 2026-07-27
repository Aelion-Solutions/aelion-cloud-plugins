package com.aelion.plugins.signs.runtime;

import com.aelion.plugins.common.Placeholders;
import com.aelion.plugins.signs.config.SignLayoutFrame;
import com.aelion.plugins.signs.config.SignLayoutState;
import com.aelion.plugins.signs.config.SignLayoutsHolder;
import com.aelion.plugins.signs.config.SignsConfig;
import com.aelion.plugins.signs.model.ManagedSign;
import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.block.sign.Side;

public final class SignRenderer {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private final SignsConfig config;
    private long animationTick;

    public SignRenderer(SignsConfig config) {
        this.config = config;
    }

    public void tickAnimation() {
        animationTick++;
    }

    public void render(ManagedSign managed) {
        var location = managed.location();
        if (location == null || location.getWorld() == null) {
            return;
        }
        if (!location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) {
            return;
        }
        Block block = location.getBlock();
        if (!(block.getState() instanceof Sign sign)) {
            return;
        }

        SignLayoutState state = SignLayoutState.fromConfigKey(managed.wallState());
        SignLayoutsHolder holder = config.layout(managed.targetGroup(), state);
        long frameIndex = animationTick;
        if (holder.animationsPerSecond() > 0) {
            // Advance frame based on APS relative to 20-ish animation ticks when tick is 50ms
            double ticksPerFrame = Math.max(1.0, (1000.0 / Math.max(1L, config.animationTickMs())) / holder.animationsPerSecond());
            frameIndex = (long) (animationTick / ticksPerFrame);
        }
        SignLayoutFrame frame = holder.frameAt(frameIndex);

        Map<String, String> values = new HashMap<>();
        values.put("name", managed.assignedDisplayName() == null ? managed.targetGroup() : managed.assignedDisplayName());
        values.put("group", managed.targetGroup());
        values.put("online", String.valueOf(managed.assignedOnline()));
        values.put("max", String.valueOf(managed.assignedMax()));
        values.put("status", state.configKey());

        var side = sign.getSide(Side.FRONT);
        for (int i = 0; i < 4; i++) {
            String line = Placeholders.apply(frame.lines().get(i), values);
            side.line(i, LEGACY.deserialize(line));
        }
        sign.update(true, false);

        Material behind = frame.blockMaterial();
        if (behind != null && behind != Material.AIR && behind.isBlock()) {
            BlockFace facing = BlockFace.NORTH;
            if (block.getBlockData() instanceof Directional directional) {
                facing = directional.getFacing().getOppositeFace();
            }
            Block support = block.getRelative(facing);
            if (support.getType() != behind) {
                support.setType(behind, false);
            }
        }
    }
}
