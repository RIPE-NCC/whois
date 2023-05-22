package net.ripe.db.nrtm4.util;

import net.ripe.db.nrtm4.UpdateNotificationFileGenerator;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator;
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Base64;

public class Ed25519Util {
    private static final Logger LOGGER = LoggerFactory.getLogger(Ed25519Util.class);

    public static String signWithEd25519(final byte[] payload, final byte[] privateKey)  {
        try {
            final Signer signer = new Ed25519Signer();
            signer.init(true, new Ed25519PrivateKeyParameters(privateKey, 0));
            signer.update(payload, 0, payload.length);
            byte[] signature = signer.generateSignature();
            return Hex.toHexString(signature);
        } catch (CryptoException ex) {
            LOGGER.error("failed to sign payload {}", ex.getMessage());
            throw new RuntimeException("failed to sign contents of file");
        }
    }

    public static AsymmetricCipherKeyPair generateEd25519KeyPair() {
        Ed25519KeyPairGenerator keyPairGenerator = new Ed25519KeyPairGenerator();
        keyPairGenerator.init(new Ed25519KeyGenerationParameters(new SecureRandom()));
        return keyPairGenerator.generateKeyPair();
    }
}
