package net.ripe.db.whois.update.authentication.credential;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.PgpCredential;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.keycert.PgpPublicKeyWrapper;
import net.ripe.db.whois.update.log.LoggerContext;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import javax.annotation.CheckForNull;
import java.util.Collection;
import java.util.List;

@Component
class PgpCredentialValidator implements CredentialValidator<PgpCredential> {

    private final RpslObjectDao rpslObjectDao;
    private final DateTimeProvider dateTimeProvider;
    private final LoggerContext loggerContext;

    @Autowired
    public PgpCredentialValidator(final RpslObjectDao rpslObjectDao, final DateTimeProvider dateTimeProvider, LoggerContext loggerContext) {
        this.rpslObjectDao = rpslObjectDao;
        this.dateTimeProvider = dateTimeProvider;
        this.loggerContext = loggerContext;
    }

    @Override
    public Class<PgpCredential> getSupportedCredentials() {
        return PgpCredential.class;
    }

    @Override
    public boolean hasValidCredential(final PreparedUpdate update, final UpdateContext updateContext, final Collection<PgpCredential> offeredCredentials, final PgpCredential knownCredential) {
        for (final PgpCredential offeredCredential : offeredCredentials) {
            if (verifySignedMessage(update, updateContext, offeredCredential, knownCredential)) {
                return true;
            }
        }

        return false;
    }

    private boolean verifySignedMessage(final PreparedUpdate update, final UpdateContext updateContext, final PgpCredential offeredCredential, final PgpCredential knownCredential) {
        final String keyId = knownCredential.getKeyId();
        final PgpPublicKeyWrapper pgpPublicKeyWrapper = getKeyWrapper(update, updateContext, keyId);
        if (pgpPublicKeyWrapper == null) {
            return false;
        }

        if (verify(update, offeredCredential, pgpPublicKeyWrapper.getPublicKey()) || verify(update, offeredCredential, pgpPublicKeyWrapper.getSubKeys())) {
            log(update, String.format("Validated %s with %s", update.getKey(), keyId));

            if (pgpPublicKeyWrapper.isExpired(dateTimeProvider)) {
                updateContext.addMessage(update, UpdateMessages.publicKeyHasExpired(keyId));
            }

            if (!offeredCredential.verifySigningTime(dateTimeProvider)) {
                updateContext.addMessage(update, UpdateMessages.messageSignedMoreThanOneWeekAgo());
            }

            return true;
        }

        return false;
    }

    private boolean verify(final PreparedUpdate update, final PgpCredential pgpCredential, final PGPPublicKey pgpPublicKey) {
        try {
            return pgpCredential.verify(pgpPublicKey);
        } catch (IllegalArgumentException e) {
            loggerContext.log(new Message(Messages.Type.WARNING, e.getMessage()), e);
            logException(update, e);
            return false;
        }
    }

    private boolean verify(final PreparedUpdate update, final PgpCredential pgpCredential, final List<PGPPublicKey> pgpPublicKeys) {
        for (PGPPublicKey next : pgpPublicKeys) {
            if (verify(update, pgpCredential, next)) {
                return true;
            }
        }
        return false;
    }

    @CheckForNull
    protected PgpPublicKeyWrapper getKeyWrapper(final PreparedUpdate update, final UpdateContext updateContext, final String keyId) {
        try {
            final RpslObject object = rpslObjectDao.getByKey(ObjectType.KEY_CERT, keyId);
            return PgpPublicKeyWrapper.parse(object);
        } catch (EmptyResultDataAccessException e) {
            updateContext.addMessage(update, UpdateMessages.keyNotFound(keyId));
            log(update, String.format("Unable to find %s: %s", keyId, e.getMessage()));
        } catch (RuntimeException e) {
            updateContext.addMessage(update, UpdateMessages.keyInvalid(keyId));
            log(update, String.format("Unable to parse %s for PGP signature: %s", keyId, e.getMessage()));
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
