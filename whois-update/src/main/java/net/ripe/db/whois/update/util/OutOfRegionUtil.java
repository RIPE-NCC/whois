package net.ripe.db.whois.update.util;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.grs.AuthoritativeResource;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static net.ripe.db.whois.common.rpsl.ObjectType.INET6NUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.INETNUM;

@Component
public class OutOfRegionUtil {

    private final AuthoritativeResourceData authoritativeResourceData;
    private final CIString source;

    @Autowired
    public OutOfRegionUtil(final AuthoritativeResourceData authoritativeResourceData,
                           @Value("${whois.source}") final String source) {
        this.authoritativeResourceData = authoritativeResourceData;
        this.source = ciString(source);
    }

    public boolean isMaintainedInRirSpace(final RpslObject rpslObject) {
        final AuthoritativeResource authoritativeResource = authoritativeResourceData.getAuthoritativeResource(this.source);
        switch (rpslObject.getType()) {
            case ROUTE:
                return authoritativeResource.isMaintainedInRirSpace(INETNUM, rpslObject.getKey());
            case ROUTE6:
                return authoritativeResource.isMaintainedInRirSpace(INET6NUM, rpslObject.getKey());
            default:
                return authoritativeResource.isMaintainedInRirSpace(rpslObject);
        }
    }

}
