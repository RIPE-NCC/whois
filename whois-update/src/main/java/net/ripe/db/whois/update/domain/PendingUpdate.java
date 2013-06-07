package net.ripe.db.whois.update.domain;

import net.ripe.db.whois.common.rpsl.RpslObject;
import org.joda.time.LocalDateTime;

import javax.annotation.concurrent.Immutable;
import java.util.Set;

@Immutable
public class PendingUpdate {
    final Set<String> passedAuthentications;
    final RpslObject object;
    LocalDateTime storedDate;
    long id;

    public PendingUpdate(final Set<String> passedAuthentications, final RpslObject object, final LocalDateTime storedDate) {
        this.passedAuthentications = passedAuthentications;
        this.object = object;
        this.storedDate = storedDate;
    }

    public PendingUpdate(final long id, final Set<String> passedAuthentications, final RpslObject object, final LocalDateTime storedDate) {
        this(passedAuthentications, object, storedDate);
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public Set<String> getPassedAuthentications() {
        return passedAuthentications;
    }

    public RpslObject getObject() {
        return object;
    }

    public LocalDateTime getStoredDate() {
        return storedDate;
    }
}
