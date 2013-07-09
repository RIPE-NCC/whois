package net.ripe.db.whois.common.domain.attrs;

import com.google.common.base.Joiner;
import net.ripe.db.whois.common.domain.CIString;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DsRdata {
    private static final Pattern DS_RDATA_PATTERN = Pattern.compile("(?i)^([1-9][0-9]{0,3}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-4]) ([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]) (([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])|RSAMD5|DH|DSA|ECC|RSASHA1|INDIRECT|PRIVATEDNS|PRIVATEOID) ([ 0-9a-fA-F]{1,128})$");
    private static final Joiner JOINER = Joiner.on(" ");
    private static final int MAX_LENGTH = 255;

    private final int keyTag;
    private final short algorithm;
    private final CIString digestType;
    private final CIString digest;

    private DsRdata(final int keyTag, final short algorithm, final CIString digestType, final CIString digest) {
        this.keyTag = keyTag;
        this.algorithm = algorithm;
        this.digestType = digestType;
        this.digest = digest;
    }


    public int getKeyTag() {
        return keyTag;
    }

    public short getAlgorithm() {
        return algorithm;
    }

    public CIString getDigestType() {
        return digestType;
    }

    public int getDigestTypeAsInteger() {
        // TODO: [RL] map string values to their integer equivalents
        return Integer.valueOf(digestType.toString());
    }

    public CIString getDigest() {
        return digest;
    }

    @Override
    public String toString() {
        return JOINER.join(keyTag, algorithm, digestType, digest);
    }

    public static DsRdata parse(final CIString value) {
        return parse(value.toString());
    }

    public static DsRdata parse(final String value) {
        if (value.length() > MAX_LENGTH) {
            throw new AttributeParseException("Too long", value);
        }

        final Matcher match = DS_RDATA_PATTERN.matcher(value);
        if (!match.matches()) {
            throw new AttributeParseException("Invalid syntax", value);
        }

        final int keyTag = Integer.valueOf(match.group(1));
        final short algorithm = Short.valueOf(match.group(2));
        final CIString digestType = CIString.ciString(match.group(3));
        final CIString digest = CIString.ciString(match.group(5));

        return new DsRdata(keyTag,algorithm,digestType,digest);
    }
}
