package net.ripe.db.whois.update.mail;

import jakarta.annotation.PostConstruct;
import jakarta.mail.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Properties;

@Component
public class MailConfiguration {

    @Value("${mail.smtp.host:localhost}")
    private String smtpHost;
    @Value("${mail.smtp.port:25}")
    private String smtpPort;
    @Value("${mail.smtp.from:}")
    private String smtpFrom;
    @Value("${mail.from}")
    private String from;
    @Value("${mail.smtp.debug:false}")
    private boolean debug;
    @Value("${mail.smtp.enabled:false}")
    private boolean outgoingMailEnabled;
    @Value("${mail.smtp.retrySending:true}")
    private boolean retrySending;


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

    public boolean isEnabled() {
        return outgoingMailEnabled;
    }

    public boolean retrySending() {
        return retrySending;
    }


    public Session getSession() {
        return session;
    }

    public String getFrom() {
        return from;
    }

    // return envelope return address
    public String getSmtpFrom() {
        return smtpFrom;
    }
}
