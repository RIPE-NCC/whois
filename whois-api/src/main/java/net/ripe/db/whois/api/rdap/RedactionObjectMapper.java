package net.ripe.db.whois.api.rdap;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rdap.domain.Redaction;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RedactionObjectMapper {

    public static String REDACTED_ENTITIES_SYNTAX = "$.entities[?(@.handle=='%s')]";

    public static String REDACTED_PARENT_SYNTAX = "$.%s[?(@.handle=='%s')].entities[?(@.handle=='%s')]";
    public static List<AttributeType> REDACTED_PERSONAL_ATTR = Lists.newArrayList(AttributeType.NOTIFY);

    public static Set<Redaction> createEntityRedactions(final RpslObject rpslObject){
        return createPersonalRedaction(rpslObject.findAttributes(REDACTED_PERSONAL_ATTR), "$");
    }

    public static Set<Redaction> createContactEntityRedaction(final RpslObject rpslObject) {
        return createPersonalRedaction(rpslObject.findAttributes(REDACTED_PERSONAL_ATTR), String.format(REDACTED_ENTITIES_SYNTAX, rpslObject.getKey()));
    }

    public static Set<Redaction> createParentEntityRedaction(final String attributeName, final String handle, final RpslObject rpslObject) {
        return createPersonalRedaction(rpslObject.findAttributes(REDACTED_PERSONAL_ATTR), String.format(REDACTED_PARENT_SYNTAX, attributeName, handle, rpslObject.getKey()));
    }
    private static Set<Redaction> createPersonalRedaction(final List<RpslAttribute> attributeTypes, final String prefix){
       return attributeTypes.stream()
               .map( rpslAttribute -> new Redaction("Updates notification e-mail information",
                                                String.format("%s.vcardArray[1][?(@[0]=='%s')]", prefix, rpslAttribute.getType().getName()),
                                                "Personal data")
                )
               .collect(Collectors.toSet());
    }
}
