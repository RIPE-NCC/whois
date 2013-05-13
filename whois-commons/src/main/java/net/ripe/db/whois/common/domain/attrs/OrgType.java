package net.ripe.db.whois.common.domain.attrs;

import com.google.common.collect.Maps;
import net.ripe.db.whois.common.domain.CIString;

import javax.annotation.Nullable;
import java.util.Map;

import static net.ripe.db.whois.common.domain.CIString.ciString;

public enum OrgType {
    IANA, RIR, NIR, LIR, WHITEPAGES, DIRECT_ASSIGNMENT, OTHER;

    private static final Map<CIString, OrgType> ORG_TYPE_MAP;

    static {
        ORG_TYPE_MAP = Maps.newHashMap();

        for (final OrgType orgType : OrgType.values()) {
            ORG_TYPE_MAP.put(ciString(orgType.name()), orgType);
        }
    }

    @Nullable
    public static OrgType getFor(final String value) {
        return getFor(ciString(value));
    }

    @Nullable
    public static OrgType getFor(final CIString value) {
        return ORG_TYPE_MAP.get(value);
    }
}
