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

    private static final Map<AttributeType, AttributeTypeRedaction> UNSUPPORTED_REGISTRANT_ATTRIBUTES = Map.of(
            MBRS_BY_REF, AttributeTypeRedaction.MBRS_BY_REF,
            MNT_DOMAINS, AttributeTypeRedaction.MNT_DOMAINS,
            MNT_LOWER, AttributeTypeRedaction.MNT_LOWER,
            MNT_REF, AttributeTypeRedaction.MNT_REF,
            MNT_ROUTES, AttributeTypeRedaction.MNT_ROUTES);

    private static final Map<AttributeType, AttributeTypeRedaction> UNSUPPORTED_PERSONAL_ATTRIBUTES = Map.of(NOTIFY, AttributeTypeRedaction.NOTIFY);

    public static String UNSUPPORTED_ENTITIES_SYNTAX = "$.entities[?(@.handle=='%s')]";

    public static String UNSUPPORTED_VCARD_SYNTAX = "$.entities[?(@.roles=='%s')].vcardArray[1][?(@[0]=='%s')]";

    public static Set<Redaction> createEntityRedaction(final List<RpslAttribute> rpslAttributes){
        return rpslAttributes.stream()
                .filter( rpslAttribute -> UNSUPPORTED_REGISTRANT_ATTRIBUTES.containsKey(rpslAttribute.getType()))
                .map( rpslAttribute -> createRedaction(
                        UNSUPPORTED_REGISTRANT_ATTRIBUTES.get(rpslAttribute.getType()),
                        String.format(UNSUPPORTED_ENTITIES_SYNTAX, String.join(",", rpslAttribute.getCleanValues()))))
                .collect(Collectors.toSet());
    }


    public static Set<Redaction> createContactEntityRedaction(final RpslObject referencedRpslObject, final List<Role> roles) {
        final String joinedRoles = roles.stream().map(Role::getValue).collect(Collectors.joining(" && "));
        return  UNSUPPORTED_PERSONAL_ATTRIBUTES.entrySet().stream()
                .filter(unsupportedVcard -> referencedRpslObject.containsAttribute(unsupportedVcard.getKey()))
                .map(unsupportedVcard -> createRedaction(unsupportedVcard.getValue(), String.format(UNSUPPORTED_VCARD_SYNTAX, joinedRoles, unsupportedVcard.getKey())))
                .collect(Collectors.toSet());
    }

    private static Redaction createRedaction(final AttributeTypeRedaction unsupportedVcard, final String unsupportedCardSyntax) {
        return new Redaction(unsupportedVcard.getName(), unsupportedCardSyntax, unsupportedVcard.getReason());
    }

}
