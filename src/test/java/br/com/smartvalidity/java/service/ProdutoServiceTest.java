package br.com.smartvalidity.java.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.entity.Produto;
import br.com.smartvalidity.model.repository.ProdutoRepository;
import br.com.smartvalidity.service.ProdutoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private ProdutoService produtoService;

    private Produto produto;

    @BeforeEach
    public void setUp() {
        produto = new Produto();
        produto.setId(1);
        produto.setCodigoBarras("7891234567895");
        produto.setDescricao("Refrigerante de Cola");
        produto.setMarca("Coca-Cola");
        produto.setUnidadeMedida("L");
        produto.setQuantidade(50);
    }

    @Test
    @DisplayName("Deve salvar um produto com sucesso")
    public void testSalvarProdutoComSucesso() {
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);
        Produto produtoSalvo = produtoService.salvar(produto);

        assertThat(produtoSalvo).isNotNull();
        assertThat(produtoSalvo.getDescricao()).isEqualTo("Refrigerante de Cola");
    }

    @Test
    @DisplayName("Deve buscar um produto por ID com sucesso")
    public void testBuscarProdutoPorId() throws SmartValidityException {
        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
        Produto produtoEncontrado = produtoService.buscarPorId(1);

        assertThat(produtoEncontrado).isNotNull();
        assertThat(produtoEncontrado.getId()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar um produto inexistente")
    public void testBuscarProdutoInexistente() {
        when(produtoRepository.findById(2)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> produtoService.buscarPorId(2))
                .isInstanceOf(SmartValidityException.class)
                .hasMessageContaining("Produto não encontrado com o ID: 2");
    }

    @Test
    @DisplayName("Deve listar todos os produtos")
    public void testListarTodosProdutos() {
        List<Produto> listaProdutos = new ArrayList<>();
        listaProdutos.add(produto);
        when(produtoRepository.findAll()).thenReturn(listaProdutos);

        List<Produto> produtosRetornados = produtoService.listarTodos();

        assertThat(produtosRetornados).isNotEmpty();
        assertThat(produtosRetornados.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve atualizar um produto com sucesso")
    public void testAtualizarProduto() throws SmartValidityException {
        Produto produtoAtualizado = new Produto();
        produtoAtualizado.setCodigoBarras("7899876543210");
        produtoAtualizado.setDescricao("Suco de Laranja");
        produtoAtualizado.setMarca("Del Valle");
        produtoAtualizado.setUnidadeMedida("ML");
        produtoAtualizado.setQuantidade(30);

        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
        when(produtoRepository.save(any(Produto.class))).thenReturn(produtoAtualizado);

        Produto resultado = produtoService.atualizar(1, produtoAtualizado);

        assertThat(resultado.getDescricao()).isEqualTo("Suco de Laranja");
    }

    @Test
    @DisplayName("Deve excluir um produto com sucesso")
    public void testExcluirProdutoComSucesso() throws SmartValidityException {
        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
        doNothing().when(produtoRepository).delete(produto);

        produtoService.excluir(1);

        verify(produtoRepository, times(1)).delete(produto);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar excluir um produto inexistente")
    public void testExcluirProdutoInexistente() {
        when(produtoRepository.findById(2)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> produtoService.excluir(2))
                .isInstanceOf(SmartValidityException.class)
                .hasMessageContaining("Produto não encontrado com o ID: 2");
    }
}
