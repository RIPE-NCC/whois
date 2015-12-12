package net.ripe.db.whois.update.handler;

import java.nio.charset.Charset;

public class CharacterSetConversion {

    private static final Charset LATIN_1_CHARSET = Charset.forName("ISO-8859-1");

    public static boolean isConvertableIntoLatin1(final String value) {
        return (value == null) || LATIN_1_CHARSET.newEncoder().canEncode(value);
    }

    public static String convertToLatin1(final String value) {
        return new String(value.getBytes(LATIN_1_CHARSET), LATIN_1_CHARSET);
    }
}
