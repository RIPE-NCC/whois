package net.ripe.db.whois.update.keycert;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
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
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import static net.ripe.db.whois.common.rpsl.AttributeType.CERTIF;

public class X509CertificateTestUtil {

    public static RpslObject createKeycertObject(final CIString key,
                                                 final X509Certificate x509,
                                                 final CIString mntner) {
        final RpslObjectBuilder builder = new RpslObjectBuilder();

        builder.append(new RpslAttribute(AttributeType.KEY_CERT, key));
        builder.append(new RpslAttribute(AttributeType.METHOD, "X509"));

        final X509CertificateWrapper x509Wrapper = new X509CertificateWrapper(x509);

        x509Wrapper.getCertificateAsString().lines().forEach(line -> {
            builder.append(new RpslAttribute(CERTIF, line));
        });

        x509Wrapper.getOwners().forEach(owner -> builder.addAttributeSorted(new RpslAttribute(AttributeType.OWNER, owner)));
        builder.addAttributeSorted(new RpslAttribute(AttributeType.FINGERPR, x509Wrapper.getFingerprint()));

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

    public static String asPem(final X509Certificate certificate) {
        return new X509CertificateWrapper(certificate).getCertificateAsString();
    }

}
