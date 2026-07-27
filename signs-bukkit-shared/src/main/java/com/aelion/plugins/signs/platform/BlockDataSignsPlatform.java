package com.aelion.plugins.signs.platform;

import java.lang.reflect.Method;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

/**
 * 1.13+ BlockData Directional facing + getTargetBlockExact / Material set targeting.
 * Implemented via reflection so signs-bukkit-shared can stay on Spigot 1.8.8 compile.
 */
public final class BlockDataSignsPlatform implements SignsPlatform {

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
}
