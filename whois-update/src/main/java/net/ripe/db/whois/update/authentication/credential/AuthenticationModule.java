package net.ripe.db.whois.update.authentication.credential;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Credential;
import net.ripe.db.whois.update.domain.Credentials;
import net.ripe.db.whois.update.domain.PasswordCredential;
import net.ripe.db.whois.update.domain.PgpCredential;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.SsoCredential;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.X509Credential;
import net.ripe.db.whois.update.log.LoggerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sun.reflect.Reflection;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
public class AuthenticationModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationModule.class);
    private static final AuthComparator AUTH_COMPARATOR = new AuthComparator();

    private final Map<Class<? extends Credential>, CredentialValidator> credentialValidatorMap;
    private final LoggerContext loggerContext;

    @Autowired
    public AuthenticationModule(LoggerContext loggerContext,
                                final CredentialValidator<?>... credentialValidators) {

        this.loggerContext = loggerContext;
        credentialValidatorMap = Maps.newHashMap();

        for (final CredentialValidator<?> credentialValidator : credentialValidators) {
            credentialValidatorMap.put(credentialValidator.getSupportedCredentials(), credentialValidator);
        }
    }

    public List<RpslObject> authenticate(final PreparedUpdate update, final UpdateContext updateContext, final Collection<RpslObject> maintainers) {
        Credentials offered = update.getCredentials();

        loggerContext.logAuthenticationStrategy(update.getUpdate(), Reflection.getCallerClass().getCanonicalName(), maintainers);

        // FIXME: [AH] this is ugly; we should add the SSO credential upon creating the Update object in InternalUpdatePerformer.createUpdate()
        if (updateContext.getUserSession() != null) {
            offered = offered.add(Collections.singleton(SsoCredential.createOfferedCredential(updateContext.getUserSession())));
        }

        final List<RpslObject> authenticatedCandidates = Lists.newArrayList();
        for (final RpslObject maintainer : maintainers) {
            if (hasValidCredentialForCandidate(update, updateContext, offered, maintainer)) {
                authenticatedCandidates.add(maintainer);
            }
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
            final CredentialValidator credentialValidator = credentialValidatorMap.get(credentialClass);
            if (credentialValidator != null && credentialValidator.hasValidCredential(update, updateContext, offered.ofType(credentialClass), credential)) {
                return true;
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

    public static class AuthComparator implements Comparator<CIString> {
        private static final CIString SSO = CIString.ciString("SSO");

        @Override
        public int compare(final CIString o1, final CIString o2) {
            final boolean o1Sso = o1.startsWith(SSO);
            final boolean o2Sso = o2.startsWith(SSO);

            if (o1Sso == o2Sso) {
                return 0;
            } else if (o1Sso) {
                return -1;
            } else if (o2Sso) {
                return 1;
            }
            return 0;   // never reached
        }
    }
}
