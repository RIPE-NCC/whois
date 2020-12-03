package net.ripe.db.whois.common;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * Convert input to latin-1 with substitutions.
 */
public class Latin1Conversion {

    private static final Logger LOGGER = LoggerFactory.getLogger(Latin1Conversion.class);

    /**
     * Byte substitutions to sanitise latin-1 input (which has single byte encoding).
     *
     * Make the following substitutions:
     * - 0x00 - 0x1f Control Characters subsituted with '?' (0x3f)
     *      - exceptions are tab (0x09), linefeed (0x0a), carriage return (0x0d).
     * - 0x7F DELETE substituted with '?' (0x3f).
     * - 0x80 - 0x9F Extended Control Characters, substituted with '?' (0x3f).
     * - 0xa0 - Non-break space substituted with regular space.
     * - 0xad - Silent hyphen is substituted by a regular hyphen (0x2d)
     *
     * Ref. https://en.wikipedia.org/wiki/ISO/IEC_8859-1
     */
    private static final byte[] SUBSTITUTIONS = new byte[] {
        0x3f, 0x3f, 0x3f, 0x3f, 0x3f, 0x3f, 0x3f, 0x3f, 0x3f, 0x09, 0x0a, 0x3f, 0x3f, 0x0d, 0x3f, 0x3f,     // 0x00 - 0x0f
        0x3f, 0x3f, 0x3f, 0x3f, 0x3f, 0x3f, 0x3f, 0x3f, 0x3f, 0x3f, 0x3f, 0x3f, 0x3f, 0x3f, 0x3f, 0x3f,     // 0x10 - 0x1f
        0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2a, 0x2b, 0x2c, 0x2d, 0x2e, 0x2f,     // 0x20 - 0x2f
        0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3a, 0x3b, 0x3c, 0x3d, 0x3e, 0x3f,     // 0x30 - 0x3f
        0x40, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a, 0x4b, 0x4c, 0x4d, 0x4e, 0x4f,     // 0x40 - 0x4f
        0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5a, 0x5b, 0x5c, 0x5d, 0x5e, 0x5f,     // 0x50 - 0x5f
        0x60, 0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6a, 0x6b, 0x6c, 0x6d, 0x6e, 0x6f,     // 0x60 - 0x6f
        0x70, 0x71, 0x72, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7a, 0x7b, 0x7c, 0x7d, 0x7e, 0x3f,     // 0x70 - 0x7f
        0x3f, 0x3f, 0x3f, 0x3f, 0x3f, 0x3f, 0x3f, 0x3f, 0x3f, 0x3f, 0x3f, 0x3f, 0x3f, 0x3f, 0x3f, 0x3f,     // 0x80 - 0x8f
        0x3f, 0x3f, 0x3f, 0x3f, 0x3f, 0x3f, 0x3f, 0x3f, 0x3f, 0x3f, 0x3f, 0x3f, 0x3f, 0x3f, 0x3f, 0x3f,     // 0x90 - 0x9f
        (byte)0x20, (byte)0xa1, (byte)0xa2, (byte)0xa3, (byte)0xa4, (byte)0xa5, (byte)0xa6, (byte)0xa7,     // 0xa0 - 0xaf
        (byte)0xa8, (byte)0xa9, (byte)0xaa, (byte)0xab, (byte)0xac, (byte)0x2d, (byte)0xae, (byte)0xaf,
        (byte)0xb0, (byte)0xb1, (byte)0xb2, (byte)0xb3, (byte)0xb4, (byte)0xb5, (byte)0xb6, (byte)0xb7,     // 0xb0 - 0xbf
        (byte)0xb8, (byte)0xb9, (byte)0xba, (byte)0xbb, (byte)0xbc, (byte)0xbd, (byte)0xbe, (byte)0xbf,
        (byte)0xc0, (byte)0xc1, (byte)0xc2, (byte)0xc3, (byte)0xc4, (byte)0xc5, (byte)0xc6, (byte)0xc7,     // 0xc0 - 0xcf
        (byte)0xc8, (byte)0xc9, (byte)0xca, (byte)0xcb, (byte)0xcc, (byte)0xcd, (byte)0xce, (byte)0xcf,
        (byte)0xd0, (byte)0xd1, (byte)0xd2, (byte)0xd3, (byte)0xd4, (byte)0xd5, (byte)0xd6, (byte)0xd7,     // 0xd0 - 0xdf
        (byte)0xd8, (byte)0xd9, (byte)0xda, (byte)0xdb, (byte)0xdc, (byte)0xdd, (byte)0xde, (byte)0xdf,
        (byte)0xe0, (byte)0xe1, (byte)0xe2, (byte)0xe3, (byte)0xe4, (byte)0xe5, (byte)0xe6, (byte)0xe7,     // 0xe0 - 0xef
        (byte)0xe8, (byte)0xe9, (byte)0xea, (byte)0xeb, (byte)0xec, (byte)0xed, (byte)0xee, (byte)0xef,
        (byte)0xf0, (byte)0xf1, (byte)0xf2, (byte)0xf3, (byte)0xf4, (byte)0xf5, (byte)0xf6, (byte)0xf7,     // 0xf0 - 0xff
        (byte)0xf8, (byte)0xf9, (byte)0xfa, (byte)0xfb, (byte)0xfc, (byte)0xfd, (byte)0xfe, (byte)0xff
    };

    static {
        if (SUBSTITUTIONS.length != 256) {
            // the array must contain substitutions for all single-byte values
            throw new IllegalStateException("Unexpected SUBSTITUTIONS length: " + SUBSTITUTIONS.length);
        }
    }

    private Latin1Conversion() {
        // do not instantiate
    }

    // NB. input byte[] parameter is modified in-place
    private static byte[] convert(@Nonnull final byte[] input) {
        for (int offset = 0; offset < input.length; offset++) {
            input[offset] = SUBSTITUTIONS[((int) input[offset]) & 0xff];
        }
        return input;
    }

    /**
     * Convert a String into latin-1 with substitutions.
     * @param value
     * @return
     */
    public static Latin1ConversionResult convert(@Nonnull final String value) {
        final CharsetEncoder charsetEncoder = StandardCharsets.ISO_8859_1.newEncoder();

        charsetEncoder.onMalformedInput(CodingErrorAction.REPLACE);
        charsetEncoder.onUnmappableCharacter(CodingErrorAction.REPLACE);

        try {
            final ByteBuffer encoded = charsetEncoder.encode(CharBuffer.wrap(value));

            final RpslObject rpslObject = RpslObject.parse(encoded.array());
            final String convertedRpslObject = new String(encoded.array(), StandardCharsets.ISO_8859_1);
            final boolean globalSubstitution = !convertedRpslObject.equals(value);

            convert(encoded);

            final RpslObject substitutedRpslObject = RpslObject.parse(encoded.array());

            Set<RpslAttribute> substitutedAttributes = Sets.newHashSet();
            for (int offset = 0; offset < rpslObject.getAttributes().size(); offset++) {
                final RpslAttribute attribute = rpslObject.getAttributes().get(offset);
                final RpslAttribute updatedAttribute = substitutedRpslObject.getAttributes().get(offset);

                if (!attribute.equals(updatedAttribute)) {
                    substitutedAttributes.add(updatedAttribute);
                }
            }

            return new Latin1ConversionResult(substitutedRpslObject, globalSubstitution, substitutedAttributes);
        } catch (CharacterCodingException e) {
            LOGGER.error(value, e);
            throw new IllegalStateException(e);
        }
    }

    public static String convertString(@Nonnull final String value) {
        final CharsetEncoder charsetEncoder = StandardCharsets.ISO_8859_1.newEncoder();

        charsetEncoder.onMalformedInput(CodingErrorAction.REPLACE);
        charsetEncoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
        try {
            final ByteBuffer encoded = charsetEncoder.encode(CharBuffer.wrap(value));
            convert(encoded);
            return new String(encoded.array(), StandardCharsets.ISO_8859_1);
        } catch (CharacterCodingException e) {
            LOGGER.error(value, e);
            throw new IllegalStateException(e);
        }

    }

    private static void convert(final ByteBuffer bb) {
        convert(bb.array());
    }

}
