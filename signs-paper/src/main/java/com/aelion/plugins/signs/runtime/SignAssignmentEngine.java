package com.aelion.plugins.signs.runtime;

import com.aelion.aero.api.AeroFleetService;
import com.aelion.aero.api.FleetGroupSnapshot;
import com.aelion.aero.api.FleetServerSnapshot;
import com.aelion.plugins.signs.config.SignLayoutState;
import com.aelion.plugins.signs.config.SignsConfig;
import com.aelion.plugins.signs.model.ManagedSign;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Assigns one joinable group member to each free wall sign (priority steal + waiting set).
 */
public final class SignAssignmentEngine {

    private final SignsConfig config;

    public SignAssignmentEngine(SignsConfig config) {
        this.config = config;
    }

    public void reassign(Iterable<ManagedSign> signs, AeroFleetService fleet) {
        Map<String, FleetGroupSnapshot> groupsByName = new HashMap<>();
        for (FleetGroupSnapshot group : fleet.listGroups()) {
            if (group.name() != null) {
                groupsByName.put(group.name().toLowerCase(Locale.ROOT), group);
            }
        }

        Set<String> claimed = new HashSet<>();
        List<ManagedSign> ordered = new ArrayList<>();
        signs.forEach(ordered::add);
        ordered.sort(Comparator.comparing(ManagedSign::key));

        // First pass: keep still-valid assignments
        for (ManagedSign sign : ordered) {
            FleetGroupSnapshot group = groupsByName.get(sign.targetGroup().toLowerCase(Locale.ROOT));
            if (group == null) {
                sign.clearAssignment();
                continue;
            }
            FleetServerSnapshot current = findMember(group, sign.assignedServerId());
            if (current != null && current.joinable() && !claimed.contains(current.id())) {
                apply(sign, current);
                claimed.add(current.id());
            } else {
                sign.clearAssignment();
            }
        }

        // Second pass: fill empty signs from remaining joinable members
        for (ManagedSign sign : ordered) {
            if (sign.assignedServerId() != null) {
                continue;
            }
            FleetGroupSnapshot group = groupsByName.get(sign.targetGroup().toLowerCase(Locale.ROOT));
            if (group == null) {
                continue;
            }
            FleetServerSnapshot best = pickBest(group, claimed, sign.templateFilter());
            if (best != null) {
                apply(sign, best);
                claimed.add(best.id());
            } else {
                SignLayoutState state = deriveGroupIdleState(group);
                sign.assign(null, null, group.name(), group.currentPlayers(), group.maxPlayers(), state.configKey());
            }
        }
    }

    private void apply(ManagedSign sign, FleetServerSnapshot member) {
        SignLayoutState state = deriveMemberState(member);
        if (state == SignLayoutState.FULL && config.switchToSearchingWhenFull()) {
            state = SignLayoutState.SEARCHING;
            sign.clearAssignment();
            sign.assign(null, null, member.name(), member.currentPlayers(), member.maxPlayers(), state.configKey());
            return;
        }
        sign.assign(
                member.id(),
                member.proxyName(),
                member.name(),
                member.currentPlayers(),
                member.maxPlayers(),
                state.configKey()
        );
    }

    private static FleetServerSnapshot findMember(FleetGroupSnapshot group, String serverId) {
        if (serverId == null) {
            return null;
        }
        for (FleetServerSnapshot member : group.members()) {
            if (serverId.equals(member.id())) {
                return member;
            }
        }
        return null;
    }

    private static FleetServerSnapshot pickBest(
            FleetGroupSnapshot group,
            Set<String> claimed,
            String templateFilter
    ) {
        FleetServerSnapshot best = null;
        int bestPriority = Integer.MIN_VALUE;
        for (FleetServerSnapshot member : group.members()) {
            if (claimed.contains(member.id()) || !member.joinable()) {
                continue;
            }
            if (templateFilter != null && member.name() != null
                    && !member.name().toLowerCase(Locale.ROOT).contains(templateFilter.toLowerCase(Locale.ROOT))) {
                continue;
            }
            int priority = priority(member);
            if (priority > bestPriority) {
                bestPriority = priority;
                best = member;
            }
        }
        return best;
    }

    private static int priority(FleetServerSnapshot member) {
        SignLayoutState state = deriveMemberState(member);
        return switch (state) {
            case EMPTY -> 3;
            case ONLINE -> 2;
            case STARTING -> 1;
            case FULL -> 0;
            case SEARCHING -> -1;
        };
    }

    static SignLayoutState deriveMemberState(FleetServerSnapshot member) {
        String live = member.liveStatus() == null ? "" : member.liveStatus().toUpperCase(Locale.ROOT);
        if ("STARTING".equals(live) || "PROVISIONING".equals(live) || "RESTARTING".equals(live)) {
            return SignLayoutState.STARTING;
        }
        if (!"RUNNING".equals(live)) {
            return SignLayoutState.SEARCHING;
        }
        if (member.currentPlayers() <= 0) {
            return SignLayoutState.EMPTY;
        }
        if (member.currentPlayers() >= member.maxPlayers()) {
            return SignLayoutState.FULL;
        }
        return SignLayoutState.ONLINE;
    }

    static SignLayoutState deriveGroupIdleState(FleetGroupSnapshot group) {
        String live = group.liveStatus() == null ? "searching" : group.liveStatus().toLowerCase(Locale.ROOT);
        return SignLayoutState.fromConfigKey(live);
    }
}
