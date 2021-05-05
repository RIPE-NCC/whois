package net.ripe.db.whois.update.keycert;

import com.google.common.base.Splitter;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import static net.ripe.db.whois.common.rpsl.AttributeType.CERTIF;
import static net.ripe.db.whois.update.keycert.X509CertificateWrapper.X509_FOOTER;
import static net.ripe.db.whois.update.keycert.X509CertificateWrapper.X509_HEADER;

public class X509CertificateTestUtil {

    public static RpslObject createKeycertObject(final CIString key,
                                                 final X509Certificate x509,
                                                 final CIString mntner) {
        final String base64;
        try {
            base64 = Base64.encodeBase64String(x509.getEncoded());
        } catch (CertificateEncodingException e) {
            throw new RuntimeException(e);
        }

        final RpslObjectBuilder builder = new RpslObjectBuilder();

        builder.append(new RpslAttribute(AttributeType.KEY_CERT, key));
        builder.append(new RpslAttribute(AttributeType.METHOD, "X509"));

        builder.append(new RpslAttribute(CERTIF, X509_HEADER));
        Splitter.fixedLength(104 - 40).split(base64).forEach(certif -> builder.append(new RpslAttribute(CERTIF, certif.replaceAll(" ", "").replaceAll("\n", ""))));
        builder.append(new RpslAttribute(CERTIF, X509_FOOTER));

        X509CertificateWrapper keyWrapper = X509CertificateWrapper.parse(builder.get());

        keyWrapper.getOwners().forEach(owner -> builder.addAttributeSorted(new RpslAttribute(AttributeType.OWNER, owner)));
        builder.addAttributeSorted(new RpslAttribute(AttributeType.FINGERPR, keyWrapper.getFingerprint()));

        builder.addAttributeSorted(new RpslAttribute(AttributeType.MNT_BY, mntner));
        builder.addAttributeSorted(new RpslAttribute(AttributeType.SOURCE, "TEST"));

        return builder.get();
    }

    public static X509Certificate generate(final String cn,
                                           final DateTimeProvider dateTimeProvider) throws OperatorCreationException, CertificateException, NoSuchAlgorithmException {

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        final Instant now = dateTimeProvider.getCurrentZonedDateTime().minusDays(1).toInstant();
        final Date notBefore = Date.from(now);
        final Date notAfter = Date.from(now.plus(Duration.ofDays(365)));

        final ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSA").build(keyPair.getPrivate());
        final X500Name x500Name = new X500Name("CN=" + cn);
        final X509v3CertificateBuilder certificateBuilder =
                new JcaX509v3CertificateBuilder(x500Name,
                        BigInteger.valueOf(now.toEpochMilli()),
                        notBefore,
                        notAfter,
                        x500Name,
                        keyPair.getPublic());

        return new JcaX509CertificateConverter()
                .setProvider(new BouncyCastleProvider()).getCertificate(certificateBuilder.build(contentSigner));
    }

    public static String asPem(final X509Certificate certificate) throws CertificateEncodingException {
        // provide pem cert with spaces instead of line breaks like Apache does:
        return X509_HEADER +
                " " +
                Base64.encodeBase64URLSafeString(certificate.getEncoded()) +
                " " +
                X509_FOOTER;
    }

}
