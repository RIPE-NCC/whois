package net.ripe.db.whois.update.generator;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.FormatHelper;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.common.rpsl.ValidationMessages;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

@Component
public class TimestampAttributeGenerator extends AttributeGenerator {
    private final DateTimeProvider dateTimeProvider;
    //TODO [TP]: remove defensive code checks wher we check whether timestamp attributes are in original object.

    @Autowired
    TimestampAttributeGenerator(final DateTimeProvider dateTimeProvider) {
        this.dateTimeProvider = dateTimeProvider;
    }

    @Override
    public RpslObject generateAttributes(final RpslObject originalObject, final RpslObject updatedObject, final Update update, final UpdateContext updateContext) {
        final Action action = updateContext.getAction(update);
        if (action == Action.CREATE || action == Action.MODIFY || action == Action.DELETE) {
            final RpslObjectBuilder builder = new RpslObjectBuilder(updatedObject);

            boolean addWarningsFlag = !updateContext.getPreparedUpdate(update).getOverrideOptions().isSkipLastModified();

            generateTimestampAttributes(builder, originalObject, updatedObject, update, updateContext, addWarningsFlag);
            return builder.get();
        }

        return updatedObject;
    }

    private void generateTimestampAttributes(final RpslObjectBuilder builder, final RpslObject originalObject, final RpslObject updatedObject, final Update update, final UpdateContext updateContext, final boolean addWarningsFlag) {
        final Action action = updateContext.getAction(update);
        final String currentDateTime = FormatHelper.dateTimeToUtcString(dateTimeProvider.getCurrentZonedDateTime());

        RpslAttribute generatedCreatedAttribute = null;
        RpslAttribute generatedLastModifiedAttribute = null;

        switch (action) {
            case CREATE:
                generatedCreatedAttribute = new RpslAttribute(AttributeType.CREATED, currentDateTime);
                generatedLastModifiedAttribute = new RpslAttribute(AttributeType.LAST_MODIFIED, currentDateTime);
                break;

            case MODIFY:
                if (originalObject.containsAttribute(AttributeType.CREATED)) {
                    generatedCreatedAttribute = new RpslAttribute(AttributeType.CREATED, originalObject.getValueForAttribute(AttributeType.CREATED));
                }

                if (updateContext.getPreparedUpdate(update).getOverrideOptions().isSkipLastModified()) {
                    if (originalObject.containsAttribute(AttributeType.LAST_MODIFIED)) {
                        generatedLastModifiedAttribute = new RpslAttribute(AttributeType.LAST_MODIFIED, originalObject.getValueForAttribute(AttributeType.LAST_MODIFIED));
                    }
                } else {
                    generatedLastModifiedAttribute = new RpslAttribute(AttributeType.LAST_MODIFIED, currentDateTime);
                }
                break;

            case DELETE:
//                 for delete we just ignore what was passed in and make sure object looks like stored version
                if (originalObject.containsAttribute(AttributeType.CREATED)) {
                    generatedCreatedAttribute = new RpslAttribute(AttributeType.CREATED, originalObject.getValueForAttribute(AttributeType.CREATED));
                }
                if (originalObject.containsAttribute(AttributeType.LAST_MODIFIED)) {
                    generatedLastModifiedAttribute = new RpslAttribute(AttributeType.LAST_MODIFIED, originalObject.getValueForAttribute(AttributeType.LAST_MODIFIED));
                }
                break;

            case NOOP:
                break;

        }

        warnAndAdd(update, updateContext, builder, updatedObject, AttributeType.CREATED, generatedCreatedAttribute, addWarningsFlag);
        warnAndAdd(update, updateContext, builder, updatedObject, AttributeType.LAST_MODIFIED, generatedLastModifiedAttribute, addWarningsFlag);

    }

    private void warnAndAdd(final Update update, final UpdateContext updateContext, final RpslObjectBuilder builder, final RpslObject updatedObject, final AttributeType attributeType, @Nullable final RpslAttribute generatedAttribute, final boolean addWarningsFlag) {

        if (generatedAttribute != null) {
            if (updatedObject.containsAttribute(attributeType)){
                if(updatedObject.findAttributes(attributeType).size() == 1) {
                    builder.replaceAttribute(updatedObject.findAttribute(attributeType), generatedAttribute);
                } else {
                    builder.removeAttributeType(attributeType);
                    builder.addAttributeSorted(generatedAttribute);
                }
            } else {
                builder.addAttributeSorted(generatedAttribute);
            }
        }

        if (addWarningsFlag && updatedObject.containsAttribute(attributeType)) {
            for (RpslAttribute inputAttribute : updatedObject.findAttributes(attributeType)) {
                if (!inputAttribute.equals(generatedAttribute)) {
                    if (generatedAttribute != null) {
                        updateContext.addMessage(update, generatedAttribute, ValidationMessages.suppliedAttributeReplacedWithGeneratedValue(attributeType));
                    } else {
                        updateContext.addMessage(update, ValidationMessages.suppliedAttributeReplacedWithGeneratedValue(attributeType));
                    }
                }
            }
        }
    }

}
