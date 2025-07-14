package net.ripe.db.whois.nrtm;


import net.ripe.db.whois.common.WhoisCommonConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@Import(WhoisCommonConfig.class)
@ComponentScan(basePackages={"net.ripe.db.whois.nrtm", "net.ripe.db.whois.query.acl", "net.ripe.db.whois.query.dao"})
public class WhoisNrtmConfig {

    @Bean("clientSynchronisationScheduler")
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(32);
        return scheduler;
    }
}
