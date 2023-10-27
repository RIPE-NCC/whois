package net.ripe.db.whois.api.rdap;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rdap.domain.Entity;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static net.ripe.db.whois.api.rdap.RedactionObjectMapper.REDACTED_PERSONAL_ATTR;
import static net.ripe.db.whois.api.rdap.domain.vcard.VCardKind.GROUP;
import static net.ripe.db.whois.api.rdap.domain.vcard.VCardKind.INDIVIDUAL;
import static net.ripe.db.whois.api.rdap.domain.vcard.VCardKind.ORGANISATION;
import static net.ripe.db.whois.common.rpsl.AttributeType.IRT;
import static net.ripe.db.whois.common.rpsl.AttributeType.ORG_NAME;
import static net.ripe.db.whois.common.rpsl.AttributeType.PERSON;
import static net.ripe.db.whois.common.rpsl.AttributeType.ROLE;

public class RdapVcardMapper {

    private static final Map<ObjectType, List<AttributeType>> PERSON_ATTRIBUTE_PER_OBJECT_TYPE = Map.of();

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

        mapNonPersonalAttributes(removePersonalAttributes(rpslObject), builder);
        entity.setVCardArray(builder.build());

        final Set<AttributeType> redactedAttributes = rpslObject.findAttributes(REDACTED_PERSONAL_ATTR).stream().map(RpslAttribute::getType).collect(Collectors.toSet());
        entity.getvCardRedactedAttr().addAll(redactedAttributes);
    }

    private static List<RpslAttribute> removePersonalAttributes(final RpslObject rpslObject){
        final List<AttributeType> personalAttributes = PERSON_ATTRIBUTE_PER_OBJECT_TYPE.get(rpslObject.getType());
        if (personalAttributes == null || personalAttributes.isEmpty()){
            return rpslObject.getAttributes();
        }
        final List<RpslAttribute> nonPersonalRpslAttributes = Lists.newArrayList();
        rpslObject.getAttributes().forEach(rpslAttribute -> {
            if (!personalAttributes.contains(rpslAttribute.getType())){
                nonPersonalRpslAttributes.add(rpslAttribute);
            }
        });
        return nonPersonalRpslAttributes;
    }

    private static void mapNonPersonalAttributes(final List<RpslAttribute> rpslAttributes, final VCardBuilder builder){
        rpslAttributes.forEach(attribute -> {
            switch(Objects.requireNonNull(attribute.getType())){
                case ADDRESS -> builder.addAdr(attribute.getCleanValues());
                case PHONE -> builder.addTel(attribute.getCleanValues());
                case FAX_NO -> builder.addFax(attribute.getCleanValues());
                case ABUSE_MAILBOX -> builder.addAbuseMailBox(attribute.getCleanValue());
                case ORG -> builder.addOrg(attribute.getCleanValues());
                case GEOLOC -> builder.addGeo(attribute.getCleanValues());
                case E_MAIL -> builder.addEmail(attribute.getCleanValues());
            }
        });
    }
}
