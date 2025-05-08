package net.ripe.db.whois.update.authentication.credential;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.strategy.AuthenticationStrategy;
import net.ripe.db.whois.common.Credentials.Credential;
import net.ripe.db.whois.update.domain.Credentials;
import net.ripe.db.whois.common.Credentials.PasswordCredential;
import net.ripe.db.whois.update.domain.PgpCredential;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.common.Credentials.SsoCredential;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.domain.X509Credential;
import net.ripe.db.whois.update.log.LoggerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class AuthenticationModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationModule.class);

    private static final String PASSWORDS_REMOVED_REMARK = "MD5 passwords older than November 2011 were removed from this maintainer, see: https://www.ripe.net/removed2011pw";

    private static final AuthComparator AUTH_COMPARATOR = new AuthComparator();

    private final Map<Class<? extends Credential>, Set<CredentialValidator<? extends Credential, ? extends Credential>>> credentialValidatorMap;
    private final LoggerContext loggerContext;

    @Autowired
    public AuthenticationModule(final LoggerContext loggerContext,
                                final CredentialValidator<? extends Credential, ? extends Credential>... credentialValidators) {

        this.loggerContext = loggerContext;
        credentialValidatorMap = Maps.newHashMap();

        for (final CredentialValidator<? extends Credential, ? extends Credential> credentialValidator : credentialValidators) {
            credentialValidatorMap.computeIfAbsent(credentialValidator.getSupportedCredentials(), credentialClass -> Sets.newHashSet()).add(credentialValidator);
        }
    }

    public List<RpslObject> authenticate(final PreparedUpdate update,
                                         final UpdateContext updateContext,
                                         final Collection<RpslObject> maintainers,
                                         final Class<? extends AuthenticationStrategy> authenticationStrategyClass) {
        final Credentials offered = update.getCredentials();
        boolean passwordRemovedRemark = false;

        loggerContext.logAuthenticationStrategy(update.getUpdate(), authenticationStrategyClass.getName(), maintainers);

        final List<RpslObject> authenticatedCandidates = Lists.newArrayList();
        for (final RpslObject maintainer : maintainers) {
            if (hasValidCredentialForCandidate(update, updateContext, offered, maintainer)) {
                authenticatedCandidates.add(maintainer);
            } else {
                if (hasPasswordRemovedRemark(maintainer)) {
                    passwordRemovedRemark = true;
                }
            }
        }

        if (authenticatedCandidates.isEmpty() && passwordRemovedRemark) {
            updateContext.addMessage(update, UpdateMessages.oldPasswordsRemoved());
        }

        return authenticatedCandidates;
    }

    private boolean hasValidCredentialForCandidate(final PreparedUpdate update, final UpdateContext updateContext, final Credentials offered, final RpslObject maintainer) {
        final List<CIString> authAttributes = Lists.newArrayList(maintainer.getValuesForAttribute(AttributeType.AUTH));
        Collections.sort(authAttributes, AUTH_COMPARATOR);

        for (final CIString auth : authAttributes) {
            final Credential credential = getCredential(auth);
            if (credential == null) {
                LOGGER.warn("Skipping unknown credential: {}", auth);
                continue;
            }

            final Class<? extends Credential> credentialClass = credential.getClass();
            for (CredentialValidator credentialValidator : credentialValidatorMap.get(credentialClass)) {
                if (credentialValidator.hasValidCredential(update, updateContext, offered.ofType(credentialValidator.getSupportedOfferedCredentialType()), credential, maintainer)) {
                    return true;
                }
            }
        }

        return false;
    }

    private Credential getCredential(final CIString auth) {
        if (auth.startsWith("md5-pw")) {
            return new PasswordCredential(auth.toString());
        }

        if (auth.startsWith("pgpkey")) {
            return PgpCredential.createKnownCredential(auth.toString());
        }

        if (auth.startsWith("x509")) {
            return X509Credential.createKnownCredential(auth.toString());
        }

        if (auth.startsWith("sso")) {
            return SsoCredential.createKnownCredential(auth.toString());
        }

        return null;
    }

    private boolean hasPasswordRemovedRemark(final RpslObject maintainer) {
        for (RpslAttribute remark : maintainer.findAttributes(AttributeType.REMARKS)) {
            if (remark.getCleanValue().equals(PASSWORDS_REMOVED_REMARK)) {
                return true;
            }
        }

        return false;
    }

    private static class AuthComparator implements Comparator<CIString> {
        private static final CIString SSO = CIString.ciString("SSO");

        @Override
        public int compare(final CIString o1, final CIString o2) {
            final boolean o1Sso = o1.startsWith(SSO);
            final boolean o2Sso = o2.startsWith(SSO);

            if (o1Sso == o2Sso) {
                return 0;
            } else if (o1Sso) {
                return -1;
            } else {
                return 1;
            }
        }
    }
}
