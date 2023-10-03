package net.ripe.db.whois.api.rdap;

import com.google.common.collect.Maps;
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
    private static final Map<AttributeType, Redaction> UNSUPPORTED_ENTITIES = Maps.newHashMap();
    static {
        UNSUPPORTED_ENTITIES.put(MBRS_BY_REF, new Redaction(new Redaction.Description("Indirect population of a set"), new Redaction.Description("No registrant mntner")));
        UNSUPPORTED_ENTITIES.put(MNT_DOMAINS, new Redaction(new Redaction.Description("Domain objects protection"), new Redaction.Description("No registrant mntner")));
        UNSUPPORTED_ENTITIES.put(MNT_LOWER, new Redaction(new Redaction.Description("Low level objects protection"), new Redaction.Description("No registrant mntner")));
        UNSUPPORTED_ENTITIES.put(MNT_REF, new Redaction(new Redaction.Description("Incoming references protection"), new Redaction.Description("No registrant mntner")));
        UNSUPPORTED_ENTITIES.put(MNT_ROUTES, new Redaction(new Redaction.Description("Route object creation protection"), new Redaction.Description("No registrant mntner")));
    }

    private static final Map<AttributeType, Redaction> UNSUPPORTED_VCARDS = Maps.newHashMap();
    {
        UNSUPPORTED_VCARDS.put(E_MAIL, new Redaction(new Redaction.Description("e-mail contact information"),new Redaction.Description("Personal data")));
        UNSUPPORTED_VCARDS.put(NOTIFY, new Redaction(new Redaction.Description("Updates notification e-mail information"), new Redaction.Description("Personal data")));
    }

    public static String UNSUPPORTED_ENTITIES_SYNTAX = "$.entities[?(@.handle=='%s')]";

    public static String UNSUPPORTED_VCARD_SYNTAX = "$.entities[?(@.roles=='%s')].vcardArray[1][?(@[0]=='%s')]";

    RedactionObjectMapper(final RpslObjectDao rpslObjectDao){
        this.rpslObjectDao = rpslObjectDao;
    }

    public List<Redaction> createContactOrEntityRedaction(final List<RpslAttribute> rpslAttributes){
        // TODO: [MH] Part of this logic can be merge with the logic of contactEntities in RdapObjectMapper. Big refactor to make a clear merge
        final List<Redaction> redactions = Lists.newArrayList();
        for (final RpslAttribute rpslAttribute: rpslAttributes) {
            final AttributeType rpslAttributeType = AttributeType.getByName(rpslAttribute.getKey());
            if (UNSUPPORTED_ENTITIES.containsKey(rpslAttributeType)){
                createRedaction(UNSUPPORTED_ENTITIES.get(rpslAttributeType), String.format(UNSUPPORTED_ENTITIES_SYNTAX, rpslAttributeType), redactions);
            } else if (CONTACT_ATTRIBUTE_TO_ROLE_NAME.containsKey(rpslAttributeType)){
                checkContactEntityLevelCreation(redactions, rpslAttribute, rpslAttributeType);
            }
        }
        return redactions;
    }

    private void checkContactEntityLevelCreation(final List<Redaction> redactions, final RpslAttribute rpslAttribute, final AttributeType rpslAttributeType) {
        final List<RpslObject> referencedRpslObject = getObject(rpslAttributeType.getReferences(), rpslAttribute.getCleanValues());
        if (!referencedRpslObject.isEmpty()) {
            for (final Map.Entry<AttributeType, Redaction> unsupportedVcard : UNSUPPORTED_VCARDS.entrySet()) {
                for (final RpslObject rpslObject : referencedRpslObject) {
                    if (rpslObject.containsAttribute(unsupportedVcard.getKey())) {
                        createRedaction(unsupportedVcard.getValue(), String.format(UNSUPPORTED_VCARD_SYNTAX,
                                        CONTACT_ATTRIBUTE_TO_ROLE_NAME.get(rpslAttributeType).name(), unsupportedVcard.getKey()),
                                redactions);
                        break;
                    }
                }
            }
        }
    }

    private void createRedaction(final Redaction redaction, final String prePath, final List<Redaction> redactions) {
        redaction.setPrePath(prePath);
        if (!redactions.contains(redaction)){
            redactions.add(redaction);
        }
    }

    private List<RpslObject> getObject(final Set<ObjectType> possibleObjectTypes, final Set<CIString> lookupKeys){
        final List<RpslObject> objects = Lists.newArrayList();
        for (final ObjectType objectType : possibleObjectTypes) {
            objects.addAll(lookupKeys.stream().map(lookupKey -> rpslObjectDao.getByKeyOrNull(objectType, lookupKey)).filter(Objects::nonNull).toList());
        }
        return objects;
    }

}
