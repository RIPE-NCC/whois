package net.ripe.db.whois.api.whois.rdap;


import com.google.common.collect.Maps;
import net.ripe.db.whois.common.domain.CIString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.Map;

@Component
public class DelegatedStatsService {
    private final Map<CIString, URI> uriMap;
    private final DelegatedStatsDao deletagedStatsDao;

    @Autowired
    public DelegatedStatsService(final DelegatedStatsDao deletagedStatsDao,
                                 @Value("${rdap.redirect.afrinic}") final String redirectAfrinic,
                                 @Value("${rdap.redirect.apnic}") final String redirectApnic,
                                 @Value("${rdap.redirect.arin}") final String redirectArin,
                                 @Value("${rdap.redirect.lacnic}") final String redirectLacnic) {
        this.deletagedStatsDao = deletagedStatsDao;

        uriMap = Maps.newHashMap();
        uriMap.put(CIString.ciString("afrinic"), URI.create(redirectAfrinic));
        uriMap.put(CIString.ciString("apnic"), URI.create(redirectApnic));
        uriMap.put(CIString.ciString("arin"), URI.create(redirectArin));
        uriMap.put(CIString.ciString("lacnic"), URI.create(redirectLacnic));
    }

    @Nullable
    public URI getUriForRedirect(final String searchValue) {
        final CIString sourceResult = deletagedStatsDao.findSourceForResource(searchValue);
        if (sourceResult != null) {
            return uriMap.get(sourceResult);
        }

        return null;
    }
}
