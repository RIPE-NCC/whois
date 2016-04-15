package net.ripe.db.whois.common.rpsl.attrs;

import net.ripe.db.whois.common.domain.CIString;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DsRdata {

   /* structure of attribute from rfc
                        1 1 1 1 1 1 1 1 1 1 2 2 2 2 2 2 2 2 2 2 3 3
    0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |           key tag             |  algorithm    |  Digest type  |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |                digest  (length depends on type)               |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |                (SHA-1 digest is 20 bytes)                     |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |                                                               |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-|
   |                                                               |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-|
   |                                                               |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
          */
    // padded regex matcher group 4 out a little (max should be 96 for type 4) in case of errant whitespaces in digest

    private final int keytag;
    private final int algorithm;
    private final int digestType;
    private final String digestHexString;

    protected static final Logger LOGGER = LoggerFactory.getLogger(DsRdata.class);

        private static final Pattern RDATA_PATTERN = Pattern.compile("^(\\d+)\\s*(\\d+)\\s*(\\d+)\\s*\\(?([ 0-9a-fA-F]+)\\)?$");

      public DsRdata(final int keytag, final int algorithm, final int digestType, final String digestHexString) {
        this.keytag = keytag;
        this.algorithm = algorithm;
        this.digestType = digestType;
        this.digestHexString = digestHexString;
    }

    public int getKeytag() {
        return keytag;
    }

    public int getAlgorithm() {
        return algorithm;
    }

    public int getDigestType() {
        return digestType;
    }

    public String getDigestHexString() {
        return digestHexString;
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
        if (!matcher.matches()) { // basic validation at first using the broad RDATA_PATTERN regex
            throw new AttributeParseException("Invalid syntax", value);
        }

        final int keytag = Integer.parseInt(matcher.group(1));
        final int algorithm = Integer.parseInt(matcher.group(2));
        final int digestType = Integer.parseInt(matcher.group(3));
        final String digestAsHex = StringUtils.deleteWhitespace(matcher.group(4));

        // a little more loose validation
        if (keytag < 0 || keytag > 65535) {
            throw new AttributeParseException("Invalid keytag: " + keytag, value);
        }

        if (algorithm < 0 || algorithm > 255) {
            throw new AttributeParseException("Invalid algorithm: " + algorithm, value);
        }

        if (digestType < 0 || digestType > 255) {
            throw new AttributeParseException("Invalid digest type: " + digestType, value);
        }

        if (!DsRdataDigestType.typeSupported(digestType)) {
            // if type is unknown we should just let it through (could be new type)
            return new DsRdata(keytag, algorithm, digestType, digestAsHex);
        }

        //strictly validate digest length for known type
        if(!DsRdataDigestType.validateLength(digestType, digestAsHex)) {
            throw new AttributeParseException("Digest format is invalid for digest type " + digestType + ": ", digestAsHex);
        }

        return new DsRdata(keytag, algorithm, digestType, digestAsHex);
    }


}
