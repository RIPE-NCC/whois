package net.ripe.db.whois.scheduler;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;


import javax.sql.DataSource;
import java.time.Duration;
import java.time.ZoneId;
import java.util.TimeZone;
import java.util.concurrent.Executor;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor="23h", defaultLockAtLeastFor = "1h")
@ImportResource(value = "classpath:applicationContext-api.xml")
@ComponentScan(basePackages="net.ripe.db.whois.scheduler")
public class SchedulerConfig {

    @Bean("scheduler")
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        return scheduler;
    }

    @Bean
    public LockProvider lockProvider(@Qualifier("internalsDataSource") DataSource dataSource) {
        return new JdbcTemplateLockProvider(
                JdbcTemplateLockProvider.Configuration.builder()
                        .usingDbTime()
                        .withJdbcTemplate(new JdbcTemplate(dataSource))
                .build()
        );
    }


}
