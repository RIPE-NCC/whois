package net.ripe.db.whois.common.rpsl.transform;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.PasswordHelper;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.common.rpsl.RpslObjectFilter;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Collections;
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

    private final List<String> passwords;

    public FilterAuthFunction(List<String> passwords) {
        this.passwords = passwords;
    }

    public FilterAuthFunction() {
        passwords = Collections.emptyList();
    }

    @Override
    public RpslObject apply(RpslObject rpslObject) {
        final List<RpslAttribute> authAttributes = rpslObject.findAttributes(AttributeType.AUTH);
        if (authAttributes.isEmpty()) {
            return rpslObject;
        }

        final Map<RpslAttribute, RpslAttribute> replace = Maps.newHashMap();
        final boolean authenticated = authenticate(authAttributes);

        for (RpslAttribute auth : authAttributes) {

            CIString authValue = auth.getCleanValue();
            Iterator<String> authIterator = SPACE_SPLITTER.split(authValue).iterator();
            String passwordType = authIterator.next().toUpperCase();

            if (passwordType.endsWith("-PW")) {     // history table has CRYPT-PW, has to be able to dummify that too!
                if (passwords.isEmpty() || !passwordType.startsWith("MD5") || !authenticated) {
                    replace.put(auth, new RpslAttribute(auth.getKey(), passwordType + FILTERED_APPENDIX));
                }
            } else if (passwordType.equals("SSO")) {
                replace.put(auth, new RpslAttribute(auth.getKey(), passwordType + FILTERED_APPENDIX));
            }
        }

        if (replace.isEmpty()) {
            return rpslObject;
        } else {
            RpslObjectFilter.addFilteredSourceReplacement(rpslObject, replace);
            return new RpslObjectBuilder(rpslObject).replaceAttributes(replace).get();
        }
    }

    private boolean authenticate(final List<RpslAttribute> attributes) {
        for (final String password : passwords) {
            for (final RpslAttribute attribute : attributes) {
                if (PasswordHelper.authenticateMd5Passwords(attribute.getCleanValue().toString(), password)) {
                    return true;
                }
            }
        }

        return false;
    }
}
