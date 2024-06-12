package net.ripe.db.whois.update.keycert;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.DateUtil;
import net.ripe.db.whois.common.Latin1Conversion;
import net.ripe.db.whois.common.x509.KeyWrapper;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectFilter;
import org.bouncycastle.bcpg.ArmoredInputStream;
import org.bouncycastle.bcpg.SignatureSubpacketTags;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPKeyFlags;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureSubpacketVector;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.bc.BcPGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Provider;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class PgpPublicKeyWrapper implements KeyWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(PgpPublicKeyWrapper.class);

    private static final String PGP_HEADER = "-----BEGIN PGP PUBLIC KEY BLOCK-----";
    private static final String PGP_FOOTER = "-----END PGP PUBLIC KEY BLOCK-----";
    private static final String METHOD = "PGP";

    private static final Long SECONDS_IN_ONE_DAY = 60L * 60L * 24L;

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
            final byte[] bytes = RpslObjectFilter.getCertificateFromKeyCert(object).getBytes(StandardCharsets.ISO_8859_1);
            final ArmoredInputStream armoredInputStream = (ArmoredInputStream) PGPUtil.getDecoderStream(new ByteArrayInputStream(bytes));
            PGPPublicKey masterKey = null;
            List<PGPPublicKey> subKeys = Lists.newArrayList();

            @SuppressWarnings("unchecked")
            final Iterator<PGPPublicKeyRing> keyRingsIterator = new BcPGPPublicKeyRingCollection(armoredInputStream).getKeyRings();
            while (keyRingsIterator.hasNext()) {
                @SuppressWarnings("unchecked")
                final Iterator<PGPPublicKey> keyIterator = keyRingsIterator.next().getPublicKeys();
                while (keyIterator.hasNext()) {
                    final PGPPublicKey key = keyIterator.next();
                    if (key.isMasterKey()) {
                        if (masterKey == null) {
                            masterKey = key;
                        } else {
                            throw new IllegalArgumentException("The supplied object has multiple keys");
                        }
                    } else {
                        if (masterKey == null) {
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
                            } catch (PGPException e) {
                                throw new IllegalStateException(e);
                            }
                        }
                    }
                }
            }

            if (masterKey == null) {
                throw new IllegalArgumentException("The supplied object has no key");
            }

            return new PgpPublicKeyWrapper(masterKey, subKeys);
        } catch (IOException | PGPException e) {
            throw new IllegalArgumentException("The supplied object has no key");
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.warn("Unexpected error, throwing no key by default", e);
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

    public static boolean looksLikePgpKey(final RpslObject rpslObject) {
        final String pgpKey = RpslObjectFilter.getCertificateFromKeyCert(rpslObject);
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
    public List<String> getOwners() {
        return Lists.newArrayList(filterRevokedUserIds(transformUserIdsToLatin1()));
    }

    private Iterator<String> transformUserIdsToLatin1() {
        try {
            return Iterators.transform(masterKey.getUserIDs(), Latin1Conversion::convertString);
        } catch (IllegalArgumentException e) {
            // Invalid UTF-8 input
            return Iterators.transform(masterKey.getRawUserIDs(), bytes -> Latin1Conversion.convertString(new String(bytes)));
        }
    }

    private Iterator<String> filterRevokedUserIds(final Iterator<String> userIds) {
        return Iterators.filter(userIds, userId -> {
                    final Iterator<PGPSignature> signatures = masterKey.getSignaturesForID(userId);
                    while ((signatures != null) && signatures.hasNext()) {
                        if (signatures.next().getSignatureType() == PGPSignature.CERTIFICATION_REVOCATION) {
                            // remove revoked user id
                            return false;
                        }
                    }
                    return true;
                });
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

    public String getKeyId() {
        final long value = masterKey.getKeyID();
        return String.format("%02X%02X%02X%02X",
            ((value >> 24) & 0xFF),
            ((value >> 16) & 0xFF),
            ((value >> 8) & 0xFF),
            ((value >> 0) & 0xFF));
    }

    public boolean isExpired(final DateTimeProvider dateTimeProvider) {
        final long validSeconds = masterKey.getValidSeconds();
        if (validSeconds > 0) {
            final int days = Long.valueOf(Long.divideUnsigned(validSeconds, SECONDS_IN_ONE_DAY)).intValue();
            final LocalDateTime expired = (DateUtil.fromDate(masterKey.getCreationTime())).plusDays(days);
            return expired.isBefore(dateTimeProvider.getCurrentDateTime());
        }

        return false;
    }

    public boolean isRevoked() {
        return masterKey.hasRevocation();
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
        return Objects.hash(masterKey, subKeys);
    }
}
