package net.ripe.db.whois.common;

import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RpslObjectCharacterConversion {

    private final static List<String> UTF8_ALLOWED_ATTRIBUTES = List.of(AttributeType.DESCR.getName(), AttributeType.REMARKS.getName());

    public static CharsetConversionResult paragraphConversion(final String paragraph){
        final RpslObject rpslObjectToConvert = RpslObject.parse(paragraph);
        final List<RpslAttribute> attrsToConvert = rpslObjectToConvert.getAttributes();

        final Map<RpslAttribute, RpslAttribute> substitutedAttributes = new HashMap<>();
        final Map<RpslAttribute, RpslAttribute> convertedMap = attrsToConvert.stream()
                .distinct() //No need to process the same attributes
                .collect(Collectors.toMap(
                        Function.identity(),
                        attr -> {
                            final RpslAttribute convertedAttribute = convertAttribute(attr);
                            if (!convertedAttribute.equals(attr)){
                                substitutedAttributes.put(attr, convertedAttribute);
                            }
                            return convertedAttribute;
                        }
                ));

        final RpslObject convertedRpslObject = new RpslObjectBuilder(rpslObjectToConvert)
                .replaceAttributes(convertedMap)
                .get();

        return new CharsetConversionResult(convertedRpslObject, substitutedAttributes);
    }

    private static RpslAttribute convertAttribute(final RpslAttribute attribute){
        return UTF8_ALLOWED_ATTRIBUTES.contains(attribute.getKey()) ?
                Utf8Conversion.createUtf8Attribute(attribute) :
                Latin1Conversion.createLatin1Attribute(attribute);
    }
}
