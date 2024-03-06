package net.ripe.db.whois.update.mail;

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
    public void checkRecipientAddressesArePunycoded() throws Exception {
        // TODO: We are not longer using MimeMessagePreparator, so there is no way to get the message that the unit
        //  test is sending
        /*when(mailSender.createMimeMessage()).thenReturn(new MimeMessage(SESSION));
        when(mailConfiguration.getFrom()).thenReturn("from@from.to");

        subject.sendEmail("to@to.to", "subject", "test", "email@Åidn.org");

        ArgumentCaptor<MimeMessagePreparator> argument = ArgumentCaptor.forClass(MimeMessagePreparator.class);
        verify(mailSender).send(any(MimeMessage.class));
        final MimeMessage mimeMessage = new SMTPMessage(SESSION);
        argument.getValue().prepare(mimeMessage);
        assertThat(mimeMessage.getHeader("Reply-To", null), is("email@xn--idn-tla.org"));*/
    }

}
