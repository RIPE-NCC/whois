package net.ripe.db.whois.rdap;

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
        addRedaction(rdapObject, rdapObject.getRedactedRpslAttrs(), rdapObject.getEntitySearchResults(), "$");

        if(rdapObject.getNetwork() != null) {
            addRedaction(rdapObject, rdapObject.getNetwork().getRedactedRpslAttrs(), rdapObject.getNetwork().getEntitySearchResults(), "$.network");
        }

        rdapObject.getNetworks().forEach( ip -> addRedaction(rdapObject, ip.getRedactedRpslAttrs(),
                ip.getEntitySearchResults(), String.format("$.networks[?(@.handle=='%s')]", ip.getHandle())));

        rdapObject.getAutnums().forEach( autnum -> addEntitiesRedaction(rdapObject, autnum.getEntitySearchResults(),
                String.format("$.autnums[?(@.handle=='%s')]", autnum.getHandle())));
    }

    private static void addRedaction(final RdapObject rdapObject, final List<RpslAttribute> redactedAttributes,
                                     final List<Entity> entities, final String prefix) {
        rdapObject.getRedacted().addAll(getRedactions(redactedAttributes, prefix));
        addEntitiesRedaction(rdapObject, entities, prefix);
    }

    private static void addEntitiesRedaction(final RdapObject rdapObject, final List<Entity> entities, final String prefix) {
        entities.forEach(entity -> rdapObject.getRedacted().addAll(getRedactions(entity.getRedactedRpslAttrs(),
                String.format("%s.entities[?(@.handle=='%s')]", prefix, entity.getHandle()))));
    }

    private static Set<Redaction> getRedactions(final List<RpslAttribute> rpslAttributes, final String prefix) {
        final Set<Redaction> redactions = Sets.newHashSet();

        final Map<AttributeType, List<CIString>> attributeTypeByValues =  rpslAttributes.stream()
                .collect(Collectors.groupingBy(RpslAttribute::getType, Collectors.mapping(RpslAttribute::getCleanValue, Collectors.toList())));

        attributeTypeByValues.forEach((key, value) -> {
            final String attributeName = key.getName();
            final String values = String.join(", ", value);

            switch (key) {
                case E_MAIL -> redactions.add(Redaction.getRedactionByRemoval("Personal e-mail information",
                        String.format("%s.vcardArray[1][?(@[0]=='%s')]", prefix, attributeName),
                        "Personal data"));
                case COUNTRY ->
                        redactions.add(Redaction.getRedactionByPartialValue(String.format("Multiple %s attributes found", attributeName),
                                String.format("%s.%s", prefix, attributeName),
                                String.format("There are multiple %s attributes %s found, but only the first %s %s returned.", attributeName, values, attributeName, value.get(0))));
                case LANGUAGE -> {
                        redactions.add(Redaction.getRedactionByPartialValue(String.format("Multiple %s attributes found", attributeName),
                                String.format("%s.lang", prefix),
                                String.format("There are multiple %s attributes %s found, but only the first %s %s returned.", attributeName, values, attributeName, value.get(0))));
                }
            }
        });

        return redactions;
    }
}
