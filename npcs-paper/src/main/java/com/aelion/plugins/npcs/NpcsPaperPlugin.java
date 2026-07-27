package com.aelion.plugins.npcs;

import com.aelion.plugins.common.PluginsVersion;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Paper entry point for Aelion NPCs (scaffold — panel-managed NPCs later).
 */
public final class NpcsPaperPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getLogger().info("Aelion NPCs enabled (v" + PluginsVersion.VERSION + ")");
    }

    @Override
    public void onDisable() {
        getLogger().info("Aelion NPCs disabled");
    }
}
