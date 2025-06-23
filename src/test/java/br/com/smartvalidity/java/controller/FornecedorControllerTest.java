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

import br.com.smartvalidity.controller.FornecedorController;
import br.com.smartvalidity.model.entity.Fornecedor;
import br.com.smartvalidity.model.seletor.FornecedorSeletor;
import br.com.smartvalidity.service.FornecedorService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do FornecedorController")
class FornecedorControllerTest {

    @Mock
    private FornecedorService fornecedorService;

    @InjectMocks
    private FornecedorController fornecedorController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(fornecedorController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Deve listar todos os fornecedores")
    void deveListarTodosFornecedores() throws Exception {
        // Given
        Fornecedor fornecedor1 = new Fornecedor();
        fornecedor1.setId(1);
        fornecedor1.setNome("Fornecedor 1");

        Fornecedor fornecedor2 = new Fornecedor();
        fornecedor2.setId(2);
        fornecedor2.setNome("Fornecedor 2");

        List<Fornecedor> fornecedores = Arrays.asList(fornecedor1, fornecedor2);
        when(fornecedorService.listarTodos()).thenReturn(fornecedores);

        // When & Then
        mockMvc.perform(get("/fornecedor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(fornecedorService).listarTodos();
    }

    @Test
    @DisplayName("Deve buscar fornecedor por ID")
    void deveBuscarFornecedorPorId() throws Exception {
        // Given
        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setId(1);
        fornecedor.setNome("Fornecedor Test");

        when(fornecedorService.buscarPorId(1)).thenReturn(fornecedor);

        // When & Then
        mockMvc.perform(get("/fornecedor/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Fornecedor Test"));

        verify(fornecedorService).buscarPorId(1);
    }

    @Test
    @DisplayName("Deve pesquisar fornecedores com seletor")
    void devePesquisarFornecedoresComSeletor() throws Exception {
        // Given
        FornecedorSeletor seletor = new FornecedorSeletor();
        seletor.setNome("Test");

        Fornecedor fornecedor1 = new Fornecedor();
        fornecedor1.setId(1);
        fornecedor1.setNome("Fornecedor Test");

        List<Fornecedor> fornecedores = Arrays.asList(fornecedor1);
        when(fornecedorService.pesquisarComSeletor(any(FornecedorSeletor.class))).thenReturn(fornecedores);

        // When & Then
        mockMvc.perform(post("/fornecedor/filtro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(seletor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(fornecedorService).pesquisarComSeletor(any(FornecedorSeletor.class));
    }

    @Test
    @DisplayName("Deve contar total de registros")
    void deveContarTotalRegistros() throws Exception {
        // Given
        FornecedorSeletor seletor = new FornecedorSeletor();
        when(fornecedorService.contarTotalRegistros(any(FornecedorSeletor.class))).thenReturn(10L);

        // When & Then
        mockMvc.perform(post("/fornecedor/contar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(seletor)))
                .andExpect(status().isOk())
                .andExpect(content().string("10"));

        verify(fornecedorService).contarTotalRegistros(any(FornecedorSeletor.class));
    }

    @Test
    @DisplayName("Deve salvar fornecedor com sucesso")
    void deveSalvarFornecedorComSucesso() throws Exception {
        // Given
        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setNome("Novo Fornecedor");
        fornecedor.setCnpj("11222333000181");
        fornecedor.setTelefone("11999999999");

        Fornecedor fornecedorSalvo = new Fornecedor();
        fornecedorSalvo.setId(1);
        fornecedorSalvo.setNome("Novo Fornecedor");
        fornecedorSalvo.setCnpj("11222333000181");
        fornecedorSalvo.setTelefone("11999999999");

        when(fornecedorService.salvar(any(Fornecedor.class))).thenReturn(fornecedorSalvo);

        // When & Then
        mockMvc.perform(post("/fornecedor")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fornecedor)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Novo Fornecedor"));

        verify(fornecedorService).salvar(any(Fornecedor.class));
    }

    @Test
    @DisplayName("Deve excluir fornecedor com sucesso")
    void deveExcluirFornecedorComSucesso() throws Exception {
        // Given
        doNothing().when(fornecedorService).excluir(1);

        // When & Then
        mockMvc.perform(delete("/fornecedor/1"))
                .andExpect(status().isNoContent());

        verify(fornecedorService).excluir(1);
    }
} 