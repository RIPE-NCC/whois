package net.ripe.db.whois.update.handler;

import java.nio.charset.Charset;

public class CharacterSetConversion {
    public static final boolean isConvertableIntoLatin1( String value) {
        boolean status = true;

        Charset latin1Set = Charset.forName("ISO8859-1");
        if(value != null  && ! latin1Set.newEncoder().canEncode(value)) {
            status = false;
        }
        return status;
    }
}
