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
    private static final String BEGINNING_OF_TIMES = "2000-01-01T00:00:00Z";
    private final DateTimeProvider dateTimeProvider;


    @Autowired
    TimestampAttributeGenerator( final DateTimeProvider dateTimeProvider ) {
        this.dateTimeProvider = dateTimeProvider;
    }

    @Override
    public RpslObject generateAttributes(final RpslObject originalObject, final RpslObject updatedObject, final Update update, final UpdateContext updateContext) {
        final RpslObjectBuilder builder = new RpslObjectBuilder(updatedObject);

        final Action action = updateContext.getAction(update);
        if (action == CREATE || action == MODIFY ) {
            cleanupTimestampAttributes(builder, updatedObject, update, updateContext);

            generateTimestampAttributes(builder, originalObject, updatedObject, update, updateContext);

        } else if (action == DELETE) {
            // do nothing for now
        }

        return builder.get();
    }

    private void cleanupTimestampAttributes( final RpslObjectBuilder builder, final RpslObject updatedObject, final Update update, final UpdateContext updateContext ) {
        if( updatedObject.containsAttribute(AttributeType.CREATED)) {
            builder.removeAttributeType(CREATED);
            updateContext.addMessage(update, ValidationMessages.suppliedAttributeReplacedWithGeneratedValue(CREATED));
        }
        if( updatedObject.containsAttribute(LAST_MODIFIED)) {
            builder.removeAttributeType(LAST_MODIFIED);
            updateContext.addMessage(update, ValidationMessages.suppliedAttributeReplacedWithGeneratedValue(LAST_MODIFIED));
        }
    }

    private void generateTimestampAttributes( final RpslObjectBuilder builder, final RpslObject originalObject, final RpslObject updatedObject, final Update update, final UpdateContext updateContext ) {
        final DateTime now = dateTimeProvider.getCurrentUtcTime();
        final String nowString = now.toString(ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC));

        final Action action = updateContext.getAction(update);
        if (action == CREATE) {
            builder.addAttributeSorted(new RpslAttribute(CREATED, nowString));
        } else if( action == MODIFY ) {
            String createdString = BEGINNING_OF_TIMES;
            if( originalObject.containsAttribute(CREATED)) {
                createdString = originalObject.getValueForAttribute(CREATED).toString();
            }
            builder.addAttributeSorted(new RpslAttribute(CREATED, createdString));
        }
        builder.addAttributeSorted(new RpslAttribute(LAST_MODIFIED, nowString));

    }
}