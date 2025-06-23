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

import br.com.smartvalidity.controller.CategoriaController;
import br.com.smartvalidity.model.entity.Categoria;
import br.com.smartvalidity.model.entity.Corredor;
import br.com.smartvalidity.service.CategoriaService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do CategoriaController")
class CategoriaControllerTest {

    @Mock
    private CategoriaService categoriaService;

    @InjectMocks
    private CategoriaController categoriaController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(categoriaController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Deve buscar todas as categorias")
    void deveBuscarTodasCategorias() throws Exception {
        // Given
        Categoria categoria1 = new Categoria();
        categoria1.setId("1");
        categoria1.setNome("Categoria 1");

        Categoria categoria2 = new Categoria();
        categoria2.setId("2");
        categoria2.setNome("Categoria 2");

        List<Categoria> categorias = Arrays.asList(categoria1, categoria2);
        when(categoriaService.buscarTodas()).thenReturn(categorias);

        // When & Then
        mockMvc.perform(get("/categoria"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[1].id").value("2"));

        verify(categoriaService).buscarTodas();
    }

    @Test
    @DisplayName("Deve buscar categoria por ID")
    void deveBuscarCategoriaPorId() throws Exception {
        // Given
        Categoria categoria = new Categoria();
        categoria.setId("1");
        categoria.setNome("Categoria Test");

        when(categoriaService.buscarPorId("1")).thenReturn(categoria);

        // When & Then
        mockMvc.perform(get("/categoria/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.nome").value("Categoria Test"));

        verify(categoriaService).buscarPorId("1");
    }

    @Test
    @DisplayName("Deve salvar categoria com sucesso")
    void deveSalvarCategoriaComSucesso() throws Exception {
        // Given
        Categoria categoria = new Categoria();
        categoria.setNome("Nova Categoria");

        Categoria categoriaSalva = new Categoria();
        categoriaSalva.setId("1");
        categoriaSalva.setNome("Nova Categoria");

        when(categoriaService.salvar(any(Categoria.class))).thenReturn(categoriaSalva);

        // When & Then
        mockMvc.perform(post("/categoria")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoria)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.nome").value("Nova Categoria"));

        verify(categoriaService).salvar(any(Categoria.class));
    }

    @Test
    @DisplayName("Deve atualizar categoria com sucesso")
    void deveAtualizarCategoriaComSucesso() throws Exception {
        // Given
        Categoria categoria = new Categoria();
        categoria.setNome("Categoria Atualizada");

        Categoria categoriaAtualizada = new Categoria();
        categoriaAtualizada.setId("1");
        categoriaAtualizada.setNome("Categoria Atualizada");

        when(categoriaService.atualizar(eq("1"), any(Categoria.class))).thenReturn(categoriaAtualizada);

        // When & Then
        mockMvc.perform(put("/categoria/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoria)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Categoria Atualizada"));

        verify(categoriaService).atualizar(eq("1"), any(Categoria.class));
    }

    @Test
    @DisplayName("Deve buscar ID do corredor da categoria")
    void deveBuscarIdCorredorDaCategoria() throws Exception {
        // Given
        Corredor corredor = new Corredor();
        corredor.setId("corredor-1");

        Categoria categoria = new Categoria();
        categoria.setId("1");
        categoria.setCorredor(corredor);

        when(categoriaService.buscarPorId("1")).thenReturn(categoria);

        // When & Then
        mockMvc.perform(get("/categoria/1/corredor"))
                .andExpect(status().isOk())
                .andExpect(content().string("corredor-1"));

        verify(categoriaService).buscarPorId("1");
    }

    @Test
    @DisplayName("Deve excluir categoria com sucesso")
    void deveExcluirCategoriaComSucesso() throws Exception {
        // Given
        doNothing().when(categoriaService).excluir("1");

        // When & Then
        mockMvc.perform(delete("/categoria/1"))
                .andExpect(status().isNoContent());

        verify(categoriaService).excluir("1");
    }
} 