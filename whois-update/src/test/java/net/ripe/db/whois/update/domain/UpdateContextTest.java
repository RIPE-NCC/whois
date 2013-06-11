package net.ripe.db.whois.update.domain;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.log.LoggerContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UpdateContextTest {
    private static final String MAINTAINER = "" +
            "mntner:          DEV-MNT\n" +
            "descr:           DEV maintainer\n" +
            "admin-c:         VM1-DEV\n" +
            "tech-c:          VM1-DEV\n" +
            "upd-to:          v.m@example.net\n" +
            "mnt-nfy:         auto@example.net\n" +
            "auth:            MD5-PW $1$q8Su3Hq/$rJt5M3TNLeRE4UoCh5bSH/\n" +
            "remarks:         password: secret\n" +
            "mnt-by:          DEV-MNT\n" +
            "referral-by:     DEV-MNT\n" +
            "changed:         BECHA@example.net 20101010\n" +
            "source:          DEV\n";

    @Mock Update update;
    @Mock LoggerContext loggerContext;
    @InjectMocks UpdateContext subject;

    @Before
    public void setUp() throws Exception {
        when(update.getUpdate()).thenReturn(update);
    }

    @Test
    public void no_warnings() {
        assertThat(subject.getGlobalMessages().getAllMessages(), hasSize(0));
    }

    @Test
    public void warnings() {
        final Message warning1 = new Message(Messages.Type.WARNING, "1");
        final Message warning2 = new Message(Messages.Type.WARNING, "2");

        subject.addGlobalMessage(warning1);
        subject.addGlobalMessage(warning2);
        assertThat(subject.getGlobalMessages().getAllMessages(), contains(warning1, warning2));
    }

    @Test
    public void no_errors() {
        final RpslObject mntner = RpslObject.parse(MAINTAINER);
        final Update update = new Update(new Paragraph(MAINTAINER), Operation.DELETE, Lists.<String>newArrayList(), mntner);
        final PreparedUpdate preparedUpdate = new PreparedUpdate(update, null, mntner, Action.DELETE);

        assertThat(subject.hasErrors(preparedUpdate), is(false));
    }

    @Test
    public void errors() {
        final String content = "mntner: DEV-ROOT-MNT";
        final RpslObject mntner = RpslObject.parse(content);
        final Update update = new Update(new Paragraph(content), Operation.DELETE, Lists.<String>newArrayList(), mntner);
        final PreparedUpdate preparedUpdate = new PreparedUpdate(update, null, mntner, Action.DELETE);

        subject.addMessage(preparedUpdate, UpdateMessages.objectNotFound("DEV-ROOT-MNT"));
        assertThat(subject.hasErrors(preparedUpdate), is(true));
    }

    @Test
    public void status() {
        final String content = "mntner: DEV-ROOT-MNT";
        final RpslObject mntner = RpslObject.parse(content);
        final Update update = new Update(new Paragraph(content), Operation.DELETE, Lists.<String>newArrayList(), mntner);
        final PreparedUpdate preparedUpdate = new PreparedUpdate(update, null, mntner, Action.DELETE);

        subject.status(preparedUpdate, UpdateStatus.FAILED_AUTHENTICATION);
        assertThat(subject.getStatus(preparedUpdate), is(UpdateStatus.FAILED_AUTHENTICATION));
    }

    @Test
    public void createAck() {
        final RpslObject object = RpslObject.parse(MAINTAINER);

        final Update delete = new Update(new Paragraph(MAINTAINER), Operation.DELETE, Lists.<String>newArrayList(), object);
        subject.setAction(delete, Action.DELETE);

        final Update deleteWithError = new Update(new Paragraph(MAINTAINER), Operation.DELETE, Lists.<String>newArrayList(), object);
        subject.setAction(deleteWithError, Action.DELETE);
        subject.addMessage(deleteWithError, UpdateMessages.objectInUse(object));
        subject.addMessage(deleteWithError, UpdateMessages.filteredNotAllowed());
        subject.status(deleteWithError, UpdateStatus.FAILED);

        final Update update = new Update(new Paragraph(MAINTAINER), Operation.UNSPECIFIED, Lists.<String>newArrayList(), object);
        subject.setAction(update, Action.MODIFY);

        final Update updateWithError = new Update(new Paragraph(MAINTAINER), Operation.UNSPECIFIED, Lists.<String>newArrayList(), object);
        subject.setAction(updateWithError, Action.MODIFY);
        subject.addMessage(updateWithError, UpdateMessages.filteredNotAllowed());
        subject.status(updateWithError, UpdateStatus.FAILED);

        final String updateMessage = "ignore";
        final Paragraph paragraphIgnore = new Paragraph(updateMessage);
        subject.ignore(paragraphIgnore);

        final Ack ack = subject.createAck();
        assertThat(ack.getNrFound(), is(4));
        assertThat(ack.getNrProcessedsuccessfully(), is(2));
        assertThat(ack.getNrDelete(), is(1));
        assertThat(ack.getNrProcessedErrrors(), is(2));
        assertThat(ack.getNrDeleteErrors(), is(1));
        assertThat(ack.getIgnoredParagraphs(), contains(paragraphIgnore));
        assertThat(ack.getUpdateStatus(), is(UpdateStatus.FAILED));

        final List<UpdateResult> failedUpdates = ack.getFailedUpdates();
        assertThat(failedUpdates, hasSize(2));

        final Iterable<Message> errors = failedUpdates.get(0).getErrors();
        assertThat(errors, contains(UpdateMessages.objectInUse(object), UpdateMessages.filteredNotAllowed()));
    }

    @Test
    public void generated_nic_handle() {
        final AutoKey nicHandle = NicHandle.parse("DW1-RIPE", ciString("RIPE"), Collections.<CIString>emptySet());

        subject.addGeneratedKey(update, ciString("AUTO-1"), new GeneratedKey(RpslObject.parse(MAINTAINER), nicHandle));
        assertThat(subject.getGeneratedKey(ciString("auto-1")).getAutoKey(), is(nicHandle));
        assertNull(subject.getGeneratedKey(ciString("AUTO-2")));
    }
}
