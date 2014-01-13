package net.ripe.db.whois.api.mail.dequeue;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.MimeMessageProvider;
import net.ripe.db.whois.api.UpdatesParser;
import net.ripe.db.whois.api.mail.MailMessage;
import net.ripe.db.whois.api.mail.dao.MailMessageDao;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.MaintenanceMode;
import net.ripe.db.whois.update.domain.*;
import net.ripe.db.whois.update.handler.UpdateRequestHandler;
import net.ripe.db.whois.update.log.LoggerContext;
import net.ripe.db.whois.update.log.UpdateLog;
import net.ripe.db.whois.update.mail.MailGateway;
import net.ripe.db.whois.update.mail.MailMessageLogCallback;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import javax.mail.Message;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyListOf;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MessageDequeueTest {
    private static final int TIMEOUT = 1000;

    @Mock MaintenanceMode maintenanceMode;
    @Mock MailGateway mailGateway;
    @Mock MailMessageDao mailMessageDao;
    @Mock MessageFilter messageFilter;
    @Mock MessageParser messageParser;
    @Mock UpdatesParser updatesParser;
    @Mock UpdateRequestHandler messageHandler;
    @Mock LoggerContext loggerContext;
    @Mock UpdateLog updateLog;
    @Mock DateTimeProvider dateTimeProvider;
    @InjectMocks MessageDequeue subject;

    @Before
    public void setUp() throws Exception {
        subject.setNrThreads(1);
        subject.setIntervalMs(1);
        when(maintenanceMode.allowUpdate()).thenReturn(true);
    }

    @After
    public void tearDown() throws Exception {
        subject.stop(true);
    }

    @Test(expected = IllegalStateException.class)
    public void start_twice() {
        when(mailMessageDao.claimMessage()).thenReturn(null);

        subject.start();
        subject.start();
    }

    @Test
    public void stop_not_running() throws InterruptedException {
        subject.stop(true);
    }

    @Test
    public void noMessages() {
        when(mailMessageDao.claimMessage()).thenReturn(null);

        subject.start();
        verifyZeroInteractions(messageHandler);
    }

    @Test
    public void handleMessage_filtered() throws Exception {
        final MimeMessage message = MimeMessageProvider.getMessageSimpleTextUnsigned();

        when(messageFilter.shouldProcess(any(MailMessage.class))).thenReturn(false);
        when(mailMessageDao.getMessage("1")).thenReturn(message);
        when(mailMessageDao.claimMessage()).thenReturn("1").thenReturn(null);

        subject.start();

        verify(mailMessageDao, timeout(TIMEOUT)).deleteMessage("1");
        verify(loggerContext, timeout(TIMEOUT)).init("20120527220444.GA6565");
        verify(loggerContext, timeout(TIMEOUT)).log(eq("msg-in.txt"), any(MailMessageLogCallback.class));
        verify(mailMessageDao, timeout(TIMEOUT)).setStatus("1", DequeueStatus.LOGGED);
        verify(mailMessageDao, timeout(TIMEOUT)).setStatus("1", DequeueStatus.PARSED);
        verifyZeroInteractions(messageHandler);
    }

    @Test
    public void handleMessage() throws Exception {
        final MimeMessage message = MimeMessageProvider.getMessageSimpleTextUnsigned();

        when(messageFilter.shouldProcess(any(MailMessage.class))).thenReturn(true);
        when(messageParser.parse(eq(message), any(UpdateContext.class))).thenReturn(
                new MailMessage("", "", "", "", "", "", Keyword.NONE, Lists.<ContentWithCredentials>newArrayList()));
        when(updatesParser.parse(any(UpdateContext.class), anyListOf(ContentWithCredentials.class))).thenReturn(Lists.<Update>newArrayList());
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
        verify(mailGateway, timeout(TIMEOUT)).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    public void handleMessage_exception() throws Exception {
        final MimeMessage message = MimeMessageProvider.getMessageSimpleTextUnsigned();

        when(messageFilter.shouldProcess(any(MailMessage.class))).thenReturn(true);
        when(messageParser.parse(eq(message), any(UpdateContext.class))).thenReturn(
                new MailMessage("", "", "", "", "", "", Keyword.NONE, Lists.<ContentWithCredentials>newArrayList()));
        when(updatesParser.parse(any(UpdateContext.class), anyListOf(ContentWithCredentials.class))).thenReturn(Lists.<Update>newArrayList());
        when(messageHandler.handle(any(UpdateRequest.class), any(UpdateContext.class))).thenThrow(RuntimeException.class);

        when(mailMessageDao.getMessage("1")).thenReturn(message);
        when(mailMessageDao.claimMessage()).thenReturn("1").thenReturn(null);

        subject.start();

        verify(mailMessageDao, timeout(TIMEOUT)).setStatus("1", DequeueStatus.LOGGED);
        verify(mailMessageDao, timeout(TIMEOUT)).setStatus("1", DequeueStatus.PARSED);
        verify(mailMessageDao, timeout(TIMEOUT)).setStatus("1", DequeueStatus.FAILED);
        verify(loggerContext, timeout(TIMEOUT)).init("20120527220444.GA6565");
        verify(loggerContext, timeout(TIMEOUT)).log(eq("msg-in.txt"), any(MailMessageLogCallback.class));
        verifyZeroInteractions(mailGateway);
        verify(mailMessageDao, never()).deleteMessage("1");
    }

    @Test
    public void handleMessage_invalidReplyTo() throws Exception {
        final MimeMessage message = new MimeMessage(null, new ByteArrayInputStream("Reply-To: <respondera: ventas@amusing.cl>".getBytes()));

        when(messageFilter.shouldProcess(any(MailMessage.class))).thenReturn(false);
        when(messageParser.parse(eq(message), any(UpdateContext.class))).thenReturn(
                new MailMessage("", null, "", "", null, "", Keyword.NONE, Lists.<ContentWithCredentials>newArrayList()));

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
        final MimeMessage message = new MimeMessage(null, new ByteArrayInputStream(("From: <\"abrahamgv@gmail.com\">\n" +
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
                return new MessageParser(loggerContext).parse(((MimeMessage) arguments[0]), ((UpdateContext) arguments[1]));
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
