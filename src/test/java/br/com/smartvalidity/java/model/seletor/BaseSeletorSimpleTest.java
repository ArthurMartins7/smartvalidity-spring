package br.com.smartvalidity.java.model.seletor;

import br.com.smartvalidity.model.seletor.BaseSeletor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BaseSeletorSimpleTest {

    private BaseSeletor seletor;

    @BeforeEach
    public void setUp() {
        seletor = new BaseSeletor();
    }

    @Test
    public void deveTestarGettersESetters() {
        // When
        seletor.setPagina(2);
        seletor.setLimite(10);

        // Then
        assertEquals(2, seletor.getPagina());
        assertEquals(10, seletor.getLimite());
    }

    @Test
    public void deveValidarTemPaginacao() {
        // Given/When/Then - Sem paginação (valores padrão)
        assertFalse(seletor.temPaginacao());

        // When/Then - Só com limite
        seletor.setLimite(10);
        assertFalse(seletor.temPaginacao());

        // When/Then - Só com página
        seletor.setLimite(0);
        seletor.setPagina(1);
        assertFalse(seletor.temPaginacao());

        // When/Then - Com limite zero
        seletor.setLimite(0);
        seletor.setPagina(1);
        assertFalse(seletor.temPaginacao());

        // When/Then - Com página zero
        seletor.setLimite(10);
        seletor.setPagina(0);
        assertFalse(seletor.temPaginacao());

        // When/Then - Com paginação válida
        seletor.setLimite(10);
        seletor.setPagina(1);
        assertTrue(seletor.temPaginacao());

        // When/Then - Com valores maiores
        seletor.setLimite(50);
        seletor.setPagina(3);
        assertTrue(seletor.temPaginacao());
    }

    @Test
    public void deveValidarStringValida() {
        // When/Then - String nula
        assertFalse(seletor.stringValida(null));

        // When/Then - String vazia
        assertFalse(seletor.stringValida(""));

        // When/Then - String só com espaços
        assertFalse(seletor.stringValida("   "));

        // When/Then - String só com tabs e quebras de linha
        assertFalse(seletor.stringValida("\t\n\r"));

        // When/Then - String válida
        assertTrue(seletor.stringValida("texto"));

        // When/Then - String com espaços mas com conteúdo
        assertTrue(seletor.stringValida("  texto  "));

        // When/Then - String com números
        assertTrue(seletor.stringValida("123"));

        // When/Then - String com caracteres especiais
        assertTrue(seletor.stringValida("@#$%"));
    }
} 