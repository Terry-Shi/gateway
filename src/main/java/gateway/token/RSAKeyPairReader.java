package gateway.token;

import gateway.error.AppException;
import com.google.common.io.ByteStreams;
import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 *
 */
@Component
public class RSAKeyPairReader {
    private static final Logger logger = LoggerFactory.getLogger(RSAKeyPairReader.class);

    public RSAPublicKey readPublicKey(String resourceName) {
        try {
            byte[] keyBytes = readBytes(resourceName);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) keyFactory.generatePublic(spec);
        } catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException e) {
            String msg = String.format("Failed to read public key from file - %s", resourceName);
            logger.error(msg, e);
            throw new AppException(msg, e);
        }
    }

    public RSAPrivateKey readPrivateKey(String resourceName) {
        try {
            byte[] keyBytes = readBytes(resourceName);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) keyFactory.generatePrivate(spec);
        } catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException e) {
            String msg = String.format("Failed to read private key from file - %s", resourceName);
            logger.error(msg, e);
            throw new AppException(msg, e);
        }
    }

    private byte[] readBytes(String resourceName) throws IOException {
        return ByteStreams.toByteArray(Resources.getResource(resourceName).openStream());
    }
}
