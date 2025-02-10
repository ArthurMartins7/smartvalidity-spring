package br.com.smartvalidity.java.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.entity.ItemProduto;
import br.com.smartvalidity.model.entity.Produto;
import br.com.smartvalidity.model.repository.ItemProdutoRepository;
import br.com.smartvalidity.service.ItemProdutoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class ItemProdutoServiceTest {

    @Mock
    private ItemProdutoRepository itemProdutoRepository;

    @InjectMocks
    private ItemProdutoService itemProdutoService;

    private ItemProduto itemProduto;
    private Produto produto;

    @BeforeEach
    public void setUp() {
        produto = new Produto();
        produto.setId(1);
        produto.setDescricao("Produto Teste");

        itemProduto = new ItemProduto();
        itemProduto.setId(1);
        itemProduto.setLote("Lote123");
        itemProduto.setPrecoCompra(10.0);
        itemProduto.setPrecoVenda(15.0);
        itemProduto.setDataFabricacao(LocalDateTime.now().minusDays(30));
        itemProduto.setDataVencimento(LocalDateTime.now().plusDays(60));
        itemProduto.setDataRecebimento(LocalDateTime.now().minusDays(5));
        itemProduto.setProduto(produto);
    }

    @Test
    @DisplayName("Deve salvar um ItemProduto com sucesso")
    public void testSalvarItemProdutoComSucesso() {
        when(itemProdutoRepository.save(any(ItemProduto.class))).thenReturn(itemProduto);

        ItemProduto itemSalvo = itemProdutoService.salvar(itemProduto);

        assertThat(itemSalvo).isNotNull();
        assertThat(itemSalvo.getLote()).isEqualTo("Lote123");
        verify(itemProdutoRepository, times(1)).save(itemProduto);
    }

    @Test
    @DisplayName("Deve buscar um ItemProduto por ID com sucesso")
    public void testBuscarItemProdutoPorIdComSucesso() throws SmartValidityException {
        when(itemProdutoRepository.findById(1)).thenReturn(Optional.of(itemProduto));

        ItemProduto itemEncontrado = itemProdutoService.buscarPorId(1);

        assertThat(itemEncontrado).isNotNull();
        assertThat(itemEncontrado.getId()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar um ItemProduto inexistente")
    public void testBuscarItemProdutoPorIdInexistente() {
        when(itemProdutoRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemProdutoService.buscarPorId(999))
                .isInstanceOf(SmartValidityException.class)
                .hasMessageContaining("ItemProduto não encontrado com o ID");
    }

    @Test
    @DisplayName("Deve listar todos os ItemProduto com sucesso")
    public void testListarTodosItemProduto() {
        when(itemProdutoRepository.findAll()).thenReturn(Arrays.asList(itemProduto));

        List<ItemProduto> itens = itemProdutoService.listarTodos();

        assertThat(itens).isNotEmpty();
        assertThat(itens.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve excluir um ItemProduto com sucesso")
    public void testExcluirItemProdutoComSucesso() throws SmartValidityException {
        when(itemProdutoRepository.findById(1)).thenReturn(Optional.of(itemProduto));

        itemProdutoService.excluir(1);

        verify(itemProdutoRepository, times(1)).delete(itemProduto);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar excluir um ItemProduto inexistente")
    public void testExcluirItemProdutoInexistente() {
        when(itemProdutoRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemProdutoService.excluir(999))
                .isInstanceOf(SmartValidityException.class)
                .hasMessageContaining("ItemProduto não encontrado com o ID");
    }

    @Test
    @DisplayName("Deve buscar ItemProduto por ID do Produto")
    public void testBuscarItemProdutoPorProdutoId() {
        when(itemProdutoRepository.findByProdutoId(1)).thenReturn(Arrays.asList(itemProduto));

        List<ItemProduto> itens = itemProdutoService.buscarPorProduto(1);

        assertThat(itens).isNotEmpty();
        assertThat(itens.get(0).getProduto().getId()).isEqualTo(1);
    }
}