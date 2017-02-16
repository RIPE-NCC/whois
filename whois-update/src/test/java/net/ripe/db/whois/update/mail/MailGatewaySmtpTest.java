package net.ripe.db.whois.update.mail;

import net.ripe.db.whois.update.log.LoggerContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.test.util.ReflectionTestUtils;

import javax.mail.SendFailedException;
import java.lang.reflect.Field;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
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
        ReflectionTestUtils.setField(subject, "outgoingMailEnabled", true);
    }

    @Test
    public void sendResponse() throws Exception {
        subject.sendEmail("to", "subject", "test", "");

        verify(mailSender, times(1)).send(any(MimeMessagePreparator.class));
    }

    @Test
    public void sendResponse_disabled() throws Exception {
        ReflectionTestUtils.setField(subject, "outgoingMailEnabled", false);
        subject.sendEmail("to", "subject", "test", "");

        verifyZeroInteractions(mailSender);
    }

    @Test
    public void send_invoked_only_once_on_permanent_negative_response() {
        Mockito.doAnswer(invocation -> {
            throw new SendFailedException("550 rejected: mail rejected for policy reasons");
        }).when(mailSender).send(any(MimeMessagePreparator.class));

        try {
            subject.sendEmail("to", "subject", "test", "");
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(SendFailedException.class));
            verify(mailSender, times(1)).send(any(MimeMessagePreparator.class));
        }
    }

    @Test
    public void sendResponseAndCheckForReplyTo() throws Exception {
        final String replyToAddress = "test@ripe.net";

        setExpectReplyToField(replyToAddress);

        subject.sendEmail("to", "subject", "test", replyToAddress);
    }

    @Test
    public void sendResponseAndCheckForEmptyReplyTo() throws Exception {
        final String replyToAddress = "";

        setExpectReplyToField(replyToAddress);

        subject.sendEmail("to", "subject", "test", "");
    }

    private void setExpectReplyToField(final String replyToAddress) {
        Mockito.doAnswer(invocation -> {

            Class<?> aClass = invocation.getArguments()[0].getClass();
            aClass.getDeclaredFields();
            Field field = aClass.getDeclaredField("val$replyTo");
            field.setAccessible(true);

            Object value = field.get(invocation.getArguments()[0]);

            if(!replyToAddress.equals(value)) {
                fail();
            }

            return null;
        }).when(mailSender).send(any(MimeMessagePreparator.class));
    }

}
