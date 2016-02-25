package net.ripe.db.whois.common;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class CharacterSetConversion {

    private static final Charset LATIN_1_CHARSET = StandardCharsets.ISO_8859_1;

    public static boolean isConvertableIntoLatin1(final String value) {
        return (value == null) || LATIN_1_CHARSET.newEncoder().canEncode(value);
    }

    public static String convertToLatin1(final String value) {
        return new String(value.getBytes(LATIN_1_CHARSET), LATIN_1_CHARSET);
    }

    public static boolean isEncodingLatin1(final String encoding) {
        return LATIN_1_CHARSET.name().equals(encoding) ||
                isAliasLatin1(encoding);
    }

    private static boolean isAliasLatin1(final String encoding) {
        for (String alias: LATIN_1_CHARSET.aliases()) {
            if (alias.equals(encoding)) {
                return true;
            }
        }
        return false;
    }
}
