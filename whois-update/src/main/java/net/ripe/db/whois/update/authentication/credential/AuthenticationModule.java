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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

        if (updateContext.getUserSession() !=null){
            offered.add(Collections.singleton(SsoCredential.createOfferedCredential(updateContext.getUserSession())));
        }

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
}
