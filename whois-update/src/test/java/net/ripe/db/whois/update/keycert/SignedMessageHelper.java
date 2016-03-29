package net.ripe.db.whois.update.keycert;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.BCPGOutputStream;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.bc.BcPGPObjectFactory;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.Iterator;

public class SignedMessageHelper {


    /**
     * Sign content with a PGP signature.
     *
     * @param secretKey secret key to sign with
     * @param password for secret key
     * @param content text to sign - line endings are always converted to canonical form
     * @return signature
     * @throws GeneralSecurityException
     * @throws IOException
     * @throws PGPException
     */
    public static String createSignature(final PGPSecretKey secretKey, final String password, final String content)
        throws GeneralSecurityException, IOException, PGPException
    {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        final OutputStream outputStream = new ArmoredOutputStream(baos);
        try {
            final PGPPrivateKey privateKey = secretKey.extractPrivateKey(new JcePBESecretKeyDecryptorBuilder().setProvider("BC").build(password.toCharArray()));
            final PGPSignatureGenerator generator = new PGPSignatureGenerator(new JcaPGPContentSignerBuilder(secretKey.getPublicKey().getAlgorithm(), PGPUtil.SHA1).setProvider("BC"));
            generator.init(PGPSignature.BINARY_DOCUMENT, privateKey);

            final InputStream inputStream = new ByteArrayInputStream(convertNewlines(content).getBytes());
            try {
                int ch;
                while ((ch = inputStream.read()) >= 0)
                {
                    generator.update((byte)ch);
                }
            } finally {
                IOUtils.closeQuietly(inputStream);
            }

            generator.generate().encode(new BCPGOutputStream(outputStream));
        } finally {
            IOUtils.closeQuietly(outputStream);
        }

        return new String(baos.toByteArray());
    }

    public static PGPSecretKey findSecretKey(final byte[] bytes, final String keyId) {
        final Iterator outer = parseKeys(bytes);
        while (outer.hasNext()) {
            final Object next = outer.next();
            if (next instanceof PGPSecretKeyRing) {
                for (PGPSecretKey secretKey : (PGPSecretKeyRing)next) {
                    if (secretKey.isMasterKey() &&
                        secretKey.isSigningKey() &&
                        getKeyId(secretKey.getKeyID()).equals(keyId)) {
                            return secretKey;
                    }
                }
            }
        }

        throw new IllegalArgumentException(keyId + " not found");
    }

    public static PGPPublicKey findPublicKey(final byte[] bytes, final String keyId) {
        final Iterator outer = parseKeys(bytes);
        while (outer.hasNext()) {
            final Object next = outer.next();
            if (next instanceof PGPPublicKeyRing) {
                for (PGPPublicKey publicKey : (PGPPublicKeyRing)next) {
                    if (publicKey.isMasterKey() &&
                        getKeyId(publicKey.getKeyID()).equals(keyId)) {
                            return publicKey;
                    }
                }
            }
        }

        throw new IllegalArgumentException(keyId + " not found");
    }

    // a signature is calculated on the text using canonical <CR><LF> line endings (RFC2440)
    private static String convertNewlines(final String content) {
        return content.replace("\n", "\r\n");
    }

    private static Iterator parseKeys(final byte[] bytes) {
        try {
            return new BcPGPObjectFactory(PGPUtil.getDecoderStream(new ByteArrayInputStream(bytes))).iterator();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static String getKeyId(final long input) {
        final StringBuilder builder = new StringBuilder();

        final byte[] keyId = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(input).array();
        for (int n = keyId.length - 4; n < keyId.length; n++) {
            builder.append(String.format("%02X", keyId[n]));
        }

        return builder.toString();
    }

}
