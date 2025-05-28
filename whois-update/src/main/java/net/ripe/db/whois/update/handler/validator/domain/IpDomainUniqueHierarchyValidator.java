package net.ripe.db.whois.update.handler.validator.domain;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.common.iptree.IpEntry;
import net.ripe.db.whois.common.iptree.IpTree;
import net.ripe.db.whois.common.iptree.Ipv4DomainTree;
import net.ripe.db.whois.common.iptree.Ipv6DomainTree;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.attrs.Domain;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class IpDomainUniqueHierarchyValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.DOMAIN);

    private final Ipv4DomainTree ipv4DomainTree;
    private final Ipv6DomainTree ipv6DomainTree;

    @Autowired
    public IpDomainUniqueHierarchyValidator(final Ipv4DomainTree ipv4DomainTree, final Ipv6DomainTree ipv6DomainTree) {
        this.ipv4DomainTree = ipv4DomainTree;
        this.ipv6DomainTree = ipv6DomainTree;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        final Domain domain = Domain.parse(update.getUpdatedObject().getKey());
        if (domain.getType() == Domain.Type.E164) {
            return Collections.emptyList();
        }

        final IpInterval reverseIp = domain.getReverseIp();
        final IpTree ipTree = getIpTree(reverseIp);

        final List<IpEntry> lessSpecific = ipTree.findFirstLessSpecific(reverseIp);
        if (!lessSpecific.isEmpty()) {
            return Arrays.asList(UpdateMessages.lessSpecificDomainFound(lessSpecific.get(0).getKey().toString()));
        }

        final List<IpEntry> moreSpecific = ipTree.findFirstMoreSpecific(reverseIp);
        if (!moreSpecific.isEmpty()) {
            return Arrays.asList(UpdateMessages.moreSpecificDomainFound(moreSpecific.get(0).getKey().toString()));
        }

        return Collections.emptyList();
    }

    private IpTree getIpTree(final IpInterval reverseIp) {
        return switch (reverseIp) {
            case Ipv4Resource ipv4Resource -> ipv4DomainTree;
            case Ipv6Resource ipv6Resource -> ipv6DomainTree;
            case null -> throw new IllegalArgumentException("Unexpected reverse ip: " + reverseIp);
        };
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
