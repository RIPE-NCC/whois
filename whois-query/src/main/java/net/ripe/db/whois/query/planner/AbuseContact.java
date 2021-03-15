package net.ripe.db.whois.query.planner;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;

public class AbuseContact {

    private final RpslObject abuseRole;
    private final boolean suspect;
    private final CIString orgId;

    public AbuseContact(final RpslObject abuseRole,
                        final boolean suspect,
                        final CIString orgId) {
        this.abuseRole = abuseRole;
        this.suspect = suspect;
        this.orgId = orgId;
    }

    public CIString getNicHandle() {
        return abuseRole.getKey();
    }

    public CIString getAbuseMailbox() {
        return abuseRole.getValueForAttribute(AttributeType.ABUSE_MAILBOX);
    }

    public boolean isSuspect() {
        return suspect;
    }

    public CIString getOrgId() {
        return orgId;
    }

    public RpslObject getAbuseRole() {
        return abuseRole;
    }
}
