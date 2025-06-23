package br.com.smartvalidity.java.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

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

import br.com.smartvalidity.controller.MuralController;
import br.com.smartvalidity.model.dto.MuralDTO;
import br.com.smartvalidity.service.MuralService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do MuralController")
class MuralControllerTest {

    @Mock
    private MuralService muralService;

    @InjectMocks
    private MuralController muralController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(muralController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Deve buscar produtos próximos do vencimento")
    void deveBuscarProximosVencer() throws Exception {
        // Given
        MuralDTO.Listagem item1 = new MuralDTO.Listagem();
        item1.setId("1");
        item1.setItemProduto("Produto 1");

        List<MuralDTO.Listagem> itens = Arrays.asList(item1);
        when(muralService.getProximosVencer()).thenReturn(itens);

        // When & Then
        mockMvc.perform(get("/mural/proximos-vencer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(muralService).getProximosVencer();
    }

    @Test
    @DisplayName("Deve buscar produtos que vencem hoje")
    void deveBuscarVencemHoje() throws Exception {
        // Given
        MuralDTO.Listagem item1 = new MuralDTO.Listagem();
        item1.setId("1");
        item1.setItemProduto("Produto 1");

        List<MuralDTO.Listagem> itens = Arrays.asList(item1);
        when(muralService.getVencemHoje()).thenReturn(itens);

        // When & Then
        mockMvc.perform(get("/mural/vencem-hoje"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(muralService).getVencemHoje();
    }

    @Test
    @DisplayName("Deve buscar produtos vencidos")
    void deveBuscarVencidos() throws Exception {
        // Given
        MuralDTO.Listagem item1 = new MuralDTO.Listagem();
        item1.setId("1");
        item1.setItemProduto("Produto Vencido");

        List<MuralDTO.Listagem> itens = Arrays.asList(item1);
        when(muralService.getVencidos()).thenReturn(itens);

        // When & Then
        mockMvc.perform(get("/mural/vencidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(muralService).getVencidos();
    }

    @Test
    @DisplayName("Deve buscar motivos de inspeção válidos")
    void deveBuscarMotivosInspecaoValidos() throws Exception {
        // Given
        List<String> motivos = Arrays.asList("Avaria/Quebra", "Promoção", "Outro");
        when(muralService.getMotivosInspecaoValidos()).thenReturn(motivos);

        // When & Then
        mockMvc.perform(get("/mural/motivos-inspecao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0]").value("Avaria/Quebra"));

        verify(muralService).getMotivosInspecaoValidos();
    }

    @Test
    @DisplayName("Deve contar páginas com filtro")
    void deveContarPaginas() throws Exception {
        // Given
        MuralDTO.Filtro filtro = new MuralDTO.Filtro();
        when(muralService.contarPaginas(any(MuralDTO.Filtro.class))).thenReturn(5);

        // When & Then
        mockMvc.perform(post("/mural/contar-paginas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filtro)))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));

        verify(muralService).contarPaginas(any(MuralDTO.Filtro.class));
    }

    @Test
    @DisplayName("Deve contar total de registros")
    void deveContarTotalRegistros() throws Exception {
        // Given
        MuralDTO.Filtro filtro = new MuralDTO.Filtro();
        when(muralService.contarTotalRegistros(any(MuralDTO.Filtro.class))).thenReturn(100L);

        // When & Then
        mockMvc.perform(post("/mural/contar-registros")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filtro)))
                .andExpect(status().isOk())
                .andExpect(content().string("100"));

        verify(muralService).contarTotalRegistros(any(MuralDTO.Filtro.class));
    }

    @Test
    @DisplayName("Deve buscar usuários de inspeção")
    void deveBuscarUsuariosInspecao() throws Exception {
        // Given
        List<String> usuarios = Arrays.asList("Usuario1", "Usuario2");
        when(muralService.getUsuariosInspecaoDisponiveis()).thenReturn(usuarios);

        // When & Then
        mockMvc.perform(get("/mural/usuarios-inspecao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(muralService).getUsuariosInspecaoDisponiveis();
    }

    @Test
    @DisplayName("Deve buscar opções de filtro")
    void deveBuscarFiltroOpcoes() throws Exception {
        // Given
        when(muralService.getMarcasDisponiveis()).thenReturn(Arrays.asList("Marca1"));
        when(muralService.getCorredoresDisponiveis()).thenReturn(Arrays.asList("Corredor1"));
        when(muralService.getCategoriasDisponiveis()).thenReturn(Arrays.asList("Categoria1"));
        when(muralService.getFornecedoresDisponiveis()).thenReturn(Arrays.asList("Fornecedor1"));
        when(muralService.getLotesDisponiveis()).thenReturn(Arrays.asList("Lote1"));
        when(muralService.getUsuariosInspecaoDisponiveis()).thenReturn(Arrays.asList("Usuario1"));

        // When & Then
        mockMvc.perform(get("/mural/filtro-opcoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.marcas.length()").value(1))
                .andExpect(jsonPath("$.corredores.length()").value(1));

        verify(muralService).getMarcasDisponiveis();
        verify(muralService).getCorredoresDisponiveis();
    }

    @Test
    @DisplayName("Deve cancelar seleção")
    void deveCancelarSelecao() throws Exception {
        // Given
        List<String> ids = Arrays.asList("1", "2", "3");
        doNothing().when(muralService).cancelarSelecao(anyList());

        // When & Then
        mockMvc.perform(post("/mural/cancelar-selecao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk());

        verify(muralService).cancelarSelecao(anyList());
    }
} 