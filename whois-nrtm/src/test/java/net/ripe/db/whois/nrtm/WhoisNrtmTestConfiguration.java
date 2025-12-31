package net.ripe.db.whois.nrtm;

import net.ripe.db.whois.common.profiles.WhoisProfile;
import net.ripe.db.whois.query.support.TestPersonalObjectAccounting;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource({
        "classpath:whois.version.properties",
        "classpath:whois.properties"
})
@Profile(WhoisProfile.TEST)
@Import(WhoisNrtmConfig.class)
@ComponentScan(basePackages = "net.ripe.db.whois.query.support", includeFilters = {
        @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = TestPersonalObjectAccounting.class
        )
})
public class WhoisNrtmTestConfiguration {
}
