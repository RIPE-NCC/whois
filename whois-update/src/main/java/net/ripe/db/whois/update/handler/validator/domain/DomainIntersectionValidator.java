package net.ripe.db.whois.update.handler.validator.domain;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.etree.NestedIntervalMap.Key;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.iptree.Ipv4DomainTree;
import net.ripe.db.whois.common.iptree.Ipv4Entry;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
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

import static net.ripe.db.whois.common.rpsl.attrs.Domain.Type.INADDR;

@Component
public class DomainIntersectionValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.DOMAIN);

    private final Ipv4DomainTree ipv4DomainTree;
    private final RpslObjectDao rpslObjectDao;

    @Autowired
    public DomainIntersectionValidator(final Ipv4DomainTree ipv4DomainTree,
                                       final RpslObjectDao rpslObjectDao) {
        this.ipv4DomainTree = ipv4DomainTree;
        this.rpslObjectDao = rpslObjectDao;
    }

    @Override
    public List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        final Domain domain = Domain.parse(update.getUpdatedObject().getKey());
        if (domain.getType() != INADDR) {
            return Collections.emptyList();
        }
        return validateIntersections((Ipv4Resource)domain.getReverseIp());
    }

    private List<Message> validateIntersections(final Ipv4Resource ipv4Resource) {
        final Ipv4Resource parentInterval = ipv4DomainTree.findFirstLessSpecific(ipv4Resource).stream()
                .map(Key::getKey)
                .findFirst()
                .orElse(Ipv4Resource.parse("0/0"));

        final List<Ipv4Entry> childEntries = ipv4DomainTree.findFirstMoreSpecific(parentInterval);
        for (final Ipv4Entry childEntry : childEntries) {
            final Ipv4Resource child = childEntry.getKey();

            if (child.intersects(ipv4Resource) && !(child.contains(ipv4Resource) || ipv4Resource.contains(child))) {
                final RpslObject domain = rpslObjectDao.getById(childEntry.getObjectId());
                return Arrays.asList(UpdateMessages.intersectingDomain(domain.getKey()));
            }
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
