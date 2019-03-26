package net.ripe.db.whois.query.planner;

import net.ripe.db.whois.common.domain.CIString;

public class AbuseContact {

    private final CIString nicHandle;
    private final CIString abuseMailbox;
    private final boolean suspect;

    public AbuseContact(final CIString nicHandle, final CIString abuseMailbox, final boolean suspect) {
        this.nicHandle = nicHandle;
        this.abuseMailbox = abuseMailbox;
        this.suspect = suspect;
    }

    public CIString getNicHandle() {
        return nicHandle;
    }

    public CIString getAbuseMailbox() {
        return abuseMailbox;
    }

    public boolean isSuspect() {
        return suspect;
    }
}
