package net.ripe.db.whois.update.authentication.credential;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.apiKey.ApiKeyUtils;
import net.ripe.db.whois.common.apiKey.OAuthSession;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.APIKeyCredential;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.SsoCredential;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.log.LoggerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static net.ripe.db.whois.common.apiKey.ApiKeyUtils.validateScope;

@Component
public class ApiKeyCredentialValidator implements CredentialValidator<APIKeyCredential, SsoCredential> {
    private final LoggerContext loggerContext;

    @Value("${apikey.authenticate.enabled:false}")
    private boolean enabled;

    @Value("${keycloak.idp.client:whois}")
    private String whoisKeycloakId;

    @Autowired
    public ApiKeyCredentialValidator(final LoggerContext loggerContext) {
        this.loggerContext = loggerContext;
    }

    @Override
    public Class<SsoCredential> getSupportedCredentials() {
        return SsoCredential.class;
    }

    @Override
    public Class<APIKeyCredential> getSupportedOfferedCredentialType() {
        return APIKeyCredential.class;
    }

    @Override
    public boolean hasValidCredential(final PreparedUpdate update, final UpdateContext updateContext, final Collection<APIKeyCredential> offeredCredentials, final SsoCredential knownCredential, final RpslObject maintainer) {
        if(!enabled) {
            return false;
        }

        for (final APIKeyCredential offered : offeredCredentials) {

            final OAuthSession oAuthSession = offered.getOfferedOAuthSession();

            if(!ApiKeyUtils.validateAudience(oAuthSession, whoisKeycloakId)) {
                updateContext.addMessage(update, UpdateMessages.invalidApiKeyAudience());
                return false;
            }

            if(!validateScope(oAuthSession, List.of(maintainer))) {
                continue;
            }

            if (oAuthSession.getUuid() != null && oAuthSession.getUuid().equals(knownCredential.getKnownUuid())) {
                log(update, String.format("Validated %s with API KEY for user: %s with apiKey: %s.", update.getFormattedKey(), oAuthSession.getEmail(), oAuthSession.getKeyId()));

                update.getUpdate().setEffectiveCredential(String.format("%s (%s)", oAuthSession.getEmail(), oAuthSession.getKeyId()), Update.EffectiveCredentialType.APIKEY);
                return true;
            }
        }
        return false;
    }

    private void log(final PreparedUpdate update, final String message) {
        loggerContext.logString(update.getUpdate(), getClass().getCanonicalName(), message);
    }
}
