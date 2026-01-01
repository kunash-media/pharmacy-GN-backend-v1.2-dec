package com.gn.pharmacy.controller;

import com.gn.pharmacy.dto.request.EmailRequest;
import com.gn.pharmacy.dto.request.OtpVerificationDto;
import com.gn.pharmacy.dto.request.ResetPasswordRequest;
import com.gn.pharmacy.service.OtpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/otp")
public class OtpController {

    @Autowired
    private OtpService otpService;

    // Add this logger declaration
    private static final Logger logger = LoggerFactory.getLogger(OtpController.class);

    @PostMapping("/send-email-body")
    public ResponseEntity<String> sendEmailOtp(@RequestBody EmailRequest emailRequest) {
        try {
            otpService.sendOtpEmail(emailRequest.getEmail());
            return ResponseEntity.ok("OTP sent successfully to email: " + emailRequest.getEmail());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to send OTP: " + e.getMessage());
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmailOtp(@RequestBody OtpVerificationDto otpVerificationDto) {
        try {
            boolean isValid = otpService.verifyEmailOtp(otpVerificationDto);
            if (isValid) {
                return ResponseEntity.ok("OTP verified successfully");
            } else {
                return ResponseEntity.badRequest().body("Invalid or expired OTP");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Verification failed: " + e.getMessage());
        }
    }

    @PostMapping("/validate-otp")
    public ResponseEntity<String> validateOtp(@RequestBody OtpVerificationDto otpVerificationDto) {
        try {
            // This only validates OTP without marking it as used
            boolean isValid = otpService.validateOtpWithoutMarkingUsed(otpVerificationDto);
            if (isValid) {
                return ResponseEntity.ok("OTP is valid");
            } else {
                return ResponseEntity.badRequest().body("Invalid or expired OTP");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Verification failed: " + e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) {
        logger.info("Password reset requested for email: {}", resetPasswordRequest.getEmail());

        try {
            // Create OTP verification DTO with new password
            OtpVerificationDto otpVerificationDto = new OtpVerificationDto();
            otpVerificationDto.setEmail(resetPasswordRequest.getEmail());
            otpVerificationDto.setOtp(resetPasswordRequest.getOtp());
            otpVerificationDto.setNewPassword(resetPasswordRequest.getNewPassword());

            // This will verify OTP AND reset password (and mark OTP as used)
            boolean isValid = otpService.verifyEmailOtp(otpVerificationDto);

            if (isValid) {
                logger.info("Password reset successful for email: {}", resetPasswordRequest.getEmail());
                return ResponseEntity.ok("Password reset successfully");
            } else {
                logger.warn("Invalid OTP for password reset: {}", resetPasswordRequest.getEmail());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Invalid or expired OTP");
            }
        } catch (Exception e) {
            logger.error("Password reset failed for email: {}", resetPasswordRequest.getEmail(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Password reset failed: " + e.getMessage());
        }
    }
}