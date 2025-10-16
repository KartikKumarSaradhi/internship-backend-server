package com.example.studentinternship.Service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class OtpService {

    private final Map<String, OtpData> otpStore = new HashMap<>();

    private static class OtpData {
        String otp;
        LocalDateTime expiry;

        OtpData(String otp, LocalDateTime expiry) {
            this.otp = otp;
            this.expiry = expiry;
        }
    }

    // Generate 6-digit OTP
    public String generateOtp(String email) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStore.put(email, new OtpData(otp, LocalDateTime.now().plusMinutes(5)));
        System.out.println("[DEBUG] OTP generated for email: " + email + ", OTP: " + otp);

        return otp;
    }

    // Validate OTP
    public boolean validateOtp(String email, String otp) {
        OtpData data = otpStore.get(email);
        if (data == null){
            System.out.println("[DEBUG] No OTP found for email: " + email);

            return false;
        }
        if (LocalDateTime.now().isAfter(data.expiry)) {
            System.out.println("[DEBUG] OTP expired for email: " + email);

            otpStore.remove(email);
            return false;
        }
        boolean valid = data.otp.equals(otp);
        System.out.println("[DEBUG] OTP validation result for email: " + email + ", OTP: " + otp + " → " + valid);

        if (valid) otpStore.remove(email);
        return valid;
    }
}
