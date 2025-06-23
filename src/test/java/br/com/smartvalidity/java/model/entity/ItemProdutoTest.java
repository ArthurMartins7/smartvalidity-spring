package br.com.smartvalidity.java.model.entity;

import br.com.smartvalidity.model.entity.ItemProduto;
import br.com.smartvalidity.model.entity.Produto;
import br.com.smartvalidity.model.enums.SituacaoValidade;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class ItemProdutoTest {

    @Test
    public void deveTestarGettersESetters() {
        // Given
        LocalDateTime agora = LocalDateTime.now();
        Produto produto = new Produto();
        produto.setId("prod-1");

        // When
        ItemProduto item = new ItemProduto();
        item.setId("item-1");
        item.setLote("LOTE123");
        item.setPrecoVenda(15.50);
        item.setDataFabricacao(agora.minusDays(30));
        item.setDataVencimento(agora.plusDays(30));
        item.setDataRecebimento(agora.minusDays(20));
        item.setInspecionado(false);
        item.setMotivoInspecao(null);
        item.setUsuarioInspecao(null);
        item.setDataHoraInspecao(null);
        item.setSituacaoValidade(SituacaoValidade.OK);
        item.setProduto(produto);

        // Then
        assertEquals("item-1", item.getId());
        assertEquals("LOTE123", item.getLote());
        assertEquals(15.50, item.getPrecoVenda());
        assertEquals(agora.minusDays(30), item.getDataFabricacao());
        assertEquals(agora.plusDays(30), item.getDataVencimento());
        assertEquals(agora.minusDays(20), item.getDataRecebimento());
        assertFalse(item.getInspecionado());
        assertNull(item.getMotivoInspecao());
        assertNull(item.getUsuarioInspecao());
        assertNull(item.getDataHoraInspecao());
        assertEquals(SituacaoValidade.OK, item.getSituacaoValidade());
        assertEquals(produto, item.getProduto());
    }

    @Test
    public void deveTestarItemInspecionado() {
        // Given
        LocalDateTime agora = LocalDateTime.now();
        ItemProduto item = new ItemProduto();

        // When
        item.setInspecionado(true);
        item.setMotivoInspecao("Avaria/Quebra");
        item.setUsuarioInspecao("admin");
        item.setDataHoraInspecao(agora);

        // Then
        assertTrue(item.getInspecionado());
        assertEquals("Avaria/Quebra", item.getMotivoInspecao());
        assertEquals("admin", item.getUsuarioInspecao());
        assertEquals(agora, item.getDataHoraInspecao());
    }

    @Test
    public void deveTestarSituacaoValidade() {
        // Given
        ItemProduto item = new ItemProduto();

        // When/Then - Testando diferentes situações
        item.setSituacaoValidade(SituacaoValidade.VENCIDO);
        assertEquals(SituacaoValidade.VENCIDO, item.getSituacaoValidade());

        item.setSituacaoValidade(SituacaoValidade.PROXIMO_DE_VENCER);
        assertEquals(SituacaoValidade.PROXIMO_DE_VENCER, item.getSituacaoValidade());

        item.setSituacaoValidade(SituacaoValidade.VENCE_HOJE);
        assertEquals(SituacaoValidade.VENCE_HOJE, item.getSituacaoValidade());

        item.setSituacaoValidade(SituacaoValidade.OK);
        assertEquals(SituacaoValidade.OK, item.getSituacaoValidade());
    }

    @Test
    public void deveTestarInspecionadoPadrao() {
        // Given/When
        ItemProduto item = new ItemProduto();

        // Then
        assertFalse(item.getInspecionado()); // Valor padrão deve ser false
    }

    @Test
    public void deveTestarRelacionamentoProduto() {
        // Given
        Produto produto = new Produto();
        produto.setId("produto-123");
        produto.setDescricao("Produto Teste");
        produto.setMarca("Marca Teste");

        ItemProduto item = new ItemProduto();

        // When
        item.setProduto(produto);

        // Then
        assertNotNull(item.getProduto());
        assertEquals("produto-123", item.getProduto().getId());
        assertEquals("Produto Teste", item.getProduto().getDescricao());
        assertEquals("Marca Teste", item.getProduto().getMarca());
    }
} 