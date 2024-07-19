package net.ripe.db.nrtm4.generator;

import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import org.mariadb.jdbc.internal.logging.Logger;
import org.mariadb.jdbc.internal.logging.LoggerFactory;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.jwk.Curve;

import java.io.ByteArrayInputStream;

@Service
public class JWSKeyPairService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JWSKeyPairService.class);
    final ECKey ecJWK;

    public JWSKeyPairService() throws JOSEException {
        this.ecJWK = new ECKeyGenerator(Curve.P_256).generate();
    }

    public ECKey getPublicJwk() {
        return ecJWK.toPublicJWK();
    }

    public String getJWSSignedPayload(final String payload)  {
        try {

            // Create the EdDSA signer
            JWSSigner signer = new ECDSASigner(ecJWK);

            // Creates the JWS object with payload
            JWSObject jwsObject = new JWSObject(
                    new JWSHeader.Builder(JWSAlgorithm.ES256)  //.base64URLEncodePayload(true)
                            .build(),
                    new Payload(payload));

            // Compute the EdDSA signature
            jwsObject.sign(signer);

            // Serialize the JWS to compact form
            return jwsObject.serialize();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public Object getJWSSignedPayload(final byte[] payload) {
        try {
            // Create the EdDSA signer
            JWSSigner signer = new ECDSASigner(ecJWK);

            // Creates the JWS object with payload
            JWSObject jwsObject = new JWSObject(
                    new JWSHeader.Builder(JWSAlgorithm.ES256)
                            .build(),
                    new Payload(payload));

            // Compute the EdDSA signature
            jwsObject.sign(signer);

            // Serialize the JWS to compact form
            return jwsObject.serialize();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
