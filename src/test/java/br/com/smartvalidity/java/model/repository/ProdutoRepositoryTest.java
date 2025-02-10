package br.com.smartvalidity.java.model.repository;

import br.com.smartvalidity.model.entity.Categoria;
import br.com.smartvalidity.model.entity.Produto;
import br.com.smartvalidity.model.repository.CategoriaRepository;
import br.com.smartvalidity.model.repository.ProdutoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class ProdutoRepositoryTest {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    private Categoria categoriaTest;
    private Produto produtoTest;

    @BeforeEach
    public void setUp() {

        categoriaTest = new Categoria();
        categoriaTest.setNome("Bebidas");
        categoriaRepository.save(categoriaTest);

        produtoTest = new Produto();
        produtoTest.setCodigoBarras("7891234567895");
        produtoTest.setDescricao("Refrigerante de Cola");
        produtoTest.setMarca("Coca-Cola");
        produtoTest.setUnidadeMedida("L");
        produtoTest.setQuantidade(50);
        produtoTest.setCategoria(categoriaTest);
        produtoRepository.save(produtoTest);
    }

    @AfterEach
    public void tearDown() {
        produtoRepository.deleteAll();
        categoriaRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve criar um produto com sucesso")
    public void testCriarProdutoSucesso() {
        Produto novoProduto = new Produto();
        novoProduto.setCodigoBarras("7899876543210");
        novoProduto.setDescricao("Suco de Laranja");
        novoProduto.setMarca("Del Valle");
        novoProduto.setUnidadeMedida("ML");
        novoProduto.setQuantidade(30);
        novoProduto.setCategoria(categoriaTest);

        Produto produtoSalvo = produtoRepository.save(novoProduto);
        assertNotNull(produtoSalvo);
        assertEquals("Suco de Laranja", produtoSalvo.getDescricao());
    }

    @Test
    @DisplayName("Deve encontrar produtos pela categoria")
    public void testFindByCategoria() {
        List<Produto> produtos = produtoRepository.findAll();
        assertFalse(produtos.isEmpty());

        for (Produto produto : produtos) {
            assertEquals(categoriaTest.getId(), produto.getCategoria().getId());
        }
    }
}
