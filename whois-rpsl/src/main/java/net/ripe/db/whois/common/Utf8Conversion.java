package net.ripe.db.whois.common;

import com.ibm.icu.impl.UTS46;
import com.ibm.icu.text.IDNA;
import com.ibm.icu.text.IDNA.Info;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.ibm.icu.text.IDNA.Error.DISALLOWED;
import static com.ibm.icu.text.IDNA.Error.INVALID_ACE_LABEL;

public class Utf8Conversion {

    private Utf8Conversion() {
        // do not instantiate
    }

    public static RpslObject convert(final String input) {
        final IDNA idna = UTS46.getUTS46Instance(IDNA.NONTRANSITIONAL_TO_ASCII);
        final RpslObject rpslObject = RpslObject.parse(input);
        final List<RpslAttribute> attrsToConvert = rpslObject.getAttributes();

        final Map<RpslAttribute, RpslAttribute> convertedMap = attrsToConvert.stream()
                .distinct() //No need to proceed same attributes
                .collect(Collectors.toMap(
                        Function.identity(),
                        attr -> createUtf8Attribute(idna, attr)
                ));

        return new RpslObjectBuilder(rpslObject)
                .replaceAttributes(convertedMap)
                .get();
    }

    private static RpslAttribute createUtf8Attribute(final IDNA idna, final RpslAttribute attribute){
        final StringBuilder result = new StringBuilder();

        //TODO: consider using org.apache.commons.text.StringEscapeUtils.unescapeUnicode
        final String utf8Value = StringEscapeUtils.unescapeJava(attribute.getValue());

        for (char ch : utf8Value.toCharArray()) {
            final char transformedCharacter = ControlCharacterSubstitutions.substitute(ch);

            final Info info = new Info();
            idna.nameToASCII(String.valueOf(transformedCharacter), new StringBuilder(), info);

            if (hasRelevantError(info)) {
                result.append(ControlCharacterSubstitutions.CHARACTER_REPLACEMENT);
            } else {
                result.append(transformedCharacter);  // Append the valid character as is
            }
        }

        return new RpslAttribute(attribute.getType(), result.toString());

    }

    private static boolean hasRelevantError(final Info info){
        //Do not take into account just domain-related errors
        return info.getErrors()
                .stream()
                .anyMatch(List.of(DISALLOWED, INVALID_ACE_LABEL)::contains);
    }
}
