package net.ripe.db.whois.api;

import net.ripe.db.whois.common.profiles.WhoisProfile;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource({
        "classpath:whois.version.properties",
        "classpath:whois.properties"
})
@Profile(WhoisProfile.TEST)
@Import(WhoisApiConfiguration.class)
public class WhoisApiTestConfiguration {
}
