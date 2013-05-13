package net.ripe.db.whois.update.authentication.credential;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.domain.X509Credential;
import net.ripe.db.whois.update.keycert.X509CertificateWrapper;
import net.ripe.db.whois.update.log.LoggerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import javax.annotation.CheckForNull;
import java.util.Collection;

@Component
public class X509CredentialValidator implements CredentialValidator<X509Credential> {

    private final RpslObjectDao rpslObjectDao;
    private final DateTimeProvider dateTimeProvider;
    private final LoggerContext loggerContext;

    @Autowired
    public X509CredentialValidator(final RpslObjectDao rpslObjectDao, final DateTimeProvider dateTimeProvider, final LoggerContext loggerContext) {
        this.rpslObjectDao = rpslObjectDao;
        this.dateTimeProvider = dateTimeProvider;
        this.loggerContext = loggerContext;
    }

    @Override
    public Class<X509Credential> getSupportedCredentials() {
        return X509Credential.class;
    }

    @Override
    public boolean hasValidCredential(final PreparedUpdate update, final UpdateContext updateContext, final Collection<X509Credential> offeredCredentials, final X509Credential knownCredential) {
        for (final X509Credential offeredCredential : offeredCredentials) {
            if (verifySignedMessage(update, updateContext, offeredCredential, knownCredential)) {
                log(update, String.format("Successfully validated with keycert: %s", knownCredential.getKeyId()));
                return true;
            }
        }

        return false;
    }

    private boolean verifySignedMessage(final PreparedUpdate update, final UpdateContext updateContext, final X509Credential offeredCredential, final X509Credential knownCredential) {
        final String keyId = knownCredential.getKeyId();
        final X509CertificateWrapper x509CertificateWrapper = getKeyWrapper(update, updateContext, keyId);
        if (x509CertificateWrapper == null) {
            return false;
        }

        if (verify(update, offeredCredential, x509CertificateWrapper)) {
            log(update, String.format("Successfully validated with keycert: {}", keyId));

            if (x509CertificateWrapper.isExpired(dateTimeProvider)) {
                updateContext.addMessage(update, UpdateMessages.certificateHasExpired(keyId));
            } else {
                if (x509CertificateWrapper.isNotYetValid(dateTimeProvider)) {
                    updateContext.addMessage(update, UpdateMessages.certificateNotYetValid(keyId));
                }
            }

            if (!offeredCredential.verifySigningTime(dateTimeProvider)) {
                updateContext.addMessage(update, UpdateMessages.messageSignedMoreThanOneWeekAgo());
            }

            return true;
        }

        return false;
    }

    private boolean verify(final PreparedUpdate update, final X509Credential credential, final X509CertificateWrapper x509CertificateWrapper) {
        try {
            return credential.verify(x509CertificateWrapper.getCertificate());
        } catch (IllegalArgumentException e ) {
            logException(update, e);
            return false;
        }
    }

    @CheckForNull
    protected X509CertificateWrapper getKeyWrapper(final PreparedUpdate update, final UpdateContext updateContext, final String keyId) {
        try {
            final RpslObject object = rpslObjectDao.getByKey(ObjectType.KEY_CERT, keyId);
            return X509CertificateWrapper.parse(object);
        } catch (EmptyResultDataAccessException e) {
            updateContext.addMessage(update, UpdateMessages.keyNotFound(keyId));
            log(update, String.format("Unable to find %s: %s", keyId, e.getMessage()));
        } catch (RuntimeException e) {
            updateContext.addMessage(update, UpdateMessages.keyInvalid(keyId));
            log(update, String.format("Unable to parse %s for X509 signature: %s", keyId, e.getMessage()));
            logException(update, e);
        }

        return null;
    }

    private void log(final PreparedUpdate update, final String message) {
        loggerContext.logString(update.getUpdate(), getClass().getCanonicalName(), message);
    }

    private void logException(final PreparedUpdate update, final Throwable throwable) {
        loggerContext.logException(update, throwable);
    }
}
