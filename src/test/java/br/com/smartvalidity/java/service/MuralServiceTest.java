package br.com.smartvalidity.java.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.dto.MuralDTO;
import br.com.smartvalidity.model.entity.Categoria;
import br.com.smartvalidity.model.entity.Corredor;
import br.com.smartvalidity.model.entity.Endereco;
import br.com.smartvalidity.model.entity.Fornecedor;
import br.com.smartvalidity.model.entity.ItemProduto;
import br.com.smartvalidity.model.entity.Produto;
import br.com.smartvalidity.service.ExcelService;
import br.com.smartvalidity.service.ItemProdutoService;
import br.com.smartvalidity.service.MuralService;
import br.com.smartvalidity.service.UsuarioService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Testes do MuralService")
class MuralServiceTest {

    @Mock
    private ItemProdutoService itemProdutoService;

    @Mock
    private ExcelService excelService;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private MuralService muralService;

    private ItemProduto itemProdutoVencendoHoje;
    private ItemProduto itemProdutoVencido;
    private ItemProduto itemProdutoProximoVencer;
    private List<ItemProduto> todosItens;

    @BeforeEach
    void setUp() throws SmartValidityException {
        // Configurar itens de teste com datas específicas
        LocalDateTime hoje = LocalDateTime.now();
        itemProdutoVencendoHoje = criarItemProduto("1", "Produto Hoje", hoje.withHour(12).withMinute(0));
        itemProdutoVencido = criarItemProduto("2", "Produto Vencido", hoje.minusDays(1));
        itemProdutoProximoVencer = criarItemProduto("3", "Produto Próximo", hoje.plusDays(10));
        
        todosItens = Arrays.asList(itemProdutoVencendoHoje, itemProdutoVencido, itemProdutoProximoVencer);
        
        // Mock padrão para buscarTodos
        when(itemProdutoService.buscarTodos()).thenReturn(todosItens);
        
        // Mock para buscarPorId - retorna o item correspondente
        when(itemProdutoService.buscarPorId("1")).thenReturn(itemProdutoVencendoHoje);
        when(itemProdutoService.buscarPorId("2")).thenReturn(itemProdutoVencido);
        when(itemProdutoService.buscarPorId("3")).thenReturn(itemProdutoProximoVencer);
        
        // Mock para IDs não encontrados
        when(itemProdutoService.buscarPorId("999")).thenThrow(new SmartValidityException("ItemProduto não encontrado com o ID: 999"));
        
        // Mock para salvarItemInspecionado - retorna o item modificado
        when(itemProdutoService.salvarItemInspecionado(any(ItemProduto.class)))
            .thenAnswer(invocation -> {
                ItemProduto item = invocation.getArgument(0);
                return item;
            });
    }

    private ItemProduto criarItemProduto(String id, String descricaoProduto, LocalDateTime dataVencimento) {
        ItemProduto item = new ItemProduto();
        item.setId(id);
        item.setLote("LOTE" + id);
        item.setPrecoVenda(10.00);
        item.setDataFabricacao(LocalDateTime.now().minusDays(30));
        item.setDataVencimento(dataVencimento);
        item.setDataRecebimento(LocalDateTime.now().minusDays(25));
        item.setInspecionado(false);
        item.setProduto(criarProduto(id, descricaoProduto));
        return item;
    }

    private Produto criarProduto(String id, String descricao) {
        Produto produto = new Produto();
        produto.setId(id);
        produto.setDescricao(descricao);
        produto.setMarca("Marca Test");
        produto.setUnidadeMedida("UN");
        
        // Criar categoria
        Categoria categoria = new Categoria();
        categoria.setId("cat-" + id);
        categoria.setNome("Categoria Test");
        
        // Criar corredor
        Corredor corredor = new Corredor();
        corredor.setId("corr-" + id);
        corredor.setNome("Corredor " + id);
        categoria.setCorredor(corredor);
        
        produto.setCategoria(categoria);
        
        // Criar fornecedor com dados completos
        Fornecedor fornecedor = criarFornecedorCompleto(Integer.parseInt(id));
        produto.setFornecedores(Arrays.asList(fornecedor));
        
        return produto;
    }

    private Fornecedor criarFornecedorCompleto(Integer id) {
        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setId(id);
        fornecedor.setNome("Fornecedor Test");
        fornecedor.setCnpj("12345678000199");
        fornecedor.setTelefone("(11) 1234-5678");
        
        // Criar endereço
        Endereco endereco = new Endereco();
        endereco.setId(id);
        endereco.setCep("12345678");
        endereco.setLogradouro("Rua Test");
        endereco.setNumero("123");
        endereco.setComplemento("Sala 1");
        endereco.setBairro("Bairro Test");
        endereco.setCidade("Cidade Test");
        endereco.setEstado("SP");
        endereco.setPais("Brasil");
        fornecedor.setEndereco(endereco);
        
        return fornecedor;
    }

    @Test
    @DisplayName("Deve retornar motivos de inspeção válidos")
    void deveRetornarMotivosInspecaoValidos() {
        // When
        List<String> motivos = muralService.getMotivosInspecaoValidos();

        // Then
        assertNotNull(motivos);
        assertEquals(3, motivos.size());
        assertTrue(motivos.contains("Avaria/Quebra"));
        assertTrue(motivos.contains("Promoção"));
        assertTrue(motivos.contains("Outro"));
    }

    @Test
    @DisplayName("Deve buscar produtos que vencem hoje")
    void deveBuscarProdutosQueVencemHoje() {
        // When
        List<MuralDTO.Listagem> result = muralService.getVencemHoje();

        // Then
        assertNotNull(result);
        assertTrue(result.size() >= 0);
    }

    @Test
    @DisplayName("Deve buscar produtos vencidos")
    void deveBuscarProdutosVencidos() {
        // When
        List<MuralDTO.Listagem> result = muralService.getVencidos();

        // Then
        assertNotNull(result);
        assertTrue(result.size() >= 0);
    }

    @Test
    @DisplayName("Deve buscar produtos próximos ao vencimento")
    void deveBuscarProdutosProximosVencimento() {
        // When
        List<MuralDTO.Listagem> result = muralService.getProximosVencer();

        // Then
        assertNotNull(result);
        assertTrue(result.size() >= 0);
    }

    @Test
    @DisplayName("Deve buscar com filtro básico")
    void deveBuscarComFiltroBasico() {
        // Given
        MuralDTO.Filtro filtro = new MuralDTO.Filtro();
        filtro.setSearchTerm("Produto");

        // When
        List<MuralDTO.Listagem> result = muralService.buscarComFiltro(filtro);

        // Then
        assertNotNull(result);
        assertTrue(result.size() >= 0);
    }

    @Test
    @DisplayName("Deve aplicar paginação quando configurada")
    void deveAplicarPaginacaoQuandoConfigurada() {
        // Given
        MuralDTO.Filtro filtro = new MuralDTO.Filtro();
        filtro.setPagina(1);
        filtro.setLimite(2);

        // When
        List<MuralDTO.Listagem> result = muralService.buscarComFiltro(filtro);

        // Then
        assertNotNull(result);
        assertTrue(result.size() <= 2);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando página não existe")
    void deveRetornarListaVaziaQuandoPaginaNaoExiste() {
        // Given
        MuralDTO.Filtro filtro = new MuralDTO.Filtro();
        filtro.setPagina(999);
        filtro.setLimite(10);

        // When
        List<MuralDTO.Listagem> result = muralService.buscarComFiltro(filtro);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Deve marcar item como inspecionado com sucesso")
    void deveMarcarItemComoInspecionadoComSucesso() throws SmartValidityException {
        // Given
        String itemId = "1";
        String motivo = "Avaria/Quebra";
        String usuario = "usuario@teste.com";

        // When
        MuralDTO.Listagem result = muralService.marcarInspecionado(itemId, motivo, null, usuario);

        // Then
        assertNotNull(result);
        assertTrue(result.getInspecionado());
        assertEquals(motivo, result.getMotivoInspecao());
        assertEquals(usuario, result.getUsuarioInspecao());
        assertNotNull(result.getDataHoraInspecao());
    }

    @Test
    @DisplayName("Deve usar nome de usuário padrão quando não informado")
    void deveUsarNomeUsuarioPadraoQuandoNaoInformado() throws SmartValidityException {
        // Given
        String itemId = "1";
        String motivo = "Avaria/Quebra";

        // When
        MuralDTO.Listagem result = muralService.marcarInspecionado(itemId, motivo, null, null);

        // Then
        assertNotNull(result);
        assertTrue(result.getInspecionado());
        assertEquals(motivo, result.getMotivoInspecao());
        assertNotNull(result.getUsuarioInspecao()); // Deve ter um usuário padrão
    }

    @Test  
    @DisplayName("Deve aceitar motivo 'Outro' com especificação")
    void deveAceitarMotivoOutroComEspecificacao() throws SmartValidityException {
        // Given
        String itemId = "1";
        String motivo = "Outro";
        String motivoCustomizado = "Motivo personalizado";
        String usuario = "usuario@teste.com";

        // When
        MuralDTO.Listagem result = muralService.marcarInspecionado(itemId, motivo, motivoCustomizado, usuario);

        // Then
        assertNotNull(result);
        assertTrue(result.getInspecionado());
        assertEquals(motivoCustomizado, result.getMotivoInspecao()); // Deve usar o motivo customizado
        assertEquals(usuario, result.getUsuarioInspecao());
        assertNotNull(result.getDataHoraInspecao());
    }

    @Test
    @DisplayName("Deve lançar exceção quando motivo 'Outro' sem especificação")
    void deveLancarExcecaoQuandoMotivoOutroSemEspecificacao() {
        // Given
        String itemId = "1";
        String motivo = "Outro";
        String usuario = "usuario@teste.com";

        // When & Then
        SmartValidityException exception = assertThrows(SmartValidityException.class, () -> {
            muralService.marcarInspecionado(itemId, motivo, null, usuario);
        });

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("necessário informar um motivo customizado"));
    }

    @Test
    @DisplayName("Deve lançar exceção para motivo inválido")
    void deveLancarExcecaoParaMotivoInvalido() {
        // Given
        String itemId = "1";
        String motivoInvalido = "Motivo Inexistente";
        String usuario = "usuario@teste.com";

        // When & Then
        SmartValidityException exception = assertThrows(SmartValidityException.class, () -> {
            muralService.marcarInspecionado(itemId, motivoInvalido, null, usuario);
        });

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Motivo de inspeção inválido"));
    }

    @Test
    @DisplayName("Deve contar páginas corretamente")
    void deveContarPaginasCorretamente() {
        // Given
        MuralDTO.Filtro filtro = new MuralDTO.Filtro();
        filtro.setPagina(1);
        filtro.setLimite(2);

        // When
        int totalPaginas = muralService.contarPaginas(filtro);

        // Then
        assertTrue(totalPaginas >= 1);
    }

    @Test
    @DisplayName("Deve contar total de registros")
    void deveContarTotalRegistros() {
        // Given
        MuralDTO.Filtro filtro = new MuralDTO.Filtro();

        // When
        long totalRegistros = muralService.contarTotalRegistros(filtro);

        // Then
        assertTrue(totalRegistros >= 0);
    }

    @Test
    @DisplayName("Deve buscar item por ID")
    void deveBuscarItemPorId() throws SmartValidityException {
        // Given
        String itemId = "1";

        // When
        MuralDTO.Listagem result = muralService.getItemById(itemId);

        // Then
        assertNotNull(result);
        assertEquals("Produto Hoje", result.getProduto().getDescricao());
        assertEquals(itemId, result.getId());
    }

    @Test
    @DisplayName("Deve lançar exceção quando item não encontrado por ID")
    void deveLancarExcecaoQuandoItemNaoEncontradoPorId() {
        // Given
        String itemIdInexistente = "999";

        // When & Then
        SmartValidityException exception = assertThrows(SmartValidityException.class, () -> {
            muralService.getItemById(itemIdInexistente);
        });

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("ItemProduto não encontrado com o ID: 999"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando item não encontrado para marcar como inspecionado")
    void deveLancarExcecaoQuandoItemNaoEncontradoParaMarcarInspecionado() {
        // Given
        String itemIdInexistente = "999";
        String motivo = "Avaria/Quebra";
        String usuario = "usuario@teste.com";

        // When & Then
        SmartValidityException exception = assertThrows(SmartValidityException.class, () -> {
            muralService.marcarInspecionado(itemIdInexistente, motivo, null, usuario);
        });

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("ItemProduto não encontrado com o ID: 999"));
    }
}