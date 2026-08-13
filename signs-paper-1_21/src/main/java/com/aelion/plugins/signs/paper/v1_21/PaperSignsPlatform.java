package com.aelion.plugins.signs.paper.v1_21;

import com.aelion.plugins.signs.platform.BlockDataSignsPlatform;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;

/**
 * Paper 1.21+ sign writes: FRONT side, Adventure lines, waxed so clients apply updates.
 */
public final class PaperSignsPlatform extends BlockDataSignsPlatform {

    private static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.legacySection();

    @Override
    public boolean writeSignLines(Block signBlock, String[] lines) {
        if (!(signBlock.getState() instanceof Sign)) {
            return false;
        }
        Sign sign = (Sign) signBlock.getState();
        SignSide front = sign.getSide(Side.FRONT);
        for (int i = 0; i < 4; i++) {
            String raw = (lines != null && i < lines.length && lines[i] != null) ? lines[i] : "";
            front.line(i, LEGACY.deserialize(raw));
        }
        sign.setWaxed(true);
        return sign.update(true);
    }
}
