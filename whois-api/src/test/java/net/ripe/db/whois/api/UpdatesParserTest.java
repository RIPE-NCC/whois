package net.ripe.db.whois.api;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.*;
import net.ripe.db.whois.update.log.LoggerContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UpdatesParserTest {
    private static final String SOURCE = "RIPE";
    private static final String MNTNER_DEV_MNT = "mntner: DEV-MNT\nsource: " + SOURCE;

    @Mock Origin origin;
    @Mock Credentials credentials;
    @Mock UpdateContext updateContext;
    @Mock RpslObjectDao rpslObjectDao;
    @Mock LoggerContext loggerContext;

    @Mock ParagraphParser paragraphParser;
    @InjectMocks UpdatesParser subject;

    @Before
    public void setUp() throws Exception {
        when(paragraphParser.createParagraphs(any(ContentWithCredentials.class))).thenAnswer(new Answer<List<Paragraph>>() {
            @Override
            public List<Paragraph> answer(final InvocationOnMock invocation) throws Throwable {
                final ContentWithCredentials content = (ContentWithCredentials)invocation.getArguments()[0];
                final Set<Credential> credentials = Sets.newLinkedHashSet();
                credentials.addAll(content.getCredentials());
                return Arrays.asList(new Paragraph(content.getContent(), new Credentials(credentials)));
            }
        });
    }

    @Test
    public void no_paragraphs() throws Exception {
        final List<Update> updates = subject.parse(updateContext, Lists.<ContentWithCredentials>newArrayList());
        assertThat(updates, hasSize(0));
    }

    @Test
    public void single_paragraph() {
        List<ContentWithCredentials> content = Lists.newArrayList();
        content.add(new ContentWithCredentials(MNTNER_DEV_MNT));

        final List<Update> updates = subject.parse(updateContext, content);

        assertThat(updates, hasSize(1));
        final Update update = updates.get(0);
        assertThat(update.getOperation(), is(Operation.UNSPECIFIED));
        assertThat(update.getDeleteReasons(), hasSize(0));
        assertFalse(update.isOverride());
        assertThat(update.getSubmittedObject(), is(RpslObject.parse(MNTNER_DEV_MNT)));
        assertThat(update.getParagraph().getContent(), is(MNTNER_DEV_MNT));
        verify(updateContext, never()).ignore(any(Paragraph.class));
    }

    @Test
    public void multiple_paragraphs() {
        List<ContentWithCredentials> content = Lists.newArrayList();
        content.add(new ContentWithCredentials(MNTNER_DEV_MNT));
        content.add(new ContentWithCredentials(MNTNER_DEV_MNT));

        final List<Update> updates = subject.parse(updateContext, content);
        assertThat(updates, hasSize(2));

        verify(updateContext, never()).ignore(any(Paragraph.class));
    }

    @Test
    public void delete_before() {
        List<ContentWithCredentials> content = Lists.newArrayList();
        content.add(new ContentWithCredentials("delete: reason\n" + MNTNER_DEV_MNT));

        final List<Update> updates = subject.parse(updateContext, content);
        assertThat(updates, hasSize(1));

        final Update update = updates.get(0);
        assertThat(update.getOperation(), is(Operation.DELETE));
        assertThat(update.getDeleteReasons(), contains(new String[] {"reason"}));
        assertFalse(update.isOverride());
        assertThat(update.getSubmittedObject(), is(RpslObject.parse(MNTNER_DEV_MNT)));

        verify(updateContext, never()).ignore(any(Paragraph.class));
    }

    @Test
    public void delete_after() {
        List<ContentWithCredentials> content = Lists.newArrayList();
        content.add(new ContentWithCredentials(MNTNER_DEV_MNT + "\ndelete: reason"));

        final List<Update> updates = subject.parse(updateContext, content);
        assertThat(updates, hasSize(1));

        final Update update = updates.get(0);
        assertThat(update.getOperation(), is(Operation.DELETE));
        assertThat(update.getDeleteReasons(), contains(new String[] {"reason"}));
        assertFalse(update.isOverride());
        assertThat(update.getSubmittedObject(), is(RpslObject.parse(MNTNER_DEV_MNT)));

        verify(updateContext, never()).ignore(any(Paragraph.class));
    }

    @Test
    public void delete_middle() {
        List<ContentWithCredentials> content = Lists.newArrayList();
        content.add(new ContentWithCredentials(
                "mntner: UPD-MNT\n" +
                "descr: description\n" +
                "admin-c: TEST-RIPE\n" +
                "mnt-by: UPD-MNT\n" +
                "referral-by: ADMIN-MNT\n" +
                "delete: reason\n" +
                "upd-to: dbtest@ripe.net\n" +
                "auth:   MD5-PW $1$fU9ZMQN9$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "changed: dbtest@ripe.net 20120707\n" +
                "source: TEST\n"));

        final List<Update> updates = subject.parse(updateContext, content);

        assertThat(updates, hasSize(1));
        final Update update = updates.get(0);
        assertThat(update.getOperation(), is(Operation.DELETE));
        assertThat(update.getDeleteReasons(), contains(new String[] {"reason"}));
        assertFalse(update.isOverride());
        assertThat(update.getSubmittedObject(), is(RpslObject.parse("" +
                "mntner: UPD-MNT\n" +
                "descr: description\n" +
                "admin-c: TEST-RIPE\n" +
                "mnt-by: UPD-MNT\n" +
                "referral-by: ADMIN-MNT\n" +
                "upd-to: dbtest@ripe.net\n" +
                "auth:   MD5-PW $1$fU9ZMQN9$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "changed: dbtest@ripe.net 20120707\n" +
                "source: TEST\n")));

        verify(updateContext, never()).ignore(any(Paragraph.class));
    }

    @Test
    public void delete_multiple() {
        List<ContentWithCredentials> content = Lists.newArrayList();
        content.add(new ContentWithCredentials(MNTNER_DEV_MNT + "\ndelete: reason1\ndelete: reason2"));

        final List<Update> updates = subject.parse(updateContext, content);

        assertThat(updates, hasSize(1));
        final Update update = updates.get(0);
        assertThat(update.getOperation(), is(Operation.DELETE));
        assertThat(update.getDeleteReasons(), contains("reason1", "reason2"));
        assertFalse(update.isOverride());
        assertThat(update.getSubmittedObject(), is(RpslObject.parse(MNTNER_DEV_MNT)));
    }

    @Test
    public void broken_override_and_delete_multiple() {
        final String input = MNTNER_DEV_MNT + "\n delete: reason\n override: pw";
        List<ContentWithCredentials> content = Lists.newArrayList();
        content.add(new ContentWithCredentials(input));

        final List<Update> updates = subject.parse(updateContext, content);

        assertThat(updates, hasSize(1));
        final Update update = updates.get(0);
        assertThat(update.getOperation(), is(Operation.UNSPECIFIED));
        assertThat(update.getDeleteReasons(), hasSize(0));
        assertFalse(update.isOverride());
        assertThat(update.getSubmittedObject(), is(RpslObject.parse(input)));
    }

    @Test
    public void no_object() {
        List<ContentWithCredentials> content = Lists.newArrayList();
        content.add(new ContentWithCredentials("some text"));

        final List<Update> updates = subject.parse(updateContext, content);
        assertThat(updates, hasSize(0));

        verify(updateContext, times(1)).ignore(any(Paragraph.class));
    }

    @Test
    public void invalid_object() {
        List<ContentWithCredentials> content = Lists.newArrayList();
        content.add(new ContentWithCredentials("1234:5678"));

        final List<Update> updates = subject.parse(updateContext, content);
        assertThat(updates, hasSize(0));

        verify(updateContext, times(1)).ignore(any(Paragraph.class));
    }

    @Test
    public void no_source() {
        List<ContentWithCredentials> content = Lists.newArrayList();
        content.add(new ContentWithCredentials("mntner: DEV-MNT"));

        final List<Update> updates = subject.parse(updateContext, content);

        assertThat(updates, hasSize(0));
        verify(updateContext).ignore(any(Paragraph.class));
    }
}
