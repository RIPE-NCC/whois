package net.ripe.db.whois.api.security.auth.filter;

import com.google.common.net.HttpHeaders;
import com.nimbusds.oauth2.sdk.token.AccessTokenType;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.ripe.db.whois.api.security.auth.token.OauthAuthenticationToken;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Nullable;
import java.io.IOException;


public class OptionalOAuthFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(OptionalOAuthFilter.class);

    private final AuthenticationManager authenticationManager;

    public OptionalOAuthFilter(final AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain) throws IOException, ServletException {
        try {
            final String bearerToken = getBearerToken(request);
            final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null || StringUtils.isEmpty(bearerToken)) {
                return;
            }

            final Authentication oauthAuthenticationToken = new OauthAuthenticationToken(bearerToken);

            final Authentication authResult = authenticationManager.authenticate(oauthAuthenticationToken);
            SecurityContextHolder.getContext().setAuthentication(authResult);
        }  catch (Exception e) {
            LOGGER.error(e.getClass().getName(), e);
        } finally {
            filterChain.doFilter(request, response);
        }
    }

    @Nullable
    public static String getBearerToken(final HttpServletRequest request) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if(StringUtils.isEmpty(authHeader) || !authHeader.startsWith(AccessTokenType.BEARER.toString())) {
            return null;
        }

        try {
            return BearerAccessToken.parse(authHeader).getValue();
        } catch (Exception e) {
            LOGGER.debug("Failed to parse bearer token, due to {}: {}", e.getClass().getName(), e.getMessage());
            return null;
        }
    }
}
