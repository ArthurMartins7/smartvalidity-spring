package br.com.smartvalidity.java.model.enums;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.smartvalidity.model.enums.FrequenciaDisparo;

@DisplayName("Testes do FrequenciaDisparo")
class FrequenciaDisparoTest {

    @Test
    @DisplayName("Deve ter valores corretos do enum")
    void deveTerValoresCorretosDoEnum() {
        // When & Then
        assertEquals(6, FrequenciaDisparo.values().length);
        assertEquals("UNICO", FrequenciaDisparo.UNICO.name());
        assertEquals("DIARIO", FrequenciaDisparo.DIARIO.name());
        assertEquals("SEMANAL", FrequenciaDisparo.SEMANAL.name());
        assertEquals("QUINZENAL", FrequenciaDisparo.QUINZENAL.name());
        assertEquals("MENSAL", FrequenciaDisparo.MENSAL.name());
        assertEquals("ANUAL", FrequenciaDisparo.ANUAL.name());
    }

    @Test
    @DisplayName("Deve converter string para enum")
    void deveConverterStringParaEnum() {
        // When & Then
        assertEquals(FrequenciaDisparo.UNICO, FrequenciaDisparo.valueOf("UNICO"));
        assertEquals(FrequenciaDisparo.DIARIO, FrequenciaDisparo.valueOf("DIARIO"));
        assertEquals(FrequenciaDisparo.SEMANAL, FrequenciaDisparo.valueOf("SEMANAL"));
        assertEquals(FrequenciaDisparo.QUINZENAL, FrequenciaDisparo.valueOf("QUINZENAL"));
        assertEquals(FrequenciaDisparo.MENSAL, FrequenciaDisparo.valueOf("MENSAL"));
        assertEquals(FrequenciaDisparo.ANUAL, FrequenciaDisparo.valueOf("ANUAL"));
    }

    @Test
    @DisplayName("Deve manter ordem dos valores")
    void deveManterOrdemDosValores() {
        // When
        FrequenciaDisparo[] valores = FrequenciaDisparo.values();

        // Then
        assertEquals(FrequenciaDisparo.UNICO, valores[0]);
        assertEquals(FrequenciaDisparo.DIARIO, valores[1]);
        assertEquals(FrequenciaDisparo.SEMANAL, valores[2]);
        assertEquals(FrequenciaDisparo.QUINZENAL, valores[3]);
        assertEquals(FrequenciaDisparo.MENSAL, valores[4]);
        assertEquals(FrequenciaDisparo.ANUAL, valores[5]);
    }
} 