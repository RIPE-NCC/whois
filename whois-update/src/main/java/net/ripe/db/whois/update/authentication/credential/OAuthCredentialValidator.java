package net.ripe.db.whois.update.authentication.credential;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.credentials.OAuthCredential;
import net.ripe.db.whois.common.credentials.SsoCredential;
import net.ripe.db.whois.common.oauth.APIKeySession;
import net.ripe.db.whois.common.oauth.AbstractOAuthSession;
import net.ripe.db.whois.common.oauth.ApiKeyDetailsCacheManager;
import net.ripe.db.whois.common.oauth.DefaultOauthSession;
import net.ripe.db.whois.common.oauth.OidcSession;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.log.LoggerContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static net.ripe.db.whois.common.oauth.OAuthUtils.validateScope;

@Component
public class OAuthCredentialValidator implements CredentialValidator<OAuthCredential, SsoCredential> {
    private final LoggerContext loggerContext;


    private final DateTimeProvider dateTimeProvider;

    private final ApiKeyDetailsCacheManager apiKeyDetailsCacheManager;

    @Autowired
    public OAuthCredentialValidator(final LoggerContext loggerContext, final ApiKeyDetailsCacheManager apiKeyDetailsCacheManager, final DateTimeProvider dateTimeProvider) {
        this.loggerContext = loggerContext;
        this.apiKeyDetailsCacheManager = apiKeyDetailsCacheManager;
        this.dateTimeProvider = dateTimeProvider;
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

        for (final OAuthCredential offered : offeredCredentials) {

            final AbstractOAuthSession abstractOAuthSession = offered.getOfferedOAuthSession();
            if(abstractOAuthSession == null) {
                continue;
            }

            if(StringUtils.isNotEmpty(abstractOAuthSession.getErrorStatus())) {
                updateContext.addMessage(update, new Message(Messages.Type.WARNING, abstractOAuthSession.getErrorStatus()));
                return false;
            }

            if(!validateScope(abstractOAuthSession, List.of(maintainer))) {
                continue;
            }

            if (abstractOAuthSession.getUuid() != null && abstractOAuthSession.getUuid().equals(knownCredential.getKnownUuid())) {

                Update.EffectiveCredentialType effectiveCredentialType;
                String effectiveCredential;
                String updateMessage;

                switch (abstractOAuthSession){
                    case APIKeySession apiKeySession -> {
                        addExpiryWarning(update, updateContext, apiKeySession);

                        effectiveCredentialType = Update.EffectiveCredentialType.APIKEY;
                        effectiveCredential = String.format("%s (%s)", apiKeySession.getEmail(), apiKeySession.getKeyId());
                        updateMessage = String.format("Validated %s with API KEY for user: %s with keyId: %s.", update.getFormattedKey(), apiKeySession.getEmail(), apiKeySession.getKeyId());
                    }
                    case OidcSession oidcSession -> {
                        effectiveCredentialType = Update.EffectiveCredentialType.OIDC;
                        effectiveCredential = oidcSession.getEmail();
                        updateMessage = String.format("Validated %s with OIDC Session for user: %s.", update.getFormattedKey(), oidcSession.getEmail());
                    }
                    case DefaultOauthSession oauthSessionAbstract -> {
                        effectiveCredentialType = Update.EffectiveCredentialType.OAUTH;
                        effectiveCredential = abstractOAuthSession.getEmail();
                        updateMessage = String.format("Validated %s with OAuth Session for user: %s.", update.getFormattedKey(), oauthSessionAbstract.getEmail());
                    }
                    default -> throw new IllegalArgumentException("Failed OAuthSession Session");
                }

                log(update, updateMessage);
                update.getUpdate().setEffectiveCredential(effectiveCredential, effectiveCredentialType);

                return true;
            }
        }
        return false;
    }

    private void addExpiryWarning(final PreparedUpdate update, final UpdateContext updateContext, final APIKeySession apiKeySession) {
        final LocalDate expiriesAt = apiKeyDetailsCacheManager.getExpiryForKeyId(apiKeySession.getKeyId());
        if(expiriesAt == null) {
            return;
        }

        if (dateTimeProvider.getCurrentDate().plusWeeks(2).isAfter(expiriesAt)) {
            updateContext.addMessage(update, UpdateMessages.apiKeyGettingExpired(apiKeySession.getKeyId(), expiriesAt.toString()));
        }
    }

    private void log(final PreparedUpdate update, final String message) {
        loggerContext.logString(update.getUpdate(), getClass().getCanonicalName(), message);
    }
}
