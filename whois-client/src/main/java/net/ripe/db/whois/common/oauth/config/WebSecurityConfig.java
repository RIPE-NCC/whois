package net.ripe.db.whois.common.oauth.config;

import jakarta.ws.rs.client.Client;
import net.ripe.db.whois.common.oauth.filter.AuthorityPrefix;
import net.ripe.db.whois.common.oauth.filter.DynamicOAuth2Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.endpoint.DefaultClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSecurityConfig.class);

    private static final DefaultClientCredentialsTokenResponseClient CLIENT_CREDENTIALS_TOKEN_RESPONSE_CLIENT =
            new DefaultClientCredentialsTokenResponseClient();

    private final OAuth2AuthorizedClientManager oauth2AuthorizedClientManager;

    private Client client;

    private final String keyCloackApi;

    WebSecurityConfig(@Value("${key.cloack.api}") final String keyCloackApi,
                      OAuth2AuthorizedClientManager oauth2AuthorizedClientManager, Client client){
        this.oauth2AuthorizedClientManager = oauth2AuthorizedClientManager;
        this.keyCloackApi = keyCloackApi;
        this.client = client;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeRequests(authorizeRequests ->
                        authorizeRequests.anyRequest().authenticated()
                )
                .addFilterBefore(new DynamicOAuth2Filter(client, keyCloackApi, oauth2AuthorizedClientManager,
                        clientRegistrationRepository()), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public InMemoryClientRegistrationRepository clientRegistrationRepository() {
        // You may use an empty repository if the filter will manage registration dynamically
        return new InMemoryClientRegistrationRepository(); // Adjust as needed
    }
}
