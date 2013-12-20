package net.ripe.db.whois.api.mail.dequeue;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.MailUpdatesTestSupport;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateRequest;
import net.ripe.db.whois.update.domain.UpdateResponse;
import net.ripe.db.whois.update.domain.UpdateStatus;
import net.ripe.db.whois.update.handler.UpdateRequestHandler;
import net.ripe.db.whois.update.mail.MailSenderStub;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kubek2k.springockito.annotations.ReplaceWithMock;
import org.kubek2k.springockito.annotations.SpringockitoContextLoader;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import javax.mail.internet.MimeMessage;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(loader = SpringockitoContextLoader.class, locations = {"classpath:applicationContext-api-test.xml"}, inheritLocations = false)
@Category(IntegrationTest.class)
public class MessageDequeueTestIntegration extends AbstractIntegrationTest {

    private static final int CLIENTS = 8;

    @Autowired private MailSenderStub mailSender;
    @Autowired private MailUpdatesTestSupport mailUpdatesTestSupport;
    @Autowired @ReplaceWithMock private UpdateRequestHandler messageHandler;

    @BeforeClass
    public static void setNumberOfThreads() {
        System.setProperty("mail.dequeue.threads", String.valueOf(CLIENTS));
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
