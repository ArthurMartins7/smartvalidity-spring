package br.com.smartvalidity.java.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.dto.ProdutoDTO;
import br.com.smartvalidity.model.entity.Categoria;
import br.com.smartvalidity.model.entity.Corredor;
import br.com.smartvalidity.model.entity.Endereco;
import br.com.smartvalidity.model.entity.Fornecedor;
import br.com.smartvalidity.model.entity.Produto;
import br.com.smartvalidity.model.repository.ProdutoRepository;
import br.com.smartvalidity.model.seletor.ProdutoSeletor;
import br.com.smartvalidity.service.ProdutoService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do ProdutoService")
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private ProdutoService produtoService;

    private Produto produtoValido;
    private Categoria categoria;
    private Corredor corredor;
    private Fornecedor fornecedor;

    @BeforeEach
    void setUp() {
        corredor = new Corredor();
        corredor.setId("corredor-1");
        corredor.setNome("Corredor A");

        categoria = new Categoria();
        categoria.setId("categoria-1");
        categoria.setNome("Categoria A");
        categoria.setCorredor(corredor);

        Endereco endereco = new Endereco();
        endereco.setId(1);
        endereco.setCep("12345-678");
        endereco.setLogradouro("Rua Teste");
        endereco.setNumero("123");
        endereco.setBairro("Centro");
        endereco.setCidade("São Paulo");
        endereco.setEstado("SP");
        endereco.setPais("Brasil");

        fornecedor = new Fornecedor();
        fornecedor.setId(1);
        fornecedor.setNome("Fornecedor A");
        fornecedor.setCnpj("12.345.678/0001-90");
        fornecedor.setEndereco(endereco);

        produtoValido = new Produto();
        produtoValido.setId("produto-1");
        produtoValido.setCodigoBarras("7891234567890");
        produtoValido.setDescricao("Produto Teste");
        produtoValido.setMarca("Marca A");
        produtoValido.setUnidadeMedida("UN");
        produtoValido.setQuantidade(10);
        produtoValido.setCategoria(categoria);
        produtoValido.setFornecedores(Arrays.asList(fornecedor));
    }

    @Test
    @DisplayName("Deve buscar todos os produtos")
    void deveBuscarTodosProdutos() {
        // Given
        List<Produto> produtos = Arrays.asList(produtoValido);
        when(produtoRepository.findAll()).thenReturn(produtos);

        // When
        List<Produto> result = produtoService.buscarTodos();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(produtoValido, result.get(0));
        verify(produtoRepository).findAll();
    }

    @Test
    @DisplayName("Deve buscar produto por ID com sucesso")
    void deveBuscarProdutoPorIdComSucesso() throws SmartValidityException {
        // Given
        String id = "produto-1";
        when(produtoRepository.findById(id)).thenReturn(Optional.of(produtoValido));

        // When
        Produto result = produtoService.buscarPorId(id);

        // Then
        assertNotNull(result);
        assertEquals(produtoValido.getId(), result.getId());
        assertEquals(produtoValido.getDescricao(), result.getDescricao());
        assertEquals(produtoValido.getCodigoBarras(), result.getCodigoBarras());
        verify(produtoRepository).findById(id);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar produto inexistente")
    void deveLancarExcecaoAoBuscarProdutoInexistente() {
        // Given
        String idInexistente = "produto-inexistente";
        when(produtoRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // When & Then
        SmartValidityException exception = assertThrows(SmartValidityException.class,
            () -> produtoService.buscarPorId(idInexistente));

        assertEquals("Produto não encontrado com o ID: " + idInexistente, exception.getMessage());
        verify(produtoRepository).findById(idInexistente);
    }

    @Test
    @DisplayName("Deve salvar produto válido")
    void deveSalvarProdutoValido() {
        // Given
        when(produtoRepository.save(produtoValido)).thenReturn(produtoValido);

        // When
        Produto result = produtoService.salvar(produtoValido);

        // Then
        assertNotNull(result);
        assertEquals(produtoValido, result);
        verify(produtoRepository).save(produtoValido);
    }

    @Test
    @DisplayName("Deve atualizar produto existente")
    void deveAtualizarProdutoExistente() throws SmartValidityException {
        // Given
        String idProduto = "produto-1";
        Produto produtoAtualizado = new Produto();
        produtoAtualizado.setCodigoBarras("7891234567890");
        produtoAtualizado.setDescricao("Produto Atualizado");
        produtoAtualizado.setMarca("Marca Atualizada");
        produtoAtualizado.setUnidadeMedida("LT");
        produtoAtualizado.setQuantidade(20);
        produtoAtualizado.setCategoria(categoria);

        when(produtoRepository.findById(idProduto)).thenReturn(Optional.of(produtoValido));
        when(produtoRepository.save(any(Produto.class))).thenReturn(produtoAtualizado);

        // When
        Produto result = produtoService.atualizar(idProduto, produtoAtualizado);

        // Then
        assertNotNull(result);
        assertEquals("Produto Atualizado", result.getDescricao());
        assertEquals("Marca Atualizada", result.getMarca());
        assertEquals("LT", result.getUnidadeMedida());
        assertEquals(20, result.getQuantidade());

        verify(produtoRepository).findById(idProduto);
        verify(produtoRepository).save(any(Produto.class));
    }

    @Test
    @DisplayName("Deve excluir produto existente")
    void deveExcluirProdutoExistente() throws SmartValidityException {
        // Given
        String idProduto = "produto-1";
        when(produtoRepository.findById(idProduto)).thenReturn(Optional.of(produtoValido));

        // When
        produtoService.excluir(idProduto);

        // Then
        verify(produtoRepository).findById(idProduto);
        verify(produtoRepository).delete(produtoValido);
    }

    @Test
    @DisplayName("Deve buscar produtos por categoria")
    void deveBuscarProdutosPorCategoria() {
        // Given
        String categoriaId = "categoria-1";
        List<Produto> produtos = Arrays.asList(produtoValido);
        when(produtoRepository.findByCategoriaId(categoriaId)).thenReturn(produtos);

        // When
        List<Produto> result = produtoService.buscarPorCategoria(categoriaId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(produtoValido, result.get(0));
        verify(produtoRepository).findByCategoriaId(categoriaId);
    }

    @Test
    @DisplayName("Deve converter produto para DTO corretamente")
    void deveConverterProdutoParaDTOCorretamente() {
        // Given
        List<Produto> produtos = Arrays.asList(produtoValido);
        when(produtoRepository.findAll()).thenReturn(produtos);

        // When
        List<ProdutoDTO> result = produtoService.listarTodosDTO();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        
        ProdutoDTO dto = result.get(0);
        assertEquals(UUID.fromString(produtoValido.getId()), dto.getId());
        assertEquals(produtoValido.getCodigoBarras(), dto.getCodigoBarras());
        assertEquals(produtoValido.getDescricao(), dto.getDescricao());
        assertEquals(produtoValido.getMarca(), dto.getMarca());
        assertEquals(produtoValido.getUnidadeMedida(), dto.getUnidadeMedida());
        assertEquals(produtoValido.getQuantidade(), dto.getQuantidade());
        
        // Verificar categoria no DTO
        assertNotNull(dto.getCategoria());
        @SuppressWarnings("unchecked")
        Map<String, Object> categoriaMap = (Map<String, Object>) dto.getCategoria();
        assertEquals(categoria.getId(), categoriaMap.get("id"));
        assertEquals(categoria.getNome(), categoriaMap.get("nome"));

        verify(produtoRepository).findAll();
    }

    @Test
    @DisplayName("Deve pesquisar produtos com seletor sem paginação")
    void devePesquisarProdutosComSeletorSemPaginacao() {
        // Given
        ProdutoSeletor seletor = new ProdutoSeletor();
        seletor.setDescricao("Produto");
        
        List<Produto> produtos = Arrays.asList(produtoValido);
        when(produtoRepository.findAll(seletor)).thenReturn(produtos);

        // When
        List<Produto> result = produtoService.pesquisarComSeletor(seletor);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(produtoValido, result.get(0));
        verify(produtoRepository).findAll(seletor);
    }

    @Test
    @DisplayName("Deve pesquisar produtos com seletor com paginação")
    void devePesquisarProdutosComSeletorComPaginacao() {
        // Given
        ProdutoSeletor seletor = new ProdutoSeletor();
        seletor.setPagina(1);
        seletor.setLimite(10);
        seletor.setDescricao("Produto");
        
        List<Produto> produtos = Arrays.asList(produtoValido);
        Page<Produto> page = new PageImpl<>(produtos);
        
        when(produtoRepository.findAll(eq(seletor), any(PageRequest.class))).thenReturn(page);

        // When
        List<Produto> result = produtoService.pesquisarComSeletor(seletor);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(produtoValido, result.get(0));
        verify(produtoRepository).findAll(eq(seletor), any(PageRequest.class));
    }

    @Test
    @DisplayName("Deve contar páginas corretamente com paginação")
    void deveContarPaginasCorretamenteComPaginacao() {
        // Given
        ProdutoSeletor seletor = new ProdutoSeletor();
        seletor.setPagina(1);
        seletor.setLimite(5);
        
        List<Produto> produtos = Arrays.asList(produtoValido);
        Page<Produto> page = new PageImpl<>(produtos, PageRequest.of(0, 5), 1);
        
        when(produtoRepository.findAll(eq(seletor), any(PageRequest.class))).thenReturn(page);

        // When
        int totalPaginas = produtoService.contarPaginas(seletor);

        // Then
        assertEquals(1, totalPaginas);
        verify(produtoRepository).findAll(eq(seletor), any(PageRequest.class));
    }

    @Test
    @DisplayName("Deve contar total de registros corretamente")
    void deveContarTotalRegistrosCorretamente() {
        // Given
        ProdutoSeletor seletor = new ProdutoSeletor();
        seletor.setDescricao("Produto");
        
        when(produtoRepository.count(seletor)).thenReturn(5L);

        // When
        long totalRegistros = produtoService.contarTotalRegistros(seletor);

        // Then
        assertEquals(5L, totalRegistros);
        verify(produtoRepository).count(seletor);
    }

    @Test
    @DisplayName("Não deve salvar produto com código de barras inválido")
    void naoDeveSalvarProdutoComCodigoBarrasInvalido() {
        // Given
        Produto produtoInvalido = new Produto();
        produtoInvalido.setCodigoBarras("123"); // Código de barras muito curto
        produtoInvalido.setDescricao("Produto Inválido");
        produtoInvalido.setMarca("Marca");
        produtoInvalido.setUnidadeMedida("UN");
        produtoInvalido.setQuantidade(1);

        // Simular erro de validação do JPA/Hibernate
        when(produtoRepository.save(produtoInvalido)).thenThrow(new RuntimeException("Código de barras inválido"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> produtoService.salvar(produtoInvalido));

        assertTrue(exception.getMessage().contains("Código de barras inválido"));
    }

    @Test
    @DisplayName("Não deve salvar produto com quantidade nula")
    void naoDeveSalvarProdutoComQuantidadeNula() {
        // Given
        Produto produtoInvalido = new Produto();
        produtoInvalido.setCodigoBarras("7891234567890");
        produtoInvalido.setDescricao("Produto Inválido");
        produtoInvalido.setMarca("Marca");
        produtoInvalido.setUnidadeMedida("UN");
        produtoInvalido.setQuantidade(null); // Quantidade nula

        // Simular erro de validação do JPA/Hibernate
        when(produtoRepository.save(produtoInvalido)).thenThrow(new RuntimeException("Quantidade não pode ser nula"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> produtoService.salvar(produtoInvalido));

        assertTrue(exception.getMessage().contains("Quantidade não pode ser nula"));
    }
} 