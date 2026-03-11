package net.ripe.db.whois.common;

import java.util.HashMap;
import java.util.Map;

// Replace control characters https://www.utf8-chartable.de/
public class CharacterSubstitutions {

    private static final Map<Character, Character> SUBSTITUTIONS_MAP = new HashMap<>();
    
    static {
        // 0x00 - 0x0f - control
        SUBSTITUTIONS_MAP.put((char)0x0000, '?');
        SUBSTITUTIONS_MAP.put((char)0x0001, '?');
        SUBSTITUTIONS_MAP.put((char)0x0002, '?');
        SUBSTITUTIONS_MAP.put((char)0x0003, '?');
        SUBSTITUTIONS_MAP.put((char)0x0004, '?');
        SUBSTITUTIONS_MAP.put((char)0x0005, '?');
        SUBSTITUTIONS_MAP.put((char)0x0006, '?');
        SUBSTITUTIONS_MAP.put((char)0x0007, '?');
        SUBSTITUTIONS_MAP.put((char)0x0008, '?');
        SUBSTITUTIONS_MAP.put((char)0x0009, '?');
        //SUBSTITUTIONS_MAP.put((char)0x000a, '?'); new line is allowed in free text
        SUBSTITUTIONS_MAP.put((char)0x000b, '?');
        SUBSTITUTIONS_MAP.put((char)0x000c, '?');
        SUBSTITUTIONS_MAP.put((char)0x000d, '?');
        SUBSTITUTIONS_MAP.put((char)0x000f, '?');

        // 0x10 - 0x1f - control
        SUBSTITUTIONS_MAP.put((char)0x0010, '?');
        SUBSTITUTIONS_MAP.put((char)0x0011, '?');
        SUBSTITUTIONS_MAP.put((char)0x0012, '?');
        SUBSTITUTIONS_MAP.put((char)0x0013, '?');
        SUBSTITUTIONS_MAP.put((char)0x0014, '?');
        SUBSTITUTIONS_MAP.put((char)0x0015, '?');
        SUBSTITUTIONS_MAP.put((char)0x0016, '?');
        SUBSTITUTIONS_MAP.put((char)0x0017, '?');
        SUBSTITUTIONS_MAP.put((char)0x0018, '?');
        SUBSTITUTIONS_MAP.put((char)0x0019, '?');
        SUBSTITUTIONS_MAP.put((char)0x001a, '?');
        SUBSTITUTIONS_MAP.put((char)0x001b, '?');
        SUBSTITUTIONS_MAP.put((char)0x001c, '?');
        SUBSTITUTIONS_MAP.put((char)0x001d, '?');
        SUBSTITUTIONS_MAP.put((char)0x001f, '?');

        // 0x7f - control
        SUBSTITUTIONS_MAP.put((char)0x007f, '?');

        // 0x80 - 0x8f - control
        SUBSTITUTIONS_MAP.put((char)0x0080, '?');
        SUBSTITUTIONS_MAP.put((char)0x0081, '?');
        SUBSTITUTIONS_MAP.put((char)0x0082, '?');
        SUBSTITUTIONS_MAP.put((char)0x0083, '?');
        SUBSTITUTIONS_MAP.put((char)0x0084, '?');
        SUBSTITUTIONS_MAP.put((char)0x0085, '?');
        SUBSTITUTIONS_MAP.put((char)0x0086, '?');
        SUBSTITUTIONS_MAP.put((char)0x0087, '?');
        SUBSTITUTIONS_MAP.put((char)0x0088, '?');
        SUBSTITUTIONS_MAP.put((char)0x0089, '?');
        SUBSTITUTIONS_MAP.put((char)0x008a, '?');
        SUBSTITUTIONS_MAP.put((char)0x008b, '?');
        SUBSTITUTIONS_MAP.put((char)0x008c, '?');
        SUBSTITUTIONS_MAP.put((char)0x008d, '?');
        SUBSTITUTIONS_MAP.put((char)0x008f, '?');

        // 0x90 - 0x9f -control
        SUBSTITUTIONS_MAP.put((char)0x0090, '?');
        SUBSTITUTIONS_MAP.put((char)0x0091, '?');
        SUBSTITUTIONS_MAP.put((char)0x0092, '?');
        SUBSTITUTIONS_MAP.put((char)0x0093, '?');
        SUBSTITUTIONS_MAP.put((char)0x0094, '?');
        SUBSTITUTIONS_MAP.put((char)0x0095, '?');
        SUBSTITUTIONS_MAP.put((char)0x0096, '?');
        SUBSTITUTIONS_MAP.put((char)0x0097, '?');
        SUBSTITUTIONS_MAP.put((char)0x0098, '?');
        SUBSTITUTIONS_MAP.put((char)0x0099, '?');
        SUBSTITUTIONS_MAP.put((char)0x009a, '?');
        SUBSTITUTIONS_MAP.put((char)0x009b, '?');
        SUBSTITUTIONS_MAP.put((char)0x009c, '?');
        SUBSTITUTIONS_MAP.put((char)0x009d, '?');
        SUBSTITUTIONS_MAP.put((char)0x009f, '?');
    }


    public static char substitute(final Character input) {
        return SUBSTITUTIONS_MAP.getOrDefault(input, input);
    }
}
