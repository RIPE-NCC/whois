package net.ripe.db.whois.api.rdap;

import net.ripe.db.whois.api.rdap.domain.Entity;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;

import java.util.Map;
import java.util.Set;

import static net.ripe.db.whois.api.rdap.domain.vcard.VCardKind.GROUP;
import static net.ripe.db.whois.api.rdap.domain.vcard.VCardKind.INDIVIDUAL;
import static net.ripe.db.whois.api.rdap.domain.vcard.VCardKind.ORGANISATION;
import static net.ripe.db.whois.common.rpsl.AttributeType.E_MAIL;
import static net.ripe.db.whois.common.rpsl.AttributeType.IRT;
import static net.ripe.db.whois.common.rpsl.AttributeType.NOTIFY;
import static net.ripe.db.whois.common.rpsl.AttributeType.ORG_NAME;
import static net.ripe.db.whois.common.rpsl.AttributeType.PERSON;
import static net.ripe.db.whois.common.rpsl.AttributeType.ROLE;

public class RdapVcardMapper {

    private static final Map<AttributeType, Set<ObjectType>> PERSONAL_ATTR_TO_OBJECT_TYPE = Map.of(
            NOTIFY, Set.of(ObjectType.values()), //personal attribute in all the objects
            E_MAIL, Set.of(ObjectType.ROLE, ObjectType.PERSON));

    public static void createVCard(final Entity entity, final RpslObject rpslObject) {
        VCardBuilder builder = new VCardBuilder();
        builder.addVersion();

        switch (rpslObject.getType()) {
            case PERSON -> builder.addFn(rpslObject.getValueForAttribute(PERSON)).addKind(INDIVIDUAL);
            case MNTNER -> builder.addFn(rpslObject.getValueForAttribute(AttributeType.MNTNER)).addKind(INDIVIDUAL);
            case ORGANISATION -> builder.addFn(rpslObject.getValueForAttribute(ORG_NAME)).addKind(ORGANISATION);
            case ROLE -> builder.addFn(rpslObject.getValueForAttribute(ROLE)).addKind(GROUP);
            case IRT -> builder.addFn(rpslObject.getValueForAttribute(IRT)).addKind(GROUP);
        }

        processAttributes(entity, rpslObject, builder);
    }

    private static void processAttributes(final Entity entity, final RpslObject rpslObject, final VCardBuilder builder){
        rpslObject.getAttributes().forEach(rpslAttribute -> {
            if (isPersonalAttribute(rpslObject.getType(), rpslAttribute)) {
                entity.getvCardRedactedAttr().add(rpslAttribute.getType());
            } else {
                mapNonPersonalAttributes(rpslAttribute, builder);
            }
        });
        entity.setVCardArray(builder.build());
    }

    private static boolean isPersonalAttribute(final ObjectType objectType, final RpslAttribute rpslAttribute) {
        return PERSONAL_ATTR_TO_OBJECT_TYPE.containsKey(rpslAttribute.getType()) && PERSONAL_ATTR_TO_OBJECT_TYPE.get(rpslAttribute.getType()).contains(objectType);
    }

    private static void mapNonPersonalAttributes(final RpslAttribute attribute, final VCardBuilder builder){
        switch(attribute.getType()){
            case ADDRESS -> builder.addAdr(attribute.getCleanValues());
            case PHONE -> builder.addTel(attribute.getCleanValues());
            case FAX_NO -> builder.addFax(attribute.getCleanValues());
            case ABUSE_MAILBOX -> builder.addAbuseMailBox(attribute.getCleanValue());
            case ORG -> builder.addOrg(attribute.getCleanValues());
            case GEOLOC -> builder.addGeo(attribute.getCleanValues());
            case E_MAIL -> builder.addEmail(attribute.getCleanValues());
        };
    }
}
