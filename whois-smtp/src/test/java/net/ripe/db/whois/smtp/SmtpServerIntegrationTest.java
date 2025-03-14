package net.ripe.db.whois.smtp;

import com.google.common.util.concurrent.Uninterruptibles;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

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
    public void testSmtpServer() {
        System.out.println("smtp port is " + smtpServer.getPort());
        Uninterruptibles.sleepUninterruptibly(1L, TimeUnit.HOURS);


    }

}
