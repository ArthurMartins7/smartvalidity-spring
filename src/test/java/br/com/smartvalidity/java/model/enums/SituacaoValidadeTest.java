package br.com.smartvalidity.java.model.enums;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.smartvalidity.model.enums.SituacaoValidade;

@DisplayName("Testes do SituacaoValidade")
class SituacaoValidadeTest {

    @Test
    @DisplayName("Deve ter valores corretos do enum")
    void deveTerValoresCorretosDoEnum() {
        // When & Then
        assertEquals(4, SituacaoValidade.values().length);
        assertEquals("VENCIDO", SituacaoValidade.VENCIDO.name());
        assertEquals("PROXIMO_DE_VENCER", SituacaoValidade.PROXIMO_DE_VENCER.name());
        assertEquals("VENCE_HOJE", SituacaoValidade.VENCE_HOJE.name());
        assertEquals("OK", SituacaoValidade.OK.name());
    }

    @Test
    @DisplayName("Deve converter string para enum")
    void deveConverterStringParaEnum() {
        // When & Then
        assertEquals(SituacaoValidade.VENCIDO, SituacaoValidade.valueOf("VENCIDO"));
        assertEquals(SituacaoValidade.PROXIMO_DE_VENCER, SituacaoValidade.valueOf("PROXIMO_DE_VENCER"));
        assertEquals(SituacaoValidade.VENCE_HOJE, SituacaoValidade.valueOf("VENCE_HOJE"));
        assertEquals(SituacaoValidade.OK, SituacaoValidade.valueOf("OK"));
    }

    @Test
    @DisplayName("Deve manter ordem dos valores")
    void deveManterOrdemDosValores() {
        // When
        SituacaoValidade[] valores = SituacaoValidade.values();

        // Then
        assertEquals(SituacaoValidade.VENCIDO, valores[0]);
        assertEquals(SituacaoValidade.PROXIMO_DE_VENCER, valores[1]);
        assertEquals(SituacaoValidade.VENCE_HOJE, valores[2]);
        assertEquals(SituacaoValidade.OK, valores[3]);
    }
} 