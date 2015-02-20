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
import org.joda.time.LocalDateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TimestampAttributeGenerator extends AttributeGenerator {
    private final DateTimeProvider dateTimeProvider;

    @Autowired
    TimestampAttributeGenerator( final DateTimeProvider dateTimeProvider ) {
        this.dateTimeProvider = dateTimeProvider;
    }

    @Override
    public RpslObject generateAttributes(final RpslObject originalObject, final RpslObject updatedObject, final Update update, final UpdateContext updateContext) {
        RpslObjectBuilder builder = new RpslObjectBuilder(updatedObject);

        Action action = updateContext.getAction(update);
        if (action == Action.CREATE || action == Action.MODIFY || action == Action.DELETE) {
            if( updatedObject.containsAttribute(AttributeType.CREATED)) {
                updateContext.addMessage(update, ValidationMessages.suppliedAttributeReplacedWithGeneratedValue(AttributeType.CREATED));
                builder.removeAttributeType(AttributeType.CREATED);
            }
            if( updatedObject.containsAttribute(AttributeType.LAST_MODIFIED)) {
                updateContext.addMessage(update, ValidationMessages.suppliedAttributeReplacedWithGeneratedValue(AttributeType.LAST_MODIFIED));
                builder.removeAttributeType(AttributeType.LAST_MODIFIED);
            }
        }

        if (action == Action.CREATE || action == Action.MODIFY ) {
            DateTime now = dateTimeProvider.getCurrentUtcTime();
            String timestampString = now.toString(ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC));

            if (updateContext.getAction(update) == Action.CREATE) {
                builder.addAttributeSorted(new RpslAttribute(AttributeType.CREATED, timestampString));
            }
            builder.addAttributeSorted(new RpslAttribute(AttributeType.LAST_MODIFIED, timestampString));
        }

        return builder.get();
    }

}
