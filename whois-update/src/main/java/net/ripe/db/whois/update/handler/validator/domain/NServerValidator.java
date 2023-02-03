package net.ripe.db.whois.update.handler.validator.domain;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
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
import net.ripe.db.whois.update.handler.validator.CustomValidationMessage;
import org.springframework.stereotype.Component;

import java.util.List;

import static net.ripe.db.whois.common.rpsl.attrs.Domain.Type.INADDR;
import static net.ripe.db.whois.common.rpsl.attrs.Domain.Type.IP6;

@Component
public class NServerValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.DOMAIN);

    @Override
    public List<CustomValidationMessage> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject updatedObject = update.getUpdatedObject();

        final Domain domain = Domain.parse(updatedObject.getKey());
        final List<CustomValidationMessage> customValidationMessages = Lists.newArrayList();

        for (final RpslAttribute nServerAttribute : updatedObject.findAttributes(AttributeType.NSERVER)) {
            final NServer nServer = NServer.parse(nServerAttribute.getCleanValue());

            switch (domain.getType()) {
                case E164:
                {
                    final boolean endsWithDomain = domain.endsWithDomain(nServer.getHostname());

                    if (endsWithDomain && nServer.getIpInterval() == null) {
                        customValidationMessages.add(new CustomValidationMessage(UpdateMessages.glueRecordMandatory(domain.getValue()) , nServerAttribute));
                    } else if (!endsWithDomain && nServer.getIpInterval() != null) {
                        customValidationMessages.add(new CustomValidationMessage(UpdateMessages.invalidGlueForEnumDomain(nServer.getIpInterval().toString()), nServerAttribute));
                    }
                    break;
                }
                case INADDR:
                case IP6:
                {
                    final boolean endsWithDomain = domain.endsWithDomain(nServer.getHostname());
                    if (domain.getReverseIp() != null) {
                        validateRipeNsServerPrefixLength(domain, update, nServerAttribute, updateContext);
                    }
                    if (endsWithDomain && nServer.getIpInterval() == null) {
                        customValidationMessages.add(new CustomValidationMessage(UpdateMessages.glueRecordMandatory(domain.getValue()), nServerAttribute));
                    } else if (!endsWithDomain && nServer.getIpInterval() != null) {
                        customValidationMessages.add(new CustomValidationMessage(UpdateMessages.hostNameMustEndWith(domain.getValue()), nServerAttribute));
                    }
                    break;
                }
            }
        }

        return customValidationMessages;
    }

    @Override
    public boolean isSkipForOverride() {
        return false;
    }

    @Override
    public ImmutableList<Action> getActions() {
        return ACTIONS;
    }

    @Override
    public ImmutableList<ObjectType> getTypes() {
        return TYPES;
    }


    private void validateRipeNsServerPrefixLength(final Domain domain, final PreparedUpdate update,
                                                  final RpslAttribute nServerAttribute, final UpdateContext updateContext){
        if ("ns.ripe.net".equalsIgnoreCase(nServerAttribute.getValue()) && hasIncorrectPrefixForRipeNsServer(domain.getReverseIp().getPrefixLength(),
                domain.getType())){
            updateContext.addMessage(update, nServerAttribute, UpdateMessages.incorrectPrefixForRipeNsServer());
        }
    }

    private boolean hasIncorrectPrefixForRipeNsServer(final int prefixLength, Domain.Type type) {
        return prefixLength!=32 && type.equals(IP6) || prefixLength!=16 && type.equals(INADDR);
    }

}
