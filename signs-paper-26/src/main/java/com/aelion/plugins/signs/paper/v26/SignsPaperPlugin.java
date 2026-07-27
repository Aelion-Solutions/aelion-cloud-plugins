package com.aelion.plugins.signs.paper.v26;

import com.aelion.plugins.signs.SignsBootstrap;
import com.aelion.plugins.signs.platform.BlockDataSignsPlatform;
import org.bukkit.plugin.java.JavaPlugin;

/** Aelion Signs for Paper 26.x. */
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
