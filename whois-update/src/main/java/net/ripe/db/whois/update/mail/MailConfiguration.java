package net.ripe.db.whois.update.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.mail.Session;
import java.io.IOException;
import java.util.Properties;

@Component
public class MailConfiguration {

    @Value("${mail.smtp.host:localhost}")
    private String smtpHost;

    @Value("${mail.smtp.port:25}")
    private String smtpPort;

    @Value("${mail.from}")
    private String from;

    @Value("${mail.smtp.debug:false}")
    private boolean debug;

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

        properties.put("mail.smtp.host", smtpHost);
        properties.put("mail.smtp.port", smtpPort);

        session = Session.getInstance(properties);
        session.setDebug(debug);
    }

    public Session getSession() {
        return session;
    }

    public String getFrom() {
        return from;
    }
}
