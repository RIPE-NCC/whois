package net.ripe.db.whois.update.mail;

import net.ripe.db.whois.update.log.LoggerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.test.util.ReflectionTestUtils;

import javax.mail.SendFailedException;
import javax.mail.internet.MimeMessage;
import java.lang.reflect.Field;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MailGatewaySmtpTest {
    @Mock LoggerContext loggerContext;
    @Mock MailConfiguration mailConfiguration;
    @Mock JavaMailSender mailSender;
    @InjectMocks private MailGatewaySmtp subject;

    @BeforeEach
    public void setUp() throws Exception {
        ReflectionTestUtils.setField(subject, "outgoingMailEnabled", true);
    }

    @Test
    public void sendResponse() {
        subject.sendEmail("to", "subject", "test", "");

        verify(mailSender).send(any(MimeMessagePreparator.class));
    }

    @Test
    public void sendResponse_disabled() {
        ReflectionTestUtils.setField(subject, "outgoingMailEnabled", false);

        subject.sendEmail("to", "subject", "test", "");

        verifyNoMoreInteractions(mailSender);
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
            verify(mailSender).send(any(MimeMessagePreparator.class));
        }
    }

    @Test
    public void sendResponseAndCheckForReplyTo() {
        final String replyToAddress = "test@ripe.net";

        setExpectReplyToField(replyToAddress);

        subject.sendEmail("to", "subject", "test", replyToAddress);
    }

    @Test
    public void sendResponseAndCheckForEmptyReplyTo() {
        final String replyToAddress = "";

        setExpectReplyToField(replyToAddress);

        subject.sendEmail("to", "subject", "test", "");
    }

    @Test
    public void checkRecipientAddressesArePunycoded() throws Exception {
        MailSenderStub mailSenderStub = new MailSenderStub();
        MailGatewaySmtp mailGatewaySmtp = new MailGatewaySmtp(loggerContext, mailConfiguration, mailSenderStub);
        ReflectionTestUtils.setField(mailGatewaySmtp, "outgoingMailEnabled", true);

        when(mailConfiguration.getFrom()).thenReturn("from@from.to");

        mailGatewaySmtp.sendEmail("to@to.to", "subject", "test", "email@Åidn.org");

        final MimeMessage message = mailSenderStub.getMessage("to@to.to");
        assertThat(message.getReplyTo()[0].toString(), is("email@xn--idn-tla.org"));
    }

    private void setExpectReplyToField(final String replyToAddress) {
        Mockito.doAnswer(invocation -> {
            final Class<?> subjectClass = invocation.getArguments()[0].getClass();
            final Field replyToField = subjectClass.getDeclaredField("arg$3");
            replyToField.setAccessible(true);

            final Object value = replyToField.get(invocation.getArguments()[0]);

            if(!replyToAddress.equals(value)) {
                fail();
            }

            return null;
        }).when(mailSender).send(any(MimeMessagePreparator.class));
    }

}
