package net.ripe.db.whois.common;

import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import org.apache.commons.lang3.StringEscapeUtils;

import java.net.IDN;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Utf8Conversion {

    private Utf8Conversion() {
        // do not instantiate
    }

    private static final Set<String> DOMAIN_ATTRIBUTES = Set.of(
            "domain",
            "nserver"
    );

    public static RpslObject convert(final String input) {
        final RpslObject rpslObject = RpslObject.parse(input);
        final List<RpslAttribute> attrsToConvert = rpslObject.getAttributes();

        final Map<RpslAttribute, RpslAttribute> convertedMap = attrsToConvert.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        attr -> {
                            //TODO: consider using org.apache.commons.text.StringEscapeUtils.unescapeUnicode
                            String value = StringEscapeUtils.unescapeJava(attr.getValue());
                            if (isDomainAttribute(attr.getType())) {
                                value = IDN.toASCII(value);
                            }
                            return new RpslAttribute(attr.getType(), value);
                        }
                ));

        return new RpslObjectBuilder(rpslObject)
                .replaceAttributes(convertedMap)
                .get();
    }

    private static boolean isDomainAttribute(final AttributeType type) {
        if (type == null){
            return false;
        }
        return DOMAIN_ATTRIBUTES.contains(type.toString().toLowerCase());
    }
}
