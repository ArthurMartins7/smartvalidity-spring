package br.com.smartvalidity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.smartvalidity.auth.AuthenticationService;
import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.dto.EmailOtpDTO;
import br.com.smartvalidity.model.dto.ForgotPasswordDTO;
import br.com.smartvalidity.model.dto.ResetPasswordDTO;
import br.com.smartvalidity.model.entity.Usuario;
import br.com.smartvalidity.model.enums.OtpPurpose;
import br.com.smartvalidity.model.repository.UsuarioRepository;
import br.com.smartvalidity.service.OtpService;
import br.com.smartvalidity.service.UsuarioService;

@RestController
@RequestMapping(path = "/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Método de login padronizado -> Basic Auth
     * <p>
     * O parâmetro Authentication já encapsula login (username) e senha (password)
     * Basic <Base64 encoded username and password>
     *
     * @param authentication
     * @return o JWT gerado
     */
    @PostMapping("authenticate")
    public String authenticate(Authentication authentication) throws SmartValidityException {
        return authenticationService.authenticate(authentication);
    }

    @PostMapping("/novo-usuario")
    @ResponseStatus(code = HttpStatus.CREATED)
    public String registrarUsuario(@RequestBody Usuario novoUsuario) throws SmartValidityException {
        Usuario salvo = this.usuarioService.salvar(novoUsuario);
        // Gera OTP de verificação (24h = 1440 min)
        otpService.generateAndSendOtp(salvo.getEmail(), OtpPurpose.EMAIL_VERIFICATION, 1440);
        return "Usuário criado com sucesso. Verifique seu e-mail para concluir a ativação.";
    }

    @PostMapping("/verificar-email-otp")
    public String verificarEmail(@RequestBody EmailOtpDTO dto) throws SmartValidityException {
        otpService.validateOtp(dto.getEmail(), dto.getOtp(), OtpPurpose.EMAIL_VERIFICATION);
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new SmartValidityException("Usuário não encontrado"));
        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);
        return "E-mail verificado com sucesso!";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestBody ForgotPasswordDTO dto) throws SmartValidityException {
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new SmartValidityException("Usuário não encontrado"));
        if(Boolean.FALSE.equals(usuario.getEmailVerificado())) {
            throw new SmartValidityException("E-mail não verificado");
        }
        otpService.generateAndSendOtp(dto.getEmail(), OtpPurpose.PASSWORD_RESET, 15);
        return "OTP enviado para redefinição de senha.";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestBody ResetPasswordDTO dto) throws SmartValidityException {
        otpService.validateOtp(dto.getEmail(), dto.getOtp(), OtpPurpose.PASSWORD_RESET);
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new SmartValidityException("Usuário não encontrado"));
        usuario.setSenha(passwordEncoder.encode(dto.getNovaSenha()));
        usuarioRepository.save(usuario);
        return "Senha redefinida com sucesso.";
    }
    
    /**
     * Retorna as informações do usuário atualmente autenticado
     * 
     * @return Objeto Usuario com as informações do usuário autenticado
     * @throws SmartValidityException Se o usuário não estiver autenticado
     */
    @GetMapping("/current-user")
    public Usuario getUserInfo() throws SmartValidityException {
        return authenticationService.getUsuarioAutenticado();
    }
}
