package net.ripe.db.whois.update.handler.validator.maintainer;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.ObjectMessages;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
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
public class MaintainerNameValidatorTest {
    @InjectMocks private MaintainerNameValidator subject;

    @Test
    public void getActions() {
        assertThat(subject.getActions(), containsInAnyOrder(Action.CREATE, Action.MODIFY));
    }

    @Test
    public void check_names() {
        for (final CIString invalidName : MaintainerNameValidator.INVALID_NAMES) {
            final RpslObject object = RpslObject.parse("mntner: " + invalidName);
            final ObjectMessages messages = validateUpdate(subject, null, object);

            assertThat(messages.getMessages().getAllMessages(), hasSize(0));
            final RpslAttribute firstAttribute = object.getAttributes().get(0);
            assertThat(messages.getMessages(firstAttribute).getAllMessages(), hasItems(UpdateMessages.reservedNameUsed()));
        }
    }
}
