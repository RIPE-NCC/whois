package net.ripe.db.whois.common.domain.attrs;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import net.ripe.db.whois.common.domain.CIString;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DsRdata {
    // TODO: [RL] This format differs from the documentation in AttributeSyntax and from the presentation format in RFC4034. It's probably wrong.
    private static final Pattern DS_RDATA_PATTERN = Pattern.compile("(?i)^([1-9][0-9]{0,3}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-4]) ([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]) (([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])|RSAMD5|DH|DSA|ECC|RSASHA1|INDIRECT|PRIVATEDNS|PRIVATEOID) ([ 0-9a-fA-F]{1,128})$");
    private static final Joiner JOINER = Joiner.on(" ");
    private static final int MAX_LENGTH = 255;

    private static final HashMap<CIString, Short> algorithmMap = Maps.newHashMap();

    static {
        algorithmMap.put(CIString.ciString("RSAMD5"), (short)1);
        algorithmMap.put(CIString.ciString("DH"), (short)2);
        algorithmMap.put(CIString.ciString("DSA"), (short)3);
        algorithmMap.put(CIString.ciString("ECC"), (short)4);
        algorithmMap.put(CIString.ciString("RSASHA1"), (short)5);
        algorithmMap.put(CIString.ciString("INDIRECT"), (short)252);
        algorithmMap.put(CIString.ciString("PRIVATEDNS"), (short)253);
        algorithmMap.put(CIString.ciString("PRIVATEOID"), (short)254);
    }

    private final int keyTag;
    private final CIString algorithm;
    private final short digestType;
    private final CIString digest;

    private DsRdata(final int keyTag, final CIString algorithm, final short digestType, final CIString digest) {
        this.keyTag = keyTag;
        this.algorithm = algorithm;
        this.digestType = digestType;
        this.digest = digest;
    }


    public int getKeyTag() {
        return keyTag;
    }

    public CIString getAlgorithm() {
        return algorithm;
    }

    public short getAlgorithmNumber() {
        try {
            return Short.valueOf(algorithm.toString());
        } catch(NumberFormatException e) {
            return algorithmMap.get(algorithm);
        }
    }

    public short getDigestType() {
        return digestType;
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
        final CIString algorithm = CIString.ciString(match.group(2));
        final short digestType = Short.valueOf(match.group(3));
        final CIString digest = CIString.ciString(match.group(5));

        return new DsRdata(keyTag, algorithm, digestType, digest);
    }
}
