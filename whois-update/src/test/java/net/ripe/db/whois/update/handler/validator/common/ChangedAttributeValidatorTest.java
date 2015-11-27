package net.ripe.db.whois.update.handler.validator.common;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectMessages;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.text.SimpleDateFormat;
import java.util.Collection;

import static net.ripe.db.whois.update.handler.validator.ValidatorTestHelper.validateUpdate;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ChangedAttributeValidatorTest {
    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;

    private ChangedAttributeValidator subject = new ChangedAttributeValidator();

    @Test
    public void getActions() {
        assertThat(subject.getActions(), containsInAnyOrder(Action.CREATE, Action.MODIFY));
    }

    @Test
    public void valid() {
        final RpslObject object = RpslObject.parse("mntner: MNT\nchanged: user@host.org 20100101\nchanged: user@host.org 20110113");

        final ObjectMessages messages = validateUpdate(subject, null, object);

        assertThat(messages.getMessages().getAllMessages(), hasSize(0));
    }

    @Test
    public void valid_missing_date() {
        final RpslObject object = RpslObject.parse("mntner: MNT\nchanged: user@host.org\nchanged: user@host.org 20110113");

        final ObjectMessages messages = validateUpdate(subject, null, object);

        assertThat(messages.getMessages().getAllMessages(), hasSize(0));
    }

    @Test
    public void invalid_two_missing_date() {
        final RpslObject object = RpslObject.parse("mntner: MNT\nchanged: user@host.org\nchanged: user@host.org");
        final ObjectMessages messages = validateUpdate(subject, null, object);

        final Collection<Message> allMessages = messages.getMessages().getAllMessages();
        assertThat(allMessages, contains(UpdateMessages.multipleMissingChangeDates()));

    }

    @Test
    public void invalid_date_leapyear() {
        final RpslObject object = RpslObject.parse("mntner: MNT\nchanged: user@host.org 19000229");
        final ObjectMessages messages = validateUpdate(subject, null, object);

        assertThat(messages.getMessages(object.findAttribute(AttributeType.CHANGED)).getAllMessages(), contains(UpdateMessages.invalidDateFormat()));
    }

    @Test
    public void invalid_date() {
        final RpslObject object = RpslObject.parse("mntner: MNT\nchanged: user@host.org 19800505");
        final ObjectMessages messages = validateUpdate(subject, null, object);

        assertThat(messages.getMessages(object.findAttribute(AttributeType.CHANGED)).getAllMessages(), contains(UpdateMessages.invalidDate("19800505")));
    }

    @Test
    public void too_far_in_the_future() {
        final LocalDate tooFarInTheFutureDate = new LocalDate().plusDays(2);
        final String dateString = new SimpleDateFormat("yyyyMMdd").format(tooFarInTheFutureDate.toDate());

        final RpslObject object = RpslObject.parse("mntner: MNT\nchanged: alpha@beta.com " + dateString );
        when(update.getSubmittedObject()).thenReturn(object);

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, object.findAttribute(AttributeType.CHANGED), UpdateMessages.dateTooFuturistic(dateString));
    }

    @Test
    public void not_too_far_in_the_future() {
        final LocalDate tooFarInTheFutureDate = new LocalDate().plusDays(1);
        final String dateString = new SimpleDateFormat("yyyyMMdd").format(tooFarInTheFutureDate.toDate());

        final RpslObject object = RpslObject.parse("mntner: MNT\nchanged: alpha@beta.com " + dateString );
        when(update.getSubmittedObject()).thenReturn(object);

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }
}
