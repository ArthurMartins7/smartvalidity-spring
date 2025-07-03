package br.com.smartvalidity.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.smartvalidity.auth.AuthorizationService;
import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.entity.Empresa;
import br.com.smartvalidity.model.entity.Usuario;
import br.com.smartvalidity.model.enums.PerfilAcesso;
import br.com.smartvalidity.model.repository.EmpresaRepository;
import br.com.smartvalidity.model.repository.UsuarioRepository;
import br.com.smartvalidity.model.seletor.UsuarioSeletor;

@Service
public class UsuarioService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    private static final int TAMANHO_SENHA_CONVITE = 6;

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
        //usuarioEditado.setCpf(Optional.ofNullable(usuarioDTO.getCpf()).orElse(usuarioEditado.getCpf()));
        usuarioEditado.setNome(Optional.ofNullable(usuarioDTO.getNome()).orElse(usuarioEditado.getNome()));
        usuarioEditado.setEmail(Optional.ofNullable(usuarioDTO.getEmail()).orElse(usuarioEditado.getEmail()));
        usuarioEditado.setSenha(Optional.ofNullable(usuarioDTO.getSenha()).orElse(usuarioEditado.getSenha()));

        return salvar(usuarioEditado);
    }

    public void excluir(String id) throws SmartValidityException {

        Usuario usuario =  this.buscarPorId(id);

        this.authorizationService.verifiarCredenciaisUsuario(id);

        // Se é assinante, delete a empresa (cascade removerá usuários)
        if (usuario.getPerfilAcesso() == PerfilAcesso.ASSINANTE) {
            if (usuario.getEmpresa() != null) {
                Empresa empresa = usuario.getEmpresa();
                empresa.getUsuarios().clear();
                empresaRepository.delete(empresa); // cascade remove assinante e colaboradores
            }
        } else {
            // colaborador: apenas remover da empresa e excluir usuário
            if (usuario.getEmpresa() != null) {
                usuario.getEmpresa().getUsuarios().remove(usuario);
            }
            usuarioRepository.delete(usuario);
        }
    }

    public boolean verificarSeExisteUsuarioAssinante() throws SmartValidityException {
        return this.usuarioRepository.existsUsuarioByPerfilAcesso(PerfilAcesso.ASSINANTE);
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

    /**
     * Convida um novo usuário gerando uma senha provisória de 6 dígitos que é
     * enviada por e-mail. A senha é criptografada antes de ser salva.
     *
     * @param usuario objeto usuário a ser persistido
     * @return usuário salvo com senha criptografada
     * @throws SmartValidityException caso e-mail já esteja em uso ou outro erro de regra
     */
    public Usuario convidarUsuario(Usuario usuario) throws SmartValidityException {
        // Verifica se e-mail já está cadastrado
        this.verificarEmailJaUtilizado(usuario.getEmail(), null);

        // Gera senha aleatória numérica
        String senhaGerada = gerarSenhaNumerica();

        // Codifica a senha gerada e define no usuário
        String senhaCodificada = passwordEncoder.encode(senhaGerada);
        usuario.setSenha(senhaCodificada);

        // Define perfil padrão se não informado
        if (usuario.getPerfilAcesso() == null) {
            usuario.setPerfilAcesso(PerfilAcesso.OPERADOR);
        }

        Usuario salvo = usuarioRepository.save(usuario);

        // Envia e-mail com a senha (texto puro)
        emailService.enviarSenhaAleatoria(usuario.getEmail(), senhaGerada);

        return salvo;
    }

    private String gerarSenhaNumerica() {
        int max = (int) Math.pow(10, TAMANHO_SENHA_CONVITE);
        int numero = new java.util.Random().nextInt(max);
        return String.format("%0" + TAMANHO_SENHA_CONVITE + "d", numero);
    }

    // Método para redefinir senha quando usuário esquece a senha
    @org.springframework.transaction.annotation.Transactional
    public void redefinirSenha(String email, String novaSenha) throws SmartValidityException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new SmartValidityException("Usuário não encontrado"));

        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
    }
}
