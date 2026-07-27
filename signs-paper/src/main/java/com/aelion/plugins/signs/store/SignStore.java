package com.aelion.plugins.signs.store;

import com.aelion.plugins.signs.model.ManagedSign;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class SignStore {

    private final JavaPlugin plugin;
    private final File file;
    private final Map<String, ManagedSign> signs = new LinkedHashMap<>();

    public SignStore(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "signs.yml");
    }

    public Collection<ManagedSign> all() {
        return List.copyOf(signs.values());
    }

    public ManagedSign get(String key) {
        return signs.get(key);
    }

    public ManagedSign getAt(String world, int x, int y, int z) {
        return signs.get(world + ":" + x + ":" + y + ":" + z);
    }

    public void put(ManagedSign sign) {
        signs.put(sign.key(), sign);
    }

    public boolean remove(String key) {
        return signs.remove(key) != null;
    }

    public int removeAll() {
        int size = signs.size();
        signs.clear();
        return size;
    }

    public int removeWorld(String world) {
        List<String> keys = new ArrayList<>();
        for (ManagedSign sign : signs.values()) {
            if (sign.world().equalsIgnoreCase(world)) {
                keys.add(sign.key());
            }
        }
        keys.forEach(signs::remove);
        return keys.size();
    }

    public void load() {
        signs.clear();
        if (!file.exists()) {
            return;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        List<?> list = yaml.getList("signs");
        if (list == null) {
            ConfigurationSection section = yaml.getConfigurationSection("signs");
            if (section != null) {
                for (String key : section.getKeys(false)) {
                    ConfigurationSection entry = section.getConfigurationSection(key);
                    if (entry != null) {
                        addFromSection(entry);
                    }
                }
            }
            return;
        }
        for (Object raw : list) {
            if (raw instanceof Map<?, ?> map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> typed = (Map<String, Object>) map;
                String world = String.valueOf(typed.getOrDefault("world", ""));
                int x = toInt(typed.get("x"));
                int y = toInt(typed.get("y"));
                int z = toInt(typed.get("z"));
                String group = String.valueOf(typed.getOrDefault("target-group", ""));
                String filter = typed.get("template-filter") == null
                        ? null
                        : String.valueOf(typed.get("template-filter"));
                if (!world.isBlank() && !group.isBlank()) {
                    put(new ManagedSign(world, x, y, z, group, filter));
                }
            }
        }
    }

    public void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        List<Map<String, Object>> list = new ArrayList<>();
        for (ManagedSign sign : signs.values()) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("world", sign.world());
            entry.put("x", sign.x());
            entry.put("y", sign.y());
            entry.put("z", sign.z());
            entry.put("target-group", sign.targetGroup());
            if (sign.templateFilter() != null) {
                entry.put("template-filter", sign.templateFilter());
            }
            list.add(entry);
        }
        yaml.set("signs", list);
        try {
            if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
                plugin.getLogger().warning("Could not create plugin data folder");
            }
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save signs.yml", e);
        }
    }

    private void addFromSection(ConfigurationSection entry) {
        String world = entry.getString("world", "");
        String group = entry.getString("target-group", "");
        if (world.isBlank() || group.isBlank()) {
            return;
        }
        put(new ManagedSign(
                world,
                entry.getInt("x"),
                entry.getInt("y"),
                entry.getInt("z"),
                group,
                entry.getString("template-filter")
        ));
    }

    private static int toInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
