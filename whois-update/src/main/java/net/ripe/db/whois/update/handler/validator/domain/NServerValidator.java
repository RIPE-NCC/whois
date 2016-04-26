package net.ripe.db.whois.update.handler.validator.domain;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.Domain;
import net.ripe.db.whois.common.rpsl.attrs.NServer;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.stereotype.Component;

@Component
public class NServerValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.DOMAIN);

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject updatedObject = update.getUpdatedObject();

        final Domain domain = Domain.parse(updatedObject.getKey());

        for (final RpslAttribute nServerAttribute : updatedObject.findAttributes(AttributeType.NSERVER)) {
            final NServer nServer = NServer.parse(nServerAttribute.getCleanValue());

            switch (domain.getType()) {
                case E164:
                    if (nServer.getIpInterval() != null) {
                        updateContext.addMessage(update, nServerAttribute, UpdateMessages.invalidGlueForEnumDomain(nServer.getIpInterval().toString()));
                    }
                    break;

                case INADDR:
                case IP6:
                    final boolean endsWithDomain = domain.endsWithDomain(nServer.getHostname());

                    if (endsWithDomain && nServer.getIpInterval() == null) {
                        updateContext.addMessage(update, nServerAttribute, UpdateMessages.glueRecordMandatory(domain.getValue()));
                    } else if (!endsWithDomain && nServer.getIpInterval() != null) {
                        updateContext.addMessage(update, nServerAttribute, UpdateMessages.hostNameMustEndWith(domain.getValue()));
                    }
                    break;
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
