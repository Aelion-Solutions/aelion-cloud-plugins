package com.aelion.plugins.signs.command;

import com.aelion.plugins.signs.model.ManagedSign;
import com.aelion.plugins.signs.runtime.SignWallService;
import com.aelion.plugins.signs.store.SignStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public final class SignsCommand implements CommandExecutor, TabCompleter {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private final SignStore store;
    private final SignWallService wall;
    private final Runnable reloadAction;

    public SignsCommand(SignStore store, SignWallService wall, Runnable reloadAction) {
        this.store = store;
        this.wall = wall;
        this.reloadAction = reloadAction;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("aelion.signs.admin")) {
            send(sender, "&cMissing permission aelion.signs.admin");
            return true;
        }
        if (args.length == 0) {
            send(sender, "&e/aesign create <group> [filter] &7| remove | removeall | cleanup [world] | reload");
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "create" -> create(sender, args);
            case "remove" -> remove(sender);
            case "removeall" -> {
                int n = store.removeAll();
                store.save();
                wall.reassignAndRender();
                send(sender, "&aRemoved &f" + n + " &asigns");
            }
            case "cleanup" -> cleanup(sender, args);
            case "reload" -> {
                reloadAction.run();
                send(sender, "&aSigns config and store reloaded");
            }
            default -> send(sender, "&cUnknown subcommand. Use create|remove|removeall|cleanup|reload");
        }
        return true;
    }

    private void create(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            send(sender, "&cPlayers only");
            return;
        }
        if (args.length < 2) {
            send(sender, "&cUsage: /aesign create <group> [filter]");
            return;
        }
        Block target = player.getTargetBlockExact(6);
        if (target == null || !(target.getState() instanceof Sign)) {
            send(sender, "&cLook at a sign within 6 blocks");
            return;
        }
        String group = args[1];
        String filter = args.length >= 3 ? args[2] : null;
        ManagedSign existing = store.getAt(
                target.getWorld().getName(),
                target.getX(),
                target.getY(),
                target.getZ()
        );
        if (existing != null) {
            send(sender, "&cThat sign is already registered for &e" + existing.targetGroup());
            return;
        }
        ManagedSign created = ManagedSign.fromLocation(target.getLocation(), group, filter);
        store.put(created);
        store.save();
        wall.reassignAndRender();
        send(sender, "&aCreated sign for group &e" + group);
    }

    private void remove(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            send(sender, "&cPlayers only");
            return;
        }
        Block target = player.getTargetBlockExact(6);
        if (target == null) {
            send(sender, "&cLook at a registered sign");
            return;
        }
        String key = target.getWorld().getName() + ":" + target.getX() + ":" + target.getY() + ":" + target.getZ();
        if (!store.remove(key)) {
            send(sender, "&cThat block is not a managed sign");
            return;
        }
        store.save();
        wall.reassignAndRender();
        send(sender, "&aRemoved managed sign");
    }

    private void cleanup(CommandSender sender, String[] args) {
        String world = args.length >= 2
                ? args[1]
                : (sender instanceof Player player ? player.getWorld().getName() : null);
        if (world == null) {
            send(sender, "&cUsage: /aesign cleanup <world>");
            return;
        }
        int n = store.removeWorld(world);
        store.save();
        wall.reassignAndRender();
        send(sender, "&aRemoved &f" + n + " &asigns in &e" + world);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            for (String s : List.of("create", "remove", "removeall", "cleanup", "reload")) {
                if (s.startsWith(args[0].toLowerCase(Locale.ROOT))) {
                    out.add(s);
                }
            }
        }
        return out;
    }

    private static void send(CommandSender sender, String legacy) {
        sender.sendMessage(LEGACY.deserialize(legacy));
    }
}
