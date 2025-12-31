package net.ripe.db.whois.db;

import net.ripe.db.nrtm4.Nrtmv4Configuration;
import net.ripe.db.whois.nrtm.WhoisNrtmConfig;
import net.ripe.db.whois.rdap.RdapConfig;
import net.ripe.db.whois.scheduler.SchedulerConfiguration;
import net.ripe.db.whois.smtp.SmtpConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.context.annotation.Import;

import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;

@Configuration
@ComponentScan(basePackages= "net.ripe.db.whois.db")
@Import({WhoisNrtmConfig.class, Nrtmv4Configuration.class, RdapConfig.class, SchedulerConfiguration.class, SmtpConfiguration.class})
@EnableMBeanExport
public class WhoisConfiguration {

    @Bean
    public MBeanServer mbeanServer() {
        // Used for reuse JVM’s platform MBeanServer
        return ManagementFactory.getPlatformMBeanServer();
    }

}
