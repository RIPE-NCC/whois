package net.ripe.db.whois.api.security.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.HttpHeaders;
import net.ripe.db.whois.common.oauth.OAuthUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;

public class OptionalApiKeyAuthFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(OptionalApiKeyAuthFilter.class);

    private final AuthenticationManager authenticationManager;

    public OptionalApiKeyAuthFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        try {
            final String[] credentials = getCredentials(authHeader);
            if (credentials == null) {
                return;
            }

            if(!isAPIKeyRequest(request)) {
                return;
            }

            final UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(credentials[0], credentials[1]);
            authRequest.setDetails(authHeader);

            final Authentication authResult = authenticationManager.authenticate(authRequest);
            SecurityContextHolder.getContext().setAuthentication(authResult);
        } catch (Exception e) {
            LOGGER.error(e.getClass().getName(), e);
        } finally {
            filterChain.doFilter(request, response);
        }
    }

    private String[] getCredentials(final String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            return null;
        }

        final String credentials = new String(Base64.getDecoder().decode(authHeader.substring("Basic ".length()).trim()));
        return credentials.split(":", 2);
    }


    private boolean isAPIKeyRequest(final HttpServletRequest httpRequest) {
        //TODO: Remove this logic when basic auth support is deprecated
        return OAuthUtils.isAPIKeyRequest(httpRequest.getHeader(HttpHeaders.AUTHORIZATION));
    }
}
