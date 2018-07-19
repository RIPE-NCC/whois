package net.ripe.db.whois.update.handler.validator.route;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.AutNum;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.apache.commons.lang.math.LongRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OriginValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.ROUTE, ObjectType.ROUTE6);

    private final List<LongRange> reservedAsnumbers;

    private final RpslObjectDao rpslObjectDao;

    @Autowired
    public OriginValidator(final RpslObjectDao rpslObjectDao,
                           @Value("${whois.reserved.as.numbers}") final String reservedAsNumbers) {
        this.rpslObjectDao = rpslObjectDao;
        this.reservedAsnumbers = parseReservedAsNumbers(reservedAsNumbers);
    }

    @Override
    public ImmutableList<Action> getActions() {
        return ACTIONS;
    }

    @Override
    public ImmutableList<ObjectType> getTypes() {
        return TYPES;
    }

    @Override
    public void validate(PreparedUpdate update, UpdateContext updateContext) {
        final RpslObject updatedObject = update.getUpdatedObject();
        if (updatedObject == null) {
            return;
        }

        AutNum autnum = AutNum.parse(updatedObject.getValueForAttribute(AttributeType.ORIGIN));

        if (isReservedAsNumber(autnum.getValue())) {
            updateContext.addMessage(update, UpdateMessages.cannotUseReservedAsNumber(autnum.getValue()));
        } else if (rpslObjectDao.findByKeyOrNull(ObjectType.AUT_NUM, updatedObject.getValueForAttribute(AttributeType.ORIGIN)) == null) {
            updateContext.addMessage(update, UpdateMessages.autnumNotFound(autnum.getValue()));
        }
    }

    private boolean isReservedAsNumber(Long asn) {
        for (LongRange range : this.reservedAsnumbers) {
            if (range.containsLong(asn)) {
                return true;
            }
            if (asn < range.getMinimumLong()) {
                break;
            }
        }
        return false;
    }

    private List<LongRange> parseReservedAsNumbers(final String reservedAsNumbers) {
        final List<LongRange> parsedAsNumbers = Lists.newArrayList();

        for (String reservedAsNumber : reservedAsNumbers.split(",")) {
            if (reservedAsNumber.contains("-")) {
                String[] startEnd = reservedAsNumber.split("-");
                parsedAsNumbers.add(new LongRange(Long.parseLong(startEnd[0]), Long.parseLong(startEnd[1])));
            } else {
                parsedAsNumbers.add(new LongRange(Long.parseLong(reservedAsNumber), Long.parseLong(reservedAsNumber)));
            }
        }

        return parsedAsNumbers;
    }
}
