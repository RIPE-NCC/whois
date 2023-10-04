package net.ripe.db.whois.api.rdap;

import com.google.common.collect.Maps;
import net.ripe.db.whois.api.rdap.domain.Redaction;
import net.ripe.db.whois.api.rdap.domain.Role;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.apache.commons.compress.utils.Lists;

import org.elasticsearch.client.Node;
import org.glassfish.jersey.internal.guava.Sets;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static net.ripe.db.whois.api.rdap.RdapObjectMapper.CONTACT_ATTRIBUTE_TO_ROLE_NAME;
import static net.ripe.db.whois.common.rpsl.AttributeType.NOTIFY;
import static net.ripe.db.whois.common.rpsl.AttributeType.MBRS_BY_REF;
import static net.ripe.db.whois.common.rpsl.AttributeType.MNT_DOMAINS;
import static net.ripe.db.whois.common.rpsl.AttributeType.MNT_LOWER;
import static net.ripe.db.whois.common.rpsl.AttributeType.MNT_REF;
import static net.ripe.db.whois.common.rpsl.AttributeType.MNT_ROUTES;

@Component
public class RedactionObjectMapper {

    private static final Map<AttributeType, AttributeTypeRedaction> UNSUPPORTED_REGISTRANT_ATTRIBUTES = Maps.newHashMap();
    static {
        UNSUPPORTED_REGISTRANT_ATTRIBUTES.put(MBRS_BY_REF, AttributeTypeRedaction.MBRS_BY_REF);
        UNSUPPORTED_REGISTRANT_ATTRIBUTES.put(MNT_DOMAINS, AttributeTypeRedaction.MNT_DOMAINS);
        UNSUPPORTED_REGISTRANT_ATTRIBUTES.put(MNT_LOWER, AttributeTypeRedaction.MNT_LOWER);
        UNSUPPORTED_REGISTRANT_ATTRIBUTES.put(MNT_REF, AttributeTypeRedaction.MNT_REF);
        UNSUPPORTED_REGISTRANT_ATTRIBUTES.put(MNT_ROUTES, AttributeTypeRedaction.MNT_ROUTES);
    }

    private static final Map<AttributeType, AttributeTypeRedaction> UNSUPPORTED_PERSONAL_ATTRIBUTES = Maps.newHashMap();
    {
        //UNSUPPORTED_VCARDS.put(E_MAIL, new Pair("e-mail contact information", "Personal data"));
        UNSUPPORTED_PERSONAL_ATTRIBUTES.put(NOTIFY, AttributeTypeRedaction.NOTIFY);
    }

    public static String UNSUPPORTED_ENTITIES_SYNTAX = "$.entities[?(@.handle=='%s')]";

    public static String UNSUPPORTED_VCARD_SYNTAX = "$.entities[?(@.roles=='%s')].vcardArray[1][?(@[0]=='%s')]";

    public static Set<Redaction> createEntityRedaction(final List<RpslAttribute> rpslAttributes){
        final Set<Redaction> redactions = Sets.newHashSet();
        for (final RpslAttribute rpslAttribute: rpslAttributes) {
            final AttributeType rpslAttributeType = AttributeType.getByName(rpslAttribute.getKey());
            if (UNSUPPORTED_REGISTRANT_ATTRIBUTES.containsKey(rpslAttributeType)){
                redactions.add(createRedaction(UNSUPPORTED_REGISTRANT_ATTRIBUTES.get(rpslAttributeType),
                        String.format(UNSUPPORTED_ENTITIES_SYNTAX, String.join(",", rpslAttribute.getCleanValues()))));
            }
        }
        return redactions;
    }

   /* public void createContactEntityRedaction(final CIString attributeValue,
                                             final Set<ObjectType> objectPossibleTypes,
                                             final List<Role> roles,
                                             final List<Redaction> redactions) {
        final RpslObject referencedRpslObject = getObject(objectPossibleTypes, attributeValue);

        if (referencedRpslObject != null){
            final String joinedRoles = roles.stream().map(Role::getValue).collect(Collectors.joining(" && "));

            for (final Map.Entry<AttributeType, AttributeTypeRedaction> unsupportedVcard : UNSUPPORTED_PERSONAL_ATTRIBUTES.entrySet()) {
                if (referencedRpslObject.containsAttribute(unsupportedVcard.getKey())) {
                    createRedaction(unsupportedVcard.getValue(), String.format(UNSUPPORTED_VCARD_SYNTAX, joinedRoles, unsupportedVcard.getKey()), redactions);
                }
            }
        }
    }*/

    public static Set<Redaction> createContactEntityRedaction(final RpslObject referencedRpslObject, final List<Role> roles) {
        final Set<Redaction> redactions = Sets.newHashSet();

            final String joinedRoles = roles.stream().map(Role::getValue).collect(Collectors.joining(" && "));

            for (final Map.Entry<AttributeType, AttributeTypeRedaction> unsupportedVcard : UNSUPPORTED_PERSONAL_ATTRIBUTES.entrySet()) {
                if (referencedRpslObject.containsAttribute(unsupportedVcard.getKey())) {
                    redactions.add(createRedaction(unsupportedVcard.getValue(), String.format(UNSUPPORTED_VCARD_SYNTAX, joinedRoles, unsupportedVcard.getKey())));
                }
            }
            return  redactions;
    }

  /*  private static void createRedaction(final AttributeTypeRedaction unsupportedVcard, final String unsupportedCardSyntax,
                                 final List<Redaction> redactions) {
        final Redaction redaction = new Redaction(new Redaction.Description(unsupportedVcard.getName()),
                unsupportedCardSyntax,
                new Redaction.Description(unsupportedVcard.getReason()));
        if (!redactions.contains(redaction)){
            redactions.add(redaction);
        }
    }
*/
    private static Redaction createRedaction(final AttributeTypeRedaction unsupportedVcard, final String unsupportedCardSyntax) {
        return new Redaction(new Redaction.Description(unsupportedVcard.getName()),
                unsupportedCardSyntax,
                new Redaction.Description(unsupportedVcard.getReason()));
    }

}
