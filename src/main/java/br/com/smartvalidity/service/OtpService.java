package br.com.smartvalidity.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.entity.OtpToken;
import br.com.smartvalidity.model.enums.OtpPurpose;
import br.com.smartvalidity.model.repository.OtpTokenRepository;
import br.com.smartvalidity.service.UsuarioService;

@Service
public class OtpService {

    @Autowired
    private EmailService emailService;

    @Autowired
    private OtpTokenRepository otpTokenRepository;

    @Autowired
    private UsuarioService usuarioService;

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

    // -------------------- ESQUECEU SENHA --------------------

    /**
     * Dispara código OTP para recuperação de senha.
     */
    public void enviarCodigoEsqueceuSenha(String email) {
        emailService.enviarCodigoVerificacao(email, OtpPurpose.ESQUECEU_SENHA);
    }

    /**
     * Valida o código OTP para recuperação de senha.
     * Não remove o token para permitir o passo de redefinição de senha.
     */
    @Transactional
    public void validarCodigoEsqueceuSenha(String email, String token) throws SmartValidityException {
        OtpToken otp = otpTokenRepository.findByEmailAndTokenAndPurpose(email, token, OtpPurpose.ESQUECEU_SENHA)
                .orElseThrow(() -> new SmartValidityException("Código inválido"));

        if (otp.isExpired()) {
            throw new SmartValidityException("Código expirado");
        }
    }

    /**
     * Redefine a senha do usuário validando o OTP informado e removendo-o após o uso.
     */
    @Transactional
    public void redefinirSenha(String email, String token, String novaSenha) throws SmartValidityException {
        OtpToken otp = otpTokenRepository.findByEmailAndTokenAndPurpose(email, token, OtpPurpose.ESQUECEU_SENHA)
                .orElseThrow(() -> new SmartValidityException("Código inválido"));

        if (otp.isExpired()) {
            throw new SmartValidityException("Código expirado");
        }

        // Atualiza a senha do usuário
        usuarioService.redefinirSenha(email, novaSenha);

        // Remove tokens utilizados
        otpTokenRepository.deleteByEmailAndPurpose(email, OtpPurpose.ESQUECEU_SENHA);
    }
} 