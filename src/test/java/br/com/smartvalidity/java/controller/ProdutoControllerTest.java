package br.com.smartvalidity.java.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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

import br.com.smartvalidity.controller.ProdutoController;
import br.com.smartvalidity.model.dto.ProdutoDTO;
import br.com.smartvalidity.model.entity.Produto;
import br.com.smartvalidity.model.seletor.ProdutoSeletor;
import br.com.smartvalidity.service.ProdutoService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do ProdutoController")
class ProdutoControllerTest {

    @Mock
    private ProdutoService produtoService;

    @InjectMocks
    private ProdutoController produtoController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(produtoController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Deve listar todos os produtos")
    void deveListarTodosProdutos() throws Exception {
        // Given
        ProdutoDTO produto1 = new ProdutoDTO();
        produto1.setId(UUID.randomUUID());
        produto1.setDescricao("Produto 1");

        ProdutoDTO produto2 = new ProdutoDTO();
        produto2.setId(UUID.randomUUID());
        produto2.setDescricao("Produto 2");

        List<ProdutoDTO> produtos = Arrays.asList(produto1, produto2);
        when(produtoService.listarTodosDTO()).thenReturn(produtos);

        // When & Then
        mockMvc.perform(get("/produto"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(produtoService).listarTodosDTO();
    }

    @Test
    @DisplayName("Deve pesquisar produtos com seletor")
    void devePesquisarProdutosComSeletor() throws Exception {
        // Given
        ProdutoSeletor seletor = new ProdutoSeletor();
        seletor.setDescricao("Test");

        Produto produto1 = new Produto();
        produto1.setId("1");
        produto1.setDescricao("Produto Test");

        List<Produto> produtos = Arrays.asList(produto1);
        when(produtoService.pesquisarComSeletor(any(ProdutoSeletor.class))).thenReturn(produtos);

        // When & Then
        mockMvc.perform(post("/produto/filtro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(seletor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(produtoService).pesquisarComSeletor(any(ProdutoSeletor.class));
    }

    @Test
    @DisplayName("Deve contar total de registros")
    void deveContarTotalRegistros() throws Exception {
        // Given
        ProdutoSeletor seletor = new ProdutoSeletor();
        when(produtoService.contarTotalRegistros(any(ProdutoSeletor.class))).thenReturn(10L);

        // When & Then
        mockMvc.perform(post("/produto/contar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(seletor)))
                .andExpect(status().isOk())
                .andExpect(content().string("10"));

        verify(produtoService).contarTotalRegistros(any(ProdutoSeletor.class));
    }

    @Test
    @DisplayName("Deve buscar produto por ID")
    void deveBuscarProdutoPorId() throws Exception {
        // Given
        Produto produto = new Produto();
        produto.setId("1");
        produto.setDescricao("Produto Test");

        when(produtoService.buscarPorId("1")).thenReturn(produto);

        // When & Then
        mockMvc.perform(get("/produto/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.descricao").value("Produto Test"));

        verify(produtoService).buscarPorId("1");
    }

    @Test
    @DisplayName("Deve salvar produto com sucesso")
    void deveSalvarProdutoComSucesso() throws Exception {
        // Given
        Produto produto = new Produto();
        produto.setDescricao("Novo Produto");
        produto.setCodigoBarras("4006381333931"); // EAN-13 válido
        produto.setMarca("Marca Test");
        produto.setUnidadeMedida("UN");
        produto.setQuantidade(100);

        Produto produtoSalvo = new Produto();
        produtoSalvo.setId("1");
        produtoSalvo.setDescricao("Novo Produto");
        produtoSalvo.setCodigoBarras("4006381333931"); // EAN-13 válido
        produtoSalvo.setMarca("Marca Test");
        produtoSalvo.setUnidadeMedida("UN");
        produtoSalvo.setQuantidade(100);

        when(produtoService.salvar(any(Produto.class))).thenReturn(produtoSalvo);

        // When & Then
        mockMvc.perform(post("/produto")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(produto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.descricao").value("Novo Produto"));

        verify(produtoService).salvar(any(Produto.class));
    }

    @Test
    @DisplayName("Deve buscar produtos por categoria")
    void deveBuscarProdutosPorCategoria() throws Exception {
        // Given
        Produto produto1 = new Produto();
        produto1.setId("1");
        produto1.setDescricao("Produto da Categoria");

        List<Produto> produtos = Arrays.asList(produto1);
        when(produtoService.buscarPorCategoria("cat1")).thenReturn(produtos);

        // When & Then
        mockMvc.perform(get("/produto/categoria/cat1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("1"));

        verify(produtoService).buscarPorCategoria("cat1");
    }

    @Test
    @DisplayName("Deve contar total de registros com seletor")
    void deveContarTotalRegistrosComSeletor() throws Exception {
        // Given
        ProdutoSeletor seletor = new ProdutoSeletor();
        seletor.setDescricao("Test");
        when(produtoService.contarTotalRegistros(any(ProdutoSeletor.class))).thenReturn(25L);

        // When & Then
        mockMvc.perform(post("/produto/contar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(seletor)))
                .andExpect(status().isOk())
                .andExpect(content().string("25"));

        verify(produtoService).contarTotalRegistros(any(ProdutoSeletor.class));
    }

    @Test
    @DisplayName("Deve atualizar produto com sucesso")
    void deveAtualizarProdutoComSucesso() throws Exception {
        // Given
        Produto produto = new Produto();
        produto.setDescricao("Produto Atualizado");
        produto.setCodigoBarras("4006381333931");
        produto.setMarca("Marca Atualizada");
        produto.setUnidadeMedida("KG");
        produto.setQuantidade(50);

        Produto produtoAtualizado = new Produto();
        produtoAtualizado.setId("1");
        produtoAtualizado.setDescricao("Produto Atualizado");
        produtoAtualizado.setCodigoBarras("4006381333931");
        produtoAtualizado.setMarca("Marca Atualizada");
        produtoAtualizado.setUnidadeMedida("KG");
        produtoAtualizado.setQuantidade(50);

        when(produtoService.atualizar(eq("1"), any(Produto.class))).thenReturn(produtoAtualizado);

        // When & Then
        mockMvc.perform(put("/produto/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(produto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.descricao").value("Produto Atualizado"));

        verify(produtoService).atualizar(eq("1"), any(Produto.class));
    }

    @Test
    @DisplayName("Deve excluir produto com sucesso")
    void deveExcluirProdutoComSucesso() throws Exception {
        // Given
        doNothing().when(produtoService).excluir("1");

        // When & Then
        mockMvc.perform(delete("/produto/1"))
                .andExpect(status().isNoContent());

        verify(produtoService).excluir("1");
    }
} 