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
import br.com.smartvalidity.model.entity.Usuario;
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
    public Usuario registrarUsuario(@RequestBody Usuario novoUsuario) throws SmartValidityException {
        return this.usuarioService.salvar(novoUsuario);
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
