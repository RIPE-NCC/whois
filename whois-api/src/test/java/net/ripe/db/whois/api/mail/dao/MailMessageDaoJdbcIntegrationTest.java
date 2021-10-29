package net.ripe.db.whois.api.mail.dao;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.MimeMessageProvider;
import net.ripe.db.whois.api.mail.dequeue.MessageDequeue;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.update.domain.DequeueStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileCopyUtils;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;

@org.junit.jupiter.api.Tag("IntegrationTest")
public class MailMessageDaoJdbcIntegrationTest extends AbstractIntegrationTest {
    private MailMessageDao subject;
    @Autowired private MessageDequeue messageDequeue;

    @BeforeEach
    public void setup() throws Exception {
        subject = new MailMessageDaoJdbc(databaseHelper.getMailupdatesDataSource(), testDateTimeProvider);
        messageDequeue.stop(false);
    }

    @Test
    public void addMessage_invalid() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            subject.addMessage(new MimeMessage((Session) null));

            assertThat(getAllMessages(), hasSize(0));
        });
    }

    @Test
    public void addMessage_multiple_times() throws Exception {
        final MimeMessage original = MimeMessageProvider.getMessageMultipartAlternativePgpSigned();

        subject.addMessage(original);
        subject.addMessage(original);
        subject.addMessage(original);

        assertThat(getAllMessages(), hasSize(3));
    }

    @Test
    public void addClaimAndGetMessage_multipart() throws Exception {
        final MimeMessage original = MimeMessageProvider.getMessageMultipartAlternativePgpSigned();

        subject.addMessage(original);
        final String messageId = subject.claimMessage();
        final MimeMessage result = subject.getMessage(messageId);

        final String originalContents = FileCopyUtils.copyToString(new InputStreamReader(original.getRawInputStream()));
        final String resultContents = FileCopyUtils.copyToString(new InputStreamReader(result.getRawInputStream()));

        assertThat(resultContents, is(originalContents));
        assertThat(getAllMessages(), hasSize(1));
    }

    @Test
    public void deleteMessage_not_existing() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            subject.deleteMessage("not_exists");
        });
    }

    @Test
    public void deleteMessage() throws Exception {
        subject.addMessage(MimeMessageProvider.getMessageSimpleTextUnsigned());
        final String messageId = subject.claimMessage();
        subject.deleteMessage(messageId);

        assertThat(getAllMessages(), hasSize(0));
    }

    @Test
    public void claimMessage_none() {
        final String messageId = subject.claimMessage();

        assertNull(messageId);
        assertThat(getAllMessages(), hasSize(0));
    }

    @Test
    public void claim_some() {
        final int nrMessages = 10;

        final MimeMessage message = MimeMessageProvider.getMessageSimpleTextUnsigned();
        for (int i = 0; i < nrMessages; i++) {
            subject.addMessage(message);
        }

        for (int i = 0; i < nrMessages; i++) {
            final String messageId = subject.claimMessage();
            assertNotNull(messageId);
        }

        final String messageId = subject.claimMessage();
        assertNull(messageId);

        final List<Map<String, Object>> list = getAllMessages();
        assertThat(list, hasSize(nrMessages));

        for (final Map<String, Object> objectMap : list) {
            assertThat((String) objectMap.get("status"), is("CLAIMED"));
            assertNotNull(objectMap.get("message"));
            assertNotNull(objectMap.get("claim_host"));
            assertNotNull(objectMap.get("changed"));
        }
    }

    @Test
    public void setStatus() {
        final MimeMessage message = MimeMessageProvider.getMessageSimpleTextUnsigned();
        subject.addMessage(message);
        final String messageId = subject.claimMessage();

        subject.setStatus(messageId, DequeueStatus.LOGGED);

        final List<Map<String, Object>> messages = getAllMessages();
        assertThat(messages, hasSize(1));

        final Map<String, Object> objectMap = messages.get(0);
        assertThat((String) objectMap.get("status"), is("LOGGED"));
    }

    @Test
    public void setStatus_unknown_message() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            subject.setStatus("not_exists", DequeueStatus.LOGGED);
        });
    }

    private List<Map<String, Object>> getAllMessages() {
        return databaseHelper.getMailupdatesTemplate().queryForList("select * from mailupdates");
    }
}
