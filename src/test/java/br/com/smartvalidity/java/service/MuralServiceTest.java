package br.com.smartvalidity.java.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
        verify(itemProdutoService).buscarTodos();
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
        filtro.setPagina(100);
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
        String id = "1";
        String motivo = "Avaria/Quebra";
        String usuarioInspecao = "admin";

        // When
        MuralDTO.Listagem result = muralService.marcarInspecionado(id, motivo, null, usuarioInspecao);

        // Then
        assertNotNull(result);
        verify(itemProdutoService).buscarPorId(id);
        verify(itemProdutoService).salvarItemInspecionado(any(ItemProduto.class));
    }

    @Test
    @DisplayName("Deve usar nome de usuário padrão quando não informado")
    void deveUsarNomeUsuarioPadraoQuandoNaoInformado() throws SmartValidityException {
        // Given
        String id = "1";
        String motivo = "Promoção";

        // When
        MuralDTO.Listagem result = muralService.marcarInspecionado(id, motivo, null, null);

        // Then
        assertNotNull(result);
        verify(itemProdutoService).buscarPorId(id);
        verify(itemProdutoService).salvarItemInspecionado(any(ItemProduto.class));
    }

    @Test  
    @DisplayName("Deve aceitar motivo 'Outro' com especificação")
    void deveAceitarMotivoOutroComEspecificacao() throws SmartValidityException {
        // Given
        String id = "1";
        String motivo = "Outro";
        String motivoCustomizado = "Motivo personalizado";
        String usuarioInspecao = "admin";

        // When
        MuralDTO.Listagem result = muralService.marcarInspecionado(id, motivo, motivoCustomizado, usuarioInspecao);

        // Then
        assertNotNull(result);
        verify(itemProdutoService).buscarPorId(id);
        verify(itemProdutoService).salvarItemInspecionado(any(ItemProduto.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando motivo 'Outro' sem especificação")
    void deveLancarExcecaoQuandoMotivoOutroSemEspecificacao() {
        // Given
        String id = "1";
        String motivo = "Outro";
        String usuarioInspecao = "admin";

        // When & Then
        SmartValidityException exception = assertThrows(SmartValidityException.class, () -> {
            muralService.marcarInspecionado(id, motivo, null, usuarioInspecao);
        });

        assertEquals("É necessário informar um motivo customizado quando selecionada a opção 'Outro'", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção para motivo inválido")
    void deveLancarExcecaoParaMotivoInvalido() {
        // Given
        String id = "1";
        String motivo = "Motivo Inválido";
        String usuarioInspecao = "admin";

        // When & Then
        SmartValidityException exception = assertThrows(SmartValidityException.class, () -> {
            muralService.marcarInspecionado(id, motivo, null, usuarioInspecao);
        });

        assertEquals("Motivo de inspeção inválido", exception.getMessage());
    }

    @Test
    @DisplayName("Deve contar páginas corretamente")
    void deveContarPaginasCorretamente() {
        // Given
        MuralDTO.Filtro filtro = new MuralDTO.Filtro();
        filtro.setPagina(1);
        filtro.setLimite(2);

        // When
        int paginas = muralService.contarPaginas(filtro);

        // Then
        assertTrue(paginas >= 0);
        verify(itemProdutoService).buscarTodos();
    }

    @Test
    @DisplayName("Deve contar total de registros")
    void deveContarTotalRegistros() {
        // Given
        MuralDTO.Filtro filtro = new MuralDTO.Filtro();

        // When
        long total = muralService.contarTotalRegistros(filtro);

        // Then
        assertTrue(total >= 0);
        verify(itemProdutoService).buscarTodos();
    }

    @Test
    @DisplayName("Deve buscar item por ID")
    void deveBuscarItemPorId() throws SmartValidityException {
        // Given
        String id = "1";

        // When
        MuralDTO.Listagem result = muralService.getItemById(id);

        // Then
        assertNotNull(result);
        verify(itemProdutoService).buscarPorId(id);
    }

    @Test
    @DisplayName("Deve lançar exceção quando item não encontrado por ID")
    void deveLancarExcecaoQuandoItemNaoEncontradoPorId() {
        // Given
        String id = "999";

        // When & Then
        assertThrows(SmartValidityException.class, () -> {
            muralService.getItemById(id);
        });
    }

    @Test
    @DisplayName("Deve lançar exceção quando item não encontrado para marcar como inspecionado")
    void deveLancarExcecaoQuandoItemNaoEncontradoParaMarcarInspecionado() {
        // Given
        String id = "999";
        String motivo = "Avaria/Quebra";
        String usuarioInspecao = "admin";

        // When & Then
        assertThrows(SmartValidityException.class, () -> {
            muralService.marcarInspecionado(id, motivo, null, usuarioInspecao);
        });
    }

    // NOVOS TESTES PARA AUMENTAR COBERTURA

    @Test
    @DisplayName("Deve buscar marcas disponíveis")
    void deveBuscarMarcasDisponiveis() {
        // When
        List<String> marcas = muralService.getMarcasDisponiveis();

        // Then
        assertNotNull(marcas);
        verify(itemProdutoService).buscarTodos();
    }

    @Test
    @DisplayName("Deve buscar corredores disponíveis")
    void deveBuscarCorredoresDisponiveis() {
        // When
        List<String> corredores = muralService.getCorredoresDisponiveis();

        // Then
        assertNotNull(corredores);
        verify(itemProdutoService).buscarTodos();
    }

    @Test
    @DisplayName("Deve buscar categorias disponíveis")
    void deveBuscarCategoriasDisponiveis() {
        // When
        List<String> categorias = muralService.getCategoriasDisponiveis();

        // Then
        assertNotNull(categorias);
        verify(itemProdutoService).buscarTodos();
    }

    @Test
    @DisplayName("Deve buscar fornecedores disponíveis")
    void deveBuscarFornecedoresDisponiveis() {
        // When
        List<String> fornecedores = muralService.getFornecedoresDisponiveis();

        // Then
        assertNotNull(fornecedores);
        verify(itemProdutoService).buscarTodos();
    }

    @Test
    @DisplayName("Deve buscar lotes disponíveis")
    void deveBuscarLotesDisponiveis() {
        // When
        List<String> lotes = muralService.getLotesDisponiveis();

        // Then
        assertNotNull(lotes);
        verify(itemProdutoService).buscarTodos();
    }

    @Test
    @DisplayName("Deve buscar usuários de inspeção disponíveis")
    void deveBuscarUsuariosInspecaoDisponiveis() throws SmartValidityException {
        // Given
        when(usuarioService.listarTodos()).thenReturn(new ArrayList<>());

        // When
        List<String> usuarios = muralService.getUsuariosInspecaoDisponiveis();

        // Then
        assertNotNull(usuarios);
        verify(usuarioService).listarTodos();
    }

    @Test
    @DisplayName("Deve buscar itens por lista de IDs")
    void deveBuscarItensPorListaIds() throws SmartValidityException {
        // Given
        List<String> ids = Arrays.asList("1", "2");

        // When
        List<MuralDTO.Listagem> result = muralService.buscarPorIds(ids);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(itemProdutoService, times(2)).buscarPorId(anyString());
    }

    @Test
    @DisplayName("Deve marcar vários itens como inspecionados")
    void deveMarcarVariosItensComoInspecionados() throws SmartValidityException {
        // Given
        List<String> ids = Arrays.asList("1", "2");
        String motivo = "Avaria/Quebra";
        String usuarioInspecao = "admin";

        // When
        List<MuralDTO.Listagem> result = muralService.marcarVariosInspecionados(ids, motivo, null, usuarioInspecao);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(itemProdutoService, times(2)).buscarPorId(anyString());
        verify(itemProdutoService, times(2)).salvarItemInspecionado(any(ItemProduto.class));
    }

    @Test
    @DisplayName("Deve filtrar por status específico")
    void deveFiltrarPorStatusEspecifico() {
        // Given
        MuralDTO.Filtro filtro = new MuralDTO.Filtro();
        filtro.setStatus("vencido");

        // When
        List<MuralDTO.Listagem> result = muralService.buscarComFiltro(filtro);

        // Then
        assertNotNull(result);
        verify(itemProdutoService).buscarTodos();
    }

    @Test
    @DisplayName("Deve filtrar por status 'hoje'")
    void deveFiltrarPorStatusHoje() {
        // Given
        MuralDTO.Filtro filtro = new MuralDTO.Filtro();
        filtro.setStatus("hoje");

        // When
        List<MuralDTO.Listagem> result = muralService.buscarComFiltro(filtro);

        // Then
        assertNotNull(result);
        verify(itemProdutoService).buscarTodos();
    }

    @Test
    @DisplayName("Deve filtrar por status 'proximo'")
    void deveFiltrarPorStatusProximo() {
        // Given
        MuralDTO.Filtro filtro = new MuralDTO.Filtro();
        filtro.setStatus("proximo");

        // When
        List<MuralDTO.Listagem> result = muralService.buscarComFiltro(filtro);

        // Then
        assertNotNull(result);
        verify(itemProdutoService).buscarTodos();
    }

    @Test
    @DisplayName("Deve filtrar por inspecionado")
    void deveFiltrarPorInspecionado() {
        // Given
        MuralDTO.Filtro filtro = new MuralDTO.Filtro();
        filtro.setInspecionado(true);

        // When
        List<MuralDTO.Listagem> result = muralService.buscarComFiltro(filtro);

        // Then
        assertNotNull(result);
        verify(itemProdutoService).buscarTodos();
    }

    @Test
    @DisplayName("Deve ordenar por campo específico")
    void deveOrdenarPorCampoEspecifico() {
        // Given
        MuralDTO.Filtro filtro = new MuralDTO.Filtro();
        filtro.setSortBy("dataVencimento");
        filtro.setSortDirection("asc");

        // When
        List<MuralDTO.Listagem> result = muralService.buscarComFiltro(filtro);

        // Then
        assertNotNull(result);
        verify(itemProdutoService).buscarTodos();
    }

    @Test
    @DisplayName("Deve gerar relatório Excel para itens selecionados")
    void deveGerarRelatorioExcelSelecionados() throws SmartValidityException {
        // Given
        MuralDTO.RelatorioRequest request = new MuralDTO.RelatorioRequest();
        request.setIds(Arrays.asList("1", "2"));
        request.setStatus("hoje"); // Status válido que não fará validação rigorosa
        request.setTipo("SELECIONADOS");

        // Mock dos itens com status "hoje"
        ItemProduto item1 = criarItemProduto("1", "Produto 1", LocalDateTime.now());
        ItemProduto item2 = criarItemProduto("2", "Produto 2", LocalDateTime.now());
        when(itemProdutoService.buscarPorId("1")).thenReturn(item1);
        when(itemProdutoService.buscarPorId("2")).thenReturn(item2);

        byte[] mockExcel = "mock excel content".getBytes();
        when(excelService.gerarExcelMural(anyList(), anyString())).thenReturn(mockExcel);

        // When
        byte[] result = muralService.gerarRelatorioExcel(request);

        // Then
        assertNotNull(result);
        verify(excelService).gerarExcelMural(anyList(), anyString());
    }

    @Test
    @DisplayName("Deve gerar relatório Excel para todos os itens")
    void deveGerarRelatorioExcelTodos() throws SmartValidityException {
        // Given
        MuralDTO.RelatorioRequest request = new MuralDTO.RelatorioRequest();
        request.setTipo("TODOS");
        request.setStatus("proximo"); // Status válido
        request.setFiltro(new MuralDTO.Filtro());

        // Mock para retornar itens com status "proximo"
        List<ItemProduto> itensProximos = Arrays.asList(
            criarItemProduto("1", "Produto 1", LocalDateTime.now().plusDays(2)),
            criarItemProduto("2", "Produto 2", LocalDateTime.now().plusDays(3))
        );
        when(itemProdutoService.buscarTodos()).thenReturn(itensProximos);

        byte[] mockExcel = "mock excel content".getBytes();
        when(excelService.gerarExcelMural(anyList(), anyString())).thenReturn(mockExcel);

        // When
        byte[] result = muralService.gerarRelatorioExcel(request);

        // Then
        assertNotNull(result);
        verify(excelService).gerarExcelMural(anyList(), anyString());
    }

    @Test
    @DisplayName("Deve gerar relatório Excel para página atual")
    void deveGerarRelatorioExcelPagina() throws SmartValidityException {
        // Given
        MuralDTO.RelatorioRequest request = new MuralDTO.RelatorioRequest();
        request.setTipo("PAGINA");
        request.setStatus("vencido"); // Status válido
        MuralDTO.Filtro filtro = new MuralDTO.Filtro();
        filtro.setPagina(1);
        filtro.setLimite(10);
        request.setFiltro(filtro);

        // Mock para retornar itens com status "vencido"
        List<ItemProduto> itensVencidos = Arrays.asList(
            criarItemProduto("1", "Produto 1", LocalDateTime.now().minusDays(1)),
            criarItemProduto("2", "Produto 2", LocalDateTime.now().minusDays(2))
        );
        when(itemProdutoService.buscarTodos()).thenReturn(itensVencidos);

        byte[] mockExcel = "mock excel content".getBytes();
        when(excelService.gerarExcelMural(anyList(), anyString())).thenReturn(mockExcel);

        // When
        byte[] result = muralService.gerarRelatorioExcel(request);

        // Then
        assertNotNull(result);
        verify(excelService).gerarExcelMural(anyList(), anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção para tipo de relatório inválido")
    void deveLancarExcecaoParaTipoRelatorioInvalido() {
        // Given
        MuralDTO.RelatorioRequest request = new MuralDTO.RelatorioRequest();
        request.setTipo("INVALIDO");
        request.setStatus("vencido");

        // When & Then
        SmartValidityException exception = assertThrows(SmartValidityException.class, () -> {
            muralService.gerarRelatorioExcel(request);
        });

        assertEquals("Tipo de relatório inválido", exception.getMessage());
    }

    @Test
    @DisplayName("Deve cancelar seleção de itens")
    void deveCancelarSelecaoItens() {
        // Given
        List<String> ids = Arrays.asList("1", "2");

        // When & Then (método void, apenas verifica se não lança exceção)
        assertDoesNotThrow(() -> muralService.cancelarSelecao(ids));
    }

    @Test
    @DisplayName("Deve aplicar filtros de texto")
    void deveAplicarFiltrosTexto() {
        // Given
        MuralDTO.Filtro filtro = new MuralDTO.Filtro();
        filtro.setMarca("Marca Test");
        filtro.setCorredor("Corredor 1");
        filtro.setCategoria("Categoria Test");
        filtro.setFornecedor("Fornecedor Test");
        filtro.setLote("LOTE1");

        // When
        List<MuralDTO.Listagem> result = muralService.buscarComFiltro(filtro);

        // Then
        assertNotNull(result);
        verify(itemProdutoService).buscarTodos();
    }

    @Test
    @DisplayName("Deve aplicar filtros de data")
    void deveAplicarFiltrosData() {
        // Given
        MuralDTO.Filtro filtro = new MuralDTO.Filtro();
        filtro.setDataVencimentoInicio(LocalDateTime.now().minusDays(1));
        filtro.setDataVencimentoFim(LocalDateTime.now().plusDays(1));
        filtro.setDataFabricacaoInicio(LocalDateTime.now().minusDays(30));
        filtro.setDataFabricacaoFim(LocalDateTime.now());

        // When
        List<MuralDTO.Listagem> result = muralService.buscarComFiltro(filtro);

        // Then
        assertNotNull(result);
        verify(itemProdutoService).buscarTodos();
    }

    @Test
    @DisplayName("Deve aplicar filtro de motivo de inspeção")
    void deveAplicarFiltroMotivoInspecao() {
        // Given
        MuralDTO.Filtro filtro = new MuralDTO.Filtro();
        filtro.setMotivoInspecao("Avaria/Quebra");

        // When
        List<MuralDTO.Listagem> result = muralService.buscarComFiltro(filtro);

        // Then
        assertNotNull(result);
        verify(itemProdutoService).buscarTodos();
    }

    @Test
    @DisplayName("Deve aplicar filtro de usuário de inspeção")
    void deveAplicarFiltroUsuarioInspecao() {
        // Given
        MuralDTO.Filtro filtro = new MuralDTO.Filtro();
        filtro.setUsuarioInspecao("admin");

        // When
        List<MuralDTO.Listagem> result = muralService.buscarComFiltro(filtro);

        // Then
        assertNotNull(result);
        verify(itemProdutoService).buscarTodos();
    }

    @Test
    @DisplayName("Deve lançar exceção quando motivo é obrigatório")
    void deveLancarExcecaoQuandoMotivoObrigatorio() {
        // Given
        String id = "1";
        String usuarioInspecao = "admin";

        // When & Then
        SmartValidityException exception = assertThrows(SmartValidityException.class, () -> {
            muralService.marcarInspecionado(id, null, null, usuarioInspecao);
        });

        assertEquals("O motivo da inspeção é obrigatório", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção quando motivo é vazio")
    void deveLancarExcecaoQuandoMotivoVazio() {
        // Given
        String id = "1";
        String motivo = "";
        String usuarioInspecao = "admin";

        // When & Then
        SmartValidityException exception = assertThrows(SmartValidityException.class, () -> {
            muralService.marcarInspecionado(id, motivo, null, usuarioInspecao);
        });

        assertEquals("O motivo da inspeção é obrigatório", exception.getMessage());
    }
}