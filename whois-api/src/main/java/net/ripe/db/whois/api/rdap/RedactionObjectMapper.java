package net.ripe.db.whois.api.rdap;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rdap.domain.Entity;
import net.ripe.db.whois.api.rdap.domain.RdapObject;
import net.ripe.db.whois.api.rdap.domain.Redaction;
import net.ripe.db.whois.common.rpsl.AttributeType;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RedactionObjectMapper {

    public static final List<AttributeType> REDACTED_PERSONAL_ATTR = Lists.newArrayList(AttributeType.NOTIFY);
    public static void mapRedactions(final RdapObject rdapObject) {

        addEntityRedaction(rdapObject);

        if(rdapObject.getNetwork() != null) {
            addEntitiesRedaction(rdapObject, rdapObject.getNetwork().getEntitySearchResults(), "$.network");
        }

        rdapObject.getNetworks().forEach( ip -> {
            addEntitiesRedaction(rdapObject, ip.getEntitySearchResults(), String.format("$.networks[?(@.handle=='%s')]", ip.getHandle()));
        });

        rdapObject.getAutnums().forEach( autnum -> {
            addEntitiesRedaction(rdapObject, autnum.getEntitySearchResults(), String.format("$.autnums[?(@.handle=='%s')]", autnum.getHandle()));
        });

        addEntitiesRedaction(rdapObject, rdapObject.getEntitySearchResults(), "$");
    }

    private static void addEntityRedaction(RdapObject rdapObject) {
        if(rdapObject instanceof Entity) {
            rdapObject.getRedacted().addAll(getPersonalRedaction( ((Entity) rdapObject).getvCardRedactedAttr(), "$"));
        }
    }

    private static void addEntitiesRedaction(final RdapObject rdapObject, final List<Entity> entities, final String prefix) {
        entities.forEach( entity -> {
            rdapObject.getRedacted().addAll(getPersonalRedaction(entity.getvCardRedactedAttr(), String.format("%s.entities[?(@.handle=='%s')]", prefix, entity.getHandle())));
        });
    }

    private static Set<Redaction> getPersonalRedaction(final List<AttributeType> attributeTypes, final String prefix){
        return attributeTypes.stream()
                .map( attributeType -> new Redaction("Updates notification e-mail information",
                        String.format("%s.vcardArray[1][?(@[0]=='%s')]", prefix, attributeType.getName()),
                        "Personal data")
                )
                .collect(Collectors.toSet());
    }
}
