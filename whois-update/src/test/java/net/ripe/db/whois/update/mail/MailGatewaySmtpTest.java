package net.ripe.db.whois.update.mail;

import jakarta.mail.SendFailedException;
import jakarta.mail.Session;
import net.ripe.db.whois.common.dao.OutgoingMessageDao;
import net.ripe.db.whois.common.dao.UndeliverableMailDao;
import net.ripe.db.whois.update.log.LoggerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MailGatewaySmtpTest {

    private static final Session SESSION = Session.getInstance(new Properties());

    @Mock LoggerContext loggerContext;
    @Mock MailConfiguration mailConfiguration;
    @Mock JavaMailSender mailSender;
    @Mock UndeliverableMailDao undeliverableMailDao;
    @Mock OutgoingMessageDao outgoingMessageDao;
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
        when(mailConfiguration.getFrom()).thenReturn("from@from.to");

        subject.sendEmail("to@to.to", "subject", "test", "email@Åidn.org");

        ArgumentCaptor<MimeMessagePreparator> argument = ArgumentCaptor.forClass(MimeMessagePreparator.class);
        verify(mailSender).send(argument.capture());
        final CustomMimeMessage mimeMessage = new CustomMimeMessage(SESSION);
        argument.getValue().prepare(mimeMessage);
        assertThat(mimeMessage.getHeader("Reply-To", null), is("email@xn--idn-tla.org"));
    }

    // helper methods

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
