package com.aelion.plugins.signs.platform;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Era-specific Bukkit helpers injected by each version band.
 */
public interface SignsPlatform {

    /**
     * Set the block behind a wall/standing sign (facing opposite the text).
     */
    void setBehindBlock(Block signBlock, Material material);

    /**
     * Ray-trace to a target block for admin create/remove.
     */
    Block getTargetBlock(Player player, int maxDistance);

    /**
     * Write four colored lines onto a sign tile and push the update to clients.
     *
     * @return {@code true} if the tile was a sign and {@code update} reported success
     */
    boolean writeSignLines(Block signBlock, String[] lines);
}
