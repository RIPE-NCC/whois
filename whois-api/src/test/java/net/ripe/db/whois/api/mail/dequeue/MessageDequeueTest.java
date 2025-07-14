package net.ripe.db.whois.api.mail.dequeue;

import com.google.common.collect.Lists;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import net.ripe.db.whois.api.MimeMessageProvider;
import net.ripe.db.whois.api.UpdatesParser;
import net.ripe.db.whois.api.mail.MailMessage;
import net.ripe.db.whois.api.mail.dao.MailMessageDao;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.MaintenanceMode;
import net.ripe.db.whois.update.domain.DequeueStatus;
import net.ripe.db.whois.update.domain.Keyword;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateRequest;
import net.ripe.db.whois.update.domain.UpdateResponse;
import net.ripe.db.whois.update.domain.UpdateStatus;
import net.ripe.db.whois.update.handler.UpdateRequestHandler;
import net.ripe.db.whois.update.log.LoggerContext;
import net.ripe.db.whois.update.log.UpdateLog;
import net.ripe.db.whois.update.mail.MailMessageLogCallback;
import net.ripe.db.whois.update.mail.WhoisMailGatewaySmtp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MessageDequeueTest {
    private static final int TIMEOUT = 1000;

    private static final Session SESSION = Session.getInstance(new Properties());

    @Mock MaintenanceMode maintenanceMode;
    @Mock WhoisMailGatewaySmtp mailGateway;
    @Mock MailMessageDao mailMessageDao;
    @Mock MessageFilter messageFilter;
    @Mock MessageParser messageParser;
    @Mock UpdatesParser updatesParser;
    @Mock UpdateRequestHandler messageHandler;
    @Mock LoggerContext loggerContext;
    @Mock UpdateLog updateLog;
    @Mock DateTimeProvider dateTimeProvider;
    @Mock MessageService messageService;
    @InjectMocks MessageDequeue subject;

    @BeforeEach
    public void setUp() throws Exception {
        ReflectionTestUtils.setField(subject, "nrThreads", 1);
        ReflectionTestUtils.setField(subject, "intervalMs", 1);
        lenient().when(maintenanceMode.allowUpdate()).thenReturn(true);
        lenient().when(dateTimeProvider.getCurrentZonedDateTime()).thenReturn(ZonedDateTime.now(ZoneOffset.UTC));
    }

    @AfterEach
    public void tearDown() {
        subject.stop(true);
    }

    @Test
    public void start_twice() {
        assertThrows(IllegalStateException.class, () -> {
            subject.start();
            subject.start();
        });
    }

    @Test
    public void stop_not_running() {
        subject.stop(true);
    }

    @Test
    public void noMessages() {
        subject.start();
        verifyNoMoreInteractions(messageHandler);
    }

    @Test
    public void handleMessage_filtered() {
        final MimeMessage message = MimeMessageProvider.getMessageSimpleTextUnsigned();

        when(mailMessageDao.getMessage("1")).thenReturn(message);
        when(mailMessageDao.claimMessage()).thenReturn("1").thenReturn(null);

        subject.start();

        verify(mailMessageDao, timeout(TIMEOUT)).deleteMessage("1");
        verify(loggerContext, timeout(TIMEOUT)).init("20120527220444.GA6565");
        verify(loggerContext, timeout(TIMEOUT)).log(eq("msg-in.txt"), any(MailMessageLogCallback.class));
        verify(mailMessageDao, timeout(TIMEOUT)).setStatus("1", DequeueStatus.LOGGED);
        verify(mailMessageDao, timeout(TIMEOUT)).setStatus("1", DequeueStatus.PARSED);
        verifyNoMoreInteractions(messageHandler);
    }

    @Test
    public void handleMessage() throws Exception {
        final MimeMessage message = MimeMessageProvider.getMessageSimpleTextUnsigned();

        when(messageFilter.shouldProcess(any(MailMessage.class))).thenReturn(true);
        when(messageParser.parse(eq(message), any(UpdateContext.class))).thenReturn(
                new MailMessage("", "", "", "", "", "", Keyword.NONE, Lists.newArrayList()));
        when(updatesParser.parse(any(UpdateContext.class), anyList())).thenReturn(Lists.newArrayList());
        when(messageHandler.handle(any(UpdateRequest.class), any(UpdateContext.class))).thenReturn(new UpdateResponse(UpdateStatus.SUCCESS, ""));

        when(mailMessageDao.getMessage("1")).thenReturn(message);
        when(mailMessageDao.claimMessage()).thenReturn("1").thenReturn(null);

        subject.start();

        verify(mailMessageDao, timeout(TIMEOUT)).deleteMessage("1");
        verify(loggerContext, timeout(TIMEOUT)).init("20120527220444.GA6565");
        verify(loggerContext, timeout(TIMEOUT)).log(eq("msg-in.txt"), any(MailMessageLogCallback.class));
        verify(mailMessageDao, timeout(TIMEOUT)).setStatus("1", DequeueStatus.LOGGED);
        verify(mailMessageDao, timeout(TIMEOUT)).setStatus("1", DequeueStatus.PARSED);
        verify(messageHandler, timeout(TIMEOUT)).handle(any(UpdateRequest.class), any(UpdateContext.class));
        verify(mailGateway, timeout(TIMEOUT)).sendEmail(anyString(), anyString(), anyString(), any());
    }

    @Test
    public void handleMessage_exception() throws Exception {
        final MimeMessage message = MimeMessageProvider.getMessageSimpleTextUnsigned();

        when(messageFilter.shouldProcess(any(MailMessage.class))).thenReturn(true);
        when(messageParser.parse(eq(message), any(UpdateContext.class))).thenReturn(
                new MailMessage("", "", "", "", "", "", Keyword.NONE, Lists.newArrayList()));
        when(updatesParser.parse(any(UpdateContext.class), anyList())).thenReturn(Lists.newArrayList());
        when(messageHandler.handle(any(UpdateRequest.class), any(UpdateContext.class))).thenThrow(RuntimeException.class);

        when(mailMessageDao.getMessage("1")).thenReturn(message);
        when(mailMessageDao.claimMessage()).thenReturn("1").thenReturn(null);

        subject.start();

        verify(mailMessageDao, timeout(TIMEOUT)).setStatus("1", DequeueStatus.LOGGED);
        verify(mailMessageDao, timeout(TIMEOUT)).setStatus("1", DequeueStatus.PARSED);
        verify(mailMessageDao, timeout(TIMEOUT)).setStatus("1", DequeueStatus.FAILED);
        verify(loggerContext, timeout(TIMEOUT)).init("20120527220444.GA6565");
        verify(loggerContext, timeout(TIMEOUT)).log(eq("msg-in.txt"), any(MailMessageLogCallback.class));
        verifyNoMoreInteractions(mailGateway);
        verify(mailMessageDao, never()).deleteMessage("1");
    }

    @Test
    public void handleMessage_invalidReplyTo() throws Exception {
        final MimeMessage message = new MimeMessage(SESSION, new ByteArrayInputStream("Reply-To: <respondera: ventas@amusing.cl>".getBytes()));

        when(messageFilter.shouldProcess(any(MailMessage.class))).thenReturn(false);
        when(messageParser.parse(eq(message), any(UpdateContext.class))).thenReturn(
                new MailMessage("", null, "", "", null, "", Keyword.NONE, Lists.newArrayList()));

        when(mailMessageDao.getMessage("1")).thenReturn(message);
        when(mailMessageDao.claimMessage()).thenReturn("1").thenReturn(null);

        subject.start();

        verify(mailMessageDao, timeout(TIMEOUT)).deleteMessage("1");
    }

    @Test
    public void getMessageIdLocalPart_local_and_domain_parts() throws Exception {
        Message message = mock(Message.class);
        when(message.getHeader("Message-Id")).thenReturn(new String[]{"<20120527220444.GA6565@ripe.net>"});

        final String messageIdLocalPart = subject.getMessageIdLocalPart(message);
        assertThat(messageIdLocalPart, is("20120527220444.GA6565"));
    }

    @Test
    public void getMessageIdLocalPart_local_part_only() throws Exception {
        Message message = mock(Message.class);
        when(message.getHeader("Message-Id")).thenReturn(new String[]{"<20120527220444.GA6565>"});

        final String messageIdLocalPart = subject.getMessageIdLocalPart(message);
        assertThat(messageIdLocalPart, is("20120527220444.GA6565"));
    }

    @Test
    public void getMessageIdLocalPart_emptyMessageId() throws Exception {
        Message message = mock(Message.class);

        final String messageIdLocalPart = subject.getMessageIdLocalPart(message);
        assertThat(messageIdLocalPart, containsString("No-Message-Id."));
    }

    @Test
    public void getMessageIdLocalPart_messageId_doesnt_match() throws Exception {
        Message message = mock(Message.class);
        when(message.getHeader("Message-Id")).thenReturn(new String[]{"<W[20"});

        final String messageIdLocalPart = subject.getMessageIdLocalPart(message);
        assertThat(messageIdLocalPart, containsString("No-Message-Id."));
    }

    @Test
    public void malformed_from_header_is_detected() throws Exception {
        final MimeMessage message = new MimeMessage(SESSION, new ByteArrayInputStream(("From: <\"abrahamgv@gmail.com\">\n" +
                "Subject: blabla\n" +
                "To: bitbucket@ripe.net\n" +
                "\n" +
                "body\n").getBytes()));

        when(mailMessageDao.getMessage("1")).thenReturn(message);
        when(mailMessageDao.claimMessage()).thenReturn("1").thenReturn(null);

        when(messageParser.parse(eq(message), any(UpdateContext.class))).thenAnswer(new Answer<MailMessage>() {
            @Override
            public MailMessage answer(InvocationOnMock invocation) throws Throwable {
                final Object[] arguments = invocation.getArguments();
                return new MessageParser(loggerContext, dateTimeProvider).parse(((MimeMessage) arguments[0]), ((UpdateContext) arguments[1]));
            }
        });

        when(messageFilter.shouldProcess(any(MailMessage.class))).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                final Object[] arguments = invocation.getArguments();
                return new MessageFilter(loggerContext).shouldProcess((MailMessage)arguments[0]);
            }
        });

        subject.start();

        verify(mailMessageDao, timeout(TIMEOUT)).deleteMessage("1");
        verify(updatesParser, never()).parse(any(UpdateContext.class), anyList());
        verify(messageHandler, never()).handle(any(UpdateRequest.class), any(UpdateContext.class));
        verify(loggerContext).log(any(net.ripe.db.whois.common.Message.class));
    }
}
