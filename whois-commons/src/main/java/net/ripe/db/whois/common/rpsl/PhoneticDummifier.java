package net.ripe.db.whois.common.rpsl;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Dummify a String using the NATO phonetic alphabet.
 *
 * Ref. https://en.wikipedia.org/wiki/NATO_phonetic_alphabet
 * Ref. AbstractAutoKeyFactory for mapping between words and the id.
 */
public class PhoneticDummifier {

    private static final Splitter SPACE_SPLITTER = Splitter.on(' ').omitEmptyStrings().trimResults();
    private static final Pattern LETTER_PATTERN = Pattern.compile("^([a-zA-Z])");

    // limit the dummified name to maximum 4 words (one character from the beginning of each word)
    private static final long MAXIMUM_CHARACTERS = 4L;

    // pad the dummified name with a specific character to be mapped to a word
    private static final String PADDING_STRING = "A";

    // pad the dummified name with a minimum number of characters (which is mapped to words)
    private static final int PADDING_SIZE = 2;

    private static final Map<String, String> MAP = Maps.newHashMap();
    static {
        MAP.put("A", "Alpha");
        MAP.put("B", "Bravo");
        MAP.put("C", "Charlie");
        MAP.put("D", "Delta");
        MAP.put("E", "Echo");
        MAP.put("F", "Foxtrot");
        MAP.put("G", "Golf");
        MAP.put("H", "Hotel");
        MAP.put("I", "India");
        MAP.put("J", "Juliet");
        MAP.put("K", "Kilo");
        MAP.put("L", "Lima");
        MAP.put("M", "Mike");
        MAP.put("N", "November");
        MAP.put("O", "Oscar");
        MAP.put("P", "Papa");
        MAP.put("Q", "Quebec");
        MAP.put("R", "Romeo");
        MAP.put("S", "Sierra");
        MAP.put("T", "Tango");
        MAP.put("U", "Uniform");
        MAP.put("V", "Victor");
        MAP.put("W", "Whiskey");
        MAP.put("X", "X-ray");
        MAP.put("Y", "Yankee");
        MAP.put("Z", "Zulu");
    }

    final String value;

    public PhoneticDummifier(final String input) {
        this.value = dummify(input);
    }

    private String dummify(final String input) {
        return map(
            StringUtils.rightPad(
                StreamSupport.stream(SPACE_SPLITTER.split(input).spliterator(), false)
                    .map(word -> {
                        final Matcher matcher = LETTER_PATTERN.matcher(word);
                        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
                    })
                    .filter(Optional::isPresent)
                    .map(optional -> optional.get().toString())
                    .limit(MAXIMUM_CHARACTERS)
                    .collect(Collectors.joining()),
                PADDING_SIZE,
                PADDING_STRING));
    }

    private String map(final String input) {
        return Arrays.stream(input.split(""))
            .map(String::toUpperCase)
            .map(MAP::get)
            .collect(Collectors.joining(" "));
    }

    @Override
    public String toString() {
        return value;
    }
}
