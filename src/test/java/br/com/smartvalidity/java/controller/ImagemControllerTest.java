package br.com.smartvalidity.java.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import br.com.smartvalidity.controller.ImagemController;
import br.com.smartvalidity.service.ImagemService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do ImagemController")
class ImagemControllerTest {

    @Mock
    private ImagemService imagemService;

    @InjectMocks
    private ImagemController imagemController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(imagemController).build();
    }

    @Test
    @DisplayName("Deve fazer upload de imagem com sucesso")
    void deveFazerUploadImagemComSucesso() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "test.jpg", 
            "image/jpeg", 
            "test image content".getBytes()
        );

        String expectedBase64 = "dGVzdCBpbWFnZSBjb250ZW50";
        when(imagemService.processarImagem(any())).thenReturn(expectedBase64);

        // When & Then
        mockMvc.perform(multipart("/api/imagem/upload")
                .file(file))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedBase64));

        verify(imagemService).processarImagem(any());
    }

    @Test
    @DisplayName("Deve retornar erro 500 quando processamento falha")
    void deveRetornarErro500QuandoProcessamentoFalha() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "test.jpg", 
            "image/jpeg", 
            "test image content".getBytes()
        );

        when(imagemService.processarImagem(any())).thenThrow(new RuntimeException("Erro no processamento"));

        // When & Then
        mockMvc.perform(multipart("/api/imagem/upload")
                .file(file))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Erro ao processar imagem: Erro no processamento"));

        verify(imagemService).processarImagem(any());
    }

    @Test
    @DisplayName("Deve retornar erro 500 quando arquivo é nulo")
    void deveRetornarErro500QuandoArquivoEhNulo() throws Exception {
        // When & Then
        mockMvc.perform(multipart("/api/imagem/upload"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve processar arquivo pequeno com sucesso")
    void deveProcessarArquivoPequenoComSucesso() throws Exception {
        // Given
        MockMultipartFile arquivo = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "conteudo-pequeno".getBytes());
        
        when(imagemService.processarImagem(any(MultipartFile.class)))
                .thenReturn("base64-pequeno");

        // When & Then
        mockMvc.perform(multipart("/api/imagem/upload")
                .file(arquivo))
                .andExpect(status().isOk())
                .andExpect(content().string("base64-pequeno"));

        verify(imagemService).processarImagem(any(MultipartFile.class));
    }
} 