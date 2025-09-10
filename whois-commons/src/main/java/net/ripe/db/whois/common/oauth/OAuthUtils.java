package net.ripe.db.whois.common.oauth;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.transform.FilterAuthFunction;
import net.ripe.db.whois.common.sso.AuthServiceClientException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class OAuthUtils {

    public static final String APIKEY_KEY_ID_QUERY_PARAM = "keyId";
    public static final String OAUTH_CUSTOM_UUID_PARAM = "ripe_user_id";
    public static final String OAUTH_CUSTOM_EMAIL_PARAM = "email";
    public static final String OAUTH_CUSTOM_AZP_PARAM = "azp";
    public static final String OAUTH_CUSTOM_SCOPE_PARAM = "scope";
    public static final String OAUTH_CUSTOM_JTI_PARAM = "jti";

    public static final String OAUTH_ANY_MNTNR_SCOPE = "whois.mntner:ANY:write";

    public static boolean validateScope(final OAuthSession oAuthSession, final List<RpslObject> maintainers) {

        final List<String> whoisScopes = getWhoisScopes(oAuthSession.getScopes());
        final List<CIString> maintainerKeys = maintainers.stream().map(RpslObject::getKey).toList();

        return whoisScopes.stream()
                .map( whoisScope ->  new OAuthSession.ScopeFormatter(whoisScope).getScopeKey())
                .anyMatch( scopeKey -> "ANY".equals(scopeKey) || maintainerKeys.contains(CIString.ciString(scopeKey)));
    }

    public static List<String> getWhoisScopes(final List<String> scopes) {
        return scopes.stream().filter(scope -> scope.startsWith("whois.mntner")).collect(Collectors.toList());
    }

    public static boolean hasValidOauthSession(final OAuthSession oAuthSession, final List<RpslObject> maintainers, final List<RpslAttribute> authAttributes) {
        if(oAuthSession == null || oAuthSession.getUuid() == null) {
            return false;
        }

        if(StringUtils.isNotEmpty(oAuthSession.getErrorStatus())) {
            return false;
        }

        if(!OAuthUtils.validateScope(oAuthSession, maintainers)) {
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
        if(authHeader == null || !authHeader.startsWith("Basic ")) {
            return null;
        }

        final String base64Credentials = authHeader.substring("Basic ".length()).trim();
        final byte[] credDecoded = new Base64().decode(base64Credentials);

        final String usernameWithPassword = new String(credDecoded, StandardCharsets.ISO_8859_1);

        return usernameWithPassword.contains(":") ?  StringUtils.substringBefore(usernameWithPassword, ":") : null;
    }
}
