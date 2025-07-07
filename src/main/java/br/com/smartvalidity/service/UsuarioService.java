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
import br.com.smartvalidity.model.enums.StatusUsuario;
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
        usuarioEditado.setCargo(Optional.ofNullable(usuarioDTO.getCargo()).orElse(usuarioEditado.getCargo()));

        return salvar(usuarioEditado);
    }

    public void excluir(String id) throws SmartValidityException {

        Usuario usuario =  this.buscarPorId(id);

        //this.authorizationService.verifiarCredenciaisUsuario(id);

        if (usuario.getPerfilAcesso() == PerfilAcesso.ASSINANTE) {
            if (usuario.getEmpresa() != null) {
                Empresa empresa = usuario.getEmpresa();
                empresa.getUsuarios().clear();
                empresaRepository.delete(empresa); 
            }
        } else {
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
        // Verifica permissão (ASSINANTE pode convidar)
        this.authorizationService.verificarPerfilAcesso();

        // Associa empresa do usuário autenticado, se existir
        Usuario autenticado = authorizationService.getUsuarioAutenticado();
        if (autenticado.getEmpresa() != null) {
            usuario.setEmpresa(autenticado.getEmpresa());
        }

        usuario.setStatus(StatusUsuario.PENDENTE);

        // Verifica se e-mail já está cadastrado
        this.verificarEmailJaUtilizado(usuario.getEmail(), null);

        // Gera senha aleatória numérica
        String senhaGerada = gerarSenhaNumerica();

        String senhaCodificada = passwordEncoder.encode(senhaGerada);
        usuario.setSenha(senhaCodificada);

        if (usuario.getPerfilAcesso() == null) {
            usuario.setPerfilAcesso(PerfilAcesso.OPERADOR);
        }

        Usuario salvo = usuarioRepository.save(usuario);

        emailService.enviarSenhaAleatoria(usuario.getEmail(), senhaGerada);

        return salvo;
    }

    private String gerarSenhaNumerica() {
        int max = (int) Math.pow(10, TAMANHO_SENHA_CONVITE);
        int numero = new java.util.Random().nextInt(max);
        return String.format("%0" + TAMANHO_SENHA_CONVITE + "d", numero);
    }

    @org.springframework.transaction.annotation.Transactional
    public void redefinirSenha(String email, String novaSenha) throws SmartValidityException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new SmartValidityException("Usuário não encontrado"));

        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
    }

    public Usuario atualizarPerfilUsuario(Usuario usuarioDTO) throws SmartValidityException {
        // Buscar o usuário autenticado
        Usuario usuarioAtual = authorizationService.getUsuarioAutenticado();
        
        // Verificar se o email já está sendo usado por outro usuário
        this.verificarEmailJaUtilizado(usuarioDTO.getEmail(), usuarioAtual.getId());
        
        // Atualizar apenas os campos permitidos
        usuarioAtual.setNome(Optional.ofNullable(usuarioDTO.getNome()).orElse(usuarioAtual.getNome()));
        usuarioAtual.setEmail(Optional.ofNullable(usuarioDTO.getEmail()).orElse(usuarioAtual.getEmail()));
        usuarioAtual.setCargo(Optional.ofNullable(usuarioDTO.getCargo()).orElse(usuarioAtual.getCargo()));
        
        return usuarioRepository.save(usuarioAtual);
    }

    public Usuario getUsuarioAutenticado() throws SmartValidityException {
        return authorizationService.getUsuarioAutenticado();
    }

    public List<Usuario> listarPendentes() throws SmartValidityException {
        Usuario usuarioAutenticado = this.authorizationService.getUsuarioAutenticado();
        return usuarioRepository.findByEmpresaAndStatus(usuarioAutenticado.getEmpresa(), StatusUsuario.PENDENTE);
    }

    public List<Usuario> listarAtivos() throws SmartValidityException {
        Usuario usuarioAutenticado = this.authorizationService.getUsuarioAutenticado();
        return usuarioRepository.findByEmpresaAndStatus(usuarioAutenticado.getEmpresa(), StatusUsuario.ATIVO);
    }

    public void reenviarConvite(String idUsuario) throws SmartValidityException {
        // Somente ASSINANTE pode reenviar convites
        this.authorizationService.verificarPerfilAcesso();

        Usuario usuarioConvidado = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new SmartValidityException("Usuário não encontrado"));

        // Verifica se está pendente
        if (usuarioConvidado.getStatus() != StatusUsuario.PENDENTE) {
            throw new SmartValidityException("Usuário já está ativo – não é possível reenviar convite.");
        }

        // Verifica se pertence à mesma empresa do assinante
        Usuario autenticado = authorizationService.getUsuarioAutenticado();
        if (autenticado.getEmpresa() == null || usuarioConvidado.getEmpresa() == null ||
                !autenticado.getEmpresa().getId().equals(usuarioConvidado.getEmpresa().getId())) {
            throw new SmartValidityException("Usuário não pertence à sua empresa.");
        }

        // Gera nova senha e envia
        String novaSenha = gerarSenhaNumerica();
        usuarioConvidado.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuarioConvidado);

        emailService.enviarSenhaAleatoria(usuarioConvidado.getEmail(), novaSenha);
    }

    public Usuario buscarAssinante() throws SmartValidityException {
        return usuarioRepository.findFirstByPerfilAcesso(PerfilAcesso.ASSINANTE)
                .orElseThrow(() -> new SmartValidityException("Usuário assinante não encontrado"));
    }
}
