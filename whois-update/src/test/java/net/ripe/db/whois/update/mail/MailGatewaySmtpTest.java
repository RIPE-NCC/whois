package net.ripe.db.whois.update.mail;

import net.ripe.db.whois.update.log.LoggerContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.test.util.ReflectionTestUtils;

import javax.mail.SendFailedException;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class MailGatewaySmtpTest {
    @Mock LoggerContext loggerContext;
    @Mock MailConfiguration mailConfiguration;
    @Mock JavaMailSender mailSender;
    @InjectMocks private MailGatewaySmtp subject;

    @Before
    public void setUp() throws Exception {
        ReflectionTestUtils.setField(subject, "outgoingMailEnabled", true);
    }

    @Test
    public void sendResponse() throws Exception {
        subject.sendEmail("to", "subject", "test");

        verify(mailSender, times(1)).send(any(MimeMessagePreparator.class));
    }

    @Test
    public void sendResponse_disabled() throws Exception {
        ReflectionTestUtils.setField(subject, "outgoingMailEnabled", false);
        subject.sendEmail("to", "subject", "test");

        verifyZeroInteractions(mailSender);
    }

    @Test
    public void send_invoked_only_once_on_permanent_negative_response() {
        Mockito.doAnswer(new Answer<Void>() {
            @Override public Void answer(InvocationOnMock invocation) throws Throwable {
                throw new SendFailedException("550 rejected: mail rejected for policy reasons");
            }
        }).when(mailSender).send(any(MimeMessagePreparator.class));

        try {
            subject.sendEmail("to", "subject", "test");
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(SendFailedException.class));
            verify(mailSender, times(1)).send(any(MimeMessagePreparator.class));
        }
    }
}
