package net.ripe.db.whois.update.handler.validator.common;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.domain.attrs.AttributeParseException;
import net.ripe.db.whois.common.domain.attrs.Changed;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChangedAttributeValidator implements BusinessRuleValidator {
    private static final LocalDate EARLIEST_CHANGED_DATE = new LocalDate(1984, 1, 1);

    @Override
    public List<Action> getActions() {
        return Lists.newArrayList(Action.CREATE, Action.MODIFY);
    }

    @Override
    public List<ObjectType> getTypes() {
        return Lists.newArrayList(ObjectType.values());
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        int missing = 0;
        List<LocalDate> localDateOrder = Lists.newArrayList();
        for (RpslAttribute attribute : update.getUpdatedObject().findAttributes(AttributeType.CHANGED)) {
            try {
                final Changed changed = Changed.parse(attribute.getCleanValue());
                final LocalDate date = changed.getDate();
                if (date == null) {
                    missing++;
                } else {
                    localDateOrder.add(date);
                    if (date.isBefore(EARLIEST_CHANGED_DATE)) {
                        updateContext.addMessage(update, attribute, UpdateMessages.invalidDate(changed.getDateString()));
                    }
                    if (dateIsMoreThanOneDayInTheFuture(date)) {
                        updateContext.addMessage(update, attribute, UpdateMessages.dateTooFuturistic(changed.getDateString()));
                    }
                }
            } catch (AttributeParseException ignored) {
                updateContext.addMessage(update, attribute, UpdateMessages.invalidDateFormat());
            }
        }

        if (missing > 1) {
            updateContext.addMessage(update, UpdateMessages.multipleMissingChangeDates());
        }
    }

    private boolean dateIsMoreThanOneDayInTheFuture(final LocalDate toCheck) {
        final LocalDate today = new LocalDate();
        return toCheck.isAfter(today.plusDays(1));
    }
}
