package net.ripe.db.whois.update.authentication.credential;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.update.domain.PasswordCredential;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.log.LoggerContext;
import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.commons.codec.digest.Crypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
class PasswordCredentialValidator implements CredentialValidator<PasswordCredential> {
    private static final Pattern MD5_PATTERN = Pattern.compile("(?i)^.*MD5-PW \\$1\\$(.{1,8})\\$(.{22}).*$");
    private static final Pattern CRYPT_PATTERN = Pattern.compile("(?i)^.*CRYPT-PW (.{2})(.{11}).*$");
    private final LoggerContext loggerContext;

    private static boolean cryptEnabled = false;

    @Autowired
    PasswordCredentialValidator(LoggerContext loggerContext) {
        this.loggerContext = loggerContext;
    }

    @Value("${authentication.crypt.enabled:false}")
    public void setCryptAuthenticationEnabled(final boolean setCryptAuthenticationEnabled) {
        this.cryptEnabled = setCryptAuthenticationEnabled;
    }

    @Override
    public Class<PasswordCredential> getSupportedCredentials() {
        return PasswordCredential.class;
    }

    @Override
    public boolean hasValidCredential(final PreparedUpdate update, final UpdateContext updateContext, final Collection<PasswordCredential> offeredCredentials, final PasswordCredential knownCredential) {
        for (final PasswordCredential offeredCredential : offeredCredentials) {
            final Matcher matcher = MD5_PATTERN.matcher(knownCredential.getPassword());
            if (matcher.matches()) {
                try {
                    final String salt = matcher.group(1);
                    final String known = String.format("$1$%s$%s", salt, matcher.group(2));
                    final String offered = Md5Crypt.md5Crypt(offeredCredential.getPassword().getBytes(), String.format("$1$%s", salt));

                    if (known.equals(offered)) {
                        loggerContext.logString(
                                update.getUpdate(),
                                getClass().getCanonicalName(),
                                String.format("Validated %s against password: %s (encrypted: %s)", update.getFormattedKey(), offeredCredential.getPassword(), knownCredential.getPassword()));

                        return true;
                    }
                } catch (IllegalArgumentException e) {
                    updateContext.addGlobalMessage(new Message(Messages.Type.WARNING, e.getMessage()));
                }
            }
            final Matcher cryptMatcher = CRYPT_PATTERN.matcher(knownCredential.getPassword());
            if (cryptEnabled && cryptMatcher.matches()) {
                try {
                    final String salt = cryptMatcher.group(1);
                    final String known = String.format("%s%s", salt, cryptMatcher.group(2));
                    final String offered = Crypt.crypt(offeredCredential.getPassword().getBytes(), salt);

                    if (known.equals(offered)) {
                        loggerContext.logString(
                                update.getUpdate(),
                                getClass().getCanonicalName(),
                                String.format("Validated %s against CRYPT-PW password: %s (encrypted: %s)", update.getKey(), offeredCredential.getPassword(), knownCredential.getPassword()));

                        return true;
                    }
                } catch (IllegalArgumentException e) {
                    updateContext.addGlobalMessage(new Message(Messages.Type.WARNING, e.getMessage()));
                }
            }
        }

        return false;
    }
}
