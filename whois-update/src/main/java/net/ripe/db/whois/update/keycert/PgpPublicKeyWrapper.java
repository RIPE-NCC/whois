package net.ripe.db.whois.update.keycert;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectFilter;
import org.bouncycastle.bcpg.ArmoredInputStream;
import org.bouncycastle.bcpg.SignatureSubpacketTags;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;
import org.joda.time.LocalDateTime;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.Provider;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class PgpPublicKeyWrapper implements KeyWrapper {
    private static final String PGP_HEADER = "-----BEGIN PGP PUBLIC KEY BLOCK-----";
    private static final String PGP_FOOTER = "-----END PGP PUBLIC KEY BLOCK-----";
    private static final String METHOD = "PGP";

    private static final Provider PROVIDER = new BouncyCastleProvider();

    private final PGPPublicKey masterKey;

    private final List<PGPPublicKey> subKeys;

    public PgpPublicKeyWrapper(final PGPPublicKey masterKey, final List<PGPPublicKey> subKeys) {
        this.masterKey = masterKey;
        this.subKeys = subKeys;
    }

    public static PgpPublicKeyWrapper parse(final RpslObject object) {
        if (!looksLikePgpKey(object)) {
            throw new IllegalArgumentException("The supplied object has no key");
        }

        try {
            final byte[] bytes = new RpslObjectFilter(object).getCertificateFromKeyCert().getBytes(Charsets.ISO_8859_1);
            final ArmoredInputStream armoredInputStream = (ArmoredInputStream) PGPUtil.getDecoderStream(new ByteArrayInputStream(bytes));
            PGPPublicKey masterKey = null;
            List<PGPPublicKey> subKeys = Lists.newArrayList();

            @SuppressWarnings("unchecked")
            final Iterator<PGPPublicKeyRing> keyRingsIterator = new PGPPublicKeyRingCollection(armoredInputStream).getKeyRings();
            while (keyRingsIterator.hasNext()) {
                @SuppressWarnings("unchecked")
                final Iterator<PGPPublicKey> keyIterator = keyRingsIterator.next().getPublicKeys();
                while (keyIterator.hasNext()) {
                    final PGPPublicKey key = keyIterator.next();
                    if (key.isMasterKey()) {
                        if (masterKey == null) {
                            if (key.isRevoked()) {
                                throw new IllegalArgumentException("The supplied key is revoked");
                            }

                            masterKey = key;
                        } else {
                            throw new IllegalArgumentException("The supplied object has multiple keys");
                        }
                    } else {
                        if (masterKey == null) {
                            continue;
                        }

                        if (key.isRevoked()) {
                            continue;
                        }

                        // RFC 4880: verify subkey binding signature issued by the top-level key
                        final Iterator<PGPSignature> signatureIterator = key.getSignaturesOfType(PGPSignature.SUBKEY_BINDING);
                        while (signatureIterator.hasNext()) {
                            final PGPSignature signature = signatureIterator.next();

                            if (!hasFlag(signature, PGPKeyFlags.CAN_SIGN)) {
                                // cannot sign with this subkey, skip it
                                continue;
                            }

                            JcaPGPContentVerifierBuilderProvider provider = new JcaPGPContentVerifierBuilderProvider().setProvider(PROVIDER);
                            signature.init(provider, masterKey);
                            try {
                                if (signature.verifyCertification(masterKey, key)) {
                                    subKeys.add(key);
                                }
                            } catch (SignatureException ignored) {
                            }
                        }
                    }
                }
            }

            if (masterKey == null) {
                throw new IllegalArgumentException("The supplied object has no key");
            }

            return new PgpPublicKeyWrapper(masterKey, subKeys);
        } catch (IOException e) {
            throw new IllegalArgumentException("The supplied object has no key");
        } catch (PGPException e) {
            throw new IllegalArgumentException("The supplied object has no key");
        }
    }

    static boolean hasFlag(final PGPSignature signature, final int flag) {
        if (signature.hasSubpackets()) {
            PGPSignatureSubpacketVector subpacketVector = signature.getHashedSubPackets();
            if (subpacketVector.hasSubpacket(SignatureSubpacketTags.KEY_FLAGS)) {
                if ((subpacketVector.getKeyFlags() & flag) > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    static boolean looksLikePgpKey(final RpslObject rpslObject) {
        final String pgpKey = new RpslObjectFilter(rpslObject).getCertificateFromKeyCert();
        return pgpKey.indexOf(PGP_HEADER) != -1 && pgpKey.indexOf(PGP_FOOTER) != -1;
    }

    public PGPPublicKey getPublicKey() {
        return masterKey;
    }

    public List<PGPPublicKey> getSubKeys() {
        return subKeys;
    }

    @Override
    public String getMethod() {
        return METHOD;
    }

    @Override
    public String getOwner() {
        String uid = null;

        Iterator iterator = masterKey.getUserIDs();
        while (iterator.hasNext()) {
            uid = iterator.next().toString(); // return last uid found
        }

        return uid;
    }

    @Override
    public String getFingerprint() {
        StringBuilder builder = new StringBuilder();
        byte[] fingerprint = masterKey.getFingerprint();
        for (int n = 0; n < fingerprint.length; n++) {
            if ((n > 0) && (n % 2) == 0) {
                builder.append(' ');
            }
            if (n == 10) {
                // We add the double space in the middle to be consistent with gpg output
                builder.append(' ');
            }
            builder.append(String.format("%02X", fingerprint[n]));
        }
        return builder.toString();
    }

    public boolean isExpired(final DateTimeProvider dateTimeProvider) {
        final int validDays = masterKey.getValidDays();
        if (validDays > 0) {
            final LocalDateTime expired = (new LocalDateTime(masterKey.getCreationTime())).plusDays(validDays);
            return expired.isBefore(dateTimeProvider.getCurrentDateTime());
        }

        return false;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        try {
            return Arrays.equals(
                    ((PgpPublicKeyWrapper) o).getPublicKey().getEncoded(),
                    this.getPublicKey().getEncoded()) &&
                    ((PgpPublicKeyWrapper) o).getSubKeys().equals(this.getSubKeys());
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int result = masterKey.hashCode();
        result = 31 * result + subKeys.hashCode();
        return result;
    }
}
