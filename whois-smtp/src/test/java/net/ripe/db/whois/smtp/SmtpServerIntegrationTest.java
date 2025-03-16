package net.ripe.db.whois.smtp;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.startsWith;

@Tag("IntegrationTest")
public class SmtpServerIntegrationTest extends AbstractSmtpIntegrationBase {


    @BeforeAll
    public static void setupStmpServer() {
        System.setProperty("smtp.enabled", "true");
    }

    @AfterAll
    public static void teardownStmpServer() {
        System.clearProperty("smtp.enabled");
    }

    @BeforeEach
    public void startSmtpServer() {
        smtpServer.start();
    }

    @AfterEach
    public void stopSmtpServer() {
        smtpServer.stop(true);
    }

    @Test
    public void testSmtpServer() throws Exception {
        final SmtpClient smtpClient = new SmtpClient("127.0.0.1", smtpServer.getPort());
        assertThat(smtpClient.readLine(), matchesPattern("220.*Whois.*"));
        smtpClient.writeLine("HELO testserver");
        assertThat(smtpClient.readLine(), matchesPattern("250.*Hello testserver"));
        smtpClient.writeLine("MAIL FROM: <user@example.com>");
        assertThat(smtpClient.readLine(), is("250 OK"));
        smtpClient.writeLine("RCPT TO: <test-dbm@ripe.net>");
        assertThat(smtpClient.readLine(), is("250 Accepted"));
        smtpClient.writeLine("DATA");
        assertThat(smtpClient.readLine(), is("354 Enter message, ending with \".\" on a line by itself"));
        smtpClient.writeLines(
            "Subject: Update\n" +
            "\n" +
            "RPSL object\n" +
            "\n" +
            ".\n");
        assertThat(smtpClient.readLine(), startsWith("250 OK"));
        smtpClient.writeLine("QUIT");
        assertThat(smtpClient.readLine(), startsWith("221 "));

        // TODO: test mailupdate is accepted and processed
    }

}
