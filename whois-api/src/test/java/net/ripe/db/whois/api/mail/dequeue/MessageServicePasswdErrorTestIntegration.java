package net.ripe.db.whois.api.mail.dequeue;

import jakarta.mail.MessagingException;
import net.ripe.db.whois.update.mail.MailSenderStub;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

@Tag("IntegrationTest")
public class MessageServicePasswdErrorTestIntegration extends AbstractMailMessageIntegrationTest {

    @Autowired
    private MailSenderStub mailSenderStub;


    private static final String FORMATTED_PASSWORD_ERROR = """
            ***Error:   Password authentication is not allowed in Mailupdates, because your
                        credentials may be compromised in transit. Please switch to PGP
                        signed mailupdates or use a different update method such as the
                        REST API or Syncupdates.
            """;

    @BeforeAll
    public static void setUp() {
        System.setProperty("mailupdates.passwd.error", "true");
    }

    @AfterAll
    public static void clearProperties() {
        System.clearProperty("mailupdates.passwd.error");
    }

    @Test
    public void test_upd_single_object_with_password_then_error() throws MessagingException, IOException {

        final String incomingMessage = """
                role:        dummy role
                address:       Singel 258
                e-mail:        dummyrole@ripe.net
                phone:         +31 6 12345678
                notify:        notify-dummy-role@ripe.net
                nic-hdl:       DR1-TEST
                mnt-by:        OWNER-MNT
                source:        TEST
                password: test
                """;

        final String from = insertIncomingMessage("NEW", incomingMessage);
        final String acknowledgement = mailSenderStub.getMessage(from).getContent().toString();
        assertThat(acknowledgement, containsString(FORMATTED_PASSWORD_ERROR));
    }

    @Test
    public void test_upd_single_object_without_password_then_no_error() throws MessagingException, IOException {
        final String incomingMessage = """
                mntner:        OWNER1-MNT
                descr:         Owner Maintainer
                admin-c:       TP1-TEST
                upd-to:        upd-to@ripe.net
                notify:        notify@ripe.net
                auth:          MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test
                mnt-by:        OWNER1-MNT
                source:        TEST
                """;


        // send message and read acknowledgement reply
        final String from = insertIncomingMessage("NEW", incomingMessage);
        final String acknowledgement = mailSenderStub.getMessage(from).getContent().toString();

        assertThat(acknowledgement, not(containsString(FORMATTED_PASSWORD_ERROR)));
    }

    @Test
    public void test_upd_multiple_objects_with_without_password_then_error() throws MessagingException, IOException {
        final String incomingMessage = """
                mntner:        OWNER1-MNT
                descr:         Owner Maintainer
                admin-c:       TP1-TEST
                upd-to:        upd-to@ripe.net
                notify:        notify@ripe.net
                auth:          MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test
                mnt-by:        OWNER1-MNT
                source:        TEST
                
                role:        dummy role
                address:       Singel 258
                e-mail:        dummyrole@ripe.net
                phone:         +31 6 12345678
                notify:        notify-dummy-role@ripe.net
                nic-hdl:       DR1-TEST
                mnt-by:        OWNER1-MNT
                source:        TEST
                password: test
                """;


        // send message and read acknowledgement reply
        final String from = insertIncomingMessage("NEW", incomingMessage);
        final String acknowledgement = mailSenderStub.getMessage(from).getContent().toString();

        assertThat(acknowledgement, containsString(FORMATTED_PASSWORD_ERROR));
    }

    @Test
    public void test_upd_multiple_objects_with_password_then_error() throws MessagingException, IOException {
        final String incomingMessage = """
                mntner:        OWNER1-MNT
                descr:         Owner Maintainer
                admin-c:       TP1-TEST
                upd-to:        upd-to@ripe.net
                notify:        notify@ripe.net
                auth:          MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test
                mnt-by:        OWNER1-MNT
                source:        TEST
                
                role:        dummy role
                address:       Singel 258
                e-mail:        dummyrole@ripe.net
                phone:         +31 6 12345678
                notify:        notify-dummy-role@ripe.net
                nic-hdl:       DR1-TEST
                mnt-by:        OWNER1-MNT
                source:        TEST
                password: test
                
                role:        dummy role 1
                address:       Singel 258
                e-mail:        dummyrole@ripe.net
                phone:         +31 6 12345678
                notify:        notify-dummy-role@ripe.net
                nic-hdl:       DR2-TEST
                mnt-by:        OWNER1-MNT
                source:        TEST
                password: test
                """;

        final String expectedGlobalWarn = String.format("""
                DETAILED EXPLANATION:
                
                ***Warning: MD5 hashed password authentication is deprecated and support will be
                            removed at the end of 2025. Please switch to an alternative
                            authentication method before then.
                %s
                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                """, FORMATTED_PASSWORD_ERROR);

        // send message and read acknowledgement reply
        final String from = insertIncomingMessage("NEW", incomingMessage);
        final String acknowledgement = mailSenderStub.getMessage(from).getContent().toString();

        assertThat(acknowledgement, containsString(expectedGlobalWarn));
    }
}
