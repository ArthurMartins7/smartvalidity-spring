package br.com.smartvalidity.java.model.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.smartvalidity.model.dto.ProdutoDTO;

@DisplayName("Testes do ProdutoDTO")
class ProdutoDTOTest {

    @Test
    @DisplayName("Deve criar ProdutoDTO com valores")
    void deveCriarProdutoDTOComValores() {
        // Given
        UUID id = UUID.randomUUID();
        String codigoBarras = "1234567890123";
        String descricao = "Produto Test";
        String marca = "Marca Test";
        String unidadeMedida = "UN";
        int quantidade = 10;

        // When
        ProdutoDTO produto = new ProdutoDTO();
        produto.setId(id);
        produto.setCodigoBarras(codigoBarras);
        produto.setDescricao(descricao);
        produto.setMarca(marca);
        produto.setUnidadeMedida(unidadeMedida);
        produto.setQuantidade(quantidade);

        // Then
        assertEquals(id, produto.getId());
        assertEquals(codigoBarras, produto.getCodigoBarras());
        assertEquals(descricao, produto.getDescricao());
        assertEquals(marca, produto.getMarca());
        assertEquals(unidadeMedida, produto.getUnidadeMedida());
        assertEquals(quantidade, produto.getQuantidade());
    }

    @Test
    @DisplayName("Deve criar ProdutoDTO vazio")
    void deveCriarProdutoDTOVazio() {
        // When
        ProdutoDTO produto = new ProdutoDTO();

        // Then
        assertNull(produto.getId());
        assertNull(produto.getCodigoBarras());
        assertNull(produto.getDescricao());
        assertNull(produto.getMarca());
        assertNull(produto.getUnidadeMedida());
        assertEquals(0, produto.getQuantidade());
    }
} 