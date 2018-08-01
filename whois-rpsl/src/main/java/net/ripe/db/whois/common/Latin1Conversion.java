package net.ripe.db.whois.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

/**
 * Convert String to the latin-1 character set.
 *
 * (1) Control Codes (unicode \u0000 -> \u009F)
 *     Substitute all (except for tab, linefeed, carriage return)
 * (2) Latin Script (unicode \u0020 -> \u007E)
 *     Maps directly to latin-1.
 * (3) Latin-1 Supplement (unicode \u00A0 -> \u00FF)
 *     Substitute non-break space \u00A0 with regular space.
 * (4) Characters outside latin-1
 *     Substitute '?' character.
 */
public class Latin1Conversion {

    private static final Logger LOGGER = LoggerFactory.getLogger(Latin1Conversion.class);

    private static final byte[] ACCEPTABLE_CONTROL_CHARACTERS = new byte[] {'\n', '\r', '\t'};

    public static boolean isLatin1(final String value) {
        return (value == null) || !needsEncoding(value);
    }

    public static String convertToLatin1(final String value) {
        final CharsetEncoder charsetEncoder = StandardCharsets.ISO_8859_1.newEncoder();
        final CharsetDecoder charsetDecoder = StandardCharsets.ISO_8859_1.newDecoder();

        charsetEncoder.onMalformedInput(CodingErrorAction.REPLACE);
        charsetEncoder.onUnmappableCharacter(CodingErrorAction.REPLACE);

        try {
            final ByteBuffer encoded = charsetEncoder.encode(CharBuffer.wrap(value));
            replaceNonPrintableCharacters(encoded);
            return charsetDecoder.decode(encoded).toString();
        } catch (CharacterCodingException e) {
            LOGGER.error(value, e);
            throw new IllegalStateException(e);
        }
    }

    private static boolean needsEncoding(final String input) {
        for (int codepoint, offset = 0; offset < input.length(); offset += Character.charCount(codepoint)) {
            codepoint = input.codePointAt(offset);
            if (needsEncoding(codepoint)) {
                return true;
            }
        }

        return false;
    }

    private static boolean needsEncoding(final int codePoint) {
        return isOutsideLatin1(codePoint) ||
            isControlCharacter(codePoint) ||
            isNonBreakSpace(codePoint);
    }

    private static boolean isControlCharacter(final int codePoint) {
        return (codePoint >= 0 && codePoint < '\u0020') && !contains(ACCEPTABLE_CONTROL_CHARACTERS, (byte) codePoint);
    }

    private static boolean isControlCharacter(final byte codePoint) {
        return (codePoint >= 0 && codePoint < 0x20) && !contains(ACCEPTABLE_CONTROL_CHARACTERS, codePoint);
    }

    private static boolean isNonBreakSpace(final int codePoint) {
        return codePoint == '\u00a0';
    }

    private static boolean isNonBreakSpace(final byte codePoint) {
        return codePoint == (byte)0xA0;
    }

    private static boolean isOutsideLatin1(final int codePoint) {
        return codePoint > '\u00FF';
    }

    private static void replaceNonPrintableCharacters(final ByteBuffer bb) {
        bb.rewind();
        while (bb.hasRemaining()) {
            final byte b = bb.get();
            if (isNonBreakSpace(b)) {
                replace(bb, ' ');
            } else {
                if (isControlCharacter(b)) {
                    replace(bb, '?');
                }
            }
        }

        bb.rewind();
    }

    private static void replace(final ByteBuffer bb, final char replacement) {
        bb.position(bb.position() - 1);
        bb.put((byte)replacement);
    }

    private static boolean contains(final byte[] array, final byte b) {
        for (byte ab : array) {
            if (ab == b) {
                return true;
            }
        }
        return false;
    }
}
