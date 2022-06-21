package net.ripe.db.whois.common.sso;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SsoTokenTranslator {

    private final AuthServiceClient authServiceClient;

    @Autowired
    public SsoTokenTranslator(final AuthServiceClient authServiceClient) {
        this.authServiceClient = authServiceClient;
    }

    public UserSession translateSsoToken(final String ssoToken) {
        final UserSession userSession = authServiceClient.getUserSession(ssoToken);
        return userSession;
    }
}
