package com.aelion.plugins.signs.config;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public final class SignsConfig {

    private final boolean enabled;
    private final long fleetPollMs;
    private final long animationTickMs;
    private final boolean switchToSearchingWhenFull;
    private final KnockbackConfig knockback;
    private final Map<SignLayoutState, SignLayoutsHolder> defaults;
    private final Map<String, Map<SignLayoutState, SignLayoutsHolder>> groupLayouts;

    public SignsConfig(
            boolean enabled,
            long fleetPollMs,
            long animationTickMs,
            boolean switchToSearchingWhenFull,
            KnockbackConfig knockback,
            Map<SignLayoutState, SignLayoutsHolder> defaults,
            Map<String, Map<SignLayoutState, SignLayoutsHolder>> groupLayouts
    ) {
        this.enabled = enabled;
        this.fleetPollMs = fleetPollMs;
        this.animationTickMs = animationTickMs;
        this.switchToSearchingWhenFull = switchToSearchingWhenFull;
        this.knockback = knockback;
        this.defaults = defaults;
        this.groupLayouts = groupLayouts;
    }

    public boolean enabled() {
        return enabled;
    }

    public long fleetPollMs() {
        return fleetPollMs;
    }

    public long animationTickMs() {
        return animationTickMs;
    }

    public boolean switchToSearchingWhenFull() {
        return switchToSearchingWhenFull;
    }

    public KnockbackConfig knockback() {
        return knockback;
    }

    public SignLayoutsHolder layout(String groupName, SignLayoutState state) {
        if (groupName != null) {
            Map<SignLayoutState, SignLayoutsHolder> override = groupLayouts.get(groupName.toLowerCase(Locale.ROOT));
            if (override != null && override.containsKey(state)) {
                return override.get(state);
            }
        }
        return defaults.getOrDefault(state, SignLayoutsHolder.empty());
    }

    public static SignsConfig from(FileConfiguration yaml) {
        ConfigurationSection knock = yaml.getConfigurationSection("knockback");
        KnockbackConfig knockback = new KnockbackConfig(
                knock == null || knock.getBoolean("enabled", true),
                knock == null ? 1.0 : knock.getDouble("distance", 1.0),
                knock == null ? 0.8 : knock.getDouble("strength", 0.8),
                knock == null
                        ? "aelion.signs.knockback.bypass"
                        : knock.getString("bypass-permission", "aelion.signs.knockback.bypass")
        );

        Map<SignLayoutState, SignLayoutsHolder> defaults = readLayouts(yaml.getConfigurationSection("layouts"));
        Map<String, Map<SignLayoutState, SignLayoutsHolder>> groupLayouts = new HashMap<>();
        ConfigurationSection groups = yaml.getConfigurationSection("group-layouts");
        if (groups != null) {
            for (String key : groups.getKeys(false)) {
                groupLayouts.put(key.toLowerCase(Locale.ROOT), readLayouts(groups.getConfigurationSection(key)));
            }
        }

        return new SignsConfig(
                yaml.getBoolean("enabled", true),
                Math.max(500L, yaml.getLong("fleet-poll-ms", 2000L)),
                Math.max(20L, yaml.getLong("animation-tick-ms", 50L)),
                yaml.getBoolean("switch-to-searching-when-full", true),
                knockback,
                defaults,
                groupLayouts
        );
    }

    private static Map<SignLayoutState, SignLayoutsHolder> readLayouts(ConfigurationSection section) {
        Map<SignLayoutState, SignLayoutsHolder> map = new EnumMap<>(SignLayoutState.class);
        if (section == null) {
            return map;
        }
        for (SignLayoutState state : SignLayoutState.values()) {
            ConfigurationSection stateSec = section.getConfigurationSection(state.configKey());
            if (stateSec == null) {
                continue;
            }
            double aps = stateSec.getDouble("animations-per-second", 0);
            List<SignLayoutFrame> frames = new ArrayList<>();
            List<?> rawFrames = stateSec.getList("frames");
            if (rawFrames != null) {
                for (Object raw : rawFrames) {
                    if (!(raw instanceof Map<?, ?> frameMap)) {
                        continue;
                    }
                    @SuppressWarnings("unchecked")
                    Map<String, Object> typed = (Map<String, Object>) frameMap;
                    List<String> lines = new ArrayList<>();
                    Object linesObj = typed.get("lines");
                    if (linesObj instanceof List<?> list) {
                        for (Object line : list) {
                            lines.add(line == null ? "" : String.valueOf(line));
                        }
                    }
                    Material material = Material.AIR;
                    Object matObj = typed.get("block-material");
                    if (matObj != null) {
                        Material parsed = Material.matchMaterial(String.valueOf(matObj));
                        if (parsed != null) {
                            material = parsed;
                        }
                    }
                    frames.add(new SignLayoutFrame(lines, material));
                }
            }
            map.put(state, new SignLayoutsHolder(aps, frames));
        }
        return map;
    }

    public record KnockbackConfig(boolean enabled, double distance, double strength, String bypassPermission) {
    }
}
