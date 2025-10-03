package com.example.service;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TotpService {

    public int[] generateToken(String secret) {
        GoogleAuthenticator gAuth = new GoogleAuthenticator();

        // Wrap secret into a GoogleAuthenticatorKey for QR generation
        GoogleAuthenticatorKey key = new GoogleAuthenticatorKey.Builder(secret).build();

        // Generate provisioning URI (can be turned into a QR)
        String issuer = "MyApp";
        String accountName = "prakash@example.com";
        String otpAuthURL = GoogleAuthenticatorQRGenerator.getOtpAuthURL(issuer, accountName, key);
        System.out.println("Provisioning URI: " + otpAuthURL);

        // Generate the current TOTP code (valid for this 30s time step)
        int currentCode = gAuth.getTotpPassword(secret);
        System.out.printf("Current TOTP: %06d%n", currentCode);

        // Validate the generated code
        boolean isCodeValid = gAuth.authorize(secret, currentCode);
        System.out.println("Is code valid? " + isCodeValid);

        // Convert code to 6 digits array
        String codeStr = String.format("%06d", currentCode); // pad with zeros
        int[] digits = new int[codeStr.length()];
        for (int i = 0; i < codeStr.length(); i++) {
            digits[i] = codeStr.charAt(i) - '0';
        }

        return digits;
    }
}
