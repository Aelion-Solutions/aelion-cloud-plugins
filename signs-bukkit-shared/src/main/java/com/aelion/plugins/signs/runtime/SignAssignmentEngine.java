package com.aelion.plugins.signs.runtime;

import com.aelion.aero.api.AeroFleetService;
import com.aelion.aero.api.FleetGroupSnapshot;
import com.aelion.aero.api.FleetServerSnapshot;
import com.aelion.plugins.signs.config.MemberFilterSpec;
import com.aelion.plugins.signs.config.SignLayoutState;
import com.aelion.plugins.signs.config.SignsConfig;
import com.aelion.plugins.signs.model.ManagedSign;
import com.aelion.plugins.signs.util.Strings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Assigns one displayable group member to each free wall sign.
 *
 * <p>Display eligibility uses liveStatus (+ config filters), not solely {@code joinable}.
 * Connect remains gated on joinable + proxy name at click time.
 */
public final class SignAssignmentEngine {

    private final SignsConfig config;

    public SignAssignmentEngine(SignsConfig config) {
        this.config = config;
    }

    public void reassign(Iterable<ManagedSign> signs, AeroFleetService fleet) {
        Map<String, FleetGroupSnapshot> groupsByName = new HashMap<String, FleetGroupSnapshot>();
        for (FleetGroupSnapshot group : fleet.listGroups()) {
            if (group.name() != null) {
                groupsByName.put(group.name().toLowerCase(Locale.ROOT), group);
            }
        }

        Set<String> claimed = new HashSet<String>();
        List<ManagedSign> ordered = new ArrayList<ManagedSign>();
        for (ManagedSign sign : signs) {
            ordered.add(sign);
        }
        Collections.sort(ordered, new Comparator<ManagedSign>() {
            @Override
            public int compare(ManagedSign a, ManagedSign b) {
                return a.key().compareTo(b.key());
            }
        });

        for (ManagedSign sign : ordered) {
            FleetGroupSnapshot group = groupsByName.get(sign.targetGroup().toLowerCase(Locale.ROOT));
            if (group == null) {
                sign.clearAssignment();
                continue;
            }
            FleetServerSnapshot current = findMember(group, sign.assignedServerId());
            if (current != null
                    && isDisplayCandidate(current, sign)
                    && !claimed.contains(current.id())) {
                apply(sign, current);
                claimed.add(current.id());
            } else {
                sign.clearAssignment();
            }
        }

        for (ManagedSign sign : ordered) {
            if (sign.assignedServerId() != null) {
                continue;
            }
            FleetGroupSnapshot group = groupsByName.get(sign.targetGroup().toLowerCase(Locale.ROOT));
            if (group == null) {
                continue;
            }
            FleetServerSnapshot best = pickBest(group, claimed, sign);
            if (best != null) {
                apply(sign, best);
                claimed.add(best.id());
            } else {
                SignLayoutState state = deriveIdleState(group, sign, claimed);
                sign.assign(
                        null,
                        null,
                        group.name(),
                        group.currentPlayers(),
                        group.maxPlayers(),
                        false,
                        null,
                        state.configKey()
                );
            }
        }
    }

    private void apply(ManagedSign sign, FleetServerSnapshot member) {
        SignLayoutState state = deriveMemberState(member);
        if (state == SignLayoutState.FULL && config.switchToSearchingWhenFull()) {
            sign.clearAssignment();
            sign.assign(
                    null,
                    null,
                    member.name(),
                    member.currentPlayers(),
                    member.maxPlayers(),
                    false,
                    member.motd(),
                    SignLayoutState.SEARCHING.configKey()
            );
            return;
        }
        sign.assign(
                member.id(),
                member.proxyName(),
                member.name(),
                member.currentPlayers(),
                member.maxPlayers(),
                member.joinable(),
                member.motd(),
                state.configKey()
        );
    }

    private FleetServerSnapshot pickBest(
            FleetGroupSnapshot group,
            Set<String> claimed,
            ManagedSign sign
    ) {
        FleetServerSnapshot best = null;
        int bestPriority = Integer.MIN_VALUE;
        for (FleetServerSnapshot member : group.members()) {
            if (claimed.contains(member.id()) || !isDisplayCandidate(member, sign)) {
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

    private boolean isDisplayCandidate(FleetServerSnapshot member, ManagedSign sign) {
        if (member == null || Strings.isBlank(member.id())) {
            return false;
        }
        SignLayoutState state = deriveMemberState(member);
        // Display RUNNING (empty/online/full) and STARTING — not raw searching/offline
        if (state == SignLayoutState.SEARCHING) {
            return false;
        }
        if (sign.templateFilter() != null && member.name() != null
                && !member.name().toLowerCase(Locale.ROOT)
                .contains(sign.templateFilter().toLowerCase(Locale.ROOT))) {
            return false;
        }
        return config.memberFilterFor(sign.targetGroup()).allows(member);
    }

    private static int priority(FleetServerSnapshot member) {
        SignLayoutState state = deriveMemberState(member);
        int base;
        switch (state) {
            case EMPTY:
                base = 30;
                break;
            case ONLINE:
                base = 20;
                break;
            case STARTING:
                base = 10;
                break;
            case FULL:
                base = 0;
                break;
            case SEARCHING:
            default:
                return -1;
        }
        // Prefer joinable members for connect-ready walls
        if (member.joinable()) {
            base += 5;
        }
        return base;
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
        // maxPlayers <= 0 means unknown — never treat as full
        if (member.maxPlayers() > 0 && member.currentPlayers() >= member.maxPlayers()) {
            return SignLayoutState.FULL;
        }
        return SignLayoutState.ONLINE;
    }

    private SignLayoutState deriveIdleState(
            FleetGroupSnapshot group,
            ManagedSign sign,
            Set<String> claimed
    ) {
        SignLayoutState fromUnclaimed = bestVisibleMemberState(group, sign, claimed);
        if (fromUnclaimed != null && fromUnclaimed != SignLayoutState.SEARCHING) {
            return fromUnclaimed;
        }
        // Other wall signs already claimed every suitable member — stay Searching
        if (hasClaimedDisplayable(group, sign, claimed)) {
            return SignLayoutState.SEARCHING;
        }
        // Members exist but none pass filters → Searching (don't fall back to group empty/online)
        if (hasMembers(group) && allDisplayableRejectedByFilter(group, sign)) {
            return SignLayoutState.SEARCHING;
        }
        return deriveGroupIdleState(group);
    }

    private boolean hasClaimedDisplayable(
            FleetGroupSnapshot group,
            ManagedSign sign,
            Set<String> claimed
    ) {
        if (claimed == null || claimed.isEmpty()) {
            return false;
        }
        for (FleetServerSnapshot member : group.members()) {
            if (member.id() == null || !claimed.contains(member.id())) {
                continue;
            }
            if (isDisplayCandidate(member, sign)) {
                return true;
            }
        }
        return false;
    }

    private boolean allDisplayableRejectedByFilter(FleetGroupSnapshot group, ManagedSign sign) {
        boolean sawDisplayable = false;
        for (FleetServerSnapshot member : group.members()) {
            if (deriveMemberState(member) == SignLayoutState.SEARCHING) {
                continue;
            }
            sawDisplayable = true;
            if (passesTemplateAndConfigFilter(member, sign)) {
                return false;
            }
        }
        return sawDisplayable;
    }

    private boolean passesTemplateAndConfigFilter(FleetServerSnapshot member, ManagedSign sign) {
        if (sign.templateFilter() != null && member.name() != null
                && !member.name().toLowerCase(Locale.ROOT)
                .contains(sign.templateFilter().toLowerCase(Locale.ROOT))) {
            return false;
        }
        return config.memberFilterFor(sign.targetGroup()).allows(member);
    }

    private static boolean hasMembers(FleetGroupSnapshot group) {
        return group.members() != null && !group.members().isEmpty();
    }

    private SignLayoutState bestVisibleMemberState(
            FleetGroupSnapshot group,
            ManagedSign sign,
            Set<String> claimed
    ) {
        SignLayoutState best = null;
        int bestPri = Integer.MIN_VALUE;
        for (FleetServerSnapshot member : group.members()) {
            if (member.id() != null && claimed != null && claimed.contains(member.id())) {
                continue;
            }
            if (!isDisplayCandidate(member, sign)) {
                continue;
            }
            SignLayoutState state = deriveMemberState(member);
            int pri = priority(member);
            if (pri > bestPri) {
                bestPri = pri;
                best = state;
            }
        }
        return best;
    }

    static SignLayoutState deriveGroupIdleState(FleetGroupSnapshot group) {
        String live = group.liveStatus() == null ? "searching" : group.liveStatus().toLowerCase(Locale.ROOT);
        return SignLayoutState.fromConfigKey(live);
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

    /** Package-visible for debug / tests. */
    boolean passesFilter(FleetServerSnapshot member, String groupName, String templateFilter) {
        ManagedSign probe = new ManagedSign("world", 0, 0, 0, groupName == null ? "" : groupName, templateFilter);
        return isDisplayCandidate(member, probe);
    }

    MemberFilterSpec filterFor(String groupName) {
        return config.memberFilterFor(groupName);
    }
}
