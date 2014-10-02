package net.ripe.db.whois.update.handler;

import java.nio.charset.Charset;

public class CharacterSetConversion {

    private final static Charset latin1Set = Charset.forName("ISO8859-1");

    public static boolean isConvertableIntoLatin1(String value) {
        return (value == null) || latin1Set.newEncoder().canEncode(value);
    }
}
