package br.com.smartvalidity.java.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.smartvalidity.auth.AuthenticationService;
import br.com.smartvalidity.auth.AuthorizationService;
import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.entity.Usuario;
import br.com.smartvalidity.model.enums.PerfilAcesso;
import br.com.smartvalidity.model.repository.UsuarioRepository;
import br.com.smartvalidity.model.seletor.UsuarioSeletor;
import br.com.smartvalidity.service.UsuarioService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do UsuarioService")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuarioValido;
    private Usuario usuarioComEmailExistente;

    @BeforeEach
    void setUp() {
        usuarioValido = new Usuario();
        usuarioValido.setId(UUID.randomUUID().toString());
        usuarioValido.setNome("João Silva");
        usuarioValido.setEmail("joao@teste.com");
        usuarioValido.setCpf("123.456.789-09");
        usuarioValido.setSenha("senhaCriptografada");
        usuarioValido.setPerfilAcesso(PerfilAcesso.OPERADOR);

        usuarioComEmailExistente = new Usuario();
        usuarioComEmailExistente.setId(UUID.randomUUID().toString());
        usuarioComEmailExistente.setNome("Maria Santos");
        usuarioComEmailExistente.setEmail("joao@teste.com"); // Mesmo email
        usuarioComEmailExistente.setCpf("987.654.321-00");
        usuarioComEmailExistente.setSenha("outraSenha");
        usuarioComEmailExistente.setPerfilAcesso(PerfilAcesso.ADMIN);
    }

    @Test
    @DisplayName("Deve salvar usuário válido com perfil padrão OPERADOR")
    void deveSalvarUsuarioValido() throws SmartValidityException {
        // Given
        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome("Pedro Costa");
        novoUsuario.setEmail("pedro@teste.com");
        novoUsuario.setCpf("111.222.333-44");
        novoUsuario.setSenha("123456");
        // Perfil não definido - deve ser OPERADOR por padrão

        String senhaEncodada = "senhaEncodada123";
        when(passwordEncoder.encode("123456")).thenReturn(senhaEncodada);
        when(usuarioRepository.existsByEmail("pedro@teste.com")).thenReturn(false);
        when(usuarioRepository.findBySenha("123456")).thenReturn(Optional.empty());
        
        Usuario usuarioSalvo = new Usuario();
        usuarioSalvo.setId(UUID.randomUUID().toString());
        usuarioSalvo.setNome("Pedro Costa");
        usuarioSalvo.setEmail("pedro@teste.com");
        usuarioSalvo.setCpf("111.222.333-44");
        usuarioSalvo.setSenha(senhaEncodada);
        usuarioSalvo.setPerfilAcesso(PerfilAcesso.OPERADOR);
        
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioSalvo);

        // When
        Usuario result = usuarioService.salvar(novoUsuario);

        // Then
        assertNotNull(result);
        assertEquals("Pedro Costa", result.getNome());
        assertEquals("pedro@teste.com", result.getEmail());
        assertEquals("111.222.333-44", result.getCpf());
        assertEquals(senhaEncodada, result.getSenha());
        assertEquals(PerfilAcesso.OPERADOR, result.getPerfilAcesso());

        verify(passwordEncoder).encode("123456");
        verify(usuarioRepository).existsByEmail("pedro@teste.com");
        verify(usuarioRepository).findBySenha("123456");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Não deve salvar usuário com email já existente")
    void naoDeveSalvarUsuarioComEmailExistente() {
        // Given
        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome("Carlos Silva");
        novoUsuario.setEmail("joao@teste.com"); // Email já existe
        novoUsuario.setCpf("555.666.777-88");
        novoUsuario.setSenha("abc123");

        when(usuarioRepository.existsByEmail("joao@teste.com")).thenReturn(true);

        // When & Then
        SmartValidityException exception = assertThrows(SmartValidityException.class, 
            () -> usuarioService.salvar(novoUsuario));

        assertEquals("Não pode utilizar um e-mail já cadastrado!", exception.getMessage());
        verify(usuarioRepository).existsByEmail("joao@teste.com");
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve verificar hash da senha antes da persistência")
    void deveVerificarHashDaSenhaAntesDaPersistencia() throws SmartValidityException {
        // Given
        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome("Ana Costa");
        novoUsuario.setEmail("ana@teste.com");
        novoUsuario.setCpf("999.888.777-66");
        novoUsuario.setSenha("minhasenha");

        String senhaEncodada = "senhaHasheada456";
        when(usuarioRepository.existsByEmail("ana@teste.com")).thenReturn(false);
        when(usuarioRepository.findBySenha("minhasenha")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("minhasenha")).thenReturn(senhaEncodada);
        
        Usuario usuarioSalvo = new Usuario();
        usuarioSalvo.setSenha(senhaEncodada);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioSalvo);

        // When
        Usuario result = usuarioService.salvar(novoUsuario);

        // Then
        assertEquals(senhaEncodada, result.getSenha());
        verify(passwordEncoder).encode("minhasenha");
        verify(usuarioRepository).findBySenha("minhasenha");
    }

    @Test
    @DisplayName("Não deve codificar senha se já estiver hashada")
    void naoDeveCodificarSenhaSeJaEstiverHashada() throws SmartValidityException {
        // Given
        String senhaJaHashada = "senhaJaHashada123";
        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome("Lucas Pereira");
        novoUsuario.setEmail("lucas@teste.com");
        novoUsuario.setCpf("444.555.666-77");
        novoUsuario.setSenha(senhaJaHashada);

        when(usuarioRepository.existsByEmail("lucas@teste.com")).thenReturn(false);
        when(usuarioRepository.findBySenha(senhaJaHashada)).thenReturn(Optional.of(usuarioValido));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(novoUsuario);

        // When
        Usuario result = usuarioService.salvar(novoUsuario);

        // Then
        verify(passwordEncoder, never()).encode(anyString());
        verify(usuarioRepository).findBySenha(senhaJaHashada);
    }

    @Test
    @DisplayName("Deve alterar usuário existente")
    void deveAlterarUsuarioExistente() throws SmartValidityException {
        // Given
        String idUsuario = usuarioValido.getId();
        Usuario usuarioAtualizado = new Usuario();
        usuarioAtualizado.setNome("João Silva Atualizado");
        usuarioAtualizado.setEmail("joao.novo@teste.com");
        usuarioAtualizado.setPerfilAcesso(PerfilAcesso.ADMIN);

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuarioValido));
        doNothing().when(authorizationService).verifiarCredenciaisUsuario(idUsuario);
        when(usuarioRepository.existsByEmailAndIdNot("joao.novo@teste.com", idUsuario)).thenReturn(false);
        when(usuarioRepository.findBySenha(any())).thenReturn(Optional.of(usuarioValido));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioAtualizado);

        // When
        Usuario result = usuarioService.alterar(idUsuario, usuarioAtualizado);

        // Then
        assertNotNull(result);
        verify(authorizationService).verifiarCredenciaisUsuario(idUsuario);
        verify(usuarioRepository).findById(idUsuario);
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Não deve alterar usuário com ID inexistente")
    void naoDeveAlterarUsuarioComIdInexistente() {
        // Given
        String idInexistente = UUID.randomUUID().toString();
        Usuario usuarioAtualizado = new Usuario();
        usuarioAtualizado.setNome("Nome Qualquer");

        when(usuarioRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // When & Then
        SmartValidityException exception = assertThrows(SmartValidityException.class,
            () -> usuarioService.alterar(idInexistente, usuarioAtualizado));

        assertEquals("Usuário não encontrado", exception.getMessage());
        verify(usuarioRepository).findById(idInexistente);
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve buscar usuário por ID com sucesso")
    void deveBuscarUsuarioPorIdComSucesso() throws SmartValidityException {
        // Given
        String id = usuarioValido.getId();
        doNothing().when(authorizationService).verificarPerfilAcesso();
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuarioValido));

        // When
        Usuario result = usuarioService.buscarPorId(id);

        // Then
        assertNotNull(result);
        assertEquals(usuarioValido.getId(), result.getId());
        assertEquals(usuarioValido.getNome(), result.getNome());
        assertEquals(usuarioValido.getEmail(), result.getEmail());

        verify(authorizationService).verificarPerfilAcesso();
        verify(usuarioRepository).findById(id);
    }

    @Test
    @DisplayName("Deve carregar usuário por username (email) para autenticação")
    void deveCarregarUsuarioPorUsername() {
        // Given
        String email = "joao@teste.com";
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuarioValido));

        // When
        var userDetails = usuarioService.loadUserByUsername(email);

        // Then
        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
        assertEquals(usuarioValido.getSenha(), userDetails.getPassword());
        assertFalse(userDetails.getAuthorities().isEmpty());

        verify(usuarioRepository).findByEmail(email);
    }

    @Test
    @DisplayName("Deve lançar exceção ao carregar usuário inexistente")
    void deveLancarExcecaoAoCarregarUsuarioInexistente() {
        // Given
        String emailInexistente = "inexistente@teste.com";
        when(usuarioRepository.findByEmail(emailInexistente))
            .thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
            () -> usuarioService.loadUserByUsername(emailInexistente));

        assertTrue(exception.getMessage().contains("Usuário não encontrado"));
        verify(usuarioRepository).findByEmail(emailInexistente);
    }

    @Test
    @DisplayName("Deve listar todos os usuários com autorização")
    void deveListarTodosUsuariosComAutorizacao() throws SmartValidityException {
        // Given
        List<Usuario> usuarios = Arrays.asList(usuarioValido, usuarioComEmailExistente);
        doNothing().when(authorizationService).verificarPerfilAcesso();
        when(usuarioRepository.findAll()).thenReturn(usuarios);

        // When
        List<Usuario> result = usuarioService.listarTodos();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(usuarioValido));
        assertTrue(result.contains(usuarioComEmailExistente));

        verify(authorizationService).verificarPerfilAcesso();
        verify(usuarioRepository).findAll();
    }

    @Test
    @DisplayName("Deve buscar usuários com seletor sem paginação")
    void deveBuscarUsuariosComSeletorSemPaginacao() throws SmartValidityException {
        // Given
        UsuarioSeletor seletor = new UsuarioSeletor();
        seletor.setNome("João");
        
        List<Usuario> usuarios = Arrays.asList(usuarioValido);
        doNothing().when(authorizationService).verificarPerfilAcesso();
        when(usuarioRepository.findAll(seletor)).thenReturn(usuarios);

        // When
        List<Usuario> result = usuarioService.buscarComSeletor(seletor);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(usuarioValido, result.get(0));

        verify(authorizationService).verificarPerfilAcesso();
        verify(usuarioRepository).findAll(seletor);
    }

    @Test
    @DisplayName("Deve buscar usuários com seletor com paginação")
    void deveBuscarUsuariosComSeletorComPaginacao() throws SmartValidityException {
        // Given
        UsuarioSeletor seletor = new UsuarioSeletor();
        seletor.setPagina(1);
        seletor.setLimite(10);
        
        List<Usuario> usuarios = Arrays.asList(usuarioValido);
        Page<Usuario> page = new PageImpl<>(usuarios);
        
        doNothing().when(authorizationService).verificarPerfilAcesso();
        when(usuarioRepository.findAll(eq(seletor), any(PageRequest.class))).thenReturn(page);

        // When
        List<Usuario> result = usuarioService.buscarComSeletor(seletor);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(usuarioValido, result.get(0));

        verify(authorizationService).verificarPerfilAcesso();
        verify(usuarioRepository).findAll(eq(seletor), any(PageRequest.class));
    }

    @Test
    @DisplayName("Deve excluir usuário com autorização")
    void deveExcluirUsuarioComAutorizacao() throws SmartValidityException {
        // Given
        String idUsuario = usuarioValido.getId();
        doNothing().when(authorizationService).verifiarCredenciaisUsuario(idUsuario);

        // When
        usuarioService.excluir(idUsuario);

        // Then
        verify(authorizationService).verifiarCredenciaisUsuario(idUsuario);
        verify(usuarioRepository).deleteById(idUsuario);
    }
} 