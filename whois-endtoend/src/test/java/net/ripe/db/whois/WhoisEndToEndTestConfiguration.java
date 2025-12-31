package net.ripe.db.whois;

import net.ripe.db.whois.common.profiles.WhoisProfile;
import net.ripe.db.whois.scheduler.SchedulerConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@PropertySource({
        "classpath:whois.version.properties",
        "classpath:whois.properties"
})
@Profile(WhoisProfile.TEST)
@ComponentScan(basePackages = "net.ripe.db.whois")
public class WhoisEndToEndTestConfiguration {

    @Bean
    public SchedulerConfiguration schedulerConfig() {
        return new SchedulerConfiguration();
    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("clientSynchronisationScheduler");
        scheduler.initialize();
        return scheduler;
    }
}
