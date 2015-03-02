package net.ripe.db.whois.update.generator;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.common.rpsl.TimestampsMode;
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
    private final TimestampsMode timestampsMode;

    @Autowired
    TimestampAttributeGenerator(final DateTimeProvider dateTimeProvider, final TimestampsMode timestampsMode) {
        this.dateTimeProvider = dateTimeProvider;
        this.timestampsMode = timestampsMode;
    }

    @Override
    public RpslObject generateAttributes(final RpslObject originalObject, final RpslObject updatedObject, final Update update, final UpdateContext updateContext) {

        if (timestampsMode.isTimestampsOff()) {
            return updatedObject;
        }

        final RpslObjectBuilder builder = new RpslObjectBuilder(updatedObject);

        final Action action = updateContext.getAction(update);
        if (action == CREATE || action == MODIFY) {
            cleanupTimestampAttributes(builder, updatedObject, update, updateContext, true);
        } else if (action == DELETE) {
            cleanupTimestampAttributes(builder, updatedObject, update, updateContext, false);
        }

        if (action == CREATE || action == MODIFY || action == DELETE) {
            generateTimestampAttributes(builder, originalObject, update, updateContext);
        }

        return builder.get();
    }

    private void cleanupTimestampAttributes(final RpslObjectBuilder builder, final RpslObject updatedObject, final Update update, final UpdateContext updateContext, final boolean withWarnings) {
        if (updatedObject.containsAttribute(AttributeType.CREATED)) {
            builder.removeAttributeType(CREATED);
            if (withWarnings) {
                updateContext.addMessage(update, ValidationMessages.suppliedAttributeReplacedWithGeneratedValue(CREATED));
            }
        }
        if (updatedObject.containsAttribute(LAST_MODIFIED)) {
            builder.removeAttributeType(LAST_MODIFIED);
            if (withWarnings) {
                updateContext.addMessage(update, ValidationMessages.suppliedAttributeReplacedWithGeneratedValue(LAST_MODIFIED));
            }
        }
    }

    private void generateTimestampAttributes( final RpslObjectBuilder builder, final RpslObject originalObject, final Update update, final UpdateContext updateContext) {
        final Action action = updateContext.getAction(update);

        final DateTime now = dateTimeProvider.getCurrentDateTimeUtc();
        final String nowString = now.toString(ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC));

        switch (action) {

            case CREATE:
                builder.addAttributeSorted(new RpslAttribute(CREATED, nowString));
                builder.addAttributeSorted(new RpslAttribute(LAST_MODIFIED, nowString));
                break;

            case MODIFY:
                if (originalObject.containsAttribute(CREATED)) {
                    builder.addAttributeSorted(new RpslAttribute(CREATED, originalObject.getValueForAttribute(CREATED)));
                }

                if (updateContext.getPreparedUpdate(update).getOverrideOptions().isSkipLastModified()) {
                    if (originalObject.containsAttribute(LAST_MODIFIED)) {
                        builder.addAttributeSorted(new RpslAttribute(LAST_MODIFIED, originalObject.getValueForAttribute(LAST_MODIFIED)));
                    }
                } else {
                    builder.addAttributeSorted(new RpslAttribute(LAST_MODIFIED, nowString));
                }
                break;

            case DELETE:
                // for delete we just ignore what was passed in and make sure object looks like stored version
                if (originalObject.containsAttribute(CREATED)) {
                    builder.addAttributeSorted(new RpslAttribute(CREATED, originalObject.getValueForAttribute(CREATED)));
                }
                if (originalObject.containsAttribute(LAST_MODIFIED)) {
                    builder.addAttributeSorted(new RpslAttribute(LAST_MODIFIED, originalObject.getValueForAttribute(LAST_MODIFIED)));
                }
                break;

            case NOOP:
                break;
        }
    }
}
