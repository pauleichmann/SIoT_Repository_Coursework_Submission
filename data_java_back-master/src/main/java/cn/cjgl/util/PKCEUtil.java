package cn.cjgl.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PKCEUtil {

    // Generate a random string as a code verifier
    public static String generateCodeVerifier2() {
        SecureRandom sr = new SecureRandom();
        byte[] codeVerifierBytes = new byte[128 / 8]; // 128 bits
        sr.nextBytes(codeVerifierBytes);
        // Convert a byte array to a Base64 URL-encoded string
        return Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifierBytes);
    }

    // Generate a code_verifier with a length between 43 and 128 characters.
    public static String generateCodeVerifier() {
        int minLength = 43;
        int maxLength = 128;
        int targetLength = minLength + (int) (Math.random() * (maxLength - minLength + 1));

        // Since Base64 encoding increases the length of the string, we need to generate a slightly shorter byte array.
        int byteArraySize = (targetLength * 3) / 4; // Approximate number of bytes required (rounded down)
        byte[] randomBytes = new byte[byteArraySize];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(randomBytes);

        // Encode using Base64.getUrlEncoder() and remove any possible padding characters ('=')
        String codeVerifier = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        // If the encoded string is still too short, it will be regenerated (this usually does not happen unless the target length is very close to 43).
        while (codeVerifier.length() < minLength) {
            byteArraySize++;
            randomBytes = new byte[byteArraySize];
            secureRandom.nextBytes(randomBytes);
            codeVerifier = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        }

        // If the encoded string is too long, truncate it (this shouldn't happen, but for safety's sake)
        if (codeVerifier.length() > maxLength) {
            codeVerifier = codeVerifier.substring(0, maxLength);
        }

        return codeVerifier;
    }
    // Generate a code challenge using the SHA-256 hash function
    public static String generateCodeChallenge(String codeVerifier) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.UTF_8));
        // Encode the hash value using Base64 URL encoding
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }

    public static void main(String[] args) {
        try {
            // generate code verifier
            String codeVerifier = generateCodeVerifier();
            System.out.println("Code Verifier: " + codeVerifier);

            // generate code challenge
            String codeChallenge = generateCodeChallenge(codeVerifier);
            System.out.println("Code Challenge: " + codeChallenge);

            // In this example, you can continue to use these values for OAuth 2.0 authorization and token requests.
            // ...

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
