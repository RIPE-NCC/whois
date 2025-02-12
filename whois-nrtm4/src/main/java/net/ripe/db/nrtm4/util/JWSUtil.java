package net.ripe.db.nrtm4.util;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemObjectGenerator;
import org.bouncycastle.util.io.pem.PemWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.text.ParseException;

public class JWSUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(JWSUtil.class);

    public static String signWithJWS(final String payload, final byte[] privateKey)  {
        try {
            final ECKey jwk = ECKey.parse(new String(privateKey));
            final JWSSigner signer = new ECDSASigner(jwk);

            final JWSObject jwsObject = new JWSObject(
                    new JWSHeader.Builder(JWSAlgorithm.ES256).keyID(jwk.getKeyID()).build(),
                    new Payload(payload));

            jwsObject.sign(signer);

            return jwsObject.serialize();
        } catch (ParseException | JOSEException ex) {
            LOGGER.error("failed to sign payload {}", ex.getMessage());
            throw new IllegalStateException("failed to sign contents of file");
        }
    }

    public static boolean verifySignature(final JWSObject jwsObjectParsed, final String pemFormat) {
        try {
            final ECKey parsedPublicKey = (ECKey) JWK.parseFromPEMEncodedObjects(pemFormat);

            final JWSVerifier verifier = new ECDSAVerifier(parsedPublicKey);
            return jwsObjectParsed.verify(verifier);
        } catch (JOSEException ex) {
            LOGGER.error("failed to verify signature {}", ex.getMessage());
            throw new IllegalStateException("failed to sign contents of file");
        }
    }

    public static String publicKeyInPemFormat(final ECKey ecKey) {
        try {
           final StringWriter stringWriter = new StringWriter();
           final PemWriter writer = new PemWriter(stringWriter);
           final PemObjectGenerator pemObject = new PemObject("PUBLIC KEY", ecKey.toPublicJWK().toECPublicKey().getEncoded());
           writer.writeObject(pemObject);
           writer.close();

           return stringWriter.toString();

        } catch (IOException | JOSEException ex) {
            LOGGER.error("failed to generate publickey in Pem format {}", ex.getMessage());
            return null;
        }
    }
 }
