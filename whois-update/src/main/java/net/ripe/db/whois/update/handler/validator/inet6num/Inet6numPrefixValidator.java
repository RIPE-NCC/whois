package net.ripe.db.whois.update.handler.validator.inet6num;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class Inet6numPrefixValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.INET6NUM);

    private static final int MINIMUM_PREFIX_LENGTH = 64;

    @Override
    public List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        Ipv6Resource ipv6Resource = Ipv6Resource.parse(update.getUpdatedObject().getKey());
        if (ipv6Resource.getPrefixLength() > MINIMUM_PREFIX_LENGTH) {
           return Arrays.asList(UpdateMessages.prefixTooSmall(MINIMUM_PREFIX_LENGTH));
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
