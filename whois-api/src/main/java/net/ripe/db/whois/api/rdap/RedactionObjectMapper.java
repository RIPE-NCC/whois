package net.ripe.db.whois.api.rdap;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.api.rdap.domain.Redaction;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import static net.ripe.db.whois.common.rpsl.AttributeType.NOTIFY;
import static net.ripe.db.whois.common.rpsl.AttributeType.MBRS_BY_REF;
import static net.ripe.db.whois.common.rpsl.AttributeType.MNT_DOMAINS;
import static net.ripe.db.whois.common.rpsl.AttributeType.MNT_LOWER;
import static net.ripe.db.whois.common.rpsl.AttributeType.MNT_REF;
import static net.ripe.db.whois.common.rpsl.AttributeType.MNT_ROUTES;

public class RedactionObjectMapper {

    private static final List<AttributeType> UNSUPPORTED_REGISTRANT_ATTRIBUTES = Lists.newArrayList(MBRS_BY_REF,
            MNT_DOMAINS, MNT_LOWER, MNT_REF, MNT_ROUTES);

    private static final List<AttributeType> UNSUPPORTED_PERSONAL_ATTRIBUTES = Lists.newArrayList(NOTIFY);

    public static String REDACTED_ENTITIES_SYNTAX = "$.entities[?(@.handle=='%s')]";
    public static String REDACTED_VCARD_SYNTAX = "$.vcardArray[1][?(@[0]=='%s')]";
    public static String REDACTED_ENTITIES_VCARD_SYNTAX = "$.entities[?(@.handle=='%s')].vcardArray[1][?(@[0]=='%s')]";

    public static Set<Redaction> createEntityRedaction(final RpslObject rpslObject){
        final Set<Redaction> redactions = Sets.newHashSet();
        for (final RpslAttribute rpslAttribute : rpslObject.getAttributes()) {
            if (UNSUPPORTED_PERSONAL_ATTRIBUTES.contains(rpslAttribute.getType())){
                redactions.add(createRedaction(rpslAttribute.getType(), String.format(REDACTED_VCARD_SYNTAX, rpslAttribute.getType())));
            }
            if (UNSUPPORTED_REGISTRANT_ATTRIBUTES.contains(rpslAttribute.getType())){
                redactions.addAll(rpslAttribute.getCleanValues().stream()
                        .map(value -> createRedaction(rpslAttribute.getType(), String.format(REDACTED_ENTITIES_SYNTAX, value)))
                        .collect(Collectors.toSet()));
            }
        }
        return redactions;
    }

    public static Set<Redaction> createContactEntityRedaction(final RpslObject rpslObject) {
        return UNSUPPORTED_PERSONAL_ATTRIBUTES.stream()
                .filter(rpslObject::containsAttribute)
                .map(unsupportedVcard -> createRedaction(unsupportedVcard, String.format(REDACTED_ENTITIES_VCARD_SYNTAX, rpslObject.getKey(), unsupportedVcard)))
                .collect(Collectors.toSet());
    }

    private static Redaction createRedaction(final AttributeType attributeType, final String unsupportedCardSyntax){
        return switch (attributeType) {
            case MBRS_BY_REF -> new Redaction("Authenticate members by reference", unsupportedCardSyntax, "No registrant mntner");
            case MNT_DOMAINS -> new Redaction("Authenticate domain objects", unsupportedCardSyntax, "No registrant mntner");
            case MNT_LOWER -> new Redaction("Authenticate more specific resources", unsupportedCardSyntax, "No registrant mntner");
            case MNT_REF -> new Redaction("Authenticate incoming references", unsupportedCardSyntax, "No registrant mntner");
            case MNT_ROUTES -> new Redaction("Authenticate route objects", unsupportedCardSyntax, "No registrant mntner");
            case NOTIFY -> new Redaction("Updates notification e-mail information", unsupportedCardSyntax, "Personal data");
            default -> throw new IllegalArgumentException("Unhandled object type: " + attributeType);
        };
    }
}
