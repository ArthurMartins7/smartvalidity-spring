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

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.entity.Categoria;
import br.com.smartvalidity.model.entity.Corredor;
import br.com.smartvalidity.model.repository.CategoriaRepository;
import br.com.smartvalidity.service.CategoriaService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do CategoriaService")
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    private Categoria categoriaValida;
    private Corredor corredor;

    @BeforeEach
    void setUp() {
        corredor = new Corredor();
        corredor.setId("corredor-1");
        corredor.setNome("Corredor A");

        categoriaValida = new Categoria();
        categoriaValida.setId("categoria-1");
        categoriaValida.setNome("Categoria A");
        categoriaValida.setCorredor(corredor);
    }

    @Test
    @DisplayName("Deve buscar todas as categorias")
    void deveBuscarTodasCategorias() {
        // Given
        List<Categoria> categorias = Arrays.asList(categoriaValida);
        when(categoriaRepository.findAll()).thenReturn(categorias);

        // When
        List<Categoria> result = categoriaService.buscarTodas();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(categoriaValida, result.get(0));
        verify(categoriaRepository).findAll();
    }

    @Test
    @DisplayName("Deve buscar categoria por ID com sucesso")
    void deveBuscarCategoriaPorIdComSucesso() throws SmartValidityException {
        // Given
        String id = "categoria-1";
        when(categoriaRepository.findById(id)).thenReturn(Optional.of(categoriaValida));

        // When
        Categoria result = categoriaService.buscarPorId(id);

        // Then
        assertNotNull(result);
        assertEquals(categoriaValida.getId(), result.getId());
        assertEquals(categoriaValida.getNome(), result.getNome());
        assertEquals(categoriaValida.getCorredor(), result.getCorredor());
        verify(categoriaRepository).findById(id);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar categoria inexistente")
    void deveLancarExcecaoAoBuscarCategoriaInexistente() {
        // Given
        String idInexistente = "categoria-inexistente";
        when(categoriaRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // When & Then
        SmartValidityException exception = assertThrows(SmartValidityException.class,
            () -> categoriaService.buscarPorId(idInexistente));

        assertEquals("Categoria não encontrada com o ID: " + idInexistente, exception.getMessage());
        verify(categoriaRepository).findById(idInexistente);
    }

    @Test
    @DisplayName("Deve salvar categoria válida")
    void deveSalvarCategoriaValida() {
        // Given
        Categoria novaCategoria = new Categoria();
        novaCategoria.setNome("Categoria B");
        novaCategoria.setCorredor(corredor);

        when(categoriaRepository.save(novaCategoria)).thenReturn(novaCategoria);

        // When
        Categoria result = categoriaService.salvar(novaCategoria);

        // Then
        assertNotNull(result);
        assertEquals(novaCategoria.getNome(), result.getNome());
        assertEquals(corredor, result.getCorredor());
        verify(categoriaRepository).save(novaCategoria);
    }

    @Test
    @DisplayName("Não deve salvar categoria com nome vazio")
    void naoDeveSalvarCategoriaComNomeVazio() {
        // Given
        Categoria categoriaInvalida = new Categoria();
        categoriaInvalida.setNome(""); // Nome vazio
        categoriaInvalida.setCorredor(corredor);

        // Simular erro de validação
        when(categoriaRepository.save(categoriaInvalida))
            .thenThrow(new RuntimeException("Nome não pode ser vazio"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> categoriaService.salvar(categoriaInvalida));

        assertTrue(exception.getMessage().contains("Nome não pode ser vazio"));
    }

    @Test
    @DisplayName("Deve salvar categoria válida com associação a corredor")
    void deveSalvarCategoriaValidaComAssociacaoACorredor() {
        // Given
        Categoria categoriaComCorredor = new Categoria();
        categoriaComCorredor.setNome("Categoria com Corredor");
        categoriaComCorredor.setCorredor(corredor);

        when(categoriaRepository.save(categoriaComCorredor)).thenReturn(categoriaComCorredor);

        // When
        Categoria result = categoriaService.salvar(categoriaComCorredor);

        // Then
        assertNotNull(result);
        assertEquals("Categoria com Corredor", result.getNome());
        assertNotNull(result.getCorredor());
        assertEquals(corredor.getId(), result.getCorredor().getId());
        assertEquals(corredor.getNome(), result.getCorredor().getNome());
        verify(categoriaRepository).save(categoriaComCorredor);
    }

    @Test
    @DisplayName("Deve atualizar nome da categoria")
    void deveAtualizarNomeDaCategoria() throws SmartValidityException {
        // Given
        String idCategoria = "categoria-1";
        Categoria categoriaAtualizada = new Categoria();
        categoriaAtualizada.setNome("Categoria Atualizada");
        categoriaAtualizada.setCorredor(corredor);

        when(categoriaRepository.findById(idCategoria)).thenReturn(Optional.of(categoriaValida));
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoriaAtualizada);

        // When
        Categoria result = categoriaService.atualizar(idCategoria, categoriaAtualizada);

        // Then
        assertNotNull(result);
        assertEquals("Categoria Atualizada", result.getNome());
        assertEquals(corredor, result.getCorredor());

        verify(categoriaRepository).findById(idCategoria);
        verify(categoriaRepository).save(any(Categoria.class));
    }

    @Test
    @DisplayName("Deve atualizar nome e corredor da categoria")
    void deveAtualizarNomeECorredorDaCategoria() throws SmartValidityException {
        // Given
        String idCategoria = "categoria-1";
        
        Corredor novoCorredor = new Corredor();
        novoCorredor.setId("corredor-2");
        novoCorredor.setNome("Corredor B");
        
        Categoria categoriaAtualizada = new Categoria();
        categoriaAtualizada.setNome("Categoria Mudou de Corredor");
        categoriaAtualizada.setCorredor(novoCorredor);

        when(categoriaRepository.findById(idCategoria)).thenReturn(Optional.of(categoriaValida));
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoriaAtualizada);

        // When
        Categoria result = categoriaService.atualizar(idCategoria, categoriaAtualizada);

        // Then
        assertNotNull(result);
        assertEquals("Categoria Mudou de Corredor", result.getNome());
        assertEquals(novoCorredor, result.getCorredor());
        assertEquals("corredor-2", result.getCorredor().getId());

        verify(categoriaRepository).findById(idCategoria);
        verify(categoriaRepository).save(any(Categoria.class));
    }

    @Test
    @DisplayName("Não deve atualizar categoria com ID inexistente")
    void naoDeveAtualizarCategoriaComIdInexistente() {
        // Given
        String idInexistente = "categoria-inexistente";
        Categoria categoriaAtualizada = new Categoria();
        categoriaAtualizada.setNome("Nome Qualquer");

        when(categoriaRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // When & Then
        SmartValidityException exception = assertThrows(SmartValidityException.class,
            () -> categoriaService.atualizar(idInexistente, categoriaAtualizada));

        assertEquals("Categoria não encontrada com o ID: " + idInexistente, exception.getMessage());
        verify(categoriaRepository).findById(idInexistente);
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve excluir categoria existente")
    void deveExcluirCategoriaExistente() throws SmartValidityException {
        // Given
        String idCategoria = "categoria-1";
        when(categoriaRepository.findById(idCategoria)).thenReturn(Optional.of(categoriaValida));

        // When
        categoriaService.excluir(idCategoria);

        // Then
        verify(categoriaRepository).findById(idCategoria);
        verify(categoriaRepository).delete(categoriaValida);
    }

    @Test
    @DisplayName("Não deve excluir categoria inexistente")
    void naoDeveExcluirCategoriaInexistente() {
        // Given
        String idInexistente = "categoria-inexistente";
        when(categoriaRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // When & Then
        SmartValidityException exception = assertThrows(SmartValidityException.class,
            () -> categoriaService.excluir(idInexistente));

        assertEquals("Categoria não encontrada com o ID: " + idInexistente, exception.getMessage());
        verify(categoriaRepository).findById(idInexistente);
        verify(categoriaRepository, never()).delete(any(Categoria.class));
    }

    @Test
    @DisplayName("Deve manter integridade da associação com corredor")
    void deveManterIntegridadeDaAssociacaoComCorredor() {
        // Given
        Categoria categoriaComAssociacao = new Categoria();
        categoriaComAssociacao.setNome("Categoria com Associação Íntegra");
        categoriaComAssociacao.setCorredor(corredor);

        when(categoriaRepository.save(categoriaComAssociacao)).thenReturn(categoriaComAssociacao);

        // When
        Categoria result = categoriaService.salvar(categoriaComAssociacao);

        // Then
        assertNotNull(result);
        assertNotNull(result.getCorredor());
        assertEquals(corredor.getId(), result.getCorredor().getId());
        assertEquals(corredor.getNome(), result.getCorredor().getNome());
        
        // Verifica se a associação é bidirecional mantida
        assertSame(corredor, result.getCorredor());
        
        verify(categoriaRepository).save(categoriaComAssociacao);
    }

    @Test
    @DisplayName("Deve permitir categoria sem corredor associado")
    void devePermitirCategoriaSemCorredorAssociado() {
        // Given
        Categoria categoriaSemCorredor = new Categoria();
        categoriaSemCorredor.setNome("Categoria Sem Corredor");
        categoriaSemCorredor.setCorredor(null);

        when(categoriaRepository.save(categoriaSemCorredor)).thenReturn(categoriaSemCorredor);

        // When
        Categoria result = categoriaService.salvar(categoriaSemCorredor);

        // Then
        assertNotNull(result);
        assertEquals("Categoria Sem Corredor", result.getNome());
        assertNull(result.getCorredor());
        verify(categoriaRepository).save(categoriaSemCorredor);
    }
} 