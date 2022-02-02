package frameworkcore;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import frameworkcore.errormanagers.ErrorHandler;
import frameworkcore.errormanagers.ErrorHandler.ErrLvl;

/**
 * This class is a utility to encrypt and decrypt sensitive data.
 * 
 * @author Dinesh Sonar (dinesh.sonar), Saurav Kumar Sahoo (saurav.kumar.sahoo)
 */
public final class EncryptionToolkit {

    private static final String EXCEPTION_ENCRYPTION = "Encryption Error";

    private static final String DEFAULT_ALGORITHM = "AES/CTR/NoPadding";

    private static final String DELIMETER = "/";

    private EncryptionToolkit() {
        // private constructor
    }

    /**
     * Encrypt given data based the String key provided
     * 
     * @param data Data to encrypt
     * @param key Key to encrypt with
     * @return String encryptedData
     */
    public static String encrypt(String data, String key) {
        return encrypt(data, getSecretKeySpec(key));
    }

    /**
     * Encrypt the data based on the key file provided
     * 
     * @param data Data to encrypt
     * @param key Key to encrypt with
     * @return String encryptedData
     */
    public static String encrypt(String data, File keyFile) {
        return encrypt(data, getSecretKeySpec(keyFile));
    }

    /**
     * Decrypt data based on the String key provided
     * 
     * @param data Data to decrypt
     * @param key Key to decrypt with
     * @return String decryptedData
     */
    public static String decrypt(String data, String key) {
        return decrypt(data, getSecretKeySpec(key));
    }

    /**
     * Decrypt data based on the key file provided
     * 
     * @param data Data to decrypt
     * @param key Key to decrypt with
     * @return String decryptedData
     */
    public static String decrypt(String data, File keyFile) {
        return decrypt(data, getSecretKeySpec(keyFile));
    }

    /**
     * Decrypt data based on the String key provided
     * 
     * @param data Data to decrypt
     * @param key Key to decrypt with
     * @return String decryptedData
     * @throws IOException on I/O Failure
     */
    public static String decryptPropertiesAsSource(String data, String propertyValue) throws IOException {
        return StringUtils.isBlank(data) ? "" : decrypt(data, getSecretKeySpecPropertiesAsSource(propertyValue));
    }

    /**
     * Generated key spec from a String key
     * 
     * @param key to generate spec with
     * @return {@link SecretKeySpec}
     */
    private static SecretKeySpec getSecretKeySpec(String key) {
        if (StringUtils.isNotBlank(key)) {
            return new SecretKeySpec(key.getBytes(), StringUtils.substringBefore(DEFAULT_ALGORITHM, DELIMETER));
        } else {
            String logMessage = "Key is blank; Key=" + key;
            ErrorHandler.frameworkError(ErrLvl.ERROR, null, logMessage);
            return null;
        }
    }

    /**
     * Generated key spec from a String key in property
     * 
     * @param key to generate spec with
     * @return {@link SecretKeySpec}
     * @throws IOException on I/O Failure
     */
    private static SecretKeySpec getSecretKeySpecPropertiesAsSource(String propertyValue) throws IOException {
        if (StringUtils.isNotBlank(propertyValue)) {
            return new SecretKeySpec(FileUtils.readFileToByteArray(new File(propertyValue)),
                    StringUtils.substringBefore(DEFAULT_ALGORITHM, DELIMETER));
        } else {
            String logMessage = "Key is blank; Key=" + propertyValue;
            ErrorHandler.frameworkError(ErrLvl.ERROR, null, logMessage);
            return null;
        }
    }

    /**
     * Generated key spec from Key File
     * 
     * @param keyFile get spec from key file
     * @return {@link SecretKeySpec} spec
     */
    private static SecretKeySpec getSecretKeySpec(File keyFile) {
        try {
            return new SecretKeySpec(FileUtils.readFileToByteArray(keyFile),
                    StringUtils.substringBefore(DEFAULT_ALGORITHM, DELIMETER));
        } catch (IOException ioe) {
            String logMessage = "Failed to read key from file; FilePath=" + keyFile.getAbsolutePath();
            ErrorHandler.frameworkError(ErrLvl.ERROR, null, logMessage);
            return null;
        }
    }

    /**
     * Encrypt the data based the key spec provided
     * 
     * @param data to encrypt
     * @param keySpec to encrypt with
     * @return String encrypted data
     */
    private static String encrypt(String data, SecretKeySpec keySpec) {
        try {
            Cipher cipher = Cipher.getInstance(DEFAULT_ALGORITHM);
            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes());
            byte[] ivBytes = cipher.getIV();

            byte[] finalEncrypted = new byte[ivBytes.length + encryptedBytes.length];
            System.arraycopy(ivBytes, 0, finalEncrypted, 0, ivBytes.length);
            System.arraycopy(encryptedBytes, 0, finalEncrypted, ivBytes.length, encryptedBytes.length);

            return Base64.encodeBase64String(finalEncrypted);
        } catch (GeneralSecurityException gse) {
            ErrorHandler.frameworkError(ErrLvl.ERROR, gse, "General Security Exception: " + EXCEPTION_ENCRYPTION);
            return null;
        }
    }

    /**
     * Decrypt the data based on the key spec provided
     * 
     * @param data to decrypt
     * @param keySpec to decrypt with
     * @return String decrypted data
     */
    private static String decrypt(String data, SecretKeySpec keySpec) {
        try {
            byte[] finalEncrypted = Base64.decodeBase64(data);
            byte[] ivBytes = Arrays.copyOfRange(finalEncrypted, 0, 16);
            byte[] encryptedBytes = Arrays.copyOfRange(finalEncrypted, ivBytes.length, finalEncrypted.length);

            Cipher cipher = Cipher.getInstance(DEFAULT_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(ivBytes));
            return new String(cipher.doFinal(encryptedBytes));
        } catch (GeneralSecurityException gse) {
            ErrorHandler.frameworkError(ErrLvl.ERROR, gse, "General Security Exception: " + EXCEPTION_ENCRYPTION);
            return null;
        }
    }

}
