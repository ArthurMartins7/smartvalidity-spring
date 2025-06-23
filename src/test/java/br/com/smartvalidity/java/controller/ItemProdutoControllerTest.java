package br.com.smartvalidity.java.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
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
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import br.com.smartvalidity.controller.ItemProdutoController;
import br.com.smartvalidity.model.entity.ItemProduto;
import br.com.smartvalidity.service.ItemProdutoService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do ItemProdutoController")
class ItemProdutoControllerTest {

    @Mock
    private ItemProdutoService itemProdutoService;

    @InjectMocks
    private ItemProdutoController itemProdutoController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(itemProdutoController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("Deve buscar todos os itens produto")
    void deveBuscarTodosItensProduto() throws Exception {
        // Given
        ItemProduto item1 = new ItemProduto();
        item1.setId("1");
        item1.setLote("LOTE001");

        ItemProduto item2 = new ItemProduto();
        item2.setId("2");
        item2.setLote("LOTE002");

        List<ItemProduto> itens = Arrays.asList(item1, item2);
        when(itemProdutoService.buscarTodos()).thenReturn(itens);

        // When & Then
        mockMvc.perform(get("/item-produto"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[1].id").value("2"));

        verify(itemProdutoService).buscarTodos();
    }

    @Test
    @DisplayName("Deve buscar item produto por ID")
    void deveBuscarItemProdutoPorId() throws Exception {
        // Given
        ItemProduto item = new ItemProduto();
        item.setId("1");
        item.setLote("LOTE001");

        when(itemProdutoService.buscarPorId("1")).thenReturn(item);

        // When & Then
        mockMvc.perform(get("/item-produto/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.lote").value("LOTE001"));

        verify(itemProdutoService).buscarPorId("1");
    }

    @Test
    @DisplayName("Deve buscar itens por produto")
    void deveBuscarItensPorProduto() throws Exception {
        // Given
        ItemProduto item1 = new ItemProduto();
        item1.setId("1");
        item1.setLote("LOTE001");

        List<ItemProduto> itens = Arrays.asList(item1);
        when(itemProdutoService.buscarPorProduto("produto-1")).thenReturn(itens);

        // When & Then
        mockMvc.perform(get("/item-produto/produto/produto-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("1"));

        verify(itemProdutoService).buscarPorProduto("produto-1");
    }

    @Test
    @DisplayName("Deve salvar item produto com sucesso")
    void deveSalvarItemProdutoComSucesso() throws Exception {
        // Given
        ItemProduto item = new ItemProduto();
        item.setLote("LOTE001");
        item.setDataVencimento(LocalDateTime.now().plusDays(30));
        item.setDataFabricacao(LocalDateTime.now().minusDays(1));
        item.setDataRecebimento(LocalDateTime.now());
        item.setPrecoVenda(10.50);

        ItemProduto itemSalvo = new ItemProduto();
        itemSalvo.setId("1");
        itemSalvo.setLote("LOTE001");
        itemSalvo.setDataVencimento(LocalDateTime.now().plusDays(30));
        itemSalvo.setDataFabricacao(LocalDateTime.now().minusDays(1));
        itemSalvo.setDataRecebimento(LocalDateTime.now());
        itemSalvo.setPrecoVenda(10.50);

        when(itemProdutoService.salvar(any(ItemProduto.class))).thenReturn(itemSalvo);

        // When & Then
        mockMvc.perform(post("/item-produto")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.lote").value("LOTE001"));

        verify(itemProdutoService).salvar(any(ItemProduto.class));
    }

    @Test
    @DisplayName("Deve atualizar item produto com sucesso")
    void deveAtualizarItemProdutoComSucesso() throws Exception {
        // Given
        ItemProduto item = new ItemProduto();
        item.setLote("LOTE_ATUALIZADO");
        item.setDataVencimento(LocalDateTime.now().plusDays(45));
        item.setDataFabricacao(LocalDateTime.now().minusDays(2));
        item.setDataRecebimento(LocalDateTime.now().minusDays(1));
        item.setPrecoVenda(15.75);

        ItemProduto itemAtualizado = new ItemProduto();
        itemAtualizado.setId("1");
        itemAtualizado.setLote("LOTE_ATUALIZADO");
        itemAtualizado.setDataVencimento(LocalDateTime.now().plusDays(45));
        itemAtualizado.setDataFabricacao(LocalDateTime.now().minusDays(2));
        itemAtualizado.setDataRecebimento(LocalDateTime.now().minusDays(1));
        itemAtualizado.setPrecoVenda(15.75);

        when(itemProdutoService.atualizar(eq("1"), any(ItemProduto.class))).thenReturn(itemAtualizado);

        // When & Then
        mockMvc.perform(put("/item-produto/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lote").value("LOTE_ATUALIZADO"));

        verify(itemProdutoService).atualizar(eq("1"), any(ItemProduto.class));
    }

    @Test
    @DisplayName("Deve excluir item produto com sucesso")
    void deveExcluirItemProdutoComSucesso() throws Exception {
        // Given
        doNothing().when(itemProdutoService).excluir("1");

        // When & Then
        mockMvc.perform(delete("/item-produto/1"))
                .andExpect(status().isNoContent());

        verify(itemProdutoService).excluir("1");
    }
} 