package net.ripe.db.whois.api.security;

import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.ripe.db.whois.api.security.auth.filter.OptionalApiKeyAuthFilter;
import net.ripe.db.whois.api.security.auth.filter.OptionalOAuthFilter;
import net.ripe.db.whois.api.security.auth.filter.OptionalOidcAuthFilter;
import net.ripe.db.whois.api.security.auth.provider.ApiKeyAuthProvider;
import net.ripe.db.whois.api.security.auth.provider.DefaultOauthAuthProvider;
import net.ripe.db.whois.api.security.auth.provider.OidcAuthProvider;
import net.ripe.db.whois.api.security.auth.validate.DefaultTokenValidator;
import net.ripe.db.whois.api.security.auth.validate.OidcTokenValidator;
import net.ripe.db.whois.common.oauth.ApiKeyAuthServiceClient;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.SpringOpaqueTokenIntrospector;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.FirewalledRequest;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static net.ripe.db.whois.common.oauth.OAuthUtils.OAUTH_CUSTOM_AZP_PARAM;
import static net.ripe.db.whois.common.oauth.OAuthUtils.OAUTH_CUSTOM_EMAIL_PARAM;
import static net.ripe.db.whois.common.oauth.OAuthUtils.OAUTH_CUSTOM_UUID_PARAM;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final int CLIENT_CONNECT_TIMEOUT = 10_000;
    private static final int CLIENT_READ_TIMEOUT = 60_000;

    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity httpSecurity,
                                                   final AuthenticationManager authenticationManager,
                                                   @Value("${oidc.auth.enable:false}") final boolean isOidcEnabled,
                                                   @Value("${oidc.session.client.id:}") final String oidcClientId) {

        return httpSecurity
                .anonymous(AbstractHttpConfigurer::disable) //Avoid AnonymousAuthenticationToken default behavior in case no filter match
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/**").permitAll()
                        .anyRequest().permitAll())
                .addFilterBefore(
                        new OptionalApiKeyAuthFilter(authenticationManager),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(
                        new OptionalOidcAuthFilter(authenticationManager, isOidcEnabled, oidcClientId),
                        OptionalApiKeyAuthFilter.class)
                .addFilterAfter(
                        new OptionalOAuthFilter(authenticationManager),
                        OptionalOidcAuthFilter.class).build();
    }

    @Bean
    public AuthenticationManager authenticationManager(final List<AuthenticationProvider> providers) {
        return new ProviderManager(providers);
    }

    @Bean
    public @NonNull NimbusJwtDecoder jwtDecoder(final OidcConfigurationProvider oidcProvider,
                                                @Value("${keycloak.idp.client:}") final String whoisKeycloakId) {
        final OIDCProviderMetadata metadata = oidcProvider.getMetadataOrInitOidcConfiguration();
        if (metadata == null) {
            throw new IllegalStateException("OIDC metadata not initialized");
        }

        final String issuer = metadata.getIssuer().toString();
        final String jwksUri = metadata.getJWKSetURI().toString();

        // --- Base decoder (signature + exp + nbf + alg handling via JWKS) ---
        final NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwksUri).build();

        // --- Combine all validators ---
        final OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
                getRequiredClaimsValidator(),
                JwtValidators.createDefaultWithIssuer(issuer),
                getIatValidator(),
                getJwtAudienceValidator(whoisKeycloakId)
        );
        decoder.setJwtValidator(validator);
        return decoder;
    }

    @Bean
    public DefaultTokenValidator defaultTokenValidator(@Value("${apikey.max.scope:10}") final int maxScopes,
                                                 @Value("${oauth.token.inspection:false}") final boolean shouldUseTokenInspector,
                                                 @Qualifier("keycloakIntrospector") final OpaqueTokenIntrospector tokenIntrospector,
                                                 final NimbusJwtDecoder jwtDecoder) {
        return new DefaultTokenValidator(
                maxScopes,
                shouldUseTokenInspector,
                jwtDecoder,
                tokenIntrospector
        );
    }

    @Bean
    public OidcTokenValidator oidcTokenValidator(@Value("${oauth.token.inspection:false}") final boolean shouldUseTokenInspector,
                                                 @Qualifier("oidcIntrospector") final OpaqueTokenIntrospector tokenIntrospector,
                                                 final NimbusJwtDecoder jwtDecoder) {
        return new OidcTokenValidator(
                shouldUseTokenInspector,
                jwtDecoder,
                tokenIntrospector
        );
    }

    // Different providers for different authentication methods
    @Bean
    public ApiKeyAuthProvider apiKeyAuthProvider(final DefaultTokenValidator defaultTokenValidator,
                                                 final ApiKeyAuthServiceClient apiKeyAuthServiceClient) {
        return new ApiKeyAuthProvider(
                defaultTokenValidator,
                apiKeyAuthServiceClient
        );
    }

    @Bean
    public DefaultOauthAuthProvider oauthAuthProvider(final DefaultTokenValidator defaultTokenValidator) {
        return new DefaultOauthAuthProvider(defaultTokenValidator);
    }


    @Bean
    public OidcAuthProvider oidcAuthProvider(final OidcTokenValidator oidcTokenValidator) {
        return new OidcAuthProvider(oidcTokenValidator);
    }

    // Spring Security firewall restrictions are disabled to avoid conflicting URI validation.
    @Bean
    public HttpFirewall permissiveFirewall() {
        return new HttpFirewall() {

            @Override
            public FirewalledRequest getFirewalledRequest(HttpServletRequest request) throws RequestRejectedException {
                return new FirewalledRequest(request) {
                    @Override
                    public void reset() {
                    }
                };
            }

            @Override
            public HttpServletResponse getFirewalledResponse(HttpServletResponse response) {
                return response;
            }
        };
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer(HttpFirewall httpFirewall) {
        return web -> web.httpFirewall(httpFirewall);
    }

    @Bean
    public OpaqueTokenIntrospector oidcIntrospector(final OidcConfigurationProvider oidcProvider,
                                                              @Value("${oidc.session.client.id:}") final String oidcClientId,
                                                              @Value("${oidc.session.client.password:}") final String oidcClientPassword) {
        return getTokenIntrospector(oidcProvider, oidcClientId, oidcClientPassword);
    }

    @Bean
    public OpaqueTokenIntrospector keycloakIntrospector(final OidcConfigurationProvider oidcProvider,
                                                        @Value("${keycloak.idp.client:}") final String whoisKeycloakId,
                                                        @Value("${keycloak.idp.password:}")  final String keycloakPassword) {
        return getTokenIntrospector(oidcProvider, whoisKeycloakId, keycloakPassword);
    }

    private static OpaqueTokenIntrospector getTokenIntrospector(final OidcConfigurationProvider oidcProvider,
                                                                          final String clientId,
                                                                          final String clientPassword) {
        final OIDCProviderMetadata metadata = oidcProvider.getMetadataOrInitOidcConfiguration();
        if (metadata == null) {
            throw new IllegalStateException("OIDC metadata not initialized");
        }

        final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(CLIENT_CONNECT_TIMEOUT);
        requestFactory.setReadTimeout(CLIENT_READ_TIMEOUT);

        final RestTemplate restTemplate = new RestTemplate(requestFactory);
        restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(clientId, clientPassword));

        final String tokenInspectionEndpoint = metadata.getIntrospectionEndpointURI().toString();

        return new SpringOpaqueTokenIntrospector(
                tokenInspectionEndpoint,
                restTemplate);
    }

    private OAuth2TokenValidator<Jwt> getIatValidator() {
        // --- IAT validation (no future-issued tokens) ---
        return jwt -> {
            Instant iat = jwt.getIssuedAt();

            if (iat == null || iat.isAfter(Instant.now())) {
                return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_token", "Token issued in future", null));
            }

            return OAuth2TokenValidatorResult.success();
        };
    }

    private OAuth2TokenValidator<Jwt> getRequiredClaimsValidator() {
        return jwt -> {
            final List<String> missing = new ArrayList<>();

            if (jwt.getClaim(OAUTH_CUSTOM_UUID_PARAM) == null) missing.add(OAUTH_CUSTOM_UUID_PARAM);
            if (jwt.getClaim(OAUTH_CUSTOM_EMAIL_PARAM) == null) missing.add(OAUTH_CUSTOM_EMAIL_PARAM);
            if (jwt.getClaim(OAUTH_CUSTOM_AZP_PARAM) == null) missing.add(OAUTH_CUSTOM_AZP_PARAM);

            if (!missing.isEmpty()) {
                return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error(
                                "invalid_token",
                                "Missing claims: " + String.join(", ", missing),
                                null));
            }

            return OAuth2TokenValidatorResult.success();
        };
    }

    private static @NonNull OAuth2TokenValidator<Jwt> getJwtAudienceValidator(final String clientId) {
        // Destination of the token. In our case it is always whois
        return token -> {
            final List<String> audiences = token.getAudience(); // maps the "aud" claim

            if (audiences != null && audiences.contains(clientId)) {
                return OAuth2TokenValidatorResult.success();
            }

            OAuth2Error error = new OAuth2Error(
                    "invalid_token",
                    "Session cannot be used because it was created for a different application or environment",
                    null
            );
            return OAuth2TokenValidatorResult.failure(error);
        };
    }
}
