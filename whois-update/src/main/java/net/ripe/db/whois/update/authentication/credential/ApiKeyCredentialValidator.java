package net.ripe.db.whois.update.authentication.credential;

import net.ripe.db.whois.common.apiKey.OAuthSession;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.APIKeyCredential;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.SsoCredential;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.log.LoggerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class ApiKeyCredentialValidator implements CredentialValidator<APIKeyCredential, SsoCredential> {
    private final LoggerContext loggerContext;

    @Value("${apikey.authenticate.enabled:false}")
    private boolean enabled;

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

            if(!oAuthSession.getScopes().isEmpty()) {
                final ScopeFormatter scopeFormatter = new ScopeFormatter(offered.getOfferedOAuthSession().getScopes().getFirst());
                if(!validateScope(maintainer, scopeFormatter)) {
                    continue;
                }
            }

            if (oAuthSession.getUuid().equals(knownCredential.getKnownUuid())) {
                log(update, String.format("Validated %s with API KEY for user: %s with apiKey: %s.", update.getFormattedKey(), oAuthSession.getEmail(), oAuthSession.getAccessKey()));

                update.getUpdate().setEffectiveCredential(oAuthSession.getAccessKey(), Update.EffectiveCredentialType.APIKEY);
                return true;
            }
        }
        return false;
    }

    private void log(final PreparedUpdate update, final String message) {
        loggerContext.logString(update.getUpdate(), getClass().getCanonicalName(), message);
    }

    private static boolean validateScope(final RpslObject maintainer, final ScopeFormatter scopeFormatter) {
        return scopeFormatter.getAppName().equalsIgnoreCase("whois")
                && scopeFormatter.getScopeType().equalsIgnoreCase(ObjectType.MNTNER.getName())
                && scopeFormatter.getScopeKey().equalsIgnoreCase(maintainer.getKey().toString());
    }

    static class ScopeFormatter {

        final String appName;
        final String scopeType;
        final String scopeKey;

        public ScopeFormatter(final String scope) {
            final String[] parts = scope.split(":|\\.");
            this.appName = parts[0];
            this.scopeType = parts[1];
            this.scopeKey = parts[2];
        }

        public String getScopeType() {
            return scopeType;
        }

        public String getScopeKey() {
            return scopeKey;
        }

        public String getAppName() {
            return appName;
        }
    }
}
