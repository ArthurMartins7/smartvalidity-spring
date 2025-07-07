package br.com.smartvalidity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import br.com.smartvalidity.model.dto.EmpresaUsuarioDTO;
import br.com.smartvalidity.model.dto.OtpValidateDTO;
import br.com.smartvalidity.model.dto.ResetPasswordDTO;
import br.com.smartvalidity.model.entity.Empresa;
import br.com.smartvalidity.model.entity.Usuario;
import br.com.smartvalidity.service.EmpresaService;
import br.com.smartvalidity.service.OtpService;
import br.com.smartvalidity.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping(path = "/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmpresaService empresaService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private OtpService otpService;

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

    @Operation(summary = "Registrar nova empresa com usuário assinante")
    @PostMapping("/registrar-empresa")
    public ResponseEntity<Empresa> registrarEmpresa(@Valid @RequestBody EmpresaUsuarioDTO dto) throws SmartValidityException {
        Empresa empresa = empresaService.cadastrarEmpresaEAssinante(dto);
        return ResponseEntity.status(201).body(empresa);
    }

    @PostMapping("/novo-usuario")
    @ResponseStatus(code = HttpStatus.CREATED)
    public Usuario registrarUsuario(@RequestBody @Valid Usuario novoUsuario) throws SmartValidityException {
        return this.usuarioService.salvar(novoUsuario);
    }


    @GetMapping("/verificar-assinatura")
    public boolean verificarSeExisteUsuarioAssinante() throws SmartValidityException {
        return this.usuarioService.verificarSeExisteUsuarioAssinante();
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

    // --------------------- OTP: Verificar E-mail ---------------------

    @PostMapping("/enviar-otp-email")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void enviarOtpEmail(@RequestBody @Valid EmailOtpDTO dto) throws SmartValidityException {
        otpService.enviarCodigoVerificacao(dto.getEmail());
    }

    @PostMapping("/validar-otp-email")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void validarOtpEmail(@RequestBody @Valid OtpValidateDTO dto) throws SmartValidityException {
        otpService.validarCodigo(dto.getEmail(), dto.getToken());
    }

    // --------------------- OTP: Esqueceu Senha ---------------------

    @PostMapping("/enviar-otp-esqueceu-senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void enviarOtpEsqueceuSenha(@RequestBody @Valid EmailOtpDTO dto) throws SmartValidityException {
        otpService.enviarCodigoEsqueceuSenha(dto.getEmail());
    }

    @PostMapping("/validar-otp-esqueceu-senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void validarOtpEsqueceuSenha(@RequestBody @Valid OtpValidateDTO dto) throws SmartValidityException {
        otpService.validarCodigoEsqueceuSenha(dto.getEmail(), dto.getToken());
    }

    @PostMapping("/resetar-senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetarSenha(@RequestBody @Valid ResetPasswordDTO dto) throws SmartValidityException {
        otpService.redefinirSenha(dto.getEmail(), dto.getToken(), dto.getNovaSenha());
    }

    @GetMapping("/assinante")
    public Usuario buscarAssinante() throws SmartValidityException {
        return usuarioService.buscarAssinante();
    }

    // --------------------- OTP: Alterar Senha (usuário logado) ---------------------

    @PostMapping("/enviar-otp-alterar-senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void enviarOtpAlterarSenha(@RequestBody @Valid EmailOtpDTO dto) throws SmartValidityException {
        otpService.enviarCodigoAlterarSenha(dto.getEmail());
    }

    @PostMapping("/validar-otp-alterar-senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void validarOtpAlterarSenha(@RequestBody @Valid OtpValidateDTO dto) throws SmartValidityException {
        otpService.validarCodigoAlterarSenha(dto.getEmail(), dto.getToken());
    }

    @PostMapping("/alterar-senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void alterarSenha(@RequestBody @Valid ResetPasswordDTO dto) throws SmartValidityException {
        otpService.alterarSenha(dto.getEmail(), dto.getToken(), dto.getNovaSenha());
    }
}
