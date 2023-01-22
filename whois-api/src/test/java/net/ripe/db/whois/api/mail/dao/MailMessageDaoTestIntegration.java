package net.ripe.db.whois.api.mail.dao;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.MimeMessageProvider;
import net.ripe.db.whois.api.mail.dequeue.MessageDequeue;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.kubek2k.springockito.annotations.ReplaceWithMock;
import org.kubek2k.springockito.annotations.SpringockitoContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import jakarta.mail.internet.MimeMessage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(loader = SpringockitoContextLoader.class, locations = {"classpath:applicationContext-api-test.xml"}, inheritLocations = false)
@Tag("IntegrationTest")
public class MailMessageDaoTestIntegration extends AbstractIntegrationTest {
    @Autowired MailMessageDao subject;
    @Autowired @ReplaceWithMock private MessageDequeue messageDequeue;

    @Test
    public void claim_multiple_threads() throws Exception {
        final int nrThreads = 10;
        final int nrMsgsPerThread = 50;
        final int nrMessages = nrThreads * nrMsgsPerThread;

        final MimeMessage message = MimeMessageProvider.getMessageSimpleTextUnsigned();
        for (int i = 0; i < nrMessages; i++) {
            subject.addMessage(message);
        }

        final CountDownLatch countDownLatch = new CountDownLatch(nrMessages);
        for (int i = 0; i < nrThreads; i++) {
            new Thread() {
                @Override
                public void run() {
                    for (int j = 0; j < nrMsgsPerThread; j++) {
                        final String messageId = subject.claimMessage();
                        if (messageId == null) {
                            throw new AssertionError("Message id should not be null");
                        }

                        countDownLatch.countDown();
                    }
                }
            }.start();
        }

        countDownLatch.await(15, TimeUnit.SECONDS);
        assertThat(countDownLatch.getCount(), is(0L));
        assertThat(subject.claimMessage(), is(nullValue()));
    }
}
