package br.com.smartvalidity.java.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.service.ImagemService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do ImagemService")
class ImagemServiceTest {

    @InjectMocks
    private ImagemService imagemService;

    @Mock
    private MultipartFile multipartFile;

    @Test
    @DisplayName("Deve processar imagem com sucesso")
    void deveProcessarImagemComSucesso() throws SmartValidityException, IOException {
        // Given
        byte[] imagemBytes = "imagem-teste".getBytes();
        when(multipartFile.getBytes()).thenReturn(imagemBytes);

        // When
        String resultado = imagemService.processarImagem(multipartFile);

        // Then
        assertNotNull(resultado);
        assertFalse(resultado.isEmpty());
        // Verifica se é uma string Base64 válida (deve conter apenas caracteres Base64)
        assertTrue(resultado.matches("^[A-Za-z0-9+/]*={0,2}$"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando falha ao ler arquivo")
    void deveLancarExcecaoQuandoFalhaAoLerArquivo() throws IOException {
        // Given
        when(multipartFile.getBytes()).thenThrow(new IOException("Erro ao ler arquivo"));

        // When & Then
        SmartValidityException exception = assertThrows(SmartValidityException.class, () -> {
            imagemService.processarImagem(multipartFile);
        });

        assertEquals("Erro ao processar arquivo", exception.getMessage());
    }

    @Test
    @DisplayName("Deve processar imagem vazia")
    void deveProcessarImagemVazia() throws SmartValidityException, IOException {
        // Given
        byte[] imagemVazia = new byte[0];
        when(multipartFile.getBytes()).thenReturn(imagemVazia);

        // When
        String resultado = imagemService.processarImagem(multipartFile);

        // Then
        assertNotNull(resultado);
        assertEquals("", resultado); // Base64 de array vazio é string vazia
    }

    @Test
    @DisplayName("Deve processar imagem grande")
    void deveProcessarImagemGrande() throws SmartValidityException, IOException {
        // Given
        byte[] imagemGrande = new byte[1024]; // 1KB
        for (int i = 0; i < imagemGrande.length; i++) {
            imagemGrande[i] = (byte) (i % 256);
        }
        when(multipartFile.getBytes()).thenReturn(imagemGrande);

        // When
        String resultado = imagemService.processarImagem(multipartFile);

        // Then
        assertNotNull(resultado);
        assertFalse(resultado.isEmpty());
        assertTrue(resultado.length() > 1000); // Base64 de 1KB deve ter mais de 1000 chars
    }
} 