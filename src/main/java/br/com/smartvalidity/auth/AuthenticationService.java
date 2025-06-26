package br.com.smartvalidity.auth;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.dto.EmpresaUsuarioDTO;
import br.com.smartvalidity.model.entity.Empresa;
import br.com.smartvalidity.model.entity.Usuario;
import br.com.smartvalidity.model.repository.UsuarioRepository;
import br.com.smartvalidity.service.EmpresaService;
import br.com.smartvalidity.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@Service
public class AuthenticationService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private final JwtService jwtService;

    public AuthenticationService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public String authenticate(Authentication authentication) {
        return jwtService.getGenerateToken(authentication);
    }

    public Usuario getUsuarioAutenticado() throws SmartValidityException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuarioAutenticado = null;

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();

            if (principal instanceof Jwt) {
                String email = ((Jwt) principal).getSubject();
                System.out.println("email value:" + email);
                Optional<Usuario> usuario = Optional.ofNullable(usuarioRepository.findByEmail(email).orElseThrow(
                        () -> new UsernameNotFoundException("Usuário não encontrado!")));

                usuarioAutenticado = usuario.get();

            }
        }

        return usuarioAutenticado;
    }
}
