package net.ripe.db.whois.update.sso;

import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.sso.AuthServiceClient;
import net.ripe.db.whois.common.sso.AuthServiceClientException;
import net.ripe.db.whois.common.sso.AuthTranslator;
import net.ripe.db.whois.common.sso.SsoHelper;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.CheckForNull;
import java.util.regex.Pattern;

@Component
public class SsoTranslator {
    private final AuthServiceClient authServiceClient;

    private final static Pattern SSO_SYNTAX_PATTERN = Pattern.compile("(.+@.+){1,90}");
    @Autowired
    public SsoTranslator(final AuthServiceClient authServiceClient) {
        this.authServiceClient = authServiceClient;
    }

    public void populateCacheAuth(final UpdateContext updateContext, final Update update){
        final RpslObject submittedObject = update.getSubmittedObject();
        SsoHelper.cacheAuthAttributes(submittedObject.getAttributes(), new AuthTranslator() {
            @Override
            public RpslAttribute translate(final String authType, final String authToken, final RpslAttribute originalAttribute) {
                if (!authType.equals("SSO") || isDuplicated(authToken, originalAttribute)) {
                    return originalAttribute;
                }

                try {
                    String userName;
                    String uuid;
                    if (SSO_SYNTAX_PATTERN.matcher(authToken).matches()) {
                        userName = authToken;
                        uuid = authServiceClient.getUuid(userName);
                    } else {
                        uuid = authToken;
                        userName = authServiceClient.getUsername(uuid);
                    }
                    updateContext.getSsoTranslation().put(userName, uuid);
                } catch (AuthServiceClientException e){
                    updateContext.addMessage(update, originalAttribute, UpdateMessages.ripeAccessAccountUnavailable(authToken));
                }
                return null;
            }

            private boolean isDuplicated(String authToken, RpslAttribute originalAttribute) {
                if (updateContext.getSsoTranslation().containsUsername(authToken)){
                    updateContext.addMessage(update, originalAttribute, UpdateMessages.duplicatedSsoAuth(authToken, updateContext.getSsoTranslation().getUuid(authToken)));
                    return true;
                }

                if (updateContext.getSsoTranslation().containsUuid(authToken)){
                    updateContext.addMessage(update, originalAttribute, UpdateMessages.duplicatedSsoAuth(updateContext.getSsoTranslation().getUsername(authToken), authToken));
                    return true;
                }
                return false;
            }
        });
    }

    public void populateCacheAuthToUsername(final UpdateContext updateContext, final RpslObject rpslObject) {
        SsoHelper.translateAuth(rpslObject, new AuthTranslator() {
            @Override
            @CheckForNull
            public RpslAttribute translate(final String authType, final String uuid, final RpslAttribute originalAttribute) {
                if (authType.equals("SSO")) {
                    if (!updateContext.getSsoTranslation().containsUuid(uuid)) {
                        try {
                            final String username = authServiceClient.getUsername(uuid);
                            updateContext.getSsoTranslation().put(username, uuid);
                        } catch (AuthServiceClientException e) {
                            if (!updateContext.getGlobalMessages().getErrors().contains(UpdateMessages.ripeAccessServerUnavailable())) {
                                updateContext.addGlobalMessage(UpdateMessages.ripeAccessServerUnavailable());
                            }
                        }
                    } else {
                        updateContext.addGlobalMessage(UpdateMessages.duplicatedSsoAuth(updateContext.getSsoTranslation().getUsername(uuid), uuid));
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
            public RpslAttribute translate(final String authType, final String userName, final RpslAttribute originalAttribute) {
                if (authType.equals("SSO")) {
                    if (!updateContext.getSsoTranslation().containsUsername(userName)) {
                        try {
                            final String uuid = authServiceClient.getUuid(userName);
                            updateContext.getSsoTranslation().put(userName, uuid);
                        } catch (AuthServiceClientException e) {
                            updateContext.addMessage(update, originalAttribute, UpdateMessages.ripeAccessAccountUnavailable(userName));
                        }
                    } else {
                        updateContext.addMessage(update, originalAttribute, UpdateMessages.duplicatedSsoAuth(userName, updateContext.getSsoTranslation().getUuid(userName)));
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
