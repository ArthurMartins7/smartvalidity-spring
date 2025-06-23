package br.com.smartvalidity.java.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.smartvalidity.model.dto.ProdutoDTO;
import br.com.smartvalidity.service.BaixaValidataService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do BaixaValidataService")
class BaixaValidataServiceTest {

    @InjectMocks
    private BaixaValidataService baixaValidataService;

    private ProdutoDTO produtoDTO;

    @BeforeEach
    void setUp() {
        produtoDTO = new ProdutoDTO();
        produtoDTO.setId(UUID.randomUUID());
        produtoDTO.setDescricao("Produto Test");
        produtoDTO.setCodigoBarras("1234567890123");
        produtoDTO.setMarca("Marca Test");
        produtoDTO.setUnidadeMedida("UN");
        produtoDTO.setQuantidade(10);
    }

    @Test
    @DisplayName("Deve retornar produto com baixa de estoque")
    void deveRetornarProdutoComBaixaDeEstoque() {
        ProdutoDTO resultado = baixaValidataService.getBaixaEstoque(produtoDTO);

        assertNotNull(resultado);
        assertEquals(produtoDTO.getId(), resultado.getId());
        assertEquals(produtoDTO.getDescricao(), resultado.getDescricao());
        assertEquals(produtoDTO.getMarca(), resultado.getMarca());
        assertEquals(produtoDTO, resultado);
    }

    @Test
    @DisplayName("Deve processar produto com valores nulos")
    void deveProcessarProdutoComValoresNulos() {
        ProdutoDTO produtoNulo = new ProdutoDTO();
        
        ProdutoDTO resultado = baixaValidataService.getBaixaEstoque(produtoNulo);

        assertNotNull(resultado);
        assertEquals(produtoNulo, resultado);
    }

    @Test
    @DisplayName("Deve processar produto completamente preenchido")
    void deveProcessarProdutoCompletamentePreenchido() {
        produtoDTO.setCodigoBarras("1234567890123");
        produtoDTO.setMarca("Marca Test");
        
        ProdutoDTO resultado = baixaValidataService.getBaixaEstoque(produtoDTO);

        assertNotNull(resultado);
        assertEquals(produtoDTO.getCodigoBarras(), resultado.getCodigoBarras());
        assertEquals(produtoDTO.getMarca(), resultado.getMarca());
        assertEquals(produtoDTO, resultado);
    }
} 