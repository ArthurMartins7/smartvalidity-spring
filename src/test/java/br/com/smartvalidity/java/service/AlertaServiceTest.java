package br.com.smartvalidity.java.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
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

import br.com.smartvalidity.model.dto.AlertaRequestDTO;
import br.com.smartvalidity.model.dto.AlertaResponseDTO;
import br.com.smartvalidity.model.entity.Alerta;
import br.com.smartvalidity.model.enums.FrequenciaDisparo;
import br.com.smartvalidity.model.repository.AlertaRepository;
import br.com.smartvalidity.service.AlertaService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do AlertaService")
class AlertaServiceTest {

    @Mock
    private AlertaRepository alertaRepository;

    @InjectMocks
    private AlertaService alertaService;

    private Alerta alertaValido;
    private AlertaRequestDTO alertaRequestDTO;

    @BeforeEach
    void setUp() {
        alertaValido = new Alerta();
        alertaValido.setId(1);
        alertaValido.setTitulo("Alerta Teste");
        alertaValido.setDescricao("Descrição do alerta teste");
        alertaValido.setDataHoraDisparo(LocalDateTime.now().plusDays(1));
        alertaValido.setDisparoRecorrente(false);
        alertaValido.setFrequenciaDisparo(FrequenciaDisparo.DIARIO);

        alertaRequestDTO = new AlertaRequestDTO();
        alertaRequestDTO.setTitulo("Alerta Teste");
        alertaRequestDTO.setDescricao("Descrição do alerta teste");
        alertaRequestDTO.setDataHoraDisparo(LocalDateTime.now().plusDays(1));
        alertaRequestDTO.setDisparoRecorrente(false);
        alertaRequestDTO.setFrequenciaDisparo(FrequenciaDisparo.DIARIO);
    }

    @Test
    @DisplayName("Deve criar alerta com sucesso")
    void deveCriarAlertaComSucesso() {
        // Given
        when(alertaRepository.save(any(Alerta.class))).thenReturn(alertaValido);

        // When
        AlertaResponseDTO result = alertaService.create(alertaRequestDTO);

        // Then
        assertNotNull(result);
        assertEquals(alertaValido.getTitulo(), result.getTitulo());
        assertEquals(alertaValido.getDescricao(), result.getDescricao());
        assertEquals(alertaValido.isDisparoRecorrente(), result.isDisparoRecorrente());
        assertEquals(alertaValido.getFrequenciaDisparo(), result.getFrequenciaDisparo());
        
        verify(alertaRepository).save(any(Alerta.class));
    }

    @Test
    @DisplayName("Deve buscar todos os alertas")
    void deveBuscarTodosAlertas() {
        // Given
        List<Alerta> alertas = Arrays.asList(alertaValido);
        when(alertaRepository.findAll()).thenReturn(alertas);

        // When
        List<AlertaResponseDTO> result = alertaService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(alertaValido.getTitulo(), result.get(0).getTitulo());
        assertEquals(alertaValido.getDescricao(), result.get(0).getDescricao());
        
        verify(alertaRepository).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há alertas")
    void deveRetornarListaVaziaQuandoNaoHaAlertas() {
        // Given
        when(alertaRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<AlertaResponseDTO> result = alertaService.findAll();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(alertaRepository).findAll();
    }

    @Test
    @DisplayName("Deve buscar alerta por ID com sucesso")
    void deveBuscarAlertaPorIdComSucesso() {
        // Given
        Integer id = 1;
        when(alertaRepository.findById(id)).thenReturn(Optional.of(alertaValido));

        // When
        Optional<AlertaResponseDTO> result = alertaService.findById(id);

        // Then
        assertTrue(result.isPresent());
        assertEquals(alertaValido.getTitulo(), result.get().getTitulo());
        assertEquals(alertaValido.getDescricao(), result.get().getDescricao());
        
        verify(alertaRepository).findById(id);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando alerta não existe")
    void deveRetornarOptionalVazioQuandoAlertaNaoExiste() {
        // Given
        Integer idInexistente = 999;
        when(alertaRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // When
        Optional<AlertaResponseDTO> result = alertaService.findById(idInexistente);

        // Then
        assertFalse(result.isPresent());
        
        verify(alertaRepository).findById(idInexistente);
    }

    @Test
    @DisplayName("Deve atualizar alerta existente com sucesso")
    void deveAtualizarAlertaExistenteComSucesso() {
        // Given
        Integer id = 1;
        AlertaRequestDTO alertaAtualizado = new AlertaRequestDTO();
        alertaAtualizado.setTitulo("Alerta Atualizado");
        alertaAtualizado.setDescricao("Nova descrição");
        alertaAtualizado.setDataHoraDisparo(LocalDateTime.now().plusDays(2));
        alertaAtualizado.setDisparoRecorrente(true);
        alertaAtualizado.setFrequenciaDisparo(FrequenciaDisparo.SEMANAL);

        Alerta alertaSalvo = new Alerta();
        alertaSalvo.setId(id);
        alertaSalvo.setTitulo("Alerta Atualizado");
        alertaSalvo.setDescricao("Nova descrição");
        alertaSalvo.setDataHoraDisparo(alertaAtualizado.getDataHoraDisparo());
        alertaSalvo.setDisparoRecorrente(true);
        alertaSalvo.setFrequenciaDisparo(FrequenciaDisparo.SEMANAL);

        when(alertaRepository.findById(id)).thenReturn(Optional.of(alertaValido));
        when(alertaRepository.save(any(Alerta.class))).thenReturn(alertaSalvo);

        // When
        Optional<AlertaResponseDTO> result = alertaService.update(id, alertaAtualizado);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Alerta Atualizado", result.get().getTitulo());
        assertEquals("Nova descrição", result.get().getDescricao());
        assertTrue(result.get().isDisparoRecorrente());
        assertEquals(FrequenciaDisparo.SEMANAL, result.get().getFrequenciaDisparo());
        
        verify(alertaRepository).findById(id);
        verify(alertaRepository).save(any(Alerta.class));
    }

    @Test
    @DisplayName("Deve retornar Optional vazio ao tentar atualizar alerta inexistente")
    void deveRetornarOptionalVazioAoTentarAtualizarAlertaInexistente() {
        // Given
        Integer idInexistente = 999;
        when(alertaRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // When
        Optional<AlertaResponseDTO> result = alertaService.update(idInexistente, alertaRequestDTO);

        // Then
        assertFalse(result.isPresent());
        
        verify(alertaRepository).findById(idInexistente);
        verify(alertaRepository, never()).save(any(Alerta.class));
    }

    @Test
    @DisplayName("Deve deletar alerta por ID")
    void deveDeletarAlertaPorId() {
        // Given
        Integer id = 1;
        doNothing().when(alertaRepository).deleteById(id);

        // When
        alertaService.delete(id);

        // Then
        verify(alertaRepository).deleteById(id);
    }
} 