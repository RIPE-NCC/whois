package net.ripe.db.whois.update;

import net.ripe.db.whois.common.configuration.WhoisCommonConfiguration;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import net.ripe.db.whois.update.mail.CustomJavaMailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.io.IOException;
import java.util.Properties;

@Configuration
@ComponentScan(basePackages= "net.ripe.db.whois.update")
public class WhoisUpdateConfiguration {

    @Bean
    public WhoisCommonConfiguration whoisCommonConfig(){
        return new WhoisCommonConfiguration();
    }

    //Ref. https://javaee.github.io/javamail/docs/api/com/sun/mail/smtp/package-summary.html
    @Bean
    public PropertiesFactoryBean javaMailProperties(@Value("${mail.smtp.from:}") final String from,
                                                    @Value("${mail.smtp.dsn.notify:FAILURE}") String notify,
                                                    @Value("${mail.smtp.dsn.ret:HDRS}") String headersRequired,
                                                    @Value("${mail.debug:false}") boolean mailDebug,
                                                    @Value("${mail.smtp.debug:false}") boolean smtpDebug) {

        final PropertiesFactoryBean factory = new PropertiesFactoryBean();

        final Properties props = new Properties();
        props.put("mail.smtp.from", from); //Mail from envelope. Email that will receive the responses
        props.put("mail.smtp.dsn.notify", notify); //Receive the failure messages
        props.put("mail.smtp.dsn.ret", headersRequired); //Get required headers in the incoming failure message
        props.put("mail.debug", mailDebug); // Enable debug for mail
        props.put("mail.smtp.debug", smtpDebug); // Enable debug mode for smtp debug
        props.put("mail.smtp.connectiontimeout", 10000); // Socket connection timeout value in milliseconds. Default is infinite timeout.
        props.put("mail.smtp.timeout", 10000); // Socket read timeout value in milliseconds. Default is infinite timeout.
        props.put("mail.smtp.writetimeout", 10000); // Socket write timeout value in milliseconds. The overhead of using this timeout is one thread per connection. Default is infinite timeout.

        factory.setProperties(props);
        return factory;
    }

    @Profile(WhoisProfile.DEPLOYED)
    @Bean
    @Primary
    public CustomJavaMailSender mailSender(@Value("${mail.smtp.host}") final String smtpHost,
                                           @Value("${mail.smtp.port:25}") final int smtpPort,
                                           final PropertiesFactoryBean javaMailProperties) throws IOException {
        final CustomJavaMailSender customJavaMailSender = new CustomJavaMailSender();
        customJavaMailSender.setHost(smtpHost);
        customJavaMailSender.setPort(smtpPort);
        customJavaMailSender.setJavaMailProperties(new Properties(javaMailProperties.getObject()));

        return customJavaMailSender;
    }
}
