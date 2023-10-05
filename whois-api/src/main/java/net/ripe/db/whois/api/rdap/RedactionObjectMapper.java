package net.ripe.db.whois.api.rdap;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rdap.domain.Redaction;
import net.ripe.db.whois.api.rdap.domain.Role;
import net.ripe.db.whois.common.domain.CIString;
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

    private static final List<AttributeType> unsupportedRegistrantAttributes = Lists.newArrayList(MBRS_BY_REF,
            MNT_DOMAINS, MNT_LOWER, MNT_REF, MNT_ROUTES);

    private static final List<AttributeType> unsupportedPersonalAttributes = Lists.newArrayList(NOTIFY);

    public static String REDACTED_ENTITIES_SYNTAX = "$.entities[?(@.handle=='%s')]";

    public static String REDACTED_VCARD_SYNTAX = "$.entities[?(@.roles=='%s')].vcardArray[1][?(@[0]=='%s')]";

    public static Set<Redaction> createEntityRedaction(final List<RpslAttribute> rpslAttributes){
        return rpslAttributes.stream()
                .filter( rpslAttribute -> unsupportedRegistrantAttributes.contains(rpslAttribute.getType()))
                .flatMap( rpslAttribute -> {
                    final List<Redaction> redactions = Lists.newArrayList();
                    for (final CIString values : rpslAttribute.getCleanValues()) {
                        redactions.add(createRedactionByAttributeType(rpslAttribute.getType(), String.format(REDACTED_ENTITIES_SYNTAX, values)));
                    }
                    return redactions.stream();
                }).collect(Collectors.toSet());
    }


    public static Set<Redaction> createContactEntityRedaction(final RpslObject rpslObject, final List<Role> roles) {
        final String joinedRoles = roles.stream().sorted().map(Role::getValue).collect(Collectors.joining(" && "));
        return unsupportedPersonalAttributes.stream().filter(rpslObject::containsAttribute).
                map(unsupportedVcard -> createRedactionByAttributeType(unsupportedVcard, String.format(REDACTED_VCARD_SYNTAX, joinedRoles, unsupportedVcard))).collect(Collectors.toSet());
    }

    private static Redaction createRedactionByAttributeType(final AttributeType attributeType, final String unsupportedCardSyntax){
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
