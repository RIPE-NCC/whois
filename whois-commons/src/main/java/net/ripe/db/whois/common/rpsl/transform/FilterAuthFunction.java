package net.ripe.db.whois.common.rpsl.transform;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.PasswordHelper;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.common.rpsl.RpslObjectFilter;
import net.ripe.db.whois.common.sso.CrowdClient;
import net.ripe.db.whois.common.sso.CrowdClientException;
import net.ripe.db.whois.common.sso.SsoTokenTranslator;
import net.ripe.db.whois.common.sso.UserSession;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
password and cookie parameters are reserved for rest api, so the port43 netty worker pool is not affected by any SSO
server timeouts or network hiccups. Jetty could suffer from that, though - AH
 */
@ThreadSafe
public class FilterAuthFunction implements FilterFunction {
    private static final Pattern SSO_PATTERN = Pattern.compile("(?i)SSO (.*)");
    public static final Splitter SPACE_SPLITTER = Splitter.on(' ');
    public static final String FILTERED_APPENDIX = " # Filtered";

    private List<String> passwords = null;
    private String token = null;
    private RpslObjectDao rpslObjectDao = null;
    private SsoTokenTranslator ssoTokenTranslator;
    private CrowdClient crowdClient;

    public FilterAuthFunction(final List<String> passwords,
                              final String token,
                              final SsoTokenTranslator ssoTokenTranslator,
                              final CrowdClient crowdClient,
                              final RpslObjectDao rpslObjectDao) {
        this.token = token;
        this.passwords = passwords;
        this.ssoTokenTranslator = ssoTokenTranslator;
        this.crowdClient = crowdClient;
        this.rpslObjectDao = rpslObjectDao;
    }

    public FilterAuthFunction() {
    }

    @Override
    public RpslObject apply(final RpslObject rpslObject) {
        final List<RpslAttribute> authAttributes = rpslObject.findAttributes(AttributeType.AUTH);
        if (authAttributes.isEmpty()) {
            return rpslObject;
        }

        final Map<RpslAttribute, RpslAttribute> replace = Maps.newHashMap();
        final boolean authenticated = isMntnerAuthenticated(passwords, token, rpslObject, rpslObjectDao);

        for (final RpslAttribute authAttribute : authAttributes) {
            final Iterator<String> authIterator = SPACE_SPLITTER.split(authAttribute.getCleanValue()).iterator();
            final String passwordType = authIterator.next().toUpperCase();

            if (authenticated) {
                if (passwordType.equals("SSO")) {
                    try {
                        final String username = crowdClient.getUsername(authIterator.next());
                        replace.put(authAttribute, new RpslAttribute(AttributeType.AUTH, "SSO " + username));
                    } catch (CrowdClientException e) {
                        // TODO: [ES] substitute empty SSO value
                        replace.put(authAttribute, new RpslAttribute(AttributeType.AUTH, "SSO"));
                    }
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

    private boolean isMntnerAuthenticated(final List<String> passwords, final String token, final RpslObject rpslObject, final RpslObjectDao rpslObjectDao) {
        if (CollectionUtils.isEmpty(passwords) && StringUtils.isBlank(token)) {
            return false;
        }

        final List<RpslAttribute> extendedAuthAttributes = Lists.newArrayList();
        final List<RpslAttribute> authAttributes = rpslObject.findAttributes(AttributeType.AUTH);

        extendedAuthAttributes.addAll(authAttributes);
        extendedAuthAttributes.addAll(getMntByAuthAttributes(rpslObject, rpslObjectDao));

        return passwordAuthentication(passwords, extendedAuthAttributes) || ssoAuthentication(token, extendedAuthAttributes);
    }

    private Set<RpslAttribute> getMntByAuthAttributes(final RpslObject rpslObject, final RpslObjectDao rpslObjectDao) {
        final Set<CIString> maintainers = rpslObject.getValuesForAttribute(AttributeType.MNT_BY);
        maintainers.remove(rpslObject.getKey());

        if (maintainers.isEmpty()) {
            return Collections.emptySet();
        }

        final Set<RpslAttribute> auths = Sets.newHashSet();
        final List<RpslObject> mntByMntners = rpslObjectDao.getByKeys(ObjectType.MNTNER, maintainers);

        for (RpslObject mntner : mntByMntners) {
            auths.addAll(mntner.findAttributes(AttributeType.AUTH));
        }
        return auths;
    }

    private boolean ssoAuthentication(final String token, final List<RpslAttribute> authAttributes) {
        if (StringUtils.isBlank(token)) {
            return false;
        }

        for (RpslAttribute attribute : authAttributes) {
            final Matcher matcher = SSO_PATTERN.matcher(attribute.getCleanValue().toString());
            if (matcher.matches()) {
                try {
                    final UserSession userSession = ssoTokenTranslator.translateSsoToken(token);
                    if (userSession != null && userSession.getUuid().equals(matcher.group(1))) {
                        return true;
                    }
                } catch (CrowdClientException e) {
                    return false;
                }
            }
        }

        return false;
    }

    private boolean passwordAuthentication(final List<String> passwords, final List<RpslAttribute> authAttributes) {
        if (CollectionUtils.isEmpty(passwords)) {
            return false;
        }

        for (RpslAttribute authAttribute : authAttributes) {
            if (PasswordHelper.authenticateMd5Passwords(authAttribute.getCleanValue().toString(), passwords)) {
                return true;
            }
        }
        return false;
    }
}
