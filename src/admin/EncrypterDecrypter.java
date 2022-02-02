package admin;

import java.io.File;

import frameworkcore.EncryptionToolkit;

/**
 * This class should be accessible to only framework/application administrators.
 * 
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
 */
public class EncrypterDecrypter {

	public static void main(String[] args) {
		String value = "";

		// encrypt(value);
		// decrypt(value);

	}

	private static void encrypt(String value) {
		System.out.println(EncryptionToolkit.encrypt(value,
				new File(System.getProperty("user.dir") + "\\lib\\crypt\\automation_encryption_key.txt")));
	}

	private static void decrypt(String value) {
		System.out.println(EncryptionToolkit.decrypt(value,
				new File(System.getProperty("user.dir") + "\\lib\\crypt\\automation_encryption_key.txt")));
	}
}
