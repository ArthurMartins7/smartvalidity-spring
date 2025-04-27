//package br.com.smartvalidity.java.model.repository;
//
//import br.com.smartvalidity.model.entity.Categoria;
//import br.com.smartvalidity.model.repository.CategoriaRepository;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//
//import jakarta.validation.ConstraintViolationException;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//@ActiveProfiles("test")
//public class CategoriaRepositoryTest {
//
//    @Autowired
//    private CategoriaRepository categoriaRepository;
//
//    private Categoria categoriaTest;
//
//    @BeforeEach
//    public void setUp() {
//        categoriaTest = new Categoria();
//        categoriaTest.setNome("Bebidas");
//        categoriaRepository.saveAndFlush(categoriaTest);
//    }
//
//    @AfterEach
//    public void tearDown() {
//        categoriaRepository.deleteAll();
//    }
//
//    @Test
//    @DisplayName("Deve criar uma categoria com sucesso")
//    public void testCriarCategoriaSucesso() {
//        Categoria novaCategoria = new Categoria();
//        novaCategoria.setNome("Laticínios");
//
//        Categoria categoriaSalva = categoriaRepository.saveAndFlush(novaCategoria);
//
//        assertNotNull(categoriaSalva);
//        assertEquals("Laticínios", categoriaSalva.getNome());
//    }
//
//    @Test
//    @DisplayName("Deve lançar exceção ao criar categoria com nome vazio")
//    public void testCriarCategoriaNomeVazio() {
//        Categoria categoriaInvalida = new Categoria();
//        categoriaInvalida.setNome("");
//
//        assertThatThrownBy(() -> categoriaRepository.saveAndFlush(categoriaInvalida))
//                .isInstanceOf(ConstraintViolationException.class)
//                .hasMessageContaining("O nome não pode ser vazio ou apenas espaços em branco.");
//    }
//
//    @Test
//    @DisplayName("Deve buscar categoria por ID com sucesso")
//    public void testBuscarCategoriaPorId() {
//        Categoria categoriaEncontrada = categoriaRepository.findById(categoriaTest.getId()).orElse(null);
//        assertNotNull(categoriaEncontrada);
//        assertEquals(categoriaTest.getNome(), categoriaEncontrada.getNome());
//    }
//
//    @Test
//    @DisplayName("Deve retornar lista vazia quando não houver categorias")
//    public void testBuscarTodasCategoriasVazio() {
//        categoriaRepository.deleteAll();
//        List<Categoria> categorias = categoriaRepository.findAll();
//        assertTrue(categorias.isEmpty());
//    }
//
//    @Test
//    @DisplayName("Deve excluir uma categoria com sucesso")
//    public void testExcluirCategoriaSucesso() {
//        categoriaRepository.deleteById(categoriaTest.getId());
//        assertFalse(categoriaRepository.findById(categoriaTest.getId()).isPresent());
//    }
//}
