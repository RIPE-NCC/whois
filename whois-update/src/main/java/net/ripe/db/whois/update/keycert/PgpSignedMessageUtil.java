package net.ripe.db.whois.update.keycert;

import org.bouncycastle.openpgp.PGPSignature;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SignatureException;

/**
 * Signed message util, mostly copied from BouncyCastle PGP tests.
 */
final class PgpSignedMessageUtil {
    private PgpSignedMessageUtil() {
    }

    static int readInputLine(final ByteArrayOutputStream out, final InputStream in) throws IOException {
        out.reset();

        int lookAhead = -1;
        int ch;

        while ((ch = in.read()) >= 0) {
            out.write(ch);
            if (ch == '\r' || ch == '\n') {
                lookAhead = readPassedEOL(out, ch, in);
                break;
            }
        }

        return lookAhead;
    }

    static int readInputLine(final ByteArrayOutputStream out, int lookAhead, final InputStream in) throws IOException {
        out.reset();

        int ch = lookAhead;

        do {
            out.write(ch);
            if (ch == '\r' || ch == '\n') {
                lookAhead = readPassedEOL(out, ch, in);
                break;
            }
        }
        while ((ch = in.read()) >= 0);

        if (ch < 0) {
            lookAhead = -1;
        }

        return lookAhead;
    }

    static int readPassedEOL(final ByteArrayOutputStream out, final int lastCh, final InputStream in) throws IOException {
        int lookAhead = in.read();

        if (lastCh == '\r' && lookAhead == '\n') {
            out.write(lookAhead);
            lookAhead = in.read();
        }

        return lookAhead;
    }

    static byte[] getLineSeparator() {
        final String nl = System.getProperty("line.separator");
        byte[] nlBytes = new byte[nl.length()];

        for (int i = 0; i != nlBytes.length; i++) {
            nlBytes[i] = (byte) nl.charAt(i);
        }

        return nlBytes;
    }

    static int getLengthWithoutSeparatorOrTrailingWhitespace(final byte[] line) {
        int end = line.length - 1;

        while (end >= 0 && isWhiteSpace(line[end])) {
            end--;
        }

        return end + 1;
    }

    static boolean isLineEnding(final byte b) {
        return b == '\r' || b == '\n';
    }

    static int getLengthWithoutWhiteSpace(final byte[] line) {
        int end = line.length - 1;

        while (end >= 0 && isWhiteSpace(line[end])) {
            end--;
        }

        return end + 1;
    }

    static boolean isWhiteSpace(byte b) {
        return isLineEnding(b) || b == '\t' || b == ' ';
    }

    static void processLine(final PGPSignature sig, final byte[] line) throws SignatureException, IOException {
        final int length = getLengthWithoutWhiteSpace(line);
        if (length > 0) {
            sig.update(line, 0, length);
        }
    }

    // Convert line endings to the canonical <CR><LF> sequence before the signature can be verified. (Ref. RFC2015).
    static byte[] canonicalise(final byte[] content) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        for (byte ch : content) {
            if (ch == '\r') {
                continue;
            }
            if (ch == '\n') {
                out.write('\r');
                out.write('\n');
            }
            else {
                out.write(ch);
            }
        }

        return out.toByteArray();
    }
}
