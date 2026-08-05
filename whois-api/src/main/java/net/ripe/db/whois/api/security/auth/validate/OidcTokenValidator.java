package net.ripe.db.whois.api.security.auth.validate;

import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

public class OidcTokenValidator extends AccessTokenValidator {

    public OidcTokenValidator(final boolean shouldUseTokenInspector,
                              final JwtDecoder jwtDecoder,
                              final OpaqueTokenIntrospector webApplicationIntrospector) {
        super(shouldUseTokenInspector, jwtDecoder, webApplicationIntrospector);
    }
}
