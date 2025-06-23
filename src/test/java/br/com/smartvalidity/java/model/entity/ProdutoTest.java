package br.com.smartvalidity.java.model.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.com.smartvalidity.model.entity.Categoria;
import br.com.smartvalidity.model.entity.Fornecedor;
import br.com.smartvalidity.model.entity.ItemProduto;
import br.com.smartvalidity.model.entity.Produto;

public class ProdutoTest {

    @Test
    public void deveTestarGettersESetters() {
        // Given
        Categoria categoria = new Categoria();
        categoria.setId("cat-1");
        categoria.setNome("Categoria Teste");

        // When
        Produto produto = new Produto();
        produto.setId("prod-1");
        produto.setCodigoBarras("1234567890123");
        produto.setDescricao("Produto Teste");
        produto.setMarca("Marca Teste");
        produto.setUnidadeMedida("UN");
        produto.setQuantidade(100);
        produto.setCategoria(categoria);

        // Then
        assertEquals("prod-1", produto.getId());
        assertEquals("1234567890123", produto.getCodigoBarras());
        assertEquals("Produto Teste", produto.getDescricao());
        assertEquals("Marca Teste", produto.getMarca());
        assertEquals("UN", produto.getUnidadeMedida());
        assertEquals(100, produto.getQuantidade());
        assertEquals(categoria, produto.getCategoria());
    }

    @Test
    public void deveTestarRelacionamentoComFornecedores() {
        // Given
        Fornecedor fornecedor1 = new Fornecedor();
        fornecedor1.setId(1);
        fornecedor1.setNome("Fornecedor 1");

        Fornecedor fornecedor2 = new Fornecedor();
        fornecedor2.setId(2);
        fornecedor2.setNome("Fornecedor 2");

        List<Fornecedor> fornecedores = Arrays.asList(fornecedor1, fornecedor2);

        Produto produto = new Produto();

        // When
        produto.setFornecedores(fornecedores);

        // Then
        assertNotNull(produto.getFornecedores());
        assertEquals(2, produto.getFornecedores().size());
        assertTrue(produto.getFornecedores().contains(fornecedor1));
        assertTrue(produto.getFornecedores().contains(fornecedor2));
    }

    @Test
    public void deveTestarRelacionamentoComItens() {
        // Given
        ItemProduto item1 = new ItemProduto();
        item1.setId("item-1");
        item1.setLote("LOTE1");

        ItemProduto item2 = new ItemProduto();
        item2.setId("item-2");
        item2.setLote("LOTE2");

        List<ItemProduto> itens = Arrays.asList(item1, item2);

        Produto produto = new Produto();

        // When
        produto.setItensProduto(itens);

        // Then
        assertNotNull(produto.getItensProduto());
        assertEquals(2, produto.getItensProduto().size());
        assertTrue(produto.getItensProduto().contains(item1));
        assertTrue(produto.getItensProduto().contains(item2));
    }

    @Test
    public void deveTestarProdutoCompleto() {
        // Given
        Categoria categoria = new Categoria();
        categoria.setNome("Alimentos");

        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setNome("Fornecedor ABC");

        ItemProduto item = new ItemProduto();
        item.setLote("LOTE123");

        // When
        Produto produto = new Produto();
        produto.setCodigoBarras("7891234567890");
        produto.setDescricao("Arroz Integral 1kg");
        produto.setMarca("Marca Premium");
        produto.setUnidadeMedida("KG");
        produto.setQuantidade(50);
        produto.setCategoria(categoria);
        produto.setFornecedores(Arrays.asList(fornecedor));
        produto.setItensProduto(Arrays.asList(item));

        // Then
        assertEquals("7891234567890", produto.getCodigoBarras());
        assertEquals("Arroz Integral 1kg", produto.getDescricao());
        assertEquals("Marca Premium", produto.getMarca());
        assertEquals("KG", produto.getUnidadeMedida());
        assertEquals(50, produto.getQuantidade());
        assertEquals("Alimentos", produto.getCategoria().getNome());
        assertEquals(1, produto.getFornecedores().size());
        assertEquals("Fornecedor ABC", produto.getFornecedores().get(0).getNome());
        assertEquals(1, produto.getItensProduto().size());
        assertEquals("LOTE123", produto.getItensProduto().get(0).getLote());
    }

    @Test
    public void devePermitirListasVazias() {
        // Given
        Produto produto = new Produto();

        // When
        produto.setFornecedores(Arrays.asList());
        produto.setItensProduto(Arrays.asList());

        // Then
        assertNotNull(produto.getFornecedores());
        assertNotNull(produto.getItensProduto());
        assertTrue(produto.getFornecedores().isEmpty());
        assertTrue(produto.getItensProduto().isEmpty());
    }
} 