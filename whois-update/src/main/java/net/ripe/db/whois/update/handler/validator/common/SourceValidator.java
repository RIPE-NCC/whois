package net.ripe.db.whois.update.handler.validator.common;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Set;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static net.ripe.db.whois.common.rpsl.AttributeType.SOURCE;
import static net.ripe.db.whois.common.rpsl.ObjectType.AUT_NUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.ROUTE;
import static net.ripe.db.whois.common.rpsl.ObjectType.ROUTE6;

@Component
public class SourceValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.copyOf(ObjectType.values());

    private static final Set<ObjectType> NON_AUTH_OBJECT_TYPES = ImmutableSet.of(AUT_NUM, ROUTE, ROUTE6);

    private final AuthoritativeResourceData authoritativeResourceData;

    private final CIString source;
    private final CIString nonAuthSource;

    @Autowired
    public SourceValidator(final AuthoritativeResourceData authoritativeResourceData,
                           @Value("${whois.source}") final String source,
                           @Value("${whois.nonauth.source}") final String nonAuthSource) {
        this.authoritativeResourceData = authoritativeResourceData;
        this.source = ciString(source);
        this.nonAuthSource = ciString(nonAuthSource);
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject updatedObject = update.getUpdatedObject();

        final CIString source = updatedObject.getValueForAttribute(SOURCE);

        if (!(source.equals(this.source) || source.equals(this.nonAuthSource))) {
            updateContext.addMessage(update, UpdateMessages.unrecognizedSource(source.toUpperCase()));
        }

        if (!NON_AUTH_OBJECT_TYPES.contains(updatedObject.getType())) {
            if (source.equals(this.nonAuthSource)) {
                updateContext.addMessage(update, UpdateMessages.sourceNotAllowed(updatedObject.getType(), source));
            }
        } else {
            boolean outOfRegion = !authoritativeResourceData.getAuthoritativeResource().isMaintainedInRirSpace(updatedObject);
            boolean rsOrOverride = updateContext.getSubject(update).hasPrincipal(Principal.OVERRIDE_MAINTAINER) || updateContext.getSubject(update).hasPrincipal(Principal.RS_MAINTAINER);

            if (outOfRegion && rsOrOverride && this.source.equals(source)) {
                updateContext.addMessage(update, UpdateMessages.wrongOutOfRegionSource(this.nonAuthSource));
            }

            if (!outOfRegion && rsOrOverride && this.nonAuthSource.equals(source)) {
                updateContext.addMessage(update, UpdateMessages.wrongOutOfRegionSource(this.source));
            }
        }
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
