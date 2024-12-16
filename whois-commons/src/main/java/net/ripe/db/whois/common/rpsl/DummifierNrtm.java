package net.ripe.db.whois.common.rpsl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.domain.CIString;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Component("dummifierNrtm")
public class DummifierNrtm implements Dummifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(DummifierNrtm.class);

    static final Set<ObjectType> SKIPPED_OBJECT_TYPES = Sets.immutableEnumSet(ObjectType.PERSON, ObjectType.ROLE);

    private static final String PERSON_ROLE_PLACEHOLDER = "DUMY-RIPE";
    static final Set<AttributeType> PERSON_ROLE_REFERENCES = Sets.immutableEnumSet(
            AttributeType.ADMIN_C,
            AttributeType.AUTHOR,
            AttributeType.PING_HDL,
            AttributeType.TECH_C,
            AttributeType.ZONE_C
    );

    static final Map<ObjectType, List<AttributeType>> ATTRIBUTES_TO_KEEP_OBJECT_TYPE = Map.of(
            ObjectType.MNTNER, Lists.newArrayList(AttributeType.LAST_MODIFIED,AttributeType.CREATED),
            ObjectType.ORGANISATION, Lists.newArrayList(AttributeType.ABUSE_C,AttributeType.COUNTRY,AttributeType.LAST_MODIFIED,AttributeType.CREATED));

    static final Map<AttributeType, String> DUMMIFICATION_REPLACEMENTS = Maps.newEnumMap(AttributeType.class);
    static {
        DUMMIFICATION_REPLACEMENTS.put(AttributeType.ADDRESS, "Dummy address for %s");
        DUMMIFICATION_REPLACEMENTS.put(AttributeType.AUTH, "MD5-PW $1$SaltSalt$DummifiedMD5HashValue.   # Real value hidden for security");
        DUMMIFICATION_REPLACEMENTS.put(AttributeType.CHANGED, "unread@ripe.net 20000101");
        DUMMIFICATION_REPLACEMENTS.put(AttributeType.E_MAIL, "unread@ripe.net");
        DUMMIFICATION_REPLACEMENTS.put(AttributeType.FAX_NO, "+31205354444");
        DUMMIFICATION_REPLACEMENTS.put(AttributeType.PHONE, "+31205354444");
        DUMMIFICATION_REPLACEMENTS.put(AttributeType.UPD_TO, "unread@ripe.net");
    }

    public RpslObject dummify(final int version, final RpslObject rpslObject) {
        final ObjectType objectType = rpslObject.getType();
        Validate.isTrue(isAllowed(version, rpslObject), "The given object type should be skipped", objectType);

        // [EB]: Shortcircuit for objects we'd normally skip for old protocols.
        if (version <= 2 && usePlaceHolder(rpslObject)) {
            return objectType.equals(ObjectType.ROLE) ? getPlaceholderRoleObject() : getPlaceholderPersonObject();
        }

        final List<RpslAttribute> attributes = Lists.newArrayList(rpslObject.getAttributes());

        stripSomeNonMandatoryAttributes(attributes, objectType);
        dummifyRemainingAttributes(attributes, rpslObject.getKey());
        insertPlaceholder(attributes);

        attributes.addAll(getDummificationRemarks(rpslObject));

        return new RpslObject(rpslObject, attributes);
    }

    private void stripSomeNonMandatoryAttributes(final List<RpslAttribute> attributes, final ObjectType objectType) {
        if (!ATTRIBUTES_TO_KEEP_OBJECT_TYPE.containsKey(objectType)) {
            return;
        }
        final ObjectTemplate objectTemplate = ObjectTemplate.getTemplate(objectType);
        final Set<AttributeType> mandatoryAttributes = objectTemplate.getMandatoryAttributes();

        attributes.removeIf(attribute -> !mandatoryAttributes.contains(attribute.getType()) && !ATTRIBUTES_TO_KEEP_OBJECT_TYPE.get(objectType).contains(attribute.getType()));
    }

    private void dummifyRemainingAttributes(final List<RpslAttribute> attributes, final CIString key) {
        final Set<AttributeType> seenAttributes = Sets.newHashSet();

        for (int i = 0; i < attributes.size(); i++) {
            final RpslAttribute attribute = attributes.get(i);

            final AttributeType attributeType = attribute.getType();
            final String replacementValue = DUMMIFICATION_REPLACEMENTS.get(attributeType);
            if (replacementValue == null) {
                continue;
            }

            final RpslAttribute replacement;
            if (seenAttributes.add(attributeType)) {
                replacement = new RpslAttribute(attribute.getKey(), String.format(replacementValue, key));
            } else {
                replacement = null;
            }

            attributes.set(i, replacement);
        }

        attributes.removeIf(Objects::isNull);
    }

    private void insertPlaceholder(List<RpslAttribute> attributes) {
        final Set<AttributeType> seenAttributes = Sets.newHashSet();

        for (int i = 0; i < attributes.size(); i++) {
            final RpslAttribute attribute = attributes.get(i);

            if (!PERSON_ROLE_REFERENCES.contains(attribute.getType())) {
                continue;
            }

            if (seenAttributes.add(attribute.getType())) {
                attributes.set(i, new RpslAttribute(attribute.getKey(), PERSON_ROLE_PLACEHOLDER));
            } else {
                attributes.remove(i);
                i--;
            }
        }
    }

    public boolean isAllowed(final int version, final RpslObject rpslObject) {
        //Here PERSON and ROLE without abuseMailBox objects will be not allowed for VERSION 3
        return version <= 2 || !usePlaceHolder(rpslObject);
    }

    private boolean usePlaceHolder(final RpslObject rpslObject) {
        final ObjectType objectType = rpslObject.getType();

        return SKIPPED_OBJECT_TYPES.contains(objectType)
                && (!ObjectType.ROLE.equals(objectType) || rpslObject.findAttributes(AttributeType.ABUSE_MAILBOX).isEmpty());
    }

    private static List<RpslAttribute> getDummificationRemarks(final RpslObject rpslObject) {
        final String source = rpslObject.getValueForAttribute(AttributeType.SOURCE).toLowerCase();
        switch(source) {
            case "ripe":
            case "ripe-nonauth":
            case "ripe-grs":
            case "test":
            case "test-nonauth":
            case "test-grs":
                return Lists.newArrayList(
                        new RpslAttribute("remarks", "        ****************************"),
                        new RpslAttribute("remarks", "        * THIS OBJECT IS MODIFIED"),
                        new RpslAttribute("remarks", "        * Please note that all data that is generally regarded as personal"),
                        new RpslAttribute("remarks", "        * data has been removed from this object."),
                        new RpslAttribute("remarks", "        * To view the original object, please query the RIPE Database at:"),
                        new RpslAttribute("remarks", "        * http://www.ripe.net/whois"),
                        new RpslAttribute("remarks", "        ****************************"));

            case "afrinic-grs":
                return Lists.newArrayList(
                        new RpslAttribute("remarks", "        ****************************"),
                        new RpslAttribute("remarks", "        * THIS OBJECT IS MODIFIED"),
                        new RpslAttribute("remarks", "        * Please note that all data that is generally regarded as personal"),
                        new RpslAttribute("remarks", "        * data has been removed from this object."),
                        new RpslAttribute("remarks", "        * To view the original object, please query the AFRINIC Database at:"),
                        new RpslAttribute("remarks", "        * http://www.afrinic.net/"),
                        new RpslAttribute("remarks", "        ****************************"));

            case "apnic-grs":
                return Lists.newArrayList(
                        new RpslAttribute("remarks", "        ****************************"),
                        new RpslAttribute("remarks", "        * THIS OBJECT IS MODIFIED"),
                        new RpslAttribute("remarks", "        * Please note that all data that is generally regarded as personal"),
                        new RpslAttribute("remarks", "        * data has been removed from this object."),
                        new RpslAttribute("remarks", "        * To view the original object, please query the APNIC Database at:"),
                        new RpslAttribute("remarks", "        * http://www.apnic.net/"),
                        new RpslAttribute("remarks", "        ****************************"));

            case "arin-grs":
                return Lists.newArrayList(
                        new RpslAttribute("remarks", "        ****************************"),
                        new RpslAttribute("remarks", "        * THIS OBJECT IS MODIFIED"),
                        new RpslAttribute("remarks", "        * Please note that all data that is generally regarded as personal"),
                        new RpslAttribute("remarks", "        * data has been removed from this object."),
                        new RpslAttribute("remarks", "        * To view the original object, please query the ARIN Database at:"),
                        new RpslAttribute("remarks", "        * http://www.arin.net/"),
                        new RpslAttribute("remarks", "        ****************************"));

            case "jpirr-grs":
                return Lists.newArrayList(
                        new RpslAttribute("remarks", "        ****************************"),
                        new RpslAttribute("remarks", "        * THIS OBJECT IS MODIFIED"),
                        new RpslAttribute("remarks", "        * Please note that all data that is generally regarded as personal"),
                        new RpslAttribute("remarks", "        * data has been removed from this object."),
                        new RpslAttribute("remarks", "        * To view the original object, please query the JPIRR Database at:"),
                        new RpslAttribute("remarks", "        * http://www.nic.ad.jp/"),
                        new RpslAttribute("remarks", "        ****************************"));

            case "lacnic-grs":
                return Lists.newArrayList(
                        new RpslAttribute("remarks", "        ****************************"),
                        new RpslAttribute("remarks", "        * THIS OBJECT IS MODIFIED"),
                        new RpslAttribute("remarks", "        * Please note that all data that is generally regarded as personal"),
                        new RpslAttribute("remarks", "        * data has been removed from this object."),
                        new RpslAttribute("remarks", "        * To view the original object, please query the LACNIC Database at:"),
                        new RpslAttribute("remarks", "        * http://www.lacnic.net/"),
                        new RpslAttribute("remarks", "        ****************************"));

            case "radb-grs":
                return Lists.newArrayList(
                        new RpslAttribute("remarks", "        ****************************"),
                        new RpslAttribute("remarks", "        * THIS OBJECT IS MODIFIED"),
                        new RpslAttribute("remarks", "        * Please note that all data that is generally regarded as personal"),
                        new RpslAttribute("remarks", "        * data has been removed from this object."),
                        new RpslAttribute("remarks", "        * To view the original object, please query the RADB Database at:"),
                        new RpslAttribute("remarks", "        * http://www.radb.net/"),
                        new RpslAttribute("remarks", "        ****************************"));

            default:
                LOGGER.warn("Unknown source {} in object {}", source, rpslObject.getKey());
                return Lists.newArrayList();
        }
    }

    public static RpslObject getPlaceholderPersonObject() {
        return RpslObject.parse("" +
                        "person:         Placeholder Person Object\n" +
                        "address:        RIPE Network Coordination Centre\n" +
                        "address:        P.O. Box 10096\n" +
                        "address:        1001 EB Amsterdam\n" +
                        "address:        The Netherlands\n" +
                        "phone:          +31 20 535 4444\n" +
                        "nic-hdl:        DUMY-RIPE\n" +
                        "mnt-by:         RIPE-DBM-MNT\n" +
                        "remarks:        **********************************************************\n" +
                        "remarks:        * This is a placeholder object to protect personal data.\n" +
                        "remarks:        * To view the original object, please query the RIPE\n" +
                        "remarks:        * Database at:\n" +
                        "remarks:        * http://www.ripe.net/whois\n" +
                        "remarks:        **********************************************************\n" +
                        "created:        2009-07-24T17:00:00Z\n" +
                        "last-modified:  2009-07-24T17:00:00Z\n" +
                        "source:         RIPE"
        );
    }

    public static RpslObject getPlaceholderPersonObject(final CIString source){
        return RpslObject.parse(String.format("""
                        person:         Placeholder Person Object
                        address:        RIPE Network Coordination Centre
                        address:        P.O. Box 10096
                        address:        1001 EB Amsterdam
                        address:        The Netherlands
                        phone:          +31 20 535 4444
                        nic-hdl:        DUMY-RIPE
                        mnt-by:         RIPE-DBM-MNT
                        remarks:        **********************************************************
                        remarks:        * This is a placeholder object to protect personal data.
                        remarks:        * To view the original object, please query the RIPE
                        remarks:        * Database at:
                        remarks:        * http://www.ripe.net/whois
                        remarks:        **********************************************************
                        created:        2009-07-24T17:00:00Z
                        last-modified:  2009-07-24T17:00:00Z
                        source:         %s""", source));
    }

    public static RpslObject getPlaceholderRoleObject() {
        return RpslObject.parse("" +
                "role:           Placeholder Role Object\n" +
                "address:        RIPE Network Coordination Centre\n" +
                "address:        P.O. Box 10096\n" +
                "address:        1001 EB Amsterdam\n" +
                "address:        The Netherlands\n" +
                "phone:          +31 20 535 4444\n" +
                "e-mail:         ripe-dbm@ripe.net\n" +
                "admin-c:        DUMY-RIPE\n" +
                "tech-c:         DUMY-RIPE\n" +
                "nic-hdl:        ROLE-RIPE\n" +
                "mnt-by:         RIPE-DBM-MNT\n" +
                "remarks:        **********************************************************\n" +
                "remarks:        * This is a placeholder object to protect personal data.\n" +
                "remarks:        * To view the original object, please query the RIPE\n" +
                "remarks:        * Database at:\n" +
                "remarks:        * http://www.ripe.net/whois\n" +
                "remarks:        **********************************************************\n" +
                "created:        2009-07-24T17:00:00Z\n" +
                "last-modified:  2009-07-24T17:00:00Z\n" +
                "source:         RIPE"
        );
    }
}
