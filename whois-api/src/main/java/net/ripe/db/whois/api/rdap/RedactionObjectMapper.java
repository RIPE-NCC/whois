package net.ripe.db.whois.api.rdap;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.api.rdap.domain.Redaction;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.apache.commons.compress.utils.Lists;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static net.ripe.db.whois.api.rdap.RdapObjectMapper.CONTACT_ATTRIBUTE_TO_ROLE_NAME;
import static net.ripe.db.whois.common.rpsl.AttributeType.E_MAIL;
import static net.ripe.db.whois.common.rpsl.AttributeType.NOTIFY;
import static net.ripe.db.whois.common.rpsl.AttributeType.MBRS_BY_REF;
import static net.ripe.db.whois.common.rpsl.AttributeType.MNT_DOMAINS;
import static net.ripe.db.whois.common.rpsl.AttributeType.MNT_LOWER;
import static net.ripe.db.whois.common.rpsl.AttributeType.MNT_REF;
import static net.ripe.db.whois.common.rpsl.AttributeType.MNT_ROUTES;

@Component
public class RedactionObjectMapper {

    private final RpslObjectDao rpslObjectDao;
    private static final Map<AttributeType, Pair> UNSUPPORTED_ENTITIES = Maps.newHashMap();
    static {
        UNSUPPORTED_ENTITIES.put(MBRS_BY_REF, new Pair("Authenticate members by reference", "No registrant mntner"));
        UNSUPPORTED_ENTITIES.put(MNT_DOMAINS, new Pair("Authenticate domain objects", "No registrant mntner"));
        UNSUPPORTED_ENTITIES.put(MNT_LOWER, new Pair("Authenticate more specific resources", "No registrant mntner"));
        UNSUPPORTED_ENTITIES.put(MNT_REF, new Pair("Authenticate incoming references", "No registrant mntner"));
        UNSUPPORTED_ENTITIES.put(MNT_ROUTES, new Pair("Authenticate route objects", "No registrant mntner"));
    }

    private static final Map<AttributeType, Pair> UNSUPPORTED_VCARDS = Maps.newHashMap();
    {
        UNSUPPORTED_VCARDS.put(E_MAIL, new Pair("e-mail contact information", "Personal data"));
        UNSUPPORTED_VCARDS.put(NOTIFY, new Pair("Updates notification e-mail information", "Personal data"));
    }

    public static String UNSUPPORTED_ENTITIES_SYNTAX = "$.entities[?(@.handle=='%s')]";

    public static String UNSUPPORTED_VCARD_SYNTAX = "$.entities[?(@.roles=='%s')].vcardArray[1][?(@[0]=='%s')]";

    RedactionObjectMapper(final RpslObjectDao rpslObjectDao){
        this.rpslObjectDao = rpslObjectDao;
    }

    public List<Redaction> createEntitiesRedaction(final List<RpslAttribute> rpslAttributes){
        final List<Redaction> redactions = Lists.newArrayList();
        final Map<CIString, Set<AttributeType>> contacts = Maps.newTreeMap();
        for (final RpslAttribute rpslAttribute: rpslAttributes) {
            final AttributeType rpslAttributeType = AttributeType.getByName(rpslAttribute.getKey());
            if (UNSUPPORTED_ENTITIES.containsKey(rpslAttributeType)){
                createRedaction(UNSUPPORTED_ENTITIES.get(rpslAttributeType), String.format(UNSUPPORTED_ENTITIES_SYNTAX, rpslAttributeType), redactions);
            } else {
                // TODO: [MH] Part of this logic can be merge with the logic of contactEntities in RdapObjectMapper. Big refactor to make a clear merge
                for (final AttributeType attributeType : CONTACT_ATTRIBUTE_TO_ROLE_NAME.keySet()) {
                    if (attributeType.equals(rpslAttributeType)){
                        rpslAttribute.getCleanValues().forEach( contactName -> {
                            if (contacts.containsKey(contactName)) {
                                contacts.get(contactName).add(attributeType);
                            } else {
                                contacts.put(contactName, Sets.newHashSet(attributeType));
                            }
                        });
                    }
                }
            }
        }
        if (!contacts.isEmpty()) {
            redactions.addAll(checkContactEntityLevelCreation(contacts));
        }
        return redactions;
    }

    private List<Redaction> checkContactEntityLevelCreation(final Map<CIString, Set<AttributeType>> contacts) {
        final List<Redaction> redactions = Lists.newArrayList();
        for (final Map.Entry<CIString, Set<AttributeType>> entry : contacts.entrySet()) {
            final Set<ObjectType> references = entry.getValue().stream().flatMap(attributeType -> attributeType.getReferences().stream()).collect(Collectors.toSet());
            final RpslObject referencedRpslObject = getObject(references, entry.getKey());
            if (referencedRpslObject == null){
                return Lists.newArrayList();
            }
            final String roles = entry.getValue().stream().map(attribute -> {
                if (CONTACT_ATTRIBUTE_TO_ROLE_NAME.containsKey(attribute)) {
                    return CONTACT_ATTRIBUTE_TO_ROLE_NAME.get(attribute).getValue();
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.joining(" && "));

            for (final Map.Entry<AttributeType, Pair> unsupportedVcard : UNSUPPORTED_VCARDS.entrySet()) {
                if (referencedRpslObject.containsAttribute(unsupportedVcard.getKey())) {
                    createRedaction(unsupportedVcard.getValue(), String.format(UNSUPPORTED_VCARD_SYNTAX, roles, unsupportedVcard.getKey()), redactions);
                }
            }
        }
        return redactions;
    }

    private void createRedaction(final Pair unsupportedVcard, final String unsupportedCardSyntax,
                                 final List<Redaction> redactions) {
        final Redaction redaction = new Redaction(new Redaction.Description(unsupportedVcard.getName()),
                unsupportedCardSyntax,
                new Redaction.Description(unsupportedVcard.reason));
        if (!redactions.contains(redaction)){
            redactions.add(redaction);
        }
    }

    private RpslObject getObject(final Set<ObjectType> possibleObjectTypes, final CIString lookupKey){
        for (final ObjectType objectType : possibleObjectTypes) {
            final RpslObject rpslObject = rpslObjectDao.getByKeyOrNull(objectType, lookupKey);
            if (rpslObject != null){
                return rpslObject;
            }
        }
        return null;
    }

    private static class Pair {

        private final String name;

        private final String reason;

        Pair(final String name, final String reason){
            this.name = name;
            this.reason = reason;
        }

        public String getName() {
            return name;
        }

        public String getReason() {
            return reason;
        }
    }

}
