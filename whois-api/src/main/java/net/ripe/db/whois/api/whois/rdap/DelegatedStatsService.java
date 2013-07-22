package net.ripe.db.whois.api.whois.rdap;


import com.google.common.collect.Maps;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.IpInterval;
import net.ripe.db.whois.common.domain.Ipv4Resource;
import net.ripe.db.whois.common.grs.AuthoritativeResource;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.Map;

@Component
public class DelegatedStatsService {
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

    @Nullable
    public URI getUriForRedirect(final String searchValue) {
        final ObjectType objectType = getObjectType(searchValue);
        for (final CIString source : uriMap.keySet()) {
            final AuthoritativeResource authoritativeResource = resourceData.getAuthoritativeResource(source);
            if (objectType != null && authoritativeResource.isMaintainedByRir(objectType, CIString.ciString(searchValue))) {
                return uriMap.get(source);
            }
        }

        return null;
    }

    private ObjectType getObjectType(final String searchValue) {
        if (searchValue.startsWith("AS")) {
            return ObjectType.AUT_NUM;
        } else {
            IpInterval<?> ipInterval;
            try {
                ipInterval = IpInterval.parse(searchValue);
            } catch (final IllegalArgumentException e) {
                return null;
            }

            if (ipInterval instanceof Ipv4Resource) {
                return ObjectType.INETNUM;
            } else {
                return ObjectType.INET6NUM;
            }
        }
    }
}
