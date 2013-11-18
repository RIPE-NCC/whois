package net.ripe.db.whois.common.rpsl.transform;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import net.ripe.db.whois.common.rpsl.*;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Map;

@ThreadSafe
public class FilterAuthFunction implements FilterFunction {
    public static final Splitter SPACE_SPLITTER = Splitter.on(' ');
    public static final String FILTERED_APPENDIX = " # Filtered";

    @Override
    public RpslObject apply(RpslObject rpslObject) {
//        if (!ObjectTemplate.getTemplate(rpslObject.getType()).hasAttribute(AttributeType.AUTH)) {
        if (!rpslObject.containsAttribute(AttributeType.AUTH)) {
            return rpslObject;
        }

        Map<RpslAttribute, RpslAttribute> replace = Maps.newHashMap();
        for (RpslAttribute auth : rpslObject.findAttributes(AttributeType.AUTH)) {
            String passwordType = SPACE_SPLITTER.split(auth.getCleanValue().toUpperCase()).iterator().next();
            if (passwordType.endsWith("-PW")) {     // history table has CRYPT-PW, has to be able to dummify that too!
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
