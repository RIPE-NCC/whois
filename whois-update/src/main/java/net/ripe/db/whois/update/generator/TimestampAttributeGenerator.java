package net.ripe.db.whois.update.generator;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
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
        RpslObject adjusted = updatedObject;

        if (updateContext.getAction(update) == Action.CREATE || updateContext.getAction(update) == Action.MODIFY) {
            DateTime now = dateTimeProvider.getCurrentUtcTime();
            String timestampString = now.toString(ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC));

            if (updateContext.getAction(update) == Action.CREATE) {
                adjusted = cleanupAttributeType(update, updateContext, updatedObject, AttributeType.CREATED, timestampString);
            }

            return cleanupAttributeType(update, updateContext, adjusted, AttributeType.LAST_MODIFIED, timestampString);
        }

        return updatedObject;
    }

}
