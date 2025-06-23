package br.com.smartvalidity.java.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.smartvalidity.controller.UsuarioController;
import br.com.smartvalidity.model.entity.Usuario;
import br.com.smartvalidity.model.seletor.UsuarioSeletor;
import br.com.smartvalidity.service.UsuarioService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do UsuarioController")
class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(usuarioController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Deve buscar usuários com seletor")
    void deveBuscarUsuariosComSeletor() throws Exception {
        // Given
        UsuarioSeletor seletor = new UsuarioSeletor();
        seletor.setNome("Test");

        Usuario usuario1 = new Usuario();
        usuario1.setId("1");
        usuario1.setNome("Usuario Test 1");

        Usuario usuario2 = new Usuario();
        usuario2.setId("2");
        usuario2.setNome("Usuario Test 2");

        List<Usuario> usuarios = Arrays.asList(usuario1, usuario2);
        when(usuarioService.buscarComSeletor(any(UsuarioSeletor.class))).thenReturn(usuarios);

        // When & Then
        mockMvc.perform(post("/usuario/filtro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(seletor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[1].id").value("2"));

        verify(usuarioService).buscarComSeletor(any(UsuarioSeletor.class));
    }

    @Test
    @DisplayName("Deve buscar todos os usuários")
    void deveBuscarTodosUsuarios() throws Exception {
        // Given
        Usuario usuario1 = new Usuario();
        usuario1.setId("1");
        usuario1.setNome("Usuario 1");

        Usuario usuario2 = new Usuario();
        usuario2.setId("2");
        usuario2.setNome("Usuario 2");

        List<Usuario> usuarios = Arrays.asList(usuario1, usuario2);
        when(usuarioService.listarTodos()).thenReturn(usuarios);

        // When & Then
        mockMvc.perform(get("/usuario"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[1].id").value("2"));

        verify(usuarioService).listarTodos();
    }

    @Test
    @DisplayName("Deve buscar usuário por ID")
    void deveBuscarUsuarioPorId() throws Exception {
        // Given
        Usuario usuario = new Usuario();
        usuario.setId("1");
        usuario.setNome("Usuario Test");

        when(usuarioService.buscarPorId("1")).thenReturn(usuario);

        // When & Then
        mockMvc.perform(get("/usuario/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.nome").value("Usuario Test"));

        verify(usuarioService).buscarPorId("1");
    }

    @Test
    @DisplayName("Deve alterar usuário com sucesso")
    void deveAlterarUsuarioComSucesso() throws Exception {
        // Given
        Usuario usuario = new Usuario();
        usuario.setNome("Usuario Alterado");

        Usuario usuarioAlterado = new Usuario();
        usuarioAlterado.setId("1");
        usuarioAlterado.setNome("Usuario Alterado");

        when(usuarioService.alterar(eq("1"), any(Usuario.class))).thenReturn(usuarioAlterado);

        // When & Then
        mockMvc.perform(put("/usuario/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usuario)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Usuario Alterado"));

        verify(usuarioService).alterar(eq("1"), any(Usuario.class));
    }

    @Test
    @DisplayName("Deve excluir usuário com sucesso")
    void deveExcluirUsuarioComSucesso() throws Exception {
        // Given
        doNothing().when(usuarioService).excluir("1");

        // When & Then
        mockMvc.perform(delete("/usuario/1"))
                .andExpect(status().isNoContent());

        verify(usuarioService).excluir("1");
    }
} 