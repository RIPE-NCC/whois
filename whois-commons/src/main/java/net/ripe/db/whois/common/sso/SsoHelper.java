package net.ripe.db.whois.common.sso;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;

import java.util.Iterator;
import java.util.Map;

public class SsoHelper {
    private static final Splitter SPACE_SPLITTER = Splitter.on(' ');

    public static RpslObject translateAuth(final RpslObject rpslObject, final AuthTranslator authTranslator) {
        if (!rpslObject.containsAttribute(AttributeType.AUTH)) {    // until IRT is phased out then we still have to mask their auth: hashes
            return rpslObject;
        }

        final Map<RpslAttribute, RpslAttribute> replace = Maps.newHashMap();
        for (RpslAttribute authAttribute : rpslObject.findAttributes(AttributeType.AUTH)) {
            final Iterator<String> authIterator = SPACE_SPLITTER.split(authAttribute.getCleanValue()).iterator();
            final String authType = authIterator.next().toUpperCase();
            if (authIterator.hasNext()) {
                final String authToken = authIterator.next();
                final RpslAttribute result = authTranslator.translate(authType, authToken, authAttribute);
                if (result != null) {
                    replace.put(authAttribute, result);
                }
            }
        }

        if (replace.isEmpty()) {
            return rpslObject;
        } else {
            return new RpslObjectBuilder(rpslObject).replaceAttributes(replace).get();
        }
    }
}
