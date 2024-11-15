package net.ripe.db.nrtm4.util;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.Ed25519Signer;
import com.nimbusds.jose.crypto.Ed25519Verifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.OctetKeyPair;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;

public class JWSUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(JWSUtil.class);

    public static String signWithJWS(final String payload, final byte[] privateKey)  {
        try {
            final OctetKeyPair jwk = OctetKeyPair.parse(new String(privateKey));
            final JWSSigner signer = new Ed25519Signer(jwk);

            final JWSObject jwsObject = new JWSObject(
                    new JWSHeader.Builder(JWSAlgorithm.Ed25519).keyID(jwk.getKeyID()).build(),
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
            final OctetKeyPair parsedPublicKey =  OctetKeyPair.parse(getPublicKey(publicKey));

            final JWSVerifier verifier = new Ed25519Verifier(parsedPublicKey);
            return jwsObjectParsed.verify(verifier);
        } catch (JOSEException | ParseException ex) {
            LOGGER.error("failed to verify signature {}", ex.getMessage());
            throw new IllegalStateException("failed to sign contents of file");
        }
    }

    public static String getPublicKey(final byte[] publicKey) {
        return new String(publicKey);
    }
}
