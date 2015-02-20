package net.ripe.db.whois.update.generator;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.common.rpsl.ValidationMessages;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static net.ripe.db.whois.common.rpsl.AttributeType.CREATED;
import static net.ripe.db.whois.common.rpsl.AttributeType.LAST_MODIFIED;
import static net.ripe.db.whois.update.domain.Action.CREATE;
import static net.ripe.db.whois.update.domain.Action.DELETE;
import static net.ripe.db.whois.update.domain.Action.MODIFY;

@Component
public class TimestampAttributeGenerator extends AttributeGenerator {
    private final DateTimeProvider dateTimeProvider;

    @Autowired
    TimestampAttributeGenerator( final DateTimeProvider dateTimeProvider ) {
        this.dateTimeProvider = dateTimeProvider;
    }

    @Override
    public RpslObject generateAttributes(final RpslObject originalObject, final RpslObject updatedObject, final Update update, final UpdateContext updateContext) {
        final RpslObjectBuilder builder = new RpslObjectBuilder(updatedObject);

        final Action action = updateContext.getAction(update);

        if (action == CREATE || action == MODIFY || action == DELETE) {
            if( updatedObject.containsAttribute(AttributeType.CREATED)) {
                updateContext.addMessage(update, ValidationMessages.suppliedAttributeReplacedWithGeneratedValue(CREATED));
                builder.removeAttributeType(CREATED);
            }
            if( updatedObject.containsAttribute(LAST_MODIFIED)) {
                updateContext.addMessage(update, ValidationMessages.suppliedAttributeReplacedWithGeneratedValue(LAST_MODIFIED));
                builder.removeAttributeType(LAST_MODIFIED);
            }
        }

        if (action == CREATE || action == MODIFY ) {
            final DateTime now = dateTimeProvider.getCurrentUtcTime();
            final String timestampString = now.toString(ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC));

            if (updateContext.getAction(update) == CREATE) {
                builder.addAttributeSorted(new RpslAttribute(CREATED, timestampString));
            }
            builder.addAttributeSorted(new RpslAttribute(LAST_MODIFIED, timestampString));
        }

        return builder.get();
    }

}
