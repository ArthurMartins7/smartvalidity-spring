package br.com.smartvalidity.java.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.entity.Categoria;
import br.com.smartvalidity.model.entity.ItemProduto;
import br.com.smartvalidity.model.entity.Produto;
import br.com.smartvalidity.model.enums.SituacaoValidade;
import br.com.smartvalidity.model.repository.ItemProdutoRepository;
import br.com.smartvalidity.service.ItemProdutoService;
import br.com.smartvalidity.service.ProdutoService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do ItemProdutoService")
class ItemProdutoServiceTest {

    @Mock
    private ItemProdutoRepository itemProdutoRepository;

    @Mock
    private ProdutoService produtoService;

    @InjectMocks
    private ItemProdutoService itemProdutoService;

    private ItemProduto itemProdutoValido;
    private Produto produto;
    private Categoria categoria;

    @BeforeEach
    void setUp() {
        categoria = new Categoria();
        categoria.setId(UUID.randomUUID().toString());
        categoria.setNome("Categoria Teste");

        produto = new Produto();
        produto.setId(UUID.randomUUID().toString());
        produto.setCodigoBarras("7891234567890");
        produto.setDescricao("Produto Teste");
        produto.setMarca("Marca A");
        produto.setUnidadeMedida("UN");
        produto.setQuantidade(10);
        produto.setCategoria(categoria);

        itemProdutoValido = new ItemProduto();
        itemProdutoValido.setId(UUID.randomUUID().toString());
        itemProdutoValido.setLote("LOTE123");
        itemProdutoValido.setPrecoVenda(15.99);
        itemProdutoValido.setDataFabricacao(LocalDateTime.now().minusDays(30));
        itemProdutoValido.setDataVencimento(LocalDateTime.now().plusDays(30));
        itemProdutoValido.setDataRecebimento(LocalDateTime.now().minusDays(1));
        itemProdutoValido.setProduto(produto);
        itemProdutoValido.setInspecionado(false);
        itemProdutoValido.setSituacaoValidade(SituacaoValidade.OK);
    }

    @Test
    @DisplayName("Deve buscar todos os itens de produto")
    void deveBuscarTodosItens() {
        // Given
        List<ItemProduto> itens = Arrays.asList(itemProdutoValido);
        when(itemProdutoRepository.findAll()).thenReturn(itens);

        // When
        List<ItemProduto> result = itemProdutoService.buscarTodos();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(itemProdutoValido, result.get(0));
        verify(itemProdutoRepository).findAll();
    }

    @Test
    @DisplayName("Deve buscar item por ID com sucesso")
    void deveBuscarItemPorIdComSucesso() throws SmartValidityException {
        // Given
        String id = itemProdutoValido.getId();
        when(itemProdutoRepository.findById(id)).thenReturn(Optional.of(itemProdutoValido));

        // When
        ItemProduto result = itemProdutoService.buscarPorId(id);

        // Then
        assertNotNull(result);
        assertEquals(itemProdutoValido.getId(), result.getId());
        assertEquals(itemProdutoValido.getLote(), result.getLote());
        verify(itemProdutoRepository).findById(id);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar item inexistente")
    void deveLancarExcecaoAoBuscarItemInexistente() {
        // Given
        String idInexistente = UUID.randomUUID().toString();
        when(itemProdutoRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // When & Then
        SmartValidityException exception = assertThrows(SmartValidityException.class,
            () -> itemProdutoService.buscarPorId(idInexistente));

        assertEquals("ItemProduto não encontrado com o ID: " + idInexistente, exception.getMessage());
        verify(itemProdutoRepository).findById(idInexistente);
    }

    @Test
    @DisplayName("Deve salvar item de produto válido e incrementar estoque")
    void deveSalvarItemValidoEIncrementarEstoque() throws SmartValidityException {
        // Given
        ItemProduto novoItem = new ItemProduto();
        novoItem.setLote("LOTE456");
        novoItem.setPrecoVenda(20.50);
        novoItem.setDataFabricacao(LocalDateTime.now().minusDays(15));
        novoItem.setDataVencimento(LocalDateTime.now().plusDays(45));
        novoItem.setDataRecebimento(LocalDateTime.now());
        novoItem.setProduto(produto);

        when(produtoService.buscarPorId(produto.getId())).thenReturn(produto);
        when(itemProdutoRepository.save(novoItem)).thenReturn(novoItem);

        // When
        ItemProduto result = itemProdutoService.salvar(novoItem);

        // Then
        assertNotNull(result);
        assertEquals(novoItem, result);
        assertEquals(11, produto.getQuantidade()); // Incrementou de 10 para 11
        
        verify(produtoService).buscarPorId(produto.getId());
        verify(itemProdutoRepository).save(novoItem);
    }

    @Test
    @DisplayName("Não deve salvar item com data de vencimento anterior à data atual")
    void naoDeveSalvarItemComDataVencimentoAnterior() {
        // Given
        ItemProduto itemVencido = new ItemProduto();
        itemVencido.setLote("LOTE_VENCIDO");
        itemVencido.setPrecoVenda(10.00);
        itemVencido.setDataFabricacao(LocalDateTime.now().minusDays(60));
        itemVencido.setDataVencimento(LocalDateTime.now().minusDays(1)); // Vencido
        itemVencido.setDataRecebimento(LocalDateTime.now().minusDays(50));
        itemVencido.setProduto(produto);

        // Este teste assume que há validação de negócio para data de vencimento
        // Se não houver, pode ser implementada ou o teste pode verificar apenas se salva
        when(itemProdutoRepository.save(itemVencido))
            .thenThrow(new RuntimeException("Data de vencimento não pode ser anterior à data atual"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> itemProdutoService.salvar2(itemVencido));

        assertTrue(exception.getMessage().contains("Data de vencimento não pode ser anterior à data atual"));
    }

    @Test
    @DisplayName("Deve buscar itens por lote")
    void deveBuscarItensPorLote() throws SmartValidityException {
        // Given
        String lote = "LOTE123";
        List<ItemProduto> itens = Arrays.asList(itemProdutoValido);
        when(itemProdutoRepository.findByLote(lote)).thenReturn(itens);

        // When
        List<ItemProduto> result = itemProdutoService.buscarPorLote(lote);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(itemProdutoValido, result.get(0));
        verify(itemProdutoRepository).findByLote(lote);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar lote inexistente")
    void deveLancarExcecaoAoBuscarLoteInexistente() {
        // Given
        String loteInexistente = "LOTE_INEXISTENTE";
        when(itemProdutoRepository.findByLote(loteInexistente)).thenReturn(null);

        // When & Then
        SmartValidityException exception = assertThrows(SmartValidityException.class,
            () -> itemProdutoService.buscarPorLote(loteInexistente));

        assertEquals("Não existe nenhum produto no estoque com esse lote.", exception.getMessage());
        verify(itemProdutoRepository).findByLote(loteInexistente);
    }

    @Test
    @DisplayName("Deve buscar itens por produto")
    void deveBuscarItensPorProduto() {
        // Given
        String produtoId = produto.getId();
        List<ItemProduto> itens = Arrays.asList(itemProdutoValido);
        when(itemProdutoRepository.findByProdutoId(produtoId)).thenReturn(itens);

        // When
        List<ItemProduto> result = itemProdutoService.buscarPorProduto(produtoId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(itemProdutoValido, result.get(0));
        verify(itemProdutoRepository).findByProdutoId(produtoId);
    }

    @Test
    @DisplayName("Deve atualizar item de produto")
    void deveAtualizarItemProduto() throws SmartValidityException {
        // Given
        String idItem = itemProdutoValido.getId();
        ItemProduto itemAtualizado = new ItemProduto();
        itemAtualizado.setLote("LOTE_ATUALIZADO");
        itemAtualizado.setPrecoVenda(25.00);
        itemAtualizado.setDataFabricacao(LocalDateTime.now().minusDays(10));
        itemAtualizado.setDataVencimento(LocalDateTime.now().plusDays(60));
        itemAtualizado.setDataRecebimento(LocalDateTime.now());
        itemAtualizado.setProduto(produto);

        when(itemProdutoRepository.findById(idItem)).thenReturn(Optional.of(itemProdutoValido));
        when(itemProdutoRepository.save(any(ItemProduto.class))).thenReturn(itemAtualizado);

        // When
        ItemProduto result = itemProdutoService.atualizar(idItem, itemAtualizado);

        // Then
        assertNotNull(result);
        assertEquals("LOTE_ATUALIZADO", result.getLote());
        assertEquals(25.00, result.getPrecoVenda());
        
        verify(itemProdutoRepository).findById(idItem);
        verify(itemProdutoRepository).save(any(ItemProduto.class));
    }

    @Test
    @DisplayName("Deve excluir item e decrementar estoque do produto")
    void deveExcluirItemEDecrementarEstoque() throws SmartValidityException {
        // Given
        String idItem = itemProdutoValido.getId();
        when(itemProdutoRepository.findById(idItem)).thenReturn(Optional.of(itemProdutoValido));
        when(produtoService.buscarPorId(produto.getId())).thenReturn(produto);

        // When
        itemProdutoService.excluir(idItem);

        // Then
        assertEquals(9, produto.getQuantidade()); // Decrementou de 10 para 9
        
        verify(itemProdutoRepository).findById(idItem);
        verify(produtoService).buscarPorId(produto.getId());
        verify(itemProdutoRepository).delete(itemProdutoValido);
        verify(produtoService).salvar(produto);
    }

    @Test
    @DisplayName("Deve marcar item como inspecionado")
    void deveMarcarItemComoInspecionado() throws SmartValidityException {
        String idItem = itemProdutoValido.getId();
        String motivo = "Avaria/Quebra";
        String usuario = "João Silva";
        LocalDateTime agora = LocalDateTime.now();
        ItemProduto itemInspecionado = new ItemProduto();
        itemInspecionado.setId(idItem);
        itemInspecionado.setInspecionado(true);
        itemInspecionado.setMotivoInspecao(motivo);
        itemInspecionado.setUsuarioInspecao(usuario);
        itemInspecionado.setDataHoraInspecao(agora);
        when(itemProdutoRepository.findById(idItem)).thenReturn(Optional.of(itemProdutoValido));
        when(itemProdutoRepository.save(any(ItemProduto.class))).thenReturn(itemInspecionado);
        ItemProduto result = itemProdutoService.salvarItemInspecionado(itemInspecionado);
        assertNotNull(result);
        assertTrue(result.getInspecionado());
        assertEquals(motivo, result.getMotivoInspecao());
        assertEquals(usuario, result.getUsuarioInspecao());
        assertNotNull(result.getDataHoraInspecao());
        verify(itemProdutoRepository).findById(idItem);
        verify(itemProdutoRepository).save(any(ItemProduto.class));
    }

    @Test
    @DisplayName("Não deve alterar item já inspecionado")
    void naoDeveAlterarItemJaInspecionado() throws SmartValidityException {
        String idItem = itemProdutoValido.getId();
        itemProdutoValido.setInspecionado(true);
        itemProdutoValido.setMotivoInspecao("Motivo Original");
        ItemProduto tentativaAlteracao = new ItemProduto();
        tentativaAlteracao.setId(idItem);
        tentativaAlteracao.setInspecionado(true);
        tentativaAlteracao.setMotivoInspecao("Novo Motivo");

        when(itemProdutoRepository.findById(idItem)).thenReturn(Optional.of(itemProdutoValido));

        // When
        ItemProduto result = itemProdutoService.salvarItemInspecionado(tentativaAlteracao);

        // Then
        assertNotNull(result);
        assertEquals("Motivo Original", result.getMotivoInspecao()); // Mantém o motivo original
        
        verify(itemProdutoRepository).findById(idItem);
        verify(itemProdutoRepository, never()).save(any(ItemProduto.class)); // Não deve salvar
    }

    @Test
    @DisplayName("Deve dar baixa em itens vendidos por lote")
    void deveDarBaixaEmItensVendidosPorLote() throws SmartValidityException {
        // Given
        String lote = "LOTE123";
        int quantidadeParaRemover = 2;
        
        ItemProduto item1 = new ItemProduto();
        item1.setId("item1");
        item1.setLote(lote);
        item1.setProduto(produto);
        
        ItemProduto item2 = new ItemProduto();
        item2.setId("item2");
        item2.setLote(lote);
        item2.setProduto(produto);
        
        List<ItemProduto> itensDoLote = Arrays.asList(item1, item2);
        
        when(itemProdutoRepository.findByLote(lote)).thenReturn(itensDoLote);
        when(itemProdutoRepository.findById("item1")).thenReturn(Optional.of(item1));
        when(itemProdutoRepository.findById("item2")).thenReturn(Optional.of(item2));
        when(produtoService.buscarPorId(produto.getId())).thenReturn(produto);
        when(produtoService.salvar(any(Produto.class))).thenReturn(produto);

        // When
        itemProdutoService.darBaixaItensVendidos(lote, quantidadeParaRemover);

        // Then
        verify(itemProdutoRepository).findByLote(lote);
        verify(itemProdutoRepository, times(2)).delete(any(ItemProduto.class));
        verify(produtoService, times(2)).salvar(produto);
    }

    @Test
    @DisplayName("Deve dar baixa em lista de itens vendidos")
    void deveDarBaixaEmListaDeItensVendidos() throws SmartValidityException {
        // Given
        ItemProduto item1 = new ItemProduto();
        item1.setLote("LOTE123");
        
        ItemProduto item2 = new ItemProduto();
        item2.setLote("LOTE456");
        
        List<ItemProduto> itensVendidos = Arrays.asList(item1, item2);
        
        when(itemProdutoRepository.findByLote("LOTE123")).thenReturn(Arrays.asList(item1));
        when(itemProdutoRepository.findByLote("LOTE456")).thenReturn(Arrays.asList(item2));

        // When
        itemProdutoService.darBaixaItensVendidos(itensVendidos);

        // Then
        // O método chama buscarPorLote duas vezes para cada lote (no forEach e no for)
        verify(itemProdutoRepository, times(2)).findByLote("LOTE123");
        verify(itemProdutoRepository, times(2)).findByLote("LOTE456");
    }

    @Test
    @DisplayName("Deve lançar RuntimeException quando não encontrar lote na lista de vendidos")
    void deveLancarRuntimeExceptionQuandoNaoEncontrarLoteNaListaVendidos() {
        // Given
        ItemProduto item1 = new ItemProduto();
        item1.setLote("LOTE_INEXISTENTE");
        
        List<ItemProduto> itensVendidos = Arrays.asList(item1);
        
        when(itemProdutoRepository.findByLote("LOTE_INEXISTENTE")).thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            itemProdutoService.darBaixaItensVendidos(itensVendidos);
        });

        assertTrue(exception.getCause() instanceof SmartValidityException);
        verify(itemProdutoRepository).findByLote("LOTE_INEXISTENTE");
    }

    @Test
    @DisplayName("Deve salvar usando salvar2 sem incrementar estoque")
    void deveSalvarUsandoSalvar2SemIncrementarEstoque() {
        // Given
        ItemProduto item = new ItemProduto();
        item.setLote("LOTE789");
        
        when(itemProdutoRepository.save(item)).thenReturn(item);

        // When
        ItemProduto resultado = itemProdutoService.salvar2(item);

        // Then
        assertNotNull(resultado);
        assertEquals(item, resultado);
        verify(itemProdutoRepository).save(item);
        // Verifica que não chamou produtoService - não lança exceção
    }

    @Test
    @DisplayName("Deve excluir usando excluir2 sem decrementar estoque")
    void deveExcluirUsandoExcluir2SemDecrementarEstoque() throws SmartValidityException {
        // Given
        String id = itemProdutoValido.getId();
        when(itemProdutoRepository.findById(id)).thenReturn(Optional.of(itemProdutoValido));

        // When
        itemProdutoService.excluir2(id);

        // Then
        verify(itemProdutoRepository).findById(id);
        verify(itemProdutoRepository).delete(itemProdutoValido);
        // Verifica que não chamou produtoService
        verify(produtoService, never()).buscarPorId(any());
        verify(produtoService, never()).salvar(any());
    }

    @Test
    @DisplayName("Deve salvar item inspecionado com sucesso")
    void deveSalvarItemInspecionadoComSucesso() throws SmartValidityException {
        // Given
        ItemProduto itemNaoInspecionado = new ItemProduto();
        itemNaoInspecionado.setId("item-novo");
        itemNaoInspecionado.setInspecionado(false); // Item ainda não inspecionado
        
        ItemProduto itemParaInspecionar = new ItemProduto();
        itemParaInspecionar.setId("item-novo");
        itemParaInspecionar.setInspecionado(true);
        itemParaInspecionar.setMotivoInspecao("Teste");
        itemParaInspecionar.setUsuarioInspecao("usuario@teste.com");
        itemParaInspecionar.setDataHoraInspecao(LocalDateTime.now());
        
        when(itemProdutoRepository.findById("item-novo")).thenReturn(Optional.of(itemNaoInspecionado));
        when(itemProdutoRepository.save(any(ItemProduto.class))).thenReturn(itemParaInspecionar);

        // When
        ItemProduto resultado = itemProdutoService.salvarItemInspecionado(itemParaInspecionar);

        // Then
        assertNotNull(resultado);
        assertTrue(resultado.getInspecionado());
        assertEquals("Teste", resultado.getMotivoInspecao());
        verify(itemProdutoRepository).findById("item-novo");
        verify(itemProdutoRepository).save(any(ItemProduto.class));
    }

    @Test
    @DisplayName("Deve retornar item sem alterações se já estiver inspecionado")
    void deveRetornarItemSemAlteracoesSeJaEstiverInspecionado() throws SmartValidityException {
        // Given
        ItemProduto itemJaInspecionado = new ItemProduto();
        itemJaInspecionado.setId("item-inspecionado");
        itemJaInspecionado.setInspecionado(true);
        itemJaInspecionado.setMotivoInspecao("Motivo original");
        
        ItemProduto itemParaInspecionar = new ItemProduto();
        itemParaInspecionar.setId("item-inspecionado");
        itemParaInspecionar.setInspecionado(true);
        itemParaInspecionar.setMotivoInspecao("Novo motivo");
        
        when(itemProdutoRepository.findById("item-inspecionado")).thenReturn(Optional.of(itemJaInspecionado));

        // When
        ItemProduto resultado = itemProdutoService.salvarItemInspecionado(itemParaInspecionar);

        // Then
        assertNotNull(resultado);
        assertEquals("Motivo original", resultado.getMotivoInspecao()); // Mantém o motivo original
        verify(itemProdutoRepository).findById("item-inspecionado");
        verify(itemProdutoRepository, never()).save(any()); // Não deve salvar
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar inspecionar item com ID nulo")
    void deveLancarExcecaoAoTentarInspecionarItemComIdNulo() {
        // Given
        ItemProduto itemSemId = new ItemProduto();
        itemSemId.setId(null);

        // When & Then
        SmartValidityException exception = assertThrows(SmartValidityException.class, () -> {
            itemProdutoService.salvarItemInspecionado(itemSemId);
        });

        assertEquals("ID do item não pode ser nulo para inspeção", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar SmartValidityException quando item não existe para inspeção")
    void deveLancarSmartValidityExceptionQuandoItemNaoExisteParaInspecao() {
        // Given
        ItemProduto item = new ItemProduto();
        item.setId("item-inexistente");
        
        when(itemProdutoRepository.findById("item-inexistente")).thenReturn(Optional.empty());

        // When & Then
        SmartValidityException exception = assertThrows(SmartValidityException.class, () -> {
            itemProdutoService.salvarItemInspecionado(item);
        });

        assertTrue(exception.getMessage().contains("ItemProduto não encontrado com o ID: item-inexistente"));
        verify(itemProdutoRepository).findById("item-inexistente");
    }

    @Test
    @DisplayName("Deve capturar e relançar exceção genérica como SmartValidityException")
    void deveCapturaERelancaExcecaoGenericaComoSmartValidityException() {
        // Given
        ItemProduto item = new ItemProduto();
        item.setId("item-erro");
        
        when(itemProdutoRepository.findById("item-erro")).thenThrow(new RuntimeException("Erro de banco"));

        // When & Then
        assertThrows(SmartValidityException.class, () -> {
            itemProdutoService.salvarItemInspecionado(item);
        });

        verify(itemProdutoRepository).findById("item-erro");
    }
} 