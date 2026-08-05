package net.ripe.db.whois.common.oauth;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.transform.FilterAuthFunction;
import net.ripe.db.whois.common.sso.AuthServiceClientException;
import net.ripe.db.whois.common.sso.UserSession;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.splitByWholeSeparator;

public class OAuthUtils {

    public static final String APIKEY_KEY_ID_QUERY_PARAM = "keyId";
    public static final String OAUTH_CUSTOM_UUID_PARAM = "ripe_user_id";
    public static final String OAUTH_CUSTOM_EMAIL_PARAM = "email";
    public static final String OAUTH_CUSTOM_AZP_PARAM = "azp";
    public static final String OAUTH_CUSTOM_ACTIVE_PARAM = "active";
    public static final String OAUTH_CUSTOM_SCOPE_PARAM = "scope";
    public static final String OAUTH_CUSTOM_JTI_PARAM = "jti";

    public static final String OAUTH_ANY_MNTNR_SCOPE = "whois.mntner:ANY.write";

    public static boolean validateScope(final AbstractOAuthSession abstractOAuthSession, final List<RpslObject> maintainers) {

        final List<String> whoisScopes = getWhoisMntnerScopes(abstractOAuthSession.getScopes());
        final List<CIString> maintainerKeys = maintainers.stream().map(RpslObject::getKey).toList();

        return whoisScopes.stream()
                .map( whoisScope ->  new AbstractOAuthSession.ScopeFormatter(whoisScope).getScopeKey())
                .anyMatch( scopeKey -> "ANY".equals(scopeKey) || maintainerKeys.contains(CIString.ciString(scopeKey)));
    }

    public static List<String> getWhoisMntnerScopes(final String scopeString) {
        if (StringUtils.isBlank(scopeString)) {
            return Collections.emptyList();
        }
        final List<String> scopes = Arrays.asList(splitByWholeSeparator(scopeString, " "));
        return getWhoisMntnerScopes(scopes);
    }

    public static List<String> getWhoisMntnerScopes(final List<String> scopes) {
        return scopes.stream().filter(scope -> scope.startsWith("whois.mntner")).collect(Collectors.toList());
    }

    public static UserSession translateOidcToUserSession(final OidcSession oidcSession){
        return new UserSession(oidcSession.getUuid(), oidcSession.getEmail(), null, true, null);
    }

    public static boolean hasValidOauthSession(final AbstractOAuthSession abstractOAuthSession, final List<RpslObject> maintainers, final List<RpslAttribute> authAttributes) {
        if(abstractOAuthSession == null || abstractOAuthSession.getUuid() == null) {
            return false;
        }

        if(StringUtils.isNotEmpty(abstractOAuthSession.getErrorStatus())) {
            return false;
        }

        if(!OAuthUtils.validateScope(abstractOAuthSession, maintainers)) {
            return false;
        }

        for (final RpslAttribute attribute : authAttributes) {
            final Matcher matcher = FilterAuthFunction.SSO_PATTERN.matcher(attribute.getCleanValue().toString());
            if (matcher.matches()) {
                try {
                    if (abstractOAuthSession.getUuid().equals(matcher.group(1))) {
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
