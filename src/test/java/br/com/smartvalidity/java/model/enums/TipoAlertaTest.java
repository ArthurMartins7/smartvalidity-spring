package br.com.smartvalidity.java.model.enums;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.smartvalidity.model.enums.TipoAlerta;

@DisplayName("Testes do TipoAlerta")
class TipoAlertaTest {

    @Test
    @DisplayName("Deve ter valores corretos do enum")
    void deveTerValoresCorretosDoEnum() {
        // When & Then
        assertEquals(4, TipoAlerta.values().length);
        assertEquals("VENCIMENTO_HOJE", TipoAlerta.VENCIMENTO_HOJE.name());
        assertEquals("VENCIMENTO_AMANHA", TipoAlerta.VENCIMENTO_AMANHA.name());
        assertEquals("VENCIMENTO_ATRASO", TipoAlerta.VENCIMENTO_ATRASO.name());
        assertEquals("PERSONALIZADO", TipoAlerta.PERSONALIZADO.name());
    }

    @Test
    @DisplayName("Deve converter string para enum")
    void deveConverterStringParaEnum() {
        // When & Then
        assertEquals(TipoAlerta.VENCIMENTO_HOJE, TipoAlerta.valueOf("VENCIMENTO_HOJE"));
        assertEquals(TipoAlerta.VENCIMENTO_AMANHA, TipoAlerta.valueOf("VENCIMENTO_AMANHA"));
        assertEquals(TipoAlerta.VENCIMENTO_ATRASO, TipoAlerta.valueOf("VENCIMENTO_ATRASO"));
        assertEquals(TipoAlerta.PERSONALIZADO, TipoAlerta.valueOf("PERSONALIZADO"));
    }

    @Test
    @DisplayName("Deve manter ordem dos valores")
    void deveManterOrdemDosValores() {
        // When
        TipoAlerta[] valores = TipoAlerta.values();

        // Then
        assertEquals(TipoAlerta.VENCIMENTO_HOJE, valores[0]);
        assertEquals(TipoAlerta.VENCIMENTO_AMANHA, valores[1]);
        assertEquals(TipoAlerta.VENCIMENTO_ATRASO, valores[2]);
        assertEquals(TipoAlerta.PERSONALIZADO, valores[3]);
    }
} 