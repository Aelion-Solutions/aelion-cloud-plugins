package com.aelion.plugins.signs.paper.v1_17;

import com.aelion.plugins.signs.SignsBootstrap;
import com.aelion.plugins.signs.platform.BlockDataSignsPlatform;
import org.bukkit.plugin.java.JavaPlugin;

/** Aelion Signs for Minecraft 1.17–1.20.6. */
public final class SignsPaperPlugin extends JavaPlugin {

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
