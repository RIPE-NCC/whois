package net.ripe.db.whois.common.apiKey;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.transform.FilterAuthFunction;
import net.ripe.db.whois.common.sso.AuthServiceClientException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;

public class ApiKeyUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiKeyUtils.class);

    public static final String APIKEY_QUERY_PARAM = "oAuthSession";
    public static boolean validateScope(final OAuthSession oAuthSession, final List<RpslObject> maintainers) {
        if(CollectionUtils.isEmpty(oAuthSession.getScopes())) {
            return true;
        }

        final OAuthSession.ScopeFormatter scopeFormatter = new OAuthSession.ScopeFormatter(oAuthSession.getScopes().getFirst());
        return scopeFormatter.getAppName().equalsIgnoreCase("whois")
                    && scopeFormatter.getScopeType().equalsIgnoreCase(ObjectType.MNTNER.getName())
                    && maintainers.stream().anyMatch( maintainer -> scopeFormatter.getScopeKey().equalsIgnoreCase(maintainer.getKey().toString()));
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

    public static boolean isAPIKeyRequest(final String authHeader) {
        final String accessKey = getAccessKey(authHeader);
        return StringUtils.isAlphanumeric(accessKey) && (accessKey.length() == 24);
    }

    public static String getAccessKey(final String authHeader) {
        if(authHeader == null || !authHeader.startsWith("Basic ")) {
            return null;
        }

        final String base64Credentials = authHeader.substring("Basic ".length()).trim();
        final byte[] credDecoded = new Base64().decode(base64Credentials);

        final String usernameWithPassword = new String(credDecoded, StandardCharsets.ISO_8859_1);

        return usernameWithPassword.contains(":") ?  StringUtils.substringBefore(usernameWithPassword, ":") : null;
    }

    @Nullable
    public static OAuthSession getOAuthSession(final String payload) {
        if(payload == null || payload.isEmpty()) {
            return null;
        }
        try {
            return new ObjectMapper().readValue(payload, OAuthSession.class);
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to serialize OAuthSession, this should never have happened", e);
            return null;
        }
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
