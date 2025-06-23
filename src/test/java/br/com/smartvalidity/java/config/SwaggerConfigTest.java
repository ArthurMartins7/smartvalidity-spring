package br.com.smartvalidity.java.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.smartvalidity.config.SwaggerConfig;
import io.swagger.v3.oas.models.OpenAPI;

@DisplayName("Testes do SwaggerConfig")
class SwaggerConfigTest {

    @Test
    @DisplayName("Deve criar configuração OpenAPI")
    void deveCriarConfiguracaoOpenAPI() {
        // Given
        SwaggerConfig config = new SwaggerConfig();

        // When
        OpenAPI openAPI = config.customOpenAPI();

        // Then
        assertNotNull(openAPI);
        assertNotNull(openAPI.getInfo());
        assertEquals("SmartValidity API", openAPI.getInfo().getTitle());
        assertEquals("1.0", openAPI.getInfo().getVersion());
        assertNotNull(openAPI.getInfo().getDescription());
    }

    @Test
    @DisplayName("Deve ter descrição da API")
    void deveTerDescricaoDaAPI() {
        // Given
        SwaggerConfig config = new SwaggerConfig();

        // When
        OpenAPI openAPI = config.customOpenAPI();

        // Then
        String descricao = openAPI.getInfo().getDescription();
        assertNotNull(descricao);
        assertTrue(descricao.contains("SmartValidity"));
    }
} 