package br.com.smartvalidity.java.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
import br.com.smartvalidity.model.entity.Fornecedor;
import br.com.smartvalidity.model.repository.FornecedorRepository;
import br.com.smartvalidity.model.seletor.FornecedorSeletor;
import br.com.smartvalidity.service.FornecedorService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do FornecedorService")
class FornecedorServiceTest {

    @Mock
    private FornecedorRepository fornecedorRepository;

    @InjectMocks
    private FornecedorService fornecedorService;

    private Fornecedor fornecedorValido;

    @BeforeEach
    void setUp() {
        fornecedorValido = new Fornecedor();
        fornecedorValido.setId(1);
        fornecedorValido.setNome("Fornecedor Teste");
        fornecedorValido.setTelefone("(11) 99999-9999");
        fornecedorValido.setCnpj("12.345.678/0001-90");
    }

    @Test
    @DisplayName("Deve salvar fornecedor com sucesso")
    void deveSalvarFornecedorComSucesso() throws SmartValidityException {
        // Given
        when(fornecedorRepository.save(fornecedorValido)).thenReturn(fornecedorValido);

        // When
        Fornecedor result = fornecedorService.salvar(fornecedorValido);

        // Then
        assertNotNull(result);
        assertEquals(fornecedorValido.getNome(), result.getNome());
        assertEquals(fornecedorValido.getTelefone(), result.getTelefone());
        assertEquals(fornecedorValido.getCnpj(), result.getCnpj());
        
        verify(fornecedorRepository).save(fornecedorValido);
    }

    @Test
    @DisplayName("Deve lançar exceção ao salvar fornecedor com erro")
    void deveLancarExcecaoAoSalvarFornecedorComErro() {
        // Given
        when(fornecedorRepository.save(fornecedorValido))
            .thenThrow(new RuntimeException("Erro de BD"));

        // When & Then
        SmartValidityException exception = assertThrows(SmartValidityException.class,
            () -> fornecedorService.salvar(fornecedorValido));
        
        assertTrue(exception.getMessage().contains("Erro ao salvar fornecedor"));
        verify(fornecedorRepository).save(fornecedorValido);
    }

    @Test
    @DisplayName("Deve listar todos os fornecedores")
    void deveListarTodosFornecedores() {
        // Given
        List<Fornecedor> fornecedores = Arrays.asList(fornecedorValido);
        when(fornecedorRepository.findAll()).thenReturn(fornecedores);

        // When
        List<Fornecedor> result = fornecedorService.listarTodos();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(fornecedorValido, result.get(0));
        
        verify(fornecedorRepository).findAll();
    }

    @Test
    @DisplayName("Deve buscar fornecedor por ID com sucesso")
    void deveBuscarFornecedorPorIdComSucesso() throws SmartValidityException {
        // Given
        Integer id = 1;
        when(fornecedorRepository.findById(id)).thenReturn(Optional.of(fornecedorValido));

        // When
        Fornecedor result = fornecedorService.buscarPorId(id);

        // Then
        assertNotNull(result);
        assertEquals(fornecedorValido.getId(), result.getId());
        assertEquals(fornecedorValido.getNome(), result.getNome());
        
        verify(fornecedorRepository).findById(id);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar fornecedor inexistente")
    void deveLancarExcecaoAoBuscarFornecedorInexistente() {
        // Given
        Integer idInexistente = 999;
        when(fornecedorRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // When & Then
        SmartValidityException exception = assertThrows(SmartValidityException.class,
            () -> fornecedorService.buscarPorId(idInexistente));
        
        assertEquals("Fornecedor não encontrado com o ID: " + idInexistente, exception.getMessage());
        verify(fornecedorRepository).findById(idInexistente);
    }

    @Test
    @DisplayName("Deve atualizar fornecedor existente")
    void deveAtualizarFornecedorExistente() throws SmartValidityException {
        // Given
        Integer id = 1;
        Fornecedor fornecedorAtualizado = new Fornecedor();
        fornecedorAtualizado.setNome("Fornecedor Atualizado");
        fornecedorAtualizado.setTelefone("(11) 88888-8888");
        fornecedorAtualizado.setCnpj("98.765.432/0001-10");

        when(fornecedorRepository.findById(id)).thenReturn(Optional.of(fornecedorValido));
        when(fornecedorRepository.save(any(Fornecedor.class))).thenReturn(fornecedorAtualizado);

        // When
        Fornecedor result = fornecedorService.atualizar(id, fornecedorAtualizado);

        // Then
        assertNotNull(result);
        assertEquals("Fornecedor Atualizado", result.getNome());
        assertEquals("(11) 88888-8888", result.getTelefone());
        assertEquals("98.765.432/0001-10", result.getCnpj());
        
        verify(fornecedorRepository).findById(id);
        verify(fornecedorRepository).save(any(Fornecedor.class));
    }

    @Test
    @DisplayName("Deve excluir fornecedor existente")
    void deveExcluirFornecedorExistente() throws SmartValidityException {
        // Given
        Integer id = 1;
        when(fornecedorRepository.findById(id)).thenReturn(Optional.of(fornecedorValido));

        // When
        fornecedorService.excluir(id);

        // Then
        verify(fornecedorRepository).findById(id);
        verify(fornecedorRepository).delete(fornecedorValido);
    }

    @Test
    @DisplayName("Deve pesquisar fornecedores com seletor sem paginação")
    void devePesquisarFornecedoresComSeletorSemPaginacao() {
        // Given
        FornecedorSeletor seletor = new FornecedorSeletor();
        List<Fornecedor> fornecedores = Arrays.asList(fornecedorValido);
        when(fornecedorRepository.findAll(seletor)).thenReturn(fornecedores);

        // When
        List<Fornecedor> result = fornecedorService.pesquisarComSeletor(seletor);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(fornecedorValido, result.get(0));
        
        verify(fornecedorRepository).findAll(seletor);
    }

    @Test
    @DisplayName("Deve pesquisar fornecedores com seletor com paginação")
    void devePesquisarFornecedoresComSeletorComPaginacao() {
        // Given
        FornecedorSeletor seletor = new FornecedorSeletor();
        seletor.setPagina(1);
        seletor.setLimite(10);
        
        List<Fornecedor> fornecedores = Arrays.asList(fornecedorValido);
        Page<Fornecedor> page = new PageImpl<>(fornecedores);
        
        when(fornecedorRepository.findAll(eq(seletor), any(PageRequest.class))).thenReturn(page);

        // When
        List<Fornecedor> result = fornecedorService.pesquisarComSeletor(seletor);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(fornecedorValido, result.get(0));
        
        verify(fornecedorRepository).findAll(eq(seletor), any(PageRequest.class));
    }

    @Test
    @DisplayName("Deve contar páginas corretamente com paginação")
    void deveContarPaginasCorretamenteComPaginacao() {
        // Given
        FornecedorSeletor seletor = new FornecedorSeletor();
        seletor.setPagina(1);
        seletor.setLimite(5);
        
        List<Fornecedor> fornecedores = Arrays.asList(fornecedorValido);
        Page<Fornecedor> page = new PageImpl<>(fornecedores, PageRequest.of(0, 5), 1);
        
        when(fornecedorRepository.findAll(eq(seletor), any(PageRequest.class))).thenReturn(page);

        // When
        int totalPaginas = fornecedorService.contarPaginas(seletor);

        // Then
        assertEquals(1, totalPaginas);
        verify(fornecedorRepository).findAll(eq(seletor), any(PageRequest.class));
    }

    @Test
    @DisplayName("Deve contar páginas quando não há paginação")
    void deveContarPaginasQuandoNaoHaPaginacao() {
        // Given
        FornecedorSeletor seletor = new FornecedorSeletor();
        when(fornecedorRepository.count(seletor)).thenReturn(5L);

        // When
        int result = fornecedorService.contarPaginas(seletor);

        // Then
        assertEquals(1, result);
        verify(fornecedorRepository).count(seletor);
    }

    @Test
    @DisplayName("Deve retornar 0 páginas quando não há registros")
    void deveRetornarZeroPaginasQuandoNaoHaRegistros() {
        // Given
        FornecedorSeletor seletor = new FornecedorSeletor();
        when(fornecedorRepository.count(seletor)).thenReturn(0L);

        // When
        int result = fornecedorService.contarPaginas(seletor);

        // Then
        assertEquals(0, result);
        verify(fornecedorRepository).count(seletor);
    }

    @Test
    @DisplayName("Deve contar total de registros")
    void deveContarTotalRegistros() {
        // Given
        FornecedorSeletor seletor = new FornecedorSeletor();
        when(fornecedorRepository.count(seletor)).thenReturn(10L);

        // When
        long result = fornecedorService.contarTotalRegistros(seletor);

        // Then
        assertEquals(10L, result);
        verify(fornecedorRepository).count(seletor);
    }
} 