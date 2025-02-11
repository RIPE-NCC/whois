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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;

public class ApiKeyUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiKeyUtils.class);

    public static final String APIKEY_KEY_ID_QUERY_PARAM = "keyId";

    public static boolean validateScope(final OAuthSession oAuthSession, final List<RpslObject> maintainers) {
        if(StringUtils.isEmpty(oAuthSession.getScope())) {
            return true;
        }

        final Optional<String> whoisScope = getWhoisScope(oAuthSession);
        if(whoisScope.isEmpty()) {
            return true;
        }

        final OAuthSession.ScopeFormatter scopeFormatter = new OAuthSession.ScopeFormatter(whoisScope.get());

        if(StringUtils.isEmpty(scopeFormatter.getScopeKey()) || StringUtils.isEmpty(scopeFormatter.getScopeType()) || StringUtils.isEmpty(scopeFormatter.getAppName())) {
            return true;
        }

        return "whois".equalsIgnoreCase(scopeFormatter.getAppName())
                    && ObjectType.MNTNER.getName().equalsIgnoreCase(scopeFormatter.getScopeType())
                    && maintainers.stream().anyMatch( maintainer -> scopeFormatter.getScopeKey().equalsIgnoreCase(maintainer.getKey().toString()));
    }

    private static Optional<String> getWhoisScope(OAuthSession oAuthSession) {
        final List<String> scopes = Arrays.asList(StringUtils.split(oAuthSession.getScope(), " "));
        return scopes.stream().filter(scope -> scope.startsWith("whois")).findFirst();
    }

    public static boolean hasValidApiKey(final OAuthSession oAuthSession, final List<RpslObject> maintainers, final List<RpslAttribute> authAttributes) {
        if(oAuthSession == null || oAuthSession.getUuid() == null) {
            return false;
        }

        if(!ApiKeyUtils.validateScope(oAuthSession, maintainers)) {
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
        return oAuthSession != null && oAuthSession.getAud() != null && Arrays.stream(oAuthSession.getAud()).anyMatch(appName -> appName.equalsIgnoreCase(keycloakClientId));
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

    public static String getOAuthSession(final OAuthSession oAuthSession) {
        try {
            return new ObjectMapper().writeValueAsString(oAuthSession);
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to serialize OAuthSession, this should never have happened", e);
            return null;
        }
    }
}
