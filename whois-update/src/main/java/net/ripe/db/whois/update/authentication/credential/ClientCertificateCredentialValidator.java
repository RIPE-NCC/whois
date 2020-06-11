package net.ripe.db.whois.update.authentication.credential;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.ClientCertificateCredential;
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
public class ClientCertificateCredentialValidator implements CredentialValidator<ClientCertificateCredential, X509Credential> {

    private final RpslObjectDao rpslObjectDao;
    private final DateTimeProvider dateTimeProvider;
    private final LoggerContext loggerContext;

    @Autowired
    public ClientCertificateCredentialValidator(final RpslObjectDao rpslObjectDao, final DateTimeProvider dateTimeProvider, final LoggerContext loggerContext) {
        this.rpslObjectDao = rpslObjectDao;
        this.dateTimeProvider = dateTimeProvider;
        this.loggerContext = loggerContext;
    }

    @Override
    public Class<ClientCertificateCredential> getSupportedCredentials() {
        return ClientCertificateCredential.class;
    }

    @Override
    public boolean hasValidCredential(final PreparedUpdate update, final UpdateContext updateContext, final Collection<ClientCertificateCredential> offeredCredentials, final X509Credential knownCredential) {
        for (final ClientCertificateCredential offeredCredential : offeredCredentials) {

            if (verifyClientCertificate(update, updateContext, offeredCredential, knownCredential)) {
                log(update, String.format("Successfully validated with keycert: %s", knownCredential.getKeyId()));
                return true;
            }
        }

        return false;
    }

    private boolean verifyClientCertificate(final PreparedUpdate update, final UpdateContext updateContext, final ClientCertificateCredential offeredCredential, final X509Credential knownCredential) {
        final String keyId = knownCredential.getKeyId();
        final X509CertificateWrapper x509CertificateWrapper = getKeyWrapper(update, updateContext, keyId);
        if (x509CertificateWrapper == null) {
            return false;
        }

        if (x509CertificateWrapper.isExpired(dateTimeProvider)) {
            updateContext.addMessage(update, UpdateMessages.certificateHasExpired(keyId));
        }

        if (x509CertificateWrapper.isNotYetValid(dateTimeProvider)) {
            updateContext.addMessage(update, UpdateMessages.certificateNotYetValid(keyId));
        }

        return x509CertificateWrapper.getFingerprint().equals(offeredCredential.getFingerprint());
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
