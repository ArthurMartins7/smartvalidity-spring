package br.com.smartvalidity.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.smartvalidity.model.entity.OtpToken;
import br.com.smartvalidity.model.enums.OtpPurpose;
import br.com.smartvalidity.model.repository.OtpTokenRepository;

@Service
public class EmailService {

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRATION_MINUTES = 15;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private OtpTokenRepository otpTokenRepository;

    /**
     * Gera e envia um código OTP (One-Time Password) para o e-mail informado.
     * <p>
     * O código é persistido no banco de dados para posterior validação e expira
     * após {@link #OTP_EXPIRATION_MINUTES} minutos.
     * </p>
     *
     * @param email   e-mail de destino
     * @param purpose finalidade do OTP (ver {@link OtpPurpose})
     */
    @Transactional
    public void enviarCodigoVerificacao(String email, OtpPurpose purpose) {



        String token = gerarCodigoNumerico();

        otpTokenRepository.deleteByEmailAndPurpose(email, purpose);

        LocalDateTime expiraEm = LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES);
        OtpToken otpToken = new OtpToken(email, token, expiraEm, purpose);
        otpTokenRepository.save(otpToken);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(definirAssunto(purpose));
        message.setText("Seu código de verificação é: " + token + "\n\n" +
                "Ele expira em " + OTP_EXPIRATION_MINUTES + " minutos.");

        mailSender.send(message);
    }

    /**
     * Gera um código numérico aleatório com {@link #OTP_LENGTH} dígitos.
     */
    private String gerarCodigoNumerico() {
        Random random = new Random();
        int numero = random.nextInt((int) Math.pow(10, OTP_LENGTH));
        return String.format("%0" + OTP_LENGTH + "d", numero);
    }

    private String definirAssunto(OtpPurpose purpose) {
        return switch (purpose) {
            case VERIFICAR_EMAIL -> "Verificação de e-mail";
            case ESQUECEU_SENHA -> "Recuperação de senha";
            case ALTERAR_SENHA -> "Alteração de senha";
            case SENHA_ALEATORIA -> "Convite para o sistema";
        };
    }

    /**
     * Envia ao usuário a senha gerada no processo de convite.
     * NÃO grava nada em OtpToken.
     *
     * @param email 
     * @param senhaGerada 
     */
    public void enviarSenhaAleatoria(String email, String senhaGerada) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(definirAssunto(OtpPurpose.SENHA_ALEATORIA));
        message.setText("Você foi convidado a acessar o SmartValidity.\n\n" +
                "Sua senha provisória é: " + senhaGerada + "\n" +
                "Recomendamos alterá-la no primeiro login.");

        mailSender.send(message);
    }
}
