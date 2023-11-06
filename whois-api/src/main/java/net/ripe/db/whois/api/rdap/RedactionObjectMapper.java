package net.ripe.db.whois.api.rdap;

import com.google.common.collect.Sets;
import net.ripe.db.whois.api.rdap.domain.Entity;
import net.ripe.db.whois.api.rdap.domain.RdapObject;
import net.ripe.db.whois.api.rdap.domain.Redaction;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RedactionObjectMapper {

    public static void mapRedactions(final RdapObject rdapObject) {

        rdapObject.getRedacted().addAll(getRedactions(rdapObject.getRedactedRpslAttrs(), "$"));

        if(rdapObject.getNetwork() != null) {
            final String prefix = "$.network";

            rdapObject.getRedacted().addAll(getRedactions(rdapObject.getNetwork().getRedactedRpslAttrs(), prefix));
            addEntitiesRedaction(rdapObject, rdapObject.getNetwork().getEntitySearchResults(), prefix);
        }

        rdapObject.getNetworks().forEach( ip -> {
            final String prefix = String.format("$.networks[?(@.handle=='%s')]", ip.getHandle());

            rdapObject.getRedacted().addAll(getRedactions(ip.getRedactedRpslAttrs(), prefix));
            addEntitiesRedaction(rdapObject, ip.getEntitySearchResults(), prefix);
        });

        rdapObject.getAutnums().forEach( autnum -> {
            addEntitiesRedaction(rdapObject, autnum.getEntitySearchResults(), String.format("$.autnums[?(@.handle=='%s')]", autnum.getHandle()));
        });

        addEntitiesRedaction(rdapObject, rdapObject.getEntitySearchResults(), "$");
    }

    private static void addEntitiesRedaction(final RdapObject rdapObject, final List<Entity> entities, final String prefix) {
        entities.forEach( entity -> {
            rdapObject.getRedacted().addAll(getRedactions(entity.getRedactedRpslAttrs(), String.format("%s.entities[?(@.handle=='%s')]", prefix, entity.getHandle())));
        });
    }

    private static Set<Redaction> getRedactions(final List<RpslAttribute> rpslAttributes, final String prefix){
        final Set<Redaction> redactions = Sets.newHashSet();

        final Map<AttributeType, List<CIString>> attributeTypeByValues =  rpslAttributes.stream()
                .collect(Collectors.groupingBy(RpslAttribute::getType, Collectors.mapping(RpslAttribute::getCleanValue, Collectors.toList())));

        attributeTypeByValues.entrySet().forEach(entry -> {
            final String attributeName = entry.getKey().getName();
            final String values = String.join(", ", entry.getValue());

            switch(entry.getKey()){
                case NOTIFY ->
                        redactions.add(Redaction.getRedactionByRemoval("Updates notification e-mail information",
                                        String.format("%s.vcardArray[1][?(@[0]=='%s')]", prefix, attributeName),
                                        "Personal data"));
                case E_MAIL ->
                        redactions.add(Redaction.getRedactionByRemoval("Personal e-mail information",
                                        String.format("%s.vcardArray[1][?(@[0]=='%s')]", prefix, attributeName),
                                        "Personal data"));
                case COUNTRY ->
                        redactions.add(Redaction.getRedactionByPartialValue(String.format("Multiple %s attributes found", attributeName),
                                        String.format("%s.%s", prefix, attributeName),
                                        String.format("There are multiple %s attributes %s found, but only the first %s %s returned.", attributeName, values, attributeName, entry.getValue().get(0))));
                case LANGUAGE ->
                        redactions.add(Redaction.getRedactionByPartialValue(String.format("Multiple %s attributes found", attributeName),
                                String.format("%s.lang", prefix),
                                String.format("There are multiple %s attributes %s found, but only the first %s %s returned.", attributeName, values, attributeName, entry.getValue().get(0))));

            }
        });

        return redactions;
    }
}
