package net.ripe.db.whois.update.handler.validator.domain;

import com.google.common.collect.ImmutableList;
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
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final Domain domain = Domain.parse(update.getUpdatedObject().getKey());
        if (domain.getType() == Domain.Type.E164) {
            return;
        }

        final IpInterval reverseIp = domain.getReverseIp();
        final IpTree ipTree = getIpTree(reverseIp);

        final List<IpEntry> lessSpecific = ipTree.findFirstLessSpecific(reverseIp);
        if (!lessSpecific.isEmpty()) {
            updateContext.addMessage(update, UpdateMessages.lessSpecificDomainFound(lessSpecific.get(0).getKey().toString()));
            return;
        }

        final List<IpEntry> moreSpecific = ipTree.findFirstMoreSpecific(reverseIp);
        if (!moreSpecific.isEmpty()) {
            updateContext.addMessage(update, UpdateMessages.moreSpecificDomainFound(moreSpecific.get(0).getKey().toString()));
        }
    }

    private IpTree getIpTree(final IpInterval reverseIp) {
        if (reverseIp instanceof Ipv4Resource) {
            return ipv4DomainTree;
        } else if (reverseIp instanceof Ipv6Resource) {
            return ipv6DomainTree;
        }

        throw new IllegalArgumentException("Unexpected reverse ip: " + reverseIp);
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
