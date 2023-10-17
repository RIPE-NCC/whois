package net.ripe.db.whois.api.rdap;

import com.google.common.collect.Sets;
import net.ripe.db.whois.api.rdap.domain.Redaction;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;

import java.util.Set;

public class RedactionObjectMapper {

    public static String REDACTED_ENTITIES_SYNTAX = "$.entities[?(@.handle=='%s')]";

    public static Set<Redaction> createMainEntityRedactions(final RpslObject rpslObject){
        final Set<Redaction> redactions = Sets.newHashSet();

        rpslObject.getAttributes().forEach( rpslAttribute -> {
            addRedactionForVcard(redactions, rpslAttribute.getType(), "$");
        });

        return redactions;
    }

    public static Set<Redaction> createContactEntityRedaction(final RpslObject rpslObject) {
        final Set<Redaction> redactions = Sets.newHashSet();
        rpslObject.getAttributes().forEach( rpslAttribute ->
            addRedactionForVcard(redactions, rpslAttribute.getType(), String.format(REDACTED_ENTITIES_SYNTAX, rpslObject.getKey()))
        );
        return  redactions;
    }

    private static void addRedactionForVcard(final Set<Redaction> redactions, final AttributeType attributeType, final String prefix){
        if (attributeType == AttributeType.NOTIFY) {
            redactions.add(new Redaction("Updates notification e-mail information",
                    String.format("%s.vcardArray[1][?(@[0]=='%s')]", prefix, attributeType.getName()),
                    "Personal data")
            );
        }
    }
}
