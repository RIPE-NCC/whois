package net.ripe.db.whois.common;

import java.util.HashMap;
import java.util.Map;

// Replace control characters https://www.utf8-chartable.de/
public class UnicodeControlCharacterSanitiser {

    private static final Map<Integer, Character> CODEPOINTS_SUBSTITUTIONS_MAP = new HashMap<>();

    //Codepoints have to be used to avoid issues with surrogates
    //https://medium.com/@kaustubh.saha/a-java-developers-guide-to-surviving-unicode-strings-6a00cf94309c

    static {
        // 0x00 - 0x0f - control
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x0000, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x0001, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x0002, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x0003, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x0004, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x0005, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x0006, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x0007, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x0008, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x0009, '\t');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x000A, '\n'); //LF
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x000B, ' '); // vertical tab
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x000C, ' '); // form feed
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x000D, '\n'); //CR
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x000E, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x000F, '?');

        // 0x10 - 0x1f - control
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x0010, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x0011, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x0012, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x0013, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x0014, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x0015, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x0016, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x0017, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x0018, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x0019, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x001A, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x001B, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x001C, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x001D, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x001E, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x001F, '?');

        // 0x7f - control
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x007f, '?');

        // 0x80 - 0x8f - control
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x0080, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x0081, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x0082, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x0083, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x0084, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x0085, '\n'); // New line
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x0086, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x0087, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x0088, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x0089, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x008A, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x008B, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x008C, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x008D, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x008E, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x008F, '?');

        // 0x90 - 0x9f -control
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x0090, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x0091, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x0092, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x0093, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x0094, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x0095, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x0096, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x0097, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x0098, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x0099, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x009A, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x009B, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x009C, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x009D, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x009E, '?');
        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x009F, '?');

        CODEPOINTS_SUBSTITUTIONS_MAP.put(0x00A0, ' '); //no-break space


        // Invisible unicode surrogate pairs
        for (int cp = 0xE0100; cp <= 0xE01EF; cp++) {
            CODEPOINTS_SUBSTITUTIONS_MAP.put(cp, '?');
        }

        // Invisible unicode
        for (int cp = 0xFE00; cp <= 0xFE0F; cp++) {
            CODEPOINTS_SUBSTITUTIONS_MAP.put(cp, '?');
        }
    }

    public static char sanitiseCodePoints(final int codePoints) {
        return CODEPOINTS_SUBSTITUTIONS_MAP.getOrDefault(codePoints, (char)codePoints);
    }

}
