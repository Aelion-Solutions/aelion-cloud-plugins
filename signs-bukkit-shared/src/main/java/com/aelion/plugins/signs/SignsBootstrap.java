package com.aelion.plugins.signs;

import com.aelion.aero.api.AeroFleetService;
import com.aelion.plugins.common.PluginsVersion;
import com.aelion.plugins.signs.command.SignsCommand;
import com.aelion.plugins.signs.config.SignsConfig;
import com.aelion.plugins.signs.listener.SignInteractListener;
import com.aelion.plugins.signs.platform.SignsPlatform;
import com.aelion.plugins.signs.runtime.SignWallService;
import com.aelion.plugins.signs.store.SignStore;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Shared enable/disable/reload for every Signs version band.
 */
public final class SignsBootstrap {

    private final JavaPlugin plugin;
    private final SignsPlatform platform;
    private SignStore store;
    private SignWallService wall;
    private AeroFleetService fleet;

    public SignsBootstrap(JavaPlugin plugin, SignsPlatform platform) {
        this.plugin = plugin;
        this.platform = platform;
    }

    public void enable() {
        plugin.saveDefaultConfig();
        store = new SignStore(plugin);
        store.load();
        wall = new SignWallService(plugin, store, platform);

        fleet = resolveFleet();
        if (fleet == null) {
            plugin.getLogger().severe("AelionAero fleet bridge not found. Install/enable Aelion Aero on this server.");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }
        if (!fleet.isConfigured()) {
            plugin.getLogger().warning("Aero panel is not configured yet; signs will show searching until it is.");
        }

        SignsConfig config = SignsConfig.from(plugin.getConfig());
        if (!config.enabled()) {
            plugin.getLogger().info("Aelion Signs is disabled in config.yml");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        wall.start(config, fleet);
        SignInteractListener interact = new SignInteractListener(store, wall);
        plugin.getServer().getPluginManager().registerEvents(interact, plugin);
        interact.registerOpenSignGuard(plugin);

        SignsCommand command = new SignsCommand(store, wall, platform, new Runnable() {
            @Override
            public void run() {
                reloadAll();
            }
        });
        PluginCommand pluginCommand = plugin.getCommand("aesign");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(command);
            pluginCommand.setTabCompleter(command);
        }

        plugin.getLogger().info("Aelion Signs enabled (v" + PluginsVersion.VERSION + ")");
    }

    public void disable() {
        if (wall != null) {
            wall.stopTasks();
        }
        if (store != null) {
            store.save();
        }
        plugin.getLogger().info("Aelion Signs disabled");
    }

    private void reloadAll() {
        plugin.reloadConfig();
        store.load();
        SignsConfig config = SignsConfig.from(plugin.getConfig());
        wall.stopTasks();
        wall.start(config, fleet);
        wall.reassignAndRender();
    }

    private AeroFleetService resolveFleet() {
        RegisteredServiceProvider<AeroFleetService> rsp =
                plugin.getServer().getServicesManager().getRegistration(AeroFleetService.class);
        return rsp == null ? null : rsp.getProvider();
    }
}
