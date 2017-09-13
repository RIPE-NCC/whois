package net.ripe.db.whois.update.sso;

import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.sso.AuthTranslator;
import net.ripe.db.whois.common.sso.CrowdClient;
import net.ripe.db.whois.common.sso.CrowdClientException;
import net.ripe.db.whois.common.sso.SsoHelper;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.CheckForNull;

@Component
public class SsoTranslator {
    private final CrowdClient crowdClient;

    @Autowired
    public SsoTranslator(final CrowdClient crowdClient) {
        this.crowdClient = crowdClient;
    }

    public void populateCacheAuthToUsername(final UpdateContext updateContext, final RpslObject rpslObject) {
        SsoHelper.translateAuth(rpslObject, new AuthTranslator() {
            @Override
            @CheckForNull
            public RpslAttribute translate(final String authType, final String authToken, final RpslAttribute originalAttribute) {
                if (authType.equals("SSO")) {
                    if (!updateContext.getSsoTranslation().containsUuid(authToken)) {
                        try {
                            final String username = crowdClient.getUsername(authToken);
                            updateContext.getSsoTranslation().put(username, authToken);
                        } catch (CrowdClientException e) {
                            if (!updateContext.getGlobalMessages().getErrors().contains(UpdateMessages.ripeAccessServerUnavailable())) {
                                updateContext.addGlobalMessage(UpdateMessages.ripeAccessServerUnavailable());
                            }
                        }
                    }
                }
                return null;
            }
        });
    }

    public void populateCacheAuthToUuid(final UpdateContext updateContext, final Update update) {
        final RpslObject submittedObject = update.getSubmittedObject();
        SsoHelper.translateAuth(submittedObject, new AuthTranslator() {
            @Override
            @CheckForNull
            public RpslAttribute translate(final String authType, final String authToken, final RpslAttribute originalAttribute) {
                if (authType.equals("SSO")) {
                    if (!updateContext.getSsoTranslation().containsUsername(authToken)) {
                        try {
                            final String uuid = crowdClient.getUuid(authToken);
                            updateContext.getSsoTranslation().put(authToken, uuid);
                        } catch (CrowdClientException e) {
                            updateContext.addMessage(update, originalAttribute, UpdateMessages.ripeAccessAccountUnavailable(authToken));
                        }
                    }
                }
                return null;
            }
        });
    }

    public RpslObject translateFromCacheAuthToUuid(final UpdateContext updateContext, final RpslObject rpslObject) {
        return SsoHelper.translateAuth(rpslObject, new AuthTranslator() {
            @Override
            @CheckForNull
            public RpslAttribute translate(String authType, String authToken, RpslAttribute originalAttribute) {
                if (authType.equals("SSO")) {
                    final String uuid = updateContext.getSsoTranslation().getUuid(authToken);
                    if (uuid != null) {
                        return new RpslAttribute(originalAttribute.getKey(), String.format("SSO %s", uuid));
                    }
                }
                return null;
            }
        });
    }

    public RpslObject translateFromCacheAuthToUsername(final UpdateContext updateContext, final RpslObject rpslObject) {
        return SsoHelper.translateAuth(rpslObject, new AuthTranslator() {
            @Override
            @CheckForNull
            public RpslAttribute translate(String authType, String authToken, RpslAttribute originalAttribute) {
                if (authType.equals("SSO")) {
                    final String username = updateContext.getSsoTranslation().getUsername(authToken);
                    if (username != null) {
                        return new RpslAttribute(originalAttribute.getKey(), String.format("SSO %s", username));
                    }
                }
                return null;
            }
        });
    }
}
