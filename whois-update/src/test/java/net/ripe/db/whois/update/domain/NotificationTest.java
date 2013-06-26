package net.ripe.db.whois.update.domain;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NotificationTest {
    @Mock Update update;
    @Mock UpdateContext updateContext;
    Notification subject;

    @Before
    public void setUp() throws Exception {
        subject = new Notification("test@me.now");
    }

    @Test
    public void getEmail() {
        assertThat(subject.getEmail(), is("test@me.now"));
    }

    @Test
    public void getUpdates_empty() {
        for (final Notification.Type type : Notification.Type.values()) {
            assertThat(subject.getUpdates(type), hasSize(0));
        }
    }

    @Test
    public void has_empty() {
        for (final Notification.Type type : Notification.Type.values()) {
            assertThat(subject.has(type), is(false));
        }
    }

    @Test
    public void add_created() {
        RpslObject created = RpslObject.parse("mntner: DEV-MNT");
        when(updateContext.getPersistedUpdate(any(UpdateContainer.class))).thenReturn(created);

        final PreparedUpdate preparedUpdate = new PreparedUpdate(update, null, created, Action.CREATE);
        subject.add(Notification.Type.SUCCESS, preparedUpdate, updateContext);

        assertThat(subject.getUpdates(Notification.Type.SUCCESS_REFERENCE), hasSize(0));
        assertThat(subject.has(Notification.Type.SUCCESS_REFERENCE), is(false));

        assertThat(subject.has(Notification.Type.SUCCESS), is(true));

        final Set<Notification.Update> updates = subject.getUpdates(Notification.Type.SUCCESS);
        assertThat(updates, hasSize(1));

        final Notification.Update create = updates.iterator().next();
        assertThat(create.getAction(), is("CREATE"));
        assertThat(create.getResult(), is("CREATED"));
        assertThat(create.isReplacement(), is(false));
        assertThat(create.getReferenceObject(), is(created));
        assertThat(create.getUpdatedObject(), is(created));
        assertThat(create.getReason(), is(""));
    }

    @Test
    public void add_modified() {
        RpslObject original = RpslObject.parse("mntner: DEV-MNT");
        RpslObject modified = RpslObject.parse("mntner: DEV-MNT\ndescr: some description");

        final PreparedUpdate preparedUpdate = new PreparedUpdate(update, original, modified, Action.MODIFY);
        subject.add(Notification.Type.SUCCESS_REFERENCE, preparedUpdate, updateContext);

        final Set<Notification.Update> updates = subject.getUpdates(Notification.Type.SUCCESS_REFERENCE);
        assertThat(updates, hasSize(1));

        final Notification.Update create = updates.iterator().next();
        assertThat(create.getAction(), is("MODIFY"));
        assertThat(create.getResult(), is("MODIFIED"));
        assertThat(create.isReplacement(), is(true));
        assertThat(create.getReferenceObject(), is(original));
        assertThat(create.getUpdatedObject(), is(modified));
        assertThat(create.getReason(), is(""));
    }

    @Test
    public void add_delete() {
        RpslObject original = RpslObject.parse("mntner: DEV-MNT");

        final PreparedUpdate preparedUpdate = new PreparedUpdate(update, original, original, Action.DELETE);
        when(update.getDeleteReasons()).thenReturn(Lists.newArrayList("reason1", "reason2"));
        subject.add(Notification.Type.FAILED_AUTHENTICATION, preparedUpdate, updateContext);

        final Set<Notification.Update> updates = subject.getUpdates(Notification.Type.FAILED_AUTHENTICATION);
        assertThat(updates, hasSize(1));

        final Notification.Update create = updates.iterator().next();
        assertThat(create.getAction(), is("DELETE"));
        assertThat(create.getResult(), is("DELETED"));
        assertThat(create.isReplacement(), is(false));
        assertThat(create.getReferenceObject(), is(original));
        assertThat(create.getUpdatedObject(), is(original));
        assertThat(create.getReason(), is("***Info:    reason1, reason2\n"));
    }

    @Test
    public void add_noop() {
        RpslObject original = RpslObject.parse("mntner: DEV-MNT");
        when(updateContext.getPersistedUpdate(any(UpdateContainer.class))).thenReturn(original);

        final PreparedUpdate preparedUpdate = new PreparedUpdate(update, original, original, Action.NOOP);
        subject.add(Notification.Type.SUCCESS_REFERENCE, preparedUpdate, updateContext);

        final Set<Notification.Update> updates = subject.getUpdates(Notification.Type.SUCCESS_REFERENCE);
        assertThat(updates, hasSize(1));

        final Notification.Update create = updates.iterator().next();
        assertThat(create.getAction(), is("NOOP"));
        assertThat(create.getResult(), is("UNCHANGED"));
        assertThat(create.isReplacement(), is(false));
        assertThat(create.getReferenceObject(), is(original));
        assertThat(create.getUpdatedObject(), is(original));
        assertThat(create.getReason(), is(""));
    }
}
