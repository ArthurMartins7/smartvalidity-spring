package br.com.smartvalidity.java.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.entity.Corredor;
import br.com.smartvalidity.model.entity.Usuario;
import br.com.smartvalidity.model.enums.PerfilAcesso;
import br.com.smartvalidity.model.repository.CorredorRepository;
import br.com.smartvalidity.model.seletor.CorredorSeletor;
import br.com.smartvalidity.service.CorredorService;
import br.com.smartvalidity.service.ImagemService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do CorredorService")
class CorredorServiceTest {

    @Mock
    private CorredorRepository corredorRepository;

    @Mock
    private ImagemService imagemService;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private CorredorService corredorService;

    private Corredor corredorValido;
    private Usuario responsavel1;
    private Usuario responsavel2;

    @BeforeEach
    void setUp() {
        responsavel1 = new Usuario();
        responsavel1.setId(UUID.randomUUID().toString());
        responsavel1.setNome("João Silva");
        responsavel1.setEmail("joao@teste.com");
        responsavel1.setCpf("123.456.789-09");
        responsavel1.setPerfilAcesso(PerfilAcesso.OPERADOR);

        responsavel2 = new Usuario();
        responsavel2.setId(UUID.randomUUID().toString());
        responsavel2.setNome("Maria Santos");
        responsavel2.setEmail("maria@teste.com");
        responsavel2.setCpf("987.654.321-00");
        responsavel2.setPerfilAcesso(PerfilAcesso.OPERADOR);

        List<Usuario> responsaveis = Arrays.asList(responsavel1, responsavel2);

        corredorValido = new Corredor();
        corredorValido.setId(UUID.randomUUID().toString());
        corredorValido.setNome("Corredor A");
        corredorValido.setResponsaveis(responsaveis);
        corredorValido.setImagemEmBase64("imagemBase64Exemplo");
    }

    @Test
    @DisplayName("Deve buscar todos os corredores")
    void deveBuscarTodosCorredores() {
        List<Corredor> corredores = Arrays.asList(corredorValido);
        when(corredorRepository.findAll()).thenReturn(corredores);
        List<Corredor> result = corredorService.listarTodos();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(corredorValido, result.get(0));
        verify(corredorRepository).findAll();
    }

    @Test
    @DisplayName("Deve buscar corredor por ID com sucesso")
    void deveBuscarCorredorPorIdComSucesso() throws SmartValidityException {
        // Given
        String id = corredorValido.getId();
        when(corredorRepository.findById(id)).thenReturn(Optional.of(corredorValido));

        // When
        Corredor result = corredorService.buscarPorId(id);

        // Then
        assertNotNull(result);
        assertEquals(corredorValido.getId(), result.getId());
        assertEquals(corredorValido.getNome(), result.getNome());
        verify(corredorRepository).findById(id);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar corredor inexistente")
    void deveLancarExcecaoAoBuscarCorredorInexistente() {
        // Given
        String idInexistente = UUID.randomUUID().toString();
        when(corredorRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // When & Then
        SmartValidityException exception = assertThrows(SmartValidityException.class,
            () -> corredorService.buscarPorId(idInexistente));

        assertEquals("Corredor não encontrado com o ID: " + idInexistente, exception.getMessage());
        verify(corredorRepository).findById(idInexistente);
    }

    @Test
    @DisplayName("Deve salvar corredor válido")
    void deveSalvarCorredorValido() throws SmartValidityException {
        // Given
        Corredor novoCorredor = new Corredor();
        novoCorredor.setNome("Corredor B");
        novoCorredor.setResponsaveis(Arrays.asList(responsavel1));

        when(corredorRepository.save(novoCorredor)).thenReturn(novoCorredor);

        // When
        Corredor result = corredorService.salvar(novoCorredor);

        // Then
        assertNotNull(result);
        assertEquals(novoCorredor.getNome(), result.getNome());
        assertEquals(1, result.getResponsaveis().size());
        verify(corredorRepository).save(novoCorredor);
    }

    @Test
    @DisplayName("Deve cadastrar corredor com múltiplos responsáveis")
    void deveCadastrarCorredorComResponsaveis() throws SmartValidityException {
        // Given
        Corredor corredorComResponsaveis = new Corredor();
        corredorComResponsaveis.setNome("Corredor Multi-Responsável");
        corredorComResponsaveis.setResponsaveis(Arrays.asList(responsavel1, responsavel2));

        when(corredorRepository.save(corredorComResponsaveis)).thenReturn(corredorComResponsaveis);

        // When
        Corredor result = corredorService.salvar(corredorComResponsaveis);

        // Then
        assertNotNull(result);
        assertEquals("Corredor Multi-Responsável", result.getNome());
        assertEquals(2, result.getResponsaveis().size());
        assertTrue(result.getResponsaveis().contains(responsavel1));
        assertTrue(result.getResponsaveis().contains(responsavel2));
        verify(corredorRepository).save(corredorComResponsaveis);
    }

    @Test
    @DisplayName("Não deve salvar corredor com nome vazio")
    void naoDeveSalvarCorredorComNomeVazio() {
        // Given
        Corredor corredorInvalido = new Corredor();
        corredorInvalido.setNome(""); // Nome vazio
        corredorInvalido.setResponsaveis(Arrays.asList(responsavel1));

        when(corredorRepository.save(corredorInvalido))
            .thenThrow(new RuntimeException("Nome não pode ser vazio"));

        // When & Then
        SmartValidityException exception = assertThrows(SmartValidityException.class,
            () -> corredorService.salvar(corredorInvalido));

        assertTrue(exception.getMessage().contains("Erro ao salvar corredor"));
    }

    @Test
    @DisplayName("Deve atualizar corredor existente")
    void deveAtualizarCorredorExistente() throws SmartValidityException {
        String idCorredor = corredorValido.getId();
        Corredor corredorAtualizado = new Corredor();
        corredorAtualizado.setNome("Corredor Atualizado");
        corredorAtualizado.setResponsaveis(Arrays.asList(responsavel2));
        when(corredorRepository.findById(idCorredor)).thenReturn(Optional.of(corredorValido));
        when(corredorRepository.save(any(Corredor.class))).thenReturn(corredorAtualizado);
        Corredor result = corredorService.atualizar(idCorredor, corredorAtualizado);
        assertNotNull(result);
        assertEquals("Corredor Atualizado", result.getNome());
        assertEquals(1, result.getResponsaveis().size());
        assertTrue(result.getResponsaveis().contains(responsavel2));
        verify(corredorRepository).findById(idCorredor);
        verify(corredorRepository).save(any(Corredor.class));
    }

    @Test
    @DisplayName("Deve excluir corredor existente")
    void deveExcluirCorredorExistente() throws SmartValidityException {
        String idCorredor = corredorValido.getId();
        when(corredorRepository.findById(idCorredor)).thenReturn(Optional.of(corredorValido));
        corredorService.excluir(idCorredor);
        verify(corredorRepository).findById(idCorredor);
        verify(corredorRepository).delete(corredorValido);
    }

    @Test
    @DisplayName("Deve salvar imagem do corredor")
    void deveSalvarImagemDoCorredor() throws SmartValidityException {
        String idCorredor = corredorValido.getId();
        String imagemBase64 = "imagemBase64Processada";
        when(corredorRepository.findById(idCorredor)).thenReturn(Optional.of(corredorValido));
        when(imagemService.processarImagem(multipartFile)).thenReturn(imagemBase64);
        when(corredorRepository.save(any(Corredor.class))).thenReturn(corredorValido);

        // When
        corredorService.salvarImagemCorredor(multipartFile, idCorredor);

        // Then
        verify(corredorRepository).findById(idCorredor);
        verify(imagemService).processarImagem(multipartFile);
        verify(corredorRepository).save(any(Corredor.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao salvar imagem de corredor inexistente")
    void deveLancarExcecaoAoSalvarImagemCorredorInexistente() throws SmartValidityException {
        // Given
        String idInexistente = UUID.randomUUID().toString();
        when(corredorRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // When & Then
        SmartValidityException exception = assertThrows(SmartValidityException.class,
            () -> corredorService.salvarImagemCorredor(multipartFile, idInexistente));

        assertEquals("Corredor não encontrada", exception.getMessage());
        verify(corredorRepository).findById(idInexistente);
        verify(imagemService, never()).processarImagem(any());
        verify(corredorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve pesquisar corredores com seletor sem paginação")
    void devePesquisarCorredoresComSeletorSemPaginacao() {
        // Given
        CorredorSeletor seletor = new CorredorSeletor();
        seletor.setNome("Corredor");
        
        List<Corredor> corredores = Arrays.asList(corredorValido);
        when(corredorRepository.findAll(seletor)).thenReturn(corredores);

        // When
        List<Corredor> result = corredorService.pesquisarComSeletor(seletor);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(corredorValido, result.get(0));
        verify(corredorRepository).findAll(seletor);
    }

    @Test
    @DisplayName("Deve pesquisar corredores com seletor com paginação")
    void devePesquisarCorredoresComSeletorComPaginacao() {
        // Given
        CorredorSeletor seletor = new CorredorSeletor();
        seletor.setPagina(1);
        seletor.setLimite(10);
        seletor.setNome("Corredor");
        
        List<Corredor> corredores = Arrays.asList(corredorValido);
        Page<Corredor> page = new PageImpl<>(corredores);
        
        when(corredorRepository.findAll(eq(seletor), any(PageRequest.class))).thenReturn(page);

        // When
        List<Corredor> result = corredorService.pesquisarComSeletor(seletor);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(corredorValido, result.get(0));
        verify(corredorRepository).findAll(eq(seletor), any(PageRequest.class));
    }

    @Test
    @DisplayName("Deve contar páginas corretamente")
    void deveContarPaginasCorretamente() {
        // Given
        CorredorSeletor seletor = new CorredorSeletor();
        seletor.setPagina(1);
        seletor.setLimite(5);
        seletor.setNome("Corredor");
        seletor.setResponsavelId(responsavel1.getId());
        
        Page<Corredor> page = new PageImpl<>(Arrays.asList(corredorValido), PageRequest.of(0, 5), 1);
        
        when(corredorRepository.findByFiltros(eq("Corredor"), eq(responsavel1.getId()), any(PageRequest.class)))
            .thenReturn(page);

        // When
        int totalPaginas = corredorService.contarPaginas(seletor);

        // Then
        assertEquals(1, totalPaginas);
        verify(corredorRepository).findByFiltros(eq("Corredor"), eq(responsavel1.getId()), any(PageRequest.class));
    }

    @Test
    @DisplayName("Deve contar total de registros com filtro")
    void deveContarTotalRegistrosComFiltro() {
        // Given
        CorredorSeletor seletor = new CorredorSeletor();
        seletor.setNome("Corredor");
        seletor.setResponsavelId(responsavel1.getId());
        
        List<Corredor> corredores = Arrays.asList(corredorValido);
        when(corredorRepository.findByFiltros("Corredor", responsavel1.getId())).thenReturn(corredores);

        // When
        long totalRegistros = corredorService.contarTotalRegistros(seletor);

        // Then
        assertEquals(1L, totalRegistros);
        verify(corredorRepository).findByFiltros("Corredor", responsavel1.getId());
    }

    @Test
    @DisplayName("Deve contar total de registros sem filtro")
    void deveContarTotalRegistrosSemFiltro() {
        // Given
        CorredorSeletor seletor = new CorredorSeletor();
        seletor.setNome("Corredor");
        // Sem responsavelId
        
        when(corredorRepository.count()).thenReturn(5L);

        // When
        long totalRegistros = corredorService.contarTotalRegistros(seletor);

        // Then
        assertEquals(5L, totalRegistros);
        verify(corredorRepository).count();
    }

    @Test
    @DisplayName("Deve filtrar por nome e responsável")
    void deveFiltrarPorNomeEResponsavel() {
        // Given
        CorredorSeletor seletor = new CorredorSeletor();
        seletor.setNome("Corredor A");
        seletor.setResponsavelId(responsavel1.getId());
        
        List<Corredor> corredores = Arrays.asList(corredorValido);
        when(corredorRepository.findAll(seletor)).thenReturn(corredores);

        // When
        List<Corredor> result = corredorService.pesquisarComSeletor(seletor);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(corredorValido, result.get(0));

        // Verifica se o corredor encontrado tem o responsável esperado
        assertTrue(result.get(0).getResponsaveis().stream()
            .anyMatch(resp -> resp.getId().equals(responsavel1.getId())));
            
        verify(corredorRepository).findAll(seletor);
    }
} 