package com.aelion.plugins.signs.command;

import com.aelion.plugins.signs.model.ManagedSign;
import com.aelion.plugins.signs.platform.SignsPlatform;
import com.aelion.plugins.signs.runtime.SignWallService;
import com.aelion.plugins.signs.store.SignStore;
import com.aelion.plugins.signs.util.ColorMessages;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public final class SignsCommand implements CommandExecutor, TabCompleter {

    private final SignStore store;
    private final SignWallService wall;
    private final SignsPlatform platform;
    private final Runnable reloadAction;

    public SignsCommand(SignStore store, SignWallService wall, SignsPlatform platform, Runnable reloadAction) {
        this.store = store;
        this.wall = wall;
        this.platform = platform;
        this.reloadAction = reloadAction;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("aelion.signs.admin")) {
            ColorMessages.send(sender, "&cMissing permission aelion.signs.admin");
            return true;
        }
        if (args.length == 0) {
            ColorMessages.send(sender,
                    "&e/aesign create <group> [filter] &7| remove | removeall | cleanup [world] | reload | debug [group]");
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        if ("create".equals(sub)) {
            create(sender, args);
        } else if ("remove".equals(sub)) {
            remove(sender);
        } else if ("removeall".equals(sub)) {
            int n = store.removeAll();
            store.save();
            wall.reassignAndRender();
            ColorMessages.send(sender, "&aRemoved &f" + n + " &asigns");
        } else if ("cleanup".equals(sub)) {
            cleanup(sender, args);
        } else if ("reload".equals(sub)) {
            reloadAction.run();
            ColorMessages.send(sender, "&aSigns config and store reloaded");
        } else if ("debug".equals(sub)) {
            String group = args.length >= 2 ? args[1] : null;
            for (String line : wall.debugLines(group)) {
                ColorMessages.send(sender, line);
            }
        } else {
            ColorMessages.send(sender, "&cUnknown subcommand. Use create|remove|removeall|cleanup|reload|debug");
        }
        return true;
    }

    private void create(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            ColorMessages.send(sender, "&cPlayers only");
            return;
        }
        Player player = (Player) sender;
        if (args.length < 2) {
            ColorMessages.send(sender, "&cUsage: /aesign create <group> [filter]");
            return;
        }
        Block target = platform.getTargetBlock(player, 6);
        if (target == null || !(target.getState() instanceof Sign)) {
            ColorMessages.send(sender, "&cLook at a sign within 6 blocks");
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
            ColorMessages.send(sender, "&cThat sign is already registered for &e" + existing.targetGroup());
            return;
        }
        ManagedSign created = ManagedSign.fromLocation(target.getLocation(), group, filter);
        store.put(created);
        store.save();
        wall.reassignAndRender();
        ColorMessages.send(sender, "&aCreated sign for group &e" + group);
    }

    private void remove(CommandSender sender) {
        if (!(sender instanceof Player)) {
            ColorMessages.send(sender, "&cPlayers only");
            return;
        }
        Player player = (Player) sender;
        Block target = platform.getTargetBlock(player, 6);
        if (target == null) {
            ColorMessages.send(sender, "&cLook at a registered sign");
            return;
        }
        String key = target.getWorld().getName() + ":" + target.getX() + ":" + target.getY() + ":" + target.getZ();
        if (!store.remove(key)) {
            ColorMessages.send(sender, "&cThat block is not a managed sign");
            return;
        }
        store.save();
        wall.reassignAndRender();
        ColorMessages.send(sender, "&aRemoved managed sign");
    }

    private void cleanup(CommandSender sender, String[] args) {
        String world = args.length >= 2
                ? args[1]
                : (sender instanceof Player ? ((Player) sender).getWorld().getName() : null);
        if (world == null) {
            ColorMessages.send(sender, "&cUsage: /aesign cleanup <world>");
            return;
        }
        int n = store.removeWorld(world);
        store.save();
        wall.reassignAndRender();
        ColorMessages.send(sender, "&aRemoved &f" + n + " &asigns in &e" + world);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<String>();
        if (args.length == 1) {
            for (String s : Arrays.asList("create", "remove", "removeall", "cleanup", "reload", "debug")) {
                if (s.startsWith(args[0].toLowerCase(Locale.ROOT))) {
                    out.add(s);
                }
            }
        }
        return out;
    }
}
