package net.ripe.db.whois.update.handler.validator.inetnum;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.domain.Interval;
import net.ripe.db.whois.common.domain.IpInterval;
import net.ripe.db.whois.common.domain.Ipv4Resource;
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
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IntersectionValidator implements BusinessRuleValidator {
    private final Ipv4Tree ipv4Tree;
    private final Ipv6Tree ipv6Tree;

    @Autowired
    public IntersectionValidator(final Ipv4Tree ipv4Tree, final Ipv6Tree ipv6Tree) {
        this.ipv4Tree = ipv4Tree;
        this.ipv6Tree = ipv6Tree;
    }

    @Override
    public List<Action> getActions() {
        return Lists.newArrayList(Action.CREATE);
    }

    @Override
    public List<ObjectType> getTypes() {
        return Lists.newArrayList(ObjectType.INETNUM, ObjectType.INET6NUM);
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final IpInterval ipInterval = IpInterval.parse(update.getReferenceObject().getKey());
        if (ipInterval instanceof Ipv4Resource) {
            validateIntersections(update, updateContext, ipInterval, ipv4Tree);
        } else {
            validateIntersections(update, updateContext, ipInterval, ipv6Tree);
        }
    }

    private void validateIntersections(final PreparedUpdate update, final UpdateContext updateContext, final IpInterval ipInterval, final IpTree ipTree) {
        final List<IpEntry> parent = ipTree.findFirstLessSpecific(ipInterval);
        Validate.notEmpty(parent, "Should always have a parent");

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
            updateContext.addMessage(update, UpdateMessages.intersectingRange(firstIntersecting));
        }
    }
}
