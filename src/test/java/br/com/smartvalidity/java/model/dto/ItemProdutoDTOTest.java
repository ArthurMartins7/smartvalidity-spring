package br.com.smartvalidity.java.model.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import br.com.smartvalidity.model.dto.ItemProdutoDTO;

public class ItemProdutoDTOTest {

    @Test
    public void deveInstanciarItemProdutoDTO() {
        // When
        ItemProdutoDTO dto = new ItemProdutoDTO();

        // Then
        assertNotNull(dto);
        assertTrue(dto instanceof ItemProdutoDTO);
    }

    @Test
    public void devePermitirMultiplasInstancias() {
        // When
        ItemProdutoDTO dto1 = new ItemProdutoDTO();
        ItemProdutoDTO dto2 = new ItemProdutoDTO();

        // Then
        assertNotNull(dto1);
        assertNotNull(dto2);
        assertNotEquals(dto1, dto2); // Objetos diferentes
    }
} 