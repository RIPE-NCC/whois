package net.ripe.db.mock;

import net.ripe.db.whois.common.profiles.WhoisProfile;
import net.ripe.db.whois.query.support.MockFactoryBean;
import net.ripe.db.whois.update.handler.UpdateRequestHandler;
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
public class UpdateRequestHandlerMockConfiguration {

    @Primary
    @Bean
    public MockFactoryBean<?> updateRequestHandler(){
        return new MockFactoryBean<>(UpdateRequestHandler.class);
    }
}
