package net.ripe.db.whois.update.handler.validator;

import net.ripe.db.whois.common.rpsl.ObjectMessages;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.*;
import net.ripe.db.whois.update.log.LoggerContext;

import static org.mockito.Mockito.mock;

public final class ValidatorTestHelper {
    private ValidatorTestHelper() {
    }

    public static ObjectMessages validateUpdate(final BusinessRuleValidator subject, final RpslObject originalObject, final RpslObject updatedObject) {
        final Update update = new Update(new Paragraph(updatedObject.toString()), Operation.UNSPECIFIED, null, updatedObject);

        final PreparedUpdate preparedUpdate = new PreparedUpdate(update, originalObject, updatedObject, Action.MODIFY);
        final UpdateContext updateContext = new UpdateContext(mock(LoggerContext.class));

        subject.validate(preparedUpdate, updateContext);

        return updateContext.getMessages(preparedUpdate);
    }
}
