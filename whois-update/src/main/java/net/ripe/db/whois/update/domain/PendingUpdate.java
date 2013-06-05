package net.ripe.db.whois.update.domain;

import net.ripe.db.whois.common.rpsl.RpslObjectBase;
import org.joda.time.LocalDateTime;

public class PendingUpdate {
    final String authenticatedBy;
    final RpslObjectBase object;
    LocalDateTime storedDate;

    public PendingUpdate(final String authenticatedBy, final RpslObjectBase object) {
        this.authenticatedBy = authenticatedBy;
        this.object = object;
        this.storedDate = new LocalDateTime();
    }

    public PendingUpdate(final String authenticatedBy, final RpslObjectBase object, final LocalDateTime storedDate) {
        this(authenticatedBy, object);
        this.storedDate = storedDate;
    }

    public String getAuthenticatedBy() {
        return authenticatedBy;
    }

    public RpslObjectBase getObject() {
        return object;
    }

    public LocalDateTime getStoredDate() {
        return storedDate;
    }
}
