package br.com.smartvalidity.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.entity.OtpToken;
import br.com.smartvalidity.model.enums.OtpPurpose;
import br.com.smartvalidity.model.repository.OtpTokenRepository;
import br.com.smartvalidity.model.repository.UsuarioRepository;

@Service
public class OtpService {

    @Autowired
    private EmailService emailService;

    @Autowired
    private OtpTokenRepository otpTokenRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    
    private void enviarCodigo(String email, OtpPurpose purpose) throws SmartValidityException {
        validarEmailParaPurpose(email, purpose);
        emailService.enviarCodigoVerificacao(email, purpose);
    }

    
    private void validarEmailParaPurpose(String email, OtpPurpose purpose) throws SmartValidityException {
        boolean existe = usuarioRepository.existsByEmail(email);

        switch (purpose) {
            case VERIFICAR_EMAIL -> {
                if (existe) {
                    throw new SmartValidityException("Este e-mail já está em uso. Tente fazer login ou utilize outra conta.");
                }
            }
            case ESQUECEU_SENHA, ALTERAR_SENHA -> {
                if (!existe) {
                    throw new SmartValidityException("Não foi possível enviar o código: não existe usuário cadastrado com este e-mail.");
                }
            }
            case SENHA_ALEATORIA -> {
                // Convite: sem validação extra (usuário será criado separadamente)
            }
        }
    }

    public void enviarCodigoVerificacao(String email) throws SmartValidityException {
        enviarCodigo(email, OtpPurpose.VERIFICAR_EMAIL);
    }

    public void enviarCodigoEsqueceuSenha(String email) throws SmartValidityException {
        enviarCodigo(email, OtpPurpose.ESQUECEU_SENHA);
    }

   
    @Transactional
    public void validarCodigo(String email, String token) throws SmartValidityException {
        OtpToken otp = otpTokenRepository.findByEmailAndTokenAndPurpose(email, token, OtpPurpose.VERIFICAR_EMAIL)
                .orElseThrow(() -> new SmartValidityException("Código inválido"));

        if (otp.isExpired()) {
            throw new SmartValidityException("Código expirado");
        }

    }

    @Transactional
    public void removerTokens(String email, OtpPurpose purpose) {
        otpTokenRepository.deleteByEmailAndPurpose(email, purpose);
    }

    // -------------------- ESQUECEU SENHA --------------------

    @Transactional
    public void validarCodigoEsqueceuSenha(String email, String token) throws SmartValidityException {
        OtpToken otp = otpTokenRepository.findByEmailAndTokenAndPurpose(email, token, OtpPurpose.ESQUECEU_SENHA)
                .orElseThrow(() -> new SmartValidityException("Código inválido"));

        if (otp.isExpired()) {
            throw new SmartValidityException("Código expirado");
        }
    }

    @Transactional
    public void redefinirSenha(String email, String token, String novaSenha) throws SmartValidityException {
        OtpToken otp = otpTokenRepository.findByEmailAndTokenAndPurpose(email, token, OtpPurpose.ESQUECEU_SENHA)
                .orElseThrow(() -> new SmartValidityException("Código inválido"));

        if (otp.isExpired()) {
            throw new SmartValidityException("Código expirado");
        }

        usuarioService.redefinirSenha(email, novaSenha);

        otpTokenRepository.deleteByEmailAndPurpose(email, OtpPurpose.ESQUECEU_SENHA);
    }
} 