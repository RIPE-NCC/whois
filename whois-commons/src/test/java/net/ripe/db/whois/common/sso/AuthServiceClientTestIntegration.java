package net.ripe.db.whois.common.sso;

import net.ripe.db.whois.common.sso.domain.HistoricalUserResponse;
import net.ripe.db.whois.common.sso.domain.ValidateTokenResponse;
import net.ripe.db.whois.common.support.AbstractDaoIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("IntegrationTest")
public class AuthServiceClientTestIntegration extends AbstractDaoIntegrationTest {

    private static final String UUID = "8ffe29be-89ef-41c8-ba7f-0e1553a623e5";

    private static final String TOKEN = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia";

    private static final String USER_EMAIL = "test@ripe.net";
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private AuthServiceClient authServiceClient;

    @BeforeEach
    public void clearCache() {
        cacheManager.getCache("ssoValidateToken").clear();
        cacheManager.getCache("ssoUuid").clear();
        cacheManager.getCache("ssoUserDetails").clear();
        cacheManager.getCache("ssoHistoricalUserDetails").clear();
    }

    @Test
    public void get_validate_token_response_is_cached() {
        assertThat(cacheManager.getCache("ssoValidateToken").get(TOKEN), is(nullValue()));

        final ValidateTokenResponse userDetails = authServiceClient.validateToken(TOKEN);

        assertThat(userDetails.response.content.email, is(USER_EMAIL));
        assertThat(((ValidateTokenResponse)cacheManager.getCache("ssoValidateToken").get(TOKEN).get()).response.content.email, is(USER_EMAIL));
    }

    @Test
    public void get_sso_uuid_response_is_cached() {
        assertThat(cacheManager.getCache("ssoUuid").get(USER_EMAIL), is(nullValue()));

        final String userUuid = authServiceClient.getUuid(USER_EMAIL);

        assertThat(userUuid, is(UUID));
        assertThat(cacheManager.getCache("ssoUuid").get(USER_EMAIL).get().toString(), is(UUID));
    }

    @Test
    public void get_user_details_response_is_cached() {
        assertThat(cacheManager.getCache("ssoUserDetails").get(UUID), is(nullValue()));

        final ValidateTokenResponse userDetails = authServiceClient.getUserDetails(UUID);

        assertThat(userDetails.response.content.email, is(USER_EMAIL));
        assertThat(((ValidateTokenResponse)cacheManager.getCache("ssoUserDetails").get(UUID).get()).response.content.email
                , is(USER_EMAIL));
    }

    @Test
    public void get_historical_user_details_response_is_cached() {
        assertThat(cacheManager.getCache("ssoHistoricalUserDetails").get(UUID), is(nullValue()));

        final HistoricalUserResponse historicalUserDetails = authServiceClient.getHistoricalUserDetails(UUID);

        assertThat(historicalUserDetails.response.results.size(), is(1));
        assertThat(((HistoricalUserResponse)cacheManager.getCache("ssoHistoricalUserDetails").get(UUID).get())
                        .response.results.get(0).action,
                is("EMAIL_CHANGE"));
    }

    @Test
    public void get_historical_event_date_time() {
        final HistoricalUserResponse historicalUserDetails = authServiceClient.getHistoricalUserDetails(UUID);

        assertThat(historicalUserDetails.response.results.get(0).eventDateTime, is(LocalDateTime.of(2015, 5, 8, 12, 32, 1, 275_379_000)));
    }


    // Errors
    @Test
    public void get_validate_token_response_null_input_then_error() {
        assertThat(cacheManager.getCache("ssoValidateToken").get(TOKEN), is(nullValue()));

        final AuthServiceClientException authServiceClientException = assertThrows(AuthServiceClientException.class, () -> {
            authServiceClient.validateToken(null);
        });

        assertThat(authServiceClientException.getMessage(), is("No Token."));
        assertThat(cacheManager.getCache("ssoValidateToken").get(TOKEN), is(nullValue()));
    }

    @Test
    public void get_sso_uuid_response_null_input_then_error() {
        assertThat(cacheManager.getCache("ssoUuid").get(USER_EMAIL), is(nullValue()));

        final AuthServiceClientException authServiceClientException = assertThrows(AuthServiceClientException.class, () -> {
            authServiceClient.getUuid(null);
        });

        assertThat(authServiceClientException.getMessage(), is("No username."));
        assertThat(cacheManager.getCache("ssoUuid").get(USER_EMAIL), is(nullValue()));
    }

    @Test
    public void get_user_details_response_null_input_then_error() {
        assertThat(cacheManager.getCache("ssoUserDetails").get(UUID), is(nullValue()));

        final AuthServiceClientException authServiceClientException = assertThrows(AuthServiceClientException.class, () -> {
            authServiceClient.getUserDetails(null);
        });

        assertThat(authServiceClientException.getMessage(), is("No UUID."));
        assertThat(cacheManager.getCache("ssoUserDetails").get(UUID), is(nullValue()));
    }

    @Test
    public void get_historical_user_details_response_null_input_then_error() {
        assertThat(cacheManager.getCache("ssoHistoricalUserDetails").get(UUID), is(nullValue()));

        final AuthServiceClientException authServiceClientException = assertThrows(AuthServiceClientException.class, () -> {
            authServiceClient.getHistoricalUserDetails(null);
        });

        assertThat(authServiceClientException.getMessage(), is("No UUID."));
        assertThat(cacheManager.getCache("ssoHistoricalUserDetails").get(UUID), is(nullValue()));
    }

}
