package net.ripe.db.whois.api.security.auth.filter;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.ripe.db.whois.api.security.auth.token.OidcAuthenticationToken;
import net.ripe.db.whois.api.security.auth.token.UnknownAuthenticationToken;
import net.ripe.db.whois.common.oauth.UnknownSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.text.ParseException;
import java.util.Objects;

import static net.ripe.db.whois.api.security.auth.filter.OptionalOAuthFilter.getBearerToken;
import static net.ripe.db.whois.common.oauth.OAuthUtils.OAUTH_CUSTOM_AZP_PARAM;

public class OptionalOidcAuthFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(OptionalOidcAuthFilter.class);

    private final AuthenticationManager authenticationManager;

    private final boolean isOidcEnabled;

    private final String oidcClientId;

    public OptionalOidcAuthFilter(final AuthenticationManager authenticationManager,
                                  final boolean isOidcEnabled,
                                  final String oidcClientId) {
        this.authenticationManager = authenticationManager;
        this.isOidcEnabled = isOidcEnabled;
        this.oidcClientId = oidcClientId;
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain) throws IOException, ServletException {
        try {
            final String bearerToken = getBearerToken(request);
            if (SecurityContextHolder.getContext().getAuthentication() != null || StringUtils.isEmpty(bearerToken)) {
                return;
            }

            if (!canProceed(bearerToken)){
                return;
            }

            if (!isOidcEnabled){
                LOGGER.warn("OIDC is not enabled");
                return;
            }

            final Authentication authentication = new OidcAuthenticationToken(bearerToken);

            final Authentication authResult = authenticationManager.authenticate(authentication);
            SecurityContextHolder.getContext().setAuthentication(authResult);
        } catch (ParseException e) {
            final Authentication authResult = new UnknownAuthenticationToken(new UnknownSession.Builder().errorStatus("Invalid Bearer Token").build());
            SecurityContextHolder.getContext().setAuthentication(authResult);
        } catch (Exception e) {
            LOGGER.error(e.getClass().getName(), e);
        } finally {
            filterChain.doFilter(request, response);
        }
    }

    private boolean canProceed(final String bearerToken) throws ParseException {
        final JWTClaimsSet jwt = JWTParser.parse(bearerToken).getJWTClaimsSet();
        return isWebClientAzp(jwt.getClaimAsString(OAUTH_CUSTOM_AZP_PARAM));
    }

    private boolean isWebClientAzp(final String azpValue) {
        return Objects.equals(azpValue, oidcClientId);
    }
}
