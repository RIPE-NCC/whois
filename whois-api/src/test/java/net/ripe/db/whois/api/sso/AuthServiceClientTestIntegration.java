package net.ripe.db.whois.api.sso;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.common.sso.AuthServiceClient;
import net.ripe.db.whois.common.sso.domain.HistoricalUserResponse;
import net.ripe.db.whois.common.sso.domain.ValidateTokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

@Tag("IntegrationTest")
public class AuthServiceClientTestIntegration extends AbstractIntegrationTest {

    private static final String UUID = "8ffe29be-89ef-41c8-ba7f-0e1553a623e5";
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private AuthServiceClient authServiceClient;

    @BeforeEach
    public void clearCache() {
        cacheManager.getCache("ssoUserDetails").clear();
        cacheManager.getCache("ssoHistoricalUserDetails").clear();
    }

    @Test
    public void get_user_details_response_is_cached() {

        assertThat(cacheManager.getCache("ssoUserDetails").get(UUID), is(nullValue()));

        final ValidateTokenResponse userDetails = authServiceClient.getUserDetails(UUID);

        assertThat(userDetails.response.content.email, is("test@ripe.net"));
        assertThat(cacheManager.getCache("ssoUserDetails").get(UUID), is(not(nullValue())));
    }

    @Test
    public void get_historical_user_details_response_is_cached() {

        assertThat(cacheManager.getCache("ssoHistoricalUserDetails").get(UUID), is(nullValue()));

        final HistoricalUserResponse historicalUserDetails = authServiceClient.getHistoricalUserDetails(UUID);

        assertThat(historicalUserDetails.response.results.size(), is(1));
        assertThat(cacheManager.getCache("ssoHistoricalUserDetails").get(UUID), is(not(nullValue())));
    }

}
