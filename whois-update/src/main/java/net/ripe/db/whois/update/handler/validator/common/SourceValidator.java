package net.ripe.db.whois.update.handler.validator.common;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static net.ripe.db.whois.common.rpsl.AttributeType.SOURCE;
import static net.ripe.db.whois.common.rpsl.ObjectType.AS_SET;
import static net.ripe.db.whois.common.rpsl.ObjectType.AUT_NUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.ROUTE;
import static net.ripe.db.whois.common.rpsl.ObjectType.ROUTE6;

@Component
public class SourceValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.copyOf(ObjectType.values());

    private static final Set<ObjectType> NON_AUTH_OBJECT_TYPES = ImmutableSet.of(AS_SET, AUT_NUM, ROUTE, ROUTE6);

    private final CIString source;
    private final CIString nonAuthSource;

    @Autowired
    public SourceValidator(@Value("${whois.source}") final String source,
                           @Value("${whois.nonauth.source}") final String nonAuthSource) {
        this.source = ciString(source);
        this.nonAuthSource = ciString(nonAuthSource);
    }

    @Override
    public List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject updatedObject = update.getUpdatedObject();

        final CIString source = updatedObject.getValueForAttribute(SOURCE);

        if (!(source.equals(this.source) || source.equals(this.nonAuthSource))) {
            updateContext.addMessage(update, UpdateMessages.unrecognizedSource(source.toUpperCase()));
        }

        if (!NON_AUTH_OBJECT_TYPES.contains(updatedObject.getType())) {
            if (source.equals(this.nonAuthSource)) {
                return Arrays.asList(UpdateMessages.sourceNotAllowed(updatedObject.getType(), source));
            }
        }

        return Collections.emptyList();
    }

    @Override
    public ImmutableList<Action> getActions() {
        return ACTIONS;
    }

    @Override
    public ImmutableList<ObjectType> getTypes() {
        return TYPES;
    }
}
