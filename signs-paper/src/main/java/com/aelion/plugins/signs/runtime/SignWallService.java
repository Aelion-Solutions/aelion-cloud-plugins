package com.aelion.plugins.signs.runtime;

import com.aelion.aero.api.AeroFleetService;
import com.aelion.plugins.signs.config.SignsConfig;
import com.aelion.plugins.signs.model.ManagedSign;
import com.aelion.plugins.signs.store.SignStore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public final class SignWallService {

    private final JavaPlugin plugin;
    private final SignStore store;
    private SignsConfig config;
    private AeroFleetService fleet;
    private SignAssignmentEngine assignment;
    private SignRenderer renderer;
    private BukkitTask animationTask;
    private BukkitTask fleetTask;
    private BukkitTask knockbackTask;

    public SignWallService(JavaPlugin plugin, SignStore store) {
        this.plugin = plugin;
        this.store = store;
    }

    public void start(SignsConfig config, AeroFleetService fleet) {
        stopTasks();
        this.config = config;
        this.fleet = fleet;
        this.assignment = new SignAssignmentEngine(config);
        this.renderer = new SignRenderer(config);

        long animTicks = Math.max(1L, config.animationTickMs() / 50L);
        animationTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            renderer.tickAnimation();
            for (ManagedSign sign : store.all()) {
                renderer.render(sign);
            }
        }, animTicks, animTicks);

        long fleetTicks = Math.max(1L, config.fleetPollMs() / 50L);
        fleetTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            try {
                if (fleet != null && fleet.isConfigured()) {
                    fleet.refresh();
                }
                Bukkit.getScheduler().runTask(plugin, this::reassignAndRender);
            } catch (RuntimeException ex) {
                plugin.getLogger().warning("Fleet refresh failed: " + ex.getMessage());
            }
        }, fleetTicks, fleetTicks);

        if (config.knockback().enabled()) {
            knockbackTask = Bukkit.getScheduler().runTaskTimer(plugin, this::applyKnockback, 5L, 5L);
        }
    }

    public void stopTasks() {
        if (animationTask != null) {
            animationTask.cancel();
            animationTask = null;
        }
        if (fleetTask != null) {
            fleetTask.cancel();
            fleetTask = null;
        }
        if (knockbackTask != null) {
            knockbackTask.cancel();
            knockbackTask = null;
        }
    }

    public void reassignAndRender() {
        if (fleet == null || assignment == null || renderer == null) {
            return;
        }
        assignment.reassign(store.all(), fleet);
        for (ManagedSign sign : store.all()) {
            renderer.render(sign);
        }
    }

    public boolean tryConnect(Player player, ManagedSign sign) {
        if (fleet == null || sign.assignedProxyName() == null || sign.assignedProxyName().isBlank()) {
            return false;
        }
        return fleet.connectPlayer(player.getUniqueId(), sign.assignedProxyName());
    }

    private void applyKnockback() {
        if (config == null || !config.knockback().enabled()) {
            return;
        }
        double distance = Math.max(0.1, config.knockback().distance());
        double strength = config.knockback().strength();
        String bypass = config.knockback().bypassPermission();
        for (ManagedSign sign : store.all()) {
            Location loc = sign.location();
            if (loc == null || loc.getWorld() == null) {
                continue;
            }
            for (Player player : loc.getWorld().getPlayers()) {
                if (player.hasPermission(bypass)) {
                    continue;
                }
                if (player.getLocation().distanceSquared(loc) > distance * distance) {
                    continue;
                }
                Vector push = player.getLocation().toVector().subtract(loc.toVector());
                if (push.lengthSquared() < 1.0E-4) {
                    push = player.getLocation().getDirection().multiply(-1);
                }
                push = push.normalize().multiply(strength).setY(0.25);
                player.setVelocity(push);
            }
        }
    }
}
