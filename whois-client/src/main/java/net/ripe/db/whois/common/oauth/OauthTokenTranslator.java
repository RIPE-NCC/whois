package net.ripe.db.whois.common.oauth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OauthTokenTranslator {

    private final net.ripe.db.whois.common.oauth.AuthServiceClient authServiceClient;

    @Autowired
    public OauthTokenTranslator(final net.ripe.db.whois.common.oauth.AuthServiceClient authServiceClient) {
        this.authServiceClient = authServiceClient;
    }

    public List<ApiKey> translateOauthToken(final String oauthToken) {
        return authServiceClient.validateJwtToken(oauthToken);
    }
}
