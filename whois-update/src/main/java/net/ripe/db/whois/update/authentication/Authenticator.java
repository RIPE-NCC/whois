package net.ripe.db.whois.update.authentication;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.dao.UserDao;
import net.ripe.db.whois.common.domain.*;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.strategy.AuthenticationFailedException;
import net.ripe.db.whois.update.authentication.strategy.AuthenticationStrategy;
import net.ripe.db.whois.update.domain.*;
import net.ripe.db.whois.update.log.LoggerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class Authenticator {
    private final IpRanges ipRanges;
    private final UserDao userDao;
    private final LoggerContext loggerContext;
    private final List<AuthenticationStrategy> authenticationStrategies;
    private final Map<CIString, Set<Principal>> principalsMap;

    @Autowired
    public Authenticator(final IpRanges ipRanges, final UserDao userDao, final Maintainers maintainers, final LoggerContext loggerContext, final AuthenticationStrategy[] authenticationStrategies) {
        this.ipRanges = ipRanges;
        this.userDao = userDao;
        this.loggerContext = loggerContext;
        this.authenticationStrategies = Lists.newArrayList(authenticationStrategies);

        final Map<CIString, Set<Principal>> tempPrincipalsMap = Maps.newHashMap();
        addMaintainers(tempPrincipalsMap, maintainers.getPowerMaintainers(), Principal.POWER_MAINTAINER);
        addMaintainers(tempPrincipalsMap, maintainers.getEnduserMaintainers(), Principal.ENDUSER_MAINTAINER);
        addMaintainers(tempPrincipalsMap, maintainers.getAllocMaintainers(), Principal.ALLOC_MAINTAINER);
        addMaintainers(tempPrincipalsMap, maintainers.getRsMaintainers(), Principal.RS_MAINTAINER);
        addMaintainers(tempPrincipalsMap, maintainers.getEnumMaintainers(), Principal.ENUM_MAINTAINER);
        addMaintainers(tempPrincipalsMap, maintainers.getDbmMaintainers(), Principal.DBM_MAINTAINER);
        this.principalsMap = Collections.unmodifiableMap(tempPrincipalsMap);
    }

    private static void addMaintainers(final Map<CIString, Set<Principal>> principalsMap, final Set<CIString> maintainers, final Principal principal) {
        for (final CIString maintainer : maintainers) {
            Set<Principal> principals = principalsMap.get(maintainer);
            if (principals == null) {
                principals = Sets.newLinkedHashSet();
                principalsMap.put(maintainer, principals);
            }

            principals.add(principal);
        }
    }

    public void authenticate(final Origin origin, final PreparedUpdate update, final UpdateContext updateContext) {
        final Set<Principal> principals;

        if (origin.isDefaultOverride()) {
            principals = Collections.singleton(Principal.OVERRIDE_MAINTAINER);
        } else if (update.isOverride()) {
            principals = performOverrideAuthentication(origin, update, updateContext);
        } else {
            principals = performAuthentication(update, updateContext);
        }

        final Subject subject = new Subject(principals);
        updateContext.subject(update, subject);
    }

    private Set<Principal> performOverrideAuthentication(final Origin origin, final PreparedUpdate update, final UpdateContext updateContext) {
        final Set<OverrideCredential> overrideCredentials = update.getCredentials().ofType(OverrideCredential.class);
        final Set<Message> authenticationMessages = Sets.newLinkedHashSet();

        if (!origin.allowRipeOperations()) {
            authenticationMessages.add(UpdateMessages.overrideNotAllowedForOrigin(origin));
        } else if (!ipRanges.isInRipeRange(IpInterval.parse(origin.getFrom()))) {
            authenticationMessages.add(UpdateMessages.overrideOnlyAllowedByDbAdmins());
        }

        if (overrideCredentials.size() != 1) {
            authenticationMessages.add(UpdateMessages.multipleOverridePasswords());
        }

        if (!authenticationMessages.isEmpty()) {
            authenticationFailed(update, updateContext, authenticationMessages);
            return Collections.emptySet();
        }

        final OverrideCredential overrideCredential = overrideCredentials.iterator().next();
        for (OverrideCredential.UsernamePassword possibleCredential : overrideCredential.getPossibleCredentials()) {
            final String username = possibleCredential.getUsername();
            try {
                final User user = userDao.getOverrideUser(username);
                if (user.isValidPassword(possibleCredential.getPassword()) && user.getObjectTypes().contains(update.getType())) {
                    updateContext.addMessage(update, UpdateMessages.overrideAuthenticationUsed());
                    return Collections.singleton(Principal.OVERRIDE_MAINTAINER);
                }
            } catch (EmptyResultDataAccessException ignore) {
                loggerContext.logMessage(update, new Message(Messages.Type.INFO, "Unknown override user", username));
            }
        }

        authenticationMessages.add(UpdateMessages.overrideAuthenticationFailed());
        authenticationFailed(update, updateContext, authenticationMessages);
        return Collections.emptySet();
    }

    private Set<Principal> performAuthentication(final PreparedUpdate update, final UpdateContext updateContext) {
        final Set<Message> authenticationMessages = Sets.newLinkedHashSet();
        final Set<RpslObject> authenticatedObjects = Sets.newLinkedHashSet();

        if (update.getCredentials().ofType(PasswordCredential.class).size() > 20) {
            authenticationMessages.add(UpdateMessages.tooManyPasswordsSpecified());
        } else {
            for (final AuthenticationStrategy authenticationStrategy : authenticationStrategies) {
                if (authenticationStrategy.supports(update)) {
                    try {
                        authenticatedObjects.addAll(authenticationStrategy.authenticate(update, updateContext));
                    } catch (AuthenticationFailedException e) {
                        authenticationMessages.addAll(e.getAuthenticationMessages());
                    }
                }
            }
        }

        if (!authenticationMessages.isEmpty()) {
            authenticationFailed(update, updateContext, authenticationMessages);
        }

        final Set<Principal> principals = Sets.newLinkedHashSet();
        for (final RpslObject authenticatedObject : authenticatedObjects) {
            principals.addAll(getPrincipals(authenticatedObject));
        }

        return principals;
    }

    private Set<Principal> getPrincipals(final RpslObject authenticatedObject) {
        if (!authenticatedObject.getType().equals(ObjectType.MNTNER)) {
            return Collections.emptySet();
        }

        final Set<Principal> principals = principalsMap.get(authenticatedObject.getKey());
        if (principals == null) {
            return Collections.emptySet();
        }

        return principals;
    }

    private void authenticationFailed(final PreparedUpdate update, final UpdateContext updateContext, final Set<Message> authenticationMessages) {
        for (final Message message : authenticationMessages) {
            updateContext.addMessage(update, message);
        }

        updateContext.status(update, UpdateStatus.FAILED_AUTHENTICATION);
    }
}
