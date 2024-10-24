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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;

public class JWSUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(JWSUtil.class);

    public static String signWithJWS(final String payload, final byte[] privateKey)  {
        try {
            final ECKey ecJWK = ECKey.parse(new String(privateKey));
            final JWSSigner signer = new ECDSASigner(ecJWK);

            final JWSObject jwsObject = new JWSObject(
                    new JWSHeader.Builder(JWSAlgorithm.ES256).keyID(ecJWK.getKeyID()).build(),
                    new Payload(payload));

            jwsObject.sign(signer);

            return jwsObject.serialize();
        } catch (ParseException | JOSEException ex) {
            LOGGER.error("failed to sign payload {}", ex.getMessage());
            throw new IllegalStateException("failed to sign contents of file");
        }
    }

    public static boolean verifySignature(final JWSObject jwsObjectParsed, final byte[] publicKey) {

        try {
            final ECKey parsedFromPemPublicKey =  JWK.parseFromPEMEncodedObjects(getPublicKey(publicKey)).toECKey();

            final JWSVerifier verifier = new ECDSAVerifier(parsedFromPemPublicKey);
            return jwsObjectParsed.verify(verifier);
        } catch (JOSEException ex) {
            LOGGER.error("failed to verify signature {}", ex.getMessage());
            throw new IllegalStateException("failed to sign contents of file");
        }
    }

    public static String getPublicKey(final byte[] publicKey) {
        return new String(publicKey);
    }
}
