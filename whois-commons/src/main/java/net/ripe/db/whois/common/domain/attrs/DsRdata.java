package net.ripe.db.whois.common.domain.attrs;

import net.ripe.db.whois.common.domain.CIString;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DsRdata {
    private static final Pattern RDATA_PATTERN = Pattern.compile("^(\\d+) (\\d+) (\\d+) ([ 0-9a-fA-F]{1,128})$");

    private final int keytag;
    private final int algorithm;
    private final int digestType;
    private final String digestHexString;

    public DsRdata(final int keytag, final int algorithm, final int digestType, final String digestHexString) {
        this.keytag = keytag;
        this.algorithm = algorithm;
        this.digestType = digestType;
        this.digestHexString = digestHexString;
    }

    @Override
    public String toString() {
        return String.format("%d %d %d %s", keytag, algorithm, digestType, digestHexString);
    }

    public static DsRdata parse(final CIString value) {
        return parse(value.toString());
    }

    public static DsRdata parse(final String value) {
        final Matcher matcher = RDATA_PATTERN.matcher(value);
        if (!matcher.matches()) {
            throw new AttributeParseException("Invalid syntax", value);
        }

        final int keytag = Integer.parseInt(matcher.group(1));
        final int algorithm = Integer.parseInt(matcher.group(2));
        final int digestType = Integer.parseInt(matcher.group(3));
        final String digestAsHex = matcher.group(4);

        if (keytag < 0 || keytag > 65535) {
            throw new AttributeParseException("Invalid keytag: " + keytag, value);
        }

        if (algorithm < 0 || algorithm > 255) {
            throw new AttributeParseException("Invalid algorithm: " + algorithm, value);
        }

        if (digestType < 0 || digestType > 255) {
            throw new AttributeParseException("Invalid digest type: " + digestType, value);
        }

        return new DsRdata(keytag, algorithm, digestType, digestAsHex);
    }
}
