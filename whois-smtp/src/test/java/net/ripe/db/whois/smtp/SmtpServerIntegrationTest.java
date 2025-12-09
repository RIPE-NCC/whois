package net.ripe.db.whois.smtp;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.Sets;
import io.netty.buffer.ByteBufUtil;
import io.netty.util.ResourceLeakDetector;
import jakarta.mail.internet.MimeMessage;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

@Tag("IntegrationTest")
public class SmtpServerIntegrationTest extends AbstractSmtpIntegrationBase {

    private static final Set<String> LEAKS = Sets.newConcurrentHashSet();

    @BeforeAll
    public static void setupStmpServer() {
        System.setProperty("mail.smtp.server.enabled", "true");
        System.setProperty("mail.smtp.server.maximum.size", "1024");
        System.setProperty("mail.smtp.from", "test-dbm@ripe.net");
    }

    @BeforeAll
    public static void paranoidLeakDetection() {
        System.setProperty("io.netty.leakDetection.targetRecords", "1000");
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
        ByteBufUtil.setLeakListener((resourceType, records) -> LEAKS.add("leak " + resourceType + ":" + records));
    }

    @AfterAll
    public static void teardownStmpServer() {
        System.clearProperty("mail.smtp.server.enabled");
        System.clearProperty("mail.smtp.server.maximum.size");
        System.clearProperty("mail.smtp.from");
    }

    @AfterAll
    public static void defaultLeakDetection() {
        System.clearProperty("io.netty.leakDetection.targetRecords");
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.SIMPLE);
    }

    @BeforeEach
    public void startSmtpServer() {
        smtpServer.start();
    }

    @AfterEach
    public void stopSmtpServer() {
        smtpServer.stop(true);
    }

    @BeforeEach
    public void resetLeaks() {
        LEAKS.clear();
    }

    @AfterEach
    public void checkForLeaks() {
        assertThat(LEAKS, is(empty()));
    }

    @Test
    public void sendSingleMessage() throws Exception {
        try (final SmtpClient smtpClient = new SmtpClient("127.0.0.1", smtpServer.getPort())) {
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

            final String messageId = mailMessageDao.claimMessage();
            final MimeMessage result = mailMessageDao.getMessage(messageId);
            assertThat(result.getSubject(), is("Update"));
            assertThat(result.getContent(), is("RPSL object\n\n"));
        }
    }

    @Test
    public void sendSingleMessageCrLf() throws Exception {
        try (final SmtpClient smtpClient = new SmtpClient("127.0.0.1", smtpServer.getPort())) {
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
                "Subject: Update\r\n" +
                "\r\n" +
                "RPSL object\r\n" +
                "\r\n" +
                ".\r\n");
            assertThat(smtpClient.readLine(), startsWith("250 OK"));
            smtpClient.writeLine("QUIT");
            assertThat(smtpClient.readLine(), startsWith("221 "));

            final String messageId = mailMessageDao.claimMessage();
            final MimeMessage result = mailMessageDao.getMessage(messageId);
            assertThat(result.getSubject(), is("Update"));
            assertThat(result.getContent(), is("RPSL object\n\n"));
        }
    }

    @Test
    public void sendMultipleMessages() {
        assertThat(ResourceLeakDetector.getLevel(), is(ResourceLeakDetector.Level.PARANOID));
        assertThat(ResourceLeakDetector.isEnabled(), is(true));

        final AtomicInteger counter = new AtomicInteger();
        ContiguousSet.closed(0,999).parallelStream().forEach(c -> {
            try {
                sendSingleMessage();
                counter.incrementAndGet();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> (counter.get() == 1_000));
    }

    @Test
    public void sendEmptyMessage() throws Exception {
        try (final SmtpClient smtpClient = new SmtpClient("127.0.0.1", smtpServer.getPort())) {
            assertThat(smtpClient.readLine(), matchesPattern("220.*Whois.*"));
            smtpClient.writeLine("HELO testserver");
            assertThat(smtpClient.readLine(), matchesPattern("250.*Hello testserver"));
            smtpClient.writeLine("MAIL FROM: <user@example.com>");
            assertThat(smtpClient.readLine(), is("250 OK"));
            smtpClient.writeLine("RCPT TO: <test-dbm@ripe.net>");
            assertThat(smtpClient.readLine(), is("250 Accepted"));
            smtpClient.writeLine("DATA");
            assertThat(smtpClient.readLine(), is("354 Enter message, ending with \".\" on a line by itself"));
            smtpClient.writeLines(".\n");
            assertThat(smtpClient.readLine(), startsWith("250 OK"));
            smtpClient.writeLine("QUIT");
            assertThat(smtpClient.readLine(), startsWith("221 "));
            assertThat(mailMessageDao.claimMessage(), is(nullValue()));
        }
    }

    @Test
    public void sendMessageFromOurselvesIsRefused() throws Exception {
        try (final SmtpClient smtpClient = new SmtpClient("127.0.0.1", smtpServer.getPort())) {
            assertThat(smtpClient.readLine(), matchesPattern("220.*Whois.*"));
            smtpClient.writeLine("HELO testserver");
            assertThat(smtpClient.readLine(), matchesPattern("250.*Hello testserver"));
            smtpClient.writeLine("MAIL FROM: Example User <unread@ripe.net>");
            assertThat(smtpClient.readLine(), is("250 OK"));
            smtpClient.writeLine("RCPT TO: Example User <unread@ripe.net>");
            assertThat(smtpClient.readLine(), is("250 Accepted"));
            smtpClient.writeLine("DATA");
            assertThat(smtpClient.readLine(), is("354 Enter message, ending with \".\" on a line by itself"));
            smtpClient.writeLines(
                "From: Test DBM <test-dbm@ripe.net> \n" +
                "From: Example User <unread@ripe.net>\n" +
                "Subject: Update\n" +
                "\n" +
                "RPSL object\n" +
                "\n" +
                ".\n");
            assertThat(smtpClient.readLine(), is("500 refusing to accept message from test-dbm@ripe.net"));
            smtpClient.writeLine("QUIT");
            assertThat(smtpClient.readLine(), startsWith("221 "));
            assertThat(mailMessageDao.claimMessage(), is(nullValue()));
        }
    }

    @Test
    public void sendMessageDataMessageLargerThanMaximum() throws Exception {
        try (final SmtpClient smtpClient = new SmtpClient("127.0.0.1", smtpServer.getPort())) {
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
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. \n" +
                "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. \n" +
                "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. \n" +
                "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.\n" +
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. \n" +
                "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. \n" +
                "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. \n" +
                "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.\n" +
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. \n" +
                "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. \n" +
                "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. \n" +
                "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.\n" +
                "\n" +
                ".\n");
            assertThat(smtpClient.readLine(), startsWith("523 the total message size exceeds the server limit"));
            smtpClient.writeLine("QUIT");
            assertThat(smtpClient.readLine(), startsWith("221 "));
            assertThat(mailMessageDao.claimMessage(), is(nullValue()));
        }
    }

    @Test
    public void sendMessageDataLineLargerThanMaximum() throws Exception {
        try (final SmtpClient smtpClient = new SmtpClient("127.0.0.1", smtpServer.getPort())) {
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
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua." +
                "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat." +
                "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur." +
                "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum." +
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua." +
                "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat." +
                "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur." +
                "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum." +
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua." +
                "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat." +
                "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur." +
                "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.\n");
            assertThat(smtpClient.readLine(), startsWith("501 line too long"));
            smtpClient.writeLine(".");
            assertThat(smtpClient.readLine(), startsWith("250 OK"));
            smtpClient.writeLine("QUIT");
            assertThat(smtpClient.readLine(), startsWith("221 "));
        }
    }

    @Test
    public void sendExtendedHelloMultipleLines() throws Exception {
        try (final SmtpClient smtpClient = new SmtpClient("127.0.0.1", smtpServer.getPort())) {
            assertThat(smtpClient.readLine(), matchesPattern("220.*Whois.*"));
            smtpClient.writeLine("EHLO testserver");
            assertThat(smtpClient.readLine(), matchesPattern("250-.* Hello testserver"));
            assertThat(smtpClient.readLine(), is("250-SIZE 1024"));
            assertThat(smtpClient.readLine(), is("250-8BITMIME"));
            assertThat(smtpClient.readLine(), is("250 HELP"));
            smtpClient.writeLine("QUIT");
            assertThat(smtpClient.readLine(), startsWith("221 "));
        }
    }

    @Test
    public void sendMessageMailFromSizeLargerThanMaximum() throws Exception {
        try (final SmtpClient smtpClient = new SmtpClient("127.0.0.1", smtpServer.getPort())) {
            assertThat(smtpClient.readLine(), matchesPattern("220.*Whois.*"));
            smtpClient.writeLine("HELO testserver");
            assertThat(smtpClient.readLine(), matchesPattern("250.*Hello testserver"));

            smtpClient.writeLine("MAIL FROM: <user@example.com>     size=1025");

            assertThat(smtpClient.readLine(), startsWith("523 the total message size exceeds the server limit"));
            smtpClient.writeLine("QUIT");
            assertThat(smtpClient.readLine(), startsWith("221 "));
        }
    }

    @Test
    public void sendMessageMailFromOurselves() throws Exception {
        try (final SmtpClient smtpClient = new SmtpClient("127.0.0.1", smtpServer.getPort())) {
            assertThat(smtpClient.readLine(), matchesPattern("220.*Whois.*"));
            smtpClient.writeLine("HELO testserver");
            assertThat(smtpClient.readLine(), matchesPattern("250.*Hello testserver"));

            smtpClient.writeLine("MAIL FROM: <test-dbm@ripe.net>");

            assertThat(smtpClient.readLine(), is("500 refusing to accept message from test-dbm@ripe.net"));
            smtpClient.writeLine("QUIT");
            assertThat(smtpClient.readLine(), startsWith("221 "));
        }
    }

    // RFC821 Section 4.5.2 "Transparency"
    // If the first character is a period and there are other characters on the line, the first character is deleted.
    @Test
    public void encodedPeriod() throws Exception {
        try (final SmtpClient smtpClient = new SmtpClient("127.0.0.1", smtpServer.getPort())) {
            assertThat(smtpClient.readLine(), matchesPattern("220.*Whois.*"));
            smtpClient.writeLine("DATA");
            assertThat(smtpClient.readLine(), is("354 Enter message, ending with \".\" on a line by itself"));
            smtpClient.writeLines(
                "Subject: Update\n" +
                "\n" +
                "RPSL object\n" +
                "..\n" +    // encoded single period
                ".\n");
            assertThat(smtpClient.readLine(), startsWith("250 OK"));
            smtpClient.writeLine("QUIT");
            assertThat(smtpClient.readLine(), startsWith("221 "));

            final String messageId = mailMessageDao.claimMessage();
            final MimeMessage result = mailMessageDao.getMessage(messageId);

            assertThat(result.getContent(), is("RPSL object\n.\n"));    // decoded single period
        }
    }

    @Test
    public void invalidCommand() throws Exception {
        try (final SmtpClient smtpClient = new SmtpClient("127.0.0.1", smtpServer.getPort())) {
            assertThat(smtpClient.readLine(), matchesPattern("220.*Whois.*"));

            smtpClient.writeLine("HELO");

            assertThat(smtpClient.readLine(), is("501 Syntactically invalid HELO argument(s)"));
            smtpClient.writeLine("QUIT");
            assertThat(smtpClient.readLine(), startsWith("221 "));
        }
    }

    @Test
    public void sendMessageEmptyFromAddress() throws Exception {
        try (final SmtpClient smtpClient = new SmtpClient("127.0.0.1", smtpServer.getPort())) {
            assertThat(smtpClient.readLine(), matchesPattern("220.*Whois.*"));
            smtpClient.writeLine("HELO testserver");
            assertThat(smtpClient.readLine(), matchesPattern("250.*Hello testserver"));

            smtpClient.writeLine("MAIL FROM:<> SIZE=1024");

            assertThat(smtpClient.readLine(), is("250 OK"));
            smtpClient.writeLine("QUIT");
            assertThat(smtpClient.readLine(), startsWith("221 "));
        }
    }

    @Test
    public void sendMessageNoFromAddress() throws Exception {
        try (final SmtpClient smtpClient = new SmtpClient("127.0.0.1", smtpServer.getPort())) {
            assertThat(smtpClient.readLine(), matchesPattern("220.*Whois.*"));
            smtpClient.writeLine("HELO testserver");
            assertThat(smtpClient.readLine(), matchesPattern("250.*Hello testserver"));

            smtpClient.writeLine("MAIL FROM:");

            assertThat(smtpClient.readLine(), is("500 unrecognised command"));
            smtpClient.writeLine("QUIT");
            assertThat(smtpClient.readLine(), startsWith("221 "));
        }
    }

}
