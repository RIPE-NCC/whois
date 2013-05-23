package net.ripe.db.whois.common.grs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AuthoritativeResourceDataUnknown implements AuthoritativeResourceData {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthoritativeResourceDataUnknown.class);

    private final AuthoritativeResource authoritativeResource;

    AuthoritativeResourceDataUnknown() {
        this.authoritativeResource = AuthoritativeResource.unknown(LOGGER);
    }

    @Override
    public AuthoritativeResource getAuthoritativeResource() {
        return authoritativeResource;
    }
}