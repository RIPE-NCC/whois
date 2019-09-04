package net.ripe.db.whois.update.handler.validator.route;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.ip.Interval;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

/**
 * Do not allow 'bogon' space to be used when creating a route(6) object.
 *
 * Ref. http://www.team-cymru.com/bogon-reference.html
 */
@Component
public class BogonValidator implements BusinessRuleValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(BogonValidator.class);

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.ROUTE, ObjectType.ROUTE6);

    private final Set<Interval> bogons;

    @Autowired
    public BogonValidator(@Value("${ipranges.bogons:}") final String ... bogons) {
        this.bogons = parseBogonPrefixes(bogons);
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject updatedObject = update.getUpdatedObject();
        if (updatedObject == null) {
            return;
        }

        getPrefix(updatedObject).ifPresent(prefix -> {
            if (isBogon(prefix.toString())) {
                updateContext.addMessage(update, UpdateMessages.bogonPrefixNotAllowed(prefix.toString()));
            }
        });
    }

    @Override
    public ImmutableList<Action> getActions() {
        return ACTIONS;
    }

    @Override
    public ImmutableList<ObjectType> getTypes() {
        return TYPES;
    }

    private Set<Interval> parseBogonPrefixes(final String ... bogons) {
        final Set<Interval> results = Sets.newHashSet();

        for (final String bogon : bogons) {
            try {
                results.add(IpInterval.parse(bogon));
            } catch (IllegalArgumentException e) {
                LOGGER.warn("{} is not a valid prefix, skipping...", bogon);
            }
        }

        return results;
    }

    private boolean isBogon(final String prefix) {
        final Interval interval;
        try {
            interval = IpInterval.parse(prefix);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("{} is not a valid prefix, skipping...", prefix);
            return false;
        }

        for (final Interval bogon : bogons) {
            if (interval.getClass().equals(bogon.getClass()) && bogon.contains(interval)) {
                return true;
            }
        }

        return false;
    }

    private Optional<CIString> getPrefix(final RpslObject rpslObject) {
        switch (rpslObject.getType()) {
            case ROUTE:
                return Optional.of(rpslObject.getValueForAttribute(AttributeType.ROUTE));
            case ROUTE6:
                return Optional.of(rpslObject.getValueForAttribute(AttributeType.ROUTE6));
            default:
                return Optional.empty();
        }
    }

}
