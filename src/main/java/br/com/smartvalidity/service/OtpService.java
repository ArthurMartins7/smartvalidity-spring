package br.com.smartvalidity.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.entity.OtpToken;
import br.com.smartvalidity.model.enums.OtpPurpose;
import br.com.smartvalidity.model.repository.OtpTokenRepository;

@Service
public class OtpService {

    @Autowired
    private EmailService emailService;

    @Autowired
    private OtpTokenRepository otpTokenRepository;

    /**
     * Dispara código de verificação de e-mail e persiste token.
     */
    public void enviarCodigoVerificacao(String email) {
        emailService.enviarCodigoVerificacao(email, OtpPurpose.VERIFICAR_EMAIL);
    }

    /**
     * Valida o código informado para o e-mail.
     * Remove o token do banco quando válido.
     */
    @Transactional
    public void validarCodigo(String email, String token) throws SmartValidityException {
        OtpToken otp = otpTokenRepository.findByEmailAndTokenAndPurpose(email, token, OtpPurpose.VERIFICAR_EMAIL)
                .orElseThrow(() -> new SmartValidityException("Código inválido"));

        if (otp.isExpired()) {
            throw new SmartValidityException("Código expirado");
        }

        // Sucesso: mantém token para ser usado na etapa de criação da conta.
    }

    /**
     * Remove tokens para e-mail e finalidade.
     */
    @Transactional
    public void removerTokens(String email, OtpPurpose purpose) {
        otpTokenRepository.deleteByEmailAndPurpose(email, purpose);
    }
} 