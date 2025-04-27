package br.com.smartvalidity.service;

import br.com.smartvalidity.auth.AuthenticationService;
import br.com.smartvalidity.auth.AuthorizationService;
import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.entity.Usuario;
import br.com.smartvalidity.model.enums.PerfilAcesso;
import br.com.smartvalidity.model.repository.UsuarioRepository;
import br.com.smartvalidity.model.seletor.UsuarioSeletor;
import org.hibernate.tool.schema.spi.SchemaManagementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usuarioRepository.findByEmail(username).orElseThrow(
                () -> new UsernameNotFoundException("Usuário não encontrado" + username));
    }

    public List<Usuario> buscarComSeletor(UsuarioSeletor seletor) throws SmartValidityException {
        this.authorizationService.verificarPerfilAcesso();

        if (seletor.temPaginacao()) {
            int numeroPagina = seletor.getPagina();
            int tamanhoPagina = seletor.getLimite();

            PageRequest pagina = PageRequest.of(numeroPagina - 1, tamanhoPagina);
            return this.usuarioRepository.findAll(seletor, pagina).toList();
        }

        return this.usuarioRepository.findAll(seletor);
    }

    public List<Usuario> listarTodos() throws SmartValidityException {
        this.authorizationService.verificarPerfilAcesso();
        return usuarioRepository.findAll();
    }

    public Usuario buscarPorId(String id) throws SmartValidityException {
        authorizationService.verificarPerfilAcesso();
        return usuarioRepository.findById(id).orElseThrow(
                () -> new SmartValidityException("Usuário não encontrado"));
    }

    public Usuario salvar(Usuario novoUsuario) throws SmartValidityException {
        this.verificarEmailJaUtilizado(novoUsuario.getEmail(), novoUsuario.getId());

        if(!this.usuarioRepository.findBySenha(novoUsuario.getSenha()).isPresent()){
            novoUsuario.setSenha(passwordEncoder.encode(novoUsuario.getSenha()));
        }

        if(novoUsuario.getPerfilAcesso() == null || novoUsuario.getPerfilAcesso().toString().isEmpty()) {
            novoUsuario.setPerfilAcesso(PerfilAcesso.OPERADOR);
        }

        return usuarioRepository.save(novoUsuario);
    }

    public Usuario alterar(String idUsuarioURL, Usuario usuarioDTO) throws SmartValidityException {
        Usuario usuarioEditado = usuarioRepository.findById(idUsuarioURL).orElseThrow(
                () -> new SmartValidityException("Usuário não encontrado"));

        this.authorizationService.verifiarCredenciaisUsuario(usuarioEditado.getId());

        this.verificarEmailJaUtilizado(usuarioDTO.getEmail(), usuarioEditado.getId());

        usuarioEditado.setPerfilAcesso(Optional.ofNullable(usuarioDTO.getPerfilAcesso()).orElse(usuarioEditado.getPerfilAcesso()));
        usuarioEditado.setCpf(Optional.ofNullable(usuarioDTO.getCpf()).orElse(usuarioEditado.getCpf()));
        usuarioEditado.setNome(Optional.ofNullable(usuarioDTO.getNome()).orElse(usuarioEditado.getNome()));
        usuarioEditado.setEmail(Optional.ofNullable(usuarioDTO.getEmail()).orElse(usuarioEditado.getEmail()));
        usuarioEditado.setSenha(Optional.ofNullable(usuarioDTO.getSenha()).orElse(usuarioEditado.getSenha()));

        return salvar(usuarioEditado);
    }

    public void excluir(String id) throws SmartValidityException {

        this.authorizationService.verifiarCredenciaisUsuario(id);

        this.usuarioRepository.deleteById(id);
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
