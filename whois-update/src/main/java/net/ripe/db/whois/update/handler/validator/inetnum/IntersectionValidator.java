package net.ripe.db.whois.update.handler.validator.inetnum;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.ip.Interval;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.common.iptree.IpEntry;
import net.ripe.db.whois.common.iptree.IpTree;
import net.ripe.db.whois.common.iptree.Ipv4Tree;
import net.ripe.db.whois.common.iptree.Ipv6Tree;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IntersectionValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.INETNUM, ObjectType.INET6NUM);

    private final Ipv4Tree ipv4Tree;
    private final Ipv6Tree ipv6Tree;

    @Autowired
    public IntersectionValidator(final Ipv4Tree ipv4Tree, final Ipv6Tree ipv6Tree) {
        this.ipv4Tree = ipv4Tree;
        this.ipv6Tree = ipv6Tree;
    }

    @Override
    public List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        final IpInterval ipInterval = IpInterval.parse(update.getReferenceObject().getKey());
        return switch (ipInterval) {
            case Ipv4Resource ipv4Resource -> validateIntersections(ipInterval, ipv4Tree, Lists.newArrayList());
            case Ipv6Resource ipv6Resource -> validateIntersections(ipInterval, ipv6Tree, Lists.newArrayList());
        };
    }

    private List<Message> validateIntersections(final IpInterval ipInterval, final IpTree ipTree, final List<Message> messages) {
        final List<IpEntry> parent = ipTree.findFirstLessSpecific(ipInterval);

        if (parent.size() != 1) {
            messages.add(UpdateMessages.invalidParentEntryForInterval(ipInterval));
            return messages;
        }

        Interval firstIntersecting = null;
        final List<IpEntry> childEntries = ipTree.findFirstMoreSpecific((IpInterval) parent.get(0).getKey());
        for (final IpEntry childEntry : childEntries) {
            final Interval child = childEntry.getKey();

            if (child.intersects(ipInterval) && !(child.contains(ipInterval) || ipInterval.contains(child))) {
                if (firstIntersecting == null || firstIntersecting.singletonIntervalAtLowerBound().compareUpperBound(child.singletonIntervalAtLowerBound()) > 0) {
                    firstIntersecting = child;
                }
            }
        }

        if (firstIntersecting != null) {
            messages.add(UpdateMessages.intersectingRange(firstIntersecting));
        }

        return messages;
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
