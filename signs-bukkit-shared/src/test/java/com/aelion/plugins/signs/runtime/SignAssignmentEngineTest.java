package com.aelion.plugins.signs.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aelion.aero.api.AeroFleetService;
import com.aelion.aero.api.FleetGroupSnapshot;
import com.aelion.aero.api.FleetServerSnapshot;
import com.aelion.plugins.signs.config.AnimationConfig;
import com.aelion.plugins.signs.config.AnimationSyncMode;
import com.aelion.plugins.signs.config.ForcefieldConfig;
import com.aelion.plugins.signs.config.ForcefieldShape;
import com.aelion.plugins.signs.config.MemberFilterSpec;
import com.aelion.plugins.signs.config.MemberPredicate;
import com.aelion.plugins.signs.config.SignLayoutState;
import com.aelion.plugins.signs.config.SignLayoutsHolder;
import com.aelion.plugins.signs.config.SignsConfig;
import com.aelion.plugins.signs.model.ManagedSign;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SignAssignmentEngineTest {

    @Test
    void runningNonJoinableStillDisplaysEmpty() {
        FleetServerSnapshot member = server("s1", "arena-1", "running", 0, 20, false, null);
        FleetGroupSnapshot group = group("bedwars", "empty", member);
        ManagedSign sign = new ManagedSign("world", 0, 64, 0, "bedwars", null);

        engine(emptyConfig(false)).reassign(Collections.singletonList(sign), fleet(group));

        assertEquals("s1", sign.assignedServerId());
        assertEquals("empty", sign.wallState());
        assertFalse(sign.assignedJoinable());
    }

    @Test
    void fullWithSwitchToSearchingClearsAssignment() {
        FleetServerSnapshot member = server("s1", "arena-1", "running", 20, 20, false, null);
        FleetGroupSnapshot group = group("bedwars", "full", member);
        ManagedSign sign = new ManagedSign("world", 0, 64, 0, "bedwars", null);

        engine(emptyConfig(true)).reassign(Collections.singletonList(sign), fleet(group));

        assertNull(sign.assignedServerId());
        assertEquals("searching", sign.wallState());
    }

    @Test
    void maxPlayersZeroNeverClassifiedFull() {
        FleetServerSnapshot member = server("s1", "arena-1", "running", 5, 0, true, null);
        assertEquals(SignLayoutState.ONLINE, SignAssignmentEngine.deriveMemberState(member));
    }

    @Test
    void filterRejectLeavesSearching() {
        FleetServerSnapshot member = server("s1", "vip-1", "running", 0, 20, true, null);
        FleetGroupSnapshot group = group("bedwars", "empty", member);
        ManagedSign sign = new ManagedSign("world", 0, 64, 0, "bedwars", null);

        MemberFilterSpec rejectVip = new MemberFilterSpec(
                Collections.<MemberPredicate>emptyList(),
                Collections.singletonList(MemberPredicate.nameContains("vip"))
        );
        SignsConfig config = configWithFilter(rejectVip, true);

        engine(config).reassign(Collections.singletonList(sign), fleet(group));

        assertNull(sign.assignedServerId());
        assertEquals("searching", sign.wallState());
    }

    @Test
    void motdContainsFiltersWhenPresent() {
        FleetServerSnapshot waiting = server("s1", "arena-1", "running", 0, 20, true, "§aWaiting");
        FleetServerSnapshot ingame = server("s2", "arena-2", "running", 2, 20, true, "§cIn-Game");
        FleetGroupSnapshot group = group("bedwars", "online", waiting, ingame);
        ManagedSign sign = new ManagedSign("world", 0, 64, 0, "bedwars", null);

        MemberFilterSpec onlyWaiting = new MemberFilterSpec(
                Collections.singletonList(MemberPredicate.motdContains("Waiting")),
                Collections.<MemberPredicate>emptyList()
        );

        engine(configWithFilter(onlyWaiting, true))
                .reassign(Collections.singletonList(sign), fleet(group));

        assertEquals("s1", sign.assignedServerId());
        assertEquals("empty", sign.wallState());
    }

    @Test
    void preferJoinableWhenRanking() {
        FleetServerSnapshot notJoinable = server("s1", "a", "running", 0, 20, false, null);
        FleetServerSnapshot joinable = server("s2", "b", "running", 0, 20, true, null);
        FleetGroupSnapshot group = group("g", "empty", notJoinable, joinable);
        ManagedSign sign = new ManagedSign("world", 0, 64, 0, "g", null);

        engine(emptyConfig(true)).reassign(Collections.singletonList(sign), fleet(group));

        assertEquals("s2", sign.assignedServerId());
        assertTrue(sign.assignedJoinable());
    }

    private static SignAssignmentEngine engine(SignsConfig config) {
        return new SignAssignmentEngine(config);
    }

    private static SignsConfig emptyConfig(boolean switchWhenFull) {
        return configWithFilter(MemberFilterSpec.empty(), switchWhenFull);
    }

    private static SignsConfig configWithFilter(MemberFilterSpec filter, boolean switchWhenFull) {
        AnimationConfig animation = new AnimationConfig(50L, AnimationSyncMode.GLOBAL, false, 32.0);
        ForcefieldConfig forcefield = new ForcefieldConfig(
                false,
                ForcefieldShape.SPHERE,
                1.0,
                2.0,
                0.0,
                0.8,
                0.25,
                5L,
                250L,
                "aelion.signs.forcefield.bypass",
                EnumSet.allOf(SignLayoutState.class),
                false,
                "CRIT",
                3,
                false,
                "ENTITY_PLAYER_ATTACK_KNOCKBACK",
                0.4f,
                1.2f
        );
        Map<SignLayoutState, SignLayoutsHolder> layouts =
                new EnumMap<SignLayoutState, SignLayoutsHolder>(SignLayoutState.class);
        return new SignsConfig(
                true,
                2000L,
                switchWhenFull,
                animation,
                forcefield,
                layouts,
                new HashMap<String, Map<SignLayoutState, SignLayoutsHolder>>(),
                filter,
                new HashMap<String, MemberFilterSpec>()
        );
    }

    private static FleetServerSnapshot server(
            String id,
            String name,
            String live,
            int online,
            int max,
            boolean joinable,
            String motd
    ) {
        return new FleetServerSnapshot(
                id, name, null, null, live, online, max, "g1", "bedwars", joinable, name, motd
        );
    }

    private static FleetGroupSnapshot group(String name, String live, FleetServerSnapshot... members) {
        int players = 0;
        int max = 0;
        for (FleetServerSnapshot m : members) {
            players += m.currentPlayers();
            max += m.maxPlayers();
        }
        return new FleetGroupSnapshot(
                "gid",
                name,
                "ACTIVE",
                players,
                max,
                members.length,
                live,
                Arrays.asList(members)
        );
    }

    private static AeroFleetService fleet(final FleetGroupSnapshot... groups) {
        final List<FleetGroupSnapshot> list = Arrays.asList(groups);
        return new AeroFleetService() {
            @Override
            public boolean isConfigured() {
                return true;
            }

            @Override
            public void refresh() {
            }

            @Override
            public List<FleetServerSnapshot> listServers() {
                return Collections.emptyList();
            }

            @Override
            public List<FleetGroupSnapshot> listGroups() {
                return list;
            }

            @Override
            public boolean connectPlayer(UUID playerId, String proxyServerName) {
                return false;
            }

            @Override
            public boolean kickPlayer(UUID playerId, String message) {
                return false;
            }

            @Override
            public boolean transferToServer(UUID playerId, String serverIdOrName) {
                return false;
            }

            @Override
            public boolean transferToGroup(UUID playerId, String groupIdOrName) {
                return false;
            }
        };
    }
}
