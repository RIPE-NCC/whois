package net.ripe.db.whois.common;

import net.ripe.db.whois.common.profiles.WhoisProfile;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@PropertySources({
        @PropertySource(value = "classpath:version.properties", ignoreResourceNotFound = true),
        @PropertySource(value = "file:${whois.config}", ignoreResourceNotFound = true),
})
@Profile({WhoisProfile.RIPE_DEPLOYED, WhoisProfile.TEST})
public class WhoisRipePropertyResolver {

    @Bean
    public static PropertySourcesPlaceholderConfigurer properties(){
        return new PropertySourcesPlaceholderConfigurer();
    }
}