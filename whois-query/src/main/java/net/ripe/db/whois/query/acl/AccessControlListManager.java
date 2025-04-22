package net.ripe.db.whois.query.acl;

import io.netty.util.internal.StringUtil;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectSlaveDao;
import net.ripe.db.whois.common.domain.BlockEvent;
import net.ripe.db.whois.common.domain.IpRanges;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.common.oauth.OAuthSession;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.sso.AuthServiceClientException;
import net.ripe.db.whois.common.sso.SsoTokenTranslator;
import net.ripe.db.whois.common.sso.UserSession;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.dao.IpAccessControlListDao;
import net.ripe.db.whois.query.dao.SSOAccessControlListDao;
import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import net.ripe.db.whois.query.domain.QueryException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

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
    private final JdbcRpslObjectSlaveDao jdbcRpslObjectSlaveDao;
    private final boolean isSSOAccountingEnabled;

    @Autowired
    public AccessControlListManager(final DateTimeProvider dateTimeProvider,
                                    final IpResourceConfiguration ipResourceConfiguration,
                                    final IpAccessControlListDao ipAccessControlListDao,
                                    final PersonalObjectAccounting personalObjectAccounting,
                                    final SSOAccessControlListDao ssoAccessControlListDao,
                                    final SsoTokenTranslator ssoTokenTranslator,
                                    final SSOResourceConfiguration ssoResourceConfiguration,
                                    @Value("${personal.accounting.by.sso:true}") final boolean isSSOAccountingEnabled,
                                    final IpRanges ipRanges,
                                    final JdbcRpslObjectSlaveDao jdbcRpslObjectSlaveDao) {
        this.dateTimeProvider = dateTimeProvider;
        this.ipResourceConfiguration = ipResourceConfiguration;
        this.ipAccessControlListDao = ipAccessControlListDao;
        this.personalObjectAccounting = personalObjectAccounting;
        this.ssoResourceConfiguration = ssoResourceConfiguration;
        this.ssoAccessControlListDao = ssoAccessControlListDao;
        this.ssoTokenTranslator = ssoTokenTranslator;
        this.ipRanges = ipRanges;
        this.isSSOAccountingEnabled = isSSOAccountingEnabled;
        this.jdbcRpslObjectSlaveDao = jdbcRpslObjectSlaveDao;
    }

    public boolean requiresAcl(final RpslObject rpslObject, final Source source, final String ssoToken) {
        if (source.isGrs()) {
            return false;
        }

        if (!StringUtil.isNullOrEmpty(ssoToken) && isUserOwnedObject(rpslObject, ssoToken)){
            return false;
        }

        final ObjectType objectType = rpslObject.getType();
        return ObjectType.PERSON.equals(objectType)
                || (ObjectType.ROLE.equals(objectType) && rpslObject.findAttributes(AttributeType.ABUSE_MAILBOX).isEmpty());
    }

    public void checkBlocked(final AccountingIdentifier accountingIdentifier) {
        if(ipResourceConfiguration.isDenied(accountingIdentifier.getRemoteAddress())) {
            throw new QueryException(QueryCompletionInfo.BLOCKED, QueryMessages.accessDeniedPermanently(accountingIdentifier.getRemoteAddress().getHostAddress()));
        }

        final String username = accountingIdentifier.getUserName();
        if( ssoResourceConfiguration.isDenied(username)) {
            throw new QueryException(QueryCompletionInfo.BLOCKED, QueryMessages.accessDeniedPermanently(username));
        }

        if(!canQueryPersonalObjects(accountingIdentifier)) {
            throw new QueryException(QueryCompletionInfo.BLOCKED, QueryMessages.accessDeniedTemporarily(username == null ? accountingIdentifier.getRemoteAddress().getHostAddress() : username));
        }
    }

    public boolean isAllowedToProxy(final InetAddress remoteAddress) {
        return ipResourceConfiguration.isProxy(remoteAddress);
    }

    public boolean isUnlimited(final InetAddress remoteAddress) {
        return ipResourceConfiguration.getLimit(remoteAddress) < 0;
    }

    public boolean canQueryPersonalObjects(final AccountingIdentifier accountingIdentifier) {
        return getPersonalObjects(accountingIdentifier) >= 0;
    }

    public boolean isTrusted(final InetAddress remoteAddress) {
        return ipRanges.isTrusted(IpInterval.asIpInterval(remoteAddress));
    }

    public int getPersonalObjects(final AccountingIdentifier accountingIdentifier) {
        if (isUnlimited(accountingIdentifier.getRemoteAddress())) {
            return Integer.MAX_VALUE;
        }

        final PersonalAccountingManager accountingManager = getAccountingManager(accountingIdentifier);
        return accountingManager.getPersonalObjects();
    }

    private PersonalAccountingManager getAccountingManager(final AccountingIdentifier accountingIdentifier) {
       final String username =  accountingIdentifier.getUserName();

       return username == null ? new RemoteAddrAccountingManager(accountingIdentifier.getRemoteAddress()) : new SSOAccountingManager(username);
    }

    private String getUserName(final String ssoToken, final OAuthSession oAuthSession) {
        if( !isSSOAccountingEnabled) {
            return null;
        }

        if(oAuthSession != null && !StringUtils.isEmpty(oAuthSession.getEmail())) {
            return oAuthSession.getEmail();
        }

        if(StringUtils.isEmpty(ssoToken)) {
            return null;
        }

        try {
            final UserSession userSession = ssoTokenTranslator.translateSsoToken(ssoToken);
            if(userSession != null && !StringUtils.isEmpty(userSession.getUsername())) {
                return userSession.getUsername();
            }
        } catch (AuthServiceClientException e) {
            LOGGER.debug("Cannot translate ssoToken, will account by remoteAddr due to {}: {}", e.getClass().getName(), e.getMessage());
        }

        return null;
    }

    /**
     * Account for the ResponseObject given
     *
     * @param accountingIdentifier The remote address and ssoTken
     * @param amount        The amount of personal objects accounted.
     */
    public void accountPersonalObjects(final AccountingIdentifier accountingIdentifier, final int amount) {
        if (isUnlimited(accountingIdentifier.getRemoteAddress())) {
            return;
        }

        final PersonalAccountingManager accountingManager = getAccountingManager(accountingIdentifier);
        accountingManager.accountPersonalObjects(amount);
    }

    private boolean isUserOwnedObject(final RpslObject rpslObject, final String userName){
        final List<RpslObjectInfo> mntnerInfoList = jdbcRpslObjectSlaveDao.findByAttribute(AttributeType.AUTH, "SSO " + userName);
        return mntnerInfoList.stream().anyMatch(rpslObjectInfo -> rpslObject.getValueForAttribute(AttributeType.MNT_BY).contains(rpslObjectInfo.getKey()));
    }

    private class SSOAccountingManager implements PersonalAccountingManager {
        private final String userName;

        public SSOAccountingManager(final String userName) {
            this.userName = userName;
        }

        @Override
        public int getPersonalObjects() {
            final int queried = personalObjectAccounting.getQueriedPersonalObjects(userName);
            final int personalDataLimit = getPersonalDataLimit();

            LOGGER.debug("personal data limit for {} is {}", userName, personalDataLimit);
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

    private class RemoteAddrAccountingManager implements PersonalAccountingManager {
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
            final IpInterval<?> maskedAddressInterval;
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

    public AccountingIdentifier getAccountingIdentifier(final InetAddress remoteAddress, final String ssoToken, final OAuthSession oAuthSession) {
        final String userName = getUserName(ssoToken, oAuthSession);
        return new AccountingIdentifier(remoteAddress, userName);
    }
}
