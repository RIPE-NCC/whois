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

import static net.ripe.db.whois.common.rpsl.attrs.Domain.Type.INADDR;

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
                {
                    final boolean endsWithDomain = domain.endsWithDomain(nServer.getHostname());

                    if (endsWithDomain && nServer.getIpInterval() == null) {
                        updateContext.addMessage(update, nServerAttribute, UpdateMessages.glueRecordMandatory(domain.getValue()));
                    } else if (!endsWithDomain && nServer.getIpInterval() != null) {
                        updateContext.addMessage(update, nServerAttribute, UpdateMessages.invalidGlueForEnumDomain(nServer.getIpInterval().toString()));
                    }
                    break;
                }
                case INADDR:
                case IP6:
                {
                    final boolean endsWithDomain = domain.endsWithDomain(nServer.getHostname());
                    if (domain.getReverseIp() != null) {
                        validateNserverCorrectPrefixes(domain, update, domain.getType().equals(INADDR),
                                nServerAttribute, updateContext);
                    }
                    if (endsWithDomain && nServer.getIpInterval() == null) {
                        updateContext.addMessage(update, nServerAttribute, UpdateMessages.glueRecordMandatory(domain.getValue()));
                    } else if (!endsWithDomain && nServer.getIpInterval() != null) {
                        updateContext.addMessage(update, nServerAttribute, UpdateMessages.hostNameMustEndWith(domain.getValue()));
                    }
                    break;
                }
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


    private void validateNserverCorrectPrefixes(final Domain domain, final PreparedUpdate update,
                                                final boolean isIpv4, final RpslAttribute nServerAttribute, final UpdateContext updateContext){
        if ("ns.ripe.net".equalsIgnoreCase(nServerAttribute.getValue()) && hasIncorrectPrefixes(domain.getReverseIp().getPrefixLength(),
                isIpv4)){
            updateContext.addMessage(update, nServerAttribute, UpdateMessages.hostNameMustEndWith(domain.getValue()));
        }
    }

    private boolean hasIncorrectPrefixes(final int prefixLength, final boolean isIpv4) {
        return prefixLength!=32 && !isIpv4 || prefixLength!=16 && isIpv4;
    }

}
