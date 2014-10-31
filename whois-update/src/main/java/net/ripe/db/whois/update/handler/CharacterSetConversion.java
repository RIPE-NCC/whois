package net.ripe.db.whois.update.handler;

import java.nio.charset.Charset;

public class CharacterSetConversion {

    private static final Charset LATIN_1_CHARSET = Charset.forName("ISO-8859-1");

    public static boolean isConvertableIntoLatin1(final String value) {
        return (value == null) || LATIN_1_CHARSET.newEncoder().canEncode(value);
    }
}
