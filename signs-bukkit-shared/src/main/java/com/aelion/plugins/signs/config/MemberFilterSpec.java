package com.aelion.plugins.signs.config;

import com.aelion.aero.api.FleetServerSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Visibility rules: {@code require-all} (AND) plus {@code reject-any} (OR hide).
 */
public final class MemberFilterSpec {

    private static final MemberFilterSpec EMPTY =
            new MemberFilterSpec(Collections.<MemberPredicate>emptyList(), Collections.<MemberPredicate>emptyList());

    private final List<MemberPredicate> requireAll;
    private final List<MemberPredicate> rejectAny;

    public MemberFilterSpec(List<MemberPredicate> requireAll, List<MemberPredicate> rejectAny) {
        this.requireAll = requireAll == null
                ? Collections.<MemberPredicate>emptyList()
                : Collections.unmodifiableList(new ArrayList<MemberPredicate>(requireAll));
        this.rejectAny = rejectAny == null
                ? Collections.<MemberPredicate>emptyList()
                : Collections.unmodifiableList(new ArrayList<MemberPredicate>(rejectAny));
    }

    public static MemberFilterSpec empty() {
        return EMPTY;
    }

    public List<MemberPredicate> requireAll() {
        return requireAll;
    }

    public List<MemberPredicate> rejectAny() {
        return rejectAny;
    }

    public boolean isEmpty() {
        return requireAll.isEmpty() && rejectAny.isEmpty();
    }

    /**
     * @return {@code true} if the member may be shown / assigned
     */
    public boolean allows(FleetServerSnapshot member) {
        for (MemberPredicate pred : requireAll) {
            if (!pred.matches(member)) {
                return false;
            }
        }
        for (MemberPredicate pred : rejectAny) {
            if (pred.matches(member)) {
                // MOTD predicates return true when motd is null (ignored); do not reject on ignore
                if (isMotdKind(pred.kind()) && member.motd() == null) {
                    continue;
                }
                return false;
            }
        }
        return true;
    }

    /**
     * Short pass/fail reason for debug output.
     */
    public String explain(FleetServerSnapshot member) {
        for (MemberPredicate pred : requireAll) {
            if (!pred.matches(member)) {
                return "fail require-all " + pred.describe();
            }
        }
        for (MemberPredicate pred : rejectAny) {
            if (isMotdKind(pred.kind()) && member.motd() == null) {
                continue;
            }
            if (pred.matches(member)) {
                return "fail reject-any " + pred.describe();
            }
        }
        return "pass";
    }

    public static MemberFilterSpec from(ConfigurationSection section) {
        if (section == null) {
            return empty();
        }
        return new MemberFilterSpec(readList(section.getList("require-all")), readList(section.getList("reject-any")));
    }

    private static List<MemberPredicate> readList(List<?> raw) {
        List<MemberPredicate> out = new ArrayList<MemberPredicate>();
        if (raw == null) {
            return out;
        }
        for (Object item : raw) {
            MemberPredicate pred = MemberPredicate.fromMap(item);
            if (pred != null) {
                out.add(pred);
            }
        }
        return out;
    }

    private static boolean isMotdKind(MemberPredicate.Kind kind) {
        return kind == MemberPredicate.Kind.MOTD_CONTAINS || kind == MemberPredicate.Kind.MOTD_MATCHES;
    }
}
