package net.ripe.db.whois.update.handler.validator.domain;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.etree.NestedIntervalMap.Key;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.iptree.Ipv4DomainTree;
import net.ripe.db.whois.common.iptree.Ipv4Entry;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.Domain;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.BadRequestException;
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
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final Domain domain = Domain.parse(update.getUpdatedObject().getKey());
        if (domain.getType() != INADDR) {
            validateNserverCorrectPrefixes(update.getUpdate(), false);
            return;
        }
        validateIntersections(update, updateContext, (Ipv4Resource)domain.getReverseIp());
        validateNserverCorrectPrefixes(update.getUpdate(), true);
    }

    private void validateIntersections(final PreparedUpdate update, final UpdateContext updateContext, final Ipv4Resource ipv4Resource) {
        final Ipv4Resource parentInterval = ipv4DomainTree.findFirstLessSpecific(ipv4Resource).stream()
                .map(Key::getKey)
                .findFirst()
                .orElse(Ipv4Resource.parse("0/0"));

        final List<Ipv4Entry> childEntries = ipv4DomainTree.findFirstMoreSpecific(parentInterval);
        for (final Ipv4Entry childEntry : childEntries) {
            final Ipv4Resource child = childEntry.getKey();

            if (child.intersects(ipv4Resource) && !(child.contains(ipv4Resource) || ipv4Resource.contains(child))) {
                final RpslObject domain = rpslObjectDao.getById(childEntry.getObjectId());
                updateContext.addMessage(update, UpdateMessages.intersectingDomain(domain.getKey()));
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


    private void validateNserverCorrectPrefixes(Update update, boolean isIpv4){
        if (hasRipeNserver(update.getSubmittedObject()) && hasIncorrectPrefixes(update.getSubmittedObject(), isIpv4)){
            throw new BadRequestException();
        }
    }

    private boolean hasIncorrectPrefixes(RpslObject rpslObject, boolean isIpv4) {
        RpslAttribute rpslAttribute = rpslObject.findAttribute(AttributeType.DESCR);
        return !rpslAttribute.getValue().contains("/32") && !isIpv4 || !rpslAttribute.getValue().contains("/16") && isIpv4;
    }

    private boolean hasRipeNserver(RpslObject rpslObject) {
        return rpslObject.findAttributes(AttributeType.NSERVER).stream().anyMatch(nserver -> nserver.getValue().equals("ns.ripe.net"));
    }
}
