package net.ripe.db.whois.smtp;

import net.ripe.db.whois.api.mail.dao.MailMessageDaoJdbc;
import net.ripe.db.whois.common.TestDateTimeProvider;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

import javax.sql.DataSource;

@Configuration
@PropertySource({
        "classpath:whois.version.properties",
        "classpath:whois.properties"
})
@Profile(WhoisProfile.TEST)
@ComponentScan(basePackages = "net.ripe.db.whois.smtp,net.ripe.db.whois.common")
public class WhoisSmtpTestConfiguration {

    @Bean
    public MailMessageDaoJdbc mailMessageDao(@Qualifier("mailupdatesDataSource") final DataSource mailupdatesDataSource,
                                             @Qualifier("testDateTimeProvider") final TestDateTimeProvider testDateTimeProvider) {
        return new MailMessageDaoJdbc(mailupdatesDataSource, testDateTimeProvider);
    }
}
