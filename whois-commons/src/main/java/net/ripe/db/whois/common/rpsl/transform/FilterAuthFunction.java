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
        if (!ObjectTemplate.getTemplate(rpslObject.getType()).hasAttribute(AttributeType.AUTH)) {
            return rpslObject;
        }

        Map<RpslAttribute, RpslAttribute> replace = Maps.newHashMap();
        for (RpslAttribute auth : rpslObject.findAttributes(AttributeType.AUTH)) {
            String passwordType = SPACE_SPLITTER.split(auth.getCleanValue().toUpperCase()).iterator().next();
            if (passwordType.endsWith("-PW")) {
                replace.put(auth, new RpslAttribute(auth.getKey(), passwordType + FILTERED_APPENDIX));
            }
        }

        RpslObject filtered = new RpslObjectFilter(rpslObject).replaceAttributes(replace);
        return filtered == rpslObject ? rpslObject : RpslObjectFilter.setFiltered(filtered);
    }
}
