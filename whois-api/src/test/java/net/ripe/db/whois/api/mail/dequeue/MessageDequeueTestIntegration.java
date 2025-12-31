package net.ripe.db.whois.api.mail.dequeue;

import jakarta.mail.internet.MimeMessage;
import net.ripe.db.mock.UpdateRequestHandlerMockConfiguration;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.MailUpdatesTestSupport;
import net.ripe.db.whois.api.WhoisApiTestConfiguration;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateRequest;
import net.ripe.db.whois.update.domain.UpdateResponse;
import net.ripe.db.whois.update.domain.UpdateStatus;
import net.ripe.db.whois.update.handler.UpdateRequestHandler;
import net.ripe.db.whois.update.mail.MailSenderStub;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@ContextConfiguration(classes= {WhoisApiTestConfiguration.class, UpdateRequestHandlerMockConfiguration.class}, inheritLocations = false)
@Tag("IntegrationTest")
public class MessageDequeueTestIntegration extends AbstractIntegrationTest {

    private static final int CLIENTS = 8;

    @Autowired private MailSenderStub mailSender;
    @Autowired private MailUpdatesTestSupport mailUpdatesTestSupport;
    @Autowired private UpdateRequestHandler messageHandler;

    @BeforeAll
    public static void setNumberOfThreads() {
        System.setProperty("mail.update.threads", String.valueOf(CLIENTS));
    }

    @Test
    public void concurrent_handling_test() throws Exception {
        final AtomicInteger processed = new AtomicInteger();
        doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                processed.incrementAndGet();
                return new UpdateResponse(UpdateStatus.SUCCESS, "");
            }
        }).when(messageHandler).handle(any(UpdateRequest.class), any(UpdateContext.class));

        final String from = mailUpdatesTestSupport.insert(getClass().getSimpleName(), "");
        final MimeMessage message = mailSender.getMessage(from);

        assertThat(message.getSubject(), containsString("SUCCESS"));
        assertThat(mailSender.anyMoreMessages(), is(false));
        assertThat(processed.intValue(), is(1));
    }
}
