package br.com.smartvalidity.java.model.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.smartvalidity.model.dto.AlertaRequestDTO;
import br.com.smartvalidity.model.enums.FrequenciaDisparo;

@DisplayName("Testes do AlertaRequestDTO")
class AlertaRequestDTOTest {

    @Test
    @DisplayName("Deve criar AlertaRequestDTO com valores")
    void deveCriarAlertaRequestDTOComValores() {
        // Given
        LocalDateTime dataDisparo = LocalDateTime.now();
        
        // When
        AlertaRequestDTO alerta = new AlertaRequestDTO();
        alerta.setTitulo("Alerta Test");
        alerta.setDescricao("Descrição test");
        alerta.setDataHoraDisparo(dataDisparo);
        alerta.setDisparoRecorrente(true);
        alerta.setFrequenciaDisparo(FrequenciaDisparo.DIARIO);

        // Then
        assertEquals("Alerta Test", alerta.getTitulo());
        assertEquals("Descrição test", alerta.getDescricao());
        assertEquals(dataDisparo, alerta.getDataHoraDisparo());
        assertTrue(alerta.isDisparoRecorrente());
        assertEquals(FrequenciaDisparo.DIARIO, alerta.getFrequenciaDisparo());
    }

    @Test
    @DisplayName("Deve criar AlertaRequestDTO vazio")
    void deveCriarAlertaRequestDTOVazio() {
        // When
        AlertaRequestDTO alerta = new AlertaRequestDTO();

        // Then
        assertNull(alerta.getTitulo());
        assertNull(alerta.getDescricao());
        assertNull(alerta.getDataHoraDisparo());
        assertFalse(alerta.isDisparoRecorrente()); // boolean primitivo = false por padrão
        assertNull(alerta.getFrequenciaDisparo());
    }

    @Test
    @DisplayName("Deve converter para entidade")
    void deveConverterParaEntidade() {
        // Given
        AlertaRequestDTO dto = new AlertaRequestDTO();
        dto.setTitulo("Teste");
        dto.setDescricao("Descrição");
        dto.setDisparoRecorrente(true);
        dto.setFrequenciaDisparo(FrequenciaDisparo.SEMANAL);

        // When
        var entidade = dto.toEntity();

        // Then
        assertNotNull(entidade);
        assertEquals("Teste", entidade.getTitulo());
        assertEquals("Descrição", entidade.getDescricao());
        assertTrue(entidade.isDisparoRecorrente());
        assertEquals(FrequenciaDisparo.SEMANAL, entidade.getFrequenciaDisparo());
    }
} 