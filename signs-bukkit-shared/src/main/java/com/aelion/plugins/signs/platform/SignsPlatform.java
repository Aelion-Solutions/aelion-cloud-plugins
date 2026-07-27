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
}
