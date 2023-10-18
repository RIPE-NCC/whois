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
    public static void mapCommmonRedactions(final RdapObject rdapObject) {

        if(rdapObject.getNetwork() != null) {
            createEntitiesRedaction(rdapObject, rdapObject.getNetwork().getEntitySearchResults(), "$.network");
        }

        rdapObject.getNetworks().forEach( ip -> {
            createEntitiesRedaction(rdapObject, ip.getEntitySearchResults(), String.format("$.networks[?(@.handle=='%s')]", ip.getHandle()));
        });

        rdapObject.getAutnums().forEach( autnum -> {
            createEntitiesRedaction(rdapObject, autnum.getEntitySearchResults(), String.format("$.autnums[?(@.handle=='%s')]", autnum.getHandle()));
        });

        createEntitiesRedaction(rdapObject, rdapObject.getEntitySearchResults(), "$");
    }

    public static void addEntityRedaction(final Entity entity) {
        entity.getRedacted().addAll(getPersonalRedaction(entity.getvCardRedactedAttr(), "$"));
    }

    private static void createEntitiesRedaction(final RdapObject rdapObject, final List<Entity> entities, final String prefix) {
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
