package br.com.smartvalidity.java.model.enums;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.smartvalidity.model.enums.PerfilAcesso;

@DisplayName("Testes do PerfilAcesso")
class PerfilAcessoTest {

    @Test
    @DisplayName("Deve ter valores corretos do enum")
    void deveTerValoresCorretosDoEnum() {
        // When & Then
        assertEquals(2, PerfilAcesso.values().length);
        assertEquals("ADMIN", PerfilAcesso.ADMIN.name());
        assertEquals("OPERADOR", PerfilAcesso.OPERADOR.name());
    }

    @Test
    @DisplayName("Deve converter string para enum")
    void deveConverterStringParaEnum() {
        // When & Then
        assertEquals(PerfilAcesso.ADMIN, PerfilAcesso.valueOf("ADMIN"));
        assertEquals(PerfilAcesso.OPERADOR, PerfilAcesso.valueOf("OPERADOR"));
    }

    @Test
    @DisplayName("Deve manter ordem dos valores")
    void deveManterOrdemDosValores() {
        // When
        PerfilAcesso[] valores = PerfilAcesso.values();

        // Then
        assertEquals(PerfilAcesso.ADMIN, valores[0]);
        assertEquals(PerfilAcesso.OPERADOR, valores[1]);
    }
} 