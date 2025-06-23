package br.com.smartvalidity.java.model.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import br.com.smartvalidity.model.dto.AlertaResponseDTO;
import br.com.smartvalidity.model.entity.Alerta;
import br.com.smartvalidity.model.enums.FrequenciaDisparo;

public class AlertaResponseDTOTest {

    @Test
    public void deveTestarGettersESetters() {
        // Given
        LocalDateTime agora = LocalDateTime.now();
        AlertaResponseDTO dto = new AlertaResponseDTO();

        // When
        dto.setId(1);
        dto.setTitulo("Alerta Teste");
        dto.setDescricao("Descrição do alerta");
        dto.setDataHoraDisparo(agora);
        dto.setDisparoRecorrente(true);
        dto.setFrequenciaDisparo(FrequenciaDisparo.DIARIO);

        // Then
        assertEquals(1, dto.getId());
        assertEquals("Alerta Teste", dto.getTitulo());
        assertEquals("Descrição do alerta", dto.getDescricao());
        assertEquals(agora, dto.getDataHoraDisparo());
        assertTrue(dto.isDisparoRecorrente());
        assertEquals(FrequenciaDisparo.DIARIO, dto.getFrequenciaDisparo());
    }

    @Test
    public void deveConverterEntityParaDTO() {
        // Given
        LocalDateTime dataDisparo = LocalDateTime.now().plusDays(1);
        Alerta alerta = new Alerta();
        alerta.setId(10);
        alerta.setTitulo("Produto Vencendo");
        alerta.setDescricao("Produtos próximos do vencimento");
        alerta.setDataHoraDisparo(dataDisparo);
        alerta.setDisparoRecorrente(true);
        alerta.setFrequenciaDisparo(FrequenciaDisparo.SEMANAL);

        // When
        AlertaResponseDTO dto = AlertaResponseDTO.fromEntity(alerta);

        // Then
        assertNotNull(dto);
        assertEquals(10, dto.getId());
        assertEquals("Produto Vencendo", dto.getTitulo());
        assertEquals("Produtos próximos do vencimento", dto.getDescricao());
        assertEquals(dataDisparo, dto.getDataHoraDisparo());
        assertTrue(dto.isDisparoRecorrente());
        assertEquals(FrequenciaDisparo.SEMANAL, dto.getFrequenciaDisparo());
    }

    @Test
    public void deveConverterEntityComDisparoNaoRecorrente() {
        // Given
        Alerta alerta = new Alerta();
        alerta.setId(20);
        alerta.setTitulo("Alerta Único");
        alerta.setDescricao("Alerta de disparo único");
        alerta.setDataHoraDisparo(LocalDateTime.now());
        alerta.setDisparoRecorrente(false);
        alerta.setFrequenciaDisparo(null);

        // When
        AlertaResponseDTO dto = AlertaResponseDTO.fromEntity(alerta);

        // Then
        assertNotNull(dto);
        assertEquals(20, dto.getId());
        assertEquals("Alerta Único", dto.getTitulo());
        assertEquals("Alerta de disparo único", dto.getDescricao());
        assertFalse(dto.isDisparoRecorrente());
        assertNull(dto.getFrequenciaDisparo());
    }

    @Test
    public void deveConverterEntityComValoresNulos() {
        // Given
        Alerta alerta = new Alerta();
        alerta.setId(null);
        alerta.setTitulo(null);
        alerta.setDescricao(null);
        alerta.setDataHoraDisparo(null);
        alerta.setDisparoRecorrente(false);
        alerta.setFrequenciaDisparo(null);

        // When
        AlertaResponseDTO dto = AlertaResponseDTO.fromEntity(alerta);

        // Then
        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getTitulo());
        assertNull(dto.getDescricao());
        assertNull(dto.getDataHoraDisparo());
        assertFalse(dto.isDisparoRecorrente());
        assertNull(dto.getFrequenciaDisparo());
    }
} 