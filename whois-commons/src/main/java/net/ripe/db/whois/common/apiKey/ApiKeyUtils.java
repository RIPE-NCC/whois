package net.ripe.db.whois.common.apiKey;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.transform.FilterAuthFunction;
import net.ripe.db.whois.common.sso.AuthServiceClientException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;

public class ApiKeyUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiKeyUtils.class);

    public static final String APIKEY_KEY_ID_QUERY_PARAM = "keyId";

    public static boolean validateMntnrScope(final OAuthSession oAuthSession, final List<RpslObject> maintainers) {
        final List<String> mntnrScopes =  oAuthSession.getScopes().stream()
                .filter( scope -> ObjectType.MNTNER.getName().equalsIgnoreCase(scope.getScopeType()))
                .map(OAuthSession.ScopeFormatter::getScopeKey).toList();

        return mntnrScopes.isEmpty() || maintainers.stream().map(rpslObject -> rpslObject.getKey().toString()).anyMatch(mntnrScopes::contains);
    }

    public static boolean hasValidApiKey(final OAuthSession oAuthSession, final List<RpslObject> maintainers, final List<RpslAttribute> authAttributes) {
        if(oAuthSession == null || oAuthSession.getUuid() == null) {
            return false;
        }

        if(!ApiKeyUtils.validateMntnrScope(oAuthSession, maintainers)) {
            return false;
        }

        for (final RpslAttribute attribute : authAttributes) {
            final Matcher matcher = FilterAuthFunction.SSO_PATTERN.matcher(attribute.getCleanValue().toString());
            if (matcher.matches()) {
                try {
                    if (oAuthSession.getUuid().equals(matcher.group(1))) {
                        return true;
                    }
                } catch (AuthServiceClientException e) {
                    return false;
                }
            }
        }

        return false;
    }

    public static boolean validateAudience(final OAuthSession oAuthSession, final String keycloakClientId) {
        return oAuthSession != null && oAuthSession.getAud() != null && oAuthSession.getAud().stream().anyMatch(appName -> appName.equalsIgnoreCase(keycloakClientId));
    }

    public static boolean isAPIKeyRequest(final String authHeader) {
        final String apiKeyId = getApiKeyId(authHeader);
        return StringUtils.isAlphanumeric(apiKeyId) && (apiKeyId.length() == 24);
    }

    public static String getApiKeyId(final String authHeader) {
        if(authHeader == null || !authHeader.startsWith("Basic ")) {
            return null;
        }

        final String base64Credentials = authHeader.substring("Basic ".length()).trim();
        final byte[] credDecoded = new Base64().decode(base64Credentials);

        final String usernameWithPassword = new String(credDecoded, StandardCharsets.ISO_8859_1);

        return usernameWithPassword.contains(":") ?  StringUtils.substringBefore(usernameWithPassword, ":") : null;
    }
}
