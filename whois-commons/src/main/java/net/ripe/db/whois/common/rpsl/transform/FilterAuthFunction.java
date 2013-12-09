package net.ripe.db.whois.common.rpsl.transform;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.*;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
        if (!rpslObject.containsAttribute(AttributeType.AUTH)) {
            return rpslObject;
        }

        Map<RpslAttribute, RpslAttribute> replace = Maps.newHashMap();
        for (RpslAttribute auth : rpslObject.findAttributes(AttributeType.AUTH)) {
            CIString authValue = auth.getCleanValue();
            String passwordType = SPACE_SPLITTER.split(authValue.toUpperCase()).iterator().next();
            if (passwordType.endsWith("-PW")) {     // history table has CRYPT-PW, has to be able to dummify that too!
                if (passwords.isEmpty() || !passwordType.startsWith("MD5") || !PasswordHelper.authenticateMd5Passwords(authValue.toString(), passwords)) {
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
}
