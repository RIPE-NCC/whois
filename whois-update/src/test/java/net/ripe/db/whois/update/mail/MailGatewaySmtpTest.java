package net.ripe.db.whois.update.mail;

import net.ripe.db.whois.update.log.LoggerContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MailGatewaySmtpTest {
    @Mock LoggerContext loggerContext;
    @Mock MailConfiguration mailConfiguration;
    @Mock JavaMailSender mailSender;
    @InjectMocks private MailGatewaySmtp subject;

    @Before
    public void setUp() throws Exception {
        subject.setOutgoingMailEnabled(true);
    }

    @Test
    public void sendResponse() throws Exception {
        subject.sendEmail("to", "subject", "test");

        verify(mailSender, times(1)).send(any(MimeMessagePreparator.class));
    }

    @Test
    public void sendResponse_disabled() throws Exception {
        subject.setOutgoingMailEnabled(false);
        subject.sendEmail("to", "subject", "test");

        verifyZeroInteractions(mailSender);
    }
}
