package com.aelion.plugins.signs.platform;

import java.lang.reflect.Method;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

/**
 * 1.13+ BlockData Directional facing + getTargetBlockExact / Material set targeting.
 * Implemented via reflection so signs-bukkit-shared can stay on Spigot 1.8.8 compile.
 *
 * <p>When 1.20+ {@code Sign#getSide}/{@code setWaxed} exist they are used so clients
 * actually receive tile updates. Older 1.13–1.19 builds fall back to {@code setLine}.
 */
public class BlockDataSignsPlatform implements SignsPlatform {

    private static final Method GET_SIDE;
    private static final Method SIDE_SET_LINE;
    private static final Method SET_WAXED;
    private static final Method UPDATE_FORCE;
    private static final Object SIDE_FRONT;

    static {
        Method getSide = null;
        Method sideSetLine = null;
        Method setWaxed = null;
        Method updateForce = null;
        Object sideFront = null;
        try {
            Class<?> signClass = Class.forName("org.bukkit.block.Sign");
            updateForce = signClass.getMethod("update", boolean.class);
            Class<?> sideClass = Class.forName("org.bukkit.block.sign.Side");
            sideFront = Enum.valueOf(sideClass.asSubclass(Enum.class), "FRONT");
            getSide = signClass.getMethod("getSide", sideClass);
            Class<?> signSideClass = Class.forName("org.bukkit.block.sign.SignSide");
            sideSetLine = signSideClass.getMethod("setLine", int.class, String.class);
            setWaxed = signClass.getMethod("setWaxed", boolean.class);
        } catch (ReflectiveOperationException ignored) {
            // 1.13–1.19: Side / setWaxed absent; legacy setLine path below.
        }
        GET_SIDE = getSide;
        SIDE_SET_LINE = sideSetLine;
        SET_WAXED = setWaxed;
        UPDATE_FORCE = updateForce;
        SIDE_FRONT = sideFront;
    }

    @Override
    public void setBehindBlock(Block signBlock, Material material) {
        if (material == null || material == Material.AIR || !material.isBlock()) {
            return;
        }
        BlockFace facing = BlockFace.NORTH;
        try {
            Object blockData = signBlock.getClass().getMethod("getBlockData").invoke(signBlock);
            Class<?> directional = Class.forName("org.bukkit.block.data.Directional");
            if (directional.isInstance(blockData)) {
                Object face = directional.getMethod("getFacing").invoke(blockData);
                Object opposite = face.getClass().getMethod("getOppositeFace").invoke(face);
                if (opposite instanceof BlockFace) {
                    facing = (BlockFace) opposite;
                }
            }
        } catch (ReflectiveOperationException ignored) {
            // Fall back to NORTH.
        }
        Block support = signBlock.getRelative(facing);
        if (support.getType() != material) {
            support.setType(material);
        }
    }

    @Override
    public Block getTargetBlock(Player player, int maxDistance) {
        try {
            Method exact = player.getClass().getMethod("getTargetBlockExact", int.class);
            Object result = exact.invoke(player, maxDistance);
            if (result instanceof Block) {
                return (Block) result;
            }
        } catch (ReflectiveOperationException ignored) {
            // try Set<Material> overload
        }
        try {
            Method modern = player.getClass().getMethod("getTargetBlock", Set.class, int.class);
            Object result = modern.invoke(player, null, maxDistance);
            if (result instanceof Block) {
                return (Block) result;
            }
        } catch (ReflectiveOperationException ignored) {
            // fall through
        }
        return new LegacySignsPlatform().getTargetBlock(player, maxDistance);
    }

    @Override
    public boolean writeSignLines(Block signBlock, String[] lines) {
        if (!(signBlock.getState() instanceof Sign)) {
            return false;
        }
        Sign sign = (Sign) signBlock.getState();
        try {
            if (GET_SIDE != null && SIDE_SET_LINE != null && SIDE_FRONT != null) {
                Object side = GET_SIDE.invoke(sign, SIDE_FRONT);
                for (int i = 0; i < 4; i++) {
                    SIDE_SET_LINE.invoke(side, Integer.valueOf(i), SignWrites.lineAt(lines, i));
                }
                if (SET_WAXED != null) {
                    SET_WAXED.invoke(sign, Boolean.TRUE);
                }
                if (UPDATE_FORCE != null) {
                    Object updated = UPDATE_FORCE.invoke(sign, Boolean.TRUE);
                    return Boolean.TRUE.equals(updated);
                }
                return sign.update(true);
            }
            return SignWrites.writeLegacy(sign, lines);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return false;
        }
    }
}
