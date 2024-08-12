package net.ripe.db.whois.common.oauth.config;

import net.ripe.db.whois.common.oauth.condition.Oauth2Condition;
import net.ripe.db.whois.common.oauth.filter.DynamicOAuth2Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.reactive.function.client.WebClient;


@Configuration
@EnableWebSecurity
@Conditional(Oauth2Condition.class)
public class WebSecurityConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSecurityConfig.class);

    private final OAuth2AuthorizedClientManager oauth2AuthorizedClientManager;

    private final String keyCloackApi;

    private final String apiKeyGateway;

    WebSecurityConfig(@Value("${key.cloack.api}") final String keyCloackApi,
                      @Value("${api.key.gateway}") final String apiKeyGateway,
                      OAuth2AuthorizedClientManager oauth2AuthorizedClientManager){
        this.oauth2AuthorizedClientManager = oauth2AuthorizedClientManager;
        this.keyCloackApi = keyCloackApi;
        this.apiKeyGateway = apiKeyGateway;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeRequests(authorizeRequests ->
                        authorizeRequests.anyRequest().authenticated()
                )
                .addFilterBefore(new DynamicOAuth2Filter(keyCloackApi, oauth2AuthorizedClientManager,
                        clientRegistrationRepository()), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    // This will include the access-token that we are including in the DynamicOAuth2Filter
    public WebClient apiKeyGatewayWebClient(OAuth2AuthorizedClientManager authorizedClientManager) {
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2FilterFunction =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        oauth2FilterFunction.setDefaultOAuth2AuthorizedClient(true);
        oauth2FilterFunction.setDefaultClientRegistrationId("api-key-gateway");
        return WebClient.builder()
                .baseUrl(apiKeyGateway)
                .apply(oauth2FilterFunction.oauth2Configuration()) //WebClient Is necessary for applying out2filterFunctions
                .build();
    }

    @Bean
    public InMemoryClientRegistrationRepository clientRegistrationRepository() {
        // You may use an empty repository if the filter will manage registration dynamically
        return new InMemoryClientRegistrationRepository(); // Adjust as needed
    }

    /*private static Client createClient() {
        final JacksonJsonProvider jsonProvider = new JacksonJsonProvider();
        jsonProvider.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
        jsonProvider.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        return ClientBuilder.newBuilder()
                .property(ClientProperties.CONNECT_TIMEOUT, 10_000)
                .property(ClientProperties.READ_TIMEOUT,    60_000)
                .register(MultiPartFeature.class)
                .register(jsonProvider)
                .build();
    }*/
}
