package ua.kpi.sc.test.api.util;

import com.bastiaanjansen.otp.HMACAlgorithm;
import com.bastiaanjansen.otp.TOTPGenerator;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

public final class TotpHelper {

    private TotpHelper() {}

    /**
     * Generates a 6-digit TOTP code from a base32-encoded secret string.
     * The secret is passed as ASCII bytes to TOTPGenerator which internally
     * base32-decodes it to the raw HMAC key.
     */
    public static String generateCode(String base32Secret) {
        byte[] secretBytes = base32Secret.getBytes(StandardCharsets.US_ASCII);
        TOTPGenerator generator = new TOTPGenerator.Builder(secretBytes)
                .withHOTPGenerator(builder -> {
                    builder.withPasswordLength(6);
                    builder.withAlgorithm(HMACAlgorithm.SHA1);
                })
                .withPeriod(Duration.ofSeconds(30))
                .build();
        return generator.now();
    }
}
