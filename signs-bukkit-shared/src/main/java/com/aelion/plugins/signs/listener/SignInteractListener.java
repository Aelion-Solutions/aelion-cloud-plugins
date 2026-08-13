package com.aelion.plugins.signs.listener;

import com.aelion.plugins.signs.model.ManagedSign;
import com.aelion.plugins.signs.runtime.SignWallService;
import com.aelion.plugins.signs.store.SignStore;
import com.aelion.plugins.signs.util.ColorMessages;
import java.lang.reflect.Method;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.java.JavaPlugin;

public final class SignInteractListener implements Listener {

    private static final String[] OPEN_SIGN_EVENTS = {
            "io.papermc.paper.event.player.PlayerOpenSignEvent",
            "org.bukkit.event.player.PlayerSignOpenEvent",
            "org.bukkit.event.player.PlayerOpenSignEvent"
    };

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
        ManagedSign sign = managedAt(block);
        if (sign == null) {
            return;
        }
        event.setCancelled(true);
        if (isOffHand(event)) {
            return;
        }
        Player player = event.getPlayer();
        if (!wall.tryConnect(player, sign)) {
            ColorMessages.send(player, "&cNo joinable server for &e" + sign.targetGroup());
        }
    }

    /**
     * 1.20+ opens the vanilla editor on right-click; cancel that for managed signs
     * so join still works after we wax the tile.
     */
    public void registerOpenSignGuard(JavaPlugin plugin) {
        Class<? extends Event> type = findOpenSignEvent();
        if (type == null) {
            return;
        }
        EventExecutor executor = new EventExecutor() {
            @Override
            public void execute(Listener listener, Event event) {
                if (!type.isInstance(event)) {
                    return;
                }
                Block block = blockFromOpenEvent(event);
                if (block == null || managedAt(block) == null) {
                    return;
                }
                if (event instanceof org.bukkit.event.Cancellable) {
                    ((org.bukkit.event.Cancellable) event).setCancelled(true);
                }
            }
        };
        plugin.getServer().getPluginManager().registerEvent(
                type,
                this,
                EventPriority.HIGH,
                executor,
                plugin,
                true
        );
    }

    private ManagedSign managedAt(Block block) {
        return store.getAt(
                block.getWorld().getName(),
                block.getX(),
                block.getY(),
                block.getZ()
        );
    }

    private static boolean isOffHand(PlayerInteractEvent event) {
        try {
            Object hand = event.getClass().getMethod("getHand").invoke(event);
            return hand != null && "OFF_HAND".equals(String.valueOf(hand));
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Event> findOpenSignEvent() {
        for (int i = 0; i < OPEN_SIGN_EVENTS.length; i++) {
            try {
                Class<?> raw = Class.forName(OPEN_SIGN_EVENTS[i]);
                if (Event.class.isAssignableFrom(raw)) {
                    return (Class<? extends Event>) raw;
                }
            } catch (ClassNotFoundException ignored) {
                // try next name
            }
        }
        return null;
    }

    private static Block blockFromOpenEvent(Event event) {
        try {
            Method getSign = event.getClass().getMethod("getSign");
            Object sign = getSign.invoke(event);
            if (sign instanceof Sign) {
                return ((Sign) sign).getBlock();
            }
            if (sign instanceof Block) {
                return (Block) sign;
            }
        } catch (ReflectiveOperationException ignored) {
            // try getBlock
        }
        try {
            Method getBlock = event.getClass().getMethod("getBlock");
            Object block = getBlock.invoke(event);
            if (block instanceof Block) {
                return (Block) block;
            }
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
        return null;
    }
}
