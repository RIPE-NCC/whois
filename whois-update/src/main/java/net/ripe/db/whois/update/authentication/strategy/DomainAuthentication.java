package net.ripe.db.whois.update.authentication.strategy;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.common.iptree.IpEntry;
import net.ripe.db.whois.common.iptree.IpTree;
import net.ripe.db.whois.common.iptree.Ipv4Tree;
import net.ripe.db.whois.common.iptree.Ipv6Tree;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.Domain;
import net.ripe.db.whois.update.authentication.credential.AuthenticationModule;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
public class DomainAuthentication extends AuthenticationStrategyBase {
    private final Ipv4Tree ipv4Tree;
    private final Ipv6Tree ipv6Tree;
    private final RpslObjectDao objectDao;
    private final AuthenticationModule authenticationModule;

    @Autowired
    public DomainAuthentication(final Ipv4Tree ipv4Tree,
                                final Ipv6Tree ipv6Tree,
                                final RpslObjectDao objectDao,
                                final AuthenticationModule authenticationModule) {
        this.ipv4Tree = ipv4Tree;
        this.ipv6Tree = ipv6Tree;
        this.objectDao = objectDao;
        this.authenticationModule = authenticationModule;
    }

    @Override
    public boolean supports(final PreparedUpdate update) {
        return update.getAction().equals(Action.CREATE) && (update.getType().equals(ObjectType.DOMAIN));
    }

    @Override
    public List<RpslObject> authenticate(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject rpslObject = update.getUpdatedObject();
        final CIString domainString = rpslObject.getKey();
        final Domain domain = Domain.parse(domainString);

        if (domain.getType() == Domain.Type.E164) {
            return Collections.emptyList();
        }

        final IpInterval<?> reverseIp = domain.getReverseIp();
        if (reverseIp instanceof Ipv4Resource) {
            return authenticate(update, updateContext, reverseIp, ipv4Tree);
        } else if (reverseIp instanceof Ipv6Resource) {
            return authenticate(update, updateContext, reverseIp, ipv6Tree);
        }

        throw new IllegalArgumentException("Unexpected reverse ip: " + reverseIp);
    }

    @SuppressWarnings("unchecked")
    private List<RpslObject> authenticate(final PreparedUpdate update, final UpdateContext updateContext, final IpInterval reverseIp, final IpTree ipTree) {
        final RpslObject rpslObject = update.getUpdatedObject();

        final List<IpEntry> ipEntries = ipTree.findExactOrFirstLessSpecific(reverseIp);
        if (ipEntries.isEmpty() || ipEntries.size() > 1) {
            throw new AuthenticationFailedException(UpdateMessages.authenticationFailed(rpslObject, AttributeType.DOMAIN, Collections.<RpslObject>emptySet()), Collections.<RpslObject>emptyList());
        }

        final IpEntry ipEntry = ipEntries.get(0);
        final RpslObject ipObject = objectDao.getById(ipEntry.getObjectId());

        final List<RpslObject> authenticated = Lists.newArrayList();

        authenticated.addAll(authenticate(update, updateContext, ipObject, AttributeType.MNT_DOMAINS));
        if (!authenticated.isEmpty()) {
            return authenticated;
        }

        authenticated.addAll(authenticate(update, updateContext, ipObject, AttributeType.MNT_LOWER));
        if (!authenticated.isEmpty()) {
            return authenticated;
        }

        return authenticate(update, updateContext, ipObject, AttributeType.MNT_BY);
    }

    private List<RpslObject> authenticate(final PreparedUpdate update, final UpdateContext updateContext, final RpslObject ipObject, final AttributeType attributeType) {
        final Set<CIString> keys = ipObject.getValuesForAttribute(attributeType);
        if (keys.isEmpty()) {
            return Collections.emptyList();
        }

        final List<RpslObject> candidates = objectDao.getByKeys(ObjectType.MNTNER, keys);
        final List<RpslObject> authenticated = authenticationModule.authenticate(update, updateContext, candidates);
        if (authenticated.isEmpty()) {
            throw new AuthenticationFailedException(UpdateMessages.authenticationFailed(ipObject, attributeType, candidates), candidates);
        }

        return authenticated;
    }
}
