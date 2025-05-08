package net.ripe.db.whois.update.authentication.credential;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.credentials.ClientCertificateCredential;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.domain.X509Credential;
import net.ripe.db.whois.common.x509.X509CertificateWrapper;
import net.ripe.db.whois.update.log.LoggerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import javax.annotation.CheckForNull;
import java.util.Collection;

@Component
public class ClientCertificateCredentialValidator implements CredentialValidator<ClientCertificateCredential, X509Credential> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientCertificateCredentialValidator.class);

    private final RpslObjectDao rpslObjectDao;
    private final DateTimeProvider dateTimeProvider;
    private final LoggerContext loggerContext;

    private final boolean enabled;

    @Autowired
    public ClientCertificateCredentialValidator(final RpslObjectDao rpslObjectDao,
                                                final DateTimeProvider dateTimeProvider,
                                                final LoggerContext loggerContext,
                                                @Value("${port.client.auth:-1}") final int clientAuthPort) {
        this.rpslObjectDao = rpslObjectDao;
        this.dateTimeProvider = dateTimeProvider;
        this.loggerContext = loggerContext;
        this.enabled = clientAuthPort >= 0;

        LOGGER.info("Client certificate authentication is {}abled", enabled? "en" : "dis");
    }

    @Override
    public Class<X509Credential> getSupportedCredentials() {
        return X509Credential.class;
    }

    @Override
    public Class<ClientCertificateCredential> getSupportedOfferedCredentialType() {
        return ClientCertificateCredential.class;
    }

    @Override
    public boolean hasValidCredential(final PreparedUpdate update, final UpdateContext updateContext, final Collection<ClientCertificateCredential> offeredCredentials, final X509Credential knownCredential, final RpslObject maintainer) {
        if (!enabled) {
            return false;
        }

        for (final ClientCertificateCredential offeredCredential : offeredCredentials) {
            log(update, String.format("Validating with offered client certificate %s", offeredCredential.getFingerprint()));

            if (verifyClientCertificate(update, updateContext, offeredCredential, knownCredential)) {
                log(update, String.format("Successfully validated with keycert: %s", knownCredential.getKeyId()));
                return true;
            }
        }

        return false;
    }

    private boolean verifyClientCertificate(final PreparedUpdate update, final UpdateContext updateContext, final ClientCertificateCredential offeredCredential, final X509Credential knownCredential) {
        // TODO: [MH] Consider to use ClientAuthCertificate from commons module, some validations for update and get
        //  is used here
        final String keyId = knownCredential.getKeyId();
        final X509CertificateWrapper x509CertificateWrapper = getKeyWrapper(update, updateContext, keyId);
        if (x509CertificateWrapper == null) {
            return false;
        }

        if (x509CertificateWrapper.isExpired(dateTimeProvider)) {
            updateContext.addMessage(update, UpdateMessages.certificateHasExpired(keyId));
            return false;
        }

        if (x509CertificateWrapper.isNotYetValid(dateTimeProvider)) {
            updateContext.addMessage(update, UpdateMessages.certificateNotYetValid(keyId));
            return false;
        }

        return x509CertificateWrapper.getCertificate().equals(offeredCredential.getCertificate());
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
