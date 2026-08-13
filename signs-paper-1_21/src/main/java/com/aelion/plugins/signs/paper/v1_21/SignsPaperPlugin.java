package com.aelion.plugins.signs.paper.v1_21;

import com.aelion.plugins.signs.SignsBootstrap;
import org.bukkit.plugin.java.JavaPlugin;

/** Aelion Signs for Minecraft 1.21.x. */
public final class SignsPaperPlugin extends JavaPlugin {

    private SignsBootstrap bootstrap;

    @Override
    public void onEnable() {
        bootstrap = new SignsBootstrap(this, new PaperSignsPlatform());
        bootstrap.enable();
    }

    @Override
    public void onDisable() {
        if (bootstrap != null) {
            bootstrap.disable();
        }
    }
}
