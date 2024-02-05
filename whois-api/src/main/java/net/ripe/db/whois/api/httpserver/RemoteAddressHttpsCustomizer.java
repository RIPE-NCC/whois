package net.ripe.db.whois.api.httpserver;

import org.eclipse.jetty.server.Request;

/**
 * When HTTP requests via trusted source use clientIp query paramater as remote Address if set
 */
public class RemoteAddressHttpsCustomizer extends RemoteAddressCustomizer {

    public RemoteAddressHttpsCustomizer(final String trustedIpRanges) {
       super(trustedIpRanges);
    }

    @Override
    String customizeRemoteAddress(final Request request) {
        return request.getRemoteAddr();
    }
}
