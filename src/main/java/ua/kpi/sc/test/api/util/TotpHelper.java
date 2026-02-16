package ua.kpi.sc.test.api.util;

import com.bastiaanjansen.otp.HMACAlgorithm;
import com.bastiaanjansen.otp.TOTPGenerator;
import org.apache.commons.codec.binary.Base32;

import java.time.Duration;

public final class TotpHelper {

    private TotpHelper() {}

    public static String generateCode(String base32Secret) {
        Base32 base32 = new Base32();
        byte[] secretBytes = base32.decode(base32Secret);
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
