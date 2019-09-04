package net.ripe.db.whois.update.keycert;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.DateUtil;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.CMSAttributes;
import org.bouncycastle.asn1.cms.Time;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessable;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignerDigestMismatchException;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.Store;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Objects;

public class X509SignedMessage {

    private static final Provider PROVIDER = new BouncyCastleProvider();

    private static final Logger LOGGER = LoggerFactory.getLogger(X509SignedMessage.class);

    private final String signedData;
    private final String signature;

    public X509SignedMessage(final String signedData, final String signature) {
        this.signedData = signedData;
        this.signature = signature;
    }

    public boolean verify(final X509Certificate certificate) {
        try {
            CMSProcessable cmsProcessable = new CMSProcessableByteArray(signedData.getBytes());

            CMSSignedData signedData = new CMSSignedData(cmsProcessable, Base64.decode(signature));

            Iterator signersIterator = signedData.getSignerInfos().getSigners().iterator();
            if (signersIterator.hasNext()) {
                SignerInformation signerInformation = (SignerInformation) signersIterator.next();

                LOGGER.debug("Message is signed by certificate: {}", getFingerprint(signerInformation, signedData.getCertificates()));

                JcaSimpleSignerInfoVerifierBuilder verifierBuilder = new JcaSimpleSignerInfoVerifierBuilder();
                verifierBuilder.setProvider(PROVIDER);
                return signerInformation.verify(verifierBuilder.build(certificate.getPublicKey()));
            }
        } catch (CMSSignerDigestMismatchException e) {
            throw new IllegalArgumentException("message-digest attribute value does not match calculated value", e);
        } catch (CMSException e) {
            throw new IllegalArgumentException("general exception verifying signed message", e);
        } catch (OperatorCreationException e) {
            throw new IllegalArgumentException("error creating verifier", e);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }

        return false;
    }

    private String getFingerprint(SignerInformation signerInformation, Store store) {
        try {
            Iterator certificateIterator = store.getMatches(signerInformation.getSID()).iterator();
            if (certificateIterator.hasNext()) {
                X509CertificateHolder certHolder = (X509CertificateHolder) certificateIterator.next();
                return getDigest(certHolder.getEncoded());
            }
            throw new IllegalArgumentException("No certificate found");
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot get certificate fingerprint", e);
        }
    }

    private String getDigest(final byte[] encoded) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(encoded);

        StringBuilder builder = new StringBuilder();
        for (byte next : md.digest()) {
            if (builder.length() > 0) {
                builder.append(':');
            }
            builder.append(String.format("%02X", next));
        }

        return builder.toString();
    }

    // The signing time must be within an hour of the current time.
    public boolean verifySigningTime(final DateTimeProvider dateTimeProvider) {
        final LocalDateTime signingTime = getSigningTime();
        if (signingTime == null) {
            return true;
        }

        final LocalDateTime currentTime = dateTimeProvider.getCurrentDateTime();
        return (signingTime.isAfter(currentTime.minusHours(1)) && signingTime.isBefore(currentTime.plusHours(1)));
    }

    @Nullable
    private LocalDateTime getSigningTime() {
        try {
            CMSProcessable cmsProcessable = new CMSProcessableByteArray(signedData.getBytes());

            CMSSignedData signedData = new CMSSignedData(cmsProcessable, Base64.decode(signature));

            Iterator signersIterator = signedData.getSignerInfos().getSigners().iterator();
            if (signersIterator.hasNext()) {
                SignerInformation signerInformation = (SignerInformation) signersIterator.next();
                return getSigningTime(signerInformation);
            }
        } catch (CMSException e) {
            LOGGER.debug("Not returning signing time", e);
        }

        return null;
    }

    @Nullable
    private LocalDateTime getSigningTime(final SignerInformation signerInformation) {
        final AttributeTable signedAttributes = signerInformation.getSignedAttributes();
        if (signedAttributes != null) {
            final ASN1EncodableVector signingTimeAttributes = signedAttributes.getAll(CMSAttributes.signingTime);
            if (signingTimeAttributes.size() == 1) {
                final ASN1Set attributeValues = ((Attribute) signingTimeAttributes.get(0)).getAttrValues();
                if (attributeValues.size() == 1) {
                    return DateUtil.fromDate(Time.getInstance(attributeValues.getObjectAt(0).toASN1Primitive()).getDate());
                }
            }
        }
        return null;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final X509SignedMessage that = (X509SignedMessage) o;

        return Objects.equals(signature, that.signature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(signature);
    }
}
