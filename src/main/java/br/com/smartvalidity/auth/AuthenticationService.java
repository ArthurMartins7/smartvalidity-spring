package br.com.smartvalidity.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.entity.Usuario;
import br.com.smartvalidity.model.enums.StatusUsuario;
import br.com.smartvalidity.model.repository.UsuarioRepository;

@Service
public class AuthenticationService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private final JwtService jwtService;

    public AuthenticationService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public String authenticate(Authentication authentication) {
        String token = jwtService.getGenerateToken(authentication);

        String email = authentication.getName();
        usuarioRepository.findByEmail(email).ifPresent(u -> {
            if (u.getStatus() == StatusUsuario.PENDENTE) {
                u.setStatus(StatusUsuario.ATIVO);
                usuarioRepository.save(u);
            }
        });

        return token;
    }

    public Usuario getUsuarioAutenticado() throws SmartValidityException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SmartValidityException("Usuário não autenticado!");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Jwt) {
            String email = ((Jwt) principal).getSubject();
            System.out.println("email value:" + email);
            
            try {
                Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado!"));
                return usuario;
            } catch (UsernameNotFoundException e) {
                throw new SmartValidityException("Usuário não encontrado!");
            }
        }

        throw new SmartValidityException("Token de autenticação inválido!");
    }
}
