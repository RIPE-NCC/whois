package net.ripe.db.whois.update.authentication.credential;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static net.ripe.db.whois.common.domain.CIString.ciString;

@Component
public class AuthenticationModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationModule.class);

    private final Map<Class<? extends Credential>, CredentialValidator> credentialValidatorMap;


    @Autowired
    public AuthenticationModule(final CredentialValidator<?>... credentialValidators) {
        credentialValidatorMap = Maps.newHashMap();

        for (final CredentialValidator<?> credentialValidator : credentialValidators) {
            credentialValidatorMap.put(credentialValidator.getSupportedCredentials(), credentialValidator);
        }
    }

    public List<RpslObject> authenticate(final PreparedUpdate update, final UpdateContext updateContext, final Collection<RpslObject> candidates) {
        final Credentials offered = update.getCredentials();

        final List<RpslObject> authenticatedCandidates = Lists.newArrayList();
        for (final RpslObject candidate : candidates) {
            if (hasValidCredentialForCandidate(update, updateContext, offered, candidate)) {
                authenticatedCandidates.add(candidate);
            }
        }

        return authenticatedCandidates;
    }

    private boolean hasValidCredentialForCandidate(final PreparedUpdate update, final UpdateContext updateContext, final Credentials offered, final RpslObject authenticationCandidate) {
        for (final CIString auth : authenticationCandidate.getValuesForAttribute(AttributeType.AUTH)) {
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
        if (auth.startsWith(ciString("MD5-PW")) || auth.startsWith(ciString("CRYPT-PW"))) {
            return new PasswordCredential(auth.toString());
        }

        if (auth.startsWith(ciString("PGPKEY"))) {
            return PgpCredential.createKnownCredential(auth.toString());
        }

        if (auth.startsWith(ciString("X509"))) {
            return X509Credential.createKnownCredential(auth.toString());
        }

        return null;
    }
}
