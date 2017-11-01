package net.ripe.db.whois.api.rest.search;

import net.ripe.db.whois.api.rest.domain.AbuseContact;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.planner.AbuseCFinder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

@Component
public class AbuseContactSearch {

    private final AbuseCFinder abuseCFinder;

    @Autowired
    public AbuseContactSearch(final AbuseCFinder abuseCFinder) {
        this.abuseCFinder = abuseCFinder;
    }

    /**
     * Find the abuse contact (abuse-c nic-hdl and e-mail address) for a given resource (inetnum, inet6num or aut-num).
     */
    @Nullable
    public AbuseContact findAbuseContact(final RpslObject rpslObject) {
        final RpslObject role = abuseCFinder.getAbuseContactRole(rpslObject);
        if (role == null) {
            return null;
        }

        return new AbuseContact(role.getKey(), role.getValueOrNullForAttribute(AttributeType.ABUSE_MAILBOX));
    }
}
