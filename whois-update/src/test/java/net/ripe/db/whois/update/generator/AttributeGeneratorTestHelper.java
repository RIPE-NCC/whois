package net.ripe.db.whois.update.generator;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.update.domain.UpdateContainer;
import net.ripe.db.whois.update.domain.UpdateContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AttributeGeneratorTestHelper {

    final private UpdateContext updateContext;
    final private UpdateContainer update;

    public AttributeGeneratorTestHelper(final UpdateContext updateContext, final UpdateContainer update) {
        this.updateContext = updateContext;
        this.update = update;
    }

    public void validateMessages(final Message... messages) {

        if (messages.length == 0) {
            verify(updateContext, never()).addMessage(any(UpdateContainer.class), any(Message.class));
        } else {
            verify(updateContext, times(messages.length)).addMessage(any(UpdateContainer.class), any(Message.class));
            for (final Message message : messages) {
                verify(updateContext).addMessage(update, message);
            }
        }
    }

    public void assertNoMessages() {
        validateMessages();
    }

    public void assertAttributeMessage(final RpslAttribute rpslAttribute, final Message message) {
        assertAttributeMessage(rpslAttribute, message, 1);
    }

    public void assertAttributeMessage(final RpslAttribute rpslAttribute, final Message message, final int times) {
        verify(updateContext, times(times)).addMessage(update, rpslAttribute, message);
    }

    public void assertAttributeMessage(final Message message) {
        verify(updateContext, times(1)).addMessage(update, message);
    }
}
