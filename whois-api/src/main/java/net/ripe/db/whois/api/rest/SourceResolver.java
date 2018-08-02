package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.source.SourceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static net.ripe.db.whois.common.rpsl.ObjectType.AUT_NUM;

@Component
public class SourceResolver {

    private final SourceContext sourceContext;
    private final AuthoritativeResourceData authoritativeResourceData;
    private final String nonAuthSourceName;

    @Autowired
    public SourceResolver(final SourceContext sourceContext,
                          final AuthoritativeResourceData authoritativeResourceData,
                          // TODO [SB]: we need to either use the property or the SourceContext for the nonauth source
                          @Value("${whois.nonauth.source}") String nonAuthSourceName) {
        this.sourceContext = sourceContext;
        this.authoritativeResourceData = authoritativeResourceData;
        this.nonAuthSourceName = nonAuthSourceName;
    }

    public String getSource(final String type, final CIString key) {

        final String mainSource = sourceContext.getCurrentSource().getName().toString();

        ObjectType objectType = ObjectType.getByNameOrNull(type);

        if (objectType != null) {
            switch (objectType) {
                case AUT_NUM:
                    return authoritativeResourceData.getAuthoritativeResource().isMaintainedInRirSpace(AUT_NUM, key)?
                            mainSource : nonAuthSourceName;
                case ROUTE:
                case ROUTE6:
                    return authoritativeResourceData.getAuthoritativeResource().isRouteMaintainedInRirSpace(objectType, key)?
                            mainSource : nonAuthSourceName;
                default:
                    return mainSource;
            }
        }

        return mainSource;
    }
}
