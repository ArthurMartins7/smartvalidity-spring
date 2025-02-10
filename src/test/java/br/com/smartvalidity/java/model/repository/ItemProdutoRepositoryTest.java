package br.com.smartvalidity.java.model.repository;

import br.com.smartvalidity.model.entity.ItemProduto;
import br.com.smartvalidity.model.entity.Produto;
import br.com.smartvalidity.model.repository.ItemProdutoRepository;
import br.com.smartvalidity.model.repository.ProdutoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class ItemProdutoRepositoryTest {

    @Autowired
    private ItemProdutoRepository itemProdutoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    private Produto produtoTest;
    private ItemProduto itemProdutoTest;

    @BeforeEach
    public void setUp() {

        produtoTest = new Produto();
        produtoTest.setCodigoBarras("7891234567895");
        produtoTest.setDescricao("Refrigerante");
        produtoTest.setMarca("Coca-Cola");
        produtoTest.setUnidadeMedida("L");
        produtoTest.setQuantidade(50);
        produtoRepository.save(produtoTest);

        itemProdutoTest = new ItemProduto();
        itemProdutoTest.setLote("Lote123");
        itemProdutoTest.setPrecoCompra(2.50);
        itemProdutoTest.setPrecoVenda(5.00);
        itemProdutoTest.setDataFabricacao(LocalDateTime.now().minusMonths(3));
        itemProdutoTest.setDataVencimento(LocalDateTime.now().plusMonths(9));
        itemProdutoTest.setDataRecebimento(LocalDateTime.now().minusWeeks(1));
        itemProdutoTest.setProduto(produtoTest);
        itemProdutoRepository.save(itemProdutoTest);
    }

    @AfterEach
    public void tearDown() {
        itemProdutoRepository.deleteAll();
        produtoRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve criar um ItemProduto com sucesso")
    public void testCriarItemProdutoSucesso() {
        ItemProduto novoItem = new ItemProduto();
        novoItem.setLote("Lote456");
        novoItem.setPrecoCompra(3.00);
        novoItem.setPrecoVenda(6.00);
        novoItem.setDataFabricacao(LocalDateTime.now().minusMonths(2));
        novoItem.setDataVencimento(LocalDateTime.now().plusMonths(8));
        novoItem.setDataRecebimento(LocalDateTime.now().minusDays(5));
        novoItem.setProduto(produtoTest);

        ItemProduto itemSalvo = itemProdutoRepository.save(novoItem);
        assertNotNull(itemSalvo);
        assertEquals("Lote456", itemSalvo.getLote());
    }

    @Test
    @DisplayName("Deve encontrar itens de produto pelo ID do produto")
    public void testFindByProdutoId() {
        List<ItemProduto> itens = itemProdutoRepository.findByProdutoId(produtoTest.getId());

        assertFalse(itens.isEmpty());
        for (ItemProduto item : itens) {
            assertEquals(produtoTest.getId(), item.getProduto().getId());
        }
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando nenhum item for encontrado")
    public void testFindByProdutoIdEmpty() {
        List<ItemProduto> itens = itemProdutoRepository.findByProdutoId(9999);
        assertTrue(itens.isEmpty());
    }
}
