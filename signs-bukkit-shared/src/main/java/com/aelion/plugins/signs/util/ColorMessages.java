package com.aelion.plugins.signs.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public final class ColorMessages {

    private ColorMessages() {
    }

    public static String color(String legacy) {
        if (legacy == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', legacy);
    }

    public static void send(CommandSender sender, String legacy) {
        sender.sendMessage(color(legacy));
    }
}
