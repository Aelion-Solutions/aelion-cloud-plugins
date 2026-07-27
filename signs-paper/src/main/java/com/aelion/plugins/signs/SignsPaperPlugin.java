package com.aelion.plugins.signs;

import com.aelion.aero.api.AeroFleetService;
import com.aelion.plugins.common.PluginsVersion;
import com.aelion.plugins.signs.command.SignsCommand;
import com.aelion.plugins.signs.config.SignsConfig;
import com.aelion.plugins.signs.listener.SignInteractListener;
import com.aelion.plugins.signs.runtime.SignWallService;
import com.aelion.plugins.signs.store.SignStore;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Paper entry point for Aelion Signs (SoftDepends Aero for fleet + Connect).
 */
public final class SignsPaperPlugin extends JavaPlugin {

    private SignStore store;
    private SignWallService wall;
    private AeroFleetService fleet;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        store = new SignStore(this);
        store.load();
        wall = new SignWallService(this, store);

        fleet = resolveFleet();
        if (fleet == null) {
            getLogger().severe("AelionAero fleet bridge not found. Install/enable Aelion Aero on this server.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (!fleet.isConfigured()) {
            getLogger().warning("Aero panel is not configured yet; signs will show searching until it is.");
        }

        SignsConfig config = SignsConfig.from(getConfig());
        if (!config.enabled()) {
            getLogger().info("Aelion Signs is disabled in config.yml");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        wall.start(config, fleet);
        getServer().getPluginManager().registerEvents(new SignInteractListener(store, wall), this);

        SignsCommand command = new SignsCommand(store, wall, this::reloadAll);
        var pluginCommand = getCommand("aesign");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(command);
            pluginCommand.setTabCompleter(command);
        }

        getLogger().info("Aelion Signs enabled (v" + PluginsVersion.VERSION + ")");
    }

    @Override
    public void onDisable() {
        if (wall != null) {
            wall.stopTasks();
        }
        if (store != null) {
            store.save();
        }
        getLogger().info("Aelion Signs disabled");
    }

    private void reloadAll() {
        reloadConfig();
        store.load();
        SignsConfig config = SignsConfig.from(getConfig());
        wall.stopTasks();
        wall.start(config, fleet);
        wall.reassignAndRender();
    }

    private AeroFleetService resolveFleet() {
        RegisteredServiceProvider<AeroFleetService> rsp =
                getServer().getServicesManager().getRegistration(AeroFleetService.class);
        return rsp == null ? null : rsp.getProvider();
    }
}
