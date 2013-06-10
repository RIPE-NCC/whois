package net.ripe.db.whois.common.domain;

import net.ripe.db.whois.common.rpsl.RpslObject;
import org.joda.time.LocalDateTime;

import javax.annotation.concurrent.Immutable;
import java.util.Set;

@Immutable
public class PendingUpdate {
    private final Set<String> passedAuthentications;
    private final RpslObject object;
    private final LocalDateTime storedDate;
    private final Integer id;

    public PendingUpdate(final Set<String> passedAuthentications, final RpslObject object, final LocalDateTime storedDate) {
        this(null, passedAuthentications, object, storedDate);
    }

    public PendingUpdate(final Integer id, final Set<String> passedAuthentications, final RpslObject object, final LocalDateTime storedDate) {
        this.passedAuthentications = passedAuthentications;
        this.object = object;
        this.storedDate = storedDate;
        this.id = id;
    }

    public Integer getId() {
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
