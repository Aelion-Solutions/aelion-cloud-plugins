package com.aelion.plugins.signs.config;

import com.aelion.plugins.signs.util.Strings;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public final class SignsConfig {

    private final boolean enabled;
    private final long fleetPollMs;
    private final boolean switchToSearchingWhenFull;
    private final AnimationConfig animation;
    private final ForcefieldConfig forcefield;
    private final Map<SignLayoutState, SignLayoutsHolder> defaults;
    private final Map<String, Map<SignLayoutState, SignLayoutsHolder>> groupLayouts;
    private final MemberFilterSpec defaultMemberFilter;
    private final Map<String, MemberFilterSpec> groupFilters;

    public SignsConfig(
            boolean enabled,
            long fleetPollMs,
            boolean switchToSearchingWhenFull,
            AnimationConfig animation,
            ForcefieldConfig forcefield,
            Map<SignLayoutState, SignLayoutsHolder> defaults,
            Map<String, Map<SignLayoutState, SignLayoutsHolder>> groupLayouts,
            MemberFilterSpec defaultMemberFilter,
            Map<String, MemberFilterSpec> groupFilters
    ) {
        this.enabled = enabled;
        this.fleetPollMs = fleetPollMs;
        this.switchToSearchingWhenFull = switchToSearchingWhenFull;
        this.animation = animation;
        this.forcefield = forcefield;
        this.defaults = defaults;
        this.groupLayouts = groupLayouts;
        this.defaultMemberFilter = defaultMemberFilter == null ? MemberFilterSpec.empty() : defaultMemberFilter;
        this.groupFilters = groupFilters == null
                ? new HashMap<String, MemberFilterSpec>()
                : groupFilters;
    }

    public boolean enabled() {
        return enabled;
    }

    public long fleetPollMs() {
        return fleetPollMs;
    }

    public boolean switchToSearchingWhenFull() {
        return switchToSearchingWhenFull;
    }

    public AnimationConfig animation() {
        return animation;
    }

    public ForcefieldConfig forcefield() {
        return forcefield;
    }

    /** @deprecated use {@link #animation()} */
    @Deprecated
    public long animationTickMs() {
        return animation.tickMs();
    }

    /** @deprecated use {@link #forcefield()} */
    @Deprecated
    public ForcefieldConfig knockback() {
        return forcefield;
    }

    public SignLayoutsHolder layout(String groupName, SignLayoutState state) {
        if (groupName != null) {
            Map<SignLayoutState, SignLayoutsHolder> override =
                    groupLayouts.get(groupName.toLowerCase(Locale.ROOT));
            if (override != null && override.containsKey(state)) {
                return override.get(state);
            }
        }
        SignLayoutsHolder holder = defaults.get(state);
        return holder == null ? SignLayoutsHolder.empty() : holder;
    }

    /**
     * Visibility filter for a target group (group-filters override, else member-filter).
     */
    public MemberFilterSpec memberFilterFor(String groupName) {
        if (groupName != null) {
            MemberFilterSpec override = groupFilters.get(groupName.toLowerCase(Locale.ROOT));
            if (override != null) {
                return override;
            }
        }
        return defaultMemberFilter;
    }

    public static SignsConfig from(FileConfiguration yaml) {
        AnimationConfig animation = readAnimation(yaml);
        ForcefieldConfig forcefield = readForcefield(yaml);

        Map<SignLayoutState, SignLayoutsHolder> defaults = readLayouts(yaml.getConfigurationSection("layouts"));
        Map<String, Map<SignLayoutState, SignLayoutsHolder>> groupLayouts =
                new HashMap<String, Map<SignLayoutState, SignLayoutsHolder>>();
        ConfigurationSection groups = yaml.getConfigurationSection("group-layouts");
        if (groups != null) {
            for (String key : groups.getKeys(false)) {
                groupLayouts.put(key.toLowerCase(Locale.ROOT), readLayouts(groups.getConfigurationSection(key)));
            }
        }

        MemberFilterSpec defaultFilter = MemberFilterSpec.from(yaml.getConfigurationSection("member-filter"));
        Map<String, MemberFilterSpec> groupFilters = new HashMap<String, MemberFilterSpec>();
        ConfigurationSection filterGroups = yaml.getConfigurationSection("group-filters");
        if (filterGroups != null) {
            for (String key : filterGroups.getKeys(false)) {
                groupFilters.put(
                        key.toLowerCase(Locale.ROOT),
                        MemberFilterSpec.from(filterGroups.getConfigurationSection(key))
                );
            }
        }

        return new SignsConfig(
                yaml.getBoolean("enabled", true),
                Math.max(500L, yaml.getLong("fleet-poll-ms", 2000L)),
                yaml.getBoolean("switch-to-searching-when-full", true),
                animation,
                forcefield,
                defaults,
                groupLayouts,
                defaultFilter,
                groupFilters
        );
    }

    private static AnimationConfig readAnimation(FileConfiguration yaml) {
        ConfigurationSection section = yaml.getConfigurationSection("animation");
        long tickMs;
        AnimationSyncMode syncMode;
        boolean onlyNear;
        double nearRadius;
        if (section != null) {
            tickMs = Math.max(20L, section.getLong("tick-ms", 50L));
            syncMode = AnimationSyncMode.fromConfig(section.getString("sync-mode", "global"));
            onlyNear = section.getBoolean("only-when-players-near", false);
            nearRadius = Math.max(1.0, section.getDouble("player-near-radius", 32.0));
        } else {
            tickMs = Math.max(20L, yaml.getLong("animation-tick-ms", 50L));
            syncMode = AnimationSyncMode.GLOBAL;
            onlyNear = false;
            nearRadius = 32.0;
        }
        return new AnimationConfig(tickMs, syncMode, onlyNear, nearRadius);
    }

    private static ForcefieldConfig readForcefield(FileConfiguration yaml) {
        ConfigurationSection ff = yaml.getConfigurationSection("forcefield");
        ConfigurationSection knock = yaml.getConfigurationSection("knockback");
        ConfigurationSection src = ff != null ? ff : knock;

        boolean enabled = src == null || src.getBoolean("enabled", true);
        ForcefieldShape shape = ForcefieldShape.fromConfig(src == null ? "sphere" : src.getString("shape", "sphere"));
        double radius = src == null
                ? 1.0
                : src.getDouble("radius", src.getDouble("distance", 1.0));
        double height = src == null ? 2.0 : src.getDouble("height", 2.0);
        double yOffset = src == null ? 0.0 : src.getDouble("y-offset", 0.0);
        double strength = src == null ? 0.8 : src.getDouble("strength", 0.8);
        double verticalStrength = src == null ? 0.25 : src.getDouble("vertical-strength", 0.25);
        long checkInterval = src == null ? 5L : Math.max(1L, src.getLong("check-interval-ticks", 5L));
        long cooldownMs = src == null ? 250L : Math.max(0L, src.getLong("cooldown-ms", 250L));
        String bypass = src == null
                ? "aelion.signs.forcefield.bypass"
                : src.getString(
                        "bypass-permission",
                        ff != null
                                ? "aelion.signs.forcefield.bypass"
                                : "aelion.signs.knockback.bypass");

        Set<SignLayoutState> applyWhen = EnumSet.allOf(SignLayoutState.class);
        if (src != null && src.isList("apply-when")) {
            applyWhen = EnumSet.noneOf(SignLayoutState.class);
            for (String raw : src.getStringList("apply-when")) {
                applyWhen.add(SignLayoutState.fromConfigKey(raw));
            }
            if (applyWhen.isEmpty()) {
                applyWhen = EnumSet.allOf(SignLayoutState.class);
            }
        }

        boolean particlesEnabled = false;
        String particleType = "CRIT";
        int particleCount = 3;
        boolean soundEnabled = false;
        String soundType = "ENTITY_PLAYER_ATTACK_KNOCKBACK";
        float soundVolume = 0.4f;
        float soundPitch = 1.2f;
        if (src != null) {
            ConfigurationSection particles = src.getConfigurationSection("particles");
            if (particles != null) {
                particlesEnabled = particles.getBoolean("enabled", false);
                particleType = particles.getString("type", "CRIT");
                particleCount = Math.max(1, particles.getInt("count", 3));
            }
            ConfigurationSection sound = src.getConfigurationSection("sound");
            if (sound != null) {
                soundEnabled = sound.getBoolean("enabled", false);
                soundType = sound.getString("type", "ENTITY_PLAYER_ATTACK_KNOCKBACK");
                soundVolume = (float) sound.getDouble("volume", 0.4);
                soundPitch = (float) sound.getDouble("pitch", 1.2);
            }
        }

        return new ForcefieldConfig(
                enabled,
                shape,
                radius,
                height,
                yOffset,
                strength,
                verticalStrength,
                checkInterval,
                cooldownMs,
                bypass,
                applyWhen,
                particlesEnabled,
                particleType,
                particleCount,
                soundEnabled,
                soundType,
                soundVolume,
                soundPitch
        );
    }

    private static Map<SignLayoutState, SignLayoutsHolder> readLayouts(ConfigurationSection section) {
        Map<SignLayoutState, SignLayoutsHolder> map = new EnumMap<SignLayoutState, SignLayoutsHolder>(SignLayoutState.class);
        if (section == null) {
            return map;
        }
        for (SignLayoutState state : SignLayoutState.values()) {
            ConfigurationSection stateSec = section.getConfigurationSection(state.configKey());
            if (stateSec == null) {
                continue;
            }
            double aps = stateSec.getDouble("animations-per-second", 0);
            AnimationMode mode = AnimationMode.fromConfig(stateSec.getString("mode", "loop"));
            List<SignLayoutFrame> frames = new ArrayList<SignLayoutFrame>();
            List<?> rawFrames = stateSec.getList("frames");
            if (rawFrames != null) {
                for (Object raw : rawFrames) {
                    if (!(raw instanceof Map)) {
                        continue;
                    }
                    @SuppressWarnings("unchecked")
                    Map<String, Object> typed = (Map<String, Object>) raw;
                    List<String> lines = new ArrayList<String>();
                    Object linesObj = typed.get("lines");
                    if (linesObj instanceof List) {
                        for (Object line : (List<?>) linesObj) {
                            lines.add(line == null ? "" : String.valueOf(line));
                        }
                    }
                    Material material = Material.AIR;
                    Object matObj = typed.get("block-material");
                    if (matObj != null) {
                        Material parsed = resolveMaterial(String.valueOf(matObj));
                        if (parsed != null) {
                            material = parsed;
                        }
                    }
                    frames.add(new SignLayoutFrame(lines, material));
                }
            }
            map.put(state, new SignLayoutsHolder(aps, mode, frames));
        }
        return map;
    }

    private static Material resolveMaterial(String name) {
        if (Strings.isBlank(name)) {
            return null;
        }
        Material matched = Material.matchMaterial(name);
        if (matched != null) {
            return matched;
        }
        // Common modern→legacy aliases for 1.8–1.12
        String upper = name.trim().toUpperCase(Locale.ROOT);
        if ("REDSTONE_BLOCK".equals(upper)) {
            return Material.matchMaterial("REDSTONE_BLOCK");
        }
        if ("GOLD_BLOCK".equals(upper)) {
            return Material.matchMaterial("GOLD_BLOCK");
        }
        if ("EMERALD_BLOCK".equals(upper)) {
            return Material.matchMaterial("EMERALD_BLOCK");
        }
        if ("BEDROCK".equals(upper)) {
            return Material.matchMaterial("BEDROCK");
        }
        return null;
    }
}
