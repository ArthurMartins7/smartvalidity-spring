package br.com.smartvalidity.java.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.smartvalidity.exception.GlobalExceptionHandler;
import br.com.smartvalidity.exception.SmartValidityException;

@DisplayName("Testes do GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    @Test
    @DisplayName("Deve criar GlobalExceptionHandler")
    void deveCriarGlobalExceptionHandler() {
        // When
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        // Then
        assertNotNull(handler);
    }

    @Test
    @DisplayName("Deve criar SmartValidityException")
    void deveCriarSmartValidityException() {
        // When
        SmartValidityException exception = new SmartValidityException("Teste");

        // Then
        assertNotNull(exception);
        assertEquals("Teste", exception.getMessage());
    }


} 