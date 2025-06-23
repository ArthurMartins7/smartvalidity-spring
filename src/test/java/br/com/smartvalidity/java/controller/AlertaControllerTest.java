package br.com.smartvalidity.java.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.smartvalidity.controller.AlertaController;
import br.com.smartvalidity.model.dto.AlertaRequestDTO;
import br.com.smartvalidity.model.dto.AlertaResponseDTO;
import br.com.smartvalidity.model.enums.FrequenciaDisparo;
import br.com.smartvalidity.service.AlertaService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do AlertaController")
class AlertaControllerTest {

    @Mock
    private AlertaService alertaService;

    @InjectMocks
    private AlertaController alertaController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(alertaController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Deve criar alerta com sucesso")
    void devecriarAlertaComSucesso() throws Exception {
        // Given
        AlertaRequestDTO request = new AlertaRequestDTO();
        request.setTitulo("Alerta Test");
        request.setDescricao("Descrição do alerta");
        request.setFrequenciaDisparo(FrequenciaDisparo.DIARIO);
        request.setDisparoRecorrente(true);

        AlertaResponseDTO response = new AlertaResponseDTO();
        response.setId(1);
        response.setTitulo("Alerta Test");
        response.setDescricao("Descrição do alerta");

        when(alertaService.create(any(AlertaRequestDTO.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/alertas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Alerta Test"));

        verify(alertaService).create(any(AlertaRequestDTO.class));
    }

    @Test
    @DisplayName("Deve buscar todos os alertas")
    void deveBuscarTodosAlertas() throws Exception {
        // Given
        AlertaResponseDTO alerta1 = new AlertaResponseDTO();
        alerta1.setId(1);
        alerta1.setTitulo("Alerta 1");

        AlertaResponseDTO alerta2 = new AlertaResponseDTO();
        alerta2.setId(2);
        alerta2.setTitulo("Alerta 2");

        List<AlertaResponseDTO> alertas = Arrays.asList(alerta1, alerta2);
        when(alertaService.findAll()).thenReturn(alertas);

        // When & Then
        mockMvc.perform(get("/api/alertas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(alertaService).findAll();
    }

    @Test
    @DisplayName("Deve buscar alerta por ID")
    void deveBuscarAlertaPorId() throws Exception {
        // Given
        AlertaResponseDTO response = new AlertaResponseDTO();
        response.setId(1);
        response.setTitulo("Alerta Test");

        when(alertaService.findById(1)).thenReturn(Optional.of(response));

        // When & Then
        mockMvc.perform(get("/api/alertas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Alerta Test"));

        verify(alertaService).findById(1);
    }

    @Test
    @DisplayName("Deve retornar 404 quando alerta não encontrado")
    void deveRetornar404QuandoAlertaNaoEncontrado() throws Exception {
        // Given
        when(alertaService.findById(999)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/alertas/999"))
                .andExpect(status().isNotFound());

        verify(alertaService).findById(999);
    }

    @Test
    @DisplayName("Deve atualizar alerta com sucesso")
    void deveAtualizarAlertaComSucesso() throws Exception {
        // Given
        AlertaRequestDTO request = new AlertaRequestDTO();
        request.setTitulo("Alerta Atualizado");

        AlertaResponseDTO response = new AlertaResponseDTO();
        response.setId(1);
        response.setTitulo("Alerta Atualizado");

        when(alertaService.update(eq(1), any(AlertaRequestDTO.class))).thenReturn(Optional.of(response));

        // When & Then
        mockMvc.perform(put("/api/alertas/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Alerta Atualizado"));

        verify(alertaService).update(eq(1), any(AlertaRequestDTO.class));
    }

    @Test
    @DisplayName("Deve excluir alerta com sucesso")
    void deveExcluirAlertaComSucesso() throws Exception {
        // Given
        doNothing().when(alertaService).delete(1);

        // When & Then
        mockMvc.perform(delete("/api/alertas/1"))
                .andExpect(status().isNoContent());

        verify(alertaService).delete(1);
    }
} 