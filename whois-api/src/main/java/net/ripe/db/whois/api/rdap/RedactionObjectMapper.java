package net.ripe.db.whois.api.rdap;

import com.google.common.collect.Sets;
import net.ripe.db.whois.api.rdap.domain.Redaction;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;

import java.util.Set;

public class RedactionObjectMapper {

    public static String REDACTED_ENTITIES_SYNTAX = "$.entities[?(@.handle=='%s')]";

    public static Set<Redaction> createEntityRedaction(final RpslObject rpslObject){
        final Set<Redaction> redactions = Sets.newHashSet();

        rpslObject.getAttributes().forEach( rpslAttribute -> {
            addRedactionForVcard(redactions, rpslAttribute.getType(), "$");

            rpslAttribute.getCleanValues().forEach( value ->
                    addRedactionForRegistrant(redactions, rpslAttribute.getType(), String.format(REDACTED_ENTITIES_SYNTAX, value))
            );
        });

        return redactions;
    }

    public static Set<Redaction> createContactEntityRedaction(final RpslObject rpslObject) {
        final Set<Redaction> redactions = Sets.newHashSet();
        rpslObject.getAttributes().forEach( rpslAttribute ->
            addRedactionForVcard(redactions, rpslAttribute.getType(),  String.format(REDACTED_ENTITIES_SYNTAX, rpslObject.getKey()))
        );
        return  redactions;
    }

    private static void addRedactionForRegistrant(final Set<Redaction> redactions, final AttributeType attributeType, final String prefix){

         switch (attributeType) {
            case MBRS_BY_REF -> redactions.add(new Redaction("Authenticate members by reference", prefix, "No registrant mntner"));
            case MNT_DOMAINS -> redactions.add(new Redaction("Authenticate domain objects", prefix, "No registrant mntner"));
            case MNT_LOWER -> redactions.add(new Redaction("Authenticate more specific resources", prefix, "No registrant mntner"));
            case MNT_REF -> redactions.add(new Redaction("Authenticate incoming references", prefix, "No registrant mntner"));
            case MNT_ROUTES -> redactions.add(new Redaction("Authenticate route objects", prefix, "No registrant mntner"));
         };
    }

    private static void addRedactionForVcard(final Set<Redaction> redactions, final AttributeType attributeType, final String prefix){
         switch (attributeType) {
            case NOTIFY -> redactions.add(new Redaction("Updates notification e-mail information",
                                                    String.format("%s.vcardArray[1][?(@[0]=='%s')]", prefix, attributeType.getName()),
                                                    "Personal data")
                                          );
        };
    }
}
