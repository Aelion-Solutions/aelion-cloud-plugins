package com.aelion.plugins.signs.bukkit.v1_8;

import com.aelion.plugins.signs.SignsBootstrap;
import com.aelion.plugins.signs.platform.LegacySignsPlatform;
import org.bukkit.plugin.java.JavaPlugin;

/** Aelion Signs for Minecraft 1.8–1.12.2. */
public final class SignsBukkitPlugin extends JavaPlugin {

    private SignsBootstrap bootstrap;

    @Override
    public void onEnable() {
        bootstrap = new SignsBootstrap(this, new LegacySignsPlatform());
        bootstrap.enable();
    }

    @Override
    public void onDisable() {
        if (bootstrap != null) {
            bootstrap.disable();
        }
    }
}
