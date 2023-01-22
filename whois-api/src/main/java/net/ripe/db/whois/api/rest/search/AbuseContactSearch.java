package net.ripe.db.whois.api.rest.search;

import net.ripe.db.whois.api.rest.domain.AbuseContact;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.planner.AbuseCFinder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.Nullable;

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
        return abuseCFinder.getAbuseContact(rpslObject)
            .map(abuseContact -> new AbuseContact(abuseContact.getNicHandle(), abuseContact.getAbuseMailbox(), abuseContact.isSuspect(), abuseContact.getOrgId()))
            .orElse(null);
    }
}
