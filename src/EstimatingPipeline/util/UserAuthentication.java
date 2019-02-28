package EstimatingPipeline.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 *
 * @author Jenny Nguyen
 */
public class UserAuthentication {
 
    public static byte[] getEncryptedPassword(String password, byte[] salt)
    throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        return factory.generateSecret(spec).getEncoded();
    }

    public static byte[] generateSalt() throws NoSuchAlgorithmException
    {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        
        return salt;
    }
    
    public static boolean authenticate(String attemptedPassword, byte[] encryptedPassword, byte[] salt) 
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        byte[] encryptedAttemptedPassword = getEncryptedPassword(attemptedPassword, salt);
        
        return Arrays.equals(encryptedPassword, encryptedAttemptedPassword);

    }
}
