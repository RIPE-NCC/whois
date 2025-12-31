package net.ripe.db.mock;

import net.ripe.db.whois.common.iptree.IpTreeCacheManager;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import net.ripe.db.whois.query.dao.IpAccessControlListDao;
import net.ripe.db.whois.query.support.MockFactoryBean;
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
public class SchedulerTestMockConfiguration {

    @Primary
    @Bean
    public MockFactoryBean<?> ipAccessControlListDao(){
        return new MockFactoryBean<>(IpAccessControlListDao.class);
    }

    @Primary
    @Bean
    public MockFactoryBean<?> ipTreeCacheManager(){
        return new MockFactoryBean<>(IpTreeCacheManager.class);
    }
}
