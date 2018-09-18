package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SourceResolver {

    private final AuthoritativeResourceData authoritativeResourceData;
    private final CIString mainSource;
    private final CIString nonAuthSource;

    @Autowired
    public SourceResolver(final AuthoritativeResourceData authoritativeResourceData,
                          @Value("${whois.source}") final String mainSource,
                          @Value("${whois.nonauth.source}") final String nonAuthSource) {
        this.authoritativeResourceData = authoritativeResourceData;
        this.mainSource = CIString.ciString(mainSource);
        this.nonAuthSource = CIString.ciString(nonAuthSource);
    }

    public String getSource(final String type, final CIString key, final String requestedSource) {
        if (!mainSource.equals(requestedSource) && !nonAuthSource.equals(requestedSource)) {
            return requestedSource;
        }

        final ObjectType objectType = ObjectType.getByNameOrNull(type);
        if (objectType == null) {
            return mainSource.toLowerCase();
        }

        return isMainSource(objectType, key) ? mainSource.toLowerCase() : nonAuthSource.toLowerCase();
    }

    private boolean isMainSource(final ObjectType objectType, final CIString key) {
        switch (objectType) {
            case AUT_NUM:
                return authoritativeResourceData.getAuthoritativeResource().isMaintainedInRirSpace(objectType, key);
            case ROUTE:
            case ROUTE6:
                return authoritativeResourceData.getAuthoritativeResource().isRouteMaintainedInRirSpace(objectType, key);
            default:
                return true;
        }
    }
}
