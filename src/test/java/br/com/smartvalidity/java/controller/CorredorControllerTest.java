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

import br.com.smartvalidity.controller.CorredorController;
import br.com.smartvalidity.model.entity.Corredor;
import br.com.smartvalidity.model.seletor.CorredorSeletor;
import br.com.smartvalidity.service.CorredorService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do CorredorController")
class CorredorControllerTest {

    @Mock
    private CorredorService corredorService;

    @InjectMocks
    private CorredorController corredorController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(corredorController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Deve listar todos os corredores")
    void deveListarTodosOsCorredores() throws Exception {
        // Given
        Corredor corredor1 = new Corredor();
        corredor1.setId("1");
        corredor1.setNome("Corredor A");

        Corredor corredor2 = new Corredor();
        corredor2.setId("2");
        corredor2.setNome("Corredor B");

        List<Corredor> corredores = Arrays.asList(corredor1, corredor2);
        when(corredorService.listarTodos()).thenReturn(corredores);

        // When & Then
        mockMvc.perform(get("/corredor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[1].id").value("2"));

        verify(corredorService).listarTodos();
    }

    @Test
    @DisplayName("Deve buscar corredor por ID")
    void deveBuscarCorredorPorId() throws Exception {
        // Given
        Corredor corredor = new Corredor();
        corredor.setId("1");
        corredor.setNome("Corredor Test");

        when(corredorService.buscarPorId("1")).thenReturn(corredor);

        // When & Then
        mockMvc.perform(get("/corredor/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.nome").value("Corredor Test"));

        verify(corredorService).buscarPorId("1");
    }

    @Test
    @DisplayName("Deve pesquisar corredores com filtro")
    void devePesquisarCorredoresComFiltro() throws Exception {
        // Given
        CorredorSeletor seletor = new CorredorSeletor();
        seletor.setNome("A");

        Corredor corredor1 = new Corredor();
        corredor1.setId("1");
        corredor1.setNome("Corredor A");

        List<Corredor> corredores = Arrays.asList(corredor1);
        when(corredorService.pesquisarComSeletor(any(CorredorSeletor.class))).thenReturn(corredores);

        // When & Then
        mockMvc.perform(post("/corredor/filtro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(seletor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(corredorService).pesquisarComSeletor(any(CorredorSeletor.class));
    }

    @Test
    @DisplayName("Deve contar páginas com seletor")
    void deveContarPaginasComSeletor() throws Exception {
        // Given
        CorredorSeletor seletor = new CorredorSeletor();
        seletor.setNome("Test");
        when(corredorService.contarPaginas(any(CorredorSeletor.class))).thenReturn(3);

        // When & Then
        mockMvc.perform(post("/corredor/filtro/paginas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(seletor)))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));

        verify(corredorService).contarPaginas(any(CorredorSeletor.class));
    }

    @Test
    @DisplayName("Deve contar total de registros")
    void deveContarTotalDeRegistros() throws Exception {
        // Given
        CorredorSeletor seletor = new CorredorSeletor();
        when(corredorService.contarTotalRegistros(any(CorredorSeletor.class))).thenReturn(50L);

        // When & Then
        mockMvc.perform(post("/corredor/contar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(seletor)))
                .andExpect(status().isOk())
                .andExpect(content().string("50"));

        verify(corredorService).contarTotalRegistros(any(CorredorSeletor.class));
    }

    @Test
    @DisplayName("Deve excluir corredor com sucesso")
    void deveExcluirCorredorComSucesso() throws Exception {
        // Given
        doNothing().when(corredorService).excluir("1");

        // When & Then
        mockMvc.perform(delete("/corredor/1"))
                .andExpect(status().isNoContent());

        verify(corredorService).excluir("1");
    }
} 