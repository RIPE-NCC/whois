package net.ripe.db.whois.common.override;

import net.ripe.db.whois.common.credentials.OverrideCredential;
import net.ripe.db.whois.common.dao.UserDao;
import net.ripe.db.whois.common.domain.IpRanges;
import net.ripe.db.whois.common.domain.User;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.sso.UserSession;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

@Component
public class OverrideCredentialValidator {

    private final IpRanges ipRanges;

    private final UserDao userDao;

    public OverrideCredentialValidator(final IpRanges ipRanges,
                                       final UserDao userDao) {
        this.ipRanges = ipRanges;
        this.userDao = userDao;
    }

    public boolean isAllowedToUseOverride(final String remoteAddress, final UserSession userSession, final String overrideUsername){
        if(ipRanges.isTrusted(IpInterval.parse(remoteAddress))) {
            return true;
        }

        if (userSession == null || userSession.getUsername() == null || overrideUsername == null) {
            return false;
        }

        return (userSession.getUsername()).equals(overrideUsername.concat("@ripe.net"));
    }

    public boolean isValidOverride(final OverrideCredential.OverrideValues overrideValues, final ObjectType objectType) throws EmptyResultDataAccessException {
        final User user = userDao.getOverrideUser(overrideValues.getUsername());
        return user.isValidPassword(overrideValues.getPassword()) && user.getObjectTypes().contains(objectType);
    }
}
