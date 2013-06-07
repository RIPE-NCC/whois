package net.ripe.db.whois.update.authentication.strategy;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.*;
import net.ripe.db.whois.common.domain.attrs.Domain;
import net.ripe.db.whois.common.etree.Interval;
import net.ripe.db.whois.common.iptree.IpEntry;
import net.ripe.db.whois.common.iptree.Ipv4Tree;
import net.ripe.db.whois.common.iptree.Ipv6Tree;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.credential.AuthenticationModule;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.CheckForNull;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
class MntByAuthentication extends AuthenticationStrategyBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(MntByAuthentication.class);

    private final Maintainers maintainers;
    private final AuthenticationModule credentialValidators;
    private final RpslObjectDao rpslObjectDao;
    private final Ipv4Tree ipv4Tree;
    private final Ipv6Tree ipv6Tree;

    @Autowired
    MntByAuthentication(final Maintainers maintainers, final AuthenticationModule credentialValidators, final RpslObjectDao rpslObjectDao, final Ipv4Tree ipv4Tree, final Ipv6Tree ipv6Tree) {
        this.maintainers = maintainers;
        this.credentialValidators = credentialValidators;
        this.rpslObjectDao = rpslObjectDao;
        this.ipv4Tree = ipv4Tree;
        this.ipv6Tree = ipv6Tree;
    }

    @Override
    public boolean supports(final PreparedUpdate update) {
        return ObjectTemplate.getTemplate(update.getType()).hasAttribute(AttributeType.MNT_BY);
    }

    @Override
    public List<RpslObject> authenticate(final PreparedUpdate update, final UpdateContext updateContext) {
        try {
            return authenticateMntBy(update, updateContext);
        } catch (AuthenticationFailedException e) {
            return authenticateByAddresSpaceHolder(update, updateContext, e);
        }
    }

    private List<RpslObject> authenticateMntBy(final PreparedUpdate update, final UpdateContext updateContext) {
        RpslObject authenticationObject = update.getReferenceObject();
        Set<CIString> keys = authenticationObject.getValuesForAttribute(AttributeType.MNT_BY);

        if (keys.isEmpty()) {
            if (update.getAction().equals(Action.MODIFY)) {
                authenticationObject = update.getUpdatedObject();
                keys = authenticationObject.getValuesForAttribute(AttributeType.MNT_BY);
            }

            if (update.getAction().equals(Action.DELETE)) {
                return Collections.emptyList();
            }
        }

        final List<RpslObject> candidates = rpslObjectDao.getByKeys(ObjectType.MNTNER, keys);
        if (isSelfReference(update, keys)) {
            candidates.add(update.getReferenceObject());
        }

        final List<RpslObject> authenticatedBy = credentialValidators.authenticate(update, updateContext, candidates);
        if (authenticatedBy.isEmpty()) {
            throw new AuthenticationFailedException(UpdateMessages.authenticationFailed(authenticationObject, AttributeType.MNT_BY, candidates));
        }

        return authenticatedBy;
    }

    private List<RpslObject> authenticateByAddresSpaceHolder(final PreparedUpdate update, final UpdateContext updateContext, final AuthenticationFailedException originalAuthenticationException) {
        if (!update.getAction().equals(Action.DELETE)) {
            throw originalAuthenticationException;
        }

        final IpInterval ipInterval = getIpObject(update);
        if (ipInterval == null) {
            throw originalAuthenticationException;
        }

        @SuppressWarnings("unchecked")
        final List<IpEntry> ipEntries = Lists.reverse(((List<IpEntry>) findExactAndAllLessSpecific(ipInterval)));
        for (final IpEntry ipEntry : ipEntries) {

            final Interval key = ipEntry.getKey();
            if (Ipv4Resource.MAX_RANGE.equals(key) || Ipv6Resource.MAX_RANGE.equals(key)) {
                LOGGER.debug("Skipping full address space object: {}", key);
                continue;
            }

            final RpslObject ipObject = rpslObjectDao.getById(ipEntry.getObjectId());
            final List<RpslObject> authenticated = authenticateAddressSpaceHolder(update, updateContext, ipObject, originalAuthenticationException);
            if (!authenticated.isEmpty()) {
                return authenticated;
            }
        }

        throw originalAuthenticationException;
    }

    @SuppressWarnings("unchecked")
    private List<? extends IpEntry> findExactAndAllLessSpecific(final IpInterval ipInterval) {
        if (ipInterval instanceof Ipv4Resource) {
            return ipv4Tree.findExactAndAllLessSpecific((Ipv4Resource) ipInterval);
        } else if (ipInterval instanceof Ipv6Resource) {
            return ipv6Tree.findExactAndAllLessSpecific((Ipv6Resource) ipInterval);
        }

        throw new IllegalArgumentException("Unexpected ip object: " + ipInterval);
    }

    @CheckForNull
    private IpInterval getIpObject(final PreparedUpdate update) {
        final CIString typeString = update.getUpdatedObject().getTypeAttribute().getCleanValue();

        switch (update.getType()) {
            case INETNUM:
            case INET6NUM:
            case ROUTE:
            case ROUTE6:
                return IpInterval.parse(typeString);
            case DOMAIN:
                return Domain.parse(typeString).getReverseIp();
            default:
                return null;
        }
    }

    private boolean hasRsMaintainer(final RpslObject object, final AttributeType attributeType) {
        return !Sets.intersection(maintainers.getRsMaintainers(), object.getValuesForAttribute(attributeType)).isEmpty();
    }

    private List<RpslObject> authenticateAddressSpaceHolder(final PreparedUpdate update, final UpdateContext updateContext, final RpslObject ipObject, final AuthenticationFailedException originalAuthenticationException) {
        if (!hasRsMaintainer(ipObject, AttributeType.MNT_LOWER) && !hasRsMaintainer(ipObject, AttributeType.MNT_BY)) {
            return Collections.emptyList();
        }

        final List<RpslObject> mntLowerCandidates = rpslObjectDao.getByKeys(ObjectType.MNTNER, ipObject.getValuesForAttribute(AttributeType.MNT_LOWER));
        final List<RpslObject> mntByCandidates = rpslObjectDao.getByKeys(ObjectType.MNTNER, ipObject.getValuesForAttribute(AttributeType.MNT_BY));

        final Set<RpslObject> candidates = Sets.newLinkedHashSet();
        candidates.addAll(mntLowerCandidates);
        candidates.addAll(mntByCandidates);

        final List<RpslObject> authenticated = credentialValidators.authenticate(update, updateContext, candidates);
        if (authenticated.isEmpty()) {
            final List<Message> messages = Lists.newArrayList(originalAuthenticationException.getAuthenticationMessages());
            messages.add(UpdateMessages.authenticationFailed(ipObject, AttributeType.MNT_LOWER, mntLowerCandidates));
            messages.add(UpdateMessages.authenticationFailed(ipObject, AttributeType.MNT_BY, mntByCandidates));
            throw new AuthenticationFailedException(messages);
        }

        return authenticated;
    }

    private boolean isSelfReference(final PreparedUpdate update, final Collection<CIString> keys) {
        return update.getType().equals(ObjectType.MNTNER) && keys.contains(update.getReferenceObject().getKey());
    }
}
