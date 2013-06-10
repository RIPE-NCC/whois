package net.ripe.db.whois.update.authentication.strategy;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.IpInterval;
import net.ripe.db.whois.common.domain.attrs.AddressPrefixRange;
import net.ripe.db.whois.common.domain.attrs.MntRoutes;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.credential.AuthenticationModule;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;

import java.util.List;
import java.util.Set;

abstract class RouteAuthentication extends AuthenticationStrategyBase {
    final AuthenticationModule authenticationModule;
    final RpslObjectDao objectDao;

    protected RouteAuthentication(final AuthenticationModule authenticationModule, final RpslObjectDao objectDao) {
        this.authenticationModule = authenticationModule;
        this.objectDao = objectDao;
    }

    @Override
    public boolean supports(final PreparedUpdate update) {
        return update.getAction().equals(Action.CREATE) && (update.getType().equals(ObjectType.ROUTE) || update.getType().equals(ObjectType.ROUTE6));
    }

    @Override
    public Set<ObjectType> getTypesWithPendingAuthenticationSupport() {
        return Sets.newHashSet(ObjectType.ROUTE, ObjectType.ROUTE6);
    }

    List<RpslObject> getCandidatesForMntRoutesAuthentication(final RpslObject authenticationObject, final PreparedUpdate update) {
        final IpInterval ipInterval = IpInterval.parse(update.getUpdatedObject().getTypeAttribute().getCleanValue());

        final Set<CIString> candidateKeys = Sets.newLinkedHashSet();
        for (final CIString mntRoutesValue : authenticationObject.getValuesForAttribute(AttributeType.MNT_ROUTES)) {
            final MntRoutes mntRoutes = MntRoutes.parse(mntRoutesValue);
            if (mntRoutes.isAnyRange()) {
                candidateKeys.add(mntRoutes.getMaintainer());
                continue;
            }

            for (final AddressPrefixRange addressPrefixRange : mntRoutes.getAddressPrefixRanges()) {
                if (AddressPrefixRange.BoundaryCheckResult.SUCCESS.equals(addressPrefixRange.checkRange(ipInterval))) {
                    candidateKeys.add(mntRoutes.getMaintainer());
                }
            }
        }

        return objectDao.getByKeys(ObjectType.MNTNER, candidateKeys);
    }
}
