package net.ripe.db.whois.common.rpsl.transform;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.credentials.OverrideCredential;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.User;
import net.ripe.db.whois.common.oauth.OAuthSession;
import net.ripe.db.whois.common.override.OverrideCredentialValidator;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.PasswordHelper;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.common.rpsl.RpslObjectFilter;
import net.ripe.db.whois.common.sso.AuthServiceClient;
import net.ripe.db.whois.common.sso.UserSession;
import net.ripe.db.whois.common.x509.ClientAuthCertificateValidator;
import net.ripe.db.whois.common.x509.X509CertificateWrapper;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.ripe.db.whois.common.oauth.OAuthUtils.hasValidOauthSession;

/*
password and cookie parameters are used in rest api lookup ONLY, so the port43 netty worker pool is not affected by any SSO
server timeouts or network hiccups. Jetty could suffer from that, though - AH
 */
@ThreadSafe
public class FilterAuthFunction implements FilterFunction {
    public static final Pattern SSO_PATTERN = Pattern.compile("(?i)SSO (.*)");
    public static final Splitter SPACE_SPLITTER = Splitter.on(' ');
    public static final String FILTERED_APPENDIX = " # Filtered";

    private List<String> passwords = null;
    private OverrideCredential overrideCredential;
    private boolean isTrusted;
    private OAuthSession oAuthSession;
    private UserSession  userSession;
    private User overrideUser;
    private RpslObjectDao rpslObjectDao = null;
    private AuthServiceClient authServiceClient;
    private List<X509CertificateWrapper> certificates;
    private ClientAuthCertificateValidator clientAuthCertificateValidator;
    private OverrideCredentialValidator overrideCredentialValidator;


    public FilterAuthFunction(final List<String> passwords,
                              final User overrideUser,
                              final OAuthSession oAuthSession,
                              final UserSession userSession,
                              final AuthServiceClient authServiceClient,
                              final RpslObjectDao rpslObjectDao,
                              final List<X509CertificateWrapper> certificates,
                              final ClientAuthCertificateValidator clientAuthCertificateValidator,
                              final OverrideCredentialValidator overrideCredentialValidator,
                              final boolean isTrusted) {
        this.userSession = userSession;
        this.passwords = passwords;
        this.overrideUser = overrideUser;
        this.authServiceClient = authServiceClient;
        this.rpslObjectDao = rpslObjectDao;
        this.certificates = certificates;
        this.clientAuthCertificateValidator = clientAuthCertificateValidator;
        this.oAuthSession = oAuthSession;
        this.isTrusted = isTrusted;
        this.overrideCredentialValidator = overrideCredentialValidator;
    }

    public FilterAuthFunction() {
    }

    @Override @Nonnull
    public RpslObject apply(final RpslObject rpslObject) {
        final List<RpslAttribute> authAttributes = rpslObject.findAttributes(AttributeType.AUTH);

        if (authAttributes.isEmpty()) {
            return rpslObject;
        }

        final Map<RpslAttribute, RpslAttribute> replace = Maps.newHashMap();
        final boolean authenticated = isOverrideAuthenticated(ObjectType.MNTNER) || isMntnerAuthenticated(rpslObject);

        for (final RpslAttribute authAttribute : authAttributes) {
            final Iterator<String> authIterator = SPACE_SPLITTER.split(authAttribute.getCleanValue()).iterator();
            final String passwordType = authIterator.next().toUpperCase();

            if (authenticated) {
                if (passwordType.equals("SSO")) {
                    final String username = authServiceClient.getUsername(authIterator.next());
                    replace.put(authAttribute, new RpslAttribute(AttributeType.AUTH, "SSO " + username));
                }
            } else {
                if (passwordType.endsWith("-PW") || passwordType.equals("SSO")) {     // history table has CRYPT-PW, dummify that too!
                    replace.put(authAttribute, new RpslAttribute(AttributeType.AUTH, passwordType + FILTERED_APPENDIX));
                }
            }
        }

        if (replace.isEmpty()) {
            return rpslObject;
        } else {
            if (!authenticated) {
                RpslObjectFilter.addFilteredSourceReplacement(rpslObject, replace);
            }
            return new RpslObjectBuilder(rpslObject).replaceAttributes(replace).get();
        }
    }

    private boolean isOverrideAuthenticated(final ObjectType objectType){
        try {
            return overrideCredentialValidator != null && overrideCredentialValidator.isAllowedAndValid(isTrusted,
                    userSession, overrideUser, objectType);
        } catch (Exception e){
            return false;
        }
    }

    private boolean isMntnerAuthenticated(final RpslObject rpslObject) {
        if (CollectionUtils.isEmpty(passwords) && userSession == null && (certificates == null || certificates.isEmpty()) && (oAuthSession == null || oAuthSession.getUuid() == null)) {
            return false;
        }

        final List<RpslObject> maintainers = getMaintainers(rpslObject);

        final List<RpslAttribute> extendedAuthAttributes = Lists.newArrayList(rpslObject.findAttributes(AttributeType.AUTH));
        extendedAuthAttributes.addAll(getMntByAuthAttributes(maintainers));

        return passwordAuthentication(extendedAuthAttributes) || apiKeyAuthenticated(rpslObject, maintainers, extendedAuthAttributes) || ssoAuthentication(extendedAuthAttributes) || clientCertAuthentication(extendedAuthAttributes);
    }

    private Set<RpslAttribute> getMntByAuthAttributes(final List<RpslObject> maintainers) {

        final Set<RpslAttribute> auths = Sets.newHashSet();

        for (final RpslObject mntner : maintainers) {
            auths.addAll(mntner.findAttributes(AttributeType.AUTH));
        }

        return auths;
    }

    private List<RpslObject> getMaintainers(final RpslObject rpslObject) {
        final Set<CIString> maintainers = rpslObject.getValuesForAttribute(AttributeType.MNT_BY);
        maintainers.remove(rpslObject.getKey());

        if (maintainers.isEmpty()) {
            return new ArrayList<>();
        }

        return rpslObjectDao.getByKeys(ObjectType.MNTNER, maintainers);
    }

    private boolean ssoAuthentication(final List<RpslAttribute> authAttributes) {
        if (userSession == null) {
            return false;
        }

        for (RpslAttribute attribute : authAttributes) {
            final Matcher matcher = SSO_PATTERN.matcher(attribute.getCleanValue().toString());
            if (matcher.matches()) {
                if (userSession.getUuid() != null && userSession.getUuid().equals(matcher.group(1))) {
                        return true;
                }
            }
        }

        return false;
    }

    private boolean clientCertAuthentication(final List<RpslAttribute> authAttributes){
        return clientAuthCertificateValidator != null && clientAuthCertificateValidator.existValidCertificate(authAttributes, certificates);
    }

    private boolean passwordAuthentication(final List<RpslAttribute> authAttributes) {
        if (CollectionUtils.isEmpty(passwords)) {
            return false;
        }

        for (final RpslAttribute authAttribute : authAttributes) {
            if (PasswordHelper.authenticateMd5Passwords(authAttribute.getCleanValue().toString(), passwords)) {
                return true;
            }
        }

        return false;
    }

    private boolean apiKeyAuthenticated(final RpslObject rpslObject, final List<RpslObject> maintainers, final List<RpslAttribute> authAttributes) {
        if(rpslObject.getType().equals(ObjectType.MNTNER)) {
            maintainers.add(rpslObject);
        }

        return hasValidOauthSession(oAuthSession, maintainers, authAttributes);
    }
}
