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

@ThreadSafe
public class FilterAuthFunction implements FilterFunction {
    public static final Splitter SPACE_SPLITTER = Splitter.on(' ');
    public static final String FILTERED_APPENDIX = " # Filtered";

    private final List<String> passwords;
    private final String cookie;

    public FilterAuthFunction(List<String> passwords) {
        this.passwords = passwords;
        this.cookie = null;
    }

    public FilterAuthFunction(String cookie) {
        this.cookie = cookie;
        this.passwords = Collections.emptyList();
    }

    public FilterAuthFunction() {
        passwords = Collections.emptyList();
        cookie = null;
    }

    @Override
    public RpslObject apply(RpslObject rpslObject) {
        if (!rpslObject.containsAttribute(AttributeType.AUTH)) {
            return rpslObject;
        }

        Map<RpslAttribute, RpslAttribute> replace = Maps.newHashMap();
        for (RpslAttribute auth : rpslObject.findAttributes(AttributeType.AUTH)) {
            CIString authValue = auth.getCleanValue();
            Iterator<String> authIterator = SPACE_SPLITTER.split(authValue.toUpperCase()).iterator();
            String passwordType = authIterator.next();
            if (passwordType.endsWith("-PW")) {     // history table has CRYPT-PW, has to be able to dummify that too!
                if (passwords.isEmpty() || !passwordType.startsWith("MD5") || !PasswordHelper.authenticateMd5Passwords(authValue.toString(), passwords)) {
                    replace.put(auth, new RpslAttribute(auth.getKey(), passwordType + FILTERED_APPENDIX));
                }
            } else if (passwordType.equals("SSO")) {
                String replacement = FILTERED_APPENDIX;
                if (cookie != null) {
                    final String username = checkCookieAgainstUuidAndReturnUsername(cookie, authIterator.next());
                    if (username != null) {
                        replacement = username;
                    }
                }

                replace.put(auth, new RpslAttribute(auth.getKey(), passwordType + replacement));
            }
        }

        if (replace.isEmpty()) {
            return rpslObject;
        } else {
            RpslObjectFilter.addFilteredSourceReplacement(rpslObject, replace);
            return new RpslObjectBuilder(rpslObject).replaceAttributes(replace).get();
        }
    }

    // TODO: implement/refactor
    private String checkCookieAgainstUuidAndReturnUsername(String cookie, String uuid) {
        return "agoston@ripe.net";
    }
}
