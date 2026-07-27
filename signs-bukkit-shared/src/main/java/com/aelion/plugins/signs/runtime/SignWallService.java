package com.aelion.plugins.signs.runtime;

import com.aelion.aero.api.AeroFleetService;
import com.aelion.plugins.signs.config.ForcefieldConfig;
import com.aelion.plugins.signs.config.ForcefieldShape;
import com.aelion.plugins.signs.config.SignLayoutState;
import com.aelion.plugins.signs.config.SignsConfig;
import com.aelion.plugins.signs.model.ManagedSign;
import com.aelion.plugins.signs.platform.SignsPlatform;
import com.aelion.plugins.signs.store.SignStore;
import com.aelion.plugins.signs.util.Strings;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public final class SignWallService {

    private final JavaPlugin plugin;
    private final SignStore store;
    private final SignsPlatform platform;
    private SignsConfig config;
    private AeroFleetService fleet;
    private SignAssignmentEngine assignment;
    private SignRenderer renderer;
    private BukkitTask animationTask;
    private BukkitTask fleetTask;
    private BukkitTask forcefieldTask;
    private final Map<UUID, Long> forcefieldCooldown = new HashMap<UUID, Long>();

    public SignWallService(JavaPlugin plugin, SignStore store, SignsPlatform platform) {
        this.plugin = plugin;
        this.store = store;
        this.platform = platform;
    }

    public void start(SignsConfig config, AeroFleetService fleet) {
        stopTasks();
        this.config = config;
        this.fleet = fleet;
        this.assignment = new SignAssignmentEngine(config);
        this.renderer = new SignRenderer(config, platform);
        forcefieldCooldown.clear();

        long animTicks = Math.max(1L, config.animation().tickMs() / 50L);
        animationTask = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {
                renderer.tickAnimation();
                for (ManagedSign sign : store.all()) {
                    renderer.renderAnimationFrame(sign);
                }
            }
        }, animTicks, animTicks);

        long fleetTicks = Math.max(1L, config.fleetPollMs() / 50L);
        fleetTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    if (fleet != null && fleet.isConfigured()) {
                        fleet.refresh();
                    }
                    Bukkit.getScheduler().runTask(plugin, new Runnable() {
                        @Override
                        public void run() {
                            reassignAndRender();
                        }
                    });
                } catch (RuntimeException ex) {
                    plugin.getLogger().warning("Fleet refresh failed: " + ex.getMessage());
                }
            }
        }, fleetTicks, fleetTicks);

        if (config.forcefield().enabled()) {
            long interval = Math.max(1L, config.forcefield().checkIntervalTicks());
            forcefieldTask = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
                @Override
                public void run() {
                    applyForcefield();
                }
            }, interval, interval);
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
        if (forcefieldTask != null) {
            forcefieldTask.cancel();
            forcefieldTask = null;
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
        if (fleet == null || Strings.isBlank(sign.assignedProxyName())) {
            return false;
        }
        return fleet.connectPlayer(player.getUniqueId(), sign.assignedProxyName());
    }

    private void applyForcefield() {
        if (config == null || !config.forcefield().enabled()) {
            return;
        }
        ForcefieldConfig ff = config.forcefield();
        double radius = Math.max(0.1, ff.radius());
        double height = Math.max(0.1, ff.height());
        String bypass = ff.bypassPermission();
        long now = System.currentTimeMillis();

        for (ManagedSign sign : store.all()) {
            SignLayoutState state = SignLayoutState.fromConfigKey(sign.wallState());
            if (!ff.applyWhen().contains(state)) {
                continue;
            }
            Location loc = sign.location();
            if (loc == null || loc.getWorld() == null) {
                continue;
            }
            Location center = loc.clone().add(0.5, 0.5 + ff.yOffset(), 0.5);
            for (Player player : loc.getWorld().getPlayers()) {
                if (player.hasPermission(bypass) || player.hasPermission("aelion.signs.knockback.bypass")) {
                    continue;
                }
                if (!inside(player.getLocation(), center, ff.shape(), radius, height)) {
                    continue;
                }
                Long last = forcefieldCooldown.get(player.getUniqueId());
                if (last != null && now - last < ff.cooldownMs()) {
                    continue;
                }
                forcefieldCooldown.put(player.getUniqueId(), now);

                Vector push = player.getLocation().toVector().subtract(center.toVector());
                if (push.lengthSquared() < 1.0E-4) {
                    push = player.getLocation().getDirection().multiply(-1);
                }
                push = push.normalize().multiply(ff.strength()).setY(ff.verticalStrength());
                player.setVelocity(push);

                playParticles(player.getLocation(), ff);
                playSound(player.getLocation(), ff);
            }
        }
    }

    private static boolean inside(
            Location playerLoc,
            Location center,
            ForcefieldShape shape,
            double radius,
            double height
    ) {
        if (playerLoc.getWorld() != center.getWorld()) {
            return false;
        }
        if (shape == ForcefieldShape.BOX) {
            return Math.abs(playerLoc.getX() - center.getX()) <= radius
                    && Math.abs(playerLoc.getZ() - center.getZ()) <= radius
                    && Math.abs(playerLoc.getY() - center.getY()) <= height;
        }
        return playerLoc.distanceSquared(center) <= radius * radius;
    }

    private void playParticles(Location location, ForcefieldConfig ff) {
        if (!ff.particlesEnabled() || Strings.isBlank(ff.particleType())) {
            return;
        }
        try {
            Class<?> particleClass = Class.forName("org.bukkit.Particle");
            Object particle = Enum.valueOf(particleClass.asSubclass(Enum.class), ff.particleType().toUpperCase());
            Method spawn = location.getWorld().getClass().getMethod(
                    "spawnParticle",
                    particleClass,
                    Location.class,
                    int.class,
                    double.class,
                    double.class,
                    double.class,
                    double.class
            );
            spawn.invoke(location.getWorld(), particle, location, ff.particleCount(), 0.2, 0.2, 0.2, 0.01);
        } catch (ReflectiveOperationException | IllegalArgumentException ignored) {
            // Particle API unavailable on this band — skip.
        }
    }

    private void playSound(Location location, ForcefieldConfig ff) {
        if (!ff.soundEnabled() || Strings.isBlank(ff.soundType())) {
            return;
        }
        try {
            Sound sound = Sound.valueOf(ff.soundType().toUpperCase());
            location.getWorld().playSound(location, sound, ff.soundVolume(), ff.soundPitch());
        } catch (IllegalArgumentException ignored) {
            // Sound enum name not present on this band.
        }
    }
}
