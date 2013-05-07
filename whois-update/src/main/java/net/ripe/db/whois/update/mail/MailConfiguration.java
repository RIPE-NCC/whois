package net.ripe.db.whois.update.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.mail.Session;
import java.util.Properties;

@Component
public class MailConfiguration {

    @Value("${mail.smtp.host}")
    private String smtpHost;

    @Value("${whois.update.mail.smtpPort:25}")
    private String smtpPort;

    @Value("${mail.from}")
    private String from;

    @Value("${whois.update.mail.debug:false}")
    private boolean debug;

    private Session session;

    @PostConstruct
    public void initSession() {
        final Properties props = new Properties();

        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);

        session = Session.getInstance(props);
        session.setDebug(debug);
    }

    public Session getSession() {
        return session;
    }

    public String getFrom() {
        return from;
    }
}
