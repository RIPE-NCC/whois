package net.ripe.db.whois.common.configuration;

import net.ripe.db.whois.common.profiles.WhoisProfile;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
@PropertySources({
        @PropertySource(value = "classpath:whois.version.properties", ignoreResourceNotFound = true),
        @PropertySource(value = "file:${whois.config}", ignoreResourceNotFound = true),
})
@Profile({WhoisProfile.DEPLOYED, WhoisProfile.TEST})
public class WhoisRipePropertyResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(WhoisRipePropertyResolver.class);

    static {
        String hostName = System.getenv("HOSTNAME");
        if (StringUtils.isBlank(hostName)) {
            try {
                hostName = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException ignored) {
                LOGGER.debug("{}: {}", ignored.getClass().getName(), ignored.getMessage());
            }

            if (StringUtils.isBlank(hostName)) {
                LOGGER.error("Acquiring HOSTNAME failed! Try\nexport HOSTNAME=$(hostname -s)\n");
                throw new IllegalStateException("Acquiring HOSTNAME failed!");
            }
        }

        final String instanceName = StringUtils.substringBefore(hostName, ".").toUpperCase();
        System.setProperty("instance.name", instanceName);
        LOGGER.info("Instance name is {}", instanceName);
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer properties(){
        return new PropertySourcesPlaceholderConfigurer();
    }
}
