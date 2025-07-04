package net.ripe.db.whois.api.elasticsearch;


import net.ripe.db.whois.common.profiles.WhoisProfile;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

@Profile({WhoisProfile.DEPLOYED})
@Primary
@Component
public class ElasticSearchInstance implements ElasticRestHighlevelClient {
    
    private static final Logger LOGGER = getLogger(ElasticSearchInstance.class);
    private final RestHighLevelClient client;

    @Autowired
    public ElasticSearchInstance(@Value("#{'${elastic.host:}'.split(',')}") final List<String> elasticHosts,
                                 @Value("${elastic.user:}") final String elasticUser,
                                 @Value("${elastic.password:}")  final String elasticPassword ) {
        this.client = getEsClient(elasticHosts, elasticUser, elasticPassword);
    }

    @Nullable
    private RestHighLevelClient getEsClient(final List<String> elasticHosts, final String elasticUser, final String elasticPassword) {
       try {
           final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
           credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(elasticUser, elasticPassword));

           return new RestHighLevelClient(RestClient.builder(asHttpHosts(elasticHosts))
                   .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)));
       }  catch (Exception e) {
           LOGGER.warn("Failed to start the ES client {}", e.getMessage());
           return null;
       }
    }

    private HttpHost[] asHttpHosts(final List<String> hosts) {
        return hosts.stream()
                .map( host -> new HttpHost(StringUtils.substringBefore(host, ":"), Integer.parseInt(StringUtils.substringAfter(host, ":")), "https"))
                .toArray(HttpHost[]::new);
    }

    @Override
    public RestHighLevelClient getClient() {
        return client;
    }
}
