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

import br.com.smartvalidity.controller.WebhookController;
import br.com.smartvalidity.model.dto.EstoqueDTO;
import br.com.smartvalidity.model.dto.ProdutoDTO;
import br.com.smartvalidity.service.BaixaValidataService;
import br.com.smartvalidity.service.WebhookService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do WebhookController")
class WebhookControllerTest {

    @Mock
    private BaixaValidataService baixaValidataService;

    @Mock
    private WebhookService webhookService;

    @InjectMocks
    private WebhookController webhookController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(webhookController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Deve processar baixa de estoque")
    void deveProcessarBaixaEstoque() throws Exception {
        // Given
        ProdutoDTO produtoRequest = new ProdutoDTO();
        produtoRequest.setCodigoBarras("1234567890123");
        produtoRequest.setDescricao("Produto Test");

        UUID produtoId = UUID.randomUUID();
        ProdutoDTO produtoResponse = new ProdutoDTO();
        produtoResponse.setId(produtoId);
        produtoResponse.setCodigoBarras("1234567890123");
        produtoResponse.setDescricao("Produto Test");

        when(baixaValidataService.getBaixaEstoque(any(ProdutoDTO.class))).thenReturn(produtoResponse);

        // When & Then
        mockMvc.perform(post("/public/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(produtoRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(produtoId.toString()))
                .andExpect(jsonPath("$.codigoBarras").value("1234567890123"));

        verify(baixaValidataService).getBaixaEstoque(any(ProdutoDTO.class));
    }

    @Test
    @DisplayName("Deve processar itens vendidos")
    void deveProcessarItensVendidos() throws Exception {
        // Given
        EstoqueDTO item1 = new EstoqueDTO();
        item1.setId("1");
        item1.setLote("LOTE001");

        EstoqueDTO item2 = new EstoqueDTO();
        item2.setId("2");
        item2.setLote("LOTE002");

        List<EstoqueDTO> itensRequest = Arrays.asList(item1, item2);
        List<EstoqueDTO> itensResponse = Arrays.asList(item1, item2);

        when(webhookService.getProdutosVendidos(anyList())).thenReturn(itensResponse);

        // When & Then
        mockMvc.perform(post("/public/webhook/baixa-validata")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itensRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[1].id").value("2"));

        verify(webhookService).getProdutosVendidos(anyList());
    }
} 