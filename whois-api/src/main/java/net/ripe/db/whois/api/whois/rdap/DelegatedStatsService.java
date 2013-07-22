package net.ripe.db.whois.api.whois.rdap;


import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.grs.AuthoritativeResource;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.query.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Map;
import java.util.Set;

@Component
public class DelegatedStatsService {
    private static final Set<ObjectType> ALLOWED_OBJECTTYPES = Sets.newHashSet(ObjectType.AUT_NUM, ObjectType.INET6NUM, ObjectType.INETNUM);
    private final Map<CIString, URI> uriMap;
    private final AuthoritativeResourceData resourceData;

    @Autowired
    public DelegatedStatsService(final AuthoritativeResourceData resourceData,
                                 @Value("${rdap.redirect.afrinic}") final String redirectAfrinic,
                                 @Value("${rdap.redirect.apnic}") final String redirectApnic,
                                 @Value("${rdap.redirect.arin}") final String redirectArin,
                                 @Value("${rdap.redirect.lacnic}") final String redirectLacnic) {
        this.resourceData = resourceData;

        uriMap = Maps.newHashMap();
        uriMap.put(CIString.ciString("afrinic-grs"), URI.create(redirectAfrinic));
        uriMap.put(CIString.ciString("apnic-grs"), URI.create(redirectApnic));
        uriMap.put(CIString.ciString("arin-grs"), URI.create(redirectArin));
        uriMap.put(CIString.ciString("lacnic-grs"), URI.create(redirectLacnic));
    }

    public URI getUriForRedirect(final Query query) {
        final Optional<ObjectType> objectTypeOptional = Iterables.tryFind(query.getObjectTypes(), new Predicate<ObjectType>() {
            @Override
            public boolean apply(ObjectType input) {
                return ALLOWED_OBJECTTYPES.contains(input);
            }
        });

        for (final CIString source : uriMap.keySet()) {
            final AuthoritativeResource authoritativeResource = resourceData.getAuthoritativeResource(source);
            if (objectTypeOptional.isPresent() && authoritativeResource.isMaintainedByRir(objectTypeOptional.get(), CIString.ciString(query.getSearchValue()))) {
                return uriMap.get(source);
            }
        }

        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
}
