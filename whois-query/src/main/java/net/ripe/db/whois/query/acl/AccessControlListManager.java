package net.ripe.db.whois.query.acl;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.domain.BlockEvent;
import net.ripe.db.whois.common.domain.IpRanges;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.sso.AuthServiceClientException;
import net.ripe.db.whois.common.sso.SsoTokenTranslator;
import net.ripe.db.whois.common.sso.UserSession;
import net.ripe.db.whois.query.dao.IpAccessControlListDao;
import net.ripe.db.whois.query.dao.SSOAccessControlListDao;
import org.apache.commons.lang.StringUtils;
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
    public static final int IPV6_NETMASK = 64;

    private final DateTimeProvider dateTimeProvider;
    private final IpResourceConfiguration ipResourceConfiguration;
    private final IpAccessControlListDao ipAccessControlListDao;
    private final PersonalObjectAccounting personalObjectAccounting;
    private final IpRanges ipRanges;
    private final SSOResourceConfiguration ssoResourceConfiguration;
    private final SSOAccessControlListDao ssoAccessControlListDao;
    private final SsoTokenTranslator ssoTokenTranslator;

    @Autowired
    public AccessControlListManager(final DateTimeProvider dateTimeProvider,
                                    final IpResourceConfiguration ipResourceConfiguration,
                                    final IpAccessControlListDao ipAccessControlListDao,
                                    final PersonalObjectAccounting personalObjectAccounting,
                                    final SSOAccessControlListDao ssoAccessControlListDao,
                                    final SsoTokenTranslator ssoTokenTranslator,
                                    final SSOResourceConfiguration ssoResourceConfiguration,
                                    final IpRanges ipRanges) {
        this.dateTimeProvider = dateTimeProvider;
        this.ipResourceConfiguration = ipResourceConfiguration;
        this.ipAccessControlListDao = ipAccessControlListDao;
        this.personalObjectAccounting = personalObjectAccounting;
        this.ssoResourceConfiguration = ssoResourceConfiguration;
        this.ssoAccessControlListDao = ssoAccessControlListDao;
        this.ssoTokenTranslator = ssoTokenTranslator;
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

    public boolean isDenied(final InetAddress remoteAddress, final String ssoToken) {
        if(ipResourceConfiguration.isDenied(remoteAddress)) {
            return true;
        }

        final String username = getUserName(ssoToken);
        return username != null ? ssoResourceConfiguration.isDenied(username) : false;
    }

    public boolean isAllowedToProxy(final InetAddress remoteAddress) {
        return ipResourceConfiguration.isProxy(remoteAddress);
    }

    public boolean isUnlimited(final InetAddress remoteAddress) {
        return ipResourceConfiguration.getLimit(remoteAddress) < 0;
    }

    public boolean canQueryPersonalObjects(final InetAddress remoteAddress, final String ssoToken) {
        return getPersonalObjects(remoteAddress,ssoToken) >= 0;
    }

    public boolean isTrusted(final InetAddress remoteAddress) {
        return ipRanges.isTrusted(IpInterval.asIpInterval(remoteAddress));
    }

    public int getPersonalObjects(final InetAddress remoteAddress, final String ssoToken) {
        if (isUnlimited(remoteAddress)) {
            return Integer.MAX_VALUE;
        }

        final PersonalAccountingManager accountingManager = getAccountingManager(remoteAddress, ssoToken);
        return accountingManager.getPersonalObjects();
    }

    private PersonalAccountingManager getAccountingManager(final InetAddress remoteAddress, final String ssoToken) {
       final String username =  getUserName(ssoToken);
       return username == null ? new RemoteAddrAccountingManager(remoteAddress) : new SSOAccountingManager(username);
    }

    private String getUserName(final String ssoToken) {
        if(StringUtils.isEmpty(ssoToken)) {
            return null;
        }

        try {
            final UserSession userSession = ssoTokenTranslator.translateSsoToken(ssoToken);
            if(userSession != null && !StringUtils.isEmpty(userSession.getUsername())) {
                return userSession.getUsername();
            }
        } catch (AuthServiceClientException e) {
            LOGGER.warn("Cannot translate ssoToken, will account by remoteAddr", e.getMessage());
        }

        return null;
    }

    /**
     * Account for the ResponseObject given
     *
     * @param remoteAddress The remote address.
     * @param amount        The amount of personal objects accounted.
     */
    public void accountPersonalObjects(final InetAddress remoteAddress, final String ssoToken, final int amount) {
        if (isUnlimited(remoteAddress)) {
            return;
        }

        final PersonalAccountingManager accountingManager = getAccountingManager(remoteAddress, ssoToken);
        accountingManager.accountPersonalObjects(amount);
    }

    public class SSOAccountingManager implements PersonalAccountingManager {
        private final String userName;

        public SSOAccountingManager(final String userName) {
            this.userName = userName;
        }

        @Override
        public int getPersonalObjects() {
            final int queried = personalObjectAccounting.getQueriedPersonalObjects(userName);
            final int personalDataLimit = getPersonalDataLimit();

            return personalDataLimit - queried;
        }

        @Override
        public void accountPersonalObjects(final int amount) {
            final int limit = getPersonalDataLimit();

            final int remaining = limit - personalObjectAccounting.accountPersonalObject(userName, amount);
            if (remaining < 0) {
                blockTemporary(limit);
            }
        }

        @Override
        public void blockTemporary(final int limit) {
            ssoAccessControlListDao.saveAclEvent(userName, dateTimeProvider.getCurrentDate(), limit, BlockEvent.Type.BLOCK_TEMPORARY);
        }

        @Override
        public int getPersonalDataLimit() {
            return ssoResourceConfiguration.getLimit();
        }
    }

    public class RemoteAddrAccountingManager implements PersonalAccountingManager {
        private final InetAddress remoteAddress;
        private final InetAddress maskedAddress;

        public RemoteAddrAccountingManager(final InetAddress remoteAddress) {
            this.remoteAddress = remoteAddress;
            this.maskedAddress =  mask(remoteAddress, IPV6_NETMASK);
        }

        @Override
        public int getPersonalObjects() {
            if (isUnlimited(remoteAddress)) {
                return Integer.MAX_VALUE;
            }

            final int queried = personalObjectAccounting.getQueriedPersonalObjects(maskedAddress);
            final int personalDataLimit = getPersonalDataLimit();

            return personalDataLimit - queried;
        }

        @Override
        public void accountPersonalObjects(final int amount) {
            final int limit = getPersonalDataLimit();
            if (limit < 0) {
                return;
            }

            final int remaining = limit - personalObjectAccounting.accountPersonalObject(maskedAddress, amount);
            if (remaining < 0) {
                blockTemporary(limit);
            }
        }

        @Override
        public void blockTemporary(final int limit) {
            IpInterval<?> maskedAddressInterval;
            if (maskedAddress instanceof Inet6Address) {
                maskedAddressInterval = Ipv6Resource.parse(mask(maskedAddress, IPV6_NETMASK).getHostAddress() + "/" + IPV6_NETMASK);
            } else {
                maskedAddressInterval = Ipv4Resource.asIpInterval(maskedAddress);
            }

           ipAccessControlListDao.saveAclEvent(maskedAddressInterval, dateTimeProvider.getCurrentDate(), limit, BlockEvent.Type.BLOCK_TEMPORARY);
        }

        @Override
        public int getPersonalDataLimit() {
            return ipResourceConfiguration.getLimit(remoteAddress);
        }
    }

    public static InetAddress mask(final InetAddress address, final int mask) {
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
