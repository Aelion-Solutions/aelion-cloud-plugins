package com.aelion.plugins.signs.platform;

import java.util.HashSet;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Sign;

/**
 * Spigot 1.8–1.12: legacy MaterialData facing + getTargetBlock(HashSet&lt;Byte&gt;).
 */
@SuppressWarnings("deprecation")
public final class LegacySignsPlatform implements SignsPlatform {

    @Override
    public void setBehindBlock(Block signBlock, Material material) {
        if (material == null || material == Material.AIR || !material.isBlock()) {
            return;
        }
        BlockFace facing = BlockFace.NORTH;
        MaterialData data = signBlock.getState().getData();
        if (data instanceof Sign) {
            Sign signData = (Sign) data;
            facing = signData.isWallSign()
                    ? signData.getAttachedFace()
                    : signData.getFacing().getOppositeFace();
        }
        Block support = signBlock.getRelative(facing);
        if (support.getType() != material) {
            support.setType(material);
        }
    }

    @Override
    public Block getTargetBlock(Player player, int maxDistance) {
        return player.getTargetBlock((HashSet<Byte>) null, maxDistance);
    }

    @Override
    public boolean writeSignLines(Block signBlock, String[] lines) {
        if (!(signBlock.getState() instanceof org.bukkit.block.Sign)) {
            return false;
        }
        return SignWrites.writeLegacy((org.bukkit.block.Sign) signBlock.getState(), lines);
    }
}
