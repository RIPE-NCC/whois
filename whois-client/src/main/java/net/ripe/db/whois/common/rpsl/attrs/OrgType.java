package net.ripe.db.whois.common.rpsl.attrs;

import com.google.common.collect.Maps;
import net.ripe.db.whois.common.domain.CIString;

import javax.annotation.Nullable;
import java.util.Map;

import static net.ripe.db.whois.common.domain.CIString.ciString;

public enum OrgType {
    IANA("for Internet Assigned Numbers Authority"),
    RIR("for Regional Internet Registries"),
    NIR("for National Internet Registries (there are no NIRs in the RIPE NCC service region)"),
    LIR("for Local Internet Registries"),
    WHITEPAGES("for special links to industry people"),
    DIRECT_ASSIGNMENT("for direct contract with RIPE NCC"),
    OTHER("for all other organisations.");

    private static final Map<CIString, OrgType> ORG_TYPE_MAP;
    private final String info;

    private OrgType(final String info) {
        this.info = info;
    }

    public String getInfo() {
        return info;
    }

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
