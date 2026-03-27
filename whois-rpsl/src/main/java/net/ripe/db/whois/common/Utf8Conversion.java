package net.ripe.db.whois.common;

import com.ibm.icu.impl.UTS46;
import com.ibm.icu.text.IDNA;
import com.ibm.icu.text.IDNA.Info;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.List;

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

    public static RpslAttribute createUtf8Attribute(final RpslAttribute attribute){
        final StringBuilder result = new StringBuilder();
        final String utf8Value = StringEscapeUtils.unescapeJava(attribute.getValue());

        utf8Value.codePoints().forEach(cp -> {
            char sanitiseCharacter = UnicodeControlCharacterSanitiser.sanitiseCodePoints(cp);
            convertUsingIDNA(result, sanitiseCharacter);
        });

        return new RpslAttribute(attribute.getKey(), result.toString());
    }

    private static void convertUsingIDNA(StringBuilder result, char transformedCharacter) {
        final IDNA idna = UTS46.getUTS46Instance(IDNA.NONTRANSITIONAL_TO_UNICODE); // avoid changing ß to ss for example
        final StringBuilder idnaTransformation = new StringBuilder();
        final Info info = new Info();

        idna.nameToUnicode(String.valueOf(transformedCharacter), idnaTransformation, info);

        if (hasRelevantError(info)) {
            result.append("?"); // Question marks replace unknown characters
        } else {
            result.append(Character.isUpperCase(transformedCharacter) ?
                    idnaTransformation.toString().toUpperCase() :
                    idnaTransformation);
        }
    }

    private static boolean hasRelevantError(final Info info){
        //Do not take into account domain-related errors
        return info.getErrors()
                .stream()
                .anyMatch(List.of(DISALLOWED, INVALID_ACE_LABEL)::contains);
    }
}
