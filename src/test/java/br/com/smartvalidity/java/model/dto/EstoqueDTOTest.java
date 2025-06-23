package br.com.smartvalidity.java.model.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.smartvalidity.model.dto.EstoqueDTO;
import br.com.smartvalidity.model.entity.Produto;

@DisplayName("Testes do EstoqueDTO")
class EstoqueDTOTest {

    @Test
    @DisplayName("Deve criar EstoqueDTO com valores")
    void deveCriarEstoqueDTOComValores() {
        // Given
        String id = "123";
        String lote = "LOTE001";
        LocalDateTime dataFabricacao = LocalDateTime.now().minusDays(30);
        LocalDateTime dataVencimento = LocalDateTime.now().plusDays(30);
        LocalDateTime dataRecebimento = LocalDateTime.now().minusDays(20);
        Double precoVenda = 25.50;
        Produto produto = new Produto();

        // When
        EstoqueDTO estoque = new EstoqueDTO();
        estoque.setId(id);
        estoque.setLote(lote);
        estoque.setDataFabricacao(dataFabricacao);
        estoque.setDataVencimento(dataVencimento);
        estoque.setDataRecebimento(dataRecebimento);
        estoque.setPrecoVenda(precoVenda);
        estoque.setProduto(produto);

        // Then
        assertEquals(id, estoque.getId());
        assertEquals(lote, estoque.getLote());
        assertEquals(dataFabricacao, estoque.getDataFabricacao());
        assertEquals(dataVencimento, estoque.getDataVencimento());
        assertEquals(dataRecebimento, estoque.getDataRecebimento());
        assertEquals(precoVenda, estoque.getPrecoVenda());
        assertEquals(produto, estoque.getProduto());
    }

    @Test
    @DisplayName("Deve criar EstoqueDTO vazio")
    void deveCriarEstoqueDTOVazio() {
        // When
        EstoqueDTO estoque = new EstoqueDTO();

        // Then
        assertNull(estoque.getId());
        assertNull(estoque.getLote());
        assertNull(estoque.getDataFabricacao());
        assertNull(estoque.getDataVencimento());
        assertNull(estoque.getDataRecebimento());
        assertNull(estoque.getPrecoVenda());
        assertNull(estoque.getProduto());
    }
} 