package com.aelion.plugins.signs.listener;

import com.aelion.plugins.signs.model.ManagedSign;
import com.aelion.plugins.signs.runtime.SignWallService;
import com.aelion.plugins.signs.store.SignStore;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public final class SignInteractListener implements Listener {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private final SignStore store;
    private final SignWallService wall;

    public SignInteractListener(SignStore store, SignWallService wall) {
        this.store = store;
        this.wall = wall;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null || !(block.getState() instanceof Sign)) {
            return;
        }
        ManagedSign sign = store.getAt(
                block.getWorld().getName(),
                block.getX(),
                block.getY(),
                block.getZ()
        );
        if (sign == null) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();
        if (!wall.tryConnect(player, sign)) {
            player.sendMessage(LEGACY.deserialize("&cNo joinable server for &e" + sign.targetGroup()));
        }
    }
}
