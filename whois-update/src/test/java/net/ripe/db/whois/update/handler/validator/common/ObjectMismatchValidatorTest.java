package net.ripe.db.whois.update.handler.validator.common;

import net.ripe.db.whois.common.rpsl.ObjectMessages;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.TimestampsMode;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static net.ripe.db.whois.update.handler.validator.ValidatorTestHelper.validateDelete;
import static net.ripe.db.whois.update.handler.validator.ValidatorTestHelper.validateUpdate;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ObjectMismatchValidatorTest {
    public static final RpslObject OBJECT = RpslObject.parse("mntner: foo");

    @Mock TimestampsMode timestampsMode;
    @InjectMocks ObjectMismatchValidator subject;

    @Before
    public void setUp() throws Exception {
        subject = new ObjectMismatchValidator(timestampsMode);
    }

    @Test
    public void getActions() {
        assertThat(subject.getActions(), containsInAnyOrder(Action.DELETE));
    }

    @Test
    public void no_original_object() {
        newMode();
        final ObjectMessages messages = validateUpdate(subject, null, OBJECT);
        assertThat(messages.getMessages().getAllMessages(), hasSize(0));
    }

    @Test
    public void not_identical() {
        newMode();
        final RpslObject updatedObject = RpslObject.parse("mntner: foo2");
        final ObjectMessages messages = validateUpdate(subject, OBJECT, updatedObject);

        assertThat(messages.getMessages().getAllMessages(), contains(UpdateMessages.objectMismatch(String.format("[%s] %s", updatedObject.getType().getName(), updatedObject.getKey()))));
    }

    @Test
    public void identical() {
        newMode();
        final ObjectMessages messages = validateUpdate(subject, OBJECT, OBJECT);

        assertThat(messages.getMessages().getAllMessages(), hasSize(0));
    }

    public static final RpslObject TIMESTAMPED_OBJECT = RpslObject.parse("" +
            "mntner: foo\n"+
            "created:2011-01-26T10:12:13\n" +
            "last-modified:2012-02-27T10:12:13\n");

    public static final RpslObject SAME_TIMESTAMPED_OBJECT_WITH_OTHER_TIMESTAMPS = RpslObject.parse("" +
            "mntner: foo\n"+
            "created:2010-01-26T10:12:13\n" +
            "last-modified:2013-02-27T10:12:13\n");

    @Test
    public void modify_old_mode_ignore_timestamps_identical() {
        oldMode();
        final ObjectMessages messages = validateUpdate(subject, TIMESTAMPED_OBJECT, SAME_TIMESTAMPED_OBJECT_WITH_OTHER_TIMESTAMPS);
        assertThat(messages.getMessages().getAllMessages(), hasSize(1));
    }

    @Test
    public void modify_new_mode_ignore_timestamps_identical() {
        newMode();
        final ObjectMessages messages = validateUpdate(subject, TIMESTAMPED_OBJECT, SAME_TIMESTAMPED_OBJECT_WITH_OTHER_TIMESTAMPS);
        assertThat(messages.getMessages().getAllMessages(), hasSize(1));
    }

    @Test
    public void delete_old_mode_ignore_timestamps_identical() {
        oldMode();
        final ObjectMessages messages = validateDelete(subject, TIMESTAMPED_OBJECT, SAME_TIMESTAMPED_OBJECT_WITH_OTHER_TIMESTAMPS);
        assertThat(messages.getMessages().getAllMessages(), hasSize(0));
    }

    @Test
    public void delete_new_mode_ignore_timestamps_identical() {
        newMode();
        final ObjectMessages messages = validateDelete(subject, TIMESTAMPED_OBJECT, SAME_TIMESTAMPED_OBJECT_WITH_OTHER_TIMESTAMPS);
        assertThat(messages.getMessages().getAllMessages(), hasSize(1));
    }

    private void oldMode() {
        when(timestampsMode.isTimestampsOff()).thenReturn(true);
    }

    private void newMode() {
        when(timestampsMode.isTimestampsOff()).thenReturn(false);
    }


}
