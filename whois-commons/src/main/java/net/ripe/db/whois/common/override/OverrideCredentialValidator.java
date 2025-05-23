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

import javax.annotation.Nullable;

@Component
public class OverrideCredentialValidator {

    private final IpRanges ipRanges;

    private final UserDao userDao;

    public OverrideCredentialValidator(final IpRanges ipRanges,
                                       final UserDao userDao) {
        this.ipRanges = ipRanges;
        this.userDao = userDao;
    }

    public boolean isAllowedAndValid(final boolean isTrusted, final UserSession userSession,
                                     final User overrideUser,
                                     final ObjectType objectType){
        if (overrideUser == null || overrideUser.getUsername() == null) {
            return false;
        }
        return isAllowedToUseOverride(isTrusted, userSession, overrideUser.getUsername().toString()) && overrideUser.getObjectTypes().contains(objectType);
    }

    public boolean isAllowedToUseOverride(final String remoteAddress, final UserSession userSession, final String overrideUsername){
        return isAllowedToUseOverride(ipRanges.isTrusted(IpInterval.parse(remoteAddress)), userSession, overrideUsername);
    }

    public boolean isAllowedToUseOverride(final boolean isTrusted, final UserSession userSession, final String overrideUsername){
        if (isTrusted) {
            return true;
        }

        return isAllowedBySSO(userSession, overrideUsername);
    }

    public boolean isAllowedBySSO(final UserSession userSession, final String overrideUsername){
        if (userSession == null || userSession.getUsername() == null || overrideUsername == null) {
            return false;
        }

        return (userSession.getUsername()).equals(overrideUsername.concat("@ripe.net"));
    }

    public boolean isValidOverride(final OverrideCredential.OverrideValues overrideValues, final ObjectType objectType) throws EmptyResultDataAccessException {
        final User user = userDao.getOverrideUser(overrideValues.getUsername());
        return user.isValidPassword(overrideValues.getPassword()) && user.getObjectTypes().contains(objectType);
    }


    @Nullable
    public User getValidOverrideUser(final String override) {
      try {
          final OverrideCredential.OverrideValues overrideValues = OverrideCredential.parse(override).getOverrideValues().orElse(null);
          final User overrideUser =  overrideValues != null ? userDao.getOverrideUser(overrideValues.getUsername()) : null;
          if (overrideUser == null) return null;

          return overrideUser.isValidPassword(overrideValues.getPassword()) ? overrideUser : null;

      } catch (Exception e) {
          return null;
      }
    }
}
