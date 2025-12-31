package net.ripe.db.mock;

import net.ripe.db.whois.common.profiles.WhoisProfile;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import net.ripe.db.whois.query.handler.QueryHandler;
import net.ripe.db.whois.query.pipeline.QueryChannelsRegistry;
import net.ripe.db.whois.query.support.MockFactoryBean;
import net.ripe.db.whois.query.support.SpyFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource({
        "classpath:whois.version.properties",
        "classpath:whois.properties"
})
@Profile(WhoisProfile.TEST)
public class QueryMockTestConfiguration {

    @Primary
    @Bean
    public MockFactoryBean<?> queryHandler(){
        return new MockFactoryBean<>(QueryHandler.class);
    }

    @Primary
    @Bean
    public MockFactoryBean<?> accessControlListManager(){
        return new MockFactoryBean<>(AccessControlListManager.class);
    }

    @Primary
    @Bean
    public SpyFactoryBean<?> queryChannelsRegistry(){
        return new SpyFactoryBean<>(QueryChannelsRegistry.class);
    }
}
