package net.ripe.db.whois.update.handler.validator.common;

import net.ripe.db.whois.common.rpsl.ObjectMessages;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import static net.ripe.db.whois.update.handler.validator.ValidatorTestHelper.validateUpdate;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
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
}
