package net.ripe.db.whois.update.authentication.strategy;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.ip.IpInterval;
import net.ripe.db.whois.common.domain.ip.Ipv4Resource;
import net.ripe.db.whois.common.domain.ip.Ipv6Resource;
import net.ripe.db.whois.common.iptree.*;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.credential.AuthenticationModule;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
class RouteIpAddressAuthentication extends RouteAuthentication {
    private final Ipv4RouteTree ipv4RouteTree;
    private final Ipv4Tree ipv4Tree;
    private final Ipv6RouteTree ipv6RouteTree;
    private final Ipv6Tree ipv6Tree;

    @Autowired
    public RouteIpAddressAuthentication(final AuthenticationModule authenticationModule, final RpslObjectDao objectDao, final Ipv4RouteTree ipv4RouteTree, final Ipv4Tree ipv4Tree, final Ipv6RouteTree ipv6RouteTree, final Ipv6Tree ipv6Tree) {
        super(authenticationModule, objectDao);
        this.ipv4RouteTree = ipv4RouteTree;
        this.ipv4Tree = ipv4Tree;
        this.ipv6RouteTree = ipv6RouteTree;
        this.ipv6Tree = ipv6Tree;
    }

    @Override
    public List<RpslObject> authenticate(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject updatedObject = update.getUpdatedObject();
        final RpslAttribute typeAttribute = updatedObject.getTypeAttribute();
        final IpInterval addressPrefix = IpInterval.parse(typeAttribute.getCleanValue());

        final List<RpslObject> ipObjects = getIpObjects(addressPrefix);
        if (ipObjects.isEmpty()) {
            throw new AuthenticationFailedException(UpdateMessages.authenticationFailed(updatedObject, typeAttribute.getType(), Collections.<RpslObject>emptyList()), Collections.<RpslObject>emptyList());
        }

        final Set<RpslObject> allCandidates = Sets.newLinkedHashSet();
        final List<Message> authenticationMessages = Lists.newArrayList();

        for (final RpslObject ipObject : ipObjects) {
            if (ipObject.containsAttribute(AttributeType.MNT_ROUTES)) {
                final List<RpslObject> candidates = getCandidatesForMntRoutesAuthentication(ipObject, update);
                allCandidates.addAll(candidates);

                final List<RpslObject> authenticated = authenticationModule.authenticate(update, updateContext, candidates);
                if (authenticated.isEmpty()) {
                    authenticationMessages.add(UpdateMessages.authenticationFailed(ipObject, AttributeType.MNT_ROUTES, candidates));
                } else {
                    return authenticated;
                }
            }
        }


        if (!authenticationMessages.isEmpty()) {
            throw new AuthenticationFailedException(authenticationMessages, allCandidates);
        }

        for (final RpslObject ipObject : ipObjects) {
            final IpInterval ipInterval = IpInterval.parse(ipObject.getTypeAttribute().getCleanValue());
            if (!addressPrefix.equals(ipInterval) && ipObject.containsAttribute(AttributeType.MNT_LOWER)) {
                final List<RpslObject> candidates = objectDao.getByKeys(ObjectType.MNTNER, ipObject.getValuesForAttribute(AttributeType.MNT_LOWER));
                allCandidates.addAll(candidates);

                final List<RpslObject> authenticated = authenticationModule.authenticate(update, updateContext, candidates);
                if (authenticated.isEmpty()) {
                    authenticationMessages.add(UpdateMessages.authenticationFailed(ipObject, AttributeType.MNT_LOWER, candidates));
                } else {
                    return authenticated;
                }
            }
        }

        if (!authenticationMessages.isEmpty()) {
            throw new AuthenticationFailedException(authenticationMessages, allCandidates);
        }

        for (final RpslObject ipObject : ipObjects) {
            if (ipObject.containsAttribute(AttributeType.MNT_BY)) {
                final List<RpslObject> candidates = objectDao.getByKeys(ObjectType.MNTNER, ipObject.getValuesForAttribute(AttributeType.MNT_BY));
                allCandidates.addAll(candidates);

                final List<RpslObject> authenticated = authenticationModule.authenticate(update, updateContext, candidates);
                if (authenticated.isEmpty()) {
                    authenticationMessages.add(UpdateMessages.authenticationFailed(ipObject, AttributeType.MNT_BY, candidates));
                } else {
                    return authenticated;
                }
            }
        }

        if (!authenticationMessages.isEmpty()) {
            throw new AuthenticationFailedException(authenticationMessages, allCandidates);
        }

        throw new AuthenticationFailedException(UpdateMessages.authenticationFailed(updatedObject, typeAttribute.getType(), Collections.<RpslObject>emptyList()), allCandidates);
    }


    private List<RpslObject> getIpObjects(final IpInterval<?> addressPrefix) {
        if (addressPrefix instanceof Ipv4Resource) {
            return getIpObjects(ipv4RouteTree, ipv4Tree, addressPrefix);
        }

        if (addressPrefix instanceof Ipv6Resource) {
            return getIpObjects(ipv6RouteTree, ipv6Tree, addressPrefix);
        }

        throw new IllegalStateException("Unexpected address prefix: " + addressPrefix);
    }

    private List<RpslObject> getIpObjects(final IpTree routeTree, final IpTree ipTree, final IpInterval addressPrefix) {
        final List<RpslObject> ipEntries = getIpObjects(routeTree, addressPrefix);
        if (ipEntries.isEmpty()) {
            return getIpObjects(ipTree, addressPrefix);
        }

        return ipEntries;
    }

    @SuppressWarnings("unchecked")
    private List<RpslObject> getIpObjects(final IpTree ipTree, final IpInterval addressPrefix) {
        final List<IpEntry> ipEntries = ipTree.findExactOrFirstLessSpecific(addressPrefix);
        final List<RpslObject> ipObjects = Lists.newArrayListWithCapacity(ipEntries.size());
        for (final IpEntry ipEntry : ipEntries) {
            ipObjects.add(objectDao.getById(ipEntry.getObjectId()));
        }

        return ipObjects;
    }
}
