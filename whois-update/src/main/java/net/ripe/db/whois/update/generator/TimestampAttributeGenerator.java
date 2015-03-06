package net.ripe.db.whois.update.generator;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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

import java.util.List;
import java.util.Set;

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
        //TODO TP : remove when timestamps always on
        if (timestampsMode.isTimestampsOff()) {
            return updatedObject;
        }

        final RpslObjectBuilder builder = new RpslObjectBuilder(updatedObject);
        List<AttributeType> warnFor = Lists.newArrayList();

        final Action action = updateContext.getAction(update);
        if (action == Action.CREATE || action == Action.MODIFY || action == Action.DELETE) {
            boolean addWarnings = updateContext.getPreparedUpdate(update).getOverrideOptions().isSkipLastModified() ? false : true;
            warnFor = cleanupTimestampAttributesAndGatherWarnings(builder, updatedObject, addWarnings);
            generateTimestampAttributes(builder, originalObject, update, updateContext);
        }

        final RpslObject resultObject = builder.get();
        if (action != Action.DELETE) {
            for (AttributeType attributeType : warnFor) {
                addWarning(attributeType, originalObject, resultObject, update, updateContext);
            }
        }

        return resultObject;
    }

    private void addWarning(final AttributeType attributeType, final RpslObject original, final RpslObject resultObject, final Update update, final UpdateContext updateContext) {
        Set<RpslAttribute> originalAttribute = Sets.newLinkedHashSet();
        if (original != null) {
            originalAttribute = Sets.newLinkedHashSet(original.findAttributes(attributeType));
        }

        final Set<RpslAttribute> updatedAttribute = Sets.newLinkedHashSet(resultObject.findAttributes(attributeType));
        if (!Sets.symmetricDifference(originalAttribute, updatedAttribute).isEmpty()) {
            updateContext.addMessage(update, resultObject.findAttribute(attributeType), ValidationMessages.suppliedAttributeReplacedWithGeneratedValue(attributeType));
        }
    }

    private List<AttributeType> cleanupTimestampAttributesAndGatherWarnings(final RpslObjectBuilder builder, final RpslObject updatedObject, final boolean addWarnings) {
        final List<AttributeType> warnFor = Lists.newArrayList();
        removeAttribute(updatedObject, builder, AttributeType.CREATED, warnFor, addWarnings);
        removeAttribute(updatedObject, builder, AttributeType.LAST_MODIFIED, warnFor, addWarnings);

        return warnFor;
    }

    private void removeAttribute(final RpslObject rpslObject, final RpslObjectBuilder builder, final AttributeType attributeType, final List<AttributeType> warnFor, final boolean addWarnings) {
        if (rpslObject.containsAttribute(attributeType)) {
            builder.removeAttributeType(attributeType);
            if (addWarnings) {
                warnFor.add(attributeType);
            }
        }
    }

    private void generateTimestampAttributes(final RpslObjectBuilder builder, final RpslObject originalObject, final Update update, final UpdateContext updateContext) {
        final Action action = updateContext.getAction(update);

        final DateTime now = dateTimeProvider.getCurrentDateTimeUtc();
        final String nowString = now.toString(ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC));

        switch (action) {

            case CREATE:
                builder.addAttributeSorted(new RpslAttribute(AttributeType.CREATED, nowString));
                builder.addAttributeSorted(new RpslAttribute(AttributeType.LAST_MODIFIED, nowString));
                break;

            case MODIFY:
                if (originalObject.containsAttribute(AttributeType.CREATED)) {
                    builder.addAttributeSorted(new RpslAttribute(AttributeType.CREATED, originalObject.getValueForAttribute(AttributeType.CREATED)));
                }

                if (updateContext.getPreparedUpdate(update).getOverrideOptions().isSkipLastModified()) {
                    if (originalObject.containsAttribute(AttributeType.LAST_MODIFIED)) {
                        builder.addAttributeSorted(new RpslAttribute(AttributeType.LAST_MODIFIED, originalObject.getValueForAttribute(AttributeType.LAST_MODIFIED)));
                    }
                } else {
                    builder.addAttributeSorted(new RpslAttribute(AttributeType.LAST_MODIFIED, nowString));
                }
                break;

            case DELETE:
                // for delete we just ignore what was passed in and make sure object looks like stored version
                if (originalObject.containsAttribute(AttributeType.CREATED)) {
                    builder.addAttributeSorted(new RpslAttribute(AttributeType.CREATED, originalObject.getValueForAttribute(AttributeType.CREATED)));
                }
                if (originalObject.containsAttribute(AttributeType.LAST_MODIFIED)) {
                    builder.addAttributeSorted(new RpslAttribute(AttributeType.LAST_MODIFIED, originalObject.getValueForAttribute(AttributeType.LAST_MODIFIED)));
                }
                break;

            case NOOP:
                break;
        }
    }
}
