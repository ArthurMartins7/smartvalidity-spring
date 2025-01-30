package br.com.smartvalidity.service;

import br.com.smartvalidity.auth.AuthenticationService;
import br.com.smartvalidity.auth.AuthorizationService;
import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.entity.Usuario;
import br.com.smartvalidity.model.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private AuthorizationService authorizationService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usuarioRepository.findByEmail(username).orElseThrow(
                () -> new UsernameNotFoundException("Usuário não encontrado" + username));
    }

    public List<Usuario> listarTodos() throws SmartValidityException {
        this.authorizationService.verificarPerfilAcesso();
        return usuarioRepository.findAll();
    }

    public Usuario salvar(Usuario usuario) throws SmartValidityException {
        this.verificarEmailJaUtilizado(usuario.getEmail(), usuario.getId());
        return usuarioRepository.save(usuario);
    }

    public void verificarEmailJaUtilizado(String email, String idUsuarioAtual) throws SmartValidityException {
        boolean emailJaUtilizado;

        if (idUsuarioAtual == null) {
            emailJaUtilizado = usuarioRepository.existsByEmail(email);
        } else {
            emailJaUtilizado = usuarioRepository.existsByEmailAndIdNot(email, idUsuarioAtual);
        }

        if (emailJaUtilizado) {
            throw new SmartValidityException("Não pode utilizar um e-mail já cadastrado!");
        }
    }
}
