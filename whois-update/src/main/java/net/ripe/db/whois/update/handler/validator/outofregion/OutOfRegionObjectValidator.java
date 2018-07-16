package net.ripe.db.whois.update.handler.validator.outofregion;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.grs.AuthoritativeResource;
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

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static net.ripe.db.whois.common.rpsl.ObjectType.AUT_NUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.ROUTE;
import static net.ripe.db.whois.common.rpsl.ObjectType.ROUTE6;

@Component
public class OutOfRegionObjectValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(AUT_NUM, ROUTE, ROUTE6);

    private final AuthoritativeResourceData authoritativeResourceData;

    private final CIString source;
    private final CIString nonAuthSource;

    @Autowired
    public OutOfRegionObjectValidator(final AuthoritativeResourceData authoritativeResourceData,
                                      @Value("${whois.source}") final String source,
                                      @Value("${whois.nonauth.source}") final String nonAuthSource) {
        this.authoritativeResourceData = authoritativeResourceData;
        this.source = ciString(source);
        this.nonAuthSource = ciString(nonAuthSource);
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
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject updatedObject = update.getUpdatedObject();
        if (updatedObject == null) {
            return;
        }

        final AuthoritativeResource authoritativeResource = authoritativeResourceData.getAuthoritativeResource(this.source);
        if (!authoritativeResource.isMaintainedInRirSpace(updatedObject)) {
            if (!(updateContext.getSubject(update).hasPrincipal(Principal.OVERRIDE_MAINTAINER) || updateContext.getSubject(update).hasPrincipal(Principal.RS_MAINTAINER))) {
                updateContext.addMessage(update, UpdateMessages.cannotCreateOutOfRegionObject());
            }
        }
    }

}
