package net.ripe.db.whois.query.acl;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.domain.BlockEvent;
import net.ripe.db.whois.common.domain.IpRanges;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.query.dao.AccessControlListDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public class AccessControlListManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccessControlListManager.class);

    static final int IPV6_NETMASK = 64;
    static final int IPV4_NETMASK = 32;

    private final DateTimeProvider dateTimeProvider;
    private final IpResourceConfiguration resourceConfiguration;
    private final AccessControlListDao accessControlListDao;
    private final PersonalObjectAccounting personalObjectAccounting;
    private final IpRanges ipRanges;

    @Autowired
    public AccessControlListManager(final DateTimeProvider dateTimeProvider,
                                    final IpResourceConfiguration resourceConfiguration,
                                    final AccessControlListDao accessControlListDao,
                                    final PersonalObjectAccounting personalObjectAccounting,
                                    final IpRanges ipRanges) {
        this.dateTimeProvider = dateTimeProvider;
        this.resourceConfiguration = resourceConfiguration;
        this.accessControlListDao = accessControlListDao;
        this.personalObjectAccounting = personalObjectAccounting;
        this.ipRanges = ipRanges;
    }

    public boolean requiresAcl(final RpslObject rpslObject, final Source source) {
        if (source.isGrs()) {
            return false;
        }

        final ObjectType objectType = rpslObject.getType();
        return ObjectType.PERSON.equals(objectType)
                || (ObjectType.ROLE.equals(objectType) && rpslObject.findAttributes(AttributeType.ABUSE_MAILBOX).isEmpty());
    }

    public boolean isDenied(final InetAddress remoteAddress) {
        return resourceConfiguration.isDenied(remoteAddress);
    }

    public boolean isAllowedToProxy(final InetAddress remoteAddress) {
        return resourceConfiguration.isProxy(remoteAddress);
    }

    int getPersonalDataLimit(final InetAddress remoteAddress) {
        return resourceConfiguration.getLimit(remoteAddress);
    }

    public boolean isUnlimited(final InetAddress remoteAddress) {
        return getPersonalDataLimit(remoteAddress) < 0;
    }

    public boolean canQueryPersonalObjects(final InetAddress remoteAddress) {
        return getPersonalObjects(remoteAddress) >= 0;
    }

    public boolean isOverride(final InetAddress remoteAddress) {
        return ipRanges.isTrusted(IpInterval.asIpInterval(remoteAddress));
    }

    public int getPersonalObjects(final InetAddress remoteAddress) {
        if (isUnlimited(remoteAddress)) {
            return Integer.MAX_VALUE;
        }

        final InetAddress maskedAddress = mask(remoteAddress, IPV6_NETMASK);
        final int queried = personalObjectAccounting.getQueriedPersonalObjects(maskedAddress);
        final int personalDataLimit = getPersonalDataLimit(remoteAddress);

        return personalDataLimit - queried;
    }

    /**
     * Account for the ResponseObject given
     *
     * @param remoteAddress The remote address.
     * @param amount        The amount of personal objects accounted.
     */
    public void accountPersonalObjects(final InetAddress remoteAddress, final int amount) {
        final int limit = getPersonalDataLimit(remoteAddress);
        if (limit < 0) {
            return;
        }

        final InetAddress maskedAddress = mask(remoteAddress, IPV6_NETMASK);
        final int remaining = limit - personalObjectAccounting.accountPersonalObject(maskedAddress, amount);
        if (remaining < 0) {
            blockTemporary(maskedAddress, limit);
        }
    }

    public void blockTemporary(final InetAddress hostAddress, final int limit) {
        String maskedAddress = getMaskedAddressAsString(hostAddress);
        accessControlListDao.saveAclEvent(maskedAddress, dateTimeProvider.getCurrentDate(), limit, BlockEvent.Type.BLOCK_TEMPORARY);
    }

    static String getMaskedAddressAsString(final InetAddress hostAddress) {
        String maskedAddress = mask(hostAddress, IPV6_NETMASK).getHostAddress();
        if (hostAddress instanceof Inet6Address) {
            maskedAddress += "/" + IPV6_NETMASK;
        } else {
            maskedAddress += "/" + IPV4_NETMASK;
        }
        return maskedAddress;
    }

    static InetAddress mask(final InetAddress address, final int mask) {
        if (address instanceof Inet6Address) {
            byte[] bytes = address.getAddress();

            int part = mask % 8;
            int firstMaskedIndex = (mask / 8) + (part == 0 ? 0 : 1);
            for (int i = firstMaskedIndex; i < bytes.length; i++) {
                bytes[i] = (byte) 0;
            }

            if (part != 0) {
                bytes[mask / 8] &= ~((1 << (8 - part)) - 1);
            }

            try {
                return Inet6Address.getByAddress(bytes);
            } catch (UnknownHostException e) {
                LOGGER.warn("We do not change the length; we cannot ever have this exception", e);
            }
        }

        return address;
    }
}
