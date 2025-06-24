package br.com.smartvalidity.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.entity.OtpToken;
import br.com.smartvalidity.model.enums.OtpPurpose;
import br.com.smartvalidity.model.repository.OtpTokenRepository;

@Service
public class OtpService {

    private static final SecureRandom random = new SecureRandom();
    private static final int OTP_LENGTH = 6; // 6 dígitos

    @Autowired
    private OtpTokenRepository otpTokenRepository;

    @Autowired
    private EmailService emailService;

    private String generateNumericOtp() {
        int number = 100000 + random.nextInt(900000); // 6 dígitos
        return String.valueOf(number);
    }

    @Transactional
    public void generateAndSendOtp(String email, OtpPurpose purpose, long minutesToExpire) {
        // Remove OTPs antigos para o mesmo propósito
        otpTokenRepository.deleteByEmailAndPurpose(email, purpose);

        String otp = generateNumericOtp();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(minutesToExpire);
        OtpToken otpToken = new OtpToken(email, otp, expiresAt, purpose);
        otpTokenRepository.save(otpToken);

        String subject;
        if (purpose == OtpPurpose.EMAIL_VERIFICATION) {
            subject = "Verificação de e-mail";
        } else {
            subject = "Redefinição de senha";
        }
        String body = String.format("Seu código OTP é: %s. Ele expira em %d minutos.", otp, minutesToExpire);
        emailService.sendEmail(email, subject, body);
    }

    @Transactional
    public void validateOtp(String email, String otp, OtpPurpose purpose) throws SmartValidityException {
        OtpToken otpToken = otpTokenRepository.findByEmailAndTokenAndPurpose(email, otp, purpose)
                .orElseThrow(() -> new SmartValidityException("OTP inválido"));

        if (otpToken.isExpired()) {
            otpTokenRepository.delete(otpToken);
            throw new SmartValidityException("OTP expirado");
        }

        // OTP válido, podemos removê-lo
        otpTokenRepository.delete(otpToken);
    }
} 