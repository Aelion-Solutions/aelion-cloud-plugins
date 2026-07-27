package com.aelion.plugins.signs.bukkit.v1_13;

import com.aelion.plugins.signs.SignsBootstrap;
import com.aelion.plugins.signs.platform.BlockDataSignsPlatform;
import org.bukkit.plugin.java.JavaPlugin;

/** Aelion Signs for Minecraft 1.13–1.16.5. */
public final class SignsBukkitPlugin extends JavaPlugin {

    private SignsBootstrap bootstrap;

    @Override
    public void onEnable() {
        bootstrap = new SignsBootstrap(this, new BlockDataSignsPlatform());
        bootstrap.enable();
    }

    @Override
    public void onDisable() {
        if (bootstrap != null) {
            bootstrap.disable();
        }
    }
}
