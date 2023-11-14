package net.ripe.db.whois.query.acl;

import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.Source;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetAddress;

@Component
public class AccessControlListManager {
    private final SSOAccessControlListManager ssoAccessControlListManager;
    private final IpAccessControlListManager ipAccessControlListManager;

    @Autowired
    public AccessControlListManager(final SSOAccessControlListManager ssoAccessControlListManager,
                                    final IpAccessControlListManager ipAccessControlListManager) {
        this.ssoAccessControlListManager = ssoAccessControlListManager;
        this.ipAccessControlListManager = ipAccessControlListManager;
    }

    public boolean requiresAcl(final RpslObject rpslObject, final Source source) {
        if (source.isGrs()) {
            return false;
        }

        final ObjectType objectType = rpslObject.getType();
        return ObjectType.PERSON.equals(objectType)
                || (ObjectType.ROLE.equals(objectType) && rpslObject.findAttributes(AttributeType.ABUSE_MAILBOX).isEmpty());
    }

    public boolean isDenied(final InetAddress remoteAddress, final String ssoId) {
        return ipAccessControlListManager.isDenied(remoteAddress) || ssoAccessControlListManager.isDenied(ssoId);
    }

    public boolean isAllowedToProxy(final InetAddress remoteAddress) {
        return ipAccessControlListManager.isAllowedToProxy(remoteAddress);
    }

    public boolean isUnlimited(final InetAddress remoteAddress) {
        return ipAccessControlListManager.isUnlimited(remoteAddress);
    }

    public boolean canQueryPersonalObjects(final InetAddress remoteAddress, final String ssoId) {
        return getPersonalObjects(remoteAddress,ssoId) >= 0;
    }

    public boolean isTrusted(final InetAddress remoteAddress) {
        return ipAccessControlListManager.isTrusted(remoteAddress);
    }

    public int getPersonalObjects(final InetAddress remoteAddress, final String ssoId) {
        if (isUnlimited(remoteAddress)) {
            return Integer.MAX_VALUE;
        }

        return StringUtils.isEmpty(ssoId) ? ipAccessControlListManager.getPersonalObjects(remoteAddress) : ssoAccessControlListManager.getPersonalObjects(ssoId);
    }

    /**
     * Account for the ResponseObject given
     *
     * @param remoteAddress The remote address.
     * @param amount        The amount of personal objects accounted.
     */
    public void accountPersonalObjects(final InetAddress remoteAddress, final String ssoId, final int amount) {
        if (isUnlimited(remoteAddress)) {
            return;
        }

        if(StringUtils.isEmpty(ssoId)) {
            ipAccessControlListManager.accountPersonalObjects(remoteAddress, amount);
            return;
        }

        ssoAccessControlListManager.accountPersonalObjects(ssoId, amount);
    }
}
