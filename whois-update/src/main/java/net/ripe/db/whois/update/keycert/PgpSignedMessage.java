package net.ripe.db.whois.update.keycert;

import com.google.common.base.Charsets;
import net.ripe.db.whois.common.DateTimeProvider;
import org.bouncycastle.bcpg.ArmoredInputStream;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureList;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.bc.BcPGPObjectFactory;
import org.bouncycastle.openpgp.operator.bc.BcPGPContentVerifierBuilderProvider;
import org.joda.time.LocalDateTime;
import org.springframework.util.FileCopyUtils;

import javax.annotation.concurrent.Immutable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.ripe.db.whois.update.keycert.PgpSignedMessageUtil.canonicalise;
import static net.ripe.db.whois.update.keycert.PgpSignedMessageUtil.getLengthWithoutSeparatorOrTrailingWhitespace;
import static net.ripe.db.whois.update.keycert.PgpSignedMessageUtil.getLineSeparator;
import static net.ripe.db.whois.update.keycert.PgpSignedMessageUtil.processLine;
import static net.ripe.db.whois.update.keycert.PgpSignedMessageUtil.readInputLine;

@Immutable
public final class PgpSignedMessage {

    public static final Pattern SIGNED_MESSAGE_PATTERN = Pattern.compile("(?ms)"
            + "-----BEGIN PGP SIGNED MESSAGE-----"
            + "\n.*?"
            + "\n-----BEGIN PGP SIGNATURE-----"
            + "\n.*?"
            + "\n-----END PGP SIGNATURE-----(\n?)");

    private final byte[] content;
    private final byte[] signature;
    private final boolean clearText;

    @SuppressWarnings("PMD.ArrayIsStoredDirectly")
    // Constructor only used internally, arrays are not modified externally
    private PgpSignedMessage(final byte[] content, final byte[] signature, final boolean clearText) {
        this.content = content;
        this.signature = signature;
        this.clearText = clearText;
    }

    public static PgpSignedMessage parse(final String signedContent, final String signature) {
        return parse(signedContent, signature, Charsets.ISO_8859_1);
    }

    public static PgpSignedMessage parse(final String signedContent, final String signature, final Charset charset) {
        try {
            final byte[] content = canonicalise(signedContent.getBytes());
            final ByteArrayInputStream signatureIn = new ByteArrayInputStream(signature.getBytes(charset));
            final InputStream decoderStream = PGPUtil.getDecoderStream(signatureIn);
            if (decoderStream instanceof ArmoredInputStream) {
                final ArmoredInputStream armoredInputStream = (ArmoredInputStream) decoderStream;
                while (true) {
                    if (!(armoredInputStream.isClearText() && armoredInputStream.read() != -1)) {
                        break;
                    }
                }
            }

            final byte[] signatureBytes = FileCopyUtils.copyToByteArray(signatureIn);
            return new PgpSignedMessage(content, signatureBytes, false);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static PgpSignedMessage parse(final String clearText) {
        final Matcher matcher = SIGNED_MESSAGE_PATTERN.matcher(clearText);
        if (matcher.find()) {
            return parse(matcher.group(0).getBytes());
        } else {
            throw new IllegalArgumentException("no signed message found");
        }
    }

    public static PgpSignedMessage parse(final String clearText, final Charset charset) {
        final Matcher matcher = SIGNED_MESSAGE_PATTERN.matcher(clearText);
        if (matcher.find()) {
            return parse(charset.encode(matcher.group(0)).array());
        } else {
            throw new IllegalArgumentException("no signed message found");
        }
    }

    private static PgpSignedMessage parse(final byte[] bytes) {
        try {
            final InputStream decoderStream = PGPUtil.getDecoderStream(new ByteArrayInputStream(bytes));     // encodeAsLatin1(matcher.group(0))
            if (!(decoderStream instanceof ArmoredInputStream)) {
                throw new IllegalArgumentException("Unexpected content");
            }

            final ArmoredInputStream in = (ArmoredInputStream) decoderStream;
            final ByteArrayOutputStream signedSectionOut = new ByteArrayOutputStream();

            // write out signed section using the local line separator.
            // note: trailing white space needs to be removed from the end of
            // each line RFC 4880 Section 7.1
            ByteArrayOutputStream lineOut = new ByteArrayOutputStream();
            int lookAhead = readInputLine(lineOut, in);
            byte[] lineSep = getLineSeparator();

            if (lookAhead != -1 && in.isClearText()) {
                byte[] line = lineOut.toByteArray();
                signedSectionOut.write(line, 0, getLengthWithoutSeparatorOrTrailingWhitespace(line));
                signedSectionOut.write(lineSep);

                while (lookAhead != -1 && in.isClearText()) {
                    lookAhead = readInputLine(lineOut, lookAhead, in);

                    line = lineOut.toByteArray();
                    signedSectionOut.write(line, 0, getLengthWithoutSeparatorOrTrailingWhitespace(line));
                    signedSectionOut.write(lineSep);
                }
            }

            signedSectionOut.close();

            final ByteArrayOutputStream signatureOut = new ByteArrayOutputStream();
            FileCopyUtils.copy(in, signatureOut);

            return new PgpSignedMessage(signedSectionOut.toByteArray(), signatureOut.toByteArray(), true);

        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public boolean verify(final PGPPublicKey publicKey) {
        try {
            PGPSignature pgpSignature = getPgpSignature();
            if (pgpSignature.getKeyAlgorithm() != publicKey.getAlgorithm()) {
                return false;
            }

            pgpSignature.init(new BcPGPContentVerifierBuilderProvider(), publicKey);

            if (clearText) {
                // read the input, making sure we ignore the last newline.
                final InputStream signatureIn = new ByteArrayInputStream(content);
                final ByteArrayOutputStream lineOut = new ByteArrayOutputStream();
                int lookAhead = readInputLine(lineOut, signatureIn);

                processLine(pgpSignature, lineOut.toByteArray());

                if (lookAhead != -1) {
                    do {
                        lookAhead = readInputLine(lineOut, lookAhead, signatureIn);

                        pgpSignature.update((byte) '\r');
                        pgpSignature.update((byte) '\n');

                        processLine(pgpSignature, lineOut.toByteArray());
                    }
                    while (lookAhead != -1);
                }

                signatureIn.close();
            } else {
                pgpSignature.update(content);
            }

            return pgpSignature.verify();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public boolean verifySigningTime(final DateTimeProvider dateTimeProvider) {
        final LocalDateTime signingTime = new LocalDateTime(getPgpSignature().getCreationTime());
        final LocalDateTime oneWeekAgo = dateTimeProvider.getCurrentDateTime().minusDays(7);
        return !signingTime.isBefore(oneWeekAgo);
    }

    public String getKeyId() {
        StringBuilder builder = new StringBuilder();

        byte[] keyId = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(getPgpSignature().getKeyID()).array();
        for (int n = keyId.length - 4; n < keyId.length; n++) {
            builder.append(String.format("%02X", keyId[n]));
        }

        return builder.toString();
    }

    public String getSignedContent() {
        return new String(content);
    }

    private PGPSignature getPgpSignature() {
        try {
            final InputStream decoderStream = PGPUtil.getDecoderStream(new ByteArrayInputStream(signature));
            final PGPObjectFactory objectFactory = new BcPGPObjectFactory(decoderStream);

            final PGPSignatureList signatureList = (PGPSignatureList) objectFactory.nextObject();
            if ((signatureList == null) || (signatureList.size() != 1)) {
                throw new IllegalArgumentException("Couldn't read PGP signature");
            }

            return signatureList.get(0);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final PgpSignedMessage that = (PgpSignedMessage) o;
        return clearText == that.clearText && Arrays.equals(content, that.content) && Arrays.equals(signature, that.signature);

    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(content);
        result = 31 * result + Arrays.hashCode(signature);
        result = 31 * result + (clearText ? 1 : 0);
        return result;
    }
}
