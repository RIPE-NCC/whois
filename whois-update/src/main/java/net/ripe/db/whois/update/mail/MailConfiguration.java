package net.ripe.db.whois.update.mail;

import jakarta.annotation.PostConstruct;
import jakarta.mail.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.stream.StreamSupport;

@Component
public class MailConfiguration {

    @Value("${mail.from}")
    private String from;

    @Autowired
    private Environment evironment;
    @Autowired
    private PropertiesFactoryBean javaMailProperties;

    private Session session;

    public MailConfiguration() {}

    public MailConfiguration(final String from) {
        this.from = from;
    }

    @PostConstruct
    public void initSession() {
        final Properties properties;
        try {
            properties = new Properties(javaMailProperties.getObject());
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't read JavaMailProperties");
        }

        addSmtpProperties(properties);

        session = Session.getInstance(properties);
    }

    private void addSmtpProperties(Properties properties) {
        final MutablePropertySources sources = ((StandardEnvironment) evironment).getPropertySources();
        StreamSupport.stream(sources.spliterator(), false)
                .filter(ps -> ps instanceof EnumerablePropertySource)
                .map(ps -> ((EnumerablePropertySource) ps).getPropertyNames())
                .flatMap(Arrays::stream)
                .distinct()
                .sorted()
                .forEach(prop -> {
                            if (prop.startsWith("mail.smtp")){
                                properties.put(prop, evironment.getProperty(prop));
                            }
                        });
    }

    public Session getSession() {
        return session;
    }

    public String getFrom() {
        return from;
    }
}
