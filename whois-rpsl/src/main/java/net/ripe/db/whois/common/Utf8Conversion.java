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

/***
 * Decode escape sequences into corresponding characters
 * ASCII substitutions
 * UTF-8 substitutions
 */

public class Utf8Conversion {

    private Utf8Conversion() {
        // do not instantiate
    }

    public static RpslObject convert(final String punycodeConversion) {
        //TODO: consider using org.apache.commons.text.StringEscapeUtils.unescapeUnicode
        final String utf8Value = StringEscapeUtils.unescapeJava(punycodeConversion);
        final RpslObject rpslObject = RpslObject.parse(utf8Value);
        final List<RpslAttribute> attrsToConvert = rpslObject.getAttributes();

        final Map<RpslAttribute, RpslAttribute> convertedMap = attrsToConvert.stream()
                .distinct() //No need to process the same attributes
                .collect(Collectors.toMap(
                        Function.identity(),
                        Utf8Conversion::createUtf8Attribute
                ));

        return new RpslObjectBuilder(rpslObject)
                .replaceAttributes(convertedMap)
                .get();
    }

    public static String convertString(final String value) {
        //TODO: consider using org.apache.commons.text.StringEscapeUtils.unescapeUnicode
        final String utf8Value = StringEscapeUtils.unescapeJava(value);
        final StringBuilder result = new StringBuilder();

        for (char ch : utf8Value.toCharArray()) {
            convertChar(result, ch);
        }

        return result.toString();
    }

    public static RpslAttribute createUtf8Attribute(final RpslAttribute attribute){
        final StringBuilder result = new StringBuilder();
        final String utf8Value = StringEscapeUtils.unescapeJava(attribute.getValue());

        for (char ch : utf8Value.toCharArray()) {
            //ASCII Substitutes
            convertChar(result, ch);
        }

        return new RpslAttribute(attribute.getKey(), result.toString());
    }

    private static void convertChar(final StringBuilder result, final char ch) {
        final IDNA idna = UTS46.getUTS46Instance(IDNA.NONTRANSITIONAL_TO_UNICODE); // avoid changing ß to ss for example

        final char transformedCharacter = CharacterSubstitutions.substitute(ch);
        final Info info = new Info();
        final StringBuilder idnaTransformation = new StringBuilder();
        idna.nameToUnicode(String.valueOf(transformedCharacter), idnaTransformation, info);

        if (hasRelevantError(info)) {
            result.append("?"); // Unknown characters are replaced by question marks
        } else {
            result.append(Character.isUpperCase(transformedCharacter) ?
                    idnaTransformation.toString().toUpperCase() :
                    idnaTransformation);
        }
    }

    private static boolean hasRelevantError(final Info info){
        //Do not take into account just domain-related errors
        return info.getErrors()
                .stream()
                .anyMatch(List.of(DISALLOWED, INVALID_ACE_LABEL)::contains);
    }
}
