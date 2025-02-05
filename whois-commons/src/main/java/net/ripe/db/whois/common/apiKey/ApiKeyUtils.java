package net.ripe.db.whois.common.apiKey;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.util.internal.StringUtil;
import net.ripe.db.whois.common.Environment;
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

    public static boolean validateScope(final OAuthSession oAuthSession, final List<RpslObject> maintainers, final Environment environment) {
        if (StringUtil.isNullOrEmpty(oAuthSession.getScope())) {
            return true;
        }

        final List<OAuthSession.ScopeFormatter> whoisScope = getWhoisScope(oAuthSession).stream().map(OAuthSession.ScopeFormatter::new).toList();
        if (whoisScope.isEmpty()) {
            return true;
        }

        if (!isValidEnvironment(environment, whoisScope)) {
            return false;
        }

        final Optional<OAuthSession.ScopeFormatter> mntnerScope = whoisScope.stream()
                .filter(scopeFormatter -> OAuthSession.ScopeType.MNTNER.equals(scopeFormatter.getScopeType()))
                .findFirst();

        return mntnerScope.map(scopeFormatter -> maintainers.stream()
                        .anyMatch(maintainer -> scopeFormatter.getScopeKey().equalsIgnoreCase(maintainer.getKey().toString())))
                .orElse(true);

    }

    private static boolean isValidEnvironment(final Environment environment, final List<OAuthSession.ScopeFormatter> whoisScope) {
        final Optional<OAuthSession.ScopeFormatter> environmentScope = whoisScope.stream()
                .filter(scopeFormatter -> OAuthSession.ScopeType.ENVIRONMENT.equals(scopeFormatter.getScopeType()))
                .findFirst();

        return environmentScope.map(scopeFormatter -> environment.name().equals(scopeFormatter.getScopeKey())).orElse(true);
    }


    private static List<String> getWhoisScope(final OAuthSession oAuthSession) {
        final List<String> scopes = Arrays.asList(StringUtils.split(oAuthSession.getScope(), " "));
        return scopes.stream().filter(scope -> scope.startsWith("whois")).toList();
    }

    public static boolean hasValidApiKey(final OAuthSession oAuthSession, final List<RpslObject> maintainers, final List<RpslAttribute> authAttributes, final Environment environment) {
        if (oAuthSession == null || oAuthSession.getUuid() == null) {
            return false;
        }

        if (Arrays.stream(oAuthSession.getAud()).noneMatch(appName -> appName.equalsIgnoreCase("whois"))) {
            return false;
        }

        if (!validateScope(oAuthSession, maintainers, environment)) {
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
        final String apiKeyId = getApiKeyId(authHeader);
        return StringUtils.isAlphanumeric(apiKeyId) && (apiKeyId.length() == 24);
    }

    public static String getApiKeyId(final String authHeader) {
        if ("Basic ".startsWith(authHeader)) {
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
