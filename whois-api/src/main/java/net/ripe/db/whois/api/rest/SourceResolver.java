package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static net.ripe.db.whois.common.rpsl.ObjectType.AUT_NUM;

@Component
public class SourceResolver {

    private final AuthoritativeResourceData authoritativeResourceData;
    private final String mainSourceNameString;
    private final String nonAuthSourceName;

    @Autowired
    public SourceResolver(final AuthoritativeResourceData authoritativeResourceData,
                          @Value("${whois.source}") final String mainSourceNameString,
                          // TODO [SB]: we need to either use the property or the SourceContext for the nonauth source
                          @Value("${whois.nonauth.source}") final String nonAuthSourceName) {
        this.authoritativeResourceData = authoritativeResourceData;
        this.mainSourceNameString = mainSourceNameString.toLowerCase();
        this.nonAuthSourceName = nonAuthSourceName.toLowerCase();
    }

    public String getSource(final String type, final CIString key, final String requestedSource) {

        if (!requestedSource.toLowerCase().equals(mainSourceNameString) && !requestedSource.toLowerCase().equals(nonAuthSourceName)) {
            return requestedSource;
        }

        ObjectType objectType = ObjectType.getByNameOrNull(type);

        if (objectType != null) {
            switch (objectType) {
                case AUT_NUM:
                    return authoritativeResourceData.getAuthoritativeResource().isMaintainedInRirSpace(AUT_NUM, key)?
                            mainSourceNameString : nonAuthSourceName;
                case ROUTE:
                case ROUTE6:
                    return authoritativeResourceData.getAuthoritativeResource().isRouteMaintainedInRirSpace(objectType, key)?
                            mainSourceNameString : nonAuthSourceName;
                default:
                    return mainSourceNameString;
            }
        }

        return mainSourceNameString;
    }
}
