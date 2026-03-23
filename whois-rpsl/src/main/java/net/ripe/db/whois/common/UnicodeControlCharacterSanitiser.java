package net.ripe.db.whois.common;

import java.util.HashMap;
import java.util.Map;

// Replace control characters https://www.utf8-chartable.de/
public class UnicodeControlCharacterSanitiser {

    private static final Map<Character, Character> SUBSTITUTIONS_MAP = new HashMap<>();
    
    static {
        // 0x00 - 0x0f - control
        SUBSTITUTIONS_MAP.put('\u0000', '?');
        SUBSTITUTIONS_MAP.put('\u0001', '?');
        SUBSTITUTIONS_MAP.put('\u0002', '?');
        SUBSTITUTIONS_MAP.put('\u0003', '?');
        SUBSTITUTIONS_MAP.put('\u0004', '?');
        SUBSTITUTIONS_MAP.put('\u0005', '?');
        SUBSTITUTIONS_MAP.put('\u0006', '?');
        SUBSTITUTIONS_MAP.put('\u0007', '?');
        SUBSTITUTIONS_MAP.put('\u0008', '?');
        SUBSTITUTIONS_MAP.put('\u0009', '\t');
        SUBSTITUTIONS_MAP.put('\n', '\n'); //LF
        SUBSTITUTIONS_MAP.put('\u000b', ' '); // vertical tab
        SUBSTITUTIONS_MAP.put('\u000c', ' '); // form feed
        SUBSTITUTIONS_MAP.put('\r', '\n'); //CR
        SUBSTITUTIONS_MAP.put('\u000e', '?');
        SUBSTITUTIONS_MAP.put('\u000f', '?');

        // 0x10 - 0x1f - control
        SUBSTITUTIONS_MAP.put('\u0010', '?');
        SUBSTITUTIONS_MAP.put('\u0011', '?');
        SUBSTITUTIONS_MAP.put('\u0012', '?');
        SUBSTITUTIONS_MAP.put('\u0013', '?');
        SUBSTITUTIONS_MAP.put('\u0014', '?');
        SUBSTITUTIONS_MAP.put('\u0015', '?');
        SUBSTITUTIONS_MAP.put('\u0016', '?');
        SUBSTITUTIONS_MAP.put('\u0017', '?');
        SUBSTITUTIONS_MAP.put('\u0018', '?');
        SUBSTITUTIONS_MAP.put('\u0019', '?');
        SUBSTITUTIONS_MAP.put('\u001a', '?');
        SUBSTITUTIONS_MAP.put('\u001b', '?');
        SUBSTITUTIONS_MAP.put('\u001c', '?');
        SUBSTITUTIONS_MAP.put('\u001d', '?');
        SUBSTITUTIONS_MAP.put('\u001e', '?');
        SUBSTITUTIONS_MAP.put('\u001f', '?');

        // 0x7f - control
        SUBSTITUTIONS_MAP.put('\u007f', '?');

        // 0x80 - 0x8f - control
        SUBSTITUTIONS_MAP.put('\u0080', '?');
        SUBSTITUTIONS_MAP.put('\u0081', '?');
        SUBSTITUTIONS_MAP.put('\u0082', '?');
        SUBSTITUTIONS_MAP.put('\u0083', '?');
        SUBSTITUTIONS_MAP.put('\u0084', '?');
        SUBSTITUTIONS_MAP.put('\u0085', '\n'); // New line
        SUBSTITUTIONS_MAP.put('\u0086', '?');
        SUBSTITUTIONS_MAP.put('\u0087', '?');
        SUBSTITUTIONS_MAP.put('\u0088', '?');
        SUBSTITUTIONS_MAP.put('\u0089', '?');
        SUBSTITUTIONS_MAP.put('\u008a', '?');
        SUBSTITUTIONS_MAP.put('\u008b', '?');
        SUBSTITUTIONS_MAP.put('\u008c', '?');
        SUBSTITUTIONS_MAP.put('\u008d', '?');
        SUBSTITUTIONS_MAP.put('\u008e', '?');
        SUBSTITUTIONS_MAP.put('\u008f', '?');

        // 0x90 - 0x9f -control
        SUBSTITUTIONS_MAP.put('\u0090', '?');
        SUBSTITUTIONS_MAP.put('\u0091', '?');
        SUBSTITUTIONS_MAP.put('\u0092', '?');
        SUBSTITUTIONS_MAP.put('\u0093', '?');
        SUBSTITUTIONS_MAP.put('\u0094', '?');
        SUBSTITUTIONS_MAP.put('\u0095', '?');
        SUBSTITUTIONS_MAP.put('\u0096', '?');
        SUBSTITUTIONS_MAP.put('\u0097', '?');
        SUBSTITUTIONS_MAP.put('\u0098', '?');
        SUBSTITUTIONS_MAP.put('\u0099', '?');
        SUBSTITUTIONS_MAP.put('\u009a', '?');
        SUBSTITUTIONS_MAP.put('\u009b', '?');
        SUBSTITUTIONS_MAP.put('\u009c', '?');
        SUBSTITUTIONS_MAP.put('\u009d', '?');
        SUBSTITUTIONS_MAP.put('\u009e', '?');
        SUBSTITUTIONS_MAP.put('\u009f', '?');

        SUBSTITUTIONS_MAP.put('\u00a0', ' '); //no-break space
    }


    public static char sanitise(final Character input) {
        return SUBSTITUTIONS_MAP.getOrDefault(input, input);
    }
}
