package net.ripe.db.whois.common.rpsl.transform;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.PasswordHelper;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.common.rpsl.RpslObjectFilter;
import net.ripe.db.whois.common.sso.CrowdClient;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/*
password and cookie parameters are reserved for rest api, so the port43 netty worker pool is not affected by any SSO
server timeouts or network hiccups. Jetty could suffer from that, though - AH
 */
@ThreadSafe
public class FilterAuthFunction implements FilterFunction {
    public static final Splitter SPACE_SPLITTER = Splitter.on(' ');
    public static final String FILTERED_APPENDIX = " # Filtered";

    List<String> passwords = null;
    String cookie = null;
    CrowdClient crowdClient = null;

    public FilterAuthFunction(List<String> passwords, String cookie, CrowdClient crowdClient) {
        this.cookie = cookie;
        this.crowdClient = crowdClient;
        this.passwords = passwords;
    }

    public FilterAuthFunction() {
    }

    @Override
    public RpslObject apply(RpslObject rpslObject) {
        if (!rpslObject.containsAttribute(AttributeType.AUTH)) {    // until IRT is phased out then we still have to mask their auth: hashes
            return rpslObject;
        }

        Map<RpslAttribute, RpslAttribute> replace = Maps.newHashMap();
        List<RpslAttribute> authAttributes = rpslObject.findAttributes(AttributeType.AUTH);
        boolean authenticated = passwordAuthentication(passwords, authAttributes) || ssoAuthentication(cookie, authAttributes);

        for (RpslAttribute authAttribute : authAttributes) {
            Iterator<String> authIterator = SPACE_SPLITTER.split(authAttribute.getCleanValue()).iterator();
            String passwordType = authIterator.next().toUpperCase();

            if (authenticated) {
                if (passwordType.equals("SSO")) {
                    final String username = crowdClient.getUsername(authIterator.next());
                    if (username != null) {
                        replace.put(authAttribute, new RpslAttribute(authAttribute.getKey(), "SSO " + username));
                    } else {
                        // TODO[as] - yes, what if that uuid is no longer in crowd?
                    }
                }
            } else {
                if (passwordType.endsWith("-PW") || passwordType.equals("SSO")) {     // history table has CRYPT-PW, dummify that too!
                    replace.put(authAttribute, new RpslAttribute(authAttribute.getKey(), passwordType + FILTERED_APPENDIX));
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

    private boolean ssoAuthentication(String cookie, List<RpslAttribute> authAttributes) {
        if (StringUtils.isBlank(cookie)) {
            return false;
        }

        // TODO: implement

        return false;
    }

    private boolean passwordAuthentication(List<String> passwords, List<RpslAttribute> authAttributes) {
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
