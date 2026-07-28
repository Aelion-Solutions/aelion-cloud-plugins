package com.aelion.plugins.signs.model;

import com.aelion.plugins.signs.util.Strings;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public final class ManagedSign {

    private final String world;
    private final int x;
    private final int y;
    private final int z;
    private final String targetGroup;
    private final String templateFilter;

    private String assignedServerId;
    private String assignedProxyName;
    private String assignedDisplayName;
    private int assignedOnline;
    private int assignedMax;
    private boolean assignedJoinable;
    private String assignedMotd;
    private String wallState = "searching";

    public ManagedSign(String world, int x, int y, int z, String targetGroup, String templateFilter) {
        this.world = Objects.requireNonNull(world, "world");
        this.x = x;
        this.y = y;
        this.z = z;
        this.targetGroup = Objects.requireNonNull(targetGroup, "targetGroup");
        this.templateFilter = Strings.isBlank(templateFilter) ? null : templateFilter.trim();
    }

    public String world() {
        return world;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int z() {
        return z;
    }

    public String targetGroup() {
        return targetGroup;
    }

    public String templateFilter() {
        return templateFilter;
    }

    public String key() {
        return world + ":" + x + ":" + y + ":" + z;
    }

    public Location location() {
        World w = Bukkit.getWorld(world);
        if (w == null) {
            return null;
        }
        return new Location(w, x, y, z);
    }

    public String assignedServerId() {
        return assignedServerId;
    }

    public String assignedProxyName() {
        return assignedProxyName;
    }

    public String assignedDisplayName() {
        return assignedDisplayName;
    }

    public int assignedOnline() {
        return assignedOnline;
    }

    public int assignedMax() {
        return assignedMax;
    }

    public boolean assignedJoinable() {
        return assignedJoinable;
    }

    public String assignedMotd() {
        return assignedMotd;
    }

    public String wallState() {
        return wallState;
    }

    public void clearAssignment() {
        this.assignedServerId = null;
        this.assignedProxyName = null;
        this.assignedDisplayName = null;
        this.assignedOnline = 0;
        this.assignedMax = 0;
        this.assignedJoinable = false;
        this.assignedMotd = null;
        this.wallState = "searching";
    }

    public void assign(
            String serverId,
            String proxyName,
            String displayName,
            int online,
            int max,
            boolean joinable,
            String motd,
            String wallState
    ) {
        this.assignedServerId = serverId;
        this.assignedProxyName = proxyName;
        this.assignedDisplayName = displayName;
        this.assignedOnline = online;
        this.assignedMax = max;
        this.assignedJoinable = joinable;
        this.assignedMotd = motd;
        this.wallState = wallState;
    }

    public static ManagedSign fromLocation(Location location, String targetGroup, String templateFilter) {
        return new ManagedSign(
                location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ(),
                targetGroup,
                templateFilter
        );
    }
}
