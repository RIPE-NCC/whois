package net.ripe.db.whois.api.mail.dao;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.MimeMessageProvider;
import net.ripe.db.whois.api.mail.dequeue.MessageDequeue;
import net.ripe.db.whois.update.domain.DequeueStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileCopyUtils;

import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;


@Tag("IntegrationTest")
public class MailMessageDaoJdbcIntegrationTest extends AbstractIntegrationTest {

    private static final Session SESSION = Session.getInstance(new Properties());

    private MailMessageDao subject;
    @Autowired private MessageDequeue messageDequeue;

    @BeforeEach
    public void setup() throws Exception {
        subject = new MailMessageDaoJdbc(databaseHelper.getMailupdatesDataSource(), testDateTimeProvider);
        messageDequeue.stop(false);
    }

    @Test
    public void addMessage_invalid() {
        assertThrows(IllegalArgumentException.class, () -> {
            subject.addMessage(new MimeMessage(SESSION));

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
        assertThrows(IllegalArgumentException.class, () -> {
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

        assertThat(messageId, is(nullValue()));
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
            assertThat(messageId, not(nullValue()));
        }

        final String messageId = subject.claimMessage();
        assertThat(messageId, is(nullValue()));

        final List<Map<String, Object>> list = getAllMessages();
        assertThat(list, hasSize(nrMessages));

        for (final Map<String, Object> objectMap : list) {
            assertThat((String) objectMap.get("status"), is("CLAIMED"));
            assertThat(objectMap.get("message"), not(nullValue()));
            assertThat(objectMap.get("claim_host"), not(nullValue()));
            assertThat(objectMap.get("changed"), not(nullValue()));
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
        assertThrows(IllegalArgumentException.class, () -> {
            subject.setStatus("not_exists", DequeueStatus.LOGGED);
        });
    }

    private List<Map<String, Object>> getAllMessages() {
        return mailupdatesTemplate.queryForList("select * from mailupdates");
    }
}
