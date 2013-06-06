package net.ripe.db.whois.update.authentication.strategy;

import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.IpInterval;
import net.ripe.db.whois.common.domain.Ipv4Resource;
import net.ripe.db.whois.common.domain.Ipv6Resource;
import net.ripe.db.whois.common.iptree.*;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.credential.AuthenticationModule;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
class InetnumAuthentication extends AuthenticationStrategyBase {
    private final AuthenticationModule authenticationModule;
    private final Ipv4Tree ipv4Tree;
    private final Ipv6Tree ipv6Tree;
    private final RpslObjectDao rpslObjectDao;

    @Autowired
    public InetnumAuthentication(final AuthenticationModule authenticationModule, final Ipv4Tree ipv4Tree, final Ipv6Tree ipv6Tree, final RpslObjectDao rpslObjectDao) {
        this.authenticationModule = authenticationModule;
        this.ipv4Tree = ipv4Tree;
        this.ipv6Tree = ipv6Tree;
        this.rpslObjectDao = rpslObjectDao;
    }

    @Override
    public boolean supports(final PreparedUpdate update) {
        return update.getAction().equals(Action.CREATE) && (update.getType().equals(ObjectType.INETNUM) || update.getType().equals(ObjectType.INET6NUM));
    }

    @Override
    public List<RpslObject> authenticate(final PreparedUpdate update, final UpdateContext updateContext) {
        final IpInterval ipInterval = IpInterval.parse(update.getUpdatedObject().getKey());

        final IpEntry ipEntry = getParentEntry(ipInterval);
        final RpslObject parentObject = rpslObjectDao.getById(ipEntry.getObjectId());

        AttributeType attributeType = AttributeType.MNT_LOWER;
        Collection<CIString> maintainerKeys = parentObject.getValuesForAttribute(attributeType);
        if (maintainerKeys.isEmpty()) {
            attributeType = AttributeType.MNT_BY;
            maintainerKeys = parentObject.getValuesForAttribute(attributeType);
        }

        final List<RpslObject> maintainers = rpslObjectDao.getByKeys(ObjectType.MNTNER, maintainerKeys);
        final List<RpslObject> authenticatedBy = authenticationModule.authenticate(update, updateContext, maintainers);
        if (authenticatedBy.isEmpty()) {
            throw new AuthenticationFailedException(UpdateMessages.parentAuthenticationFailed(parentObject, attributeType, maintainers));
        }

        return authenticatedBy;
    }

    private IpEntry getParentEntry(final IpInterval ipInterval) {
        if (ipInterval instanceof Ipv4Resource) {
            final List<Ipv4Entry> parent = ipv4Tree.findFirstLessSpecific((Ipv4Resource) ipInterval);
            Validate.isTrue(parent.size() == 1);
            return parent.get(0);
        } else if (ipInterval instanceof Ipv6Resource) {
            final List<Ipv6Entry> parent = ipv6Tree.findFirstLessSpecific((Ipv6Resource) ipInterval);
            Validate.isTrue(parent.size() == 1);
            return parent.get(0);
        }

        throw new IllegalArgumentException("Unexpected IpInterval: " + ipInterval);
    }
}
