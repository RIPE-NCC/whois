package net.ripe.db.whois.update.authentication.credential;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.oauth.APIKeySession;
import net.ripe.db.whois.common.oauth.OAuthSession;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.OAuthCredential;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.SsoCredential;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.log.LoggerContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

import static net.ripe.db.whois.common.oauth.OAuthUtils.validateScope;

@Component
public class OAuthCredentialValidator implements CredentialValidator<OAuthCredential, SsoCredential> {
    private final LoggerContext loggerContext;

    @Value("${apikey.authenticate.enabled:false}")
    private boolean enabled;

    @Autowired
    public OAuthCredentialValidator(final LoggerContext loggerContext) {
        this.loggerContext = loggerContext;
    }

    @Override
    public Class<SsoCredential> getSupportedCredentials() {
        return SsoCredential.class;
    }

    @Override
    public Class<OAuthCredential> getSupportedOfferedCredentialType() {
        return OAuthCredential.class;
    }

    @Override
    public boolean hasValidCredential(final PreparedUpdate update, final UpdateContext updateContext, final Collection<OAuthCredential> offeredCredentials, final SsoCredential knownCredential, final RpslObject maintainer) {
        if(!enabled) {
            return false;
        }

        for (final OAuthCredential offered : offeredCredentials) {

            final OAuthSession oAuthSession = offered.getOfferedOAuthSession();
            if(oAuthSession == null) {
                continue;
            }

            if(StringUtils.isNotEmpty(oAuthSession.getErrorStatus())) {
                updateContext.addMessage(update,  new Message(Messages.Type.WARNING, oAuthSession.getErrorStatus()));
                return false;
            }

            if(!validateScope(oAuthSession, List.of(maintainer))) {
                continue;
            }

            if (oAuthSession.getUuid() != null && oAuthSession.getUuid().equals(knownCredential.getKnownUuid())) {

                Update.EffectiveCredentialType effectiveCredentialType;
                String effectiveCredential;
                String updateMessage;

                if(oAuthSession instanceof APIKeySession) {
                    effectiveCredentialType = Update.EffectiveCredentialType.APIKEY;
                    effectiveCredential = String.format("%s (%s)", oAuthSession.getEmail(), ((APIKeySession) oAuthSession).getKeyId());
                    updateMessage = String.format("Validated %s with API KEY for user: %s with keyId: %s.", update.getFormattedKey(), oAuthSession.getEmail(), ((APIKeySession) oAuthSession).getKeyId());
                } else {
                    effectiveCredentialType = Update.EffectiveCredentialType.OAUTH;
                    effectiveCredential = oAuthSession.getEmail();
                    updateMessage = String.format("Validated %s with OAuth Session for user: %s.", update.getFormattedKey(), oAuthSession.getEmail());
                }

                log(update, updateMessage);
                update.getUpdate().setEffectiveCredential(effectiveCredential, effectiveCredentialType);

                return true;
            }
        }
        return false;
    }

    private void log(final PreparedUpdate update, final String message) {
        loggerContext.logString(update.getUpdate(), getClass().getCanonicalName(), message);
    }
}
