package net.ripe.db.whois.update.authentication;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.dao.UserDao;
import net.ripe.db.whois.common.domain.*;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.strategy.AuthenticationFailedException;
import net.ripe.db.whois.update.authentication.strategy.AuthenticationStrategy;
import net.ripe.db.whois.update.dao.PendingUpdateDao;
import net.ripe.db.whois.update.domain.*;
import net.ripe.db.whois.update.log.LoggerContext;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import javax.annotation.CheckForNull;
import java.util.*;

@Component
public class Authenticator {
    private static final Logger LOGGER = LoggerFactory.getLogger(Authenticator.class);

    private final IpRanges ipRanges;
    private final UserDao userDao;
    private final PendingUpdateDao pendingUpdateDao;
    private final LoggerContext loggerContext;
    private final List<AuthenticationStrategy> authenticationStrategies;
    private final Map<CIString, Set<Principal>> principalsMap;
    private final Map<ObjectType, Set<String>> typesWithPendingAuthenticationSupport;

    @Autowired
    public Authenticator(final IpRanges ipRanges,
                         final UserDao userDao,
                         final Maintainers maintainers,
                         final LoggerContext loggerContext,
                         final AuthenticationStrategy[] authenticationStrategies,
                         final PendingUpdateDao pendingUpdateDao) {
        this.ipRanges = ipRanges;
        this.userDao = userDao;
        this.loggerContext = loggerContext;
        this.pendingUpdateDao = pendingUpdateDao;
        Arrays.sort(authenticationStrategies);
        this.authenticationStrategies = Arrays.asList(authenticationStrategies);

        final Map<CIString, Set<Principal>> tempPrincipalsMap = Maps.newHashMap();
        addMaintainers(tempPrincipalsMap, maintainers.getPowerMaintainers(), Principal.POWER_MAINTAINER);
        addMaintainers(tempPrincipalsMap, maintainers.getEnduserMaintainers(), Principal.ENDUSER_MAINTAINER);
        addMaintainers(tempPrincipalsMap, maintainers.getAllocMaintainers(), Principal.ALLOC_MAINTAINER);
        addMaintainers(tempPrincipalsMap, maintainers.getRsMaintainers(), Principal.RS_MAINTAINER);
        addMaintainers(tempPrincipalsMap, maintainers.getEnumMaintainers(), Principal.ENUM_MAINTAINER);
        addMaintainers(tempPrincipalsMap, maintainers.getDbmMaintainers(), Principal.DBM_MAINTAINER);
        this.principalsMap = Collections.unmodifiableMap(tempPrincipalsMap);

        typesWithPendingAuthenticationSupport = Maps.newEnumMap(ObjectType.class);
        for (final AuthenticationStrategy authenticationStrategy : authenticationStrategies) {
            for (final ObjectType objectType : authenticationStrategy.getTypesWithPendingAuthenticationSupport()) {
                Set<String> strategiesWithPendingAuthenticationSupport = typesWithPendingAuthenticationSupport.get(objectType);
                if (strategiesWithPendingAuthenticationSupport == null) {
                    strategiesWithPendingAuthenticationSupport = new HashSet<>();
                    typesWithPendingAuthenticationSupport.put(objectType, strategiesWithPendingAuthenticationSupport);
                }

                strategiesWithPendingAuthenticationSupport.add(authenticationStrategy.getName());
            }
        }

        for (final Map.Entry<ObjectType, Set<String>> objectTypeSetEntry : typesWithPendingAuthenticationSupport.entrySet()) {
            final Set<String> authenticationStrategyNames = objectTypeSetEntry.getValue();
            Validate.isTrue(authenticationStrategyNames.size() > 1, "Pending authentication makes no sense for 1 authentication strategy:", authenticationStrategyNames);
            LOGGER.info("Pending authentication supported for {}: {}", objectTypeSetEntry.getKey(), authenticationStrategyNames);
        }
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
        final Subject subject;

        if (origin.isDefaultOverride()) {
            subject = new Subject(Principal.OVERRIDE_MAINTAINER);
        } else if (update.isOverride()) {
            subject = performOverrideAuthentication(origin, update, updateContext);
        } else {
            subject = performAuthentication(origin, update, updateContext);
        }

        updateContext.subject(update, subject);
    }

    private Subject performOverrideAuthentication(final Origin origin, final PreparedUpdate update, final UpdateContext updateContext) {
        final Set<OverrideCredential> overrideCredentials = update.getCredentials().ofType(OverrideCredential.class);
        final Set<Message> authenticationMessages = Sets.newLinkedHashSet();

        if (!origin.allowAdminOperations()) {
            authenticationMessages.add(UpdateMessages.overrideNotAllowedForOrigin(origin));
        } else if (!ipRanges.isTrusted(IpInterval.parse(origin.getFrom()))) {
            authenticationMessages.add(UpdateMessages.overrideOnlyAllowedByDbAdmins());
        }

        if (overrideCredentials.size() != 1) {
            authenticationMessages.add(UpdateMessages.multipleOverridePasswords());
        }

        if (!authenticationMessages.isEmpty()) {
            handleFailure(update, updateContext, Subject.EMPTY, authenticationMessages);
            return Subject.EMPTY;
        }

        final OverrideCredential overrideCredential = overrideCredentials.iterator().next();
        for (OverrideCredential.UsernamePassword possibleCredential : overrideCredential.getPossibleCredentials()) {
            final String username = possibleCredential.getUsername();
            try {
                final User user = userDao.getOverrideUser(username);
                if (user.isValidPassword(possibleCredential.getPassword()) && user.getObjectTypes().contains(update.getType())) {
                    updateContext.addMessage(update, UpdateMessages.overrideAuthenticationUsed());
                    return new Subject(Principal.OVERRIDE_MAINTAINER);
                }
            } catch (EmptyResultDataAccessException ignore) {
                loggerContext.logMessage(update, new Message(Messages.Type.INFO, "Unknown override user: %s", username));
            }
        }

        authenticationMessages.add(UpdateMessages.overrideAuthenticationFailed());
        handleFailure(update, updateContext, Subject.EMPTY, authenticationMessages);
        return Subject.EMPTY;
    }

    private Subject performAuthentication(final Origin origin, final PreparedUpdate update, final UpdateContext updateContext) {
        final Set<Message> authenticationMessages = Sets.newLinkedHashSet();
        final Set<RpslObject> authenticatedObjects = Sets.newLinkedHashSet();

        final Set<String> passedAuthentications = new HashSet<>();
        final Set<String> failedAuthentications = new HashSet<>();
        final Map<String, Collection<RpslObject>> pendingAuthentications = new HashMap<>();

        if (update.getCredentials().ofType(PasswordCredential.class).size() > 20) {
            authenticationMessages.add(UpdateMessages.tooManyPasswordsSpecified());
        } else {
            for (final AuthenticationStrategy authenticationStrategy : authenticationStrategies) {
                if (authenticationStrategy.supports(update)) {
                    try {
                        authenticatedObjects.addAll(authenticationStrategy.authenticate(update, updateContext));
                        passedAuthentications.add(authenticationStrategy.getName());
                    } catch (AuthenticationFailedException e) {
                        authenticationMessages.addAll(e.getAuthenticationMessages());

                        if (authenticationStrategy.getTypesWithPendingAuthenticationSupport().contains(update.getType())) {
                            pendingAuthentications.put(authenticationStrategy.getName(), e.getCandidates());
                        } else {
                            failedAuthentications.add(authenticationStrategy.getName());
                        }
                    }
                }
            }
        }

        final Set<Principal> principals = Sets.newLinkedHashSet();
        for (final RpslObject authenticatedObject : authenticatedObjects) {
            principals.addAll(getPrincipals(authenticatedObject));
        }

        // TODO: [AH] remove the isDeployed() when we are done migrating power-maintainer tests to syncupdates (a lot of tests that require power mntner are using mailupdates ATM)
        if (!principals.isEmpty() && !origin.isDefaultOverride() && WhoisProfile.isDeployed()) {
            if (!origin.allowAdminOperations() || !ipRanges.isTrusted(IpInterval.parse(origin.getFrom()))) {
                authenticationMessages.add(UpdateMessages.ripeMntnerUpdatesOnlyAllowedFromWithinNetwork());
            }
        }

        filterAuthentication(updateContext, update, passedAuthentications, failedAuthentications, pendingAuthentications);

        final Subject subject = new Subject(principals, passedAuthentications, failedAuthentications, pendingAuthentications);
        if (!authenticationMessages.isEmpty()) {
            handleFailure(update, updateContext, subject, authenticationMessages);
        }

        return subject;
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

    private void handleFailure(final PreparedUpdate update, final UpdateContext updateContext, final Subject subject, final Set<Message> authenticationMessages) {
        if (isPending(update, updateContext, subject.getPendingAuthentications()) && subject.getFailedAuthentications().isEmpty()) {
            updateContext.status(update, UpdateStatus.PENDING_AUTHENTICATION);
        } else {
            updateContext.status(update, UpdateStatus.FAILED_AUTHENTICATION);
        }

        for (final Message message : authenticationMessages) {
            updateContext.addMessage(update, message);
        }
    }

    boolean isPending(final PreparedUpdate update, final UpdateContext updateContext, final Set<String> pendingAuths) {
        // TODO: [AH] remove this if when pending is deployed
        if (WhoisProfile.isDeployed()) {
            return false;
        }

        if (!Action.CREATE.equals(update.getAction())) {
            return false;
        }

        final Set<String> supportedPendingAuths = typesWithPendingAuthenticationSupport.get(update.getType());

        return !updateContext.hasErrors(update)
                && pendingAuths.size() > 0
                && pendingAuths.size() < supportedPendingAuths.size();
    }

    private void filterAuthentication(UpdateContext updateContext, PreparedUpdate update, Set<String> passedAuthentications, Set<String> failedAuthentications, Map<String, Collection<RpslObject>> pendingAuthentications) {
        // we only have pending filter ATM
        if (isPending(update, updateContext, pendingAuthentications.keySet())) {
            final PendingUpdate pendingUpdate = findAndStorePendingUpdate(updateContext, update);
            if (pendingUpdate != null) {
                if (failedAuthentications.remove("MntByAuthentication")) {
                    passedAuthentications.add("MntByAuthentication");
                }
            }
        }
    }

    @CheckForNull
    private PendingUpdate findAndStorePendingUpdate(UpdateContext updateContext, final PreparedUpdate update) {
        final RpslObject rpslObject = update.getUpdatedObject();

        for (final PendingUpdate pendingUpdate : pendingUpdateDao.findByTypeAndKey(rpslObject.getType(), rpslObject.getKey().toString())) {
            if (rpslObject.equals(pendingUpdate.getObject())) {
                updateContext.addPendingUpdate(update, pendingUpdate);
                return pendingUpdate;
            }
        }
        return null;
    }

    public boolean isAuthenticationForTypeComplete(final ObjectType objectType, final PendingUpdate pendingUpdate) {
        final Set<String> authenticationStrategyNames = typesWithPendingAuthenticationSupport.get(objectType);
        return pendingUpdate.getPassedAuthentications().containsAll(authenticationStrategyNames);
    }
}
