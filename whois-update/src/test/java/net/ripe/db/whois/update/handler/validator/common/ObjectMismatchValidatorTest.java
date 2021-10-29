package net.ripe.db.whois.update.handler.validator.common;

import net.ripe.db.whois.common.rpsl.ObjectMessages;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.ripe.db.whois.update.handler.validator.ValidatorTestHelper.validateDelete;
import static net.ripe.db.whois.update.handler.validator.ValidatorTestHelper.validateUpdate;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(MockitoExtension.class)
public class ObjectMismatchValidatorTest {
    public static final RpslObject OBJECT = RpslObject.parse("mntner: foo");

    @InjectMocks ObjectMismatchValidator subject;

    @Test
    public void getActions() {
        assertThat(subject.getActions(), containsInAnyOrder(Action.DELETE));
    }

    @Test
    public void no_original_object() {
        final ObjectMessages messages = validateUpdate(subject, null, OBJECT);
        assertThat(messages.getMessages().getAllMessages(), hasSize(0));
    }

    @Test
    public void not_identical() {
        final RpslObject updatedObject = RpslObject.parse("mntner: foo2");
        final ObjectMessages messages = validateUpdate(subject, OBJECT, updatedObject);

        assertThat(messages.getMessages().getAllMessages(), contains(UpdateMessages.objectMismatch(String.format("[%s] %s", updatedObject.getType().getName(), updatedObject.getKey()))));
    }

    @Test
    public void identical() {
        final ObjectMessages messages = validateUpdate(subject, OBJECT, OBJECT);

        assertThat(messages.getMessages().getAllMessages(), hasSize(0));
    }

    private static final RpslObject TIMESTAMPED_OBJECT = RpslObject.parse("" +
            "mntner: foo\n"+
            "created:2011-01-26T10:12:13Z\n" +
            "last-modified:2012-02-27T10:12:13Z\n");

    private static final RpslObject SAME_TIMESTAMPED_OBJECT_WITH_OTHER_TIMESTAMPS = RpslObject.parse("" +
            "mntner: foo\n"+
            "created:2010-01-26T10:12:13Z\n" +
            "last-modified:2013-02-27T10:12:13Z\n");

    @Test
    public void modify_ignore_timestamps_identical() {
        final ObjectMessages messages = validateUpdate(subject, TIMESTAMPED_OBJECT, SAME_TIMESTAMPED_OBJECT_WITH_OTHER_TIMESTAMPS);
        assertThat(messages.getMessages().getAllMessages(), hasSize(0));
    }

    @Test
    public void delete_ignore_timestamps_identical() {
        final ObjectMessages messages = validateDelete(subject, TIMESTAMPED_OBJECT, SAME_TIMESTAMPED_OBJECT_WITH_OTHER_TIMESTAMPS);
        assertThat(messages.getMessages().getAllMessages(), hasSize(0));
    }

    @Test
    public void delete_ignore_does_not_ignore_status_attribute() {
        final RpslObject referenced = RpslObject.parse("" +
                "aut-num: foo\n" +
                "status: ASSIGNED\n");

        final RpslObject updated = RpslObject.parse("" +
                "aut-num: foo\n" +
                "status: LEGACY\n");

        final ObjectMessages messages = validateDelete(subject, referenced, updated);
        assertThat(messages.getMessages().getAllMessages(), hasSize(1));
    }

    @Test
    public void delete_ignore_does_not_ignore_sponsoring_org_attribute() {
        final RpslObject referenced = RpslObject.parse("" +
                "aut-num: foo\n" +
                "sponsoring-org: ab\n");

        final RpslObject updated = RpslObject.parse("" +
                "aut-num: foo\n" +
                "sponsoring-org: cd\n");

        final ObjectMessages messages = validateDelete(subject, referenced, updated);
        assertThat(messages.getMessages().getAllMessages(), hasSize(1));
    }

    @Test
    public void delete_ignore_does_not_ignore_other_generated_attributes() {
        final RpslObject referenced = RpslObject.parse("" +
                "aut-num: foo\n" +
                "fingerpr: a\n" +
                "owner: b\n" +
                "method: c\n");

        final RpslObject updated = RpslObject.parse("" +
                "aut-num: foo\n");

        final ObjectMessages messages = validateDelete(subject, referenced, updated);
        assertThat(messages.getMessages().getAllMessages(), hasSize(0));
    }

}
