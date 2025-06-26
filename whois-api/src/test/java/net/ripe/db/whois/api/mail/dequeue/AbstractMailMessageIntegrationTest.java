package net.ripe.db.whois.api.mail.dequeue;

import jakarta.mail.internet.MimeMessage;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.MailUpdatesTestSupport;
import net.ripe.db.whois.common.mail.EmailStatusType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

public class AbstractMailMessageIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MailUpdatesTestSupport mailUpdatesTestSupport;

    protected static final String BOUNCED_MAIL_RECIPIENT = "nonexistant@host.org";

    protected static final String ANOTHER_BOUNCED_MAIL_RECIPIENT = "nonexistant1@host.org";

    protected static final String UNSUBSCRIBED_MAIL_RECIPIENT = "enduser@ripe.net";


    @BeforeAll
    public static void setSmtpFrom() {
        // Email address to use for SMTP MAIL command. This sets the envelope return address.
        // Ref. https://javaee.github.io/javamail/docs/api/com/sun/mail/smtp/package-summary.html
        System.setProperty("mail.smtp.from", "bounce-handler@ripe.net");
        System.setProperty("mail.smtp.dsn.notify", "SUCCESS,FAILURE,DELAY"); // Action supported types
        System.setProperty("mail.smtp.dsn.ret", "HDRS"); // Return the message headers in the DSN (Delivery Message Notification)
        System.setProperty("mail.smtp.enabled", "true");
    }

    @AfterAll
    public static void clearSmtpFrom() {
        System.clearProperty("mail.smtp.from");
        System.clearProperty("mail.smtp.dsn.notify");
        System.clearProperty("mail.smtp.dsn.ret");
        System.clearProperty("mail.smtp.enabled");
    }

    @BeforeEach
    public void setup() {
        databaseHelper.addObject(
                "person:        Test Person\n" +
                        "nic-hdl:       TP1-TEST\n" +
                        "source:        TEST");
        databaseHelper.addObject(
                "mntner:        OWNER-MNT\n" +
                        "descr:         Owner Maintainer\n" +
                        "admin-c:       TP1-TEST\n" +
                        "upd-to:        upd-to@ripe.net\n" +
                        "notify:        notify@ripe.net\n" +
                        "auth:          MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                        "mnt-by:        OWNER-MNT\n" +
                        "source:        TEST");
    }

    // helper methods
    protected String insertIncomingMessage(final String subject, final String body) {
        return mailUpdatesTestSupport.insert(subject, body);
    }

    protected void insertIncomingMessage(final MimeMessage message) {
        mailUpdatesTestSupport.insert(message);
    }

    protected void insertOutgoingMessageId(final String messageId, final String emailAddress) {
        internalsTemplate.update(
                "INSERT INTO outgoing_message (message_id, email) VALUES (?, ?)", messageId, emailAddress);
    }

    protected boolean isUndeliverableAddress(final String emailAddress) {
        return internalsTemplate.queryForObject("SELECT count(email) FROM email_status WHERE email= ? and status=?",
                (rs, rowNum) -> rs.getInt(1), emailAddress, EmailStatusType.UNDELIVERABLE.name()) == 1;
    }

    protected boolean isUnsubscribeAddress(final String emailAddress) {
        return internalsTemplate.queryForObject("SELECT count(email) FROM email_status WHERE email= ? and status=?",
                (rs, rowNum) -> rs.getInt(1), emailAddress, EmailStatusType.UNSUBSCRIBE.name()) == 1;
    }

    protected void insertUndeliverableAddress(final String emailAddress) {
        internalsTemplate.update(
                "INSERT INTO email_status (email, status) VALUES (?, ?)", emailAddress, EmailStatusType.UNDELIVERABLE.name());
    }


    protected int countUndeliverableAddresses(){
        return internalsTemplate.queryForObject("SELECT count(email) FROM email_status", Integer.class);
    }

    protected int countOutgoingForAddress(final String emailAddress) {
        return internalsTemplate.queryForObject("SELECT count(message_id) FROM outgoing_message WHERE email = ?", Integer.class, emailAddress);
    }

    protected void updateOutgoingMessageIdForEmail(final String messageId, final String email){
        internalsTemplate.update("UPDATE outgoing_message SET message_id = ? WHERE email = ?", messageId, email);
    }

    protected boolean anyIncomingMessages() {
        return mailupdatesTemplate.queryForObject("SELECT count(message) FROM mailupdates", Integer.class) > 0;
    }
}
