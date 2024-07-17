package net.ripe.db.nrtm4.generator;

import org.mariadb.jdbc.internal.logging.Logger;
import org.mariadb.jdbc.internal.logging.LoggerFactory;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.Ed25519Signer;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.jwk.gen.OctetKeyPairGenerator;

@Service
public class JWSKeyPairService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JWSKeyPairService.class);
    final OctetKeyPair jwk;

    public JWSKeyPairService() throws JOSEException {
        this.jwk = new OctetKeyPairGenerator(Curve.Ed25519).generate();
    }

    public OctetKeyPair getPublicJwk() {
        return jwk.toPublicJWK();
    }

    public String getJWSSignedPayload(final String payload)  {
        try {

            // Create the EdDSA signer
            JWSSigner signer = new Ed25519Signer(this.jwk);

            // Creates the JWS object with payload
            JWSObject jwsObject = new JWSObject(
                    new JWSHeader.Builder(JWSAlgorithm.EdDSA)  //.base64URLEncodePayload(true)
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
