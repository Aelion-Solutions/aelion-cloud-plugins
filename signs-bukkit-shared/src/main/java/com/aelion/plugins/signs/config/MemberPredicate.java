package com.aelion.plugins.signs.config;

import com.aelion.aero.api.FleetServerSnapshot;
import com.aelion.plugins.signs.util.Strings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * One visibility rule against a fleet member.
 *
 * <p>MOTD predicates are ignored when {@link FleetServerSnapshot#motd()} is null/blank
 * (until the panel/Aero plumbs live MOTD).
 */
public final class MemberPredicate {

    public enum Kind {
        NAME_CONTAINS,
        NAME_MATCHES,
        LIVE_STATUS,
        PLAYERS_MIN,
        PLAYERS_MAX,
        JOINABLE,
        MOTD_CONTAINS,
        MOTD_MATCHES
    }

    private final Kind kind;
    private final String stringValue;
    private final List<String> stringList;
    private final int intValue;
    private final boolean boolValue;
    private final Pattern pattern;

    private MemberPredicate(
            Kind kind,
            String stringValue,
            List<String> stringList,
            int intValue,
            boolean boolValue,
            Pattern pattern
    ) {
        this.kind = kind;
        this.stringValue = stringValue;
        this.stringList = stringList;
        this.intValue = intValue;
        this.boolValue = boolValue;
        this.pattern = pattern;
    }

    public Kind kind() {
        return kind;
    }

    /**
     * @return {@code true} if the member matches this predicate; MOTD rules with no MOTD
     *         always return {@code true} (ignored)
     */
    public boolean matches(FleetServerSnapshot member) {
        if (member == null) {
            return false;
        }
        switch (kind) {
            case NAME_CONTAINS:
                return containsIgnoreCase(member.name(), stringValue);
            case NAME_MATCHES:
                return pattern != null && member.name() != null && pattern.matcher(member.name()).find();
            case LIVE_STATUS: {
                String live = member.liveStatus() == null ? "" : member.liveStatus().toLowerCase(Locale.ROOT);
                for (String allowed : stringList) {
                    if (live.equals(allowed)) {
                        return true;
                    }
                }
                return false;
            }
            case PLAYERS_MIN:
                return member.currentPlayers() >= intValue;
            case PLAYERS_MAX:
                return member.currentPlayers() <= intValue;
            case JOINABLE:
                return member.joinable() == boolValue;
            case MOTD_CONTAINS: {
                String raw = member.motd();
                if (raw == null) {
                    return true; // not reported yet — ignored
                }
                return containsIgnoreCase(stripColors(raw), stringValue);
            }
            case MOTD_MATCHES: {
                String raw = member.motd();
                if (raw == null) {
                    return true; // not reported yet — ignored
                }
                String motd = stripColors(raw);
                return pattern != null && motd != null && pattern.matcher(motd).find();
            }
            default:
                return false;
        }
    }

    /**
     * Human-readable reason used by {@code /aesign debug}.
     */
    public String describe() {
        switch (kind) {
            case NAME_CONTAINS:
                return "name-contains=" + stringValue;
            case NAME_MATCHES:
                return "name-matches=" + stringValue;
            case LIVE_STATUS:
                return "live-status=" + stringList;
            case PLAYERS_MIN:
                return "players-min=" + intValue;
            case PLAYERS_MAX:
                return "players-max=" + intValue;
            case JOINABLE:
                return "joinable=" + boolValue;
            case MOTD_CONTAINS:
                return "motd-contains=" + stringValue;
            case MOTD_MATCHES:
                return "motd-matches=" + stringValue;
            default:
                return kind.name();
        }
    }

    public static MemberPredicate nameContains(String value) {
        return new MemberPredicate(Kind.NAME_CONTAINS, value, null, 0, false, null);
    }

    public static MemberPredicate nameMatches(String regex) {
        return new MemberPredicate(Kind.NAME_MATCHES, regex, null, 0, false, compile(regex));
    }

    public static MemberPredicate liveStatus(List<String> statuses) {
        List<String> normalized = new ArrayList<String>();
        if (statuses != null) {
            for (String s : statuses) {
                if (!Strings.isBlank(s)) {
                    normalized.add(s.trim().toLowerCase(Locale.ROOT));
                }
            }
        }
        return new MemberPredicate(
                Kind.LIVE_STATUS,
                null,
                Collections.unmodifiableList(normalized),
                0,
                false,
                null
        );
    }

    public static MemberPredicate playersMin(int min) {
        return new MemberPredicate(Kind.PLAYERS_MIN, null, null, min, false, null);
    }

    public static MemberPredicate playersMax(int max) {
        return new MemberPredicate(Kind.PLAYERS_MAX, null, null, max, false, null);
    }

    public static MemberPredicate joinable(boolean value) {
        return new MemberPredicate(Kind.JOINABLE, null, null, 0, value, null);
    }

    public static MemberPredicate motdContains(String value) {
        return new MemberPredicate(Kind.MOTD_CONTAINS, value, null, 0, false, null);
    }

    public static MemberPredicate motdMatches(String regex) {
        return new MemberPredicate(Kind.MOTD_MATCHES, regex, null, 0, false, compile(regex));
    }

    /**
     * Parse one map entry from YAML list item, e.g. {@code {name-contains: "bw-"}}.
     *
     * @return predicate, or {@code null} if the map is empty/unknown
     */
    @SuppressWarnings("unchecked")
    public static MemberPredicate fromMap(Object raw) {
        if (!(raw instanceof java.util.Map)) {
            return null;
        }
        java.util.Map<String, Object> map = (java.util.Map<String, Object>) raw;
        if (map.containsKey("name-contains")) {
            return nameContains(String.valueOf(map.get("name-contains")));
        }
        if (map.containsKey("name-matches")) {
            return nameMatches(String.valueOf(map.get("name-matches")));
        }
        if (map.containsKey("live-status")) {
            Object v = map.get("live-status");
            List<String> list = new ArrayList<String>();
            if (v instanceof List) {
                for (Object o : (List<?>) v) {
                    list.add(String.valueOf(o));
                }
            } else if (v != null) {
                list.add(String.valueOf(v));
            }
            return liveStatus(list);
        }
        if (map.containsKey("players-min")) {
            return playersMin(toInt(map.get("players-min"), 0));
        }
        if (map.containsKey("players-max")) {
            return playersMax(toInt(map.get("players-max"), Integer.MAX_VALUE));
        }
        if (map.containsKey("joinable")) {
            return joinable(toBoolean(map.get("joinable")));
        }
        if (map.containsKey("motd-contains")) {
            return motdContains(String.valueOf(map.get("motd-contains")));
        }
        if (map.containsKey("motd-matches")) {
            return motdMatches(String.valueOf(map.get("motd-matches")));
        }
        return null;
    }

    private static Pattern compile(String regex) {
        if (Strings.isBlank(regex)) {
            return null;
        }
        try {
            return Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        } catch (PatternSyntaxException ex) {
            return null;
        }
    }

    private static boolean containsIgnoreCase(String haystack, String needle) {
        if (haystack == null || needle == null) {
            return false;
        }
        return haystack.toLowerCase(Locale.ROOT).contains(needle.toLowerCase(Locale.ROOT));
    }

    static String stripColors(String input) {
        if (input == null) {
            return null;
        }
        // Strip legacy §X and &X color codes
        return input.replaceAll("(?i)[§&][0-9a-fk-or]", "");
    }

    private static int toInt(Object raw, int fallback) {
        if (raw instanceof Number) {
            return ((Number) raw).intValue();
        }
        if (raw == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(String.valueOf(raw).trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private static boolean toBoolean(Object raw) {
        if (raw instanceof Boolean) {
            return (Boolean) raw;
        }
        return "true".equalsIgnoreCase(String.valueOf(raw));
    }
}
