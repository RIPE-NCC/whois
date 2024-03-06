package net.ripe.db.whois.update.mail;

import jakarta.mail.Address;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import net.ripe.db.whois.common.dao.OutgoingMessageDao;
import net.ripe.db.whois.common.dao.UndeliverableMailDao;
import net.ripe.db.whois.update.log.LoggerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Properties;

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
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage(SESSION));
        when(mailConfiguration.getFrom()).thenReturn("test@ripe.net");
        subject.sendEmail("to", "subject", "test", "test@ripe.net");

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    public void sendResponse_disabled() {
        ReflectionTestUtils.setField(subject, "outgoingMailEnabled", false);

        subject.sendEmail("to", "subject", "test", "");

        verifyNoMoreInteractions(mailSender);
    }

    @Test
    public void send_invoked_only_once_on_permanent_negative_response() {
        ReflectionTestUtils.setField(subject, "retrySending", true);

        Mockito.doAnswer(invocation -> {
            throw new MailSendException("550 rejected: mail rejected for policy reasons");
        }).when(mailSender).send(any(MimeMessage.class));

        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage(SESSION));
        when(mailConfiguration.getFrom()).thenReturn("test@ripe.net");

        subject.sendEmail("to", "subject", "test", "");

        verifyNoMoreInteractions(mailSender); //we are logging the error
    }

    @Test
    public void sendResponseAndCheckForReplyTo() {
        final String replyToAddress = "test@ripe.net";

        setExpectReplyToField(replyToAddress);
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage(SESSION));
        when(mailConfiguration.getFrom()).thenReturn(replyToAddress);

        subject.sendEmail("to", "subject", "test", replyToAddress);
    }

    @Test
    public void sendResponseAndCheckForEmptyReplyTo() {
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage(SESSION));
        when(mailConfiguration.getFrom()).thenReturn("test@ripe.net");

        subject.sendEmail("to", "subject", "test", "");
    }

    @Test
    public void checkRecipientAddressesArePunycoded() {
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage(SESSION));
        when(mailConfiguration.getFrom()).thenReturn("from@from.to");

        setExpectReplyToField("email@xn--idn-tla.org");

        subject.sendEmail("to@to.to", "subject", "test", "email@Åidn.org");
    }

    private void setExpectReplyToField(final String replyToAddress) {
        Mockito.doAnswer(invocation -> {
            final MimeMessage mimeMessage = (MimeMessage)invocation.getArguments()[0];
            final Address messageReplyToAddress = mimeMessage.getReplyTo()[0];

            if(!replyToAddress.equals(messageReplyToAddress.toString())) {
                fail();
            }

            return null;
        }).when(mailSender).send(any(MimeMessage.class));
    }

}
