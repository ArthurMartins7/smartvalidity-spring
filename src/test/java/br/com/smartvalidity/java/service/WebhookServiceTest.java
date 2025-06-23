package br.com.smartvalidity.java.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.dto.EstoqueDTO;
import br.com.smartvalidity.model.entity.Produto;
import br.com.smartvalidity.service.ItemProdutoService;
import br.com.smartvalidity.service.WebhookService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do WebhookService")
class WebhookServiceTest {

    @Mock
    private ItemProdutoService itemProdutoService;

    @InjectMocks
    private WebhookService webhookService;

    private EstoqueDTO estoqueDTO;
    private Produto produto;

    @BeforeEach
    void setUp() {
        produto = new Produto();
        produto.setDescricao("Produto Test");
        
        estoqueDTO = new EstoqueDTO();
        estoqueDTO.setId("1");
        estoqueDTO.setLote("LOTE123");
        estoqueDTO.setDataFabricacao(LocalDateTime.now().minusDays(30));
        estoqueDTO.setDataVencimento(LocalDateTime.now().plusDays(30));
        estoqueDTO.setDataRecebimento(LocalDateTime.now().minusDays(20));
        estoqueDTO.setPrecoVenda(25.50);
        estoqueDTO.setProduto(produto);
    }

    @Test
    @DisplayName("Deve processar produtos vendidos 1 com sucesso")
    void deveProcessarProdutosVendidos1ComSucesso() {
        List<Object> itensVendidos = Arrays.asList("item1", "item2", "item3");

        List<Object> resultado = webhookService.getProdutosVendidos1(itensVendidos);

        assertNotNull(resultado);
        assertEquals(3, resultado.size());
        assertEquals(itensVendidos, resultado);
    }

    @Test
    @DisplayName("Deve processar lista vazia de produtos vendidos 1")
    void deveProcessarListaVaziaDeProdutosVendidos1() {
        List<Object> itensVendidos = Arrays.asList();

        List<Object> resultado = webhookService.getProdutosVendidos1(itensVendidos);

        assertNotNull(resultado);
        assertEquals(0, resultado.size());
    }

    @Test
    @DisplayName("Deve processar produtos vendidos com sucesso")
    void deveProcessarProdutosVendidosComSucesso() throws SmartValidityException {
        List<EstoqueDTO> itensVendidos = Arrays.asList(estoqueDTO);
        doNothing().when(itemProdutoService).darBaixaItensVendidos(anyString(), anyInt());

        List<EstoqueDTO> resultado = webhookService.getProdutosVendidos(itensVendidos);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(estoqueDTO, resultado.get(0));
        verify(itemProdutoService).darBaixaItensVendidos("LOTE123", 1);
    }

    @Test
    @DisplayName("Deve processar múltiplos produtos vendidos")
    void deveProcessarMultiplosProdutosVendidos() throws SmartValidityException {
        EstoqueDTO estoqueDTO2 = new EstoqueDTO();
        estoqueDTO2.setId("2");
        estoqueDTO2.setLote("LOTE123");
        estoqueDTO2.setPrecoVenda(15.75);
        
        List<EstoqueDTO> itensVendidos = Arrays.asList(estoqueDTO, estoqueDTO2);
        doNothing().when(itemProdutoService).darBaixaItensVendidos(anyString(), anyInt());

        List<EstoqueDTO> resultado = webhookService.getProdutosVendidos(itensVendidos);

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(itemProdutoService).darBaixaItensVendidos("LOTE123", 2);
    }

    @Test
    @DisplayName("Deve lançar exceção quando serviço de item produto falha")
    void deveLancarExcecaoQuandoServicoDeItemProdutoFalha() throws SmartValidityException {
        List<EstoqueDTO> itensVendidos = Arrays.asList(estoqueDTO);
        doThrow(new SmartValidityException("Erro ao dar baixa")).when(itemProdutoService)
            .darBaixaItensVendidos(anyString(), anyInt());

        SmartValidityException exception = assertThrows(SmartValidityException.class, () -> {
            webhookService.getProdutosVendidos(itensVendidos);
        });

        assertEquals("Erro ao dar baixa", exception.getMessage());
        verify(itemProdutoService).darBaixaItensVendidos("LOTE123", 1);
    }
} 